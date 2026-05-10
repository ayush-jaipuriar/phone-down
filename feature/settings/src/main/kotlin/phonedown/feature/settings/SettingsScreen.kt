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
import androidx.compose.ui.tooling.preview.Preview
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownProBadge
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSettingRow
import phonedown.core.designsystem.PhoneDownSectionHeaderTextStyle
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownSwitchRow
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownThemeControl
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
    onPrivacyPolicyClick: () -> Unit,
    callPausePermissionGranted: Boolean = false,
    onCallPausePermissionRequested: () -> Unit = {},
    onTermsOfServiceClick: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onDeleteRequested: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmationTextChanged: (String) -> Unit,
    onDeleteIncludeBackupChanged: (Boolean) -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDefaultDurationClick: () -> Unit = {},
) {
    var showCallPauseEducation by remember { mutableStateOf(false) }

    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .testTag(SettingsTestTags.SCREEN),
        topPadding = PhoneDownSpacing.lg,
    ) {
        FocusSection(
            uiState = uiState,
            onProClick = onProClick,
            onSoundToggled = onSoundToggled,
            onHapticsToggled = onHapticsToggled,
            onThemeModeSelected = onThemeModeSelected,
            onDefaultDurationClick = onDefaultDurationClick,
            callPausePermissionGranted = callPausePermissionGranted,
            onCallPausePermissionClick = { showCallPauseEducation = true },
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        AccountSection(
            uiState = uiState,
            onAccountClick = onAccountClick,
            onProClick = onProClick,
            onBackupClick = onBackupClick,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        AboutSection(
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onDeleteRequested = onDeleteRequested,
        )

        if (uiState.showDeleteConfirmation) {
            DeleteConfirmationDialog(
                uiState = uiState,
                onConfirm = onDeleteConfirmed,
                onDismiss = onDeleteDismissed,
                onConfirmationTextChanged = onDeleteConfirmationTextChanged,
                onIncludeBackupChanged = onDeleteIncludeBackupChanged,
            )
        }

        if (showCallPauseEducation) {
            CallPausePermissionDialog(
                onConfirm = {
                    showCallPauseEducation = false
                    onCallPausePermissionRequested()
                },
                onDismiss = { showCallPauseEducation = false },
            )
        }
    }
}

@Composable
private fun FocusSection(
    uiState: SettingsUiState,
    onProClick: () -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDefaultDurationClick: () -> Unit,
    callPausePermissionGranted: Boolean,
    onCallPausePermissionClick: () -> Unit,
) {
    SettingsSectionHeader(title = "Focus") {
        PhoneDownSettingRow(
            title = "Default Duration",
            trailing = formatDuration(uiState.defaultDurationSeconds),
            showChevron = true,
            onClick = onDefaultDurationClick,
        )
        PhoneDownSettingRow(
            title = "Duration Presets",
            trailing = DURATION_PRESETS.joinToString(", ") { "$it min" },
        )
        PhoneDownSettingRow(
            title = "Custom Duration",
            supportingText = "Free tier limited to one custom slot",
            trailing = "Pro",
            showChevron = true,
            onClick = onProClick,
        )
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
        PhoneDownSettingRow(
            title = "Pause for calls",
            supportingText = "Let Phone Down pause automatically during phone calls",
            trailing = if (callPausePermissionGranted) "On" else "Permission needed",
            showChevron = !callPausePermissionGranted,
            onClick = if (callPausePermissionGranted) null else onCallPausePermissionClick,
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
private fun CallPausePermissionDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Allow call pause?") },
        text = {
            Text(
                "Phone Down can pause your focus timer when a phone call starts. Android requires phone-state permission for this, but the app does not read call audio, contacts, numbers, or call contents.",
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Allow")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Not now")
            }
        },
    )
}

@Composable
private fun AccountSection(
    uiState: SettingsUiState,
    onAccountClick: () -> Unit,
    onProClick: () -> Unit,
    onBackupClick: () -> Unit,
) {
    SettingsSectionHeader(title = "Account") {
        PhoneDownSettingRow(
            title = "Google Account",
            supportingText = if (uiState.isSignedIn) {
                "Manage your account and backup"
            } else {
                "Sign in to enable cloud backup"
            },
            trailing = if (uiState.isSignedIn) "Manage" else "Sign In",
            showChevron = true,
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
                showChevron = true,
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
                    showChevron = true,
                    onClick = onProClick,
                )
            }
            !uiState.isSignedIn -> {
                PhoneDownSettingRow(
                    title = "Backup & Restore",
                    supportingText = "Sign in to Google to enable backup",
                    trailing = "Sign In",
                    showChevron = true,
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
                    showChevron = true,
                    onClick = onBackupClick,
                )
            }
        }

    }
}

@Composable
private fun AboutSection(
    onPrivacyPolicyClick: () -> Unit,
    onDeleteRequested: () -> Unit,
) {
    SettingsSectionHeader(title = "About") {
        PhoneDownSettingRow(
            title = "Privacy Policy",
            showChevron = true,
            onClick = onPrivacyPolicyClick,
        )
        PhoneDownSettingRow(
            title = "Version",
            trailing = "0.1.0",
        )
        PhoneDownSettingRow(
            title = "Delete All Data",
            supportingText = "Permanently remove all sessions and settings from this device",
            destructive = true,
            onClick = onDeleteRequested,
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
            color = PhoneDownDesign.colors.textPrimary,
            style = PhoneDownSectionHeaderTextStyle,
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

@Composable
private fun DeleteConfirmationDialog(
    uiState: SettingsUiState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    onConfirmationTextChanged: (String) -> Unit,
    onIncludeBackupChanged: (Boolean) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Delete All Data") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md)) {
                Text(
                    "This will permanently delete:",
                    style = MaterialTheme.typography.bodyMedium,
                )
                Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs)) {
                    Text("• All focus sessions and history", style = MaterialTheme.typography.bodySmall)
                    Text("• All penalty events", style = MaterialTheme.typography.bodySmall)
                    Text("• All app settings and preferences", style = MaterialTheme.typography.bodySmall)
                    if (uiState.isSignedIn) {
                        Text("• Your account connection", style = MaterialTheme.typography.bodySmall)
                    }
                }

                if (uiState.isSignedIn) {
                    PhoneDownSwitchRow(
                        title = "Also delete cloud backup",
                        checked = uiState.deleteIncludeBackup,
                        onCheckedChange = onIncludeBackupChanged,
                    )
                }

                Text(
                    "Type DELETE to confirm:",
                    style = MaterialTheme.typography.labelMedium,
                    color = PhoneDownDesign.colors.textSecondary,
                )

                androidx.compose.material3.OutlinedTextField(
                    value = uiState.deleteConfirmationText,
                    onValueChange = onConfirmationTextChanged,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                enabled = uiState.deleteConfirmationText == "DELETE" && !uiState.isDeleting,
            ) {
                Text(
                    if (uiState.isDeleting) "Deleting..." else "Delete",
                    color =
                        if (uiState.deleteConfirmationText == "DELETE") {
                            PhoneDownDesign.colors.danger
                        } else {
                            PhoneDownDesign.colors.textTertiary
                        },
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !uiState.isDeleting) {
                Text("Cancel")
            }
        },
    )
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
            onPrivacyPolicyClick = {},
            onDeleteRequested = {},
            onDeleteConfirmed = {},
            onDeleteDismissed = {},
            onDeleteConfirmationTextChanged = {},
            onDeleteIncludeBackupChanged = {},
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
            onPrivacyPolicyClick = {},
            onDeleteRequested = {},
            onDeleteConfirmed = {},
            onDeleteDismissed = {},
            onDeleteConfirmationTextChanged = {},
            onDeleteIncludeBackupChanged = {},
            onSoundToggled = {},
            onHapticsToggled = {},
            onThemeModeSelected = {},
        )
    }
}
