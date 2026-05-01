package phonedown.feature.settings

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
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
                    onAccountClick = { accountClicks += 1 },
                    onProClick = { proClicks += 1 },
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
}
