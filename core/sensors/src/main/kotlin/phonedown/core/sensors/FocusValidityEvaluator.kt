@file:Suppress(
    "LongMethod",
    "CyclomaticComplexMethod",
    "DestructuringDeclarationWithTooManyEntries",
    "MagicNumber",
)

package phonedown.core.sensors

import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

class FocusValidityEvaluator(
    private val config: FocusSensorConfig = FocusSensorConfig(),
    private val debugDiagnosticsEnabled: Boolean = false,
) {
    private var stableCandidateStartedAtMillis: Long? = null
    private val movementSamples = ArrayDeque<MovementSample>()

    fun evaluate(snapshot: FocusSensorSnapshot): FocusValidityResult {
        pruneOldSamples(snapshot.elapsedRealtimeMillis)
        movementSamples.addLast(
            MovementSample(
                elapsedRealtimeMillis = snapshot.elapsedRealtimeMillis,
                motionMagnitude = snapshot.linearMotionMagnitude,
            ),
        )

        val gravityMagnitude = magnitude(snapshot.gravityX, snapshot.gravityY, snapshot.gravityZ)
        if (gravityMagnitude <= 0.1f || SensorSource.Accelerometer !in snapshot.activeSensors) {
            stableCandidateStartedAtMillis = null
            return FocusValidityResult(
                isValid = false,
                reason = FocusValidityReason.SensorsUnavailable,
                stabilityState = FocusStabilityState.Unavailable,
                orientationConfidence = null,
                movementScore = null,
                diagnostics =
                    if (debugDiagnosticsEnabled) {
                        FocusSensorDiagnostics(
                            tiltDegrees = snapshot.tiltDegrees ?: 90f,
                            normalizedZ = 0f,
                            orientationConfidence = 0f,
                            currentMotionMagnitude = snapshot.linearMotionMagnitude,
                            rollingMovementScore = 0f,
                            stableForMillis = 0L,
                            activeSensors = snapshot.activeSensors,
                        )
                    } else {
                        null
                    },
            )
        }

        val normalizedZ = snapshot.gravityZ / gravityMagnitude
        val tiltDegrees = snapshot.tiltDegrees ?: tiltFromNormalizedZ(normalizedZ)
        val orientationConfidence = (-normalizedZ).coerceIn(0f, 1f)
        val movementScore = rollingMovementScore()

        val faceDownCandidate =
            normalizedZ <= config.faceDownZThreshold && tiltDegrees <= config.flatTiltThresholdDegrees
        val faceUp = normalizedZ >= abs(config.faceDownZThreshold)
        val vertical = abs(normalizedZ) <= config.verticalZThreshold
        val moving =
            snapshot.linearMotionMagnitude >= config.pickupMotionThreshold ||
                movementScore >= config.rollingMotionThreshold
        val pocketLike =
            !faceDownCandidate &&
                normalizedZ < -0.45f &&
                tiltDegrees in config.flatTiltThresholdDegrees..config.pocketTiltThresholdDegrees &&
                movementScore >= config.pocketMotionThreshold

        val (reason, stabilityState, isValid, stableForMillis) =
            when {
                faceUp -> {
                    stableCandidateStartedAtMillis = null
                    Evaluation(FocusValidityReason.FaceUp, FocusStabilityState.Unstable, false, 0L)
                }

                vertical -> {
                    stableCandidateStartedAtMillis = null
                    Evaluation(FocusValidityReason.Vertical, FocusStabilityState.Unstable, false, 0L)
                }

                pocketLike -> {
                    stableCandidateStartedAtMillis = null
                    Evaluation(FocusValidityReason.PocketLike, FocusStabilityState.Unstable, false, 0L)
                }

                !faceDownCandidate -> {
                    stableCandidateStartedAtMillis = null
                    Evaluation(FocusValidityReason.UnknownOrientation, FocusStabilityState.Unstable, false, 0L)
                }

                moving -> {
                    stableCandidateStartedAtMillis = null
                    Evaluation(FocusValidityReason.Moving, FocusStabilityState.Unstable, false, 0L)
                }

                else -> {
                    val startedAt =
                        stableCandidateStartedAtMillis ?: snapshot.elapsedRealtimeMillis.also {
                            stableCandidateStartedAtMillis = it
                        }
                    val stableDuration = snapshot.elapsedRealtimeMillis - startedAt
                    if (stableDuration >= config.stableDurationMillis) {
                        Evaluation(
                            FocusValidityReason.FaceDownStable,
                            FocusStabilityState.Stable,
                            true,
                            stableDuration,
                        )
                    } else {
                        Evaluation(
                            FocusValidityReason.FaceDownStabilizing,
                            FocusStabilityState.Stabilizing,
                            false,
                            stableDuration,
                        )
                    }
                }
            }

        return FocusValidityResult(
            isValid = isValid,
            reason = reason,
            stabilityState = stabilityState,
            orientationConfidence = orientationConfidence,
            movementScore = movementScore,
            diagnostics =
                if (debugDiagnosticsEnabled) {
                    FocusSensorDiagnostics(
                        tiltDegrees = tiltDegrees,
                        normalizedZ = normalizedZ,
                        orientationConfidence = orientationConfidence,
                        currentMotionMagnitude = snapshot.linearMotionMagnitude,
                        rollingMovementScore = movementScore,
                        stableForMillis = stableForMillis,
                        activeSensors = snapshot.activeSensors,
                    )
                } else {
                    null
                },
        )
    }

    private fun pruneOldSamples(nowMillis: Long) {
        while (movementSamples.isNotEmpty() &&
            nowMillis - movementSamples.first().elapsedRealtimeMillis > config.movementWindowMillis
        ) {
            movementSamples.removeFirst()
        }
    }

    private fun rollingMovementScore(): Float {
        if (movementSamples.isEmpty()) {
            return 0f
        }
        val averageSquared =
            movementSamples
                .map { sample -> sample.motionMagnitude.pow(2) }
                .average()
                .toFloat()
        return sqrt(averageSquared)
    }

    private fun tiltFromNormalizedZ(normalizedZ: Float): Float =
        Math.toDegrees(acos(abs(normalizedZ).coerceIn(0f, 1f)).toDouble()).toFloat()

    private fun magnitude(
        x: Float,
        y: Float,
        z: Float,
    ): Float = sqrt(x * x + y * y + z * z)

    private data class MovementSample(
        val elapsedRealtimeMillis: Long,
        val motionMagnitude: Float,
    )

    private data class Evaluation(
        val reason: FocusValidityReason,
        val stabilityState: FocusStabilityState,
        val isValid: Boolean,
        val stableForMillis: Long,
    )
}
