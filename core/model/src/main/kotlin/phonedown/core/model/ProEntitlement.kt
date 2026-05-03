package phonedown.core.model

sealed class ProEntitlement {
    data object Free : ProEntitlement()

    data class Pro(
        val expiryDateMillis: Long? = null,
    ) : ProEntitlement()
}

fun ProEntitlement.isPro(): Boolean = this is ProEntitlement.Pro
