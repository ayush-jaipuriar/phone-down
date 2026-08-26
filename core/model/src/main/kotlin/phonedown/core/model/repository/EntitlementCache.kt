package phonedown.core.model.repository

import phonedown.core.model.ProEntitlement

/**
 * Cache for Pro entitlement state.
 * Used to persist entitlement across app restarts and provide offline access.
 */
interface EntitlementCache {
    suspend fun read(): ProEntitlement?

    suspend fun write(entitlement: ProEntitlement)

    suspend fun clear()

    suspend fun isValid(): Boolean
}
