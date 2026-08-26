package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.ZoneId

class GetBestWeekdayUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(daysBack: Int = 90): BestDayResult? {
        val now = clock.currentTimeMillis()
        val windowStart = now - daysBack.toLong() * 24 * 60 * 60 * 1000L

        val sessions =
            fetchSessions(windowStart, now)
                .filter { it.result != SessionResult.Abandoned && it.validFocusSeconds > 0 }

        if (sessions.isEmpty()) return null

        val dayMap = mutableMapOf<Int, Long>()
        for (s in sessions) {
            val dayOfWeek =
                Instant
                    .ofEpochMilli(s.startedAtEpochMillis)
                    .atZone(ZoneId.systemDefault())
                    .dayOfWeek
                    .value
            dayMap[dayOfWeek] = (dayMap[dayOfWeek] ?: 0) + s.validFocusSeconds
        }

        val best = dayMap.maxByOrNull { it.value } ?: return null
        return BestDayResult(dayOfWeekValue = best.key, focusSeconds = best.value)
    }

    private suspend fun fetchSessions(
        startMillis: Long,
        endMillis: Long,
    ): List<FocusSession> = sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()
}
