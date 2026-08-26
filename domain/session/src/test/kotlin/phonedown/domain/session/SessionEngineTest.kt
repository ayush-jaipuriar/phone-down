package phonedown.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.common.Clock
import phonedown.core.common.IdGenerator
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState

class SessionEngineTest {
    private val clock = FakeClock()
    private val idGenerator = FakeIdGenerator()
    private val engine = SessionEngine(clock = clock, idGenerator = idGenerator)

    @Test
    fun startSessionEntersWaitingState() {
        val runtime = engine.startSession(plannedDurationSeconds = 1_500L)

        assertEquals(SessionState.WaitingForPhoneDown, runtime.session.state)
        assertEquals(1_500L, runtime.session.plannedDurationSeconds)
        assertEquals(1_500L, runtime.session.requiredDurationSeconds)
        assertTrue(runtime.session.clean)
    }

    @Test
    fun validForThreeSecondsEntersActive() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)

        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        assertEquals(SessionState.Arming, runtime.session.state)

        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        assertEquals(SessionState.Active, runtime.session.state)
    }

    @Test
    fun invalidDuringArmingResetsToWaiting() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)

        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameInvalid).runtime

        assertEquals(SessionState.WaitingForPhoneDown, runtime.session.state)
        assertFalse(runtime.phoneIsValid)
    }

    @Test
    fun minorPickupWithinGraceRecordsMinorInterruptionAndResumesArming() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        runtime = engine.processInput(runtime, SessionInput.PhoneBecameInvalid).runtime
        clock.advanceBy(4_000L)
        val transition = engine.processInput(runtime, SessionInput.PhoneBecameValid)

        assertEquals(SessionState.Arming, transition.session.state)
        assertFalse(transition.session.clean)
        assertEquals(1, transition.session.interruptionCount)
        assertEquals(1, transition.session.minorInterruptionCount)
        assertEquals(1, transition.penaltyEvents.size)
        assertEquals(PenaltyEventType.MinorPickup, transition.penaltyEvents.first().type)
    }

    @Test
    fun invalidPastGraceAddsPenaltyAndLongInvalidMarksBroken() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameInvalid).runtime

        clock.advanceBy(6_000L)
        val penaltyTransition = engine.processInput(runtime, SessionInput.Tick)
        runtime = penaltyTransition.runtime

        assertEquals(60L, runtime.session.penaltySeconds)
        assertEquals(660L, runtime.session.requiredDurationSeconds)
        assertEquals(1, runtime.session.penaltyInterruptionCount)
        assertEquals(PenaltyEventType.PenaltyPickup, penaltyTransition.penaltyEvents.single().type)

        clock.advanceBy(55_000L)
        val brokenTransition = engine.processInput(runtime, SessionInput.Tick)

        assertTrue(brokenTransition.session.broken)
        assertEquals(SessionState.Broken, brokenTransition.session.state)
        assertEquals(PenaltyEventType.LongPickup, brokenTransition.penaltyEvents.single().type)
    }

    @Test
    fun thirdPenaltyMarksSessionBroken() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        repeat(3) {
            runtime = engine.processInput(runtime, SessionInput.PhoneBecameInvalid).runtime
            clock.advanceBy(6_000L)
            runtime = engine.processInput(runtime, SessionInput.Tick).runtime
            runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
            clock.advanceBy(3_000L)
            runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        }

        assertTrue(runtime.session.broken)
        assertEquals(SessionState.Active, runtime.session.state)
        assertEquals(3, runtime.session.penaltyInterruptionCount)
    }

    @Test
    fun manualEndUsesEarlyEndThresholds() {
        assertEquals(
            SessionResult.Invalidated,
            manualEndResultAfterValidFocusSeconds(validFocusSeconds = 200L),
        )
        assertEquals(
            SessionResult.Partial,
            manualEndResultAfterValidFocusSeconds(validFocusSeconds = 790L),
        )
        assertEquals(
            SessionResult.StrongPartial,
            manualEndResultAfterValidFocusSeconds(validFocusSeconds = 990L),
        )
    }

    @Test
    fun callPauseRemovesCleanStatusAndRecordsEventOnEnd() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        runtime = engine.processInput(runtime, SessionInput.CallStarted).runtime
        assertEquals(SessionState.PausedByCall, runtime.session.state)
        assertFalse(runtime.session.clean)

        clock.advanceBy(10_000L)
        val transition = engine.processInput(runtime, SessionInput.CallEnded)

        assertEquals(SessionState.WaitingForPhoneDown, transition.session.state)
        assertEquals(PenaltyEventType.CallPause, transition.penaltyEvents.single().type)
    }

    @Test
    fun manualPauseStopsProgressAndResumeRequiresPhoneDownAgain() {
        var runtime = engine.startSession(plannedDurationSeconds = 600L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        clock.advanceBy(30_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        runtime = engine.processInput(runtime, SessionInput.ManualPauseRequested).runtime
        val pausedFocusSeconds = runtime.session.validFocusSeconds

        assertEquals(SessionState.PausedByUser, runtime.session.state)
        assertFalse(runtime.session.clean)

        clock.advanceBy(30_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        assertEquals(pausedFocusSeconds, runtime.session.validFocusSeconds)

        val resumeTransition = engine.processInput(runtime, SessionInput.ManualResumeRequested)
        runtime = resumeTransition.runtime

        assertEquals(SessionState.WaitingForPhoneDown, runtime.session.state)
        assertEquals(PenaltyEventType.ManualPause, resumeTransition.penaltyEvents.single().type)

        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        assertEquals(SessionState.Arming, runtime.session.state)
    }

    @Test
    fun addTimeExtendsRequiredDurationAndCompletionThreshold() {
        var runtime = engine.startSession(plannedDurationSeconds = 60L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime

        runtime = engine.processInput(runtime, SessionInput.AddTimeRequested(additionalSeconds = 60L)).runtime

        assertEquals(60L, runtime.session.plannedDurationSeconds)
        assertEquals(120L, runtime.session.requiredDurationSeconds)

        clock.advanceBy(60_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.Active, runtime.session.state)

        clock.advanceBy(60_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.Completed, runtime.session.state)
    }

    private fun manualEndResultAfterValidFocusSeconds(validFocusSeconds: Long): SessionResult {
        var runtime = engine.startSession(plannedDurationSeconds = 1_000L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        clock.advanceBy(validFocusSeconds * 1_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        return engine.processInput(runtime, SessionInput.ManualEndRequested).session.result!!
    }

    @Test
    fun callStartedWhilePausedByPickupTransitionsToPausedByCall() {
        var runtime = engine.startSession(plannedDurationSeconds = 300L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.Active, runtime.session.state)

        // User picks up phone to answer
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameInvalid).runtime
        assertEquals(SessionState.PausedByPickup, runtime.session.state)

        // Incoming call is detected
        runtime = engine.processInput(runtime, SessionInput.CallStarted).runtime
        assertEquals(SessionState.PausedByCall, runtime.session.state)
        assertTrue(runtime.session.callInterrupted)

        // Talking on the phone for 2 minutes should not break the session or add pickup penalties
        clock.advanceBy(120_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.PausedByCall, runtime.session.state)
        assertEquals(0, runtime.session.penaltyInterruptionCount)

        // Call ends, user puts phone face down
        runtime = engine.processInput(runtime, SessionInput.CallEnded).runtime
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.Active, runtime.session.state)
    }

    @Test
    fun manualPauseWhilePausedByPickupTransitionsToPausedByUser() {
        var runtime = engine.startSession(plannedDurationSeconds = 300L)
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.Active, runtime.session.state)

        // User picks up phone to tap Pause button on screen
        runtime = engine.processInput(runtime, SessionInput.PhoneBecameInvalid).runtime
        assertEquals(SessionState.PausedByPickup, runtime.session.state)

        // User taps Pause
        runtime = engine.processInput(runtime, SessionInput.ManualPauseRequested).runtime
        assertEquals(SessionState.PausedByUser, runtime.session.state)
        assertFalse(runtime.session.clean)

        // Leaving the phone paused for 5 minutes does not accumulate pickup penalties
        clock.advanceBy(300_000L)
        runtime = engine.processInput(runtime, SessionInput.Tick).runtime
        assertEquals(SessionState.PausedByUser, runtime.session.state)
        assertEquals(0, runtime.session.penaltyInterruptionCount)
    }
}

private class FakeClock : Clock {
    private var wallTimeMillis = 1_000_000L
    private var elapsedTimeMillis = 10_000L

    override fun currentTimeMillis(): Long = wallTimeMillis

    override fun elapsedRealtimeMillis(): Long = elapsedTimeMillis

    fun advanceBy(durationMillis: Long) {
        wallTimeMillis += durationMillis
        elapsedTimeMillis += durationMillis
    }
}

private class FakeIdGenerator : IdGenerator {
    private var nextId = 0

    override fun newId(): String {
        val id = "id-$nextId"
        nextId += 1
        return id
    }
}
