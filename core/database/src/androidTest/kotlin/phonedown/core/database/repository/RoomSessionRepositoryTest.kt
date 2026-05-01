package phonedown.core.database.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import phonedown.core.database.PhoneDownDatabase
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionState

@RunWith(AndroidJUnit4::class)
class RoomSessionRepositoryTest {
    private lateinit var database: PhoneDownDatabase
    private lateinit var repository: RoomSessionRepository

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(
            context,
            PhoneDownDatabase::class.java
        ).build()
        repository = RoomSessionRepository(database)
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createDomainSession(id: String = "session-1") = FocusSession(
        id = id,
        plannedDurationSeconds = 1500,
        requiredDurationSeconds = 1500,
        validFocusSeconds = 0,
        actualElapsedSeconds = 0,
        penaltySeconds = 0,
        interruptionCount = 0,
        minorInterruptionCount = 0,
        penaltyInterruptionCount = 0,
        startedAtEpochMillis = 1000L,
        endedAtEpochMillis = null,
        startElapsedRealtime = 5000L,
        endElapsedRealtime = null,
        state = SessionState.Active,
        result = null,
        clean = true,
        broken = false,
        callInterrupted = false,
        createdAtEpochMillis = 900L,
        updatedAtEpochMillis = 1000L
    )

    private fun createDomainPenalty(id: String = "penalty-1", sessionId: String = "session-1") = PenaltyEvent(
        id = id,
        sessionId = sessionId,
        type = PenaltyEventType.MinorPickup,
        startedAtEpochMillis = 1100L,
        endedAtEpochMillis = 1105L,
        durationSeconds = 5,
        penaltySeconds = 0
    )

    @Test
    fun upsertSessionWithPenaltyEventWritesAtomically() = runTest {
        val session = createDomainSession()
        val penalty = createDomainPenalty()

        repository.upsertSessionWithPenaltyEvent(session, penalty)

        val retrievedSession = repository.getSession("session-1")
        assertEquals(session, retrievedSession)

        val retrievedPenalties = repository.getPenaltyEvents("session-1")
        assertEquals(1, retrievedPenalties.size)
        assertEquals(penalty, retrievedPenalties[0])
    }

    @Test
    fun observeLatestSessionsReturnsDomainModels() = runTest {
        repository.upsertSession(createDomainSession("session-1"))
        
        val latest = repository.observeLatestSessions(10).first()
        assertEquals(1, latest.size)
        assertEquals("session-1", latest[0].id)
        assertEquals(SessionState.Active, latest[0].state)
    }
}
