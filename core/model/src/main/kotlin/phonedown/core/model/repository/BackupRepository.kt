package phonedown.core.model.repository

import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.UserSettings

sealed class BackupResult {
    data class Success(
        val backupId: String,
        val timestampMillis: Long,
    ) : BackupResult()

    data class Failure(
        val reason: String,
    ) : BackupResult()
}

sealed class RestoreResult {
    data class Success(
        val sessionsRestored: Int,
        val settingsRestored: Boolean,
    ) : RestoreResult()

    data class Failure(
        val reason: String,
    ) : RestoreResult()

    data object NoBackupFound : RestoreResult()
}

sealed class RestorePayloadResult {
    data class Success(
        val payload: RestorePayload,
    ) : RestorePayloadResult()

    data class Failure(
        val reason: String,
    ) : RestorePayloadResult()

    data object NoBackupFound : RestorePayloadResult()
}

sealed class DeleteBackupResult {
    data object Deleted : DeleteBackupResult()

    data object NoBackupFound : DeleteBackupResult()

    data class Failure(
        val reason: String,
    ) : DeleteBackupResult()
}

data class RestorePayload(
    val sessions: List<FocusSession>,
    val penaltyEvents: List<PenaltyEvent>,
    val settings: UserSettings,
)

interface BackupRepository {
    suspend fun createBackup(
        sessions: List<FocusSession>,
        penaltyEvents: List<PenaltyEvent>,
        settings: UserSettings,
    ): BackupResult

    suspend fun restoreBackup(): RestoreResult

    suspend fun fetchRestorePayload(): RestorePayloadResult =
        when (val result = restoreBackup()) {
            is RestoreResult.Success ->
                RestorePayloadResult.Failure("Backup payload is unavailable from this repository")
            is RestoreResult.Failure -> RestorePayloadResult.Failure(result.reason)
            RestoreResult.NoBackupFound -> RestorePayloadResult.NoBackupFound
        }

    suspend fun getLastBackupTime(): Long?

    suspend fun deleteBackup(): DeleteBackupResult
}
