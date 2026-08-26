package phonedown.app.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.first
import phonedown.core.model.AccountState
import phonedown.core.model.ProEntitlement
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.DriveAccessTokenProvider
import phonedown.core.model.repository.DriveAccessTokenResult
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository

class AutoBackupWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val entryPoint =
            EntryPointAccessors.fromApplication(
                applicationContext,
                AutoBackupWorkerEntryPoint::class.java,
            )

        val settingsRepository = entryPoint.settingsRepository()
        val authRepository = entryPoint.authRepository()
        val billingRepository = entryPoint.billingRepository()
        val driveAccessTokenProvider = entryPoint.driveAccessTokenProvider()
        val backupRepository = entryPoint.backupRepository()
        val sessionRepository = entryPoint.sessionRepository()

        val settings = settingsRepository.settings.first()
        val isSignedIn = authRepository.accountState.first() is AccountState.SignedIn
        val isPro = billingRepository.entitlement.first() is ProEntitlement.Pro
        if (!settings.backupOptIn || !settings.autoBackupEnabled || !isSignedIn || !isPro) {
            return Result.success()
        }

        when (driveAccessTokenProvider.getAccessToken()) {
            is DriveAccessTokenResult.Success -> Unit
            DriveAccessTokenResult.RequiresUserAction,
            DriveAccessTokenResult.SignedOut,
            -> return Result.success()

            is DriveAccessTokenResult.Failure -> return Result.retry()
        }

        val sessions = sessionRepository.getAllSessions()
        val penalties = sessionRepository.getAllPenaltyEvents()
        return when (val result = backupRepository.createBackup(sessions, penalties, settings)) {
            is phonedown.core.model.repository.BackupResult.Success -> {
                settingsRepository.setLastBackupEpochMillis(result.timestampMillis)
                Result.success()
            }
            is phonedown.core.model.repository.BackupResult.Failure -> Result.retry()
        }
    }
}

@EntryPoint
@InstallIn(SingletonComponent::class)
interface AutoBackupWorkerEntryPoint {
    fun authRepository(): AuthRepository

    fun backupRepository(): BackupRepository

    fun billingRepository(): BillingRepository

    fun driveAccessTokenProvider(): DriveAccessTokenProvider

    fun sessionRepository(): SessionRepository

    fun settingsRepository(): SettingsRepository
}
