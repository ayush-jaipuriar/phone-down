package phonedown.app.runtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import phonedown.core.model.BillingEvent
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ProProduct
import phonedown.core.model.ProPurchase
import phonedown.core.model.repository.BillingRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FreeAccessBillingRepository
    @Inject
    constructor() : BillingRepository {
        override val products: Flow<List<ProProduct>> = flowOf(emptyList())
        override val purchases: Flow<List<ProPurchase>> = flowOf(emptyList())
        override val entitlement: Flow<ProEntitlement> = flowOf(ProEntitlement.Pro())
        override val events: Flow<BillingEvent> = emptyFlow()

        override suspend fun loadProducts() = Unit

        override suspend fun launchPurchaseFlow(product: ProProduct): Nothing =
            error("Unsupported operation in public free mode")

        override suspend fun restorePurchases() = Unit

        override suspend fun syncPurchases() = Unit

        override suspend fun acknowledgePurchase(purchaseToken: String) = Unit
    }
