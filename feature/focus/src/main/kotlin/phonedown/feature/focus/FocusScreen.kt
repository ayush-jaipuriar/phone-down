package phonedown.feature.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import phonedown.core.designsystem.PhoneDownAccent
import phonedown.core.designsystem.PhoneDownButton
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownMetricCard
import phonedown.core.designsystem.PhoneDownProgressRing
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName", "LongMethod")
fun FocusScreen(onStartFocusClick: () -> Unit = {}) {
    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(FocusTestTags.SCREEN),
    ) {
        PhoneDownTopBar(title = "Phone Down")

        Spacer(modifier = Modifier.height(PhoneDownSpacing.xl))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
        ) {
            PhoneDownProgressRing(progress = 0f) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "25:00",
                        modifier = Modifier.testTag(FocusTestTags.TIMER),
                        color = PhoneDownDesign.colors.textPrimary,
                        style = MaterialTheme.typography.displayLarge,
                    )
                    Text(
                        text = "Focus",
                        color = PhoneDownDesign.colors.textSecondary,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            PhoneDownButton(
                text = "Start Focus",
                onClick = onStartFocusClick,
                modifier = Modifier.testTag(FocusTestTags.START_BUTTON),
            )

            Text(
                text = "Default 25 min",
                color = PhoneDownDesign.colors.textSecondary,
                style = MaterialTheme.typography.labelMedium,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        PhoneDownCard(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = PhoneDownSpacing.md)
                    .testTag(FocusTestTags.TODAY_METRICS),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm)) {
                Text(
                    text = "TODAY",
                    color = PhoneDownDesign.colors.textTertiary,
                    style = MaterialTheme.typography.labelSmall,
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
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
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun FocusScreenLightPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        FocusScreen()
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun FocusScreenDarkPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Dark) {
        FocusScreen()
    }
}
