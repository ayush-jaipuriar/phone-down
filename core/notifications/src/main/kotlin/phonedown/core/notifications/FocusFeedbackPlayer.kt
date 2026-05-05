@file:Suppress("MagicNumber")

package phonedown.core.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import phonedown.core.model.UserSettings

enum class FocusFeedbackEvent {
    PhoneDownDetected,
    TimerStarted,
    PhonePickedUp,
    SessionCompleted,
    SessionBroken,
}

class FocusFeedbackPlayer(
    private val context: Context,
) {
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private var toneGenerator: ToneGenerator? = null

    fun play(
        event: FocusFeedbackEvent,
        settings: UserSettings,
    ) {
        if (settings.soundEnabled && audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL) {
            playTone(event)
        }
        if (settings.hapticsEnabled) {
            vibrate(event)
        }
    }

    private fun playTone(event: FocusFeedbackEvent) {
        val (tone, durationMs) =
            when (event) {
                FocusFeedbackEvent.PhoneDownDetected -> ToneGenerator.TONE_PROP_BEEP to 60
                FocusFeedbackEvent.TimerStarted -> ToneGenerator.TONE_CDMA_PIP to 120
                FocusFeedbackEvent.PhonePickedUp -> ToneGenerator.TONE_PROP_ACK to 90
                FocusFeedbackEvent.SessionCompleted -> ToneGenerator.TONE_PROP_BEEP2 to 220
                FocusFeedbackEvent.SessionBroken -> ToneGenerator.TONE_PROP_NACK to 140
            }
        currentToneGenerator().startTone(tone, durationMs)
    }

    @SuppressLint("MissingPermission")
    private fun vibrate(event: FocusFeedbackEvent) {
        val vibrator =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
                manager.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
        if (!vibrator.hasVibrator()) {
            return
        }
        val (timings, amplitudes) =
            when (event) {
                FocusFeedbackEvent.PhoneDownDetected ->
                    longArrayOf(0L, 70L) to intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE)
                FocusFeedbackEvent.TimerStarted ->
                    longArrayOf(0L, 80L, 45L, 90L) to intArrayOf(0, 180, 0, VibrationEffect.DEFAULT_AMPLITUDE)
                FocusFeedbackEvent.PhonePickedUp ->
                    longArrayOf(0L, 90L) to intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE)
                FocusFeedbackEvent.SessionCompleted ->
                    longArrayOf(0L, 70L, 50L, 120L) to intArrayOf(0, 160, 0, VibrationEffect.DEFAULT_AMPLITUDE)
                FocusFeedbackEvent.SessionBroken ->
                    longArrayOf(0L, 120L, 60L, 120L) to intArrayOf(0, 220, 0, 220)
            }
        vibrator.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
    }

    @Synchronized
    private fun currentToneGenerator(): ToneGenerator {
        val existing = toneGenerator
        if (existing != null) {
            return existing
        }
        return ToneGenerator(AudioManager.STREAM_NOTIFICATION, 80).also {
            toneGenerator = it
        }
    }

    @Synchronized
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
    }
}
