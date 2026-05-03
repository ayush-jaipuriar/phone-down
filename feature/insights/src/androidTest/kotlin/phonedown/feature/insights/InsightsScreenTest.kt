package phonedown.feature.insights

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.v2.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.domain.insights.FocusQualityLabel
import phonedown.domain.insights.FocusQualityResult
import phonedown.domain.insights.InsightSummary
import phonedown.domain.insights.StreakResult

class InsightsScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val sampleState =
        InsightsUiState(
            today =
                InsightSummary(
                    totalFocusSeconds = 4800,
                    sessionCount = 3,
                    cleanSessionCount = 2,
                ),
            focusQuality = FocusQualityResult(78, FocusQualityLabel.Focused, 0.8f, 0.6f, 0.5f, 0.9f),
            streak = StreakResult(5, 12),
            isEmpty = false,
            isLoading = false,
        )

    @Test
    fun insightsContentShowsAnalyticsSections() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsContent(uiState = sampleState, onRefresh = {})
            }
        }

        composeRule.onNodeWithTag(InsightsTestTags.SCREEN).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.QUALITY_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.WEEKLY_CHART).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.SESSION_SUMMARY).assertIsDisplayed()
    }

    @Test
    fun insightsContentShowsEmptyState() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsContent(
                    uiState = InsightsUiState(isEmpty = true, isLoading = false),
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithTag(InsightsTestTags.SCREEN).assertIsDisplayed()
    }

    @Test
    fun insightsContentShowsLoadingState() {
        composeRule.setContent {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsContent(
                    uiState = InsightsUiState(isLoading = true),
                    onRefresh = {},
                )
            }
        }

        composeRule.onNodeWithTag(InsightsTestTags.SCREEN).assertIsDisplayed()
    }
}
