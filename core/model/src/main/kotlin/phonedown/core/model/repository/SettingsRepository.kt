package phonedown.core.model.repository

import kotlinx.coroutines.flow.Flow
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings

interface SettingsRepository {
    val settings: Flow<UserSettings>

    suspend fun setDefaultDurationSeconds(seconds: Long)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setHapticsEnabled(enabled: Boolean)
    suspend fun setThemeMode(themeMode: ThemeMode)
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun setBackupOptIn(enabled: Boolean)
    suspend fun setAutoBackupEnabled(enabled: Boolean)
    suspend fun setLastBackupEpochMillis(epochMillis: Long?)
    suspend fun setFreeCustomDurationSeconds(seconds: Long?)
}
