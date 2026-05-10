@file:Suppress("MagicNumber")

package phonedown.domain.session

import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState

class SessionRecoveryClassifier(
    private val clock: Clock,
) {
    fun classify(session: FocusSession): FocusSession {
        val nowWall = clock.currentTimeMillis()
        val nowElapsed = clock.elapsedRealtimeMillis()
        val actualElapsedSeconds =
            ((nowElapsed - session.startElapsedRealtime).coerceAtLeast(0L) / 1_000L)

        return when (session.state) {
            SessionState.Created,
            SessionState.WaitingForPhoneDown,
            SessionState.Arming,
            SessionState.PausedByPickup,
            SessionState.PausedByCall,
            SessionState.PausedByUser,
            ->
                session.copy(
                    state = SessionState.Abandoned,
                    result = SessionResult.Abandoned,
                    clean = false,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                    actualElapsedSeconds = actualElapsedSeconds,
                    updatedAtEpochMillis = nowWall,
                )

            SessionState.Active ->
                session.copy(
                    state = SessionState.Broken,
                    result = SessionResult.Broken,
                    clean = false,
                    broken = true,
                    endedAtEpochMillis = nowWall,
                    endElapsedRealtime = nowElapsed,
                    actualElapsedSeconds = actualElapsedSeconds,
                    updatedAtEpochMillis = nowWall,
                )

            SessionState.Completed,
            SessionState.EndedEarly,
            SessionState.Invalidated,
            SessionState.Broken,
            SessionState.Abandoned,
            -> session
        }
    }
}
