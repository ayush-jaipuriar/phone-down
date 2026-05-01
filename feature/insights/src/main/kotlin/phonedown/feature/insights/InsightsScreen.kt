package phonedown.feature.insights

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownAccent
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownMetricCard
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode

@Suppress("MagicNumber")
private val WEEKLY_FOCUS_VALUES = listOf(3.9f, 3.2f, 4.5f, 3.8f, 2.7f, 4.0f, 0.5f)

private const val ROUNDED_CHART_CORNER_PX = 8f

@Composable
@Suppress("FunctionName", "LongMethod")
fun InsightsScreen() {
    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(InsightsTestTags.SCREEN),
    ) {
        PhoneDownTopBar(title = "Insights")

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

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
                        value = "1h 20m",
                        modifier = Modifier.weight(1f),
                    )
                    PhoneDownMetricCard(
                        label = "Sessions",
                        value = "3",
                        modifier = Modifier.weight(1f),
                    )
                    PhoneDownMetricCard(
                        label = "Clean",
                        value = "2",
                        modifier = Modifier.weight(1f),
                        accent = PhoneDownAccent.Success,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

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
                            text = "8h 45m",
                            color = PhoneDownDesign.colors.textPrimary,
                            style = MaterialTheme.typography.headlineMedium,
                        )
                    }
                    Text(
                        text = "+12%",
                        color = PhoneDownDesign.colors.success,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
                WeeklyFocusChart(
                    values = WEEKLY_FOCUS_VALUES,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(118.dp),
                )
            }
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.SESSION_SUMMARY)) {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
                SummaryRow(label = "Sessions", value = "18")
                SummaryRow(label = "Clean Sessions", value = "10")
                SummaryRow(label = "Clean Rate", value = "56%")
                SummaryRow(label = "Interruptions", value = "8")
            }
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        PhoneDownCard(modifier = Modifier.testTag(InsightsTestTags.QUALITY_CARD)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Focus Quality",
                        color = PhoneDownDesign.colors.textSecondary,
                        style = MaterialTheme.typography.labelMedium,
                    )
                    Text(
                        text = "Focused",
                        color = PhoneDownDesign.colors.success,
                        style = MaterialTheme.typography.titleMedium,
                    )
                }
                Text(
                    text = "78",
                    color = PhoneDownDesign.colors.textPrimary,
                    style = MaterialTheme.typography.headlineMedium,
                )
            }
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun SummaryRow(
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
@Suppress("FunctionName")
private fun WeeklyFocusChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
) {
    val progressColor = PhoneDownDesign.colors.progress
    val trackColor = PhoneDownDesign.colors.borderSubtle

    Canvas(modifier = modifier) {
        val maxValue = values.maxOrNull()?.coerceAtLeast(1f) ?: 1f
        val barWidth = size.width / ((values.size * 2) + 1)
        val gap = barWidth
        values.forEachIndexed { index, value ->
            val left = gap + (index * (barWidth + gap))
            val barHeight = (value / maxValue) * size.height
            drawRoundRect(
                color = trackColor,
                topLeft = Offset(left, 0f),
                size = Size(barWidth, size.height),
                cornerRadius = CornerRadius(ROUNDED_CHART_CORNER_PX, ROUNDED_CHART_CORNER_PX),
            )
            drawRoundRect(
                color = progressColor,
                topLeft = Offset(left, size.height - barHeight),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(ROUNDED_CHART_CORNER_PX, ROUNDED_CHART_CORNER_PX),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun InsightsScreenLightPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        InsightsScreen()
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun InsightsScreenDarkPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Dark) {
        InsightsScreen()
    }
}
