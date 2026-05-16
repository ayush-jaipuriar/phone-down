package phonedown.app.settings

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.launch
import phonedown.app.backup.DriveAuthorizationUiStep
import phonedown.feature.settings.SettingsScreen

@Composable
@Suppress("FunctionName")
fun SettingsRoute(
    onAccountClick: () -> Unit,
    onProClick: () -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    onThemeModeSelected: (phonedown.core.model.ThemeMode) -> Unit,
    callPausePermissionGranted: Boolean,
    onCallPausePermissionRequested: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    var pendingAuthorizationAction by remember { mutableStateOf<DriveAuthorizationAction?>(null) }
    val backupAuthorizationLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            when (val step = viewModel.completeBackupAuthorization(result.resultCode, result.data)) {
                is DriveAuthorizationUiStep.AccessToken -> {
                    when (pendingAuthorizationAction) {
                        DriveAuthorizationAction.Backup -> viewModel.triggerBackup()
                        DriveAuthorizationAction.DeleteCloudBackup -> viewModel.deleteAllData()
                        null -> Unit
                    }
                    pendingAuthorizationAction = null
                }
                DriveAuthorizationUiStep.Cancelled -> {
                    if (pendingAuthorizationAction == DriveAuthorizationAction.DeleteCloudBackup) {
                        viewModel.showDeleteError("Google Drive authorization is required before deleting your cloud backup.")
                    }
                    pendingAuthorizationAction = null
                }
                is DriveAuthorizationUiStep.Error -> {
                    when (pendingAuthorizationAction) {
                        DriveAuthorizationAction.Backup -> viewModel.showBackupError(step.message)
                        DriveAuthorizationAction.DeleteCloudBackup -> viewModel.showDeleteError(step.message)
                        null -> Unit
                    }
                    pendingAuthorizationAction = null
                }
                is DriveAuthorizationUiStep.LaunchResolution -> {
                    when (pendingAuthorizationAction) {
                        DriveAuthorizationAction.Backup -> viewModel.showBackupError("Google Drive authorization could not be completed.")
                        DriveAuthorizationAction.DeleteCloudBackup -> viewModel.showDeleteError("Google Drive authorization could not be completed.")
                        null -> Unit
                    }
                    pendingAuthorizationAction = null
                }
            }
        }

    SettingsScreen(
        uiState = uiState,
        onAccountClick = onAccountClick,
        onProClick = onProClick,
        onBackupClick = {
            coroutineScope.launch {
                pendingAuthorizationAction = DriveAuthorizationAction.Backup
                when (val step = viewModel.beginBackupAuthorization()) {
                    is DriveAuthorizationUiStep.AccessToken -> {
                        pendingAuthorizationAction = null
                        viewModel.triggerBackup()
                    }
                    DriveAuthorizationUiStep.Cancelled -> pendingAuthorizationAction = null
                    is DriveAuthorizationUiStep.Error -> {
                        pendingAuthorizationAction = null
                        viewModel.showBackupError(step.message)
                    }
                    is DriveAuthorizationUiStep.LaunchResolution ->
                        backupAuthorizationLauncher.launch(IntentSenderRequest.Builder(step.pendingIntent).build())
                }
            }
        },
        onAutoBackupToggled = viewModel::setAutoBackupEnabled,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        callPausePermissionGranted = callPausePermissionGranted,
        onCallPausePermissionRequested = onCallPausePermissionRequested,
        onDeleteRequested = viewModel::showDeleteConfirmation,
        onDeleteConfirmed = {
            if (!uiState.deleteIncludeBackup || !uiState.isSignedIn) {
                viewModel.deleteAllData()
            } else {
                coroutineScope.launch {
                    pendingAuthorizationAction = DriveAuthorizationAction.DeleteCloudBackup
                    when (val step = viewModel.beginBackupAuthorization()) {
                        is DriveAuthorizationUiStep.AccessToken -> {
                            pendingAuthorizationAction = null
                            viewModel.deleteAllData()
                        }
                        DriveAuthorizationUiStep.Cancelled -> {
                            pendingAuthorizationAction = null
                            viewModel.showDeleteError("Google Drive authorization is required before deleting your cloud backup.")
                        }
                        is DriveAuthorizationUiStep.Error -> {
                            pendingAuthorizationAction = null
                            viewModel.showDeleteError(step.message)
                        }
                        is DriveAuthorizationUiStep.LaunchResolution ->
                            backupAuthorizationLauncher.launch(IntentSenderRequest.Builder(step.pendingIntent).build())
                    }
                }
            }
        },
        onDeleteDismissed = viewModel::dismissDeleteConfirmation,
        onDeleteConfirmationTextChanged = viewModel::setDeleteConfirmationText,
        onDeleteIncludeBackupChanged = viewModel::setDeleteIncludeBackup,
        onSoundToggled = viewModel::setSoundEnabled,
        onHapticsToggled = viewModel::setHapticsEnabled,
        onThemeModeSelected = { mode ->
            viewModel.setThemeMode(mode)
            onThemeModeSelected(mode)
        },
    )
}

private enum class DriveAuthorizationAction {
    Backup,
    DeleteCloudBackup,
}
