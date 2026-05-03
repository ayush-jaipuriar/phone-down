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
    onThemeModeSelected: (phonedown.core.model.ThemeMode) -> Unit,
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    SettingsScreen(
        uiState = uiState,
        onAccountClick = onAccountClick,
        onProClick = onProClick,
        onBackupClick = viewModel::triggerBackup,
        onSoundToggled = viewModel::setSoundEnabled,
        onHapticsToggled = viewModel::setHapticsEnabled,
        onThemeModeSelected = { mode ->
            viewModel.setThemeMode(mode)
            onThemeModeSelected(mode)
        },
    )
}
