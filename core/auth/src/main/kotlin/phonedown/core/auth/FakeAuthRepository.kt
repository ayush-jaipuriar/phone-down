package phonedown.core.auth

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import phonedown.core.model.AccountState
import phonedown.core.model.repository.AuthRepository

/**
 * Fake auth repository for development and UI testing.
 *
 * Simulates Google Sign-In with a mock account. Not for production use.
 */
class FakeAuthRepository : AuthRepository {

    private val _accountState = MutableStateFlow<AccountState>(AccountState.SignedOut)
    override val accountState: Flow<AccountState> = _accountState.asStateFlow()

    private var authToken: String? = null

    override suspend fun signIn() {
        delay(1_500)
        _accountState.value = AccountState.SignedIn(
            displayName = "Test User",
            email = "test@example.com",
            photoUrl = null,
        )
        authToken = "fake_auth_token_${System.currentTimeMillis()}"
    }

    override suspend fun signOut() {
        delay(500)
        _accountState.value = AccountState.SignedOut
        authToken = null
    }

    override fun getAuthToken(): String? = authToken
}
