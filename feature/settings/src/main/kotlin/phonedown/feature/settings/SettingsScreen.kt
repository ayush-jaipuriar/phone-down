@file:Suppress("LongMethod", "MagicNumber")

package phonedown.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import phonedown.feature.settings.SettingsUiState
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val DURATION_PRESETS = listOf(10L, 15L, 25L, 45L, 60L)

@Composable
@Suppress("FunctionName")
fun SettingsScreen(
    uiState: SettingsUiState,
    onAccountClick: () -> Unit,
    onProClick: () -> Unit,
    onBackupClick: () -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(SettingsTestTags.SCREEN),
    ) {
        PhoneDownTopBar(title = "Settings")

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        TimerSection(
            uiState = uiState,
            onProClick = onProClick,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        PreferencesSection(
            uiState = uiState,
            onSoundToggled = onSoundToggled,
            onHapticsToggled = onHapticsToggled,
            onThemeModeSelected = onThemeModeSelected,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        AccountBackupSection(
            onAccountClick = onAccountClick,
            onProClick = onProClick,
            onBackupClick = onBackupClick,
            uiState = uiState,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        ProSection(onProClick = onProClick)

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        PrivacySection(
            onDeleteRequested = { showDeleteDialog = true },
            onProClick = onProClick,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        AboutSection()

        if (showDeleteDialog) {
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = { Text("Delete All Local Data") },
                text = { Text("This will permanently delete all your session history, settings, and preferences. This action cannot be undone.") },
                confirmButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Delete", color = PhoneDownDesign.colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }
    }
}

@Composable
private fun TimerSection(
    uiState: SettingsUiState,
    onProClick: () -> Unit,
) {
    SettingsSectionHeader(title = "Timer") {
        PhoneDownSettingRow(
            title = "Default Duration",
            trailing = formatDuration(uiState.defaultDurationSeconds),
        )
        PhoneDownSettingRow(
            title = "Duration Presets",
            trailing = DURATION_PRESETS.joinToString(", ") { "${it} min" },
        )
        PhoneDownSettingRow(
            title = "Custom Duration",
            supportingText = "Free tier limited to one custom slot",
            trailing = "Pro",
            onClick = onProClick,
        )
    }
}

@Composable
private fun PreferencesSection(
    uiState: SettingsUiState,
    onSoundToggled: (Boolean) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
) {
    SettingsSectionHeader(title = "Preferences") {
        PhoneDownSwitchRow(
            title = "Sounds",
            checked = uiState.soundEnabled,
            onCheckedChange = onSoundToggled,
            modifier = Modifier.testTag(SettingsTestTags.SOUND_SWITCH),
        )
        PhoneDownSwitchRow(
            title = "Haptics",
            checked = uiState.hapticsEnabled,
            onCheckedChange = onHapticsToggled,
            modifier = Modifier.testTag(SettingsTestTags.HAPTICS_SWITCH),
        )
        Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs)) {
            PhoneDownSettingRow(title = "Theme")
            PhoneDownThemeControl(
                selectedThemeMode = uiState.themeMode,
                onThemeModeSelected = onThemeModeSelected,
                modifier = Modifier.testTag(SettingsTestTags.THEME_CONTROL),
            )
        }
        PhoneDownSettingRow(
            title = "Start Delay",
            trailing = "3 seconds",
        )
    }
}

@Composable
private fun AccountBackupSection(
    uiState: SettingsUiState,
    onAccountClick: () -> Unit,
    onProClick: () -> Unit,
    onBackupClick: () -> Unit,
) {
    SettingsSectionHeader(title = "Account & Backup") {
        PhoneDownSettingRow(
            title = "Google Account",
            supportingText = if (uiState.isSignedIn) {
                "Manage your account and backup"
            } else {
                "Sign in to enable cloud backup"
            },
            trailing = if (uiState.isSignedIn) "Manage" else "Sign In",
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

        when {
            !uiState.isProUser -> {
                PhoneDownSettingRow(
                    title = "Backup & Restore",
                    supportingText = "Cloud backup for your sessions and settings",
                    trailing = "Pro",
                    onClick = onProClick,
                )
            }
            !uiState.isSignedIn -> {
                PhoneDownSettingRow(
                    title = "Backup & Restore",
                    supportingText = "Sign in to Google to enable backup",
                    trailing = "Sign In",
                    onClick = onAccountClick,
                )
            }
            else -> {
                PhoneDownSettingRow(
                    title = "Backup & Restore",
                    supportingText = if (uiState.isBackingUp) {
                        "Backing up..."
                    } else if (uiState.lastBackupEpochMillis != null) {
                        "Last backup: ${formatBackupTime(uiState.lastBackupEpochMillis)}"
                    } else {
                        "No backup yet. Tap to back up now."
                    },
                    trailing = if (uiState.isBackingUp) "..." else "Back Up",
                    onClick = onBackupClick,
                )
            }
        }

        if (uiState.isProUser && uiState.isSignedIn) {
            PhoneDownSettingRow(
                title = "Auto Backup",
                supportingText = "Daily automatic backup to Google Drive",
                trailing = if (uiState.autoBackupEnabled) "On" else "Off",
            )
        }
    }
}

@Composable
private fun ProSection(onProClick: () -> Unit) {
    SettingsSectionHeader(title = "Pro") {
        PhoneDownSettingRow(
            title = "Upgrade to Pro",
            supportingText = "Unlock advanced insights, backup, and unlimited history",
            trailing = "Pro",
            onClick = onProClick,
        )
        PhoneDownSettingRow(
            title = "Restore Purchases",
            trailing = "Pro",
        )
        PhoneDownSettingRow(
            title = "Manage Subscription",
            trailing = "Pro",
        )
    }
}

@Composable
private fun PrivacySection(
    onDeleteRequested: () -> Unit,
    onProClick: () -> Unit,
) {
    SettingsSectionHeader(title = "Privacy") {
        PhoneDownSettingRow(
            title = "Local Data",
            supportingText = "All session data and preferences are stored locally on your device",
        )
        PhoneDownSettingRow(
            title = "Cloud Backup",
            supportingText = "Pro users can backup data to Google Drive. Backup is opt-in and encrypted.",
        )
        PhoneDownSettingRow(
            title = "Export Data",
            supportingText = "Export your session history as a file",
            trailing = "Pro",
            onClick = onProClick,
        )
        PhoneDownSettingRow(
            title = "Delete All Local Data",
            supportingText = "Permanently remove all sessions and settings from this device",
            trailing = "Delete",
            onClick = onDeleteRequested,
        )
    }
}

@Composable
private fun AboutSection() {
    SettingsSectionHeader(title = "About") {
        PhoneDownSettingRow(
            title = "Version",
            trailing = "0.1.0",
        )
        PhoneDownSettingRow(
            title = "Privacy Policy",
            trailing = "View",
        )
        PhoneDownSettingRow(
            title = "Terms of Service",
            trailing = "View",
        )
        PhoneDownSettingRow(
            title = "Support",
            trailing = "Contact",
        )
    }
}

@Composable
private fun SettingsSectionHeader(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs)) {
        Text(
            text = title,
            color = PhoneDownDesign.colors.textSecondary,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
        )
        PhoneDownCard {
            Column(content = { content() })
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    return "${minutes} min"
}

private fun formatBackupTime(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun SettingsScreenLightPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        SettingsScreen(
            uiState = SettingsUiState(),
            onAccountClick = {},
            onProClick = {},
            onBackupClick = {},
            onSoundToggled = {},
            onHapticsToggled = {},
            onThemeModeSelected = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun SettingsScreenDarkPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Dark) {
        SettingsScreen(
            uiState = SettingsUiState(themeMode = ThemeMode.Dark),
            onAccountClick = {},
            onProClick = {},
            onBackupClick = {},
            onSoundToggled = {},
            onHapticsToggled = {},
            onThemeModeSelected = {},
        )
    }
}
