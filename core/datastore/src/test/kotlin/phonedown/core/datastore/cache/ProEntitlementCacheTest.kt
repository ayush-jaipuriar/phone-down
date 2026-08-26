package phonedown.core.datastore.cache

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import phonedown.core.model.ProEntitlement

@OptIn(ExperimentalCoroutinesApi::class)
class ProEntitlementCacheTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    private val testDispatcher = StandardTestDispatcher()

    private fun createDataStore(): DataStore<Preferences> =
        PreferenceDataStoreFactory.create(
            scope = kotlinx.coroutines.CoroutineScope(testDispatcher + Job()),
            produceFile = { tempFolder.newFile("test_prefs.preferences_pb") },
        )

    @Test
    fun `read returns null when cache is empty`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            assertNull(cache.read())
        }

    @Test
    fun `write and read free entitlement`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            cache.write(ProEntitlement.Free)
            assertEquals(ProEntitlement.Free, cache.read())
        }

    @Test
    fun `write and read pro entitlement with expiry`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            val expiry = System.currentTimeMillis() + 86_400_000L
            cache.write(ProEntitlement.Pro(expiryDateMillis = expiry))

            val result = cache.read() as ProEntitlement.Pro
            assertEquals(expiry, result.expiryDateMillis)
        }

    @Test
    fun `write and read pro entitlement without expiry`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            cache.write(ProEntitlement.Pro(expiryDateMillis = null))

            val result = cache.read() as ProEntitlement.Pro
            assertNull(result.expiryDateMillis)
        }

    @Test
    fun `cache is valid after write`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            cache.write(ProEntitlement.Free)
            assertTrue(cache.isValid())
        }

    @Test
    fun `cache is invalid after clear`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            cache.write(ProEntitlement.Free)
            cache.clear()
            assertFalse(cache.isValid())
            assertNull(cache.read())
        }

    @Test
    fun `lifetime pro cache remains valid indefinitely without expiring at 24 hours`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            cache.write(ProEntitlement.Pro(expiryDateMillis = null))

            assertTrue(cache.isValid())
            val result = cache.read()
            assertTrue(result is ProEntitlement.Pro)
            assertNull((result as ProEntitlement.Pro).expiryDateMillis)
        }

    @Test
    fun `subscription pro cache remains valid while before expiry date`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            val futureExpiry = System.currentTimeMillis() + 30L * 24 * 60 * 60 * 1000L // 30 days
            cache.write(ProEntitlement.Pro(expiryDateMillis = futureExpiry))

            assertTrue(cache.isValid())
            val result = cache.read() as ProEntitlement.Pro
            assertEquals(futureExpiry, result.expiryDateMillis)
        }

    @Test
    fun `subscription pro cache expires after expiry date passes`() =
        runTest(testDispatcher) {
            val cache = ProEntitlementCache(createDataStore())
            val pastExpiry = System.currentTimeMillis() - 10_000L // 10s ago
            cache.write(ProEntitlement.Pro(expiryDateMillis = pastExpiry))

            assertFalse(cache.isValid())
            assertNull(cache.read())
        }
}
