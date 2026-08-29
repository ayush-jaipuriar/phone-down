package phonedown.feature.pro

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName")
fun ProScreen(
    uiState: ProScreenState = ProScreenState(),
    onBack: () -> Unit,
) {
    PhoneDownScreen(modifier = Modifier.fillMaxSize()) {
        PhoneDownTopBar(
            title = "Phone Down Pro",
            trailing = {
                Text(
                    text = "Close",
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onBack),
                )
            },
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.lg))

        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg)) {
            Text(
                text = "Your complete focus toolkit",
                style = MaterialTheme.typography.headlineSmall,
                color = PhoneDownDesign.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = "Explore the tools that support consistent, reflective focus.",
                style = MaterialTheme.typography.bodyLarge,
                color = PhoneDownDesign.colors.textSecondary,
            )
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
            ) {
                uiState.features.forEach { feature ->
                    ProFeatureRow(feature)
                }
            }
        }
    }
}

@Composable
private fun ProFeatureRow(feature: ProFeatureSummary) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier =
                Modifier
                    .padding(top = 7.dp)
                    .size(8.dp)
                    .clip(MaterialTheme.shapes.small)
                    .background(PhoneDownDesign.colors.toggle),
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xxs),
        ) {
            Text(
                text = feature.title,
                style = MaterialTheme.typography.titleSmall,
                color = PhoneDownDesign.colors.textPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = feature.description,
                style = MaterialTheme.typography.bodyMedium,
                color = PhoneDownDesign.colors.textSecondary,
            )
        }
    }
}

data class ProScreenState(
    val features: List<ProFeatureSummary> = defaultProFeatures,
)

data class ProFeatureSummary(
    val title: String,
    val description: String,
)

private val defaultProFeatures =
    listOf(
        ProFeatureSummary(
            title = "Advanced insights",
            description = "Heatmaps, trends, quality patterns, and focus highlights.",
        ),
        ProFeatureSummary(
            title = "Unlimited history",
            description = "Review your complete focus-session history.",
        ),
        ProFeatureSummary(
            title = "Flexible focus controls",
            description = "Use custom durations and the complete focus toolkit.",
        ),
        ProFeatureSummary(
            title = "Backup and restore",
            description = "Protect focus data with your Google account.",
        ),
    )

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun ProScreenPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        ProScreen(onBack = {})
    }
}
