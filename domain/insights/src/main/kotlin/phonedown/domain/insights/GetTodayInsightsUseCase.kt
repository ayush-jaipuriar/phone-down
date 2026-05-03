package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetTodayInsightsUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): InsightSummary {
        val now = clock.currentTimeMillis()
        val todayStart = startOfTodayMillis(now)

        val sessions = sessionRepository.observeSessionsInWindow(todayStart, now).first()

        return summarize(sessions)
    }

    companion object {
        fun startOfTodayMillis(nowMillis: Long): Long {
            val today = LocalDate.ofInstant(Instant.ofEpochMilli(nowMillis), ZoneId.systemDefault())
            return today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        }

        fun summarize(sessions: List<FocusSession>): InsightSummary {
            val meaningful = sessions.filter { it.result != SessionResult.Abandoned }
            var totalFocus = 0L
            var cleanCount = 0
            var interruptionCount = 0
            var penaltyCount = 0
            var penaltySeconds = 0L
            var brokenCount = 0
            var invalidatedCount = 0
            var abandonedCount = 0

            for (s in sessions) {
                if (s.result == SessionResult.Abandoned) {
                    abandonedCount++
                    continue
                }
                totalFocus += s.validFocusSeconds
                if (s.clean) cleanCount++
                interruptionCount += s.interruptionCount
                penaltyCount += s.penaltyInterruptionCount
                penaltySeconds += s.penaltySeconds
                when (s.result) {
                    SessionResult.Broken -> brokenCount++
                    SessionResult.Invalidated -> invalidatedCount++
                    else -> {}
                }
            }

            return InsightSummary(
                totalFocusSeconds = totalFocus,
                sessionCount = meaningful.size,
                cleanSessionCount = cleanCount,
                interruptionCount = interruptionCount,
                penaltyCount = penaltyCount,
                penaltySeconds = penaltySeconds,
                incompleteSessionCount = brokenCount + invalidatedCount,
                brokenSessionCount = brokenCount,
                invalidatedSessionCount = invalidatedCount,
                abandonedSessionCount = abandonedCount,
            )
        }
    }
}
