package phonedown.app.backup

import org.junit.Assert.assertEquals
import org.junit.Test

class AutoBackupEligibilityTest {
    @Test
    fun `eligibility requires every prerequisite across full boolean matrix`() {
        val cases =
            listOf(
                Triple(false, false, false) to false,
                Triple(false, false, true) to false,
                Triple(false, true, false) to false,
                Triple(false, true, true) to false,
                Triple(true, false, false) to false,
                Triple(true, false, true) to false,
                Triple(true, true, false) to false,
                Triple(true, true, true) to true,
            )

        cases.forEach { (inputs, expected) ->
            val (backupOptIn, autoBackupEnabled, isSignedIn) = inputs
            assertEquals(
                "backupOptIn=$backupOptIn, autoBackupEnabled=$autoBackupEnabled, isSignedIn=$isSignedIn",
                expected,
                isAutoBackupEligible(
                    backupOptIn = backupOptIn,
                    autoBackupEnabled = autoBackupEnabled,
                    isSignedIn = isSignedIn,
                ),
            )
        }
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
