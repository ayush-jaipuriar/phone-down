@file:Suppress("LongParameterList")

package phonedown.core.sensors

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class FocusValidityEvaluatorTest {
    private val config =
        FocusSensorConfig(
            stableDurationMillis = 3_000L,
            movementWindowMillis = 2_000L,
        )

    @Test
    fun stableFaceDownBecomesValidAfterThreeSeconds() {
        val evaluator = FocusValidityEvaluator(config = config, debugDiagnosticsEnabled = true)

        val first = evaluator.evaluate(faceDownSnapshot(elapsedRealtimeMillis = 0L))
        val second = evaluator.evaluate(faceDownSnapshot(elapsedRealtimeMillis = 2_000L))
        val third = evaluator.evaluate(faceDownSnapshot(elapsedRealtimeMillis = 3_000L))

        assertFalse(first.isValid)
        assertEquals(FocusValidityReason.FaceDownStabilizing, first.reason)
        assertEquals(FocusStabilityState.Stabilizing, first.stabilityState)

        assertFalse(second.isValid)
        assertEquals(FocusValidityReason.FaceDownStabilizing, second.reason)

        assertTrue(third.isValid)
        assertEquals(FocusValidityReason.FaceDownStable, third.reason)
        assertEquals(FocusStabilityState.Stable, third.stabilityState)
        assertNotNull(third.diagnostics)
    }

    @Test
    fun faceUpIsRejected() {
        val evaluator = FocusValidityEvaluator(config = config)

        val result =
            evaluator.evaluate(
                snapshot(
                    elapsedRealtimeMillis = 0L,
                    gravityZ = 9.7f,
                    tiltDegrees = 5f,
                    linearMotionMagnitude = 0.02f,
                ),
            )

        assertFalse(result.isValid)
        assertEquals(FocusValidityReason.FaceUp, result.reason)
    }

    @Test
    fun verticalIsRejected() {
        val evaluator = FocusValidityEvaluator(config = config)

        val result =
            evaluator.evaluate(
                snapshot(
                    elapsedRealtimeMillis = 0L,
                    gravityX = 9.7f,
                    gravityZ = 0.2f,
                    tiltDegrees = 82f,
                    linearMotionMagnitude = 0.03f,
                ),
            )

        assertFalse(result.isValid)
        assertEquals(FocusValidityReason.Vertical, result.reason)
    }

    @Test
    fun strongMovementIsRejectedEvenWhenFaceDown() {
        val evaluator = FocusValidityEvaluator(config = config)

        val result =
            evaluator.evaluate(
                faceDownSnapshot(
                    elapsedRealtimeMillis = 0L,
                    linearMotionMagnitude = 1.6f,
                ),
            )

        assertFalse(result.isValid)
        assertEquals(FocusValidityReason.Moving, result.reason)
    }

    @Test
    fun walkingLikeRollingMovementFallsBackToInvalid() {
        val evaluator = FocusValidityEvaluator(config = config)

        repeat(4) { index ->
            evaluator.evaluate(
                faceDownSnapshot(
                    elapsedRealtimeMillis = index * 500L,
                    linearMotionMagnitude = 0.85f,
                ),
            )
        }

        val result =
            evaluator.evaluate(
                faceDownSnapshot(
                    elapsedRealtimeMillis = 2_000L,
                    linearMotionMagnitude = 0.82f,
                ),
            )

        assertFalse(result.isValid)
        assertEquals(FocusValidityReason.Moving, result.reason)
    }

    @Test
    fun pocketLikeCaseIsRejectedConservatively() {
        val evaluator = FocusValidityEvaluator(config = config)

        val result =
            evaluator.evaluate(
                snapshot(
                    elapsedRealtimeMillis = 0L,
                    gravityX = 3.0f,
                    gravityY = 0.0f,
                    gravityZ = -8.7f,
                    tiltDegrees = 38f,
                    linearMotionMagnitude = 0.6f,
                ),
            )

        assertFalse(result.isValid)
        assertEquals(FocusValidityReason.PocketLike, result.reason)
    }

    @Test
    fun tinyVibrationDoesNotBreakStableCandidate() {
        val evaluator = FocusValidityEvaluator(config = config)

        evaluator.evaluate(faceDownSnapshot(elapsedRealtimeMillis = 0L, linearMotionMagnitude = 0.05f))
        evaluator.evaluate(faceDownSnapshot(elapsedRealtimeMillis = 1_500L, linearMotionMagnitude = 0.08f))
        val result =
            evaluator.evaluate(
                faceDownSnapshot(elapsedRealtimeMillis = 3_100L, linearMotionMagnitude = 0.12f),
            )

        assertTrue(result.isValid)
        assertEquals(FocusValidityReason.FaceDownStable, result.reason)
    }

    @Test
    fun missingAccelerometerIsUnavailable() {
        val evaluator = FocusValidityEvaluator(config = config, debugDiagnosticsEnabled = true)

        val result =
            evaluator.evaluate(
                snapshot(
                    elapsedRealtimeMillis = 0L,
                    gravityZ = 0f,
                    activeSensors = emptySet(),
                ),
            )

        assertFalse(result.isValid)
        assertEquals(FocusValidityReason.SensorsUnavailable, result.reason)
        assertEquals(FocusStabilityState.Unavailable, result.stabilityState)
        assertNotNull(result.diagnostics)
    }

    private fun faceDownSnapshot(
        elapsedRealtimeMillis: Long,
        linearMotionMagnitude: Float = 0.04f,
    ): FocusSensorSnapshot =
        snapshot(
            elapsedRealtimeMillis = elapsedRealtimeMillis,
            gravityZ = -9.6f,
            tiltDegrees = 6f,
            linearMotionMagnitude = linearMotionMagnitude,
        )

    private fun snapshot(
        elapsedRealtimeMillis: Long,
        gravityX: Float = 0.2f,
        gravityY: Float = 0.1f,
        gravityZ: Float = -9.6f,
        tiltDegrees: Float? = 6f,
        linearMotionMagnitude: Float = 0.04f,
        activeSensors: Set<SensorSource> = setOf(SensorSource.Accelerometer),
    ): FocusSensorSnapshot =
        FocusSensorSnapshot(
            elapsedRealtimeMillis = elapsedRealtimeMillis,
            gravityX = gravityX,
            gravityY = gravityY,
            gravityZ = gravityZ,
            linearMotionMagnitude = linearMotionMagnitude,
            tiltDegrees = tiltDegrees,
            activeSensors = activeSensors,
        )
}
