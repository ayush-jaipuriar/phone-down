package phonedown.core.model.repository

import kotlinx.coroutines.flow.Flow
import phonedown.core.model.FocusSession
import phonedown.core.model.PenaltyEvent

interface SessionRepository {
    suspend fun upsertSession(session: FocusSession)

    fun observeSession(id: String): Flow<FocusSession?>

    suspend fun getSession(id: String): FocusSession?

    fun observeLatestSessions(limit: Int): Flow<List<FocusSession>>

    fun observeSessionsInWindow(
        startEpochMillis: Long,
        endEpochMillis: Long,
    ): Flow<List<FocusSession>>

    suspend fun getRecoverableSessions(): List<FocusSession>

    suspend fun recordPenaltyEvent(event: PenaltyEvent)

    suspend fun upsertSessionWithPenaltyEvent(
        session: FocusSession,
        event: PenaltyEvent,
    )

    fun observePenaltyEvents(sessionId: String): Flow<List<PenaltyEvent>>

    suspend fun getPenaltyEvents(sessionId: String): List<PenaltyEvent>

    suspend fun getAllSessions(): List<FocusSession>

    suspend fun getAllPenaltyEvents(): List<PenaltyEvent>

    suspend fun clearAllSessions()

    suspend fun clearAllPenaltyEvents()
}
