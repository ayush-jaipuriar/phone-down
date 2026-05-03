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

    AccountScreen(
        accountState = uiState.accountState,
        isProUser = uiState.isProUser,
        onSignIn = viewModel::signIn,
        onSignOut = viewModel::signOut,
        onBack = onBack,
    )
}
