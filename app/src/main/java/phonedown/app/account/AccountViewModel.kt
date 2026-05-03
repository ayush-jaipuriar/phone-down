package phonedown.app.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
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
    ) : ViewModel() {
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
}

data class AccountUiState(
    val accountState: AccountState = AccountState.SignedOut,
    val isProUser: Boolean = false,
)
