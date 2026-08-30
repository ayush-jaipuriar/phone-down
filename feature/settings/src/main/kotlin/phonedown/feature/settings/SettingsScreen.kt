@file:Suppress("LongMethod", "MagicNumber", "FunctionName")

package phonedown.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSectionHeaderTextStyle
import phonedown.core.designsystem.PhoneDownSettingRow
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
    appVersionLabel: String = "--",
    onAccountClick: () -> Unit,
    onProClick: () -> Unit,
    onBackupClick: () -> Unit,
    onAutoBackupToggled: (Boolean) -> Unit,
    onPrivacyPolicyClick: () -> Unit,
    callPausePermissionGranted: Boolean = false,
    onCallPausePermissionRequested: () -> Unit = {},
    onSupportClick: () -> Unit = {},
    onPortfolioClick: () -> Unit = {},
    onDeleteRequested: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    onDeleteDismissed: () -> Unit,
    onDeleteConfirmationTextChanged: (String) -> Unit,
    onDeleteIncludeBackupChanged: (Boolean) -> Unit,
    onSoundToggled: (Boolean) -> Unit,
    onHapticsToggled: (Boolean) -> Unit,
    onThemeModeSelected: (ThemeMode) -> Unit,
    onDefaultDurationSelected: (Long) -> Unit,
) {
    var showCallPauseEducation by remember { mutableStateOf(false) }
    var showDefaultDurationPicker by remember { mutableStateOf(false) }

    PhoneDownScreen(
        modifier =
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .testTag(SettingsTestTags.SCREEN),
        topPadding = PhoneDownSpacing.lg,
    ) {
        FocusSection(
            uiState = uiState,
            onSoundToggled = onSoundToggled,
            onHapticsToggled = onHapticsToggled,
            onThemeModeSelected = onThemeModeSelected,
            onDefaultDurationClick = { showDefaultDurationPicker = true },
            callPausePermissionGranted = callPausePermissionGranted,
            onCallPausePermissionClick = { showCallPauseEducation = true },
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        AccountSection(
            uiState = uiState,
            onAccountClick = onAccountClick,
            onProClick = onProClick,
            onBackupClick = onBackupClick,
            onAutoBackupToggled = onAutoBackupToggled,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.sm))

        AboutSection(
            appVersionLabel = appVersionLabel,
            onPrivacyPolicyClick = onPrivacyPolicyClick,
            onSupportClick = onSupportClick,
            onPortfolioClick = onPortfolioClick,
            onDeleteRequested = onDeleteRequested,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.xxl))

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

        if (showDefaultDurationPicker) {
            DefaultDurationDialog(
                currentDurationSeconds = uiState.defaultDurationSeconds,
                onSelect = { seconds ->
                    onDefaultDurationSelected(seconds)
                    showDefaultDurationPicker = false
                },
                onDismiss = { showDefaultDurationPicker = false },
            )
        }
    }
}

@Composable
private fun FocusSection(
    uiState: SettingsUiState,
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
            supportingText = "Choose any whole-minute duration when you start a focus session.",
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
                "Phone Down can pause your focus timer when a phone call starts. " +
                    "Android requires phone-state permission for this, " +
                    "but the app does not read call audio, contacts, numbers, or call contents.",
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
    onAutoBackupToggled: (Boolean) -> Unit,
) {
    SettingsSectionHeader(title = "Account") {
        PhoneDownSettingRow(
            title = "Google Account",
            supportingText =
                if (uiState.isSignedIn) {
                    "Manage your account and backup"
                } else {
                    "Sign in to enable cloud backup"
                },
            trailing = if (uiState.isSignedIn) "Manage" else "Sign In",
            showChevron = true,
            modifier = Modifier.testTag(SettingsTestTags.ACCOUNT_ROW),
            onClick = onAccountClick,
        )
        PhoneDownSettingRow(
            title = "Phone Down Pro",
            supportingText = "Advanced insights, backup, and unlimited history",
            modifier = Modifier.testTag(SettingsTestTags.PRO_ROW),
            showChevron = true,
            onClick = onProClick,
        )

        if (!uiState.isSignedIn) {
            PhoneDownSettingRow(
                title = "Backup & Restore",
                supportingText = "Sign in to Google to enable backup",
                trailing = "Sign In",
                showChevron = true,
                onClick = onAccountClick,
            )
        } else {
            PhoneDownSettingRow(
                title = "Backup & Restore",
                supportingText =
                    if (uiState.backupError != null) {
                        uiState.backupError
                    } else if (uiState.isBackingUp) {
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
            if (uiState.backupOptIn) {
                PhoneDownSwitchRow(
                    title = "Auto Backup",
                    supportingText = "Back up once daily when network is available.",
                    checked = uiState.autoBackupEnabled,
                    onCheckedChange = onAutoBackupToggled,
                )
            }
        }
    }
}

@Composable
private fun AboutSection(
    appVersionLabel: String,
    onPrivacyPolicyClick: () -> Unit,
    onSupportClick: () -> Unit,
    onPortfolioClick: () -> Unit,
    onDeleteRequested: () -> Unit,
) {
    SettingsSectionHeader(title = "About") {
        PhoneDownSettingRow(
            title = "Send Feedback",
            supportingText = "Report a bug or share a testing note",
            showChevron = true,
            onClick = onSupportClick,
        )
        PhoneDownSettingRow(
            title = "Privacy Policy",
            showChevron = true,
            onClick = onPrivacyPolicyClick,
        )
        PhoneDownSettingRow(
            title = "Made with care by Ayush Jaipuriar",
            supportingText = "View portfolio",
            showChevron = true,
            onClick = onPortfolioClick,
        )
        PhoneDownSettingRow(
            title = "Version",
            trailing = appVersionLabel,
        )
        PhoneDownSettingRow(
            title = "Delete All Data",
            supportingText = "Permanently remove all sessions and settings from this device",
            destructive = true,
            modifier = Modifier.testTag(SettingsTestTags.DELETE_ROW),
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
            style =
                PhoneDownSectionHeaderTextStyle.copy(
                    fontSize = 18.sp,
                    lineHeight = 24.sp,
                ),
        )
        PhoneDownCard {
            Column(content = { content() })
        }
    }
}

private fun formatDuration(seconds: Long): String {
    val minutes = seconds / 60
    return "$minutes min"
}

private fun formatBackupTime(epochMillis: Long): String {
    val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
    return sdf.format(Date(epochMillis))
}

@Composable
private fun DefaultDurationDialog(
    currentDurationSeconds: Long,
    onSelect: (Long) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Default Duration") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs)) {
                Text(
                    text = "Used when starting a session without a one-time duration override.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PhoneDownDesign.colors.textSecondary,
                )
                DURATION_PRESETS.forEach { minutes ->
                    val seconds = minutes * 60L
                    TextButton(
                        onClick = { onSelect(seconds) },
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Text(
                            text = if (seconds == currentDurationSeconds) "$minutes minutes - Selected" else "$minutes minutes",
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
    )
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

                if (uiState.deleteError != null) {
                    Text(
                        text = uiState.deleteError,
                        style = MaterialTheme.typography.bodySmall,
                        color = PhoneDownDesign.colors.danger,
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
            onAutoBackupToggled = {},
            onPrivacyPolicyClick = {},
            onDeleteRequested = {},
            onDeleteConfirmed = {},
            onDeleteDismissed = {},
            onDeleteConfirmationTextChanged = {},
            onDeleteIncludeBackupChanged = {},
            onSoundToggled = {},
            onHapticsToggled = {},
            onThemeModeSelected = {},
            onDefaultDurationSelected = {},
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
            onAutoBackupToggled = {},
            onPrivacyPolicyClick = {},
            onDeleteRequested = {},
            onDeleteConfirmed = {},
            onDeleteDismissed = {},
            onDeleteConfirmationTextChanged = {},
            onDeleteIncludeBackupChanged = {},
            onSoundToggled = {},
            onHapticsToggled = {},
            onThemeModeSelected = {},
            onDefaultDurationSelected = {},
        )
    }
}
