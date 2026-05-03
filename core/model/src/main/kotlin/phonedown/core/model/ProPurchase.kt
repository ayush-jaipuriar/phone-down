package phonedown.core.model

enum class ProPurchaseState {
    Pending,
    Completed,
    Acknowledged,
}

data class ProPurchase(
    val productId: String,
    val purchaseToken: String,
    val state: ProPurchaseState,
    val purchaseTimeMillis: Long,
)
