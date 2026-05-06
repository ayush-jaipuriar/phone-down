package phonedown.feature.focus.state

sealed interface FocusEvent {
    data object StartClicked : FocusEvent

    data object EndClicked : FocusEvent

    data object EndConfirmed : FocusEvent

    data object EndDismissed : FocusEvent

    data object DurationSelectorClicked : FocusEvent

    data object DurationSelectorDismissed : FocusEvent

    data class DurationSelected(
        val seconds: Long,
    ) : FocusEvent

    data object RetrySensorsClicked : FocusEvent

    data object ReadyBackClicked : FocusEvent

    data object PauseClicked : FocusEvent

    data object ResumeClicked : FocusEvent

    data object AddTimeClicked : FocusEvent

    data class AddTimeSelected(
        val minutes: Int,
    ) : FocusEvent

    data object BackToHomeClicked : FocusEvent
}
