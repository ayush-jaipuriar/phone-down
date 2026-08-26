package phonedown.feature.insights

import app.cash.paparazzi.DeviceConfig
import app.cash.paparazzi.Paparazzi
import org.junit.Rule
import org.junit.Test
import phonedown.core.designsystem.PhoneDownTheme
import phonedown.core.model.ThemeMode
import phonedown.domain.insights.FocusQualityLabel
import phonedown.domain.insights.FocusQualityResult
import phonedown.domain.insights.InsightSummary
import phonedown.domain.insights.StreakResult

class InsightsScreenScreenshotTest {
    @get:Rule
    val paparazzi = Paparazzi(deviceConfig = DeviceConfig.PIXEL_5)

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

    private val emptyState = InsightsUiState(isEmpty = true, isLoading = false)

    private val loadingState = InsightsUiState(isLoading = true)

    private val testReferenceDate = java.time.LocalDate.of(2026, 5, 12)

    @Test
    fun insightsContentLight() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsContent(uiState = sampleState, onRefresh = {}, referenceDate = testReferenceDate)
            }
        }
    }

    @Test
    fun insightsContentDark() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Dark) {
                InsightsContent(uiState = sampleState, onRefresh = {}, referenceDate = testReferenceDate)
            }
        }
    }

    @Test
    fun insightsContentEmptyLight() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsContent(uiState = emptyState, onRefresh = {}, referenceDate = testReferenceDate)
            }
        }
    }

    @Test
    fun insightsContentLoadingLight() {
        paparazzi.snapshot {
            PhoneDownTheme(themeMode = ThemeMode.Light) {
                InsightsContent(uiState = loadingState, onRefresh = {}, referenceDate = testReferenceDate)
            }
        }
    }
}
