package phonedown.core.model

/**
 * Domain representation of a focus session.
 *
 * This is a pure Kotlin data class with no Room, DataStore, or Android dependencies.
 * It is the contract exposed by [SessionRepository] and consumed by feature modules.
 *
 * Timing fields:
 * - [startedAtEpochMillis] / [endedAtEpochMillis]: wall-clock time for display and analytics.
 * - [startElapsedRealtime] / [endElapsedRealtime]: monotonic time for accurate duration
 *   measurement across process-death recovery (see architecture.md).
 */
data class FocusSession(
    /** UUID string uniquely identifying this session. */
    val id: String,

    /** The full duration the user originally chose, in seconds. */
    val plannedDurationSeconds: Long,

    /**
     * The minimum valid-focus seconds needed for a non-Abandoned result.
     * Set by session engine based on phase rules.
     */
    val requiredDurationSeconds: Long,

    /** Cumulative seconds of clean focus time earned so far. */
    val validFocusSeconds: Long,

    /** Total elapsed seconds since session start (including pauses). */
    val actualElapsedSeconds: Long,

    /** Cumulative penalty seconds deducted from valid focus time. */
    val penaltySeconds: Long,

    /** Total number of interruption events (minor + penalty). */
    val interruptionCount: Int,

    /** Count of interruptions below the penalty threshold. */
    val minorInterruptionCount: Int,

    /** Count of interruptions that triggered a penalty deduction. */
    val penaltyInterruptionCount: Int,

    /** Wall-clock epoch millis when the session was first started. */
    val startedAtEpochMillis: Long,

    /** Wall-clock epoch millis when the session ended; null if still active. */
    val endedAtEpochMillis: Long?,

    /** SystemClock.elapsedRealtime() captured at session start. */
    val startElapsedRealtime: Long,

    /** SystemClock.elapsedRealtime() captured at session end; null if still active. */
    val endElapsedRealtime: Long?,

    /** Current lifecycle state of this session. */
    val state: SessionState,

    /** Final outcome; null until the session reaches a terminal state. */
    val result: SessionResult?,

    /** True if the session completed with zero penalty events. */
    val clean: Boolean,

    /** True if the session ended in a Broken state. */
    val broken: Boolean,

    /** True if the session was paused by at least one call event. */
    val callInterrupted: Boolean,

    /** Wall-clock epoch millis when this record was first persisted. */
    val createdAtEpochMillis: Long,

    /** Wall-clock epoch millis when this record was last modified. */
    val updatedAtEpochMillis: Long,
)
