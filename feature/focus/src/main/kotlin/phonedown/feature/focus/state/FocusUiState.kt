@file:Suppress("MagicNumber")

package phonedown.feature.focus.state

data class FocusUiState(
    val presentationState: FocusPresentationState = FocusPresentationState.Idle,
    val selectedDurationSeconds: Long = 25 * 60,
    val remainingSeconds: Long = 25 * 60,
    val focusedSeconds: Long = 0,
    val elapsedSeconds: Long = 0,
    val penaltySeconds: Long = 0,
    val interruptionCount: Int = 0,
    val clean: Boolean = true,
    val graceRemainingSeconds: Long = 0,
    // Today summary
    val todayTotalFocusSeconds: Long = 0,
    val todaySessionsCount: Int = 0,
    val todayCleanCount: Int = 0,
    val freeCustomDurationSeconds: Long? = null,
    val showDurationSelector: Boolean = false,
    val showEndConfirmation: Boolean = false,
    val showAddTime: Boolean = false,
)

enum class FocusPresentationState {
    Idle,
    ReadyToFocus,
    WaitingForPhoneDown,
    Arming,
    Active,
    PausedByPickup,
    PausedByUser,
    PausedByCall,
    CleanStatusLost,
    CompletedClean,
    CompletedInterrupted,
    EndedEarly,
    Broken,
    Invalid,
    SensorUnavailable,
}
