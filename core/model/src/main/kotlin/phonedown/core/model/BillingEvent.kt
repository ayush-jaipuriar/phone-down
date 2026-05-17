package phonedown.core.model

sealed class BillingEvent {
    data class PurchaseCompleted(
        val productId: String,
    ) : BillingEvent()

    data class PurchasePending(
        val productId: String,
    ) : BillingEvent()

    data object PurchaseCancelled : BillingEvent()

    data class PurchaseFailed(
        val message: String,
    ) : BillingEvent()

    data class RestoreCompleted(
        val restoredPurchaseCount: Int,
    ) : BillingEvent()

    data object RestoreNoPurchases : BillingEvent()

    data class RestoreFailed(
        val message: String,
    ) : BillingEvent()
}
