@file:Suppress("MagicNumber")

package phonedown.core.sensors

data class FocusSensorConfig(
    val stableDurationMillis: Long = 750L,
    val movementWindowMillis: Long = 2_000L,
    val faceDownZThreshold: Float = -0.72f,
    val verticalZThreshold: Float = 0.35f,
    val flatTiltThresholdDegrees: Float = 32f,
    val proximityFaceDownZThreshold: Float = -0.55f,
    val proximityFlatTiltThresholdDegrees: Float = 36f,
    val pocketTiltThresholdDegrees: Float = 40f,
    val pickupMotionThreshold: Float = 1.1f,
    val rollingMotionThreshold: Float = 0.75f,
    val pocketMotionThreshold: Float = 0.45f,
    val lowPassAlpha: Float = 0.9f,
)
