package phonedown.core.backup.serializer

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.backup.dto.BackupData
import phonedown.core.backup.dto.BackupSession
import phonedown.core.backup.dto.BackupSettings

class BackupSerializerTest {
    @Test
    fun `serialize and deserialize round trip`() {
        val original =
            BackupData(
                schemaVersion = 1,
                exportedAtMillis = 1717420800000,
                sessions =
                    listOf(
                        BackupSession(
                            id = "session-1",
                            plannedDurationSeconds = 1500,
                            requiredDurationSeconds = 1200,
                            validFocusSeconds = 1500,
                            actualElapsedSeconds = 1500,
                            penaltySeconds = 0,
                            interruptionCount = 0,
                            minorInterruptionCount = 0,
                            penaltyInterruptionCount = 0,
                            startedAtEpochMillis = 1717420800000,
                            endedAtEpochMillis = 1717422300000,
                            startElapsedRealtime = 1000000,
                            endElapsedRealtime = 1001500,
                            state = "COMPLETED",
                            result = "CLEAN_COMPLETED",
                            clean = true,
                            broken = false,
                            callInterrupted = false,
                            createdAtEpochMillis = 1717420800000,
                            updatedAtEpochMillis = 1717422300000,
                        ),
                    ),
                penaltyEvents = emptyList(),
                settings =
                    BackupSettings(
                        defaultDurationSeconds = 1500,
                        soundEnabled = true,
                        hapticsEnabled = true,
                        themeMode = "System",
                        onboardingCompleted = true,
                        backupOptIn = true,
                        autoBackupEnabled = true,
                        freeCustomDurationSeconds = null,
                    ),
            )

        val json = BackupSerializer.serialize(original)
        val deserialized = BackupSerializer.deserialize(json)

        assertEquals(original.schemaVersion, deserialized.schemaVersion)
        assertEquals(original.exportedAtMillis, deserialized.exportedAtMillis)
        assertEquals(original.sessions.size, deserialized.sessions.size)
        assertEquals(original.sessions.first().id, deserialized.sessions.first().id)
        assertEquals(original.settings.themeMode, deserialized.settings.themeMode)
    }

    @Test
    fun `validateSchemaVersion returns true for current version`() {
        val data =
            BackupData(
                schemaVersion = BackupSerializer.CURRENT_SCHEMA_VERSION,
                exportedAtMillis = 0,
                sessions = emptyList(),
                penaltyEvents = emptyList(),
                settings =
                    BackupSettings(
                        defaultDurationSeconds = 1500,
                        soundEnabled = true,
                        hapticsEnabled = true,
                        themeMode = "System",
                        onboardingCompleted = false,
                        backupOptIn = false,
                        autoBackupEnabled = false,
                        freeCustomDurationSeconds = null,
                    ),
            )
        assertTrue(BackupSerializer.validateSchemaVersion(data))
    }

    @Test
    fun `validateSchemaVersion returns false for mismatch`() {
        val data =
            BackupData(
                schemaVersion = 999,
                exportedAtMillis = 0,
                sessions = emptyList(),
                penaltyEvents = emptyList(),
                settings =
                    BackupSettings(
                        defaultDurationSeconds = 1500,
                        soundEnabled = true,
                        hapticsEnabled = true,
                        themeMode = "System",
                        onboardingCompleted = false,
                        backupOptIn = false,
                        autoBackupEnabled = false,
                        freeCustomDurationSeconds = null,
                    ),
            )
        assertFalse(BackupSerializer.validateSchemaVersion(data))
    }
}
