package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetHeatmapDataUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(daysBack: Int = 371): List<HeatmapDay> {
        val now = clock.currentTimeMillis()
        val today = LocalDate.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault())
        val startDate = today.minusDays(daysBack.toLong())
        val windowStart = startDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

        val sessions = fetchSessions(windowStart, now)

        val minutesByDay = mutableMapOf<Long, Long>()
        for (s in sessions) {
            if (s.result == SessionResult.Abandoned) continue
            val epochDay = GetStreakUseCase.epochDayOf(s.startedAtEpochMillis)
            minutesByDay[epochDay] = (minutesByDay[epochDay] ?: 0) + s.validFocusSeconds / 60
        }

        val result = mutableListOf<HeatmapDay>()
        var date = startDate
        while (!date.isAfter(today)) {
            val epochDay = date.toEpochDay()
            val minutes = (minutesByDay[epochDay] ?: 0).toInt()
            val level = when {
                minutes == 0 -> 0
                minutes <= 15 -> 1
                minutes <= 30 -> 2
                minutes <= 60 -> 3
                else -> 4
            }
            result.add(HeatmapDay(dateEpochDay = epochDay, focusMinutes = minutes, level = level))
            date = date.plusDays(1)
        }

        return result
    }

    private suspend fun fetchSessions(startMillis: Long, endMillis: Long): List<FocusSession> =
        sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()
}
