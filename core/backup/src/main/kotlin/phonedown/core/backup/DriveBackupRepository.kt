package phonedown.core.backup

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import phonedown.core.backup.mapper.BackupDataMapper
import phonedown.core.backup.serializer.BackupSerializer
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.BackupRepository
import phonedown.core.model.repository.BackupResult
import phonedown.core.model.repository.DeleteBackupResult
import phonedown.core.model.repository.DriveAccessTokenProvider
import phonedown.core.model.repository.DriveAccessTokenResult
import phonedown.core.model.repository.RestorePayload
import phonedown.core.model.repository.RestorePayloadResult
import phonedown.core.model.repository.RestoreResult
import phonedown.core.model.repository.SettingsRepository

class DriveBackupRepository
    constructor(
        private val driveAppDataClient: DriveAppDataClient,
        private val driveAccessTokenProvider: DriveAccessTokenProvider,
        private val settingsRepository: SettingsRepository,
        private val clock: Clock,
    ) : BackupRepository {
        override suspend fun createBackup(
            sessions: List<FocusSession>,
            penaltyEvents: List<PenaltyEvent>,
            settings: UserSettings,
        ): BackupResult {
            return withContext(Dispatchers.IO) {
                val accessToken =
                    when (val tokenResult = driveAccessTokenProvider.getAccessToken()) {
                        is DriveAccessTokenResult.Success -> tokenResult.accessToken
                        DriveAccessTokenResult.RequiresUserAction -> {
                            return@withContext BackupResult.Failure("Google Drive permission is needed. Open Backup & Restore and try again.")
                        }
                        DriveAccessTokenResult.SignedOut -> {
                            return@withContext BackupResult.Failure("Sign in to Google before using backup.")
                        }
                        is DriveAccessTokenResult.Failure -> {
                            return@withContext BackupResult.Failure(tokenResult.reason)
                        }
                    }

                try {
                    val backupData = BackupDataMapper.toBackupData(sessions, penaltyEvents, settings)
                    val payload = BackupSerializer.serialize(backupData)
                    val existingBackups = driveAppDataClient.listBackupFiles(accessToken)
                    val createdBackup = driveAppDataClient.createBackup(accessToken, payload)
                    existingBackups
                        .asSequence()
                        .filter { it.id != createdBackup.id }
                        .forEach { driveAppDataClient.deleteFile(accessToken, it.id) }

                    BackupResult.Success(
                        backupId = createdBackup.id,
                        timestampMillis = clock.currentTimeMillis(),
                    )
                } catch (exception: DriveUnauthorizedException) {
                    driveAccessTokenProvider.clearCachedAccessToken()
                    BackupResult.Failure("Google Drive authorization expired. Please try backup again.")
                } catch (exception: DriveApiException) {
                    BackupResult.Failure(exception.message ?: "Backup failed.")
                } catch (exception: Exception) {
                    BackupResult.Failure(exception.message ?: "Backup failed.")
                }
            }
        }

        override suspend fun restoreBackup(): RestoreResult =
            when (val result = fetchRestorePayload()) {
                is RestorePayloadResult.Success ->
                    RestoreResult.Success(
                        sessionsRestored = result.payload.sessions.size,
                        settingsRestored = true,
                    )
                is RestorePayloadResult.Failure -> RestoreResult.Failure(result.reason)
                RestorePayloadResult.NoBackupFound -> RestoreResult.NoBackupFound
            }

        override suspend fun fetchRestorePayload(): RestorePayloadResult {
            return withContext(Dispatchers.IO) {
                val accessToken =
                    when (val tokenResult = driveAccessTokenProvider.getAccessToken()) {
                        is DriveAccessTokenResult.Success -> tokenResult.accessToken
                        DriveAccessTokenResult.RequiresUserAction -> {
                            return@withContext RestorePayloadResult.Failure("Google Drive permission is needed. Open Backup & Restore and try again.")
                        }
                        DriveAccessTokenResult.SignedOut -> {
                            return@withContext RestorePayloadResult.Failure("Sign in to Google before restoring from backup.")
                        }
                        is DriveAccessTokenResult.Failure -> {
                            return@withContext RestorePayloadResult.Failure(tokenResult.reason)
                        }
                    }

                try {
                    val latestBackup = driveAppDataClient.listBackupFiles(accessToken).firstOrNull()
                        ?: return@withContext RestorePayloadResult.NoBackupFound
                    val payloadJson = driveAppDataClient.downloadBackup(accessToken, latestBackup.id)
                    val backupData = BackupSerializer.deserialize(payloadJson)
                    if (!BackupSerializer.validateSchemaVersion(backupData)) {
                        return@withContext RestorePayloadResult.Failure("Unsupported backup version: ${backupData.schemaVersion}")
                    }
                    val (sessions, penaltyEvents, settings) = BackupDataMapper.fromBackupData(backupData)
                    RestorePayloadResult.Success(
                        RestorePayload(
                            sessions = sessions,
                            penaltyEvents = penaltyEvents,
                            settings = settings,
                        ),
                    )
                } catch (exception: DriveUnauthorizedException) {
                    driveAccessTokenProvider.clearCachedAccessToken()
                    RestorePayloadResult.Failure("Google Drive authorization expired. Please try restore again.")
                } catch (exception: DriveApiException) {
                    RestorePayloadResult.Failure(exception.message ?: "Restore failed.")
                } catch (exception: Exception) {
                    RestorePayloadResult.Failure(exception.message ?: "Restore failed.")
                }
            }
        }

        override suspend fun getLastBackupTime(): Long? = settingsRepository.settings.first().lastBackupEpochMillis

        override suspend fun deleteBackup(): DeleteBackupResult {
            return withContext(Dispatchers.IO) {
                val accessToken =
                    when (val tokenResult = driveAccessTokenProvider.getAccessToken()) {
                        is DriveAccessTokenResult.Success -> tokenResult.accessToken
                        DriveAccessTokenResult.RequiresUserAction -> {
                            return@withContext DeleteBackupResult.Failure("Google Drive permission is needed before deleting your cloud backup.")
                        }
                        DriveAccessTokenResult.SignedOut -> {
                            return@withContext DeleteBackupResult.Failure("Sign in to Google before deleting your cloud backup.")
                        }
                        is DriveAccessTokenResult.Failure -> {
                            return@withContext DeleteBackupResult.Failure(tokenResult.reason)
                        }
                    }

                try {
                    val backups = driveAppDataClient.listBackupFiles(accessToken)
                    if (backups.isEmpty()) {
                        return@withContext DeleteBackupResult.NoBackupFound
                    }
                    backups.forEach { driveAppDataClient.deleteFile(accessToken, it.id) }
                    DeleteBackupResult.Deleted
                } catch (_: DriveUnauthorizedException) {
                    driveAccessTokenProvider.clearCachedAccessToken()
                    DeleteBackupResult.Failure("Google Drive authorization expired. Please try deleting the cloud backup again.")
                } catch (exception: DriveApiException) {
                    DeleteBackupResult.Failure(exception.message ?: "Deleting cloud backup failed.")
                } catch (exception: Exception) {
                    DeleteBackupResult.Failure(exception.message ?: "Deleting cloud backup failed.")
                }
            }
        }
    }
