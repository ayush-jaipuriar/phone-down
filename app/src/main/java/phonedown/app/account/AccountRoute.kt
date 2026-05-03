package phonedown.app.account

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import phonedown.feature.account.AccountScreen

@Composable
@Suppress("FunctionName")
fun AccountRoute(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val restoreState by viewModel.restoreState.collectAsStateWithLifecycle()

    val isRestoring = restoreState is RestoreState.InProgress
    val restoreError = (restoreState as? RestoreState.Error)?.message
    val restoreSuccess = (restoreState as? RestoreState.Success)?.let {
        "Restored ${it.sessionsRestored} sessions successfully."
    }

    AccountScreen(
        accountState = uiState.accountState,
        isProUser = uiState.isProUser,
        isRestoring = isRestoring,
        restoreError = restoreError,
        restoreSuccess = restoreSuccess,
        onSignIn = viewModel::signIn,
        onSignOut = viewModel::signOut,
        onRestoreClick = viewModel::restoreBackup,
        onClearRestoreState = viewModel::clearRestoreState,
        onBack = onBack,
    )
}
