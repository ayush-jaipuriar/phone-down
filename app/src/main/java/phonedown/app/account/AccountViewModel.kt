package phonedown.app.account

import android.content.Intent
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
import phonedown.app.backup.AutoBackupScheduling
import phonedown.app.backup.DriveAuthorizationUiStep
import phonedown.app.backup.DriveAuthorizationCoordinator
import phonedown.core.model.AccountState
import phonedown.core.model.GoogleAccount
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
        private val driveAuthorizationManager: DriveAuthorizationCoordinator,
        private val autoBackupScheduler: AutoBackupScheduling,
    ) : ViewModel() {
        private val _restoreState = MutableStateFlow<RestoreState>(RestoreState.Idle)
        val restoreState: StateFlow<RestoreState> = _restoreState.asStateFlow()
        private val _signInState = MutableStateFlow<SignInState>(SignInState.Idle)
        val signInState: StateFlow<SignInState> = _signInState.asStateFlow()

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

        fun beginSignIn() {
            _signInState.value = SignInState.InProgress
        }

        fun completeSignIn(account: GoogleAccount) {
            viewModelScope.launch {
                driveAuthorizationManager.clearCachedAccessToken()
                authRepository.applyGoogleAccount(account)
                autoBackupScheduler.refreshSchedule()
                _signInState.value = SignInState.Idle
            }
        }

        fun failSignIn(message: String) {
            _signInState.value = SignInState.Error(message)
        }

        fun cancelSignIn() {
            _signInState.value = SignInState.Idle
        }

        fun clearSignInState() {
            _signInState.value = SignInState.Idle
        }

        fun signOut() {
            viewModelScope.launch {
                driveAuthorizationManager.clearCachedAccessToken()
                authRepository.signOut()
                autoBackupScheduler.refreshSchedule()
            }
        }

        suspend fun beginRestoreAuthorization(): DriveAuthorizationUiStep = driveAuthorizationManager.beginAuthorization()

        fun completeRestoreAuthorization(
            resultCode: Int,
            data: Intent?,
        ): DriveAuthorizationUiStep = driveAuthorizationManager.completeAuthorization(resultCode, data)

        fun failRestore(message: String) {
            _restoreState.value = RestoreState.Error(message)
        }

        fun restoreBackup() {
            viewModelScope.launch {
                _restoreState.value = RestoreState.InProgress
                val result = restoreBackupUseCase()
                _restoreState.value =
                    when (result) {
                        is RestoreBackupOutcome.Success -> {
                            autoBackupScheduler.refreshSchedule()
                            RestoreState.Success(result.sessionsRestored)
                        }
                        is RestoreBackupOutcome.Failure ->
                            RestoreState.Error(result.reason)
                        RestoreBackupOutcome.NoBackupFound ->
                            RestoreState.NoBackupFound("No backup found for this account.")
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

sealed class SignInState {
    data object Idle : SignInState()

    data object InProgress : SignInState()

    data class Error(
        val message: String,
    ) : SignInState()
}

sealed class RestoreState {
    data object Idle : RestoreState()

    data object InProgress : RestoreState()

    data class Success(
        val sessionsRestored: Int,
    ) : RestoreState()

    data class Error(
        val message: String,
    ) : RestoreState()

    data class NoBackupFound(
        val message: String,
    ) : RestoreState()
}
