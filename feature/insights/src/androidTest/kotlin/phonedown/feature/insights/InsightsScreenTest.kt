package phonedown.feature.insights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode

class InsightsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun insightsScreenShowsAnalyticsSections() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsScreen()
            }
        }

        composeRule.onNodeWithTag(InsightsTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.QUALITY_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.WEEKLY_CHART).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.SESSION_SUMMARY).assertIsDisplayed()
    }
}
