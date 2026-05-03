package phonedown.core.datastore.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import phonedown.core.model.ProEntitlement
import phonedown.core.model.repository.EntitlementCache

private val ENTITLEMENT_TYPE_KEY = stringPreferencesKey("pro_entitlement_type")
private val ENTITLEMENT_EXPIRY_KEY = longPreferencesKey("pro_entitlement_expiry")
private val ENTITLEMENT_CACHED_AT_KEY = longPreferencesKey("pro_entitlement_cached_at")
private const val CACHE_TTL_MILLIS = 24 * 60 * 60 * 1000L // 24 hours

class ProEntitlementCache(
    private val dataStore: DataStore<Preferences>,
) : EntitlementCache {

    override suspend fun read(): ProEntitlement? {
        return dataStore.data.map { preferences ->
            val type = preferences[ENTITLEMENT_TYPE_KEY]
            val expiry = preferences[ENTITLEMENT_EXPIRY_KEY]
            val cachedAt = preferences[ENTITLEMENT_CACHED_AT_KEY]

            if (type == null || cachedAt == null) {
                null
            } else if (System.currentTimeMillis() - cachedAt > CACHE_TTL_MILLIS) {
                null
            } else {
                when (type) {
                    "free" -> ProEntitlement.Free
                    "pro" -> ProEntitlement.Pro(expiryDateMillis = if (expiry == -1L) null else expiry)
                    else -> null
                }
            }
        }.firstOrNull()
    }

    override suspend fun write(entitlement: ProEntitlement) {
        dataStore.edit { preferences ->
            when (entitlement) {
                is ProEntitlement.Free -> {
                    preferences[ENTITLEMENT_TYPE_KEY] = "free"
                    preferences.remove(ENTITLEMENT_EXPIRY_KEY)
                }
                is ProEntitlement.Pro -> {
                    preferences[ENTITLEMENT_TYPE_KEY] = "pro"
                    preferences[ENTITLEMENT_EXPIRY_KEY] = entitlement.expiryDateMillis ?: -1L
                }
            }
            preferences[ENTITLEMENT_CACHED_AT_KEY] = System.currentTimeMillis()
        }
    }

    override suspend fun clear() {
        dataStore.edit { preferences ->
            preferences.remove(ENTITLEMENT_TYPE_KEY)
            preferences.remove(ENTITLEMENT_EXPIRY_KEY)
            preferences.remove(ENTITLEMENT_CACHED_AT_KEY)
        }
    }

    override suspend fun isValid(): Boolean {
        return dataStore.data.map { preferences ->
            val cachedAt = preferences[ENTITLEMENT_CACHED_AT_KEY]
            cachedAt != null && (System.currentTimeMillis() - cachedAt <= CACHE_TTL_MILLIS)
        }.firstOrNull() ?: false
    }
}
