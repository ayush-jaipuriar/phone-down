@file:Suppress("MagicNumber", "LongMethod", "FunctionName", "MaxLineLength", "MatchingDeclarationName", "UnusedParameter")

package phonedown.feature.insights

import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import phonedown.core.charts.FocusHeatmap
import phonedown.core.charts.PhoneDownBarChart
import phonedown.core.charts.PhoneDownHourlyChart
import phonedown.core.charts.PhoneDownLineChart
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownCardHeaderTextStyle
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.domain.insights.AdvancedInsights
import phonedown.domain.insights.BestDayResult
import phonedown.domain.insights.BestHourResult
import phonedown.domain.insights.FocusQualityLabel
import phonedown.domain.insights.FocusQualityResult
import phonedown.domain.insights.HeatmapDay
import phonedown.domain.insights.HourFocus
import phonedown.domain.insights.InsightSummary
import phonedown.domain.insights.SessionHistoryItem
import phonedown.domain.insights.StreakResult
import phonedown.domain.insights.TrendPoint
import phonedown.domain.insights.WeeklyInsight
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.TextStyle
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
    val selectedDateEpochDay: Long? = null,
    val hourlyFocus: List<HourFocus> = emptyList(),
    val selectedDaySummary: InsightSummary? = null,
)

@Composable
@Suppress("FunctionName", "LongMethod")
fun InsightsContent(
    uiState: InsightsUiState,
    onRefresh: () -> Unit,
    onExport: () -> Unit = {},
    onDaySelected: (Long) -> Unit = {},
    onBackToToday: () -> Unit = {},
    referenceDate: LocalDate = LocalDate.now(),
) {
    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(InsightsTestTags.SCREEN),
        topPadding = PhoneDownSpacing.lg,
    ) {
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
            modifier =
                Modifier
                    .fillMaxSize()
                    .testTag(InsightsTestTags.LIST),
            contentPadding = PaddingValues(vertical = PhoneDownSpacing.md),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
        ) {
            item {
                InsightsCalendarStrip(
                    selectedDateEpochDay = uiState.selectedDateEpochDay,
                    onDaySelected = onDaySelected,
                    today = referenceDate,
                )
            }

            if (uiState.selectedDateEpochDay != null &&
                uiState.selectedDateEpochDay != referenceDate.toEpochDay()
            ) {
                item {
                    BackToTodayButton(onClick = onBackToToday)
                }
            }

            item {
                TodaySection(
                    summary = uiState.selectedDaySummary ?: uiState.today,
                    selectedDateEpochDay = uiState.selectedDateEpochDay,
                    referenceDate = referenceDate,
                )
            }

            if (uiState.hourlyFocus.any { it.focusMinutes > 0 }) {
                item { HourlyChartSection(hourlyFocus = uiState.hourlyFocus) }
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
            item { AdvancedInsightsHeader() }

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

            item { ExportSection(onClick = onExport) }
        }
    }
}

@Composable
private fun TodaySection(
    summary: InsightSummary,
    selectedDateEpochDay: Long? = null,
    referenceDate: LocalDate = LocalDate.now(),
) {
    val locale = LocalConfiguration.current.locales[0]
    val label =
        when (selectedDateEpochDay) {
            null -> "Today"
            referenceDate.toEpochDay() -> "Today"
            else -> {
                val date = LocalDate.ofEpochDay(selectedDateEpochDay)
                date.dayOfWeek.getDisplayName(TextStyle.FULL, locale)
            }
        }
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            InsightsCardTitle(text = label)
            Row(horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
                InsightsMetricCard(
                    label = "Total Focus",
                    value = formatDuration(summary.totalFocusSeconds),
                    modifier = Modifier.weight(1f),
                )
                InsightsMetricCard(
                    label = "Sessions",
                    value = summary.sessionCount.toString(),
                    modifier = Modifier.weight(1f),
                )
                InsightsMetricCard(
                    label = "Clean",
                    value = summary.cleanSessionCount.toString(),
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
private fun InsightsCardTitle(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = PhoneDownDesign.colors.textPrimary,
        style =
            PhoneDownCardHeaderTextStyle.copy(
                fontSize = 14.sp,
                lineHeight = 19.sp,
            ),
        modifier = modifier,
    )
}

@Composable
private fun InsightsMetricCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
    ) {
        Text(
            text = value,
            color = PhoneDownDesign.colors.textPrimary,
            style =
                MaterialTheme.typography.titleMedium.copy(
                    fontSize = 17.sp,
                    lineHeight = 23.sp,
                    fontWeight = FontWeight.Bold,
                ),
            textAlign = TextAlign.Center,
        )
        Text(
            text = label,
            color = PhoneDownDesign.colors.textSecondary,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontSize = 12.5.sp,
                    lineHeight = 17.sp,
                ),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun InsightsHeroValue(
    text: String,
    color: Color = PhoneDownDesign.colors.textPrimary,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        style =
            MaterialTheme.typography.headlineMedium.copy(
                fontSize = 24.sp,
                lineHeight = 30.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        modifier = modifier,
    )
}

@Composable
private fun InsightsEmphasisValue(
    text: String,
    color: Color = PhoneDownDesign.colors.textPrimary,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        style =
            MaterialTheme.typography.titleMedium.copy(
                fontSize = 17.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.SemiBold,
            ),
        modifier = modifier,
    )
}

@Composable
private fun InsightsPrimaryBody(
    text: String,
    color: Color = PhoneDownDesign.colors.textPrimary,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        color = color,
        style =
            MaterialTheme.typography.bodyMedium.copy(
                fontSize = 14.sp,
                lineHeight = 20.sp,
            ),
        modifier = modifier,
    )
}

@Composable
private fun BackToTodayButton(onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End,
    ) {
        Text(
            text = "\u2190 Back to Today",
            color = PhoneDownDesign.colors.progress,
            style =
                MaterialTheme.typography.labelMedium.copy(
                    fontSize = 13.sp,
                    lineHeight = 18.sp,
                ),
            modifier = Modifier.clickable(onClick = onClick),
        )
    }
}

@Composable
private fun HourlyChartSection(hourlyFocus: List<HourFocus>) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            InsightsCardTitle(text = "Focus by Hour")
            PhoneDownHourlyChart(
                values = hourlyFocus.map { it.focusMinutes },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(140.dp),
            )
        }
    }
}

@Composable
private fun WeeklyChartSection(weekly: WeeklyInsight) {
    PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.WEEKLY_CHART)) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    InsightsCardTitle(text = "7 Day Overview")
                    InsightsHeroValue(
                        text = formatDuration(weekly.totalFocusSeconds),
                    )
                }
                weekly.changePercent?.let { pct ->
                    val label = if (pct >= 0) "+${pct.toInt()}%" else "${pct.toInt()}%"
                    val color =
                        if (pct >= 0) {
                            PhoneDownDesign.colors.success
                        } else {
                            PhoneDownDesign.colors.danger
                        }
                    Text(
                        text = label,
                        color = color,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
            val locale = LocalConfiguration.current.locales[0]
            val values = weekly.days.map { it.focusSeconds / 3600f }
            val labels =
                weekly.days.map { day ->
                    val date = java.time.LocalDate.ofEpochDay(day.dateEpochDay)
                    date.dayOfWeek.getDisplayName(java.time.format.TextStyle.SHORT, locale)
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
                InsightsCardTitle(text = "Focus Quality")
                InsightsEmphasisValue(
                    text = quality.label.name,
                    color =
                        when (quality.label) {
                            FocusQualityLabel.Deep, FocusQualityLabel.Focused -> PhoneDownDesign.colors.success
                            FocusQualityLabel.Steady -> PhoneDownDesign.colors.progress
                            else -> PhoneDownDesign.colors.textSecondary
                        },
                )
            }
            InsightsHeroValue(
                text = quality.score.toString(),
            )
        }
    }
}

@Composable
private fun StreakSection(streak: StreakResult) {
    PhoneDownCard {
        Row(modifier = Modifier.fillMaxWidth()) {
            Column(modifier = Modifier.weight(1f)) {
                InsightsCardTitle(text = "Streak")
                InsightsEmphasisValue(
                    text = "${streak.currentStreakDays} days",
                )
            }
            InsightsPrimaryBody(
                text = "Best: ${streak.longestStreakDays}d",
                color = PhoneDownDesign.colors.textSecondary,
            )
        }
    }
}

@Composable
private fun HistorySection(history: List<SessionHistoryItem>) {
    PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.SESSION_SUMMARY)) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            InsightsCardTitle(text = "Session History")
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
            InsightsPrimaryBody(
                text = formatDate(item.startedAtEpochMillis),
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
private fun AdvancedInsightsHeader() {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = PhoneDownSpacing.screen),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        InsightsCardTitle(
            text = "Advanced Insights",
        )
    }
}

@Composable
private fun HeatmapSection(days: List<HeatmapDay>) {
    PhoneDownCard {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm)) {
            InsightsCardTitle(text = "Focus Heatmap")
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
            InsightsCardTitle(text = "Best Focus Time")
            Row(horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
                bestHour?.let { hour ->
                    Column(modifier = Modifier.weight(1f)) {
                        InsightsEmphasisValue(
                            text = formatHour(hour.hour),
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
                        InsightsEmphasisValue(
                            text = dayOfWeekName(day.dayOfWeekValue),
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
            InsightsCardTitle(text = label)
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
    PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.ADVANCED_CARD)) {
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
            InsightsCardTitle(text = "Season Highlights")
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
        InsightsPrimaryBody(
            text = label,
            modifier = Modifier.weight(1f),
        )
        InsightsPrimaryBody(
            text = value,
        )
    }
}

@Composable
private fun ExportSection(onClick: () -> Unit) {
    PhoneDownCard(
        modifier =
            Modifier
                .clickable(role = Role.Button, onClick = onClick)
                .testTag(InsightsTestTags.EXPORT_DATA),
    ) {
        InsightsPrimaryBody(
            text = "Export Data",
            modifier = Modifier.fillMaxWidth(),
        )
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
