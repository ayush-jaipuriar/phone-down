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
        ToneGenerator(AudioManager.STREAM_NOTIFICATION, 50).let { generator ->
            generator.startTone(tone, durationMs)
            generator.release()
        }
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
        val timings =
            when (event) {
                FocusFeedbackEvent.PhoneDownDetected -> longArrayOf(0L, 20L)
                FocusFeedbackEvent.TimerStarted -> longArrayOf(0L, 30L, 20L, 30L)
                FocusFeedbackEvent.PhonePickedUp -> longArrayOf(0L, 50L)
                FocusFeedbackEvent.SessionCompleted -> longArrayOf(0L, 40L, 20L, 70L)
                FocusFeedbackEvent.SessionBroken -> longArrayOf(0L, 35L)
            }
        vibrator.vibrate(VibrationEffect.createWaveform(timings, -1))
    }
}
