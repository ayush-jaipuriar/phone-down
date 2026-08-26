package phonedown.core.datastore.security

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.encryptedDataStore: DataStore<Preferences> by preferencesDataStore(name = "encrypted_prefs")

/**
 * Encrypted DataStore wrapper for sensitive data like auth tokens.
 *
 * Note: For V1, this uses a standard DataStore since we use fake repositories.
 * When integrating real Google Sign-In, migrate to EncryptedSharedPreferences
 * from androidx.security:security-crypto or use SQLCipher for full database encryption.
 *
 * Usage:
 * ```
 * val token = encryptedDataStore.getToken()
 * encryptedDataStore.saveToken(token)
 * encryptedDataStore.clearToken()
 * ```
 */
class EncryptedDataStore(
    private val context: Context,
) {
    private val dataStore: DataStore<Preferences> = context.encryptedDataStore

    val token: Flow<String?> =
        dataStore.data.map { prefs ->
            prefs[AUTH_TOKEN_KEY]
        }

    suspend fun saveToken(token: String) {
        dataStore.edit { prefs ->
            prefs[AUTH_TOKEN_KEY] = token
        }
    }

    suspend fun clearToken() {
        dataStore.edit { prefs ->
            prefs.remove(AUTH_TOKEN_KEY)
        }
    }

    companion object {
        private val AUTH_TOKEN_KEY = stringPreferencesKey("auth_token")
    }
}
