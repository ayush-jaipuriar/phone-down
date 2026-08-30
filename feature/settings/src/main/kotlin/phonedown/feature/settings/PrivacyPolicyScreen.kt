@file:Suppress("FunctionName", "MaxLineLength")

package phonedown.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName")
fun PrivacyPolicyScreen(onBack: () -> Unit) {
    val scrollState = rememberScrollState()

    PhoneDownScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        PhoneDownTopBar(
            title = "Privacy Policy",
            trailing = {
                Text(
                    text = "Back",
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onBack),
                )
            },
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
        ) {
            PolicySection(
                title = "Introduction",
                content =
                    "Phone Down is a focus and productivity application designed to help you stay present. " +
                        "We take your privacy seriously and are committed to protecting your personal information.",
            )

            PolicySection(
                title = "Data We Collect",
                content =
                    "We collect focus session data (duration, start/end times, interruptions) and app settings. " +
                        "If you sign in with Google, we access your display name and email. " +
                        "Release builds may send crash logs and basic diagnostics to Firebase Crashlytics. " +
                        "We do not collect location, contacts, call content, phone numbers, or advertising identifiers.",
            )

            PolicySection(
                title = "How We Use Your Data",
                content =
                    "Session data is used to display your focus history and insights. " +
                        "Settings are used to personalize your experience. If you enable backup, " +
                        "data is stored in your personal Google Drive app data folder. Crash diagnostics are used only " +
                        "to find and fix stability problems. We do not sell your data or use it for advertising.",
            )

            PolicySection(
                title = "Data Storage and Security",
                content =
                    "Phone Down operates primarily offline. All data is stored locally on your device. " +
                        "Optional cloud backup uses your personal Google Drive app data folder. " +
                        "Google account profile details are stored in app preferences. Google Drive access tokens " +
                        "are kept in memory and cleared when you sign out. Network traffic uses HTTPS.",
            )

            PolicySection(
                title = "Optional Permissions",
                content =
                    "Notification permission lets Phone Down show an active focus-session notification. " +
                        "Optional phone-state permission lets the app pause focus automatically during a phone call. " +
                        "Phone Down does not read or store the caller's number, call content, or call history.",
            )

            PolicySection(
                title = "Your Rights",
                content =
                    "You can delete all local data at any time through Settings > Privacy. " +
                        "You can export your focus history. When deleting local data, you can also choose to delete " +
                        "your cloud backup. You can sign out and revoke access through your Google Account settings.",
            )

            PolicySection(
                title = "Children's Privacy",
                content = "Phone Down is not intended for children under 13. We do not knowingly collect data from children under 13.",
            )

            PolicySection(
                title = "Third-Party Services",
                content =
                    "We use Google Sign-In for optional account access, Google Drive for optional backup and restore, " +
                        "and Firebase Crashlytics for release crash diagnostics. These services are governed by " +
                        "Google's Privacy Policy.",
            )

            PolicySection(
                title = "Policy Changes",
                content =
                    "We may update this Privacy Policy from time to time. " +
                        "Changes will be posted in the app and updated in this document.",
            )

            PolicySection(
                title = "Contact Us",
                content =
                    "If you have questions about this Privacy Policy or your data, " +
                        "contact us at support@phonedown.app or through Settings > About > Support.",
            )

            Text(
                text = "Last Updated: August 30, 2026",
                style = MaterialTheme.typography.labelSmall,
                color = PhoneDownDesign.colors.textTertiary,
            )
        }
    }
}

@Composable
private fun PolicySection(
    title: String,
    content: String,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = PhoneDownDesign.colors.textPrimary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = PhoneDownDesign.colors.textSecondary,
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun PrivacyPolicyScreenPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        PrivacyPolicyScreen(onBack = {})
    }
}
