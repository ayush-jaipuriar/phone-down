@file:Suppress("MaxLineLength")

package phonedown.core.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.SettingsRepository
import javax.inject.Inject

class DataStoreSettingsRepository
    @Inject
    constructor(
        private val dataStore: DataStore<Preferences>,
    ) : SettingsRepository {
        override val settings: Flow<UserSettings> =
            dataStore.data.map { prefs ->
                UserSettings(
                    defaultDurationSeconds =
                        prefs[DEFAULT_DURATION_SECONDS_KEY]
                            ?: UserSettings.DEFAULT_DURATION_SECONDS,
                    soundEnabled = prefs[SOUND_ENABLED_KEY] ?: true,
                    hapticsEnabled = prefs[HAPTICS_ENABLED_KEY] ?: true,
                    themeMode = prefs[THEME_MODE_KEY]?.toThemeMode() ?: ThemeMode.System,
                    onboardingCompleted = prefs[ONBOARDING_COMPLETED_KEY] ?: false,
                    backupOptIn = prefs[BACKUP_OPT_IN_KEY] ?: false,
                    autoBackupEnabled = prefs[AUTO_BACKUP_ENABLED_KEY] ?: false,
                    lastBackupEpochMillis = prefs[LAST_BACKUP_EPOCH_MILLIS_KEY],
                    freeCustomDurationSeconds = prefs[FREE_CUSTOM_DURATION_SECONDS_KEY],
                )
            }

        override suspend fun setDefaultDurationSeconds(seconds: Long) {
            dataStore.edit { it[DEFAULT_DURATION_SECONDS_KEY] = seconds }
        }

        override suspend fun setSoundEnabled(enabled: Boolean) {
            dataStore.edit { it[SOUND_ENABLED_KEY] = enabled }
        }

        override suspend fun setHapticsEnabled(enabled: Boolean) {
            dataStore.edit { it[HAPTICS_ENABLED_KEY] = enabled }
        }

        override suspend fun setThemeMode(themeMode: ThemeMode) {
            dataStore.edit { it[THEME_MODE_KEY] = themeMode.name }
        }

        override suspend fun setOnboardingCompleted(completed: Boolean) {
            dataStore.edit { it[ONBOARDING_COMPLETED_KEY] = completed }
        }

        override suspend fun setBackupOptIn(enabled: Boolean) {
            dataStore.edit { it[BACKUP_OPT_IN_KEY] = enabled }
        }

        override suspend fun setAutoBackupEnabled(enabled: Boolean) {
            dataStore.edit { it[AUTO_BACKUP_ENABLED_KEY] = enabled }
        }

        override suspend fun setLastBackupEpochMillis(epochMillis: Long?) {
            dataStore.edit { prefs ->
                if (epochMillis == null) {
                    prefs.remove(LAST_BACKUP_EPOCH_MILLIS_KEY)
                } else {
                    prefs[LAST_BACKUP_EPOCH_MILLIS_KEY] = epochMillis
                }
            }
        }

        override suspend fun setFreeCustomDurationSeconds(seconds: Long?) {
            dataStore.edit { prefs ->
                if (seconds == null) {
                    prefs.remove(FREE_CUSTOM_DURATION_SECONDS_KEY)
                } else {
                    prefs[FREE_CUSTOM_DURATION_SECONDS_KEY] = seconds
                }
            }
        }

        override suspend fun resetToDefaults() {
            dataStore.edit { prefs ->
                prefs[DEFAULT_DURATION_SECONDS_KEY] = UserSettings.DEFAULT_DURATION_SECONDS
                prefs[SOUND_ENABLED_KEY] = true
                prefs[HAPTICS_ENABLED_KEY] = true
                prefs[THEME_MODE_KEY] = ThemeMode.System.name
                prefs[ONBOARDING_COMPLETED_KEY] = false
                prefs[BACKUP_OPT_IN_KEY] = false
                prefs[AUTO_BACKUP_ENABLED_KEY] = false
                prefs.remove(LAST_BACKUP_EPOCH_MILLIS_KEY)
                prefs.remove(FREE_CUSTOM_DURATION_SECONDS_KEY)
            }
        }

        override suspend fun restoreSettings(settings: UserSettings) {
            dataStore.edit { prefs ->
                prefs[DEFAULT_DURATION_SECONDS_KEY] = settings.defaultDurationSeconds
                prefs[SOUND_ENABLED_KEY] = settings.soundEnabled
                prefs[HAPTICS_ENABLED_KEY] = settings.hapticsEnabled
                prefs[THEME_MODE_KEY] = settings.themeMode.name
                prefs[ONBOARDING_COMPLETED_KEY] = settings.onboardingCompleted
                prefs[BACKUP_OPT_IN_KEY] = settings.backupOptIn
                prefs[AUTO_BACKUP_ENABLED_KEY] = settings.autoBackupEnabled
                settings.lastBackupEpochMillis?.let { prefs[LAST_BACKUP_EPOCH_MILLIS_KEY] = it } ?: prefs.remove(LAST_BACKUP_EPOCH_MILLIS_KEY)
                settings.freeCustomDurationSeconds?.let { prefs[FREE_CUSTOM_DURATION_SECONDS_KEY] = it } ?: prefs.remove(FREE_CUSTOM_DURATION_SECONDS_KEY)
            }
        }

        private fun String.toThemeMode(): ThemeMode = ThemeMode.entries.firstOrNull { it.name == this } ?: ThemeMode.System

        companion object {
            val DEFAULT_DURATION_SECONDS_KEY = longPreferencesKey("default_duration_seconds")
            val SOUND_ENABLED_KEY = booleanPreferencesKey("sound_enabled")
            val HAPTICS_ENABLED_KEY = booleanPreferencesKey("haptics_enabled")

            // Kept backward compatible with previous ThemeModeDataStore
            val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
            val ONBOARDING_COMPLETED_KEY = booleanPreferencesKey("onboarding_completed")
            val BACKUP_OPT_IN_KEY = booleanPreferencesKey("backup_opt_in")
            val AUTO_BACKUP_ENABLED_KEY = booleanPreferencesKey("auto_backup_enabled")
            val LAST_BACKUP_EPOCH_MILLIS_KEY = longPreferencesKey("last_backup_epoch_millis")
            val FREE_CUSTOM_DURATION_SECONDS_KEY = longPreferencesKey("free_custom_duration_seconds")
        }
    }
