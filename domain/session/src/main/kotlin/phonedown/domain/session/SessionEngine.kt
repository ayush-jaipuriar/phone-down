@file:Suppress(
    "LargeClass",
    "TooManyFunctions",
    "LongMethod",
    "LongParameterList",
    "ReturnCount",
    "MagicNumber",
)

package phonedown.domain.session

import phonedown.core.common.Clock
import phonedown.core.common.IdGenerator
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState

class SessionEngine(
    private val clock: Clock,
    private val idGenerator: IdGenerator,
    private val ruleConfig: SessionRuleConfig = SessionRuleConfig(),
) {
    fun startSession(plannedDurationSeconds: Long): SessionRuntime {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()
        val session =
            FocusSession(
                id = idGenerator.newId(),
                plannedDurationSeconds = plannedDurationSeconds,
                requiredDurationSeconds = plannedDurationSeconds,
                validFocusSeconds = 0L,
                actualElapsedSeconds = 0L,
                penaltySeconds = 0L,
                interruptionCount = 0,
                minorInterruptionCount = 0,
                penaltyInterruptionCount = 0,
                startedAtEpochMillis = nowWall,
                endedAtEpochMillis = null,
                startElapsedRealtime = nowElapsed,
                endElapsedRealtime = null,
                state = SessionState.WaitingForPhoneDown,
                result = null,
                clean = true,
                broken = false,
                callInterrupted = false,
                createdAtEpochMillis = nowWall,
                updatedAtEpochMillis = nowWall,
            )
        return SessionRuntime(session = session)
    }

    fun processInput(
        runtime: SessionRuntime,
        input: SessionInput,
    ): SessionTransition {
        if (runtime.session.result != null) {
            return SessionTransition(runtime)
        }

        return when (input) {
            SessionInput.PhoneBecameValid -> onPhoneBecameValid(runtime)
            SessionInput.PhoneBecameInvalid -> onPhoneBecameInvalid(runtime)
            SessionInput.Tick -> onTick(runtime)
            SessionInput.CallStarted -> onCallStarted(runtime)
            SessionInput.CallEnded -> onCallEnded(runtime)
            SessionInput.ManualEndRequested -> onManualEndRequested(runtime)
        }
    }

    private fun onPhoneBecameValid(runtime: SessionRuntime): SessionTransition {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()
        val session = runtime.session

        return when (session.state) {
            SessionState.WaitingForPhoneDown ->
                SessionTransition(
                    runtime =
                        runtime.copy(
                            session =
                                session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                    state = SessionState.Arming,
                                ),
                            phoneIsValid = true,
                            armingStartedAtElapsedMillis = nowElapsed,
                        ),
                )

            SessionState.Arming -> SessionTransition(runtime.copy(phoneIsValid = true))

            SessionState.PausedByPickup,
            SessionState.Broken,
            -> resolvePickupAndBeginArming(runtime, nowWall, nowElapsed)

            else -> SessionTransition(runtime)
        }
    }

    private fun onPhoneBecameInvalid(runtime: SessionRuntime): SessionTransition {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()

        return when (runtime.session.state) {
            SessionState.Arming ->
                SessionTransition(
                    runtime =
                        runtime.copy(
                            session =
                                runtime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                    state = SessionState.WaitingForPhoneDown,
                                ),
                            phoneIsValid = false,
                            armingStartedAtElapsedMillis = null,
                        ),
                )

            SessionState.Active -> {
                val activeRuntime = applyActiveProgress(runtime, nowWall, nowElapsed)
                SessionTransition(
                    runtime =
                        activeRuntime.copy(
                            session =
                                activeRuntime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                    state = SessionState.PausedByPickup,
                                ),
                            phoneIsValid = false,
                            activeStartedAtElapsedMillis = null,
                            activeBaseFocusSeconds = activeRuntime.session.validFocusSeconds,
                            interruptionStartedAtElapsedMillis = nowElapsed,
                            penaltyAppliedForCurrentInterruption = false,
                            longInterruptionRecorded = false,
                        ),
                )
            }

            else -> SessionTransition(runtime.copy(phoneIsValid = false))
        }
    }

    private fun onTick(runtime: SessionRuntime): SessionTransition {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()

        return when (runtime.session.state) {
            SessionState.Arming -> {
                val armingStartedAt = runtime.armingStartedAtElapsedMillis ?: nowElapsed
                if (runtime.phoneIsValid &&
                    nowElapsed - armingStartedAt >= ruleConfig.armingDurationMillis
                ) {
                    SessionTransition(
                        runtime =
                            runtime.copy(
                                session =
                                    runtime.session.withTimestamps(
                                        nowWall = nowWall,
                                        nowElapsed = nowElapsed,
                                        state = SessionState.Active,
                                    ),
                                activeStartedAtElapsedMillis = nowElapsed,
                                activeBaseFocusSeconds = runtime.session.validFocusSeconds,
                                armingStartedAtElapsedMillis = null,
                            ),
                    )
                } else {
                    SessionTransition(
                        runtime =
                            runtime.copy(
                                session =
                                    runtime.session.withTimestamps(
                                        nowWall = nowWall,
                                        nowElapsed = nowElapsed,
                                    ),
                            ),
                    )
                }
            }

            SessionState.Active -> {
                val activeRuntime = applyActiveProgress(runtime, nowWall, nowElapsed)
                if (activeRuntime.session.validFocusSeconds >= activeRuntime.session.requiredDurationSeconds) {
                    SessionTransition(runtime = completeSession(activeRuntime, nowWall, nowElapsed))
                } else {
                    SessionTransition(runtime = activeRuntime)
                }
            }

            SessionState.PausedByPickup,
            SessionState.Broken,
            -> processPickupInterruptionTick(runtime, nowWall, nowElapsed)

            SessionState.PausedByCall ->
                SessionTransition(
                    runtime =
                        runtime.copy(
                            session =
                                runtime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                ),
                        ),
                )

            else ->
                SessionTransition(
                    runtime =
                        runtime.copy(
                            session =
                                runtime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                ),
                        ),
                )
        }
    }

    private fun onCallStarted(runtime: SessionRuntime): SessionTransition {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()

        return when (runtime.session.state) {
            SessionState.Active -> {
                val activeRuntime = applyActiveProgress(runtime, nowWall, nowElapsed)
                SessionTransition(
                    runtime =
                        activeRuntime.copy(
                            session =
                                activeRuntime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                    state = SessionState.PausedByCall,
                                    clean = false,
                                    callInterrupted = true,
                                ),
                            phoneIsValid = false,
                            activeStartedAtElapsedMillis = null,
                            activeBaseFocusSeconds = activeRuntime.session.validFocusSeconds,
                            callStartedAtElapsedMillis = nowElapsed,
                            armingStartedAtElapsedMillis = null,
                        ),
                )
            }

            SessionState.WaitingForPhoneDown,
            SessionState.Arming,
            ->
                SessionTransition(
                    runtime =
                        runtime.copy(
                            session =
                                runtime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                    state = SessionState.PausedByCall,
                                    clean = false,
                                    callInterrupted = true,
                                ),
                            phoneIsValid = false,
                            armingStartedAtElapsedMillis = null,
                            callStartedAtElapsedMillis = nowElapsed,
                        ),
                )

            else -> SessionTransition(runtime)
        }
    }

    private fun onCallEnded(runtime: SessionRuntime): SessionTransition {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()

        if (runtime.session.state != SessionState.PausedByCall) {
            return SessionTransition(runtime)
        }

        val callStartedAt = runtime.callStartedAtElapsedMillis ?: nowElapsed
        val event =
            buildPenaltyEvent(
                sessionId = runtime.session.id,
                type = PenaltyEventType.CallPause,
                startedAtElapsedMillis = callStartedAt,
                endedAtElapsedMillis = nowElapsed,
            )

        return SessionTransition(
            runtime =
                runtime.copy(
                    session =
                        runtime.session.withTimestamps(
                            nowWall = nowWall,
                            nowElapsed = nowElapsed,
                            state = SessionState.WaitingForPhoneDown,
                        ),
                    phoneIsValid = false,
                    callStartedAtElapsedMillis = null,
                ),
            penaltyEvents = listOf(event),
        )
    }

    private fun onManualEndRequested(runtime: SessionRuntime): SessionTransition {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()
        val events = mutableListOf<PenaltyEvent>()
        var workingRuntime = runtime

        if (workingRuntime.session.state == SessionState.Active) {
            workingRuntime = applyActiveProgress(workingRuntime, nowWall, nowElapsed)
        }

        if (workingRuntime.session.state == SessionState.PausedByPickup &&
            !workingRuntime.penaltyAppliedForCurrentInterruption
        ) {
            val interruptionStartedAt = workingRuntime.interruptionStartedAtElapsedMillis ?: nowElapsed
            val minorEvent =
                buildPenaltyEvent(
                    sessionId = workingRuntime.session.id,
                    type = PenaltyEventType.MinorPickup,
                    startedAtElapsedMillis = interruptionStartedAt,
                    endedAtElapsedMillis = nowElapsed,
                )
            events += minorEvent
            workingRuntime =
                workingRuntime.copy(
                    session =
                        workingRuntime.session.withTimestamps(
                            nowWall = nowWall,
                            nowElapsed = nowElapsed,
                            clean = false,
                            interruptionCount = workingRuntime.session.interruptionCount + 1,
                            minorInterruptionCount = workingRuntime.session.minorInterruptionCount + 1,
                        ),
                )
        }

        if (workingRuntime.session.state == SessionState.PausedByCall) {
            val callStartedAt = workingRuntime.callStartedAtElapsedMillis ?: nowElapsed
            events +=
                buildPenaltyEvent(
                    sessionId = workingRuntime.session.id,
                    type = PenaltyEventType.CallPause,
                    startedAtElapsedMillis = callStartedAt,
                    endedAtElapsedMillis = nowElapsed,
                )
        }

        events +=
            buildPenaltyEvent(
                sessionId = workingRuntime.session.id,
                type = PenaltyEventType.ManualEnd,
                startedAtElapsedMillis = nowElapsed,
                endedAtElapsedMillis = nowElapsed,
            )

        val finalizedSession =
            classifyManualEnd(
                session =
                    workingRuntime.session.withTimestamps(
                        nowWall = nowWall,
                        nowElapsed = nowElapsed,
                        clean = false,
                    ),
                nowWall = nowWall,
                nowElapsed = nowElapsed,
            )

        return SessionTransition(
            runtime =
                workingRuntime.copy(
                    session = finalizedSession,
                    phoneIsValid = false,
                    armingStartedAtElapsedMillis = null,
                    activeStartedAtElapsedMillis = null,
                    interruptionStartedAtElapsedMillis = null,
                    callStartedAtElapsedMillis = null,
                ),
            penaltyEvents = events,
        )
    }

    private fun resolvePickupAndBeginArming(
        runtime: SessionRuntime,
        nowWall: Long,
        nowElapsed: Long,
    ): SessionTransition {
        val interruptionStartedAt = runtime.interruptionStartedAtElapsedMillis ?: nowElapsed
        val events = mutableListOf<PenaltyEvent>()
        var session = runtime.session

        if (!runtime.penaltyAppliedForCurrentInterruption) {
            events +=
                buildPenaltyEvent(
                    sessionId = session.id,
                    type = PenaltyEventType.MinorPickup,
                    startedAtElapsedMillis = interruptionStartedAt,
                    endedAtElapsedMillis = nowElapsed,
                )
            session =
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    clean = false,
                    interruptionCount = session.interruptionCount + 1,
                    minorInterruptionCount = session.minorInterruptionCount + 1,
                )
        } else {
            session =
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                )
        }

        return SessionTransition(
            runtime =
                runtime.copy(
                    session =
                        session.copy(
                            state = SessionState.Arming,
                            updatedAtEpochMillis = nowWall,
                        ),
                    phoneIsValid = true,
                    armingStartedAtElapsedMillis = nowElapsed,
                    activeStartedAtElapsedMillis = null,
                    activeBaseFocusSeconds = session.validFocusSeconds,
                    interruptionStartedAtElapsedMillis = null,
                    penaltyAppliedForCurrentInterruption = false,
                    longInterruptionRecorded = false,
                ),
            penaltyEvents = events,
        )
    }

    private fun processPickupInterruptionTick(
        runtime: SessionRuntime,
        nowWall: Long,
        nowElapsed: Long,
    ): SessionTransition {
        val interruptionStartedAt =
            runtime.interruptionStartedAtElapsedMillis
                ?: return SessionTransition(
                    runtime =
                        runtime.copy(
                            session =
                                runtime.session.withTimestamps(
                                    nowWall = nowWall,
                                    nowElapsed = nowElapsed,
                                ),
                        ),
                )
        val interruptionDuration = nowElapsed - interruptionStartedAt
        val events = mutableListOf<PenaltyEvent>()
        var session = runtime.session.withTimestamps(nowWall = nowWall, nowElapsed = nowElapsed)
        var penaltyApplied = runtime.penaltyAppliedForCurrentInterruption
        var longEventRecorded = runtime.longInterruptionRecorded

        if (!penaltyApplied &&
            interruptionDuration > ruleConfig.interruptionGracePeriodMillis
        ) {
            val penaltyEvent =
                buildPenaltyEvent(
                    sessionId = session.id,
                    type = PenaltyEventType.PenaltyPickup,
                    startedAtElapsedMillis = interruptionStartedAt,
                    endedAtElapsedMillis = nowElapsed,
                    penaltySeconds = ruleConfig.penaltySecondsIncrement,
                )
            events += penaltyEvent
            penaltyApplied = true
            session =
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    clean = false,
                    penaltySeconds = session.penaltySeconds + ruleConfig.penaltySecondsIncrement,
                    requiredDurationSeconds = session.requiredDurationSeconds + ruleConfig.penaltySecondsIncrement,
                    interruptionCount = session.interruptionCount + 1,
                    penaltyInterruptionCount = session.penaltyInterruptionCount + 1,
                )

            if (session.penaltyInterruptionCount >= ruleConfig.brokenPenaltyInterruptions) {
                session =
                    session.copy(
                        state = SessionState.Broken,
                        broken = true,
                        clean = false,
                        updatedAtEpochMillis = nowWall,
                    )
            }
        }

        if (!longEventRecorded &&
            interruptionDuration > ruleConfig.brokenInterruptionDurationMillis
        ) {
            events +=
                buildPenaltyEvent(
                    sessionId = session.id,
                    type = PenaltyEventType.LongPickup,
                    startedAtElapsedMillis = interruptionStartedAt,
                    endedAtElapsedMillis = nowElapsed,
                )
            longEventRecorded = true
            session =
                session.copy(
                    state = SessionState.Broken,
                    broken = true,
                    clean = false,
                    updatedAtEpochMillis = nowWall,
                )
        }

        return SessionTransition(
            runtime =
                runtime.copy(
                    session = session,
                    penaltyAppliedForCurrentInterruption = penaltyApplied,
                    longInterruptionRecorded = longEventRecorded,
                ),
            penaltyEvents = events,
        )
    }

    private fun applyActiveProgress(
        runtime: SessionRuntime,
        nowWall: Long,
        nowElapsed: Long,
    ): SessionRuntime {
        if (runtime.session.state != SessionState.Active) {
            return runtime.copy(
                session = runtime.session.withTimestamps(nowWall = nowWall, nowElapsed = nowElapsed),
            )
        }

        val activeStartedAt = runtime.activeStartedAtElapsedMillis ?: nowElapsed
        val additionalFocusSeconds = ((nowElapsed - activeStartedAt).coerceAtLeast(0L) / 1_000L)
        val updatedFocusSeconds = runtime.activeBaseFocusSeconds + additionalFocusSeconds

        return runtime.copy(
            session =
                runtime.session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    validFocusSeconds = updatedFocusSeconds,
                ),
        )
    }

    private fun completeSession(
        runtime: SessionRuntime,
        nowWall: Long,
        nowElapsed: Long,
    ): SessionRuntime {
        val session = runtime.session
        if (session.broken) {
            return runtime.copy(
                session =
                    session.withTimestamps(
                        nowWall = nowWall,
                        nowElapsed = nowElapsed,
                        state = SessionState.Broken,
                        result = SessionResult.Broken,
                        clean = false,
                        endedAtEpochMillis = nowWall,
                        endElapsedRealtime = nowElapsed,
                    ),
                activeStartedAtElapsedMillis = null,
                armingStartedAtElapsedMillis = null,
            )
        }

        val result =
            if (session.clean) {
                SessionResult.CleanCompleted
            } else {
                SessionResult.CompletedWithInterruption
            }

        return runtime.copy(
            session =
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    state = SessionState.Completed,
                    result = result,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                ),
            activeStartedAtElapsedMillis = null,
            armingStartedAtElapsedMillis = null,
        )
    }

    private fun classifyManualEnd(
        session: FocusSession,
        nowWall: Long,
        nowElapsed: Long,
    ): FocusSession {
        if (session.broken) {
            return session.withTimestamps(
                nowWall = nowWall,
                nowElapsed = nowElapsed,
                state = SessionState.Broken,
                result = SessionResult.Broken,
                endedAtEpochMillis = nowWall,
                endElapsedRealtime = nowElapsed,
                clean = false,
            )
        }

        if (session.validFocusSeconds >= session.requiredDurationSeconds) {
            return if (session.clean) {
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    state = SessionState.Completed,
                    result = SessionResult.CleanCompleted,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                )
            } else {
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    state = SessionState.Completed,
                    result = SessionResult.CompletedWithInterruption,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                )
            }
        }

        val completedPercent =
            if (session.plannedDurationSeconds == 0L) {
                0.0
            } else {
                session.validFocusSeconds.toDouble() * 100.0 / session.plannedDurationSeconds.toDouble()
            }

        return when {
            completedPercent <= 20.0 ->
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    state = SessionState.Invalidated,
                    result = SessionResult.Invalidated,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                    clean = false,
                )

            completedPercent < 80.0 ->
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    state = SessionState.EndedEarly,
                    result = SessionResult.Partial,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                    clean = false,
                )

            else ->
                session.withTimestamps(
                    nowWall = nowWall,
                    nowElapsed = nowElapsed,
                    state = SessionState.EndedEarly,
                    result = SessionResult.StrongPartial,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                    clean = false,
                )
        }
    }

    private fun buildPenaltyEvent(
        sessionId: String,
        type: PenaltyEventType,
        startedAtElapsedMillis: Long,
        endedAtElapsedMillis: Long,
        penaltySeconds: Long = 0L,
    ): PenaltyEvent {
        val durationMillis = (endedAtElapsedMillis - startedAtElapsedMillis).coerceAtLeast(0L)
        val endedAtEpochMillis = clock.currentTimeMillis()
        return PenaltyEvent(
            id = idGenerator.newId(),
            sessionId = sessionId,
            type = type,
            startedAtEpochMillis = endedAtEpochMillis - durationMillis,
            endedAtEpochMillis = endedAtEpochMillis,
            durationSeconds = durationMillis / 1_000L,
            penaltySeconds = penaltySeconds,
        )
    }

    private fun FocusSession.withTimestamps(
        nowWall: Long,
        nowElapsed: Long,
        state: SessionState = this.state,
        result: SessionResult? = this.result,
        validFocusSeconds: Long = this.validFocusSeconds,
        penaltySeconds: Long = this.penaltySeconds,
        requiredDurationSeconds: Long = this.requiredDurationSeconds,
        interruptionCount: Int = this.interruptionCount,
        minorInterruptionCount: Int = this.minorInterruptionCount,
        penaltyInterruptionCount: Int = this.penaltyInterruptionCount,
        clean: Boolean = this.clean,
        broken: Boolean = this.broken,
        callInterrupted: Boolean = this.callInterrupted,
        endedAtEpochMillis: Long? = this.endedAtEpochMillis,
        endElapsedRealtime: Long? = this.endElapsedRealtime,
    ): FocusSession =
        copy(
            validFocusSeconds = validFocusSeconds,
            actualElapsedSeconds = ((nowElapsed - startElapsedRealtime).coerceAtLeast(0L) / 1_000L),
            penaltySeconds = penaltySeconds,
            requiredDurationSeconds = requiredDurationSeconds,
            interruptionCount = interruptionCount,
            minorInterruptionCount = minorInterruptionCount,
            penaltyInterruptionCount = penaltyInterruptionCount,
            state = state,
            result = result,
            clean = clean,
            broken = broken,
            callInterrupted = callInterrupted,
            endedAtEpochMillis = endedAtEpochMillis,
            endElapsedRealtime = endElapsedRealtime,
            updatedAtEpochMillis = nowWall,
        )
}
