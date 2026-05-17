package phonedown.core.model

const val PRO_MONTHLY_PRODUCT_ID = "pro_monthly"
const val PRO_YEARLY_PRODUCT_ID = "pro_yearly"
const val PRO_LIFETIME_PRODUCT_ID = "pro_lifetime"

val PRO_SUBSCRIPTION_PRODUCT_IDS =
    setOf(
        PRO_MONTHLY_PRODUCT_ID,
        PRO_YEARLY_PRODUCT_ID,
    )

fun String.isSubscriptionProductId(): Boolean = this in PRO_SUBSCRIPTION_PRODUCT_IDS
