package phonedown.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import phonedown.core.model.ThemeMode

val Context.phoneDownThemeModeDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "phone_down_theme_mode",
)

class ThemeModePreference(
    private val dataStore: DataStore<Preferences>,
) {
    val themeMode: Flow<ThemeMode> =
        dataStore.data.map { preferences ->
            preferences[themeModeKey].toThemeMode()
        }

    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }

    private fun String?.toThemeMode(): ThemeMode {
        val matchingThemeMode = ThemeMode.entries.firstOrNull { mode -> mode.name == this }
        return matchingThemeMode ?: ThemeMode.System
    }

    private companion object {
        val themeModeKey = stringPreferencesKey("theme_mode")
    }
}
