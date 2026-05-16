package phonedown.app.backup

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.AccountState
import phonedown.core.model.ProEntitlement
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.BillingRepository
import phonedown.core.model.repository.SettingsRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class AutoBackupScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val billingRepository: BillingRepository,
        private val clock: Clock,
    ) : AutoBackupScheduling {
        override suspend fun refreshSchedule() {
            val settings = settingsRepository.settings.first()
            val isSignedIn = authRepository.accountState.first() is AccountState.SignedIn
            val isPro = billingRepository.entitlement.first() is ProEntitlement.Pro
            val shouldSchedule = settings.backupOptIn && settings.autoBackupEnabled && isSignedIn && isPro

            if (!shouldSchedule) {
                WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
                return
            }

            val now = clock.currentTimeMillis()
            val lastBackup = settings.lastBackupEpochMillis
            val initialDelayMillis =
                if (lastBackup == null) {
                    0L
                } else {
                    max(0L, BACKUP_INTERVAL_MILLIS - (now - lastBackup))
                }

            val request =
                PeriodicWorkRequestBuilder<AutoBackupWorker>(1, TimeUnit.DAYS)
                    .setConstraints(
                        Constraints
                            .Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build(),
                    ).setInitialDelay(initialDelayMillis, TimeUnit.MILLISECONDS)
                    .addTag(UNIQUE_WORK_NAME)
                    .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                UNIQUE_WORK_NAME,
                ExistingPeriodicWorkPolicy.UPDATE,
                request,
            )
        }

        private companion object {
            const val UNIQUE_WORK_NAME = "phone_down_auto_backup"
            const val BACKUP_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L
        }
    }

interface AutoBackupScheduling {
    suspend fun refreshSchedule()
}
