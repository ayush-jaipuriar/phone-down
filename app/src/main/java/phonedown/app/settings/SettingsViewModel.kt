package phonedown.app.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import phonedown.core.model.AccountState
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ThemeMode
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository
import phonedown.feature.settings.SettingsUiState
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel
    @Inject
    constructor(
        private val settingsRepository: SettingsRepository,
        private val billingRepository: BillingRepository,
        private val authRepository: AuthRepository,
        private val backupRepository: BackupRepository,
        private val sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(SettingsUiState())
        val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

        init {
            viewModelScope.launch {
                combine(
                    settingsRepository.settings,
                    billingRepository.entitlement,
                    authRepository.accountState,
                ) { settings, entitlement, accountState ->
                    SettingsUiState(
                        defaultDurationSeconds = settings.defaultDurationSeconds,
                        soundEnabled = settings.soundEnabled,
                        hapticsEnabled = settings.hapticsEnabled,
                        themeMode = settings.themeMode,
                        autoBackupEnabled = settings.autoBackupEnabled,
                        lastBackupEpochMillis = settings.lastBackupEpochMillis,
                        backupOptIn = settings.backupOptIn,
                        isProUser = entitlement is ProEntitlement.Pro,
                        isSignedIn = accountState is AccountState.SignedIn,
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

        fun showDeleteConfirmation() {
            _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
        }

        fun dismissDeleteConfirmation() {
            _uiState.value =
                _uiState.value.copy(
                    showDeleteConfirmation = false,
                    deleteConfirmationText = "",
                    deleteIncludeBackup = true,
                    deleteSuccess = false,
                )
        }

        fun setDeleteConfirmationText(text: String) {
            _uiState.value = _uiState.value.copy(deleteConfirmationText = text)
        }

        fun setDeleteIncludeBackup(include: Boolean) {
            _uiState.value = _uiState.value.copy(deleteIncludeBackup = include)
        }

        fun deleteAllData() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isDeleting = true)
                try {
                    sessionRepository.clearAllSessions()
                    sessionRepository.clearAllPenaltyEvents()
                    settingsRepository.resetToDefaults()
                    if (_uiState.value.deleteIncludeBackup && _uiState.value.isSignedIn) {
                        backupRepository.deleteBackup()
                        authRepository.signOut()
                    }
                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting = false,
                            deleteSuccess = true,
                            showDeleteConfirmation = false,
                            deleteConfirmationText = "",
                        )
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            isDeleting = false,
                            backupError = e.message ?: "Delete failed",
                        )
                }
            }
        }

        fun triggerBackup() {
            viewModelScope.launch {
                _uiState.value = _uiState.value.copy(isBackingUp = true, backupError = null)
                try {
                    val sessions = sessionRepository.getAllSessions()
                    val penalties = sessionRepository.getAllPenaltyEvents()
                    val currentSettings = settingsRepository.settings.first()
                    val result = backupRepository.createBackup(sessions, penalties, currentSettings)
                    when (result) {
                        is phonedown.core.model.repository.BackupResult.Success -> {
                            settingsRepository.setLastBackupEpochMillis(result.timestampMillis)
                            _uiState.value =
                                _uiState.value.copy(
                                    isBackingUp = false,
                                    lastBackupEpochMillis = result.timestampMillis,
                                )
                        }
                        is phonedown.core.model.repository.BackupResult.Failure -> {
                            _uiState.value =
                                _uiState.value.copy(
                                    isBackingUp = false,
                                    backupError = result.reason,
                                )
                        }
                    }
                } catch (e: Exception) {
                    _uiState.value =
                        _uiState.value.copy(
                            isBackingUp = false,
                            backupError = e.message ?: "Backup failed",
                        )
                }
            }
        }
    }
