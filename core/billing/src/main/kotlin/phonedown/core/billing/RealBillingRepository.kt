package phonedown.core.billing

import android.content.Context
import android.util.Log
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClient.ProductType
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import phonedown.core.model.BillingEvent
import phonedown.core.model.PRO_LIFETIME_PRODUCT_ID
import phonedown.core.model.PRO_MONTHLY_PRODUCT_ID
import phonedown.core.model.PRO_YEARLY_PRODUCT_ID
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType
import phonedown.core.model.ProPurchase
import phonedown.core.model.ProPurchaseState
import phonedown.core.model.isSubscriptionProductId
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.EntitlementCache
import kotlin.coroutines.resume

private const val TAG = "RealBillingRepository"

class RealBillingRepository(
    context: Context,
    private val cache: EntitlementCache,
    private val activityProvider: BillingActivityProvider,
) : BillingRepository,
    PurchasesUpdatedListener {
    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connectionMutex = Mutex()
    private val productDetailsById = java.util.concurrent.ConcurrentHashMap<String, ProductDetails>()

    private val _products = MutableStateFlow<List<ProProduct>>(emptyList())
    override val products: Flow<List<ProProduct>> = _products.asStateFlow()

    private val _purchases = MutableStateFlow<List<ProPurchase>>(emptyList())
    override val purchases: Flow<List<ProPurchase>> = _purchases.asStateFlow()

    private val _entitlement = MutableStateFlow<ProEntitlement>(ProEntitlement.Free)
    override val entitlement: Flow<ProEntitlement> = _entitlement.asStateFlow()

    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 8)
    override val events: Flow<BillingEvent> = _events.asSharedFlow()

    private val billingClient =
        BillingClient
            .newBuilder(context.applicationContext)
            .setListener(this)
            .enablePendingPurchases(
                PendingPurchasesParams
                    .newBuilder()
                    .enableOneTimeProducts()
                    .build(),
            ).build()

    init {
        repositoryScope.launch {
            cache.read()?.let { cachedEntitlement ->
                _entitlement.value = cachedEntitlement
            }
        }
    }

    override suspend fun loadProducts() {
        ensureConnected()

        val subscriptionDetails =
            queryProductDetails(
                productIds = listOf(PRO_MONTHLY_PRODUCT_ID, PRO_YEARLY_PRODUCT_ID),
                productType = ProductType.SUBS,
            )
        val lifetimeDetails =
            queryProductDetails(
                productIds = listOf(PRO_LIFETIME_PRODUCT_ID),
                productType = ProductType.INAPP,
            )

        val allDetails = subscriptionDetails + lifetimeDetails
        productDetailsById.clear()
        allDetails.forEach { detail ->
            productDetailsById[detail.productId] = detail
        }

        val mappedProducts =
            allDetails
                .mapNotNull(::toProProduct)
                .sortedBy { product ->
                    when (product.type) {
                        ProProductType.Monthly -> 0
                        ProProductType.Yearly -> 1
                        ProProductType.Lifetime -> 2
                    }
                }

        if (mappedProducts.isEmpty()) {
            throw IllegalStateException("No Play Billing products are available yet. Finish Play Console product setup and try again.")
        }

        _products.value = mappedProducts
    }

    override suspend fun launchPurchaseFlow(product: ProProduct) {
        ensureConnected()
        if (_products.value.none { it.id == product.id }) {
            loadProducts()
        }

        val activity =
            activityProvider.currentActivity()
                ?: throw IllegalStateException("Open the paywall while the app is visible and try again.")
        val productDetails =
            productDetailsById[product.id]
                ?: throw IllegalStateException("That Play product is unavailable right now. Try again in a moment.")

        val billingParams = createBillingFlowParams(product, productDetails)
        val billingResult = billingClient.launchBillingFlow(activity, billingParams)
        if (billingResult.responseCode != BillingResponseCode.OK) {
            throw IllegalStateException(billingResult.toFriendlyMessage(defaultMessage = "Unable to start the purchase flow right now."))
        }
    }

    override suspend fun restorePurchases() {
        ensureConnected()
        try {
            val activePurchases = queryAllActivePurchases()
            applyPurchases(activePurchases)
            if (activePurchases.isEmpty()) {
                _events.emit(BillingEvent.RestoreNoPurchases)
            } else {
                _events.emit(BillingEvent.RestoreCompleted(activePurchases.size))
            }
        } catch (exception: Exception) {
            _events.emit(BillingEvent.RestoreFailed(exception.message ?: "Unable to restore purchases right now."))
        }
    }

    override suspend fun syncPurchases() {
        try {
            ensureConnected()
            val activePurchases = queryAllActivePurchases()
            applyPurchases(activePurchases)
        } catch (exception: Exception) {
            Log.w(TAG, "Billing sync failed; keeping cached entitlement if available.", exception)
        }
    }

    override suspend fun acknowledgePurchase(purchaseToken: String) {
        ensureConnected()
        val billingResult =
            suspendCancellableCoroutine<BillingResult> { continuation ->
                val params =
                    AcknowledgePurchaseParams
                        .newBuilder()
                        .setPurchaseToken(purchaseToken)
                        .build()
                billingClient.acknowledgePurchase(params) { result ->
                    if (continuation.isActive) {
                        continuation.resume(result)
                    }
                }
            }
        if (billingResult.responseCode != BillingResponseCode.OK) {
            throw IllegalStateException(billingResult.toFriendlyMessage(defaultMessage = "Purchase acknowledgment failed."))
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?,
    ) {
        when (billingResult.responseCode) {
            BillingResponseCode.OK -> {
                if (purchases.isNullOrEmpty()) {
                    repositoryScope.launch {
                        _events.emit(BillingEvent.PurchaseFailed("Google Play did not return purchase details. Please try again."))
                    }
                } else {
                    repositoryScope.launch {
                        try {
                            applyPurchases(purchases)
                            purchases
                                .mapNotNull { it.products.firstOrNull() }
                                .forEach { productId ->
                                    val mappedState = _purchases.value.firstOrNull { it.productId == productId }?.state
                                    when (mappedState) {
                                        ProPurchaseState.Pending -> _events.emit(BillingEvent.PurchasePending(productId))
                                        ProPurchaseState.Acknowledged,
                                        ProPurchaseState.Completed,
                                        null,
                                        -> _events.emit(BillingEvent.PurchaseCompleted(productId))
                                    }
                                }
                        } catch (exception: Exception) {
                            _events.emit(BillingEvent.PurchaseFailed(exception.message ?: "Purchase processing failed."))
                        }
                    }
                }
            }
            BillingResponseCode.USER_CANCELED -> repositoryScope.launch { _events.emit(BillingEvent.PurchaseCancelled) }
            else ->
                repositoryScope.launch {
                    _events.emit(
                        BillingEvent.PurchaseFailed(
                            billingResult.toFriendlyMessage(defaultMessage = "Google Play could not complete the purchase."),
                        ),
                    )
                }
        }
    }

    private suspend fun ensureConnected() {
        connectionMutex.withLock {
            if (billingClient.isReady) {
                return
            }
            val connectionResult =
                suspendCancellableCoroutine<BillingResult> { continuation ->
                    billingClient.startConnection(
                        object : BillingClientStateListener {
                            override fun onBillingServiceDisconnected() {
                                if (continuation.isActive) {
                                    continuation.resume(
                                        BillingResult
                                            .newBuilder()
                                            .setResponseCode(BillingResponseCode.SERVICE_DISCONNECTED)
                                            .setDebugMessage("Billing service disconnected.")
                                            .build(),
                                    )
                                }
                            }

                            override fun onBillingSetupFinished(billingResult: BillingResult) {
                                if (continuation.isActive) {
                                    continuation.resume(billingResult)
                                }
                            }
                        },
                    )
                }
            if (connectionResult.responseCode != BillingResponseCode.OK) {
                throw IllegalStateException(
                    connectionResult.toFriendlyMessage(defaultMessage = "Google Play Billing is unavailable right now."),
                )
            }
        }
    }

    private suspend fun queryProductDetails(
        productIds: List<String>,
        productType: String,
    ): List<ProductDetails> {
        if (productIds.isEmpty()) {
            return emptyList()
        }
        val params =
            QueryProductDetailsParams
                .newBuilder()
                .setProductList(
                    productIds.map { productId ->
                        QueryProductDetailsParams
                            .Product
                            .newBuilder()
                            .setProductId(productId)
                            .setProductType(productType)
                            .build()
                    },
                ).build()

        return suspendCancellableCoroutine { continuation ->
            billingClient.queryProductDetailsAsync(params) { billingResult, queryResult ->
                if (billingResult.responseCode != BillingResponseCode.OK) {
                    continuation.resumeWith(
                        Result.failure(
                            IllegalStateException(billingResult.toFriendlyMessage(defaultMessage = "Could not load Play products.")),
                        ),
                    )
                    return@queryProductDetailsAsync
                }
                continuation.resume(queryResult.productDetailsList)
            }
        }
    }

    private suspend fun queryAllActivePurchases(): List<Purchase> {
        val subscriptions = queryPurchases(ProductType.SUBS)
        val oneTimePurchases = queryPurchases(ProductType.INAPP)
        return (subscriptions + oneTimePurchases)
            .distinctBy { it.purchaseToken }
    }

    private suspend fun queryPurchases(productType: String): List<Purchase> {
        val params =
            QueryPurchasesParams
                .newBuilder()
                .setProductType(productType)
                .build()
        return suspendCancellableCoroutine { continuation ->
            billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
                if (billingResult.responseCode != BillingResponseCode.OK) {
                    continuation.resumeWith(
                        Result.failure(
                            IllegalStateException(
                                billingResult.toFriendlyMessage(defaultMessage = "Could not refresh purchases from Google Play."),
                            ),
                        ),
                    )
                    return@queryPurchasesAsync
                }
                continuation.resume(purchases)
            }
        }
    }

    private suspend fun applyPurchases(purchases: List<Purchase>) {
        val mappedPurchases = mutableListOf<ProPurchase>()
        for (purchase in purchases) {
            val productId = purchase.products.firstOrNull() ?: continue
            val mappedState =
                when (purchase.purchaseState) {
                    Purchase.PurchaseState.PENDING -> ProPurchaseState.Pending
                    Purchase.PurchaseState.PURCHASED -> {
                        if (!purchase.isAcknowledged) {
                            acknowledgePurchase(purchase.purchaseToken)
                        }
                        ProPurchaseState.Acknowledged
                    }
                    else -> continue
                }
            mappedPurchases +=
                ProPurchase(
                    productId = productId,
                    purchaseToken = purchase.purchaseToken,
                    state = mappedState,
                    purchaseTimeMillis = purchase.purchaseTime,
                )
        }

        _purchases.value = mappedPurchases
        val resolvedEntitlement = resolveEntitlement(mappedPurchases)
        _entitlement.value = resolvedEntitlement
        cache.write(resolvedEntitlement)
    }

    private fun resolveEntitlement(purchases: List<ProPurchase>): ProEntitlement {
        val activePurchases =
            purchases.filter { purchase ->
                purchase.state == ProPurchaseState.Acknowledged || purchase.state == ProPurchaseState.Completed
            }
        return when {
            activePurchases.any { it.productId == PRO_LIFETIME_PRODUCT_ID } -> ProEntitlement.Pro(expiryDateMillis = null)
            activePurchases.any { it.productId.isSubscriptionProductId() } -> ProEntitlement.Pro(expiryDateMillis = null)
            else -> ProEntitlement.Free
        }
    }

    private fun createBillingFlowParams(
        product: ProProduct,
        productDetails: ProductDetails,
    ): BillingFlowParams {
        val productDetailsParamsBuilder =
            BillingFlowParams
                .ProductDetailsParams
                .newBuilder()
                .setProductDetails(productDetails)

        if (product.type != ProProductType.Lifetime) {
            val offerToken =
                productDetails.subscriptionOfferDetails
                    ?.firstOrNull()
                    ?.offerToken
                    ?: throw IllegalStateException("This subscription is missing a purchasable offer in Google Play Console.")
            productDetailsParamsBuilder.setOfferToken(offerToken)
        }

        return BillingFlowParams
            .newBuilder()
            .setProductDetailsParamsList(listOf(productDetailsParamsBuilder.build()))
            .build()
    }

    private fun toProProduct(details: ProductDetails): ProProduct? =
        when (details.productId) {
            PRO_MONTHLY_PRODUCT_ID,
            PRO_YEARLY_PRODUCT_ID,
            -> {
                val phase =
                    details.subscriptionOfferDetails
                        ?.firstOrNull()
                        ?.pricingPhases
                        ?.pricingPhaseList
                        ?.lastOrNull()
                        ?: return null
                ProProduct(
                    id = details.productId,
                    type = if (details.productId == PRO_MONTHLY_PRODUCT_ID) ProProductType.Monthly else ProProductType.Yearly,
                    priceAmountMicros = phase.priceAmountMicros,
                    formattedPrice = phase.formattedPrice,
                    billingPeriod = phase.billingPeriod,
                )
            }

            PRO_LIFETIME_PRODUCT_ID -> {
                val offerDetails = details.oneTimePurchaseOfferDetails ?: return null
                ProProduct(
                    id = details.productId,
                    type = ProProductType.Lifetime,
                    priceAmountMicros = offerDetails.priceAmountMicros,
                    formattedPrice = offerDetails.formattedPrice,
                    billingPeriod = null,
                )
            }

            else -> null
        }
}

private fun BillingResult.toFriendlyMessage(defaultMessage: String): String =
    when (responseCode) {
        BillingResponseCode.BILLING_UNAVAILABLE -> "Google Play Billing is unavailable on this device right now."
        BillingResponseCode.ITEM_UNAVAILABLE -> "That product is not available in Google Play yet."
        BillingResponseCode.NETWORK_ERROR -> "Google Play Billing hit a network error. Please try again."
        BillingResponseCode.SERVICE_DISCONNECTED,
        BillingResponseCode.SERVICE_UNAVAILABLE,
        -> "Google Play Billing is temporarily unavailable. Please try again."
        BillingResponseCode.DEVELOPER_ERROR -> "The Play Billing configuration is incomplete. Finish Play Console setup and try again."
        BillingResponseCode.FEATURE_NOT_SUPPORTED -> "This device does not support the required Google Play Billing feature."
        else -> debugMessage.takeIf { it.isNotBlank() } ?: defaultMessage
    }
