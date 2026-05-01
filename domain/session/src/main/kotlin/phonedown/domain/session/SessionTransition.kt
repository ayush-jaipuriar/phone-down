package phonedown.domain.session

import phonedown.core.model.PenaltyEvent

data class SessionTransition(
    val runtime: SessionRuntime,
    val penaltyEvents: List<PenaltyEvent> = emptyList(),
) {
    val session = runtime.session
}
