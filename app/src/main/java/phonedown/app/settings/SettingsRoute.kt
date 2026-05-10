package phonedown.app.settings

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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

    SettingsScreen(
        uiState = uiState,
        onAccountClick = onAccountClick,
        onProClick = onProClick,
        onBackupClick = viewModel::triggerBackup,
        onPrivacyPolicyClick = onPrivacyPolicyClick,
        callPausePermissionGranted = callPausePermissionGranted,
        onCallPausePermissionRequested = onCallPausePermissionRequested,
        onDeleteRequested = viewModel::showDeleteConfirmation,
        onDeleteConfirmed = viewModel::deleteAllData,
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
