package phonedown.feature.focus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class FocusScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun focusScreenShowsPrimaryTimerAndAction() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                FocusScreen()
            }
        }

        composeRule.onNodeWithTag(FocusTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(FocusTestTags.TIMER).assertIsDisplayed()
        composeRule.onNodeWithTag(FocusTestTags.START_BUTTON).assertIsDisplayed()
        composeRule.onNodeWithTag(FocusTestTags.TODAY_METRICS).assertIsDisplayed()
    }
}
