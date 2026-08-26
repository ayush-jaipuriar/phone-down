package phonedown.core.billing

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import phonedown.core.model.BillingEvent
import phonedown.core.model.PRO_LIFETIME_PRODUCT_ID
import phonedown.core.model.PRO_MONTHLY_PRODUCT_ID
import phonedown.core.model.PRO_YEARLY_PRODUCT_ID
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType
import phonedown.core.model.ProPurchase
import phonedown.core.model.ProPurchaseState
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.EntitlementCache

/**
 * Fake billing repository for development and UI testing.
 *
 * Returns hardcoded products and simulates purchase flow with a 2-second delay.
 * Reads initial entitlement from cache if available.
 * Not for production use.
 */
class FakeBillingRepository(
    private val cache: EntitlementCache? = null,
) : BillingRepository {
    private val _products =
        MutableStateFlow(
            listOf(
                ProProduct(
                    id = PRO_MONTHLY_PRODUCT_ID,
                    type = ProProductType.Monthly,
                    priceAmountMicros = 4_990_000,
                    formattedPrice = "$4.99",
                    billingPeriod = "P1M",
                ),
                ProProduct(
                    id = PRO_YEARLY_PRODUCT_ID,
                    type = ProProductType.Yearly,
                    priceAmountMicros = 29_990_000,
                    formattedPrice = "$29.99",
                    billingPeriod = "P1Y",
                ),
                ProProduct(
                    id = PRO_LIFETIME_PRODUCT_ID,
                    type = ProProductType.Lifetime,
                    priceAmountMicros = 79_990_000,
                    formattedPrice = "$79.99",
                    billingPeriod = null,
                ),
            ),
        )
    override val products: Flow<List<ProProduct>> = _products.asStateFlow()

    private val _purchases = MutableStateFlow<List<ProPurchase>>(emptyList())
    override val purchases: Flow<List<ProPurchase>> = _purchases.asStateFlow()
    private val _events = MutableSharedFlow<BillingEvent>(extraBufferCapacity = 4)
    override val events: Flow<BillingEvent> = _events.asSharedFlow()

    private val _entitlement = MutableStateFlow<ProEntitlement>(ProEntitlement.Free)
    override val entitlement: Flow<ProEntitlement> = _entitlement.asStateFlow()

    init {
        // Read cached entitlement on initialization if available
        kotlinx.coroutines.runBlocking {
            cache?.read()?.let { cached ->
                _entitlement.value = cached
            }
        }
    }

    override suspend fun loadProducts() {
        // Products are already loaded in this fake implementation.
    }

    override suspend fun launchPurchaseFlow(product: ProProduct) {
        delay(2_000)
        val purchase =
            ProPurchase(
                productId = product.id,
                purchaseToken = "fake_token_${System.currentTimeMillis()}",
                state = ProPurchaseState.Acknowledged,
                purchaseTimeMillis = System.currentTimeMillis(),
            )
        _purchases.value = _purchases.value + purchase
        val newEntitlement =
            ProEntitlement.Pro(
                expiryDateMillis = if (product.type == ProProductType.Lifetime) null else System.currentTimeMillis() + 365L.daysInMillis(),
            )
        _entitlement.value = newEntitlement
        cache?.write(newEntitlement)
        _events.tryEmit(BillingEvent.PurchaseCompleted(product.id))
    }

    override suspend fun restorePurchases() {
        delay(1_000)
        if (_purchases.value.isNotEmpty()) {
            val latestPurchase = _purchases.value.maxByOrNull { it.purchaseTimeMillis }
            if (latestPurchase != null) {
                val product = _products.value.find { it.id == latestPurchase.productId }
                val newEntitlement =
                    ProEntitlement.Pro(
                        expiryDateMillis =
                            if (product?.type ==
                                ProProductType.Lifetime
                            ) {
                                null
                            } else {
                                System.currentTimeMillis() + 365L.daysInMillis()
                            },
                    )
                _entitlement.value = newEntitlement
                cache?.write(newEntitlement)
                _events.tryEmit(BillingEvent.RestoreCompleted(_purchases.value.size))
            }
        } else {
            _events.tryEmit(BillingEvent.RestoreNoPurchases)
        }
    }

    override suspend fun syncPurchases() {
        val cached = cache?.read()
        if (cached != null) {
            _entitlement.value = cached
        } else if (_purchases.value.isEmpty()) {
            _entitlement.value = ProEntitlement.Free
            cache?.write(ProEntitlement.Free)
        }
    }

    override suspend fun acknowledgePurchase(purchaseToken: String) {
        _purchases.value =
            _purchases.value.map { purchase ->
                if (purchase.purchaseToken == purchaseToken) {
                    purchase.copy(state = ProPurchaseState.Acknowledged)
                } else {
                    purchase
                }
            }
    }

    private fun Long.daysInMillis(): Long = this * 24 * 60 * 60 * 1000
}
