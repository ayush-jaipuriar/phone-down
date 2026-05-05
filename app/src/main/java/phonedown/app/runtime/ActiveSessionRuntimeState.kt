package phonedown.app.runtime

import phonedown.core.model.FocusSession
import phonedown.core.sensors.FocusValidityResult

data class ActiveSessionRuntimeState(
    val session: FocusSession? = null,
    val latestValidity: FocusValidityResult? = null,
    val shouldDimScreen: Boolean = false,
    val shouldKeepScreenAwake: Boolean = false,
    val shouldStopService: Boolean = false,
    val notificationTitle: String = "Phone Down",
    val notificationBody: String = "Waiting for phone down",
)
