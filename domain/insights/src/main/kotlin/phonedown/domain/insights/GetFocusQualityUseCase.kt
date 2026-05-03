package phonedown.domain.insights

import kotlinx.coroutines.flow.first
import phonedown.core.common.Clock
import phonedown.core.model.FocusSession
import phonedown.core.model.SessionResult
import phonedown.core.model.repository.SessionRepository

class GetFocusQualityUseCase(
    private val sessionRepository: SessionRepository,
    private val clock: Clock,
) {
    suspend operator fun invoke(): FocusQualityResult? {
        val now = clock.currentTimeMillis()
        val sevenDaysAgo = GetTodayInsightsUseCase.startOfTodayMillis(now) - 7 * 24 * 60 * 60 * 1000L

        val sessions = fetchSessions(sevenDaysAgo, now)
            .filter { it.result != SessionResult.Abandoned }

        if (sessions.isEmpty()) return null

        val activeSessionIds = activeSessionIds(sessions)
        if (activeSessionIds.isEmpty()) return null

        val totalSessions = activeSessionIds.size.toFloat()
        val completedSessions = sessions.filter {
            it.id in activeSessionIds &&
                (it.result == SessionResult.CleanCompleted || it.result == SessionResult.CompletedWithInterruption)
        }.size.toFloat()

        val cleanCompleted = sessions.filter {
            it.id in activeSessionIds &&
                it.result == SessionResult.CleanCompleted
        }.size.toFloat()

        val completionRate = if (totalSessions > 0) completedSessions / totalSessions else 0f
        val cleanRatio = if (completedSessions > 0) cleanCompleted / completedSessions else 0f

        val totalFocusSeconds = sessions.filter { it.id in activeSessionIds }.sumOf { it.validFocusSeconds }
        val focusVolumeScore = normalizeFocusVolume(totalFocusSeconds)

        val totalInterruptions = sessions.sumOf { it.interruptionCount }.toFloat()
        val interruptionScore = normalizeInterruptions(totalInterruptions, totalSessions)

        val score = ((completionRate * 40f) + (cleanRatio * 25f) + (focusVolumeScore * 20f) + (interruptionScore * 15f))
            .toInt()
            .coerceIn(0, 100)

        val label = when (score) {
            in 90..100 -> FocusQualityLabel.Deep
            in 75..89 -> FocusQualityLabel.Focused
            in 60..74 -> FocusQualityLabel.Steady
            in 40..59 -> FocusQualityLabel.Fragmented
            else -> FocusQualityLabel.Scattered
        }

        return FocusQualityResult(
            score = score,
            label = label,
            completionRate = completionRate,
            cleanRatio = cleanRatio,
            focusVolumeScore = focusVolumeScore,
            interruptionScore = interruptionScore,
        )
    }

    private suspend fun fetchSessions(startMillis: Long, endMillis: Long): List<FocusSession> =
        sessionRepository.observeSessionsInWindow(startMillis, endMillis).first()

    companion object {
        fun activeSessionIds(sessions: List<FocusSession>): Set<String> =
            sessions.filter { it.validFocusSeconds > 0 }.map { it.id }.toSet()

        fun normalizeFocusVolume(totalFocusSeconds: Long): Float {
            val hours = totalFocusSeconds / 3600f
            return (hours / 14f).coerceIn(0f, 1f)
        }

        fun normalizeInterruptions(totalInterruptions: Float, sessionCount: Float): Float {
            if (sessionCount <= 0f) return 1f
            val avgInterruptions = totalInterruptions / sessionCount
            return (1f - (avgInterruptions / 5f)).coerceIn(0f, 1f)
        }
    }
}
