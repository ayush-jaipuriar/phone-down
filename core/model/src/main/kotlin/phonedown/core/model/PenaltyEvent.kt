package phonedown.core.model

/**
 * Domain representation of a single interruption event within a focus session.
 *
 * Pure Kotlin data class; no Room or Android dependencies.
 * Exposed by [SessionRepository] alongside its parent [FocusSession].
 */
data class PenaltyEvent(
    /** UUID string uniquely identifying this event. */
    val id: String,

    /** ID of the [FocusSession] this event belongs to. */
    val sessionId: String,

    /** Category of interruption that occurred. */
    val type: PenaltyEventType,

    /** Wall-clock epoch millis when this event started. */
    val startedAtEpochMillis: Long,

    /** Wall-clock epoch millis when this event ended; null if still ongoing. */
    val endedAtEpochMillis: Long?,

    /** Duration of the pickup/pause in seconds. */
    val durationSeconds: Long,

    /** Penalty seconds deducted from the session's valid focus time. */
    val penaltySeconds: Long,
)
