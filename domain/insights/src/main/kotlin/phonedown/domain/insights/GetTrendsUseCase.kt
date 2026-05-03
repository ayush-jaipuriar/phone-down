package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class GetTrendsUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    data class AllTrends(
        val completionRate: List<TrendPoint>,
        val cleanRatio: List<TrendPoint>,
        val interruptions: List<TrendPoint>,
        val focusQuality: List<TrendPoint>,
    )

    suspend operator fun invoke(weeksBack: Int = 12): AllTrends {
        val now = clock.currentTimeMillis()
        val windowStart = now - weeksBack.toLong() * 7 * 24 * 60 * 60 * 1000L

        val sessions = fetchSessions(windowStart, now)
        val weekBuckets = buildWeekBuckets(sessions, now, weeksBack)

        val completionRate = mutableListOf<TrendPoint>()
        val cleanRatio = mutableListOf<TrendPoint>()
        val interruptions = mutableListOf<TrendPoint>()
        val focusQuality = mutableListOf<TrendPoint>()

        for ((weekLabel, weekSessions) in weekBuckets) {
            val active = weekSessions.filter {
                it.result != SessionResult.Abandoned && it.validFocusSeconds > 0
            }
            if (active.isEmpty()) {
                completionRate.add(TrendPoint(weekLabel, 0f))
                cleanRatio.add(TrendPoint(weekLabel, 0f))
                interruptions.add(TrendPoint(weekLabel, 0f))
                focusQuality.add(TrendPoint(weekLabel, 0f))
                continue
            }

            val completed = active.count {
                it.result == SessionResult.CleanCompleted || it.result == SessionResult.CompletedWithInterruption
            }
            val cleanCompleted = active.count { it.result == SessionResult.CleanCompleted }
            val cr = completed.toFloat() / active.size
            val crPct = cr * 100f

            val cleanR = if (completed > 0) cleanCompleted.toFloat() / completed else 0f
            val cleanRPct = cleanR * 100f

            val totalInterruptions = active.sumOf { it.interruptionCount }
            val avgInterruptions = totalInterruptions.toFloat() / active.size

            val totalFocusSeconds = active.sumOf { it.validFocusSeconds }
            val fqScore = computeSimpleFQ(cr, cleanR, totalFocusSeconds, avgInterruptions, active.size)

            completionRate.add(TrendPoint(weekLabel, crPct))
            cleanRatio.add(TrendPoint(weekLabel, cleanRPct))
            interruptions.add(TrendPoint(weekLabel, avgInterruptions))
            focusQuality.add(TrendPoint(weekLabel, fqScore.toFloat()))
        }

        return AllTrends(completionRate, cleanRatio, interruptions, focusQuality)
    }

    private suspend fun fetchSessions(startMillis: Long, endMillis: Long): List<FocusSession> =
        sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()

    companion object {
        fun buildWeekBuckets(
            sessions: List<FocusSession>,
            nowMillis: Long,
            weeksBack: Int,
        ): List<Pair<String, List<FocusSession>>> {
            val todayEnd = nowMillis
            val buckets = mutableListOf<Pair<String, List<FocusSession>>>()

            for (i in weeksBack - 1 downTo 0) {
                val weekEnd = todayEnd - i.toLong() * 7 * 24 * 60 * 60 * 1000L
                val weekStart = weekEnd - 7 * 24 * 60 * 60 * 1000L
                val startDate = LocalDate.ofInstant(Instant.ofEpochMilli(weekStart), ZoneId.systemDefault())
                val label = "W${weeksBack - i}"
                val inWeek = sessions.filter {
                    it.startedAtEpochMillis in weekStart until weekEnd
                }
                buckets.add(label to inWeek)
            }

            return buckets
        }

        fun computeSimpleFQ(
            completionRate: Float,
            cleanRatio: Float,
            totalFocusSeconds: Long,
            avgInterruptions: Float,
            sessionCount: Int,
        ): Int {
            val fvScore = ((totalFocusSeconds / 3600f) / 7f).coerceIn(0f, 1f)
            val intScore = (1f - (avgInterruptions / 5f).coerceIn(0f, 1f))
            return ((completionRate * 40f) + (cleanRatio * 25f) + (fvScore * 20f) + (intScore * 15f))
                .toInt()
                .coerceIn(0, 100)
        }
    }
}
