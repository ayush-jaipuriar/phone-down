package phonedown.feature.account

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import phonedown.core.designsystem.PhoneDownButton
import phonedown.core.designsystem.PhoneDownCard
import phonedown.core.designsystem.PhoneDownDesign
import phonedown.core.designsystem.PhoneDownScreen
import phonedown.core.designsystem.PhoneDownSpacing
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.designsystem.PhoneDownTopBar
import phonedown.core.model.AccountState
import phonedown.core.model.ThemeMode

@Composable
@Suppress("FunctionName")
fun AccountScreen(
    accountState: AccountState,
    isProUser: Boolean,
    isSigningIn: Boolean,
    signInError: String?,
    isRestoring: Boolean,
    restoreError: String?,
    noBackupFoundMessage: String?,
    restoreSuccess: String?,
    onSignIn: () -> Unit,
    onSignOut: () -> Unit,
    onRestoreClick: () -> Unit,
    onClearRestoreState: () -> Unit,
    onClearSignInError: () -> Unit,
    onBack: () -> Unit,
) {
    var showRestoreDialog by remember { mutableStateOf(false) }

    PhoneDownScreen(
        modifier = Modifier.fillMaxSize(),
    ) {
        PhoneDownTopBar(
            title = "Account",
            trailing = {
                Text(
                    text = "Back",
                    color = PhoneDownDesign.colors.textSecondary,
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.clickable(onClick = onBack),
                )
            },
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.lg))

        when (accountState) {
            is AccountState.SignedOut -> {
                SignedOutContent(
                    isSigningIn = isSigningIn,
                    onSignIn = onSignIn,
                )
            }
            is AccountState.SignedIn -> {
                SignedInContent(
                    accountState = accountState,
                    isProUser = isProUser,
                    isRestoring = isRestoring,
                    onRestoreClick = { showRestoreDialog = true },
                    onSignOut = onSignOut,
                )
            }
        }

        if (showRestoreDialog) {
            AlertDialog(
                onDismissRequest = { showRestoreDialog = false },
                title = { Text("Restore from Backup") },
                text = { Text("This will replace all local data with your latest backup. This action cannot be undone.") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showRestoreDialog = false
                            onRestoreClick()
                        },
                    ) {
                        Text("Restore", color = PhoneDownDesign.colors.danger)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showRestoreDialog = false }) {
                        Text("Cancel")
                    }
                },
            )
        }

        if (restoreError != null) {
            AlertDialog(
                onDismissRequest = onClearRestoreState,
                title = { Text("Restore Failed") },
                text = { Text(restoreError) },
                confirmButton = {
                    TextButton(onClick = onClearRestoreState) {
                        Text("OK")
                    }
                },
            )
        }

        if (noBackupFoundMessage != null) {
            AlertDialog(
                onDismissRequest = onClearRestoreState,
                title = { Text("No Backup Found") },
                text = { Text(noBackupFoundMessage) },
                confirmButton = {
                    TextButton(onClick = onClearRestoreState) {
                        Text("OK")
                    }
                },
            )
        }

        if (signInError != null) {
            AlertDialog(
                onDismissRequest = onClearSignInError,
                title = { Text("Sign-In Needs Attention") },
                text = { Text(signInError) },
                confirmButton = {
                    TextButton(onClick = onClearSignInError) {
                        Text("OK")
                    }
                },
            )
        }

        if (restoreSuccess != null) {
            AlertDialog(
                onDismissRequest = onClearRestoreState,
                title = { Text("Restore Complete") },
                text = { Text(restoreSuccess) },
                confirmButton = {
                    TextButton(onClick = onClearRestoreState) {
                        Text("OK")
                    }
                },
            )
        }
    }
}

@Composable
private fun SignedOutContent(
    isSigningIn: Boolean,
    onSignIn: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.lg),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "Sign in to enable cloud backup and sync your focus data across devices.",
            style = MaterialTheme.typography.bodyLarge,
            color = PhoneDownDesign.colors.textSecondary,
            textAlign = TextAlign.Center,
        )

        Text(
            text = "Your data stays private. We only use your Google account for backup purposes.",
            style = MaterialTheme.typography.bodyMedium,
            color = PhoneDownDesign.colors.textTertiary,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        PhoneDownButton(
            text = if (isSigningIn) "Signing in..." else "Sign in with Google",
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isSigningIn,
        )

        if (isSigningIn) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PhoneDownDesign.colors.progress,
                strokeWidth = 2.dp,
            )
        }
    }
}

@Composable
private fun SignedInContent(
    accountState: AccountState.SignedIn,
    isProUser: Boolean,
    isRestoring: Boolean,
    onRestoreClick: () -> Unit,
    onSignOut: () -> Unit,
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.md),
    ) {
        PhoneDownCard {
            Column(
                verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs),
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(MaterialTheme.shapes.medium)
                        .background(PhoneDownDesign.colors.surface),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = accountState.displayName?.firstOrNull()?.toString() ?: "?",
                        style = MaterialTheme.typography.headlineMedium,
                        color = PhoneDownDesign.colors.textPrimary,
                    )
                }

                Text(
                    text = accountState.displayName ?: "Unknown User",
                    style = MaterialTheme.typography.titleMedium,
                    color = PhoneDownDesign.colors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                )

                accountState.email?.let { email ->
                    Text(
                        text = email,
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                    )
                }
            }
        }

        if (isProUser) {
            PhoneDownCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.xs),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Phone Down Pro",
                        style = MaterialTheme.typography.titleSmall,
                        color = PhoneDownDesign.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "You have unlimited access to advanced insights, backup, and export features.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                    )
                }
            }

            PhoneDownCard {
                Column(
                    verticalArrangement = Arrangement.spacedBy(PhoneDownSpacing.sm),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Text(
                        text = "Backup & Restore",
                        style = MaterialTheme.typography.titleSmall,
                        color = PhoneDownDesign.colors.textPrimary,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = "Restore your focus data from a previous backup.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = PhoneDownDesign.colors.textSecondary,
                    )
                    if (isRestoring) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = PhoneDownDesign.colors.progress,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        PhoneDownButton(
                            text = "Restore from Backup",
                            onClick = onRestoreClick,
                            quiet = true,
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(PhoneDownSpacing.md))

        PhoneDownButton(
            text = "Sign Out",
            onClick = onSignOut,
            modifier = Modifier.fillMaxWidth(),
            quiet = true,
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun AccountScreenSignedOutPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        AccountScreen(
            accountState = AccountState.SignedOut,
            isProUser = false,
            isSigningIn = false,
            signInError = null,
            isRestoring = false,
            restoreError = null,
            noBackupFoundMessage = null,
            restoreSuccess = null,
            onSignIn = {},
            onSignOut = {},
            onRestoreClick = {},
            onClearRestoreState = {},
            onClearSignInError = {},
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
@Suppress("FunctionName", "UnusedPrivateMember")
private fun AccountScreenSignedInPreview() {
    PhoneDownTheme(themeMode = ThemeMode.Light) {
        AccountScreen(
            accountState = AccountState.SignedIn(
                displayName = "Test User",
                email = "test@example.com",
                photoUrl = null,
            ),
            isProUser = true,
            isSigningIn = false,
            signInError = null,
            isRestoring = false,
            restoreError = null,
            noBackupFoundMessage = null,
            restoreSuccess = null,
            onSignIn = {},
            onSignOut = {},
            onRestoreClick = {},
            onClearRestoreState = {},
            onClearSignInError = {},
            onBack = {},
        )
    }
}
