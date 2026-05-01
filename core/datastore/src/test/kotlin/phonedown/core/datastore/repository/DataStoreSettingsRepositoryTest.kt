package phonedown.core.datastore.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings
import java.io.File

@OptIn(ExperimentalCoroutinesApi::class)
class DataStoreSettingsRepositoryTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher + Job())
    private lateinit var dataStore: DataStore<Preferences>
    private lateinit var repository: DataStoreSettingsRepository
    private val testFile = File("test_datastore.preferences_pb")

    @Before
    fun setup() {
        dataStore = PreferenceDataStoreFactory.create(
            scope = testScope,
            produceFile = { testFile }
        )
        repository = DataStoreSettingsRepository(dataStore)
    }

    @After
    fun teardown() {
        testFile.delete()
        testScope.cancel()
    }

    @Test
    fun defaultSettingsAreEmitted() = testScope.runTest {
        repository.settings.test {
            assertEquals(UserSettings(), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun updateSettingsEmitsNewValues() = testScope.runTest {
        repository.settings.test {
            // initial
            awaitItem()

            repository.setThemeMode(ThemeMode.Dark)
            assertEquals(ThemeMode.Dark, awaitItem().themeMode)

            repository.setDefaultDurationSeconds(1000)
            assertEquals(1000L, awaitItem().defaultDurationSeconds)

            repository.setLastBackupEpochMillis(12345L)
            assertEquals(12345L, awaitItem().lastBackupEpochMillis)

            // Testing removing a nullable value
            repository.setLastBackupEpochMillis(null)
            assertEquals(null, awaitItem().lastBackupEpochMillis)

            cancelAndIgnoreRemainingEvents()
        }
    }
}
