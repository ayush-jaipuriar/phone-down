package phonedown.domain.session

class EndSessionUseCase(
    private val processSessionInputUseCase: ProcessSessionInputUseCase,
) {
    suspend operator fun invoke(runtime: SessionRuntime): SessionTransition =
        processSessionInputUseCase(runtime, SessionInput.ManualEndRequested)
}
