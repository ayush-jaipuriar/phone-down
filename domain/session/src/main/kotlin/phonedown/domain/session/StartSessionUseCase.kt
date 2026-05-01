package phonedown.domain.session

import phonedown.core.model.repository.SessionRepository

class StartSessionUseCase(
    private val sessionEngine: SessionEngine,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(plannedDurationSeconds: Long): SessionRuntime {
        val runtime = sessionEngine.startSession(plannedDurationSeconds)
        sessionRepository.upsertSession(runtime.session)
        return runtime
    }
}
