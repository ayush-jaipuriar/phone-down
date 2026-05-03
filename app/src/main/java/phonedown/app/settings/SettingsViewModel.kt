package phonedown.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ThemeMode
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.SettingsRepository
import phonedown.feature.settings.SettingsUiState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        billingRepository: BillingRepository,
    ) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                settingsRepository.settings,
                billingRepository.entitlement,
            ) { settings, entitlement ->
                SettingsUiState(
                    defaultDurationSeconds = settings.defaultDurationSeconds,
                    soundEnabled = settings.soundEnabled,
                    hapticsEnabled = settings.hapticsEnabled,
                    themeMode = settings.themeMode,
                    autoBackupEnabled = settings.autoBackupEnabled,
                    lastBackupEpochMillis = settings.lastBackupEpochMillis,
                    backupOptIn = settings.backupOptIn,
                    isProUser = entitlement is ProEntitlement.Pro,
                )
            }.collect { state ->
                _uiState.value = state
            }
        }
    }

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setSoundEnabled(enabled) }
    }

    fun setHapticsEnabled(enabled: Boolean) {
        viewModelScope.launch { settingsRepository.setHapticsEnabled(enabled) }
    }

    fun setThemeMode(themeMode: ThemeMode) {
        viewModelScope.launch { settingsRepository.setThemeMode(themeMode) }
    }

    fun setDefaultDuration(seconds: Long) {
        viewModelScope.launch { settingsRepository.setDefaultDurationSeconds(seconds) }
    }
}
