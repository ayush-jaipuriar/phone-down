package phonedown.core.billing

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProProductType
import phonedown.core.model.ProPurchase
import phonedown.core.model.ProPurchaseState
import phonedown.core.model.repository.BillingRepository

/**
 * Fake billing repository for development and UI testing.
 *
 * Returns hardcoded products and simulates purchase flow with a 2-second delay.
 * Not for production use.
 */
class FakeBillingRepository : BillingRepository {

    private val _products = MutableStateFlow(
        listOf(
            ProProduct(
                id = "phone_down_pro_monthly",
                type = ProProductType.Monthly,
                priceAmountMicros = 4_990_000,
                formattedPrice = "$4.99",
                billingPeriod = "P1M",
            ),
            ProProduct(
                id = "phone_down_pro_yearly",
                type = ProProductType.Yearly,
                priceAmountMicros = 29_990_000,
                formattedPrice = "$29.99",
                billingPeriod = "P1Y",
            ),
            ProProduct(
                id = "phone_down_pro_lifetime",
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

    private val _entitlement = MutableStateFlow<ProEntitlement>(ProEntitlement.Free)
    override val entitlement: Flow<ProEntitlement> = _entitlement.asStateFlow()

    override suspend fun loadProducts() {
        // Products are already loaded in this fake implementation.
    }

    override suspend fun launchPurchaseFlow(product: ProProduct) {
        delay(2_000)
        val purchase = ProPurchase(
            productId = product.id,
            purchaseToken = "fake_token_${System.currentTimeMillis()}",
            state = ProPurchaseState.Acknowledged,
            purchaseTimeMillis = System.currentTimeMillis(),
        )
        _purchases.value = _purchases.value + purchase
        _entitlement.value = ProEntitlement.Pro(
            expiryDateMillis = if (product.type == ProProductType.Lifetime) null else System.currentTimeMillis() + 365L.daysInMillis(),
        )
    }

    override suspend fun restorePurchases() {
        delay(1_000)
        if (_purchases.value.isNotEmpty()) {
            val latestPurchase = _purchases.value.maxByOrNull { it.purchaseTimeMillis }
            if (latestPurchase != null) {
                val product = _products.value.find { it.id == latestPurchase.productId }
                _entitlement.value = ProEntitlement.Pro(
                    expiryDateMillis = if (product?.type == ProProductType.Lifetime) null else System.currentTimeMillis() + 365L.daysInMillis(),
                )
            }
        }
    }

    override suspend fun acknowledgePurchase(purchaseToken: String) {
        _purchases.value = _purchases.value.map { purchase ->
            if (purchase.purchaseToken == purchaseToken) {
                purchase.copy(state = ProPurchaseState.Acknowledged)
            } else {
                purchase
            }
        }
    }

    private fun Long.daysInMillis(): Long = this * 24 * 60 * 60 * 1000
}
