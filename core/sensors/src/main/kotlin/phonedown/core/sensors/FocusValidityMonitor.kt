package phonedown.core.sensors

import kotlinx.coroutines.flow.StateFlow

interface FocusValidityMonitor {
    val validity: StateFlow<FocusValidityResult>

    fun start()

    fun stop()
}
