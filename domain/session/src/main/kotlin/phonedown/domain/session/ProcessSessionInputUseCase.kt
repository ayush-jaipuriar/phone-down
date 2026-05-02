package phonedown.domain.session

import phonedown.core.model.repository.SessionRepository

class ProcessSessionInputUseCase(
    private val sessionEngine: SessionEngine,
    private val sessionRepository: SessionRepository,
) {
    suspend operator fun invoke(
        runtime: SessionRuntime,
        input: SessionInput,
    ): SessionTransition {
        val transition = sessionEngine.processInput(runtime, input)
        persistTransition(transition)
        return transition
    }

    private suspend fun persistTransition(transition: SessionTransition) {
        val events = transition.penaltyEvents
        when (events.size) {
            0 -> sessionRepository.upsertSession(transition.session)
            1 ->
                sessionRepository.upsertSessionWithPenaltyEvent(
                    transition.session,
                    events.first(),
                )
            else -> {
                sessionRepository.upsertSessionWithPenaltyEvent(
                    transition.session,
                    events.first(),
                )
                for (event in events.drop(1)) {
                    sessionRepository.recordPenaltyEvent(event)
                }
            }
        }
    }
}
