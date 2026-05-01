package phonedown.core.database.repository

import androidx.room.withTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import phonedown.core.database.PhoneDownDatabase
import phonedown.core.database.mapper.toDomainModel
import phonedown.core.database.mapper.toEntity
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent
import phonedown.core.model.repository.SessionRepository
import javax.inject.Inject

class RoomSessionRepository @Inject constructor(
    private val database: PhoneDownDatabase,
) : SessionRepository {

    private val sessionDao = database.focusSessionDao()
    private val penaltyEventDao = database.penaltyEventDao()

    override suspend fun upsertSession(session: FocusSession) {
        sessionDao.upsertSession(session.toEntity())
    }

    override fun observeSession(id: String): Flow<FocusSession?> {
        return sessionDao.getSession(id).map { it?.toDomainModel() }
    }

    override suspend fun getSession(id: String): FocusSession? {
        return sessionDao.getSessionOnce(id)?.toDomainModel()
    }

    override fun observeLatestSessions(limit: Int): Flow<List<FocusSession>> {
        return sessionDao.observeLatestSessions(limit).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long
    ): Flow<List<FocusSession>> {
        return sessionDao.observeSessionsInWindow(startEpochMillis, endEpochMillis).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getRecoverableSessions(): List<FocusSession> {
        return sessionDao.getRecoverableSessions().map { it.toDomainModel() }
    }

    override suspend fun recordPenaltyEvent(event: PenaltyEvent) {
        penaltyEventDao.insertPenaltyEvent(event.toEntity())
    }

    override suspend fun upsertSessionWithPenaltyEvent(session: FocusSession, event: PenaltyEvent) {
        database.withTransaction {
            sessionDao.upsertSession(session.toEntity())
            penaltyEventDao.insertPenaltyEvent(event.toEntity())
        }
    }

    override fun observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>> {
        return penaltyEventDao.observePenaltyEventsForSession(sessionId).map { list ->
            list.map { it.toDomainModel() }
        }
    }

    override suspend fun getPenaltyEvents(sessionId: String): List<PenaltyEvent> {
        return penaltyEventDao.getPenaltyEventsForSession(sessionId).map { it.toDomainModel() }
    }
}
