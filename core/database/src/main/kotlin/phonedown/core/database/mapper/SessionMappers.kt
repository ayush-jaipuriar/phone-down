package phonedown.core.database.mapper

import phonedown.core.database.entity.FocusSessionEntity
import phonedown.core.database.entity.PenaltyEventEntity
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent

fun FocusSessionEntity.toDomainModel(): FocusSession {
    return FocusSession(
        id = id,
        plannedDurationSeconds = plannedDurationSeconds,
        requiredDurationSeconds = requiredDurationSeconds,
        validFocusSeconds = validFocusSeconds,
        actualElapsedSeconds = actualElapsedSeconds,
        penaltySeconds = penaltySeconds,
        interruptionCount = interruptionCount,
        minorInterruptionCount = minorInterruptionCount,
        penaltyInterruptionCount = penaltyInterruptionCount,
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = endedAtEpochMillis,
        startElapsedRealtime = startElapsedRealtime,
        endElapsedRealtime = endElapsedRealtime,
        state = state.toSessionState(),
        result = result?.toSessionResult(),
        clean = clean,
        broken = broken,
        callInterrupted = callInterrupted,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}

fun FocusSession.toEntity(): FocusSessionEntity {
    return FocusSessionEntity(
        id = id,
        plannedDurationSeconds = plannedDurationSeconds,
        requiredDurationSeconds = requiredDurationSeconds,
        validFocusSeconds = validFocusSeconds,
        actualElapsedSeconds = actualElapsedSeconds,
        penaltySeconds = penaltySeconds,
        interruptionCount = interruptionCount,
        minorInterruptionCount = minorInterruptionCount,
        penaltyInterruptionCount = penaltyInterruptionCount,
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = endedAtEpochMillis,
        startElapsedRealtime = startElapsedRealtime,
        endElapsedRealtime = endElapsedRealtime,
        state = state.toStorageString(),
        result = result?.toStorageString(),
        clean = clean,
        broken = broken,
        callInterrupted = callInterrupted,
        createdAtEpochMillis = createdAtEpochMillis,
        updatedAtEpochMillis = updatedAtEpochMillis,
    )
}

fun PenaltyEventEntity.toDomainModel(): PenaltyEvent {
    return PenaltyEvent(
        id = id,
        sessionId = sessionId,
        type = type.toPenaltyEventType(),
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = endedAtEpochMillis,
        durationSeconds = durationSeconds,
        penaltySeconds = penaltySeconds,
    )
}

fun PenaltyEvent.toEntity(): PenaltyEventEntity {
    return PenaltyEventEntity(
        id = id,
        sessionId = sessionId,
        type = type.toStorageString(),
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = endedAtEpochMillis,
        durationSeconds = durationSeconds,
        penaltySeconds = penaltySeconds,
    )
}
