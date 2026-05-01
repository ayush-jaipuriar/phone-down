package phonedown.core.database.mapper

import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState

// Fallback values for unknown strings from the database (e.g. from future versions)
private val FALLBACK_STATE = SessionState.Broken
private val FALLBACK_RESULT = SessionResult.Broken
private val FALLBACK_PENALTY = PenaltyEventType.MinorPickup

fun SessionState.toStorageString(): String = when (this) {
    SessionState.Created -> "created"
    SessionState.WaitingForPhoneDown -> "waiting_for_phone_down"
    SessionState.Arming -> "arming"
    SessionState.Active -> "active"
    SessionState.PausedByPickup -> "paused_by_pickup"
    SessionState.PausedByCall -> "paused_by_call"
    SessionState.Completed -> "completed"
    SessionState.EndedEarly -> "ended_early"
    SessionState.Invalidated -> "invalidated"
    SessionState.Broken -> "broken"
    SessionState.Abandoned -> "abandoned"
}

fun String.toSessionState(): SessionState = when (this) {
    "created" -> SessionState.Created
    "waiting_for_phone_down" -> SessionState.WaitingForPhoneDown
    "arming" -> SessionState.Arming
    "active" -> SessionState.Active
    "paused_by_pickup" -> SessionState.PausedByPickup
    "paused_by_call" -> SessionState.PausedByCall
    "completed" -> SessionState.Completed
    "ended_early" -> SessionState.EndedEarly
    "invalidated" -> SessionState.Invalidated
    "broken" -> SessionState.Broken
    "abandoned" -> SessionState.Abandoned
    else -> FALLBACK_STATE
}

fun SessionResult.toStorageString(): String = when (this) {
    SessionResult.CleanCompleted -> "clean_completed"
    SessionResult.CompletedWithInterruption -> "completed_with_interruption"
    SessionResult.Partial -> "partial"
    SessionResult.StrongPartial -> "strong_partial"
    SessionResult.Invalidated -> "invalidated"
    SessionResult.Broken -> "broken"
    SessionResult.Abandoned -> "abandoned"
}

fun String.toSessionResult(): SessionResult = when (this) {
    "clean_completed" -> SessionResult.CleanCompleted
    "completed_with_interruption" -> SessionResult.CompletedWithInterruption
    "partial" -> SessionResult.Partial
    "strong_partial" -> SessionResult.StrongPartial
    "invalidated" -> SessionResult.Invalidated
    "broken" -> SessionResult.Broken
    "abandoned" -> SessionResult.Abandoned
    else -> FALLBACK_RESULT
}

fun PenaltyEventType.toStorageString(): String = when (this) {
    PenaltyEventType.MinorPickup -> "minor_pickup"
    PenaltyEventType.PenaltyPickup -> "penalty_pickup"
    PenaltyEventType.LongPickup -> "long_pickup"
    PenaltyEventType.CallPause -> "call_pause"
    PenaltyEventType.ForceClose -> "force_close"
    PenaltyEventType.DeviceRestart -> "device_restart"
    PenaltyEventType.ManualEnd -> "manual_end"
}

fun String.toPenaltyEventType(): PenaltyEventType = when (this) {
    "minor_pickup" -> PenaltyEventType.MinorPickup
    "penalty_pickup" -> PenaltyEventType.PenaltyPickup
    "long_pickup" -> PenaltyEventType.LongPickup
    "call_pause" -> PenaltyEventType.CallPause
    "force_close" -> PenaltyEventType.ForceClose
    "device_restart" -> PenaltyEventType.DeviceRestart
    "manual_end" -> PenaltyEventType.ManualEnd
    else -> FALLBACK_PENALTY
}
