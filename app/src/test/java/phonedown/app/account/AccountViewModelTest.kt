package phonedown.app.account

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import phonedown.app.backup.AutoBackupScheduling
import phonedown.app.backup.DriveAuthorizationCoordinator
import phonedown.app.backup.DriveAuthorizationUiStep
import phonedown.core.model.AccountState
import phonedown.core.model.GoogleAccount
import phonedown.core.model.repository.AuthRepository

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
    fun `completeSignIn applies Google account to repository`() =
        runTest(testDispatcher) {
            val authRepo = FakeAuthRepository()
            val viewModel = createViewModel(authRepo = authRepo)

            viewModel.beginSignIn()
            viewModel.completeSignIn(
                GoogleAccount(
                    accountId = "account-1",
                    displayName = "Real User",
                    email = "real@test.com",
                    photoUrl = null,
                ),
            )
            testScheduler.advanceUntilIdle()

            assertTrue(viewModel.signInState.value is SignInState.Idle)
            assertEquals("real@test.com", (authRepo.stateFlow.value as AccountState.SignedIn).email)
            assertEquals("account-1", authRepo.lastAppliedAccount?.accountId)
        }

    @Test
    fun `failSignIn exposes error state`() =
        runTest(testDispatcher) {
            val viewModel = createViewModel()

            viewModel.failSignIn("Google Sign-In failed")

            val state = viewModel.signInState.value
            assertTrue(state is SignInState.Error)
            assertEquals("Google Sign-In failed", (state as SignInState.Error).message)
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
    fun `restoreBackup no backup found shows dedicated empty state`() =
        runTest(testDispatcher) {
            val restorer = FakeBackupRestorer(RestoreBackupOutcome.NoBackupFound)
            val viewModel = createViewModel(backupRestorer = restorer)

            viewModel.restoreBackup()
            testScheduler.advanceUntilIdle()

            val state = viewModel.restoreState.value
            assertTrue(state is RestoreState.NoBackupFound)
            assertEquals("No backup found for this account.", (state as RestoreState.NoBackupFound).message)
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
        backupRestorer: BackupRestorer = FakeBackupRestorer(RestoreBackupOutcome.NoBackupFound),
        driveAuthorizationCoordinator: DriveAuthorizationCoordinator = FakeDriveAuthorizationCoordinator(),
        autoBackupScheduling: AutoBackupScheduling = FakeAutoBackupScheduling(),
    ): AccountViewModel =
        AccountViewModel(
            authRepository = authRepo,
            restoreBackupUseCase = backupRestorer,
            driveAuthorizationManager = driveAuthorizationCoordinator,
            autoBackupScheduler = autoBackupScheduling,
        )
}

private class FakeAuthRepository(
    private val initialState: AccountState = AccountState.SignedOut,
) : AuthRepository {
    val stateFlow = MutableStateFlow(initialState)
    var signOutCalled = false
    var lastAppliedAccount: GoogleAccount? = null

    override val accountState = stateFlow

    override suspend fun applyGoogleAccount(account: GoogleAccount) {
        lastAppliedAccount = account
        stateFlow.value =
            AccountState.SignedIn(
                displayName = account.displayName,
                email = account.email,
                photoUrl = account.photoUrl,
                accountId = account.accountId,
            )
    }

    override suspend fun signOut() {
        signOutCalled = true
        stateFlow.value = AccountState.SignedOut
    }
}

private class FakeBackupRestorer(
    private val outcome: RestoreBackupOutcome,
) : BackupRestorer {
    override suspend fun invoke(): RestoreBackupOutcome = outcome
}

private class FakeDriveAuthorizationCoordinator : DriveAuthorizationCoordinator {
    override suspend fun beginAuthorization(): DriveAuthorizationUiStep = DriveAuthorizationUiStep.Cancelled

    override fun completeAuthorization(
        resultCode: Int,
        data: android.content.Intent?,
    ): DriveAuthorizationUiStep = DriveAuthorizationUiStep.Cancelled

    override fun clearCachedAccessToken() {}
}

private class FakeAutoBackupScheduling : AutoBackupScheduling {
    override suspend fun refreshSchedule() {}
}
