package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.DayOfWeek
import java.time.Instant
import java.time.ZoneId

class GetAdvancedInsightsUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): AdvancedInsights? {
        val now = clock.currentTimeMillis()
        val windowStart = now - 365L * 24 * 60 * 60 * 1000L

        val sessions = fetchSessions(windowStart, now)
            .filter { it.result != SessionResult.Abandoned && it.validFocusSeconds > 0 }

        if (sessions.isEmpty()) return null

        val cleanSessions = sessions.filter { it.clean }
        val longestClean = cleanSessions.maxOfOrNull { it.validFocusSeconds } ?: 0L

        val avgSession = sessions.sumOf { it.validFocusSeconds } / sessions.size

        var weekday = 0L
        var weekend = 0L
        for (s in sessions) {
            val dow = Instant.ofEpochMilli(s.startedAtEpochMillis).atZone(ZoneId.systemDefault()).dayOfWeek
            if (dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY) {
                weekend += s.validFocusSeconds
            } else {
                weekday += s.validFocusSeconds
            }
        }

        return AdvancedInsights(
            longestCleanFocusSeconds = longestClean,
            averageSessionSeconds = avgSession,
            weekdayFocusSeconds = weekday,
            weekendFocusSeconds = weekend,
        )
    }

    private suspend fun fetchSessions(startMillis: Long, endMillis: Long): List<FocusSession> =
        sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()
}
