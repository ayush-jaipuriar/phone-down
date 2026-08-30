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
import phonedown.core.model.repository.AuthRepository
import phonedown.core.model.repository.SettingsRepository
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AutoBackupScheduler
    @Inject
    constructor(
        @ApplicationContext private val context: Context,
        private val settingsRepository: SettingsRepository,
        private val authRepository: AuthRepository,
        private val clock: Clock,
    ) : AutoBackupScheduling {
        override suspend fun refreshSchedule() {
            val settings = settingsRepository.settings.first()
            val isSignedIn = authRepository.accountState.first() is AccountState.SignedIn
            val decision =
                if (
                    !isAutoBackupEligible(
                        backupOptIn = settings.backupOptIn,
                        autoBackupEnabled = settings.autoBackupEnabled,
                        isSignedIn = isSignedIn,
                    )
                ) {
                    AutoBackupScheduleDecision.Cancel
                } else {
                    autoBackupScheduleDecision(
                        isEligible = true,
                        lastBackupEpochMillis = settings.lastBackupEpochMillis,
                        currentTimeMillis = clock.currentTimeMillis(),
                    )
                }

            when (decision) {
                AutoBackupScheduleDecision.Cancel -> {
                    WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
                }

                is AutoBackupScheduleDecision.Schedule -> {
                    val request =
                        PeriodicWorkRequestBuilder<AutoBackupWorker>(1, TimeUnit.DAYS)
                            .setConstraints(
                                Constraints
                                    .Builder()
                                    .setRequiredNetworkType(NetworkType.CONNECTED)
                                    .build(),
                            ).setInitialDelay(decision.initialDelayMillis, TimeUnit.MILLISECONDS)
                            .addTag(UNIQUE_WORK_NAME)
                            .build()

                    WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                        UNIQUE_WORK_NAME,
                        ExistingPeriodicWorkPolicy.UPDATE,
                        request,
                    )
                }
            }
        }

        private companion object {
            const val UNIQUE_WORK_NAME = "phone_down_auto_backup"
        }
    }

interface AutoBackupScheduling {
    suspend fun refreshSchedule()
}
