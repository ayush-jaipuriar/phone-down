package phonedown.core.model

/**
 * Summarises the outcome of a completed or terminated focus session.
 *
 * Results are persisted as stable strings via explicit mapper functions.
 * Do NOT rely on `name` directly for storage — always use the dedicated mapper.
 */
enum class SessionResult {
    /** Session completed with zero pickups and no interruptions. */
    CleanCompleted,

    /** Session completed but had at least one minor or penalty interruption. */
    CompletedWithInterruption,

    /**
     * Session ended early but achieved a meaningful portion of the planned duration.
     * The exact threshold is defined by the session engine.
     */
    Partial,

    /**
     * Session ended early but achieved a high proportion of the planned duration
     * (above a secondary threshold, below full completion).
     */
    StrongPartial,

    /** Session record is invalid (e.g. data inconsistency). */
    Invalidated,

    /** Session ended due to process death or device restart with unrecoverable state. */
    Broken,

    /** Session was never meaningfully started or was discarded. */
    Abandoned,
}
