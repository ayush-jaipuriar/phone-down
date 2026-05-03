package phonedown.core.model.repository

import kotlinx.coroutines.flow.Flow
import phonedown.core.model.AccountState

interface AuthRepository {
    val accountState: Flow<AccountState>

    suspend fun signIn()

    suspend fun signOut()

    fun getAuthToken(): String?
}
