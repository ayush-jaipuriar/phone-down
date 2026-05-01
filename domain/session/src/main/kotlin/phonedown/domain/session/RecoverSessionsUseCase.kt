package phonedown.domain.session

import phonedown.core.model.FocusSession
import phonedown.core.model.repository.SessionRepository

class RecoverSessionsUseCase(
    private val sessionRepository: SessionRepository,
    private val sessionRecoveryClassifier: SessionRecoveryClassifier,
) {
    suspend operator fun invoke(): List<FocusSession> {
        val recoverableSessions = sessionRepository.getRecoverableSessions()
        return recoverableSessions.map { session ->
            val classifiedSession = sessionRecoveryClassifier.classify(session)
            if (classifiedSession != session) {
                sessionRepository.upsertSession(classifiedSession)
            }
            classifiedSession
        }
    }
}
