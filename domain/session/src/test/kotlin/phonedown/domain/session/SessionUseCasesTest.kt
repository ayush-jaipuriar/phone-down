package phonedown.domain.session

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.common.Clock
import phonedown.core.common.IdGenerator
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.repository.SessionRepository

class SessionUseCasesTest {
    private val clock = TestClock()
    private val idGenerator = TestIdGenerator()
    private val engine = SessionEngine(clock = clock, idGenerator = idGenerator)

    @Test
    fun startSessionPersistsCreatedRuntime() = runTest {
        val repository = FakeSessionRepository()
        val useCase = StartSessionUseCase(engine, repository)

        val runtime = useCase(plannedDurationSeconds = 1_500L)

        assertEquals(runtime.session, repository.sessionsById[runtime.session.id])
    }

    @Test
    fun processInputPersistsPenaltyEventAlongsideSession() = runTest {
        val repository = FakeSessionRepository()
        val startSessionUseCase = StartSessionUseCase(engine, repository)
        val processInputUseCase = ProcessSessionInputUseCase(engine, repository)

        var runtime = startSessionUseCase(plannedDurationSeconds = 600L)
        runtime = processInputUseCase(runtime, SessionInput.PhoneBecameValid).runtime
        clock.advanceBy(3_000L)
        runtime = processInputUseCase(runtime, SessionInput.Tick).runtime
        runtime = processInputUseCase(runtime, SessionInput.PhoneBecameInvalid).runtime
        clock.advanceBy(6_000L)
        val transition = processInputUseCase(runtime, SessionInput.Tick)

        assertEquals(1, transition.penaltyEvents.size)
        assertEquals(1, repository.recordedEvents.size)
        assertEquals(660L, repository.sessionsById[transition.session.id]?.requiredDurationSeconds)
    }

    @Test
    fun recoverSessionsPersistsRecoveredStates() = runTest {
        val repository = FakeSessionRepository()
        val activeSession = engine.startSession(600L).session.copy(state = phonedown.core.model.SessionState.Active)
        repository.sessionsById[activeSession.id] = activeSession
        repository.recoverableSessions = listOf(activeSession)
        val useCase = RecoverSessionsUseCase(
            sessionRepository = repository,
            sessionRecoveryClassifier = SessionRecoveryClassifier(clock),
        )

        val recoveredSessions = useCase()

        assertEquals(1, recoveredSessions.size)
        assertEquals(phonedown.core.model.SessionState.Broken, recoveredSessions.first().state)
        assertTrue(repository.upsertedSessions.any { it.id == activeSession.id })
    }
}

private class TestClock : Clock {
    private var wallTimeMillis = 1_000_000L
    private var elapsedTimeMillis = 10_000L

    override fun currentTimeMillis(): Long = wallTimeMillis

    override fun elapsedRealtimeMillis(): Long = elapsedTimeMillis

    fun advanceBy(durationMillis: Long) {
        wallTimeMillis += durationMillis
        elapsedTimeMillis += durationMillis
    }
}

private class TestIdGenerator : IdGenerator {
    private var nextId = 0

    override fun newId(): String {
        val value = "generated-$nextId"
        nextId += 1
        return value
    }
}

private class FakeSessionRepository : SessionRepository {
    val sessionsById = linkedMapOf<String, FocusSession>()
    val recordedEvents = mutableListOf<PenaltyEvent>()
    val upsertedSessions = mutableListOf<FocusSession>()
    var recoverableSessions: List<FocusSession> = emptyList()

    override suspend fun upsertSession(session: FocusSession) {
        sessionsById[session.id] = session
        upsertedSessions += session
    }

    override fun observeSession(id: String): Flow<FocusSession?> = flowOf(sessionsById[id])

    override suspend fun getSession(id: String): FocusSession? = sessionsById[id]

    override fun observeLatestSessions(limit: Int): Flow<List<FocusSession>> {
        return flowOf(sessionsById.values.toList().takeLast(limit).reversed())
    }

    override fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<FocusSession>> = flowOf(
        sessionsById.values.filter { session ->
            session.startedAtEpochMillis in startEpochMillis..endEpochMillis
        },
    )

    override suspend fun getRecoverableSessions(): List<FocusSession> = recoverableSessions

    override suspend fun recordPenaltyEvent(event: PenaltyEvent) {
        recordedEvents += event
    }

    override suspend fun upsertSessionWithPenaltyEvent(
        session: FocusSession,
        event: PenaltyEvent,
    ) {
        upsertSession(session)
        recordPenaltyEvent(event)
    }

    override fun observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>> {
        return flowOf(recordedEvents.filter { it.sessionId == sessionId })
    }

    override suspend fun getPenaltyEvents(sessionId: String): List<PenaltyEvent> {
        return recordedEvents.filter { it.sessionId == sessionId }
    }
}
