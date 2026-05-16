package phonedown.feature.settings

import phonedown.core.model.ThemeMode

data class SettingsUiState(
    val defaultDurationSeconds: Long = 1500,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val themeMode: ThemeMode = ThemeMode.System,
    val autoBackupEnabled: Boolean = false,
    val lastBackupEpochMillis: Long? = null,
    val backupOptIn: Boolean = false,
    val isProUser: Boolean = false,
    val isSignedIn: Boolean = false,
    val isBackingUp: Boolean = false,
    val backupError: String? = null,
    val showDeleteConfirmation: Boolean = false,
    val deleteConfirmationText: String = "",
    val deleteIncludeBackup: Boolean = true,
    val isDeleting: Boolean = false,
    val deleteSuccess: Boolean = false,
    val deleteError: String? = null,
)
