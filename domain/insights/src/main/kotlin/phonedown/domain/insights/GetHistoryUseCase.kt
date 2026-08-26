package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.model.repository.SessionRepository

class GetHistoryUseCase(
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        page: Int = 0,
        pageSize: Int = 20,
    ): List<SessionHistoryItem> {
        val sessions = sessionRepository.observeLatestSessions(limit = (page + 1) * pageSize).first()

        val startIndex = page * pageSize
        val pageItems = sessions.drop(startIndex).take(pageSize)

        return pageItems.map { session ->
            SessionHistoryItem(
                sessionId = session.id,
                startedAtEpochMillis = session.startedAtEpochMillis,
                plannedDurationSeconds = session.plannedDurationSeconds,
                validFocusSeconds = session.validFocusSeconds,
                result = session.result,
                clean = session.clean,
                broken = session.broken,
            )
        }
    }
}
