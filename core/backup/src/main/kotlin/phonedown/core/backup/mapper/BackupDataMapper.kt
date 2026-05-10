package phonedown.core.backup.mapper

import phonedown.core.backup.dto.BackupData
import phonedown.core.backup.dto.BackupPenaltyEvent
import phonedown.core.backup.dto.BackupSession
import phonedown.core.backup.dto.BackupSettings
import phonedown.core.backup.serializer.BackupSerializer
import phonedown.core.database.mapper.toPenaltyEventType
import phonedown.core.database.mapper.toSessionResult
import phonedown.core.database.mapper.toSessionState
import phonedown.core.database.mapper.toStorageString
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings

object BackupDataMapper {
    fun toBackupData(
        sessions: List<FocusSession>,
        penaltyEvents: List<PenaltyEvent>,
        settings: UserSettings,
    ): BackupData =
        BackupData(
            schemaVersion = BackupSerializer.CURRENT_SCHEMA_VERSION,
            exportedAtMillis = System.currentTimeMillis(),
            sessions = sessions.map { toBackupSession(it) },
            penaltyEvents = penaltyEvents.map { toBackupPenaltyEvent(it) },
            settings = toBackupSettings(settings),
        )

    fun fromBackupData(data: BackupData): Triple<List<FocusSession>, List<PenaltyEvent>, UserSettings> =
        Triple(
            data.sessions.map { fromBackupSession(it) },
            data.penaltyEvents.map { fromBackupPenaltyEvent(it) },
            fromBackupSettings(data.settings),
        )

    private fun toBackupSession(session: FocusSession): BackupSession =
        BackupSession(
            id = session.id,
            plannedDurationSeconds = session.plannedDurationSeconds,
            requiredDurationSeconds = session.requiredDurationSeconds,
            validFocusSeconds = session.validFocusSeconds,
            actualElapsedSeconds = session.actualElapsedSeconds,
            penaltySeconds = session.penaltySeconds,
            interruptionCount = session.interruptionCount,
            minorInterruptionCount = session.minorInterruptionCount,
            penaltyInterruptionCount = session.penaltyInterruptionCount,
            startedAtEpochMillis = session.startedAtEpochMillis,
            endedAtEpochMillis = session.endedAtEpochMillis,
            startElapsedRealtime = session.startElapsedRealtime,
            endElapsedRealtime = session.endElapsedRealtime,
            state = session.state.toStorageString(),
            result = session.result?.toStorageString(),
            clean = session.clean,
            broken = session.broken,
            callInterrupted = session.callInterrupted,
            createdAtEpochMillis = session.createdAtEpochMillis,
            updatedAtEpochMillis = session.updatedAtEpochMillis,
        )

    private fun fromBackupSession(dto: BackupSession): FocusSession =
        FocusSession(
            id = dto.id,
            plannedDurationSeconds = dto.plannedDurationSeconds,
            requiredDurationSeconds = dto.requiredDurationSeconds,
            validFocusSeconds = dto.validFocusSeconds,
            actualElapsedSeconds = dto.actualElapsedSeconds,
            penaltySeconds = dto.penaltySeconds,
            interruptionCount = dto.interruptionCount,
            minorInterruptionCount = dto.minorInterruptionCount,
            penaltyInterruptionCount = dto.penaltyInterruptionCount,
            startedAtEpochMillis = dto.startedAtEpochMillis,
            endedAtEpochMillis = dto.endedAtEpochMillis,
            startElapsedRealtime = dto.startElapsedRealtime,
            endElapsedRealtime = dto.endElapsedRealtime,
            state = dto.state.toSessionState(),
            result = dto.result?.toSessionResult(),
            clean = dto.clean,
            broken = dto.broken,
            callInterrupted = dto.callInterrupted,
            createdAtEpochMillis = dto.createdAtEpochMillis,
            updatedAtEpochMillis = dto.updatedAtEpochMillis,
        )

    private fun toBackupPenaltyEvent(event: PenaltyEvent): BackupPenaltyEvent =
        BackupPenaltyEvent(
            id = event.id,
            sessionId = event.sessionId,
            type = event.type.toStorageString(),
            startedAtEpochMillis = event.startedAtEpochMillis,
            endedAtEpochMillis = event.endedAtEpochMillis,
            durationSeconds = event.durationSeconds,
            penaltySeconds = event.penaltySeconds,
        )

    private fun fromBackupPenaltyEvent(dto: BackupPenaltyEvent): PenaltyEvent =
        PenaltyEvent(
            id = dto.id,
            sessionId = dto.sessionId,
            type = dto.type.toPenaltyEventType(),
            startedAtEpochMillis = dto.startedAtEpochMillis,
            endedAtEpochMillis = dto.endedAtEpochMillis,
            durationSeconds = dto.durationSeconds,
            penaltySeconds = dto.penaltySeconds,
        )

    private fun toBackupSettings(settings: UserSettings): BackupSettings =
        BackupSettings(
            defaultDurationSeconds = settings.defaultDurationSeconds,
            soundEnabled = settings.soundEnabled,
            hapticsEnabled = settings.hapticsEnabled,
            themeMode = settings.themeMode.toBackupString(),
            onboardingCompleted = settings.onboardingCompleted,
            backupOptIn = settings.backupOptIn,
            autoBackupEnabled = settings.autoBackupEnabled,
            freeCustomDurationSeconds = settings.freeCustomDurationSeconds,
        )

    private fun fromBackupSettings(dto: BackupSettings): UserSettings =
        UserSettings(
            defaultDurationSeconds = dto.defaultDurationSeconds,
            soundEnabled = dto.soundEnabled,
            hapticsEnabled = dto.hapticsEnabled,
            themeMode = dto.themeMode.toThemeMode(),
            onboardingCompleted = dto.onboardingCompleted,
            backupOptIn = dto.backupOptIn,
            autoBackupEnabled = dto.autoBackupEnabled,
            freeCustomDurationSeconds = dto.freeCustomDurationSeconds,
        )

    private fun ThemeMode.toBackupString(): String =
        when (this) {
            ThemeMode.System -> "system"
            ThemeMode.Light -> "light"
            ThemeMode.Dark -> "dark"
        }

    private fun String.toThemeMode(): ThemeMode =
        when (this) {
            "system", "System" -> ThemeMode.System
            "light", "Light" -> ThemeMode.Light
            "dark", "Dark" -> ThemeMode.Dark
            else -> ThemeMode.System
        }
}
