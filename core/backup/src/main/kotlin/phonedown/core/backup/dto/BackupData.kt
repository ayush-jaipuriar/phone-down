package phonedown.core.backup.dto

import kotlinx.serialization.Serializable

@Serializable
data class BackupData(
    val schemaVersion: Int,
    val exportedAtMillis: Long,
    val sessions: List<BackupSession>,
    val penaltyEvents: List<BackupPenaltyEvent>,
    val settings: BackupSettings,
)

@Serializable
data class BackupSession(
    val id: String,
    val plannedDurationSeconds: Long,
    val requiredDurationSeconds: Long,
    val validFocusSeconds: Long,
    val actualElapsedSeconds: Long,
    val penaltySeconds: Long,
    val interruptionCount: Int,
    val minorInterruptionCount: Int,
    val penaltyInterruptionCount: Int,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val startElapsedRealtime: Long,
    val endElapsedRealtime: Long?,
    val state: String,
    val result: String?,
    val clean: Boolean,
    val broken: Boolean,
    val callInterrupted: Boolean,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long,
)

@Serializable
data class BackupPenaltyEvent(
    val id: String,
    val sessionId: String,
    val type: String,
    val startedAtEpochMillis: Long,
    val endedAtEpochMillis: Long?,
    val durationSeconds: Long,
    val penaltySeconds: Long,
)

@Serializable
data class BackupSettings(
    val defaultDurationSeconds: Long,
    val soundEnabled: Boolean,
    val hapticsEnabled: Boolean,
    val themeMode: String,
    val onboardingCompleted: Boolean,
    val backupOptIn: Boolean,
    val autoBackupEnabled: Boolean,
    val freeCustomDurationSeconds: Long?,
)
