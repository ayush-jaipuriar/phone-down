package phonedown.app.settings

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
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.core.model.AccountState
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.ProEntitlement
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.BackupResult
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.RestoreResult
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository

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

    private fun createViewModel(
        settingsRepo: SettingsRepository = FakeSettingsRepository(),
        billingRepo: BillingRepository = FakeBillingRepository(),
        authRepo: AuthRepository = FakeAuthRepository(),
        backupRepo: BackupRepository = FakeBackupRepository(),
        sessionRepo: SessionRepository = FakeSessionRepository(),
    ): SettingsViewModel =
        SettingsViewModel(
            settingsRepository = settingsRepo,
            billingRepository = billingRepo,
            authRepository = authRepo,
            backupRepository = backupRepo,
            sessionRepository = sessionRepo,
        )

    @Test
    fun `initial uiState reflects repository defaults`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            val state = viewModel.uiState.value
            assertEquals(1500, state.defaultDurationSeconds)
            assertTrue(state.soundEnabled)
            assertTrue(state.hapticsEnabled)
            assertEquals(ThemeMode.System, state.themeMode)
            assertFalse(state.autoBackupEnabled)
            assertNull(state.lastBackupEpochMillis)
            assertFalse(state.backupOptIn)
            assertFalse(state.isProUser)
            assertFalse(state.isSignedIn)
        }

    @Test
    fun `setSoundEnabled updates repository and uiState`() =
        runTest(testDispatcher) {
            val repo = FakeSettingsRepository()
            val viewModel = createViewModel(settingsRepo = repo)

            viewModel.setSoundEnabled(false)
            testScheduler.advanceUntilIdle()

            assertFalse(viewModel.uiState.value.soundEnabled)
            assertFalse(repo.latestSettings().soundEnabled)
        }

    @Test
    fun `setHapticsEnabled updates repository and uiState`() =
        runTest(testDispatcher) {
            val repo = FakeSettingsRepository()
            val viewModel = createViewModel(settingsRepo = repo)

            viewModel.setHapticsEnabled(false)
            testScheduler.advanceUntilIdle()

            assertFalse(viewModel.uiState.value.hapticsEnabled)
            assertFalse(repo.latestSettings().hapticsEnabled)
        }

    @Test
    fun `setThemeMode updates repository and uiState`() =
        runTest(testDispatcher) {
            val repo = FakeSettingsRepository()
            val viewModel = createViewModel(settingsRepo = repo)

            viewModel.setThemeMode(ThemeMode.Dark)
            testScheduler.advanceUntilIdle()

            assertEquals(ThemeMode.Dark, viewModel.uiState.value.themeMode)
            assertEquals(ThemeMode.Dark, repo.latestSettings().themeMode)
        }

    @Test
    fun `setDefaultDuration updates repository and uiState`() =
        runTest(testDispatcher) {
            val repo = FakeSettingsRepository()
            val viewModel = createViewModel(settingsRepo = repo)

            viewModel.setDefaultDuration(1800)
            testScheduler.advanceUntilIdle()

            assertEquals(1800, viewModel.uiState.value.defaultDurationSeconds)
            assertEquals(1800, repo.latestSettings().defaultDurationSeconds)
        }

    @Test
    fun `repository flow emission updates uiState`() =
        runTest(testDispatcher) {
            val repo = FakeSettingsRepository()
            val viewModel = createViewModel(settingsRepo = repo)

            repo.setSoundEnabled(false)
            testScheduler.advanceUntilIdle()

            assertFalse(viewModel.uiState.value.soundEnabled)
        }

    @Test
    fun `settings flow emits updated values`() =
        runTest(testDispatcher) {
            val repo = FakeSettingsRepository()
            val viewModel = createViewModel(settingsRepo = repo)

            viewModel.setHapticsEnabled(false)
            testScheduler.advanceUntilIdle()

            val emitted = repo.settings.first()
            assertFalse(emitted.hapticsEnabled)
        }

    @Test
    fun `auth state reflected in uiState`() =
        runTest(testDispatcher) {
            val authRepo = FakeAuthRepository(AccountState.SignedIn("Test", "test@test.com", null))
            val viewModel = createViewModel(authRepo = authRepo)

            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isSignedIn)
        }

    @Test
    fun `pro entitlement reflected in uiState`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository(ProEntitlement.Pro())
            val viewModel = createViewModel(billingRepo = billingRepo)

            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isProUser)
        }

    @Test
    fun `showDeleteConfirmation sets showDeleteConfirmation true`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.showDeleteConfirmation()

            assertTrue(viewModel.uiState.value.showDeleteConfirmation)
        }

    @Test
    fun `dismissDeleteConfirmation resets delete state`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            viewModel.showDeleteConfirmation()
            viewModel.setDeleteConfirmationText("DELETE")
            viewModel.dismissDeleteConfirmation()

            assertFalse(viewModel.uiState.value.showDeleteConfirmation)
            assertEquals("", viewModel.uiState.value.deleteConfirmationText)
            assertTrue(viewModel.uiState.value.deleteIncludeBackup)
        }

    @Test
    fun `deleteAllData clears sessions and penalties and resets settings`() =
        runTest(testDispatcher) {
            val sessionRepo = FakeSessionRepository()
            val settingsRepo = FakeSettingsRepository()
            val viewModel = createViewModel(sessionRepo = sessionRepo, settingsRepo = settingsRepo)
            testScheduler.advanceUntilIdle()

            viewModel.deleteAllData()
            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.deleteSuccess)
            assertFalse(viewModel.uiState.value.isDeleting)
        }

    @Test
    fun `deleteAllData with signed in and include backup deletes backup and signs out`() =
        runTest(testDispatcher) {
            val authRepo = FakeAuthRepository(AccountState.SignedIn("Test", "test@test.com", null))
            val backupRepo = FakeBackupRepository()
            val viewModel = createViewModel(authRepo = authRepo, backupRepo = backupRepo)
            testScheduler.advanceUntilIdle()

            viewModel.deleteAllData()
            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.deleteSuccess)
        }
}

private class FakeBillingRepository(
    private val initialEntitlement: ProEntitlement = ProEntitlement.Free,
) : BillingRepository {
    override val products: Flow<List<phonedown.core.model.ProProduct>> = MutableStateFlow(emptyList())
    override val purchases: Flow<List<phonedown.core.model.ProPurchase>> = MutableStateFlow(emptyList())
    override val entitlement: Flow<ProEntitlement> = MutableStateFlow(initialEntitlement)

    override suspend fun loadProducts() {}

    override suspend fun launchPurchaseFlow(product: phonedown.core.model.ProProduct) {}

    override suspend fun restorePurchases() {}

    override suspend fun acknowledgePurchase(purchaseToken: String) {}
}

private class FakeAuthRepository(
    private val initialState: AccountState = AccountState.SignedOut,
) : AuthRepository {
    override val accountState: Flow<AccountState> = MutableStateFlow(initialState)

    override suspend fun signIn() {}

    override suspend fun signOut() {}

    override fun getAuthToken(): String? = null
}

private class FakeBackupRepository : BackupRepository {
    override suspend fun createBackup(
        sessions: List<FocusSession>,
        penaltyEvents: List<PenaltyEvent>,
        settings: UserSettings,
    ): BackupResult = BackupResult.Success("backup_1", System.currentTimeMillis())

    override suspend fun restoreBackup(): RestoreResult = RestoreResult.NoBackupFound

    override suspend fun getLastBackupTime(): Long? = null

    override suspend fun deleteBackup(): Boolean = true
}

private class FakeSessionRepository : SessionRepository {
    override suspend fun upsertSession(session: FocusSession) {}

    override fun observeSession(id: String): Flow<FocusSession?> = MutableStateFlow(null)

    override suspend fun getSession(id: String): FocusSession? = null

    override fun observeLatestSessions(limit: Int): Flow<List<FocusSession>> = MutableStateFlow(emptyList())

    override fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<FocusSession>> = MutableStateFlow(emptyList())

    override suspend fun getRecoverableSessions(): List<FocusSession> = emptyList()

    override suspend fun recordPenaltyEvent(event: PenaltyEvent) {}

    override suspend fun upsertSessionWithPenaltyEvent(
        session: FocusSession,
        event: PenaltyEvent,
    ) {}

    override fun observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>> = MutableStateFlow(emptyList())

    override suspend fun getPenaltyEvents(sessionId: String): List<PenaltyEvent> = emptyList()

    override suspend fun getAllSessions(): List<FocusSession> = emptyList()

    override suspend fun getAllPenaltyEvents(): List<PenaltyEvent> = emptyList()

    override suspend fun clearAllSessions() {}

    override suspend fun clearAllPenaltyEvents() {}
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
