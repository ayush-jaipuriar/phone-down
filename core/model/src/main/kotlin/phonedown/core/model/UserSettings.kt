package phonedown.core.model

/**
 * All user-configurable settings for the Phone Down app.
 *
 * Pure Kotlin data class; persisted via DataStore in [SettingsRepository].
 * Free-vs-Pro entitlement enforcement is NOT done here; that belongs to the billing domain.
 */
data class UserSettings(
    /** Default planned focus duration in seconds (default: 25 minutes). */
    val defaultDurationSeconds: Long = DEFAULT_DURATION_SECONDS,

    /** Whether session-end sounds are enabled. */
    val soundEnabled: Boolean = true,

    /** Whether haptic feedback is enabled during sessions. */
    val hapticsEnabled: Boolean = true,

    /** App colour theme preference. */
    val themeMode: ThemeMode = ThemeMode.System,

    /** True once the user has completed the onboarding flow. */
    val onboardingCompleted: Boolean = false,

    /**
     * True if the user has explicitly opted into Google Drive backup.
     * Actual backup transport is implemented in a later phase.
     */
    val backupOptIn: Boolean = false,

    /**
     * True if automatic periodic backup is enabled.
     * Only relevant when [backupOptIn] is also true.
     */
    val autoBackupEnabled: Boolean = false,

    /**
     * Epoch millis of the last successful backup.
     * Null if no backup has ever been made.
     */
    val lastBackupEpochMillis: Long? = null,

    /**
     * Custom free-tier duration limit in seconds.
     * Null means the app has not yet set a limit (pending product decision).
     * Enforcement is handled in the billing/domain layer.
     */
    val freeCustomDurationSeconds: Long? = null,
) {
    companion object {
        /** 25 minutes in seconds. */
        const val DEFAULT_DURATION_SECONDS: Long = 25L * 60L
    }
}
