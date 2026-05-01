package phonedown.domain.session

sealed interface SessionInput {
    data object PhoneBecameValid : SessionInput

    data object PhoneBecameInvalid : SessionInput

    data object Tick : SessionInput

    data object CallStarted : SessionInput

    data object CallEnded : SessionInput

    data object ManualEndRequested : SessionInput
}
