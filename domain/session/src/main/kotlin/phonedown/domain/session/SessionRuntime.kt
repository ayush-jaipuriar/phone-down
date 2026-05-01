package phonedown.domain.session

import phonedown.core.model.FocusSession

data class SessionRuntime(
    val session: FocusSession,
    val phoneIsValid: Boolean = false,
    val armingStartedAtElapsedMillis: Long? = null,
    val activeStartedAtElapsedMillis: Long? = null,
    val activeBaseFocusSeconds: Long = 0L,
    val interruptionStartedAtElapsedMillis: Long? = null,
    val penaltyAppliedForCurrentInterruption: Boolean = false,
    val longInterruptionRecorded: Boolean = false,
    val callStartedAtElapsedMillis: Long? = null,
)
