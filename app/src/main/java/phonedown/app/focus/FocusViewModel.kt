package phonedown.app.focus

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import phonedown.app.runtime.ActiveSessionRuntimeCoordinator
import phonedown.app.runtime.ActiveSessionRuntimeState
import phonedown.core.model.SessionState
import phonedown.core.model.repository.SessionRepository
import phonedown.core.model.repository.SettingsRepository
import phonedown.core.sensors.FocusValidityReason
import phonedown.domain.insights.GetTodayInsightsUseCase
import phonedown.feature.focus.state.FocusEvent
import phonedown.feature.focus.state.FocusPresentationState
import phonedown.feature.focus.state.FocusUiState
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

@HiltViewModel
class FocusViewModel
    @Inject
    constructor(
        private val runtimeCoordinator: ActiveSessionRuntimeCoordinator,
        private val settingsRepository: SettingsRepository,
        sessionRepository: SessionRepository,
    ) : ViewModel() {
        private val localViewState = MutableStateFlow(LocalViewState())
        private var interruptionStartTime: Long? = null

        private val startOfDayMillis =
            LocalDate
                .now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        private val endOfDayMillis = startOfDayMillis + 24 * 60 * 60 * 1000L

        val uiState: StateFlow<FocusUiState> =
            combine(
                runtimeCoordinator.state,
                settingsRepository.settings,
                sessionRepository.observeSessionsInWindow(startOfDayMillis, endOfDayMillis),
                localViewState,
            ) { runtimeState, settings, todaySessions, localView ->
                val presentationState = mapToPresentationState(runtimeState, localView)

                val defaultDuration = settings.defaultDurationSeconds
                val session = runtimeState.session

                val selectedDurationSeconds = session?.plannedDurationSeconds ?: localView.temporaryDurationSeconds ?: defaultDuration
                val remainingSeconds =
                    session?.let { (it.requiredDurationSeconds - it.validFocusSeconds).coerceAtLeast(0L) } ?: selectedDurationSeconds

                // Track grace period for interruption screen
                val isPausedByPickup = session?.state == SessionState.PausedByPickup
                if (isPausedByPickup && interruptionStartTime == null) {
                    interruptionStartTime = System.currentTimeMillis()
                } else if (!isPausedByPickup) {
                    interruptionStartTime = null
                }
                val graceRemainingSeconds =
                    if (isPausedByPickup) {
                        val startMs = interruptionStartTime
                        if (startMs != null) {
                            val elapsed = (System.currentTimeMillis() - startMs) / 1000L
                            (5L - elapsed).coerceAtLeast(0L)
                        } else {
                            0L
                        }
                    } else {
                        0L
                    }

                val todaySummary = GetTodayInsightsUseCase.summarize(todaySessions)

                FocusUiState(
                    presentationState = presentationState,
                    selectedDurationSeconds = selectedDurationSeconds,
                    remainingSeconds = remainingSeconds,
                    elapsedSeconds = session?.actualElapsedSeconds ?: 0L,
                    penaltySeconds = session?.penaltySeconds ?: 0L,
                    interruptionCount = session?.interruptionCount ?: 0,
                    clean = session?.clean ?: true,
                    graceRemainingSeconds = graceRemainingSeconds,
                    todayTotalFocusSeconds = todaySummary.totalFocusSeconds,
                    todaySessionsCount = todaySummary.sessionCount,
                    todayCleanCount = todaySummary.cleanSessionCount,
                    freeCustomDurationSeconds = settings.freeCustomDurationSeconds,
                    showDurationSelector = localView.showDurationSelector,
                    showEndConfirmation = localView.showEndConfirmation,
                    showAddTime = localView.showAddTime,
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = FocusUiState(),
            )

        private fun mapToPresentationState(
            runtimeState: ActiveSessionRuntimeState,
            localView: LocalViewState,
        ): FocusPresentationState {
            if (runtimeState.latestValidity?.reason == FocusValidityReason.SensorsUnavailable) {
                return FocusPresentationState.SensorUnavailable
            }

            val session = runtimeState.session ?: return FocusPresentationState.Idle

            return when (session.state) {
                SessionState.Created -> FocusPresentationState.ReadyToFocus
                SessionState.WaitingForPhoneDown -> FocusPresentationState.WaitingForPhoneDown
                SessionState.Arming -> FocusPresentationState.Arming
                SessionState.Active -> FocusPresentationState.Active
                SessionState.PausedByPickup -> FocusPresentationState.PausedByPickup
                SessionState.PausedByCall -> FocusPresentationState.PausedByCall
                SessionState.PausedByUser -> FocusPresentationState.PausedByUser
                SessionState.Completed ->
                    if (session.clean) {
                        FocusPresentationState.CompletedClean
                    } else {
                        FocusPresentationState.CompletedInterrupted
                    }
                SessionState.EndedEarly -> FocusPresentationState.EndedEarly
                SessionState.Invalidated -> FocusPresentationState.Invalid
                SessionState.Broken -> FocusPresentationState.Broken
                SessionState.Abandoned -> FocusPresentationState.Idle
            }
        }

        fun onEvent(event: FocusEvent) {
            when (event) {
                FocusEvent.StartClicked -> Unit
                FocusEvent.EndClicked -> {
                    val state = uiState.value.presentationState
                    if (state == FocusPresentationState.WaitingForPhoneDown) {
                        viewModelScope.launch { runtimeCoordinator.endSession() }
                    } else {
                        localViewState.update { it.copy(showEndConfirmation = true) }
                    }
                }
                FocusEvent.EndConfirmed -> {
                    localViewState.update { it.copy(showEndConfirmation = false) }
                    viewModelScope.launch { runtimeCoordinator.endSession() }
                }
                FocusEvent.EndDismissed -> {
                    localViewState.update { it.copy(showEndConfirmation = false) }
                }
                FocusEvent.DurationSelectorClicked -> {
                    localViewState.update { it.copy(showDurationSelector = true) }
                }
                FocusEvent.DurationSelectorDismissed -> {
                    localViewState.update { it.copy(showDurationSelector = false) }
                }
                is FocusEvent.DurationSelected -> {
                    localViewState.update { it.copy(showDurationSelector = false, temporaryDurationSeconds = event.seconds) }
                    viewModelScope.launch {
                        settingsRepository.setDefaultDurationSeconds(event.seconds)
                    }
                }
                FocusEvent.RetrySensorsClicked -> Unit
                FocusEvent.ReadyBackClicked -> {
                    viewModelScope.launch { runtimeCoordinator.endSession() }
                }
                FocusEvent.PauseClicked -> {
                    viewModelScope.launch { runtimeCoordinator.pauseSession() }
                }
                FocusEvent.ResumeClicked -> {
                    viewModelScope.launch { runtimeCoordinator.resumeSession() }
                }
                FocusEvent.AddTimeClicked -> {
                    localViewState.update { it.copy(showAddTime = !it.showAddTime) }
                }
                is FocusEvent.AddTimeSelected -> {
                    localViewState.update { it.copy(showAddTime = false) }
                    viewModelScope.launch { runtimeCoordinator.addTime(event.minutes * 60L) }
                }
                FocusEvent.BackToHomeClicked -> {
                    viewModelScope.launch {
                        runtimeCoordinator.clearFinishedRuntime()
                    }
                }
            }
        }

        private data class LocalViewState(
            val showDurationSelector: Boolean = false,
            val showEndConfirmation: Boolean = false,
            val temporaryDurationSeconds: Long? = null,
            val showAddTime: Boolean = false,
        )
    }
