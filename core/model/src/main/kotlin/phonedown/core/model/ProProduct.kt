package phonedown.core.model

enum class ProProductType {
    Monthly,
    Yearly,
    Lifetime,
}

data class ProProduct(
    val id: String,
    val type: ProProductType,
    val priceAmountMicros: Long,
    val formattedPrice: String,
    val billingPeriod: String?,
)
