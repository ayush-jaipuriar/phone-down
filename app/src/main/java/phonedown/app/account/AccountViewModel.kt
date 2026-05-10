package phonedown.app.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import phonedown.core.model.AccountState
import phonedown.core.model.ProEntitlement
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BillingRepository
import javax.inject.Inject

@HiltViewModel
class AccountViewModel
    @Inject
    constructor(
        private val authRepository: AuthRepository,
        billingRepository: BillingRepository,
        private val restoreBackupUseCase: BackupRestorer,
    ) : ViewModel() {
        private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
        val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()

        val uiState: StateFlow<AccountUiState> =
            combine(
                authRepository.accountState,
                billingRepository.entitlement,
            ) { accountState, entitlement ->
                AccountUiState(
                    accountState = accountState,
                    isProUser = entitlement is ProEntitlement.Pro,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = AccountUiState(),
            )

        fun signIn() {
            viewModelScope.launch { authRepository.signIn() }
        }

        fun signOut() {
            viewModelScope.launch { authRepository.signOut() }
        }

        fun restoreBackup() {
            viewModelScope.launch {
                _restoreState.value = RestoreState.InProgress
                val result = restoreBackupUseCase()
                _restoreState.value =
                    when (result) {
                        is RestoreBackupOutcome.Success ->
                            RestoreState.Success(result.sessionsRestored)
                        is RestoreBackupOutcome.Failure ->
                            RestoreState.Error(result.reason)
                        RestoreBackupOutcome.NoBackupFound ->
                            RestoreState.Error("No backup found")
                    }
            }
        }

        fun clearRestoreState() {
            _restoreState.value = RestoreState.Idle
        }
    }

data class AccountUiState(
    val accountState: AccountState = AccountState.SignedOut,
    val isProUser: Boolean = false,
)

sealed class RestoreState {
    data object Idle : RestoreState()

    data object InProgress : RestoreState()

    data class Success(
        val sessionsRestored: Int,
    ) : RestoreState()

    data class Error(
        val message: String,
    ) : RestoreState()
}
