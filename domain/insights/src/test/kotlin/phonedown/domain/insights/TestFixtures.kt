package phonedown.domain.insights

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionResult
import phonedown.core.model.SessionState
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.ZoneId

object TestFixtures {
    fun testSession(
        id: String = "session-1",
        startedAtEpochMillis: Long = Instant.parse("2026-01-15T10:00:00Z").toEpochMilli(),
        validFocusSeconds: Long = 1500,
        clean: Boolean = true,
        result: SessionResult? = SessionResult.CleanCompleted,
        broken: Boolean = false,
        interruptionCount: Int = 0,
        penaltyInterruptionCount: Int = 0,
        penaltySeconds: Long = 0,
        plannedDurationSeconds: Long = 1500,
    ): FocusSession = FocusSession(
        id = id,
        plannedDurationSeconds = plannedDurationSeconds,
        requiredDurationSeconds = 1200,
        validFocusSeconds = validFocusSeconds,
        actualElapsedSeconds = 1500,
        penaltySeconds = penaltySeconds,
        interruptionCount = interruptionCount,
        minorInterruptionCount = 0,
        penaltyInterruptionCount = penaltyInterruptionCount,
        startedAtEpochMillis = startedAtEpochMillis,
        endedAtEpochMillis = startedAtEpochMillis + 1500_000L,
        startElapsedRealtime = 0,
        endElapsedRealtime = 1500_000L,
        state = SessionState.Completed,
        result = result,
        clean = clean,
        broken = broken,
        callInterrupted = false,
        createdAtEpochMillis = startedAtEpochMillis,
        updatedAtEpochMillis = startedAtEpochMillis,
    )

    fun jan15_2026_10am(): Long = Instant.parse("2026-01-15T10:00:00Z").toEpochMilli()

    fun jan15_2026_midnight(): Long {
        val instant = Instant.parse("2026-01-15T10:00:00Z")
        val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
    }
}

class FakeSessionRepository(
    private var sessions: List<FocusSession> = emptyList(),
    private var penaltyEvents: List<PenaltyEvent> = emptyList(),
) : SessionRepository {
    var upsertedSessions = mutableListOf<FocusSession>()

    fun setSessions(newSessions: List<FocusSession>) {
        sessions = newSessions
    }

    override suspend fun upsertSession(session: FocusSession) {
        upsertedSessions.add(session)
    }

    override fun observeSession(id: String): Flow<FocusSession?> = flowOf(sessions.find { it.id == id })

    override suspend fun getSession(id: String): FocusSession? = sessions.find { it.id == id }

    override fun observeLatestSessions(limit: Int): Flow<List<FocusSession>> =
        flowOf(sessions.sortedByDescending { it.startedAtEpochMillis }.take(limit))

    override fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<FocusSession>> =
        flowOf(sessions.filter { it.startedAtEpochMillis in startEpochMillis until endEpochMillis })

    override suspend fun getRecoverableSessions(): List<FocusSession> = emptyList()

    override suspend fun recordPenaltyEvent(event: PenaltyEvent) {
        penaltyEvents = penaltyEvents + event
    }

    override suspend fun upsertSessionWithPenaltyEvent(session: FocusSession, event: PenaltyEvent) {
        upsertSession(session)
        recordPenaltyEvent(event)
    }

    override fun observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>> =
        flowOf(penaltyEvents.filter { it.sessionId == sessionId })

    override suspend fun getPenaltyEvents(sessionId: String): List<PenaltyEvent> =
        penaltyEvents.filter { it.sessionId == sessionId }

    override suspend fun getAllSessions(): List<FocusSession> = sessions

    override suspend fun getAllPenaltyEvents(): List<PenaltyEvent> = penaltyEvents

    override suspend fun clearAllSessions() {
        sessions = emptyList()
    }

    override suspend fun clearAllPenaltyEvents() {
        penaltyEvents = emptyList()
    }
}

class TestClock(private var timeMillis: Long) : Clock {
    fun setTime(timeMillis: Long) {
        this.timeMillis = timeMillis
    }

    override fun currentTimeMillis(): Long = timeMillis

    override fun elapsedRealtimeMillis(): Long = timeMillis
}
