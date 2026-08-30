package phonedown.app.backup

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AutoBackupEligibilityTest {
    @Test
    fun `eligible when backup is opted in enabled and account is signed in`() {
        assertTrue(
            isAutoBackupEligible(
                backupOptIn = true,
                autoBackupEnabled = true,
                isSignedIn = true,
            ),
        )
    }

    @Test
    fun `ineligible when backup is not opted in`() {
        assertFalse(
            isAutoBackupEligible(
                backupOptIn = false,
                autoBackupEnabled = true,
                isSignedIn = true,
            ),
        )
    }

    @Test
    fun `ineligible when automatic backup is disabled`() {
        assertFalse(
            isAutoBackupEligible(
                backupOptIn = true,
                autoBackupEnabled = false,
                isSignedIn = true,
            ),
        )
    }

    @Test
    fun `ineligible when account is signed out`() {
        assertFalse(
            isAutoBackupEligible(
                backupOptIn = true,
                autoBackupEnabled = true,
                isSignedIn = false,
            ),
        )
    }
}

class AutoBackupScheduleDecisionTest {
    @Test
    fun `eligible state with no previous backup schedules immediately`() {
        assertEquals(
            AutoBackupScheduleDecision.Schedule(initialDelayMillis = 0L),
            autoBackupScheduleDecision(
                isEligible =
                    isAutoBackupEligible(
                        backupOptIn = true,
                        autoBackupEnabled = true,
                        isSignedIn = true,
                    ),
                lastBackupEpochMillis = null,
                currentTimeMillis = 1_000_000L,
            ),
        )
    }

    @Test
    fun `recent backup schedules after remaining daily interval`() {
        assertEquals(
            AutoBackupScheduleDecision.Schedule(initialDelayMillis = 79_200_000L),
            autoBackupScheduleDecision(
                isEligible =
                    isAutoBackupEligible(
                        backupOptIn = true,
                        autoBackupEnabled = true,
                        isSignedIn = true,
                    ),
                lastBackupEpochMillis = 1_000_000L,
                currentTimeMillis = 8_200_000L,
            ),
        )
    }

    @Test
    fun `overdue backup schedules immediately`() {
        assertEquals(
            AutoBackupScheduleDecision.Schedule(initialDelayMillis = 0L),
            autoBackupScheduleDecision(
                isEligible =
                    isAutoBackupEligible(
                        backupOptIn = true,
                        autoBackupEnabled = true,
                        isSignedIn = true,
                    ),
                lastBackupEpochMillis = 1_000_000L,
                currentTimeMillis = 87_400_001L,
            ),
        )
    }

    @Test
    fun `ineligible state cancels periodic work`() {
        assertEquals(
            AutoBackupScheduleDecision.Cancel,
            autoBackupScheduleDecision(
                isEligible =
                    isAutoBackupEligible(
                        backupOptIn = true,
                        autoBackupEnabled = false,
                        isSignedIn = true,
                    ),
                lastBackupEpochMillis = null,
                currentTimeMillis = 1_000_000L,
            ),
        )
    }
}
