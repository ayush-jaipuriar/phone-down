package phonedown.core.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import phonedown.core.model.AccountState
import phonedown.core.model.GoogleAccount
import phonedown.core.model.repository.AuthRepository

/**
 * Fake auth repository for development and UI testing.
 *
 * Simulates Google Sign-In with a mock account. Not for production use.
 */
class FakeAuthRepository : AuthRepository {
    private val _accountState = MutableStateFlow<AccountState>(AccountState.SignedOut)
    override val accountState: Flow<AccountState> = _accountState.asStateFlow()

    suspend fun signIn() {
        delay(1_500)
        applyGoogleAccount(
            GoogleAccount(
                accountId = "fake-test-user",
                displayName = "Test User",
                email = "test@example.com",
                photoUrl = null,
            ),
        )
    }

    override suspend fun applyGoogleAccount(account: GoogleAccount) {
        _accountState.value =
            AccountState.SignedIn(
                displayName = account.displayName,
                email = account.email,
                photoUrl = account.photoUrl,
                accountId = account.accountId,
            )
    }

    override suspend fun signOut() {
        delay(500)
        _accountState.value = AccountState.SignedOut
    }
}
