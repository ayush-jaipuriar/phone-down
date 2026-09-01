package phonedown.core.database.dao

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
import phonedown.core.database.entity.FocusSessionEntity
import phonedown.core.database.entity.PenaltyEventEntity
import phonedown.core.database.mapper.toStorageString
import phonedown.core.model.PenaltyEventType
import phonedown.core.model.SessionState

@RunWith(AndroidJUnit4::class)
class FocusSessionDaoTest {
    private lateinit var database: PhoneDownDatabase
    private lateinit var sessionDao: FocusSessionDao
    private lateinit var penaltyDao: PenaltyEventDao

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database =
            Room
                .inMemoryDatabaseBuilder(
                    context,
                    PhoneDownDatabase::class.java,
                ).build()
        sessionDao = database.focusSessionDao()
        penaltyDao = database.penaltyEventDao()
    }

    @After
    fun teardown() {
        database.close()
    }

    private fun createSessionEntity(
        id: String = "session-1",
        startedAt: Long = 1000L,
        state: String = SessionState.Active.toStorageString(),
    ) = FocusSessionEntity(
        id = id,
        plannedDurationSeconds = 1500,
        requiredDurationSeconds = 1500,
        validFocusSeconds = 0,
        actualElapsedSeconds = 0,
        penaltySeconds = 0,
        interruptionCount = 0,
        minorInterruptionCount = 0,
        penaltyInterruptionCount = 0,
        startedAtEpochMillis = startedAt,
        endedAtEpochMillis = null,
        startElapsedRealtime = 5000L,
        endElapsedRealtime = null,
        state = state,
        result = null,
        clean = true,
        broken = false,
        callInterrupted = false,
        createdAtEpochMillis = 900L,
        updatedAtEpochMillis = 1000L,
    )

    private fun createPenaltyEntity(
        id: String = "penalty-1",
        sessionId: String = "session-1",
    ) = PenaltyEventEntity(
        id = id,
        sessionId = sessionId,
        type = PenaltyEventType.MinorPickup.toStorageString(),
        startedAtEpochMillis = 1100L,
        endedAtEpochMillis = 1105L,
        durationSeconds = 5,
        penaltySeconds = 0,
    )

    @Test
    fun upsertAndGetSession() =
        runTest {
            val entity = createSessionEntity()
            sessionDao.upsertSession(entity)

            val retrieved = sessionDao.getSession("session-1").first()
            assertEquals(entity, retrieved)
        }

    @Test
    fun deleteSessionCascadesToPenaltyEvents() =
        runTest {
            val session = createSessionEntity()
            sessionDao.upsertSession(session)

            val penalty = createPenaltyEntity()
            penaltyDao.insertPenaltyEvent(penalty)

            val eventsBefore = penaltyDao.getPenaltyEventsForSession("session-1")
            assertEquals(1, eventsBefore.size)

            sessionDao.deleteSession("session-1")

            val eventsAfter = penaltyDao.getPenaltyEventsForSession("session-1")
            assertTrue(eventsAfter.isEmpty())
        }

    @Test
    fun getRecoverableSessionsFiltersCorrectly() =
        runTest {
            val active = createSessionEntity(id = "active", state = SessionState.Active.toStorageString())
            val waiting =
                createSessionEntity(
                    id = "waiting",
                    state = SessionState.WaitingForPhoneDown.toStorageString(),
                )
            val completed = createSessionEntity(id = "completed", state = SessionState.Completed.toStorageString())
            val broken = createSessionEntity(id = "broken", state = SessionState.Broken.toStorageString())
            val userPaused = createSessionEntity(id = "user-paused", state = SessionState.PausedByUser.toStorageString())

            sessionDao.upsertSession(active)
            sessionDao.upsertSession(waiting)
            sessionDao.upsertSession(completed)
            sessionDao.upsertSession(broken)
            sessionDao.upsertSession(userPaused)

            val recoverable = sessionDao.getRecoverableSessions()
            assertEquals(3, recoverable.size)
            assertEquals(
                setOf("active", "waiting", "user-paused"),
                recoverable.map { it.id }.toSet(),
            )
        }

    @Test
    fun observeLatestSessionsReturnsOrdered() =
        runTest {
            sessionDao.upsertSession(createSessionEntity(id = "older", startedAt = 1000L))
            sessionDao.upsertSession(createSessionEntity(id = "newer", startedAt = 2000L))

            val latest = sessionDao.observeLatestSessions(2).first()
            assertEquals(2, latest.size)
            assertEquals("newer", latest[0].id)
            assertEquals("older", latest[1].id)
        }
}
