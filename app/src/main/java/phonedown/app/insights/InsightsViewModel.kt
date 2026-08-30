package phonedown.app.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import phonedown.core.model.ProEntitlement
import phonedown.core.model.repository.BillingRepository
import phonedown.domain.insights.GetAdvancedInsightsUseCase
import phonedown.domain.insights.GetBestHourUseCase
import phonedown.domain.insights.GetBestWeekdayUseCase
import phonedown.domain.insights.GetDayInsightsUseCase
import phonedown.domain.insights.GetFocusQualityUseCase
import phonedown.domain.insights.GetHeatmapDataUseCase
import phonedown.domain.insights.GetHistoryUseCase
import phonedown.domain.insights.GetHourlyFocusUseCase
import phonedown.domain.insights.GetStreakUseCase
import phonedown.domain.insights.GetTodayInsightsUseCase
import phonedown.domain.insights.GetTrendsUseCase
import phonedown.domain.insights.GetWeeklyInsightsUseCase
import phonedown.domain.insights.SessionHistoryItem
import phonedown.feature.insights.InsightsUiState
import javax.inject.Inject

@HiltViewModel
class InsightsViewModel
    @Inject
    constructor(
        private val getTodayInsights: GetTodayInsightsUseCase,
        private val getWeeklyInsights: GetWeeklyInsightsUseCase,
        private val getFocusQuality: GetFocusQualityUseCase,
        private val getStreak: GetStreakUseCase,
        private val getHistory: GetHistoryUseCase,
        private val getHeatmapData: GetHeatmapDataUseCase,
        private val getBestHour: GetBestHourUseCase,
        private val getBestDay: GetBestWeekdayUseCase,
        private val getTrends: GetTrendsUseCase,
        private val getAdvancedInsights: GetAdvancedInsightsUseCase,
        private val getHourlyFocus: GetHourlyFocusUseCase,
        private val getDayInsights: GetDayInsightsUseCase,
        billingRepository: BillingRepository,
    ) : ViewModel() {
        private val _uiState = MutableStateFlow(InsightsUiState())
        val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

        private val entitlementFlow = billingRepository.entitlement

        private var daySelectionJob: Job? = null

        init {
            refresh()
        }

        fun onDaySelected(epochDay: Long) {
            daySelectionJob?.cancel()
            _uiState.update { it.copy(selectedDateEpochDay = epochDay) }
            daySelectionJob =
                viewModelScope.launch {
                    val summary = getDayInsights(epochDay)
                    val selectedDate = java.time.LocalDate.ofEpochDay(epochDay)
                    val hourly = getHourlyFocus.invoke(selectedDate)
                    _uiState.update { current ->
                        if (current.selectedDateEpochDay == epochDay) {
                            current.copy(
                                selectedDaySummary = summary,
                                hourlyFocus = hourly,
                            )
                        } else {
                            current
                        }
                    }
                }
        }

        fun onBackToToday() {
            daySelectionJob?.cancel()
            daySelectionJob =
                viewModelScope.launch {
                    val hourly = getHourlyFocus()
                    _uiState.update { current ->
                        current.copy(
                            selectedDateEpochDay = null,
                            selectedDaySummary = null,
                            hourlyFocus = hourly,
                        )
                    }
                }
        }

        fun refresh() {
            viewModelScope.launch {
                _uiState.update { it.copy(isLoading = true) }

                val today = getTodayInsights()
                val weekly = getWeeklyInsights()
                val focusQuality = getFocusQuality()
                val streak = getStreak()
                val history = getHistory()
                val heatmap = getHeatmapData()
                val bestHour = getBestHour()
                val bestDay = getBestDay()
                val trends = getTrends()
                val advanced = getAdvancedInsights()
                val entitlement = entitlementFlow.first()

                val selectedDay = _uiState.value.selectedDateEpochDay
                val selectedSummary = if (selectedDay != null) getDayInsights(selectedDay) else null
                val hourly =
                    if (selectedDay != null) {
                        getHourlyFocus(java.time.LocalDate.ofEpochDay(selectedDay))
                    } else {
                        getHourlyFocus()
                    }

                val isEmpty =
                    today.sessionCount == 0 &&
                        weekly.totalFocusSeconds == 0L &&
                        focusQuality == null

                _uiState.update { current ->
                    current.copy(
                        today = today,
                        weekly = weekly,
                        focusQuality = focusQuality,
                        streak = streak,
                        history = history,
                        heatmap = heatmap,
                        bestHour = bestHour,
                        bestDay = bestDay,
                        completionRateTrend = trends.completionRate,
                        cleanRatioTrend = trends.cleanRatio,
                        interruptionTrend = trends.interruptions,
                        focusQualityTrend = trends.focusQuality,
                        advanced = advanced,
                        isEmpty = isEmpty,
                        isLoading = false,
                        isProUser = entitlement is ProEntitlement.Pro,
                        hourlyFocus = hourly,
                        selectedDateEpochDay = selectedDay,
                        selectedDaySummary = selectedSummary,
                    )
                }
            }
        }

        fun prepareFocusHistoryExport(onReady: (List<SessionHistoryItem>) -> Unit) {
            viewModelScope.launch {
                onReady(getHistory(pageSize = Int.MAX_VALUE))
            }
        }
    }
