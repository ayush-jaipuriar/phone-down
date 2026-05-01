package phonedown.domain.session

class EndSessionUseCase(
    private val processSessionInputUseCase: ProcessSessionInputUseCase,
) {
    suspend operator fun invoke(runtime: SessionRuntime): SessionTransition {
        return processSessionInputUseCase(runtime, SessionInput.ManualEndRequested)
    }
}
