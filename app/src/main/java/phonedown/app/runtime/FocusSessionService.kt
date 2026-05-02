@file:Suppress("MagicNumber")

package phonedown.app.runtime

import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import phonedown.app.MainActivity
import phonedown.core.model.repository.SettingsRepository
import phonedown.core.notifications.FocusFeedbackPlayer
import phonedown.core.notifications.FocusForegroundNotificationManager
import phonedown.core.notifications.FocusForegroundNotificationState
import phonedown.core.sensors.FocusValidityMonitor
import javax.inject.Inject

@AndroidEntryPoint
class FocusSessionService : Service() {
    @Inject
    lateinit var runtimeCoordinator: ActiveSessionRuntimeCoordinator

    @Inject
    lateinit var focusValidityMonitor: FocusValidityMonitor

    @Inject
    lateinit var callInterruptionMonitor: CallInterruptionMonitor

    @Inject
    lateinit var notificationManager: FocusForegroundNotificationManager

    @Inject
    lateinit var feedbackPlayer: FocusFeedbackPlayer

    @Inject
    lateinit var settingsRepository: SettingsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var sensorJob: Job? = null
    private var callJob: Job? = null
    private var tickJob: Job? = null
    private var stateJob: Job? = null
    private var lastCallState = false

    override fun onCreate() {
        super.onCreate()
        notificationManager.ensureChannel()
        stateJob =
            serviceScope.launch {
                runtimeCoordinator.state.collectLatest { state ->
                    startForegroundCompat(
                        FocusForegroundNotificationManager.NOTIFICATION_ID,
                        notificationManager.buildForegroundNotification(
                            state =
                                FocusForegroundNotificationState(
                                    title = state.notificationTitle,
                                    body = state.notificationBody,
                                ),
                            contentIntent = focusContentIntent(),
                            endSessionIntent = endSessionPendingIntent(),
                        ),
                    )
                    if (state.shouldStopService) {
                        stopRuntimeLoops()
                        stopSelf()
                        runtimeCoordinator.clearFinishedRuntime()
                    }
                }
            }
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int,
    ): Int {
        val restartMode =
            when (intent?.action) {
                FocusSessionServiceContract.ACTION_END -> {
                    serviceScope.launch {
                        val result = runtimeCoordinator.endSession()
                        playFeedback(result.feedbackEvents)
                    }
                    START_STICKY
                }

                FocusSessionServiceContract.ACTION_START -> {
                    serviceScope.launch {
                        runtimeCoordinator.ensureSessionStarted(intent.requestedDurationSeconds())
                        startRuntimeLoops(forceRestartSensors = true)
                    }
                    START_STICKY
                }

                FocusSessionServiceContract.ACTION_RETRY_SENSORS -> {
                    serviceScope.launch {
                        runtimeCoordinator.ensureSessionStarted(intent.requestedDurationSeconds())
                        startRuntimeLoops(forceRestartSensors = true)
                    }
                    START_STICKY
                }

                null -> {
                    serviceScope.launch {
                        runtimeCoordinator.recoverFromUnexpectedServiceRestart()
                        stopSelf()
                    }
                    START_NOT_STICKY
                }

                else -> {
                    stopSelf()
                    START_NOT_STICKY
                }
            }
        return restartMode
    }

    override fun onDestroy() {
        stopRuntimeLoops()
        runBlocking {
            runtimeCoordinator.flushCurrentRuntime()
        }
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun startRuntimeLoops(forceRestartSensors: Boolean = false) {
        if (forceRestartSensors && sensorJob != null) {
            focusValidityMonitor.stop()
            sensorJob?.cancel()
            sensorJob = null
        }
        if (sensorJob == null) {
            focusValidityMonitor.start()
            sensorJob =
                serviceScope.launch {
                    focusValidityMonitor.validity.collectLatest { result ->
                        val step = runtimeCoordinator.onSensorValidityChanged(result)
                        playFeedback(step.feedbackEvents)
                    }
                }
        }
        if (callJob == null) {
            callInterruptionMonitor.start()
            callJob =
                serviceScope.launch {
                    callInterruptionMonitor.isInCall.collectLatest { isInCall ->
                        if (isInCall == lastCallState) {
                            return@collectLatest
                        }
                        lastCallState = isInCall
                        val step = runtimeCoordinator.onCallStateChanged(isInCall)
                        playFeedback(step.feedbackEvents)
                    }
                }
        }
        if (tickJob == null) {
            tickJob =
                serviceScope.launch {
                    while (true) {
                        delay(1_000L)
                        val step = runtimeCoordinator.onTick()
                        playFeedback(step.feedbackEvents)
                    }
                }
        }
    }

    private fun stopRuntimeLoops() {
        focusValidityMonitor.stop()
        callInterruptionMonitor.stop()
        sensorJob?.cancel()
        callJob?.cancel()
        tickJob?.cancel()
        sensorJob = null
        callJob = null
        tickJob = null
        lastCallState = false
    }

    private suspend fun playFeedback(events: List<phonedown.core.notifications.FocusFeedbackEvent>) {
        if (events.isEmpty()) return
        val settings = settingsRepository.settings.first()
        for (event in events) {
            feedbackPlayer.play(event, settings)
        }
    }

    private fun focusContentIntent(): PendingIntent {
        val intent =
            Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(FocusSessionServiceContract.EXTRA_OPEN_FOCUS, true)
            }
        return PendingIntent.getActivity(
            this,
            10,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    private fun endSessionPendingIntent(): PendingIntent {
        val intent =
            Intent(this, FocusSessionService::class.java).apply {
                action = FocusSessionServiceContract.ACTION_END
            }
        return PendingIntent.getService(
            this,
            11,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
    }

    companion object {
        fun start(
            context: Context,
            plannedDurationSeconds: Long? = null,
        ) {
            val intent =
                Intent(context, FocusSessionService::class.java).apply {
                    action = FocusSessionServiceContract.ACTION_START
                    plannedDurationSeconds?.let {
                        putExtra(FocusSessionServiceContract.EXTRA_PLANNED_DURATION_SECONDS, it)
                    }
                }
            ContextCompat.startForegroundService(context, intent)
        }

        fun retrySensors(
            context: Context,
            plannedDurationSeconds: Long? = null,
        ) {
            val intent =
                Intent(context, FocusSessionService::class.java).apply {
                    action = FocusSessionServiceContract.ACTION_RETRY_SENSORS
                    plannedDurationSeconds?.let {
                        putExtra(FocusSessionServiceContract.EXTRA_PLANNED_DURATION_SECONDS, it)
                    }
                }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    private fun startForegroundCompat(
        notificationId: Int,
        notification: android.app.Notification,
    ) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                notificationId,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC,
            )
        } else {
            startForeground(notificationId, notification)
        }
    }

    private fun Intent?.requestedDurationSeconds(): Long? {
        val currentIntent = this ?: return null
        if (!currentIntent.hasExtra(FocusSessionServiceContract.EXTRA_PLANNED_DURATION_SECONDS)) {
            return null
        }
        return currentIntent.getLongExtra(FocusSessionServiceContract.EXTRA_PLANNED_DURATION_SECONDS, 0L)
    }
}
