package phonedown.app.account

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.core.model.AccountState
import phonedown.core.model.ProEntitlement
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BillingRepository

@OptIn(ExperimentalCoroutinesApi::class)
class AccountViewModelTest {
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
    fun `initial uiState reflects signed out`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()
            testScheduler.advanceUntilIdle()

            assertEquals(AccountState.SignedOut, viewModel.uiState.value.accountState)
            assertFalse(viewModel.uiState.value.isProUser)
        }

    @Test
    fun `signed in state reflected in uiState`() =
        runTest(testDispatcher) {
            val authRepo = FakeAuthRepository(AccountState.SignedIn("Test User", "test@test.com", null))
            val viewModel = createViewModel(authRepo = authRepo)

            // Collect the state to activate the flow
            val job = launch { viewModel.uiState.collect {} }
            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.accountState is AccountState.SignedIn)
            assertEquals("Test User", (viewModel.uiState.value.accountState as AccountState.SignedIn).displayName)
            job.cancel()
        }

    @Test
    fun `pro entitlement reflected in uiState`() =
        runTest(testDispatcher) {
            val billingRepo = FakeBillingRepository(ProEntitlement.Pro())
            val viewModel = createViewModel(billingRepo = billingRepo)

            // Collect the state to activate the flow
            val job = launch { viewModel.uiState.collect {} }
            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.uiState.value.isProUser)
            job.cancel()
        }

    @Test
    fun `signIn calls repository`() =
        runTest(testDispatcher) {
            val authRepo = FakeAuthRepository()
            val viewModel = createViewModel(authRepo = authRepo)

            viewModel.signIn()
            testScheduler.advanceUntilIdle()

            assertTrue(authRepo.signInCalled)
        }

    @Test
    fun `signOut calls repository`() =
        runTest(testDispatcher) {
            val authRepo = FakeAuthRepository(AccountState.SignedIn("Test", "test@test.com", null))
            val viewModel = createViewModel(authRepo = authRepo)

            viewModel.signOut()
            testScheduler.advanceUntilIdle()

            assertTrue(authRepo.signOutCalled)
        }

    @Test
    fun `restoreBackup success updates restoreState`() =
        runTest(testDispatcher) {
            val restorer = FakeBackupRestorer(RestoreBackupOutcome.Success(5, true))
            val viewModel = createViewModel(backupRestorer = restorer)

            viewModel.restoreBackup()
            testScheduler.advanceUntilIdle()

            val state = viewModel.restoreState.value
            assertTrue(state is RestoreState.Success)
            assertEquals(5, (state as RestoreState.Success).sessionsRestored)
        }

    @Test
    fun `restoreBackup failure updates restoreState`() =
        runTest(testDispatcher) {
            val restorer = FakeBackupRestorer(RestoreBackupOutcome.Failure("Network error"))
            val viewModel = createViewModel(backupRestorer = restorer)

            viewModel.restoreBackup()
            testScheduler.advanceUntilIdle()

            val state = viewModel.restoreState.value
            assertTrue(state is RestoreState.Error)
            assertEquals("Network error", (state as RestoreState.Error).message)
        }

    @Test
    fun `restoreBackup no backup found shows error`() =
        runTest(testDispatcher) {
            val restorer = FakeBackupRestorer(RestoreBackupOutcome.NoBackupFound)
            val viewModel = createViewModel(backupRestorer = restorer)

            viewModel.restoreBackup()
            testScheduler.advanceUntilIdle()

            val state = viewModel.restoreState.value
            assertTrue(state is RestoreState.Error)
            assertEquals("No backup found", (state as RestoreState.Error).message)
        }

    @Test
    fun `clearRestoreState resets to idle`() =
        runTest(testDispatcher) {
            val restorer = FakeBackupRestorer(RestoreBackupOutcome.Success(5, true))
            val viewModel = createViewModel(backupRestorer = restorer)

            viewModel.restoreBackup()
            testScheduler.advanceUntilIdle()
            assertTrue(viewModel.restoreState.value is RestoreState.Success)

            viewModel.clearRestoreState()

            assertTrue(viewModel.restoreState.value is RestoreState.Idle)
        }

    private fun createViewModel(
        authRepo: AuthRepository = FakeAuthRepository(),
        billingRepo: BillingRepository = FakeBillingRepository(ProEntitlement.Free),
        backupRestorer: BackupRestorer = FakeBackupRestorer(RestoreBackupOutcome.NoBackupFound),
    ): AccountViewModel =
        AccountViewModel(
            authRepository = authRepo,
            billingRepository = billingRepo,
            restoreBackupUseCase = backupRestorer,
        )
}

private class FakeAuthRepository(
    private val initialState: AccountState = AccountState.SignedOut,
) : AuthRepository {
    val stateFlow = MutableStateFlow(initialState)
    var signInCalled = false
    var signOutCalled = false

    override val accountState = stateFlow

    override suspend fun signIn() {
        signInCalled = true
        stateFlow.value = AccountState.SignedIn("Test User", "test@test.com", null)
    }

    override suspend fun signOut() {
        signOutCalled = true
        stateFlow.value = AccountState.SignedOut
    }

    override fun getAuthToken(): String? = null
}

private class FakeBillingRepository(
    private val initialEntitlement: ProEntitlement = ProEntitlement.Free,
) : BillingRepository {
    override val products = kotlinx.coroutines.flow.flowOf(emptyList<phonedown.core.model.ProProduct>())
    override val purchases = kotlinx.coroutines.flow.flowOf(emptyList<phonedown.core.model.ProPurchase>())
    override val entitlement = kotlinx.coroutines.flow.flowOf(initialEntitlement)

    override suspend fun loadProducts() {}

    override suspend fun launchPurchaseFlow(product: phonedown.core.model.ProProduct) {}

    override suspend fun restorePurchases() {}

    override suspend fun acknowledgePurchase(purchaseToken: String) {}
}

private class FakeBackupRestorer(
    private val outcome: RestoreBackupOutcome,
) : BackupRestorer {
    override suspend fun invoke(): RestoreBackupOutcome = outcome
}
