package phonedown.app.account

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import phonedown.feature.account.AccountScreen

@Composable
@Suppress("FunctionName")
fun AccountRoute(
    onBack: () -> Unit,
    viewModel: AccountViewModel = hiltViewModel(),
) {
    val context = LocalContext.current
    val activity = context.findActivity()
    val coroutineScope = rememberCoroutineScope()
    val googleSignInCoordinator =
        remember(context.applicationContext) {
            GoogleSignInCoordinator(context.applicationContext)
        }
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val restoreState by viewModel.restoreState.collectAsStateWithLifecycle()
    val signInState by viewModel.signInState.collectAsStateWithLifecycle()

    val isRestoring = restoreState is RestoreState.InProgress
    val restoreError = (restoreState as? RestoreState.Error)?.message
    val restoreSuccess =
        (restoreState as? RestoreState.Success)?.let {
            "Restored ${it.sessionsRestored} sessions successfully."
        }
    val signInError = (signInState as? SignInState.Error)?.message

    AccountScreen(
        accountState = uiState.accountState,
        isProUser = uiState.isProUser,
        isSigningIn = signInState is SignInState.InProgress,
        signInError = signInError,
        isRestoring = isRestoring,
        restoreError = restoreError,
        restoreSuccess = restoreSuccess,
        onSignIn = {
            coroutineScope.launch {
                val currentActivity = activity
                if (currentActivity == null) {
                    viewModel.failSignIn("Google Sign-In needs an active Android screen.")
                    return@launch
                }

                viewModel.beginSignIn()
                try {
                    val account = googleSignInCoordinator.signIn(currentActivity)
                    viewModel.completeSignIn(account)
                } catch (exception: GoogleSignInCancelledException) {
                    viewModel.cancelSignIn()
                } catch (exception: GoogleSignInException) {
                    viewModel.failSignIn(exception.message ?: "Google Sign-In failed.")
                }
            }
        },
        onSignOut = {
            coroutineScope.launch {
                googleSignInCoordinator.clearCredentialState(context)
                viewModel.signOut()
            }
        },
        onRestoreClick = viewModel::restoreBackup,
        onClearRestoreState = viewModel::clearRestoreState,
        onClearSignInError = viewModel::clearSignInState,
        onBack = onBack,
    )
}

private tailrec fun Context.findActivity(): Activity? =
    when (this) {
        is Activity -> this
        is ContextWrapper -> baseContext.findActivity()
        else -> null
    }
