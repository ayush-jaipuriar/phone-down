package phonedown.core.backup.mapper

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings

class BackupDataMapperTest {
    @Test
    fun `toBackupData maps domain models correctly`() {
        val session =
            FocusSession(
                id = "session-1",
                plannedDurationSeconds = 1500,
                requiredDurationSeconds = 1200,
                validFocusSeconds = 1500,
                actualElapsedSeconds = 1500,
                penaltySeconds = 0,
                interruptionCount = 0,
                minorInterruptionCount = 0,
                penaltyInterruptionCount = 0,
                startedAtEpochMillis = 1000,
                endedAtEpochMillis = 2500,
                startElapsedRealtime = 500,
                endElapsedRealtime = 2000,
                state = SessionState.Completed,
                result = SessionResult.CleanCompleted,
                clean = true,
                broken = false,
                callInterrupted = false,
                createdAtEpochMillis = 1000,
                updatedAtEpochMillis = 2500,
            )

        val penalty =
            PenaltyEvent(
                id = "penalty-1",
                sessionId = "session-1",
                type = PenaltyEventType.MinorPickup,
                startedAtEpochMillis = 1200,
                endedAtEpochMillis = 1210,
                durationSeconds = 10,
                penaltySeconds = 0,
            )

        val settings =
            UserSettings(
                defaultDurationSeconds = 1800,
                soundEnabled = false,
                hapticsEnabled = false,
                themeMode = ThemeMode.Dark,
                onboardingCompleted = true,
                backupOptIn = true,
                autoBackupEnabled = true,
                freeCustomDurationSeconds = 3000,
            )

        val backupData =
            BackupDataMapper.toBackupData(
                sessions = listOf(session),
                penaltyEvents = listOf(penalty),
                settings = settings,
            )

        assertEquals(1, backupData.schemaVersion)
        assertEquals(1, backupData.sessions.size)
        assertEquals("session-1", backupData.sessions.first().id)
        assertEquals("completed", backupData.sessions.first().state)
        assertEquals("clean_completed", backupData.sessions.first().result)
        assertEquals(1, backupData.penaltyEvents.size)
        assertEquals("minor_pickup", backupData.penaltyEvents.first().type)
        assertEquals("dark", backupData.settings.themeMode)
        assertEquals(1800, backupData.settings.defaultDurationSeconds)
    }

    @Test
    fun `fromBackupData maps dto to domain correctly`() {
        val backupData =
            BackupDataMapper.toBackupData(
                sessions =
                    listOf(
                        FocusSession(
                            id = "session-1",
                            plannedDurationSeconds = 1500,
                            requiredDurationSeconds = 1200,
                            validFocusSeconds = 1500,
                            actualElapsedSeconds = 1500,
                            penaltySeconds = 0,
                            interruptionCount = 0,
                            minorInterruptionCount = 0,
                            penaltyInterruptionCount = 0,
                            startedAtEpochMillis = 1000,
                            endedAtEpochMillis = 2500,
                            startElapsedRealtime = 500,
                            endElapsedRealtime = 2000,
                            state = SessionState.Completed,
                            result = SessionResult.CleanCompleted,
                            clean = true,
                            broken = false,
                            callInterrupted = false,
                            createdAtEpochMillis = 1000,
                            updatedAtEpochMillis = 2500,
                        ),
                    ),
                penaltyEvents = emptyList(),
                settings =
                    UserSettings(
                        defaultDurationSeconds = 1500,
                        themeMode = ThemeMode.System,
                    ),
            )

        val (sessions, penalties, settings) = BackupDataMapper.fromBackupData(backupData)

        assertEquals(1, sessions.size)
        assertEquals("session-1", sessions.first().id)
        assertEquals(SessionState.Completed, sessions.first().state)
        assertEquals(SessionResult.CleanCompleted, sessions.first().result)
        assertEquals(0, penalties.size)
        assertEquals(1500, settings.defaultDurationSeconds)
        assertEquals(ThemeMode.System, settings.themeMode)
    }

    @Test
    fun `round trip preserves all fields`() {
        val originalSession =
            FocusSession(
                id = "session-2",
                plannedDurationSeconds = 1800,
                requiredDurationSeconds = 1440,
                validFocusSeconds = 1200,
                actualElapsedSeconds = 2000,
                penaltySeconds = 60,
                interruptionCount = 1,
                minorInterruptionCount = 0,
                penaltyInterruptionCount = 1,
                startedAtEpochMillis = 5000,
                endedAtEpochMillis = 7000,
                startElapsedRealtime = 1000,
                endElapsedRealtime = 3000,
                state = SessionState.Completed,
                result = SessionResult.CompletedWithInterruption,
                clean = false,
                broken = false,
                callInterrupted = false,
                createdAtEpochMillis = 5000,
                updatedAtEpochMillis = 7000,
            )

        val originalSettings =
            UserSettings(
                defaultDurationSeconds = 1800,
                soundEnabled = false,
                hapticsEnabled = true,
                themeMode = ThemeMode.Light,
                onboardingCompleted = true,
                backupOptIn = true,
                autoBackupEnabled = false,
                freeCustomDurationSeconds = null,
            )

        val backupData =
            BackupDataMapper.toBackupData(
                sessions = listOf(originalSession),
                penaltyEvents = emptyList(),
                settings = originalSettings,
            )

        val (sessions, _, settings) = BackupDataMapper.fromBackupData(backupData)
        val restoredSession = sessions.first()

        assertEquals(originalSession.id, restoredSession.id)
        assertEquals(originalSession.plannedDurationSeconds, restoredSession.plannedDurationSeconds)
        assertEquals(originalSession.validFocusSeconds, restoredSession.validFocusSeconds)
        assertEquals(originalSession.penaltySeconds, restoredSession.penaltySeconds)
        assertEquals(originalSession.interruptionCount, restoredSession.interruptionCount)
        assertEquals(originalSession.clean, restoredSession.clean)
        assertEquals(originalSession.broken, restoredSession.broken)
        assertEquals(originalSettings.defaultDurationSeconds, settings.defaultDurationSeconds)
        assertEquals(originalSettings.soundEnabled, settings.soundEnabled)
        assertEquals(originalSettings.themeMode, settings.themeMode)
        assertNull(settings.freeCustomDurationSeconds)
    }
}
