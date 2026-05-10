package phonedown.core.model

/**
 * Represents the lifecycle state of a focus session at any given point in time.
 *
 * States are persisted as stable strings via explicit mapper functions.
 * Do NOT rely on `name` directly for storage — always use the dedicated mapper.
 */
enum class SessionState {
    /** Session record created; timer has not started yet. */
    Created,

    /** Waiting for the user to place the phone face-down to begin arming. */
    WaitingForPhoneDown,

    /** Phone is face-down; stabilisation countdown is running. */
    Arming,

    /** Session is actively counting down (phone face-down, stable). */
    Active,

    /** Session is paused because the phone was picked up. */
    PausedByPickup,

    /** Session is paused because an incoming call was detected. */
    PausedByCall,

    /** Session is paused because the user explicitly tapped pause. */
    PausedByUser,

    /** Session reached its required duration and ended cleanly. */
    Completed,

    /** User manually ended the session before completion. */
    EndedEarly,

    /**
     * Session was marked invalid during recovery (e.g. data inconsistency).
     * Classification happens in the session engine, not in the persistence layer.
     */
    Invalidated,

    /**
     * Session is considered broken (e.g. process death with unclear state).
     * Classification happens in the session engine, not in the persistence layer.
     */
    Broken,

    /**
     * Session was abandoned (e.g. never progressed or disappeared without completion).
     * Classification happens in the session engine, not in the persistence layer.
     */
    Abandoned,
}
