package phonedown.app.account

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import phonedown.app.backup.DriveAuthorizationUiStep
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
    val noBackupFoundMessage = (restoreState as? RestoreState.NoBackupFound)?.message
    val restoreSuccess =
        (restoreState as? RestoreState.Success)?.let {
            "Restored ${it.sessionsRestored} sessions successfully."
        }
    val signInError = (signInState as? SignInState.Error)?.message
    val restoreAuthorizationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (val step = viewModel.completeRestoreAuthorization(result.resultCode, result.data)) {
                is DriveAuthorizationUiStep.AccessToken -> viewModel.restoreBackup()
                DriveAuthorizationUiStep.Cancelled -> Unit
                is DriveAuthorizationUiStep.Error -> viewModel.failRestore(step.message)
                is DriveAuthorizationUiStep.LaunchResolution -> viewModel.failRestore("Google Drive authorization could not be completed.")
            }
        }

    AccountScreen(
        accountState = uiState.accountState,
        isSigningIn = signInState is SignInState.InProgress,
        signInError = signInError,
        isRestoring = isRestoring,
        restoreError = restoreError,
        noBackupFoundMessage = noBackupFoundMessage,
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
        onRestoreClick = {
            coroutineScope.launch {
                when (val step = viewModel.beginRestoreAuthorization()) {
                    is DriveAuthorizationUiStep.AccessToken -> viewModel.restoreBackup()
                    DriveAuthorizationUiStep.Cancelled -> Unit
                    is DriveAuthorizationUiStep.Error -> viewModel.failRestore(step.message)
                    is DriveAuthorizationUiStep.LaunchResolution ->
                        restoreAuthorizationLauncher.launch(IntentSenderRequest.Builder(step.pendingIntent).build())
                }
            }
        },
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
