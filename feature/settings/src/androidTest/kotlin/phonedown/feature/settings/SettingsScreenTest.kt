package phonedown.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class SettingsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun settingsScreenShowsThemeAndNavigableRows() {
        var accountClicks = 0
        var proClicks = 0

        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(),
                    onAccountClick = { accountClicks += 1 },
                    onProClick = { proClicks += 1 },
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

        composeRule.onNodeWithTag(SettingsTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsTestTags.THEME_CONTROL).assertIsDisplayed()
        composeRule.onNodeWithTag(SettingsTestTags.ACCOUNT_ROW).performClick()
        composeRule.onNodeWithTag(SettingsTestTags.PRO_ROW).performClick()

        assertEquals(1, accountClicks)
        assertEquals(1, proClicks)
    }

    @Test
    fun soundToggleReflectsStateAndEmitsChanges() {
        var soundEnabled = true

        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(soundEnabled = soundEnabled),
                    onAccountClick = {},
                    onProClick = {},
                    onBackupClick = {},
                    onPrivacyPolicyClick = {},
                    onDeleteRequested = {},
                    onDeleteConfirmed = {},
                    onDeleteDismissed = {},
                    onDeleteConfirmationTextChanged = {},
                    onDeleteIncludeBackupChanged = {},
                    onSoundToggled = { soundEnabled = it },
                    onHapticsToggled = {},
                    onThemeModeSelected = {},
                )
            }
        }

        composeRule.onNodeWithText("Sounds").assertIsDisplayed()
        val switch = composeRule.onNodeWithTag(SettingsTestTags.SOUND_SWITCH)
        switch.assertIsOn()
        switch.performClick()

        assertEquals(false, soundEnabled)
    }

    @Test
    fun hapticsToggleReflectsStateAndEmitsChanges() {
        var hapticsEnabled = false

        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(hapticsEnabled = hapticsEnabled),
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
                    onHapticsToggled = { hapticsEnabled = it },
                    onThemeModeSelected = {},
                )
            }
        }

        composeRule.onNodeWithText("Haptics").assertIsDisplayed()
        val switch = composeRule.onNodeWithTag(SettingsTestTags.HAPTICS_SWITCH)
        switch.assertIsOff()
        switch.performClick()

        assertEquals(true, hapticsEnabled)
    }

    @Test
    fun deleteDataDialogShowsAndDismisses() {
        composeRule.setContent {
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

        composeRule.onNodeWithText("Delete All Local Data").performClick()
        composeRule.onNodeWithText("This will permanently delete all your session history, settings, and preferences. This action cannot be undone.").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("This will permanently delete all your session history, settings, and preferences. This action cannot be undone.").assertDoesNotExist()
    }

    @Test
    fun timerSectionDisplaysDefaultDuration() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(defaultDurationSeconds = 1800),
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

        composeRule.onNodeWithText("Default Duration").assertIsDisplayed()
        composeRule.onNodeWithText("30 min").assertIsDisplayed()
    }
}
