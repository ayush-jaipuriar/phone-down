package phonedown.app.backup

private const val AUTO_BACKUP_INTERVAL_MILLIS = 24L * 60L * 60L * 1000L

fun isAutoBackupEligible(
    backupOptIn: Boolean,
    autoBackupEnabled: Boolean,
    isSignedIn: Boolean,
): Boolean = backupOptIn && autoBackupEnabled && isSignedIn

sealed class AutoBackupScheduleDecision {
    data object Cancel : AutoBackupScheduleDecision()

    data class Schedule(
        val initialDelayMillis: Long,
    ) : AutoBackupScheduleDecision()
}

fun autoBackupScheduleDecision(
    isEligible: Boolean,
    lastBackupEpochMillis: Long?,
    currentTimeMillis: Long,
): AutoBackupScheduleDecision {
    if (!isEligible) {
        return AutoBackupScheduleDecision.Cancel
    }

    val initialDelayMillis =
        if (lastBackupEpochMillis == null) {
            0L
        } else {
            (AUTO_BACKUP_INTERVAL_MILLIS - (currentTimeMillis - lastBackupEpochMillis)).coerceAtLeast(0L)
        }
    return AutoBackupScheduleDecision.Schedule(initialDelayMillis)
}
