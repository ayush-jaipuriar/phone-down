@file:Suppress("MagicNumber", "LongMethod")

package phonedown.feature.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import phonedown.core.charts.FocusHeatmap
import phonedown.core.charts.PhoneDownBarChart
import phonedown.core.charts.PhoneDownLineChart
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownMetricCard
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode
import phonedown.domain.insights.AdvancedInsights
import phonedown.domain.insights.BestDayResult
import phonedown.domain.insights.BestHourResult
import phonedown.domain.insights.FocusQualityLabel
import phonedown.domain.insights.FocusQualityResult
import phonedown.domain.insights.HeatmapDay
import phonedown.domain.insights.InsightSummary
import phonedown.domain.insights.SessionHistoryItem
import phonedown.domain.insights.StreakResult
import phonedown.domain.insights.TrendPoint
import phonedown.domain.insights.WeeklyInsight
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class InsightsUiState(
    val today: InsightSummary = InsightSummary(),
    val weekly: WeeklyInsight? = null,
    val focusQuality: FocusQualityResult? = null,
    val streak: StreakResult? = null,
    val history: List<SessionHistoryItem> = emptyList(),
    val heatmap: List<HeatmapDay> = emptyList(),
    val bestHour: BestHourResult? = null,
    val bestDay: BestDayResult? = null,
    val completionRateTrend: List<TrendPoint> = emptyList(),
    val cleanRatioTrend: List<TrendPoint> = emptyList(),
    val interruptionTrend: List<TrendPoint> = emptyList(),
    val focusQualityTrend: List<TrendPoint> = emptyList(),
    val advanced: AdvancedInsights? = null,
    val isEmpty: Boolean = true,
    val isLoading: Boolean = true,
    val isProUser: Boolean = false,
)

@Composable
@Suppress("FunctionName", "LongMethod")
fun InsightsContent(
    uiState: InsightsUiState,
    onRefresh: () -> Unit,
) {
    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(InsightsTestTags.SCREEN),
    ) {
        PhoneDownTopBar(title = "Insights")

        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(color = PhoneDownDesign.colors.progress)
            }
            return@PhoneDownScreen
        }

        if (uiState.isEmpty) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "Complete your first focus session to see insights.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = PhoneDownDesign.colors.textSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(32.dp),
                )
            }
            return@PhoneDownScreen
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = PhoneDownSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
        ) {
            item { TodaySection(summary = uiState.today) }

            if (!uiState.isProUser && uiState.today.sessionCount >= 3) {
                item { UpsellBanner() }
            }

            uiState.weekly?.let { weekly ->
                item { WeeklyChartSection(weekly = weekly) }
            }

            uiState.focusQuality?.let { fq ->
                item { FocusQualitySection(quality = fq) }
            }

            uiState.streak?.let { streak ->
                item { StreakSection(streak = streak) }
            }

            if (uiState.history.isNotEmpty()) {
                item { HistorySection(history = uiState.history) }
            }

            item { Spacer(modifier = Modifier.height(PhoneDownSpacing.md)) }
            item { ProHeader() }

            if (uiState.isProUser) {
                if (uiState.heatmap.isNotEmpty()) {
                    item { HeatmapSection(days = uiState.heatmap) }
                }

                uiState.bestHour?.let { hour ->
                    item { BestTimeSection(bestHour = hour, bestDay = uiState.bestDay) }
                }

                if (uiState.completionRateTrend.isNotEmpty()) {
                    item { TrendSection(label = "Completion Rate %", points = uiState.completionRateTrend) }
                }
                if (uiState.cleanRatioTrend.isNotEmpty()) {
                    item { TrendSection(label = "Clean Ratio %", points = uiState.cleanRatioTrend) }
                }
                if (uiState.interruptionTrend.isNotEmpty()) {
                    item { TrendSection(label = "Interruptions", points = uiState.interruptionTrend) }
                }
                if (uiState.focusQualityTrend.isNotEmpty()) {
                    item { TrendSection(label = "Focus Quality", points = uiState.focusQualityTrend) }
                }

                uiState.advanced?.let { advanced ->
                    item { AdvancedSection(advanced = advanced) }
                }

                item { ExportSection() }
            } else {
                item { ProTeaserCard() }
            }
        }
    }
}

@Composable
private fun TodaySection(summary: InsightSummary) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Text(
                text = "Today",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
                PhoneDownMetricCard(
                    label = "Total Focus",
                    value = formatDuration(summary.totalFocusSeconds),
                    modifier = Modifier.weight(1f),
                )
                PhoneDownMetricCard(
                    label = "Sessions",
                    value = summary.sessionCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                PhoneDownMetricCard(
                    label = "Clean",
                    value = summary.cleanSessionCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun WeeklyChartSection(weekly: WeeklyInsight) {
    PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.WEEKLY_CHART)) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "7 Day Overview",
                        color = PhoneDownDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = formatDuration(weekly.totalFocusSeconds),
                        color = PhoneDownDesign.colors.textPrimary,
                        style = MaterialTheme.typography.headlineMedium,
                    )
                }
                weekly.changePercent?.let { pct ->
                    val label = if (pct >= 0) "+${pct.toInt()}%" else "${pct.toInt()}%"
                    val color =
                        if (pct >= 0) PhoneDownDesign.colors.success
                        else PhoneDownDesign.colors.danger
                    Text(
                        text = label,
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            val values = weekly.days.map { it.focusSeconds / 3600f }
            val labels = weekly.days.map { day ->
                val date = java.time.LocalDate.ofEpochDay(day.dateEpochDay)
                date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, Locale.getDefault())
            }
            PhoneDownBarChart(
                values = values,
                labels = labels,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp),
            )
        }
    }
}

@Composable
private fun FocusQualitySection(quality: FocusQualityResult) {
    PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.QUALITY_CARD)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Focus Quality",
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = quality.label.name,
                    color =
                        when (quality.label) {
                            FocusQualityLabel.Deep, FocusQualityLabel.Focused -> PhoneDownDesign.colors.success
                            FocusQualityLabel.Steady -> PhoneDownDesign.colors.progress
                            else -> PhoneDownDesign.colors.textSecondary
                        },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = quality.score.toString(),
                color = PhoneDownDesign.colors.textPrimary,
                style = MaterialTheme.typography.headlineMedium,
            )
        }
    }
}

@Composable
private fun StreakSection(streak: StreakResult) {
    PhoneDownCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Streak",
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                )
                Text(
                    text = "${streak.currentStreakDays} days",
                    color = PhoneDownDesign.colors.textPrimary,
                    style = MaterialTheme.typography.titleMedium,
                )
            }
            Text(
                text = "Best: ${streak.longestStreakDays}d",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Composable
private fun HistorySection(history: List<SessionHistoryItem>) {
    PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.SESSION_SUMMARY)) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Text(
                text = "Session History",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            history.forEach { item ->
                HistoryRow(item = item)
            }
        }
    }
}

@Composable
private fun HistoryRow(item: SessionHistoryItem) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = formatDate(item.startedAtEpochMillis),
                color = PhoneDownDesign.colors.textPrimary,
                style = MaterialTheme.typography.bodyMedium,
            )
            Text(
                text = formatDuration(item.validFocusSeconds),
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelSmall,
            )
        }
        val resultText =
            when {
                item.broken -> "Broken"
                item.clean -> "Clean"
                else -> item.result?.name ?: "Active"
            }
        val resultColor =
            when {
                item.broken -> PhoneDownDesign.colors.danger
                item.clean -> PhoneDownDesign.colors.success
                else -> PhoneDownDesign.colors.textSecondary
            }
        Text(
            text = resultText,
            color = resultColor,
            style = MaterialTheme.typography.labelMedium,
        )
    }
}

@Composable
private fun ProHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PhoneDownSpacing.screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Advanced Insights",
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelMedium,
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Pro",
            color = PhoneDownDesign.colors.progress,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun HeatmapSection(days: List<HeatmapDay>) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm)) {
            Text(
                text = "Focus Heatmap",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            FocusHeatmap(
                days = days,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun BestTimeSection(
    bestHour: BestHourResult?,
    bestDay: BestDayResult?,
) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Text(
                text = "Best Focus Time",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
                bestHour?.let { hour ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = formatHour(hour.hour),
                            color = PhoneDownDesign.colors.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = formatDuration(hour.focusSeconds),
                            color = PhoneDownDesign.colors.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
                bestDay?.let { day ->
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = dayOfWeekName(day.dayOfWeekValue),
                            color = PhoneDownDesign.colors.textPrimary,
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Text(
                            text = formatDuration(day.focusSeconds),
                            color = PhoneDownDesign.colors.textSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendSection(
    label: String,
    points: List<TrendPoint>,
) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Text(
                text = label,
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            PhoneDownLineChart(
                values = points.map { it.value },
                labels = points.map { it.label },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(120.dp),
            )
        }
    }
}

@Composable
private fun AdvancedSection(advanced: AdvancedInsights) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Text(
                text = "Season Highlights",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
            )
            AdvancedRow(label = "Longest Clean", value = formatDuration(advanced.longestCleanFocusSeconds))
            AdvancedRow(label = "Average Session", value = formatDuration(advanced.averageSessionSeconds))
            AdvancedRow(label = "Weekday Focus", value = formatDuration(advanced.weekdayFocusSeconds))
            AdvancedRow(label = "Weekend Focus", value = formatDuration(advanced.weekendFocusSeconds))
        }
    }
}

@Composable
private fun AdvancedRow(
    label: String,
    value: String,
) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = label,
            modifier = Modifier.weight(1f),
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
        Text(
            text = value,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

@Composable
private fun ExportSection() {
    PhoneDownCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Export Data",
                color = PhoneDownDesign.colors.textPrimary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = "Pro",
                color = PhoneDownDesign.colors.progress,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun ProTeaserCard() {
    PhoneDownCard {
        Column(
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "Upgrade to Pro",
                style = MaterialTheme.typography.titleSmall,
                color = PhoneDownDesign.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Unlock advanced insights including focus heatmaps, best focus times, trend analysis, and data export.",
                style = MaterialTheme.typography.bodyMedium,
                color = PhoneDownDesign.colors.textSecondary,
                textAlign = TextAlign.Center,
            )
            Text(
                text = "Pro",
                style = MaterialTheme.typography.labelSmall,
                color = PhoneDownDesign.colors.progress,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
private fun UpsellBanner() {
    PhoneDownCard {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
            ) {
                Text(
                    text = "See your focus patterns over time",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PhoneDownDesign.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = "Upgrade to Pro for advanced insights, heatmaps, and trend analysis.",
                    style = MaterialTheme.typography.bodySmall,
                    color = PhoneDownDesign.colors.textSecondary,
                )
            }
            Text(
                text = "Pro",
                style = MaterialTheme.typography.labelSmall,
                color = PhoneDownDesign.colors.progress,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val hours = seconds / 3600
    val minutes = (seconds % 3600) / 60
    return if (hours > 0) "${hours}h ${minutes}m" else "${minutes}m"
}

private fun formatDate(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

private fun formatHour(hour: Int): String =
    when {
        hour == 0 -> "12 AM"
        hour < 12 -> "$hour AM"
        hour == 12 -> "12 PM"
        else -> "${hour - 12} PM"
    }

private fun dayOfWeekName(value: Int): String =
    when (value) {
        1 -> "Monday"
        2 -> "Tuesday"
        3 -> "Wednesday"
        4 -> "Thursday"
        5 -> "Friday"
        6 -> "Saturday"
        7 -> "Sunday"
        else -> ""
    }

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun InsightsContentLightPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        InsightsContent(
            uiState =
                InsightsUiState(
                    today =
                        InsightSummary(
                            totalFocusSeconds = 4800,
                            sessionCount = 3,
                            cleanSessionCount = 2,
                        ),
                    focusQuality = FocusQualityResult(78, FocusQualityLabel.Focused, 0.8f, 0.6f, 0.5f, 0.9f),
                    streak = StreakResult(5, 12),
                    isEmpty = false,
                    isLoading = false,
                ),
            onRefresh = {},
        )
    }
}
