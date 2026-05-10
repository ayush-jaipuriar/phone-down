package phonedown.app.account

import phonedown.app.runtime.ActiveSessionRuntimeCoordinator
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.RestorePayloadResult
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository
import javax.inject.Inject

interface BackupRestorer {
    suspend operator fun invoke(): RestoreBackupOutcome
}

class RestoreBackupUseCase
    @Inject
    constructor(
        private val backupRepository: BackupRepository,
        private val sessionRepository: SessionRepository,
        private val settingsRepository: SettingsRepository,
        private val runtimeCoordinator: ActiveSessionRuntimeCoordinator,
    ) : BackupRestorer {
        override suspend operator fun invoke(): RestoreBackupOutcome {
            if (runtimeCoordinator.hasActiveRuntime()) {
                return RestoreBackupOutcome.Failure("End your current focus session before restoring data.")
            }

            return when (val result = backupRepository.fetchRestorePayload()) {
                is RestorePayloadResult.Success -> {
                    val payload = result.payload
                    sessionRepository.replaceAllData(
                        sessions = payload.sessions,
                        penaltyEvents = payload.penaltyEvents,
                    )
                    settingsRepository.restoreSettings(payload.settings)
                    RestoreBackupOutcome.Success(
                        sessionsRestored = payload.sessions.size,
                        settingsRestored = true,
                    )
                }
                is RestorePayloadResult.Failure -> RestoreBackupOutcome.Failure(result.reason)
                RestorePayloadResult.NoBackupFound -> RestoreBackupOutcome.NoBackupFound
            }
        }
    }

sealed class RestoreBackupOutcome {
    data class Success(
        val sessionsRestored: Int,
        val settingsRestored: Boolean,
    ) : RestoreBackupOutcome()

    data class Failure(
        val reason: String,
    ) : RestoreBackupOutcome()

    data object NoBackupFound : RestoreBackupOutcome()
}
