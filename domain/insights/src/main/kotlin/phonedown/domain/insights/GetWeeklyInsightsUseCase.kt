package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetWeeklyInsightsUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): WeeklyInsight {
        val now = clock.currentTimeMillis()
        val todayStart = GetTodayInsightsUseCase.startOfTodayMillis(now)
        val sevenDaysAgo = todayStart - 7 * 24 * 60 * 60 * 1000L
        val fourteenDaysAgo = todayStart - 14 * 24 * 60 * 60 * 1000L

        val currentSessions = fetchSessions(sevenDaysAgo, now)
        val previousSessions = fetchSessions(fourteenDaysAgo, sevenDaysAgo)

        val days = buildDayInsights(currentSessions, todayStart)
        val totalFocus = days.sumOf { it.focusSeconds }
        val previousTotal = previousSessions
            .filter { it.result != SessionResult.Abandoned }
            .sumOf { it.validFocusSeconds }
        val changePercent = if (previousTotal > 0) {
            ((totalFocus - previousTotal).toFloat() / previousTotal) * 100f
        } else {
            null
        }

        return WeeklyInsight(
            days = days,
            totalFocusSeconds = totalFocus,
            changePercent = changePercent,
        )
    }

    private suspend fun fetchSessions(startMillis: Long, endMillis: Long): List<FocusSession> =
        sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()

    private fun buildDayInsights(sessions: List<FocusSession>, todayStartMillis: Long): List<DayInsight> {
        val dayMap = mutableMapOf<Long, DayInsight>()
        for (i in 0 until 7) {
            val dayStart = todayStartMillis - i * 24 * 60 * 60 * 1000L
            val epochDay = Instant.ofEpochMilli(dayStart).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
            dayMap[epochDay] = DayInsight(
                dateEpochDay = epochDay,
                focusSeconds = 0,
                sessionCount = 0,
                cleanSessionCount = 0,
            )
        }

        for (s in sessions) {
            if (s.result == SessionResult.Abandoned) continue
            val dayStart = startOfDayMillis(s.startedAtEpochMillis)
            val epochDay = Instant.ofEpochMilli(dayStart).atZone(ZoneId.systemDefault()).toLocalDate().toEpochDay()
            val existing = dayMap[epochDay] ?: continue
            dayMap[epochDay] = existing.copy(
                focusSeconds = existing.focusSeconds + s.validFocusSeconds,
                sessionCount = existing.sessionCount + 1,
                cleanSessionCount = existing.cleanSessionCount + if (s.clean) 1 else 0,
            )
        }

        return dayMap.values.sortedByDescending { it.dateEpochDay }
    }

    companion object {
        fun startOfDayMillis(epochMillis: Long): Long {
            val date = LocalDate.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault())
            return date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }
    }
}
