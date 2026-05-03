package phonedown.core.model

sealed class AccountState {
    data object SignedOut : AccountState()

    data class SignedIn(
        val displayName: String?,
        val email: String?,
        val photoUrl: String?,
    ) : AccountState()
}

fun AccountState.isSignedIn(): Boolean = this is AccountState.SignedIn
