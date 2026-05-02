@file:Suppress("TooManyFunctions", "ReturnCount", "MaxLineLength", "MagicNumber")

package phonedown.app.runtime

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.SessionState
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository
import phonedown.core.notifications.FocusFeedbackEvent
import phonedown.core.sensors.FocusStabilityState
import phonedown.core.sensors.FocusValidityReason
import phonedown.core.sensors.FocusValidityResult
import phonedown.domain.session.EndSessionUseCase
import phonedown.domain.session.RecoverSessionsUseCase
import phonedown.domain.session.SessionEngine
import phonedown.domain.session.SessionInput
import phonedown.domain.session.SessionRuntime
import phonedown.domain.session.SessionTransition
import phonedown.domain.session.StartSessionUseCase
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ActiveSessionRuntimeCoordinator
    @Inject
    constructor(
        private val startSessionUseCase: StartSessionUseCase,
        private val endSessionUseCase: EndSessionUseCase,
        private val recoverSessionsUseCase: RecoverSessionsUseCase,
        private val sessionEngine: SessionEngine,
        private val sessionRepository: SessionRepository,
        private val settingsRepository: SettingsRepository,
    ) {
        private val _state = MutableStateFlow(ActiveSessionRuntimeState())
        val state: StateFlow<ActiveSessionRuntimeState> = _state.asStateFlow()

        private var currentRuntime: SessionRuntime? = null
        private var latestValidity: FocusValidityResult? = null
        private var lastPhysicalValid = false
        private var lastPersistedElapsedSeconds = 0L

        suspend fun ensureSessionStarted(plannedDurationSeconds: Long? = null): RuntimeStepResult {
            val existing = currentRuntime
            if (existing != null && existing.session.result == null) {
                return RuntimeStepResult(state = _state.value)
            }
            val settings = settingsRepository.settings.first()
            val runtime = startSessionUseCase(plannedDurationSeconds ?: settings.defaultDurationSeconds)
            currentRuntime = runtime
            lastPersistedElapsedSeconds = runtime.session.actualElapsedSeconds
            latestValidity = null
            lastPhysicalValid = false
            updateState(runtime.session, null)
            return RuntimeStepResult(state = _state.value)
        }

        suspend fun recoverFromAppLaunch() {
            if (hasActiveRuntime()) {
                return
            }
            recoverSessionsUseCase()
        }

        suspend fun recoverFromBoot() {
            recoverSessionsUseCase()
        }

        suspend fun recoverFromUnexpectedServiceRestart() {
            if (hasActiveRuntime()) {
                return
            }
            recoverSessionsUseCase()
        }

        suspend fun onSensorValidityChanged(result: FocusValidityResult): RuntimeStepResult {
            latestValidity = result
            val runtime =
                currentRuntime ?: run {
                    _state.value = _state.value.copy(latestValidity = result)
                    return RuntimeStepResult(state = _state.value)
                }
            val physicalValid =
                result.reason == FocusValidityReason.FaceDownStable ||
                    result.reason == FocusValidityReason.FaceDownStabilizing ||
                    result.stabilityState == FocusStabilityState.Stabilizing ||
                    result.stabilityState == FocusStabilityState.Stable
            if (physicalValid == lastPhysicalValid) {
                updateState(runtime.session, result)
                return RuntimeStepResult(state = _state.value)
            }
            lastPhysicalValid = physicalValid
            val input = if (physicalValid) SessionInput.PhoneBecameValid else SessionInput.PhoneBecameInvalid
            return applyTransition(runtime, sessionEngine.processInput(runtime, input))
        }

        suspend fun onTick(): RuntimeStepResult {
            val runtime = currentRuntime ?: return RuntimeStepResult(state = _state.value)
            return applyTransition(runtime, sessionEngine.processInput(runtime, SessionInput.Tick))
        }

        suspend fun endSession(): RuntimeStepResult {
            val runtime = currentRuntime ?: return RuntimeStepResult(state = _state.value)
            val transition = endSessionUseCase(runtime)
            return applyTransition(runtime, transition, forcePersist = true)
        }

        suspend fun onCallStateChanged(isInCall: Boolean): RuntimeStepResult {
            val runtime = currentRuntime ?: return RuntimeStepResult(state = _state.value)
            val input = if (isInCall) SessionInput.CallStarted else SessionInput.CallEnded
            return applyTransition(runtime, sessionEngine.processInput(runtime, input))
        }

        suspend fun flushCurrentRuntime() {
            val runtime = currentRuntime ?: return
            persistSession(runtime.session)
        }

        fun hasActiveRuntime(): Boolean {
            val runtime = currentRuntime ?: return false
            return runtime.session.result == null
        }

        fun clearFinishedRuntime() {
            if (_state.value.shouldStopService) {
                currentRuntime = null
                latestValidity = null
                lastPhysicalValid = false
                _state.value = ActiveSessionRuntimeState()
            }
        }

        private suspend fun applyTransition(
            previousRuntime: SessionRuntime,
            transition: SessionTransition,
            forcePersist: Boolean = false,
        ): RuntimeStepResult {
            currentRuntime = transition.runtime
            val previousSession = previousRuntime.session
            val nextSession = transition.session
            val feedbackEvents = deriveFeedbackEvents(previousSession, nextSession)
            val persistNow = forcePersist || shouldPersist(previousSession, nextSession, transition.penaltyEvents)
            if (persistNow) {
                persistTransition(transition)
                lastPersistedElapsedSeconds = nextSession.actualElapsedSeconds
            }
            updateState(nextSession, latestValidity)
            return RuntimeStepResult(
                state = _state.value,
                feedbackEvents = feedbackEvents,
            )
        }

        private fun shouldPersist(
            previous: FocusSession,
            next: FocusSession,
            events: List<PenaltyEvent>,
        ): Boolean =
            events.isNotEmpty() ||
                previous.state != next.state ||
                previous.result != next.result ||
                previous.broken != next.broken ||
                next.actualElapsedSeconds - lastPersistedElapsedSeconds >= PERSIST_EVERY_SECONDS

        private suspend fun persistTransition(transition: SessionTransition) {
            when (transition.penaltyEvents.size) {
                0 -> persistSession(transition.session)
                1 ->
                    sessionRepository.upsertSessionWithPenaltyEvent(
                        transition.session,
                        transition.penaltyEvents.first(),
                    )
                else -> {
                    sessionRepository.upsertSessionWithPenaltyEvent(
                        transition.session,
                        transition.penaltyEvents.first(),
                    )
                    for (event in transition.penaltyEvents.drop(1)) {
                        sessionRepository.recordPenaltyEvent(event)
                    }
                }
            }
        }

        private suspend fun persistSession(session: FocusSession) {
            sessionRepository.upsertSession(session)
        }

        private fun deriveFeedbackEvents(
            previous: FocusSession,
            next: FocusSession,
        ): List<FocusFeedbackEvent> {
            val events = mutableListOf<FocusFeedbackEvent>()
            if (previous.state == SessionState.WaitingForPhoneDown && next.state == SessionState.Arming) {
                events += FocusFeedbackEvent.PhoneDownDetected
            }
            if (previous.state == SessionState.Arming && next.state == SessionState.Active) {
                events += FocusFeedbackEvent.TimerStarted
            }
            if (previous.state == SessionState.Active && next.state == SessionState.PausedByPickup) {
                events += FocusFeedbackEvent.PhonePickedUp
            }
            if (!previous.broken && next.broken) {
                events += FocusFeedbackEvent.SessionBroken
            }
            if (previous.result == null && next.result != null && next.result != phonedown.core.model.SessionResult.Broken) {
                events += FocusFeedbackEvent.SessionCompleted
            }
            return events
        }

        private fun updateState(
            session: FocusSession,
            validity: FocusValidityResult?,
        ) {
            _state.value =
                ActiveSessionRuntimeState(
                    session = session,
                    latestValidity = validity,
                    shouldDimScreen = session.state == SessionState.Arming || session.state == SessionState.Active,
                    shouldStopService =
                        session.result != null ||
                            session.state == SessionState.Completed ||
                            session.state == SessionState.EndedEarly ||
                            session.state == SessionState.Invalidated ||
                            session.state == SessionState.Abandoned,
                    notificationTitle = "Phone Down",
                    notificationBody = notificationBody(session),
                )
        }

        private fun notificationBody(session: FocusSession): String =
            when (session.state) {
                SessionState.WaitingForPhoneDown -> "Waiting for phone down"
                SessionState.Arming -> "Hold still to begin"
                SessionState.Active -> "Focus active - ${remainingMinutes(session)} min left"
                SessionState.PausedByPickup,
                SessionState.PausedByCall,
                -> "Focus paused - return phone down"
                SessionState.Broken -> "Session broken - continue honestly"
                SessionState.Completed -> "Session completed"
                SessionState.EndedEarly -> "Session ended early"
                SessionState.Invalidated -> "Not enough focus time to count"
                SessionState.Abandoned -> "Session abandoned"
                SessionState.Created -> "Preparing session"
            }

        private fun remainingMinutes(session: FocusSession): Long {
            val remainingSeconds = (session.requiredDurationSeconds - session.validFocusSeconds).coerceAtLeast(0L)
            return if (remainingSeconds == 0L) 0L else (remainingSeconds + 59L) / 60L
        }

        companion object {
            private const val PERSIST_EVERY_SECONDS = 5L
        }
    }

data class RuntimeStepResult(
    val state: ActiveSessionRuntimeState,
    val feedbackEvents: List<FocusFeedbackEvent> = emptyList(),
)
