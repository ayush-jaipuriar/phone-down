package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetStreakUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): StreakResult? {
        val now = clock.currentTimeMillis()
        val todayEpochDay = LocalDate.ofInstant(Instant.ofEpochMilli(now), ZoneId.systemDefault()).toEpochDay()

        val distantPast = now - 365L * 24 * 60 * 60 * 1000L
        val sessions = fetchSessions(distantPast, now)

        val daysWithSessions = sessions
            .filter { it.result != SessionResult.Abandoned && it.validFocusSeconds > 0 }
            .map { epochDayOf(it.startedAtEpochMillis) }
            .toSet()

        if (daysWithSessions.isEmpty()) return StreakResult(currentStreakDays = 0, longestStreakDays = 0)

        val currentStreak = computeCurrentStreak(todayEpochDay, daysWithSessions)
        val longestStreak = computeLongestStreak(daysWithSessions)

        return StreakResult(
            currentStreakDays = currentStreak,
            longestStreakDays = longestStreak,
        )
    }

    private suspend fun fetchSessions(startMillis: Long, endMillis: Long): List<FocusSession> =
        sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()

    companion object {
        fun epochDayOf(epochMillis: Long): Long {
            return LocalDate.ofInstant(Instant.ofEpochMilli(epochMillis), ZoneId.systemDefault()).toEpochDay()
        }

        fun computeCurrentStreak(todayEpochDay: Long, activeDays: Set<Long>): Int {
            var streak = 0
            var day = todayEpochDay
            while (day in activeDays) {
                streak++
                day--
            }
            return streak
        }

        fun computeLongestStreak(activeDays: Set<Long>): Int {
            if (activeDays.isEmpty()) return 0
            val sorted = activeDays.sorted()
            var longest = 1
            var current = 1
            for (i in 1 until sorted.size) {
                if (sorted[i] == sorted[i - 1] + 1) {
                    current++
                    longest = maxOf(longest, current)
                } else {
                    current = 1
                }
            }
            return longest
        }
    }
}
