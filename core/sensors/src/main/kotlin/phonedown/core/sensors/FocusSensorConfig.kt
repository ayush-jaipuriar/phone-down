package phonedown.core.sensors

data class FocusSensorConfig(
    val stableDurationMillis: Long = 3_000L,
    val movementWindowMillis: Long = 2_000L,
    val faceDownZThreshold: Float = -0.82f,
    val verticalZThreshold: Float = 0.35f,
    val flatTiltThresholdDegrees: Float = 25f,
    val pocketTiltThresholdDegrees: Float = 40f,
    val pickupMotionThreshold: Float = 1.1f,
    val rollingMotionThreshold: Float = 0.75f,
    val pocketMotionThreshold: Float = 0.45f,
    val lowPassAlpha: Float = 0.9f,
)
