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
        val zone = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(now), zone)
        val sevenDaysAgo =
            today
                .minusDays(7)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()
        val fourteenDaysAgo =
            today
                .minusDays(14)
                .atStartOfDay(zone)
                .toInstant()
                .toEpochMilli()

        val currentSessions = fetchSessions(sevenDaysAgo, now)
        val previousSessions = fetchSessions(fourteenDaysAgo, sevenDaysAgo)

        val days = buildDayInsights(currentSessions, now)
        val totalFocus = days.sumOf { it.focusSeconds }
        val previousTotal =
            previousSessions
                .filter { it.result != SessionResult.Abandoned }
                .sumOf { it.validFocusSeconds }
        val changePercent =
            if (previousTotal > 0) {
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

    private suspend fun fetchSessions(
        startMillis: Long,
        endMillis: Long,
    ): List<FocusSession> = sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()

    private fun buildDayInsights(
        sessions: List<FocusSession>,
        nowMillis: Long,
    ): List<DayInsight> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(nowMillis), zone)
        val dayMap = mutableMapOf<Long, DayInsight>()
        for (i in 0 until 7) {
            val epochDay = today.minusDays(i.toLong()).toEpochDay()
            dayMap[epochDay] =
                DayInsight(
                    dateEpochDay = epochDay,
                    focusSeconds = 0,
                    sessionCount = 0,
                    cleanSessionCount = 0,
                )
        }

        for (s in sessions) {
            if (s.result == SessionResult.Abandoned) continue
            val epochDay = LocalDate.ofInstant(Instant.ofEpochMilli(s.startedAtEpochMillis), zone).toEpochDay()
            val existing = dayMap[epochDay] ?: continue
            dayMap[epochDay] =
                existing.copy(
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
