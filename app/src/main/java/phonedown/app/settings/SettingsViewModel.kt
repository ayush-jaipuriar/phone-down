package phonedown.app.settings

import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import phonedown.app.backup.AutoBackupScheduling
import phonedown.app.backup.DriveAuthorizationCoordinator
import phonedown.app.backup.DriveAuthorizationUiStep
import phonedown.core.model.AccountState
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ThemeMode
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.DeleteBackupResult
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
        private val driveAuthorizationManager: DriveAuthorizationCoordinator,
        private val autoBackupScheduler: AutoBackupScheduling,
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
                    Triple(settings, entitlement, accountState)
                }.collect { (settings, entitlement, accountState) ->
                    _uiState.update { current ->
                        current.copy(
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
                    }
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
            _uiState.update { it.copy(showDeleteConfirmation = true) }
        }

        fun dismissDeleteConfirmation() {
            _uiState.update {
                it.copy(
                    showDeleteConfirmation = false,
                    deleteConfirmationText = "",
                    deleteIncludeBackup = true,
                    deleteSuccess = false,
                    deleteError = null,
                )
            }
        }

        fun setDeleteConfirmationText(text: String) {
            _uiState.update { it.copy(deleteConfirmationText = text) }
        }

        fun setDeleteIncludeBackup(include: Boolean) {
            _uiState.update { it.copy(deleteIncludeBackup = include, deleteError = null) }
        }

        fun showDeleteError(message: String) {
            _uiState.update { it.copy(isDeleting = false, deleteError = message) }
        }

        fun deleteAllData() {
            viewModelScope.launch {
                _uiState.update { it.copy(isDeleting = true, deleteError = null, backupError = null) }
                try {
                    if (_uiState.value.deleteIncludeBackup && _uiState.value.isSignedIn) {
                        when (val deleteResult = backupRepository.deleteBackup()) {
                            DeleteBackupResult.Deleted,
                            DeleteBackupResult.NoBackupFound,
                            -> Unit
                            is DeleteBackupResult.Failure -> {
                                _uiState.update {
                                    it.copy(
                                        isDeleting = false,
                                        deleteError = deleteResult.reason,
                                    )
                                }
                                return@launch
                            }
                        }
                    }
                    sessionRepository.clearAllSessions()
                    sessionRepository.clearAllPenaltyEvents()
                    settingsRepository.resetToDefaults()
                    if (_uiState.value.deleteIncludeBackup && _uiState.value.isSignedIn) {
                        driveAuthorizationManager.clearCachedAccessToken()
                        authRepository.signOut()
                    }
                    autoBackupScheduler.refreshSchedule()
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteSuccess = true,
                            showDeleteConfirmation = false,
                            deleteConfirmationText = "",
                            deleteError = null,
                        )
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isDeleting = false,
                            deleteError = e.message ?: "Delete failed",
                        )
                    }
                }
            }
        }

        suspend fun beginBackupAuthorization(): DriveAuthorizationUiStep = driveAuthorizationManager.beginAuthorization()

        fun completeBackupAuthorization(
            resultCode: Int,
            data: Intent?,
        ): DriveAuthorizationUiStep = driveAuthorizationManager.completeAuthorization(resultCode, data)

        fun showBackupError(message: String) {
            _uiState.update { it.copy(backupError = message, isBackingUp = false) }
        }

        fun setAutoBackupEnabled(enabled: Boolean) {
            viewModelScope.launch {
                settingsRepository.setAutoBackupEnabled(enabled)
                autoBackupScheduler.refreshSchedule()
            }
        }

        fun triggerBackup() {
            viewModelScope.launch {
                _uiState.update { it.copy(isBackingUp = true, backupError = null) }
                try {
                    val sessions = sessionRepository.getAllSessions()
                    val penalties = sessionRepository.getAllPenaltyEvents()
                    val currentSettings = settingsRepository.settings.first()
                    val isFirstBackupOptIn = !currentSettings.backupOptIn
                    val settingsForBackup =
                        if (isFirstBackupOptIn) {
                            currentSettings.copy(
                                backupOptIn = true,
                                autoBackupEnabled = true,
                            )
                        } else {
                            currentSettings
                        }
                    val result = backupRepository.createBackup(sessions, penalties, settingsForBackup)
                    when (result) {
                        is phonedown.core.model.repository.BackupResult.Success -> {
                            settingsRepository.setBackupOptIn(true)
                            if (isFirstBackupOptIn) {
                                settingsRepository.setAutoBackupEnabled(true)
                            }
                            settingsRepository.setLastBackupEpochMillis(result.timestampMillis)
                            autoBackupScheduler.refreshSchedule()
                            _uiState.update {
                                it.copy(
                                    autoBackupEnabled = settingsForBackup.autoBackupEnabled,
                                    backupOptIn = true,
                                    isBackingUp = false,
                                    lastBackupEpochMillis = result.timestampMillis,
                                )
                            }
                        }
                        is phonedown.core.model.repository.BackupResult.Failure -> {
                            _uiState.update {
                                it.copy(
                                    isBackingUp = false,
                                    backupError = result.reason,
                                )
                            }
                        }
                    }
                } catch (e: Exception) {
                    _uiState.update {
                        it.copy(
                            isBackingUp = false,
                            backupError = e.message ?: "Backup failed",
                        )
                    }
                }
            }
        }
    }
