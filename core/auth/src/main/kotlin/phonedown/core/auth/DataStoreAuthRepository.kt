package phonedown.core.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import phonedown.core.model.AccountState
import phonedown.core.model.GoogleAccount
import phonedown.core.model.repository.AuthRepository

class DataStoreAuthRepository(
    private val dataStore: DataStore<Preferences>,
) : AuthRepository {
    override val accountState: Flow<AccountState> =
        dataStore.data.map { preferences ->
            val email = preferences[EMAIL_KEY]
            if (email.isNullOrBlank()) {
                AccountState.SignedOut
            } else {
                AccountState.SignedIn(
                    displayName = preferences[DISPLAY_NAME_KEY],
                    email = email,
                    photoUrl = preferences[PHOTO_URL_KEY],
                    accountId = preferences[ACCOUNT_ID_KEY],
                )
            }
        }

    override suspend fun applyGoogleAccount(account: GoogleAccount) {
        dataStore.edit { preferences ->
            preferences[EMAIL_KEY] = account.email
            account.accountId?.let { preferences[ACCOUNT_ID_KEY] = it } ?: preferences.remove(ACCOUNT_ID_KEY)
            account.displayName?.let { preferences[DISPLAY_NAME_KEY] = it } ?: preferences.remove(DISPLAY_NAME_KEY)
            account.photoUrl?.let { preferences[PHOTO_URL_KEY] = it } ?: preferences.remove(PHOTO_URL_KEY)
        }
    }

    override suspend fun signOut() {
        dataStore.edit { preferences ->
            preferences.remove(ACCOUNT_ID_KEY)
            preferences.remove(DISPLAY_NAME_KEY)
            preferences.remove(EMAIL_KEY)
            preferences.remove(PHOTO_URL_KEY)
        }
    }

    private companion object {
        val ACCOUNT_ID_KEY = stringPreferencesKey("auth_google_account_id")
        val DISPLAY_NAME_KEY = stringPreferencesKey("auth_google_display_name")
        val EMAIL_KEY = stringPreferencesKey("auth_google_email")
        val PHOTO_URL_KEY = stringPreferencesKey("auth_google_photo_url")
    }
}
