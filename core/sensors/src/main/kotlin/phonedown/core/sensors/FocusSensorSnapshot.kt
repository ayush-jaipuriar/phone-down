package phonedown.core.sensors

data class FocusSensorSnapshot(
    val elapsedRealtimeMillis: Long,
    val gravityX: Float,
    val gravityY: Float,
    val gravityZ: Float,
    val linearMotionMagnitude: Float,
    val tiltDegrees: Float?,
    val proximityNear: Boolean?,
    val activeSensors: Set<SensorSource>,
)
