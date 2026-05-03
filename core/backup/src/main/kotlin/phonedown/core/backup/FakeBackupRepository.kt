package phonedown.core.backup

import kotlinx.coroutines.delay
import phonedown.core.backup.dto.BackupData
import phonedown.core.backup.mapper.BackupDataMapper
import phonedown.core.backup.serializer.BackupSerializer
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.BackupResult
import phonedown.core.model.repository.RestoreResult

/**
 * Fake backup repository for development and testing.
 *
 * Stores backups in memory as serialized JSON, simulating Google Drive operations.
 * Not for production use.
 */
class FakeBackupRepository : BackupRepository {

    private var storedJson: String? = null
    private var lastBackupTime: Long? = null

    override suspend fun createBackup(
        sessions: List<FocusSession>,
        penaltyEvents: List<PenaltyEvent>,
        settings: UserSettings,
    ): BackupResult {
        delay(1_500)
        return try {
            val backupData = BackupDataMapper.toBackupData(sessions, penaltyEvents, settings)
            storedJson = BackupSerializer.serialize(backupData)
            val backupId = "backup_${System.currentTimeMillis()}"
            val timestamp = System.currentTimeMillis()
            lastBackupTime = timestamp
            BackupResult.Success(backupId, timestamp)
        } catch (e: Exception) {
            BackupResult.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun restoreBackup(): RestoreResult {
        delay(1_000)
        val json = storedJson ?: return RestoreResult.NoBackupFound
        return try {
            val backupData = BackupSerializer.deserialize(json)
            if (!BackupSerializer.validateSchemaVersion(backupData)) {
                return RestoreResult.Failure("Unsupported backup version: ${backupData.schemaVersion}")
            }
            val (sessions, _, _) = BackupDataMapper.fromBackupData(backupData)
            RestoreResult.Success(
                sessionsRestored = sessions.size,
                settingsRestored = true,
            )
        } catch (e: Exception) {
            RestoreResult.Failure(e.message ?: "Unknown error")
        }
    }

    override suspend fun getLastBackupTime(): Long? = lastBackupTime

    override suspend fun deleteBackup(): Boolean {
        delay(500)
        val hadBackup = storedJson != null
        storedJson = null
        lastBackupTime = null
        return hadBackup
    }
}
