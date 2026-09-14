package com.maddog.warforge.ui

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.Handler
import android.os.HandlerThread
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.atomic.AtomicInteger

/**
 * Feedback without shipping any audio assets: short system tones plus haptics.
 *
 * Everything runs on a private thread. ToneGenerator.startTone and Vibrator.vibrate are
 * both binder calls into the audio/vibrator services and either can stall for hundreds of
 * milliseconds; called from the drag handler they were long enough to ANR the game on a loaded
 * device. Nothing here is allowed to touch the UI thread.
 *
 * Swap [play] for a SoundPool if you add real rivet/servo samples later.
 */
class Sfx(context: Context) {

    @Volatile var enabled: Boolean = true

    private val thread = HandlerThread("warforge-sfx").apply { start() }
    private val handler = Handler(thread.looper)

    /** Bounds the backlog: if audio is stalling, drop cues instead of queueing them. */
    private val queued = AtomicInteger(0)

    private val appContext = context.applicationContext
    private var toneGen: ToneGenerator? = null
    private var vibrator: Vibrator? = null
    private var hapticsBlocked = false
    private var released = false

    init {
        handler.post {
            toneGen = try {
                ToneGenerator(AudioManager.STREAM_MUSIC, VOLUME)
            } catch (_: RuntimeException) {
                null   // some devices refuse a second ToneGenerator; carry on silently
            }
            vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE)
                    as? VibratorManager)?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        }
    }

    fun snap() = play(ToneGenerator.TONE_PROP_BEEP, 60, 12L)
    fun lock() = play(ToneGenerator.TONE_PROP_ACK, 140, 28L)
    fun win() = play(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 300, 45L)
    fun error() = play(ToneGenerator.TONE_PROP_NACK, 90, 20L)

    private fun play(tone: Int, toneMs: Int, hapticMs: Long) {
        if (!enabled || released) return
        if (queued.get() >= MAX_QUEUED) return
        queued.incrementAndGet()
        handler.post {
            queued.decrementAndGet()
            if (!enabled || released) return@post
            try { toneGen?.startTone(tone, toneMs) } catch (_: RuntimeException) { }
            vibrate(hapticMs)
        }
    }

    private fun vibrate(ms: Long) {
        if (hapticsBlocked) return
        val v = vibrator ?: return
        if (!v.hasVibrator()) return
        // hasVibrator() says the hardware exists, not that we may use it -- haptics are a
        // nicety, so a refused vibrate must never be allowed to take the game down.
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                v.vibrate(VibrationEffect.createOneShot(ms, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION") v.vibrate(ms)
            }
        } catch (_: SecurityException) {
            hapticsBlocked = true
        }
    }

    fun release() {
        released = true
        handler.post {
            try { toneGen?.release() } catch (_: RuntimeException) { }
            toneGen = null
        }
        thread.quitSafely()
    }

    private companion object {
        const val VOLUME = 55
        const val MAX_QUEUED = 3
    }
}
