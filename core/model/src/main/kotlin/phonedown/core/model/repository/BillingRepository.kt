package phonedown.core.model.repository

import kotlinx.coroutines.flow.Flow
import phonedown.core.model.BillingEvent
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProPurchase

interface BillingRepository {
    val products: Flow<List<ProProduct>>
    val purchases: Flow<List<ProPurchase>>
    val entitlement: Flow<ProEntitlement>
    val events: Flow<BillingEvent>

    suspend fun loadProducts()

    suspend fun launchPurchaseFlow(product: ProProduct)

    suspend fun restorePurchases()

    suspend fun syncPurchases()

    suspend fun acknowledgePurchase(purchaseToken: String)
}
