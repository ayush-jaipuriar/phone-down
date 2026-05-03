package phonedown.core.model.repository

import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.UserSettings

sealed class BackupResult {
    data class Success(val backupId: String, val timestampMillis: Long) : BackupResult()
    data class Failure(val reason: String) : BackupResult()
}

sealed class RestoreResult {
    data class Success(
        val sessionsRestored: Int,
        val settingsRestored: Boolean,
    ) : RestoreResult()
    data class Failure(val reason: String) : RestoreResult()
    data object NoBackupFound : RestoreResult()
}

interface BackupRepository {
    suspend fun createBackup(
        sessions: List<FocusSession>,
        penaltyEvents: List<PenaltyEvent>,
        settings: UserSettings,
    ): BackupResult

    suspend fun restoreBackup(): RestoreResult

    suspend fun getLastBackupTime(): Long?

    suspend fun deleteBackup(): Boolean
}
