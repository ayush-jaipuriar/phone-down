package phonedown.domain.session

sealed interface SessionInput {
    data object PhoneBecameValid : SessionInput

    data object PhoneBecameInvalid : SessionInput

    data object Tick : SessionInput

    data object CallStarted : SessionInput

    data object CallEnded : SessionInput

    data object ManualPauseRequested : SessionInput

    data object ManualResumeRequested : SessionInput

    data class AddTimeRequested(
        val additionalSeconds: Long,
    ) : SessionInput

    data object ManualEndRequested : SessionInput
}
