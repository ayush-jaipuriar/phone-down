package phonedown.feature.settings

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownProBadge
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSettingRow
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownSwitchRow
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownThemeControl
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName", "LongMethod")
fun SettingsScreen(
    onAccountClick: () -> Unit,
    onProClick: () -> Unit,
    selectedThemeMode: ThemeMode = ThemeMode.System,
    onThemeModeSelected: (ThemeMode) -> Unit = {},
) {
    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(SettingsTestTags.SCREEN),
    ) {
        PhoneDownTopBar(title = "Settings")

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        SettingsSection(title = "Focus") {
            PhoneDownSettingRow(
                title = "Default Duration",
                trailing = "25 min",
            )
            PhoneDownSettingRow(
                title = "Duration Presets",
                trailing = "10, 15, 25, 45, 60",
            )
            Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs)) {
                Text(
                    text = "Theme",
                    color = PhoneDownDesign.colors.textPrimary,
                    style = MaterialTheme.typography.bodyMedium,
                )
                PhoneDownThemeControl(
                    selectedThemeMode = selectedThemeMode,
                    onThemeModeSelected = onThemeModeSelected,
                    modifier = Modifier.testTag(SettingsTestTags.THEME_CONTROL),
                )
            }
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        SettingsSection(title = "Preferences") {
            PhoneDownSwitchRow(
                title = "Sounds",
                checked = true,
                onCheckedChange = {},
            )
            PhoneDownSwitchRow(
                title = "Haptics",
                checked = true,
                onCheckedChange = {},
            )
            PhoneDownSettingRow(
                title = "Start Delay",
                trailing = "3 seconds",
            )
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        SettingsSection(title = "Account") {
            PhoneDownSettingRow(
                title = "Google Account",
                supportingText = "john.doe@gmail.com",
                trailing = "Connected",
                modifier = Modifier.testTag(SettingsTestTags.ACCOUNT_ROW),
                onClick = onAccountClick,
            )
            Row(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .testTag(SettingsTestTags.PRO_ROW),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                PhoneDownSettingRow(
                    title = "Phone Down Pro",
                    supportingText = "Advanced insights, backup, and unlimited history",
                    modifier = Modifier.weight(1f),
                    onClick = onProClick,
                )
                PhoneDownProBadge()
            }
            PhoneDownSettingRow(
                title = "Backup & Restore",
                supportingText = "Last backup: Today, 9:15 AM",
                trailing = "Pro",
            )
        }
    }
}

@Composable
@Suppress("FunctionName")
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs)) {
        Text(
            text = title,
            color = PhoneDownDesign.colors.textPrimary,
            style = MaterialTheme.typography.labelLarge,
        )
        PhoneDownCard {
            Column(content = { content() })
        }
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun SettingsScreenLightPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        SettingsScreen(
            onAccountClick = {},
            onProClick = {},
            selectedThemeMode = ThemeMode.Light,
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun SettingsScreenDarkPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Dark) {
        SettingsScreen(
            onAccountClick = {},
            onProClick = {},
            selectedThemeMode = ThemeMode.Dark,
        )
    }
}
