package phonedown.app.insights

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import phonedown.domain.insights.InsightSummary
import phonedown.domain.insights.SessionHistoryItem

class InsightsEmptyStateTest {
    @Test
    fun `older history prevents empty state even when current summaries are empty`() {
        val history =
            listOf(
                SessionHistoryItem(
                    sessionId = "older-session",
                    startedAtEpochMillis = 0L,
                    plannedDurationSeconds = 1_500L,
                    validFocusSeconds = 1_200L,
                    result = null,
                    clean = false,
                    broken = false,
                ),
            )

        assertFalse(
            isInsightsEmpty(
                today = InsightSummary(),
                weeklyFocusSeconds = 0L,
                hasFocusQuality = false,
                history = history,
            ),
        )
    }

    @Test
    fun `no summaries or history is empty`() {
        assertTrue(
            isInsightsEmpty(
                today = InsightSummary(),
                weeklyFocusSeconds = 0L,
                hasFocusQuality = false,
                history = emptyList(),
            ),
        )
    }
}
