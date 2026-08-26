package phonedown.feature.settings

import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.isToggleable
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class SettingsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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
                    onAutoBackupToggled = {},
                    onPrivacyPolicyClick = {},
                    onDeleteRequested = {},
                    onDeleteConfirmed = {},
                    onDeleteDismissed = {},
                    onDeleteConfirmationTextChanged = {},
                    onDeleteIncludeBackupChanged = {},
                    onSoundToggled = { soundEnabled = it },
                    onHapticsToggled = {},
                    onThemeModeSelected = {},
                    onDefaultDurationSelected = {},
                )
            }
        }

        composeRule.onNodeWithText("Sounds").assertIsDisplayed()
        val switch =
            composeRule.onAllNodes(
                isToggleable() and hasAnyAncestor(hasTestTag(SettingsTestTags.SOUND_SWITCH)),
            )[0]
        switch.assertIsOn()
        switch.performClick()

        assertEquals(false, soundEnabled)
    }

    @Test
    fun hapticsToggleReflectsStateAndEmitsChanges() {
        var hapticsEnabled = true

        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(hapticsEnabled = hapticsEnabled),
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
                    onHapticsToggled = { hapticsEnabled = it },
                    onThemeModeSelected = {},
                    onDefaultDurationSelected = {},
                )
            }
        }

        composeRule.onNodeWithText("Haptics").assertIsDisplayed()
        val switch =
            composeRule.onAllNodes(
                isToggleable() and hasAnyAncestor(hasTestTag(SettingsTestTags.HAPTICS_SWITCH)),
            )[0]
        switch.assertIsOn()
        switch.performClick()

        assertEquals(false, hapticsEnabled)
    }

    @Test
    fun deleteDataDialogShowsAndDismisses() {
        var showDeleteConfirmation by mutableStateOf(false)

        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(showDeleteConfirmation = showDeleteConfirmation),
                    onAccountClick = {},
                    onProClick = {},
                    onBackupClick = {},
                    onAutoBackupToggled = {},
                    onPrivacyPolicyClick = {},
                    onDeleteRequested = { showDeleteConfirmation = true },
                    onDeleteConfirmed = {},
                    onDeleteDismissed = { showDeleteConfirmation = false },
                    onDeleteConfirmationTextChanged = {},
                    onDeleteIncludeBackupChanged = {},
                    onSoundToggled = {},
                    onHapticsToggled = {},
                    onThemeModeSelected = {},
                    onDefaultDurationSelected = {},
                )
            }
        }

        composeRule.onNodeWithTag(SettingsTestTags.DELETE_ROW).performScrollTo()
        composeRule.onNodeWithTag(SettingsTestTags.DELETE_ROW).performClick()
        composeRule.onNodeWithText("This will permanently delete:").assertIsDisplayed()
        composeRule.onNodeWithText("Cancel").performClick()
        composeRule.onNodeWithText("This will permanently delete:").assertDoesNotExist()
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

        composeRule.onNodeWithText("Default Duration").assertIsDisplayed()
        composeRule.onNodeWithText("30 min").assertIsDisplayed()
    }

    @Test
    fun defaultDurationRowOpensPickerAndEmitsSelection() {
        var selectedDurationSeconds: Long? = null
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                SettingsScreen(
                    uiState = SettingsUiState(defaultDurationSeconds = 25 * 60L),
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
                    onDefaultDurationSelected = { selectedDurationSeconds = it },
                )
            }
        }

        composeRule.onNodeWithText("Default Duration").performClick()
        composeRule.onNodeWithText("45 minutes").performClick()

        assertEquals(45 * 60L, selectedDurationSeconds)
    }
}
