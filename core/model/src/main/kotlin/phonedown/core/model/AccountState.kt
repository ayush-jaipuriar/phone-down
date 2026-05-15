package phonedown.core.model

sealed class AccountState {
    data object SignedOut : AccountState()

    data class SignedIn(
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
        val accountId: String? = null,
    ) : AccountState()
}

fun AccountState.isSignedIn(): Boolean = this is AccountState.SignedIn
