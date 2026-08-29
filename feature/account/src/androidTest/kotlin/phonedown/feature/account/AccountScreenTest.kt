package phonedown.feature.account

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.AccountState
import phonedown.core.model.ThemeMode

class AccountScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun signedInAccountShowsIncludedBackupWithoutPurchaseState() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                AccountScreen(
                    accountState = AccountState.SignedIn(displayName = null, email = null, photoUrl = null),
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

        composeRule.onNodeWithText("Backup & Restore").assertIsDisplayed()
        composeRule.onNodeWithText("Restore from Backup").assertIsDisplayed()
        composeRule.onAllNodesWithText("Free plan").assertCountEquals(0)
        composeRule.onAllNodesWithText("Google Play", substring = true).assertCountEquals(0)
    }

    @Test
    fun signedOutAccountExplainsHowToEnableBackup() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                AccountScreen(
                    accountState = AccountState.SignedOut,
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

        composeRule.onNodeWithText("Sign in to enable cloud backup and sync your focus data across devices.").assertIsDisplayed()
        composeRule.onNodeWithText("Sign in with Google").assertIsDisplayed()
    }
}
