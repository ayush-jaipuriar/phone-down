@file:Suppress("MagicNumber")

package phonedown.core.notifications

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.SoundPool
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
    private var soundPool: SoundPool? = null
    private val loadedSoundEvents = mutableSetOf<FocusFeedbackEvent>()
    private val soundIds = mutableMapOf<FocusFeedbackEvent, Int>()

    init {
        ensureSoundPool()
    }

    @Synchronized
    private fun ensureSoundPool(): SoundPool {
        val currentPool = soundPool
        if (currentPool != null) {
            return currentPool
        }
        val pool =
            SoundPool
                .Builder()
                .setMaxStreams(2)
                .setAudioAttributes(
                    AudioAttributes
                        .Builder()
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build(),
                ).build()
        pool.setOnLoadCompleteListener { _, sampleId, status ->
            if (status != 0) return@setOnLoadCompleteListener
            soundIds.entries
                .firstOrNull { it.value == sampleId }
                ?.key
                ?.let { loadedSoundEvents += it }
        }
        soundIds[FocusFeedbackEvent.TimerStarted] = pool.load(context, R.raw.focus_start_chime, 1)
        soundIds[FocusFeedbackEvent.SessionCompleted] = pool.load(context, R.raw.focus_complete_chime, 1)
        soundPool = pool
        return pool
    }

    @Synchronized
    fun prepare() {
        ensureSoundPool()
    }

    fun play(
        event: FocusFeedbackEvent,
        settings: UserSettings,
    ) {
        if (
            settings.soundEnabled &&
            audioManager.ringerMode == AudioManager.RINGER_MODE_NORMAL &&
            shouldPlayAudibleCue(event)
        ) {
            playTone(event)
        }
        if (settings.hapticsEnabled) {
            vibrate(event)
        }
    }

    private fun shouldPlayAudibleCue(event: FocusFeedbackEvent): Boolean =
        when (event) {
            FocusFeedbackEvent.PhoneDownDetected -> false
            else -> true
        }

    private fun playTone(event: FocusFeedbackEvent) {
        if (playCustomSoundIfAvailable(event)) {
            return
        }
        val (tone, durationMs) =
            when (event) {
                FocusFeedbackEvent.PhoneDownDetected -> ToneGenerator.TONE_PROP_BEEP to 60
                FocusFeedbackEvent.TimerStarted -> ToneGenerator.TONE_PROP_PROMPT to 160
                FocusFeedbackEvent.PhonePickedUp -> ToneGenerator.TONE_PROP_ACK to 90
                FocusFeedbackEvent.SessionCompleted -> ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD to 260
                FocusFeedbackEvent.SessionBroken -> ToneGenerator.TONE_PROP_NACK to 140
            }
        currentToneGenerator().startTone(tone, durationMs)
    }

    private fun playCustomSoundIfAvailable(event: FocusFeedbackEvent): Boolean {
        val pool = ensureSoundPool()
        val sampleId = soundIds[event] ?: return false
        if (event !in loadedSoundEvents) {
            return false
        }
        val playbackRate =
            when (event) {
                FocusFeedbackEvent.TimerStarted -> 1.0f
                FocusFeedbackEvent.SessionCompleted -> 1.0f
                else -> 1.0f
            }
        pool.play(sampleId, 1f, 1f, 2, 0, playbackRate)
        return true
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
                    longArrayOf(0L, 75L, 35L, 125L) to intArrayOf(0, 200, 0, 255)
                FocusFeedbackEvent.PhonePickedUp ->
                    longArrayOf(0L, 90L) to intArrayOf(0, VibrationEffect.DEFAULT_AMPLITUDE)
                FocusFeedbackEvent.SessionCompleted ->
                    longArrayOf(0L, 70L, 35L, 105L, 40L, 150L) to
                        intArrayOf(0, 175, 0, 215, 0, 255)
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
        return ToneGenerator(AudioManager.STREAM_NOTIFICATION, 100).also {
            toneGenerator = it
        }
    }

    @Synchronized
    fun release() {
        toneGenerator?.release()
        toneGenerator = null
        soundPool?.release()
        soundPool = null
        soundIds.clear()
        loadedSoundEvents.clear()
    }
}
