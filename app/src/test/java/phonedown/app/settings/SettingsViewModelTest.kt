package phonedown.app.settings

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.SettingsRepository
import phonedown.feature.settings.SettingsUiState

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial uiState reflects repository defaults`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        testScheduler.advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(1500, state.defaultDurationSeconds)
        assertTrue(state.soundEnabled)
        assertTrue(state.hapticsEnabled)
        assertEquals(ThemeMode.System, state.themeMode)
        assertFalse(state.autoBackupEnabled)
        assertNull(state.lastBackupEpochMillis)
        assertFalse(state.backupOptIn)
    }

    @Test
    fun `setSoundEnabled updates repository and uiState`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        viewModel.setSoundEnabled(false)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.soundEnabled)
        assertFalse(repo.latestSettings().soundEnabled)
    }

    @Test
    fun `setHapticsEnabled updates repository and uiState`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        viewModel.setHapticsEnabled(false)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hapticsEnabled)
        assertFalse(repo.latestSettings().hapticsEnabled)
    }

    @Test
    fun `setThemeMode updates repository and uiState`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        viewModel.setThemeMode(ThemeMode.Dark)
        testScheduler.advanceUntilIdle()

        assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
        assertEquals(ThemeMode.Dark, repo.latestSettings().themeMode)
    }

    @Test
    fun `setDefaultDuration updates repository and uiState`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        viewModel.setDefaultDuration(1800)
        testScheduler.advanceUntilIdle()

        assertEquals(1800, viewModel.uiState.value.defaultDurationSeconds)
        assertEquals(1800, repo.latestSettings().defaultDurationSeconds)
    }

    @Test
    fun `repository flow emission updates uiState`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        repo.setSoundEnabled(false)
        testScheduler.advanceUntilIdle()

        assertFalse(viewModel.uiState.value.soundEnabled)
    }

    @Test
    fun `settings flow emits updated values`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = SettingsViewModel(repo)

        viewModel.setHapticsEnabled(false)
        testScheduler.advanceUntilIdle()

        val emitted = repo.settings.first()
        assertFalse(emitted.hapticsEnabled)
    }
}

private class FakeSettingsRepository(
    initialSettings: UserSettings = UserSettings(),
) : SettingsRepository {
    private val settingsFlow = kotlinx.coroutines.flow.MutableStateFlow(initialSettings)

    override val settings: kotlinx.coroutines.flow.Flow<UserSettings> = settingsFlow

    fun latestSettings(): UserSettings = settingsFlow.value

    override suspend fun setDefaultDurationSeconds(seconds: Long) {
        settingsFlow.value = settingsFlow.value.copy(defaultDurationSeconds = seconds)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(soundEnabled = enabled)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(hapticsEnabled = enabled)
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(themeMode = themeMode)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(onboardingCompleted = completed)
    }

    override suspend fun setBackupOptIn(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(backupOptIn = enabled)
    }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(autoBackupEnabled = enabled)
    }

    override suspend fun setLastBackupEpochMillis(epochMillis: Long?) {
        settingsFlow.value = settingsFlow.value.copy(lastBackupEpochMillis = epochMillis)
    }

    override suspend fun setFreeCustomDurationSeconds(seconds: Long?) {
        settingsFlow.value = settingsFlow.value.copy(freeCustomDurationSeconds = seconds)
    }
}
