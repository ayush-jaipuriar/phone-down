package phonedown.core.sensors

data class FocusValidityResult(
    val isValid: Boolean,
    val reason: FocusValidityReason,
    val stabilityState: FocusStabilityState,
    val orientationConfidence: Float?,
    val movementScore: Float?,
    val diagnostics: FocusSensorDiagnostics? = null,
)

enum class FocusValidityReason {
    FaceDownStable,
    FaceDownStabilizing,
    FaceUp,
    Vertical,
    Moving,
    PocketLike,
    UnknownOrientation,
    SensorsUnavailable,
}

enum class FocusStabilityState {
    Unstable,
    Stabilizing,
    Stable,
    Unavailable,
}

data class FocusSensorDiagnostics(
    val tiltDegrees: Float,
    val normalizedZ: Float,
    val orientationConfidence: Float,
    val currentMotionMagnitude: Float,
    val rollingMovementScore: Float,
    val stableForMillis: Long,
    val activeSensors: Set<SensorSource>,
)

enum class SensorSource {
    Accelerometer,
    RotationVector,
}
