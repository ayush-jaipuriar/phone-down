package phonedown.core.model

/**
 * Categorises the kind of interruption event that occurred during a focus session.
 *
 * Types are persisted as stable strings via explicit mapper functions.
 * Do NOT rely on `name` directly for storage — always use the dedicated mapper.
 */
enum class PenaltyEventType {
    /** Phone was picked up briefly; no penalty applied (below penalty threshold). */
    MinorPickup,

    /** Phone was picked up long enough to trigger a penalty deduction. */
    PenaltyPickup,

    /** Phone was picked up for an extended duration. */
    LongPickup,

    /** Session paused due to an incoming or ongoing phone call. */
    CallPause,

    /** App was force-closed by the user or system. */
    ForceClose,

    /** Device was restarted mid-session. */
    DeviceRestart,

    /** User tapped "End session" manually. */
    ManualEnd,

    /** User manually paused the focus session. */
    ManualPause,
}
