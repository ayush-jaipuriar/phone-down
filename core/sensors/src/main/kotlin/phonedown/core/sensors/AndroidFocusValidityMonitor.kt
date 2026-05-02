package phonedown.core.sensors

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.sqrt

class AndroidFocusValidityMonitor(
    context: Context,
    private val sensorManager: SensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager,
    private val config: FocusSensorConfig = FocusSensorConfig(),
    private val debugDiagnosticsEnabled: Boolean = false,
) : FocusValidityMonitor, SensorEventListener {
    private val evaluator = FocusValidityEvaluator(
        config = config,
        debugDiagnosticsEnabled = debugDiagnosticsEnabled,
    )
    private val _validity = MutableStateFlow(
        FocusValidityResult(
            isValid = false,
            reason = FocusValidityReason.SensorsUnavailable,
            stabilityState = FocusStabilityState.Unavailable,
            orientationConfidence = null,
            movementScore = null,
            diagnostics = null,
        ),
    )
    override val validity: StateFlow<FocusValidityResult> = _validity.asStateFlow()

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    private var started = false
    private val gravity = FloatArray(3)
    private val linearAcceleration = FloatArray(3)
    private var latestTiltDegrees: Float? = null
    private val activeSensors = mutableSetOf<SensorSource>()

    override fun start() {
        if (started) {
            return
        }
        val accelerometerSensor = accelerometer
        if (accelerometerSensor == null) {
            publishUnavailable()
            return
        }
        started = true
        activeSensors.clear()
        sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME)
        activeSensors += SensorSource.Accelerometer

        rotationVector?.let { sensor ->
            sensorManager.registerListener(this, sensor, SensorManager.SENSOR_DELAY_GAME)
            activeSensors += SensorSource.RotationVector
        }
    }

    override fun stop() {
        if (!started) {
            return
        }
        started = false
        sensorManager.unregisterListener(this)
        activeSensors.clear()
    }

    override fun onSensorChanged(event: SensorEvent) {
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> handleAccelerometer(event)
            Sensor.TYPE_ROTATION_VECTOR -> handleRotationVector(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    private fun handleAccelerometer(event: SensorEvent) {
        val values = event.values
        for (index in 0..2) {
            gravity[index] =
                config.lowPassAlpha * gravity[index] + (1f - config.lowPassAlpha) * values[index]
            linearAcceleration[index] = values[index] - gravity[index]
        }

        val snapshot = FocusSensorSnapshot(
            elapsedRealtimeMillis = event.timestamp / 1_000_000L,
            gravityX = gravity[0],
            gravityY = gravity[1],
            gravityZ = gravity[2],
            linearMotionMagnitude = magnitude(
                linearAcceleration[0],
                linearAcceleration[1],
                linearAcceleration[2],
            ),
            tiltDegrees = latestTiltDegrees ?: tiltFromGravity(gravity[2]),
            activeSensors = activeSensors.toSet(),
        )
        _validity.value = evaluator.evaluate(snapshot)
    }

    private fun handleRotationVector(event: SensorEvent) {
        val rotationMatrix = FloatArray(9)
        SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
        val orientation = FloatArray(3)
        SensorManager.getOrientation(rotationMatrix, orientation)
        val pitch = Math.toDegrees(orientation[1].toDouble()).toFloat()
        val roll = Math.toDegrees(orientation[2].toDouble()).toFloat()
        latestTiltDegrees = maxOf(abs(pitch), abs(roll))
    }

    private fun publishUnavailable() {
        _validity.value = FocusValidityResult(
            isValid = false,
            reason = FocusValidityReason.SensorsUnavailable,
            stabilityState = FocusStabilityState.Unavailable,
            orientationConfidence = null,
            movementScore = null,
            diagnostics = null,
        )
    }

    private fun magnitude(x: Float, y: Float, z: Float): Float {
        return sqrt(x * x + y * y + z * z)
    }

    private fun tiltFromGravity(z: Float): Float {
        val normalized = (z / SensorManager.GRAVITY_EARTH).coerceIn(-1f, 1f)
        return Math.toDegrees(asin(abs(normalized)).toDouble()).toFloat().let { 90f - it }
    }
}
