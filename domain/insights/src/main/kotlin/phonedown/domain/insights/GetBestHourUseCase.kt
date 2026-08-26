package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.ZoneId

class GetBestHourUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(daysBack: Int = 90): BestHourResult? {
        val now = clock.currentTimeMillis()
        val windowStart = now - daysBack.toLong() * 24 * 60 * 60 * 1000L

        val sessions =
            fetchSessions(windowStart, now)
                .filter { it.result != SessionResult.Abandoned && it.validFocusSeconds > 0 }

        if (sessions.isEmpty()) return null

        val hourMap = mutableMapOf<Int, Long>()
        for (s in sessions) {
            val hour = Instant.ofEpochMilli(s.startedAtEpochMillis).atZone(ZoneId.systemDefault()).hour
            hourMap[hour] = (hourMap[hour] ?: 0) + s.validFocusSeconds
        }

        val best = hourMap.maxByOrNull { it.value } ?: return null
        return BestHourResult(hour = best.key, focusSeconds = best.value)
    }

    private suspend fun fetchSessions(
        startMillis: Long,
        endMillis: Long,
    ): List<FocusSession> = sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()
}
