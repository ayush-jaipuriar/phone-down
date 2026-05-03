@file:Suppress("MaxLineLength")

package phonedown.domain.insights

import phonedown.core.model.SessionResult

data class InsightSummary(
    val totalFocusSeconds: Long = 0,
    val sessionCount: Int = 0,
    val cleanSessionCount: Int = 0,
    val interruptionCount: Int = 0,
    val penaltyCount: Int = 0,
    val penaltySeconds: Long = 0,
    val incompleteSessionCount: Int = 0,
    val brokenSessionCount: Int = 0,
    val invalidatedSessionCount: Int = 0,
    val abandonedSessionCount: Int = 0,
)

data class DayInsight(
    val dateEpochDay: Long,
    val focusSeconds: Long,
    val sessionCount: Int,
    val cleanSessionCount: Int,
)

data class WeeklyInsight(
    val days: List<DayInsight>,
    val totalFocusSeconds: Long,
    val changePercent: Float?,
)

data class FocusQualityResult(
    val score: Int,
    val label: FocusQualityLabel,
    val completionRate: Float,
    val cleanRatio: Float,
    val focusVolumeScore: Float,
    val interruptionScore: Float,
)

enum class FocusQualityLabel {
    Deep,
    Focused,
    Steady,
    Fragmented,
    Scattered,
}

data class StreakResult(
    val currentStreakDays: Int,
    val longestStreakDays: Int,
)

data class BestHourResult(
    val hour: Int,
    val focusSeconds: Long,
)

data class BestDayResult(
    val dayOfWeekValue: Int,
    val focusSeconds: Long,
)

data class TrendPoint(
    val label: String,
    val value: Float,
)

data class HeatmapDay(
    val dateEpochDay: Long,
    val focusMinutes: Int,
    val level: Int,
)

data class SessionHistoryItem(
    val sessionId: String,
    val startedAtEpochMillis: Long,
    val plannedDurationSeconds: Long,
    val validFocusSeconds: Long,
    val result: SessionResult?,
    val clean: Boolean,
    val broken: Boolean,
)

data class AdvancedInsights(
    val longestCleanFocusSeconds: Long = 0,
    val averageSessionSeconds: Long = 0,
    val weekdayFocusSeconds: Long = 0,
    val weekendFocusSeconds: Long = 0,
)
