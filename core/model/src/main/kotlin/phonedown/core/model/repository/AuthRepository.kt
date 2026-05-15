package phonedown.core.model.repository

import kotlinx.coroutines.flow.Flow
import phonedown.core.model.AccountState
import phonedown.core.model.GoogleAccount

interface AuthRepository {
    val accountState: Flow<AccountState>

    suspend fun applyGoogleAccount(account: GoogleAccount)

    suspend fun signOut()
}
