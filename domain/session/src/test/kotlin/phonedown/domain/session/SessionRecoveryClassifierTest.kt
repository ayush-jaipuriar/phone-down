package phonedown.domain.session

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState

class SessionRecoveryClassifierTest {
    private val clock =
        object : Clock {
            override fun currentTimeMillis(): Long = 50_000L

            override fun elapsedRealtimeMillis(): Long = 80_000L
        }

    private val classifier = SessionRecoveryClassifier(clock)

    @Test
    fun waitingSessionRecoversAsAbandoned() {
        val recovered = classifier.classify(session(state = SessionState.WaitingForPhoneDown))

        assertEquals(SessionState.Abandoned, recovered.state)
        assertEquals(SessionResult.Abandoned, recovered.result)
    }

    @Test
    fun activeSessionRecoversAsBroken() {
        val recovered = classifier.classify(session(state = SessionState.Active))

        assertEquals(SessionState.Broken, recovered.state)
        assertEquals(SessionResult.Broken, recovered.result)
        assertTrue(recovered.broken)
    }

    @Test
    fun pausedByCallRecoversAsAbandoned() {
        val recovered = classifier.classify(session(state = SessionState.PausedByCall))

        assertEquals(SessionState.Abandoned, recovered.state)
        assertEquals(SessionResult.Abandoned, recovered.result)
    }

    private fun session(state: SessionState): FocusSession =
        FocusSession(
            id = "session-1",
            plannedDurationSeconds = 1_500L,
            requiredDurationSeconds = 1_500L,
            validFocusSeconds = 0L,
            actualElapsedSeconds = 0L,
            penaltySeconds = 0L,
            interruptionCount = 0,
            minorInterruptionCount = 0,
            penaltyInterruptionCount = 0,
            startedAtEpochMillis = 10_000L,
            endedAtEpochMillis = null,
            startElapsedRealtime = 20_000L,
            endElapsedRealtime = null,
            state = state,
            result = null,
            clean = true,
            broken = false,
            callInterrupted = false,
            createdAtEpochMillis = 10_000L,
            updatedAtEpochMillis = 10_000L,
        )
}
