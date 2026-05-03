@file:Suppress("MaxLineLength")

package phonedown.app.runtime

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.core.common.Clock
import phonedown.core.common.IdGenerator
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.SessionState
import phonedown.core.model.ThemeMode
import phonedown.core.model.UserSettings
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository
import phonedown.core.sensors.FocusStabilityState
import phonedown.core.sensors.FocusValidityReason
import phonedown.core.sensors.FocusValidityResult
import phonedown.domain.session.EndSessionUseCase
import phonedown.domain.session.ProcessSessionInputUseCase
import phonedown.domain.session.RecoverSessionsUseCase
import phonedown.domain.session.SessionEngine
import phonedown.domain.session.SessionRecoveryClassifier
import phonedown.domain.session.StartSessionUseCase

class ActiveSessionRuntimeCoordinatorTest {
    private val clock = TestClock()
    private val idGenerator = TestIdGenerator()
    private val sessionEngine = SessionEngine(clock = clock, idGenerator = idGenerator)

    @Test
    fun recoverFromAppLaunchSkipsClassificationWhileRuntimeIsActive() =
        runTest {
            val sessionRepository = FakeSessionRepository()
            val settingsRepository = FakeSettingsRepository()
            val coordinator = createCoordinator(sessionRepository, settingsRepository)

            val started = coordinator.ensureSessionStarted().state.session ?: error("session missing")
            sessionRepository.recoverableSessions = listOf(started.copy(state = SessionState.Active))

            coordinator.recoverFromAppLaunch()

            assertEquals(1, sessionRepository.upsertedSessions.size)
            assertTrue(sessionRepository.upsertedSessions.none { it.result != null })
        }

    @Test
    fun recoverFromUnexpectedServiceRestartClassifiesDanglingSession() =
        runTest {
            val sessionRepository = FakeSessionRepository()
            val settingsRepository = FakeSettingsRepository()
            val coordinator = createCoordinator(sessionRepository, settingsRepository)
            val danglingSession =
                sessionEngine
                    .startSession(plannedDurationSeconds = 600L)
                    .session
                    .copy(state = SessionState.Active)
            sessionRepository.sessionsById[danglingSession.id] = danglingSession
            sessionRepository.recoverableSessions = listOf(danglingSession)

            coordinator.recoverFromUnexpectedServiceRestart()

            val recovered = sessionRepository.sessionsById.getValue(danglingSession.id)
            assertEquals(SessionState.Broken, recovered.state)
            assertTrue(recovered.broken)
        }

    @Test
    fun callStateChangesPauseAndResumeSession() =
        runTest {
            val sessionRepository = FakeSessionRepository()
            val settingsRepository = FakeSettingsRepository()
            val coordinator = createCoordinator(sessionRepository, settingsRepository)

            coordinator.ensureSessionStarted()
            coordinator.onSensorValidityChanged(faceDownStable())
            clock.advanceBy(3_000L)
            coordinator.onTick()

            val paused = coordinator.onCallStateChanged(isInCall = true).state.session ?: error("paused")
            assertEquals(SessionState.PausedByCall, paused.state)

            val resumed = coordinator.onCallStateChanged(isInCall = false).state.session ?: error("resumed")
            assertEquals(SessionState.WaitingForPhoneDown, resumed.state)
            assertFalse(resumed.clean)
            assertTrue(sessionRepository.recordedEvents.any { it.sessionId == resumed.id })
        }

    @Test
    fun activeTicksPersistOnFiveSecondCadence() =
        runTest {
            val sessionRepository = FakeSessionRepository()
            val settingsRepository = FakeSettingsRepository()
            val coordinator = createCoordinator(sessionRepository, settingsRepository)

            coordinator.ensureSessionStarted()
            coordinator.onSensorValidityChanged(faceDownStable())
            clock.advanceBy(3_000L)
            coordinator.onTick()
            val persistedBeforeTicks = sessionRepository.upsertedSessions.size

            repeat(4) {
                clock.advanceBy(1_000L)
                coordinator.onTick()
            }

            assertEquals(persistedBeforeTicks, sessionRepository.upsertedSessions.size)

            clock.advanceBy(1_000L)
            coordinator.onTick()

            assertTrue(sessionRepository.upsertedSessions.size > persistedBeforeTicks)
        }

    private fun createCoordinator(
        sessionRepository: FakeSessionRepository,
        settingsRepository: FakeSettingsRepository,
    ): ActiveSessionRuntimeCoordinator =
        ActiveSessionRuntimeCoordinator(
            startSessionUseCase = StartSessionUseCase(sessionEngine, sessionRepository),
            endSessionUseCase =
                EndSessionUseCase(
                    ProcessSessionInputUseCase(sessionEngine, sessionRepository),
                ),
            recoverSessionsUseCase =
                RecoverSessionsUseCase(
                    sessionRepository = sessionRepository,
                    sessionRecoveryClassifier = SessionRecoveryClassifier(clock),
                ),
            sessionEngine = sessionEngine,
            sessionRepository = sessionRepository,
            settingsRepository = settingsRepository,
        )

    private fun faceDownStable(): FocusValidityResult =
        FocusValidityResult(
            isValid = true,
            reason = FocusValidityReason.FaceDownStable,
            stabilityState = FocusStabilityState.Stable,
            orientationConfidence = 1f,
            movementScore = 0.05f,
        )
}

private class FakeSettingsRepository(
    initialSettings: UserSettings = UserSettings(),
) : SettingsRepository {
    private val settingsFlow = MutableStateFlow(initialSettings)

    override val settings: Flow<UserSettings> = settingsFlow

    override suspend fun setDefaultDurationSeconds(seconds: Long) {
        settingsFlow.value = settingsFlow.value.copy(defaultDurationSeconds = seconds)
    }

    override suspend fun setSoundEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(soundEnabled = enabled)
    }

    override suspend fun setHapticsEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(hapticsEnabled = enabled)
    }

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsFlow.value = settingsFlow.value.copy(themeMode = themeMode)
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(onboardingCompleted = completed)
    }

    override suspend fun setBackupOptIn(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(backupOptIn = enabled)
    }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        settingsFlow.value = settingsFlow.value.copy(autoBackupEnabled = enabled)
    }

    override suspend fun setLastBackupEpochMillis(epochMillis: Long?) {
        settingsFlow.value = settingsFlow.value.copy(lastBackupEpochMillis = epochMillis)
    }

    override suspend fun setFreeCustomDurationSeconds(seconds: Long?) {
        settingsFlow.value = settingsFlow.value.copy(freeCustomDurationSeconds = seconds)
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
        val id = "session-$nextId"
        nextId += 1
        return id
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

    override fun observeLatestSessions(limit: Int): Flow<List<FocusSession>> =
        flowOf(
            sessionsById.values
                .toList()
                .takeLast(limit)
                .reversed(),
        )

    override fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<FocusSession>> =
        flowOf(
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

    override fun observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>> =
        flowOf(recordedEvents.filter { it.sessionId == sessionId })

    override suspend fun getPenaltyEvents(sessionId: String): List<PenaltyEvent> = recordedEvents.filter { it.sessionId == sessionId }

    override suspend fun getAllSessions(): List<FocusSession> = sessionsById.values.toList()

    override suspend fun getAllPenaltyEvents(): List<PenaltyEvent> = recordedEvents

    override suspend fun clearAllSessions() {
        sessionsById.clear()
    }

    override suspend fun clearAllPenaltyEvents() {
        recordedEvents.clear()
    }
}
