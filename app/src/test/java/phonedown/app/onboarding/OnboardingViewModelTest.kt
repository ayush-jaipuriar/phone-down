package phonedown.app.onboarding

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.SettingsRepository

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

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
    fun `completeOnboarding persists onboardingCompleted true`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = OnboardingViewModel(repo)

        viewModel.completeOnboarding {}
        testScheduler.advanceUntilIdle()

        assertTrue(repo.latestSettings().onboardingCompleted)
    }

    @Test
    fun `completeOnboarding invokes the onCompleted callback`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = OnboardingViewModel(repo)
        var callbackInvoked = false

        viewModel.completeOnboarding { callbackInvoked = true }
        testScheduler.advanceUntilIdle()

        assertTrue(callbackInvoked)
    }

    @Test
    fun `persistence write completes before callback executes`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = OnboardingViewModel(repo)

        viewModel.completeOnboarding {
            assertTrue(repo.latestSettings().onboardingCompleted)
        }
        testScheduler.advanceUntilIdle()
    }

    @Test
    fun `fresh settings default to onboardingCompleted false`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()

        assertFalse(repo.latestSettings().onboardingCompleted)
    }

    @Test
    fun `settings flow reflects onboarding completion`() = runTest(testDispatcher) {
        val repo = FakeSettingsRepository()
        val viewModel = OnboardingViewModel(repo)

        viewModel.completeOnboarding {}
        testScheduler.advanceUntilIdle()

        val emitted = repo.settings.first()
        assertTrue(emitted.onboardingCompleted)
    }
}

private class FakeSettingsRepository(
    initialSettings: UserSettings = UserSettings(),
) : SettingsRepository {
    private val settingsFlow = MutableStateFlow(initialSettings)

    override val settings: Flow<UserSettings> = settingsFlow

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

    override suspend fun resetToDefaults() {
        settingsFlow.value = UserSettings()
    }
}
