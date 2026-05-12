package phonedown.feature.insights

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performScrollTo
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.SessionResult
import phonedown.core.model.ThemeMode
import phonedown.domain.insights.DayInsight
import phonedown.domain.insights.FocusQualityLabel
import phonedown.domain.insights.FocusQualityResult
import phonedown.domain.insights.InsightSummary
import phonedown.domain.insights.SessionHistoryItem
import phonedown.domain.insights.StreakResult
import phonedown.domain.insights.WeeklyInsight
import java.time.LocalDate

class InsightsScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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
            weekly =
                WeeklyInsight(
                    days =
                        (0L..6L).map { offset ->
                            DayInsight(
                                dateEpochDay = LocalDate.now().minusDays(6L - offset).toEpochDay(),
                                focusSeconds = (offset + 1L) * 600L,
                                sessionCount = 1,
                                cleanSessionCount = 1,
                            )
                        },
                    totalFocusSeconds = 16_800,
                    changePercent = 12f,
                ),
            history =
                listOf(
                    SessionHistoryItem(
                        sessionId = "session-1",
                        startedAtEpochMillis = 1_700_000_000_000L,
                        plannedDurationSeconds = 1500L,
                        validFocusSeconds = 1200L,
                        result = SessionResult.CleanCompleted,
                        clean = true,
                        broken = false,
                    ),
                ),
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
        composeRule.onNodeWithTag(InsightsTestTags.QUALITY_CARD).performScrollTo()
        composeRule.onNodeWithTag(InsightsTestTags.QUALITY_CARD).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.WEEKLY_CHART).performScrollTo()
        composeRule.onNodeWithTag(InsightsTestTags.WEEKLY_CHART).assertIsDisplayed()
        composeRule.onNodeWithTag(InsightsTestTags.SESSION_SUMMARY).performScrollTo()
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
