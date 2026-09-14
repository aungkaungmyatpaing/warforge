package com.maddog.warforge.ui

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import android.util.Log
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.random.Random

/**
 * The background score, synthesised rather than shipped.
 *
 * A workshop at night: a low drone, a chord that turns over every two bars, a slow pulse
 * and the occasional tap of metal. It is written here for the same reason the vehicles
 * are - no audio assets means nothing to license, nothing to download, and a loop that
 * can be retuned by changing a number.
 *
 * The loop is an exact number of bars and every sustained voice is snapped to a whole
 * number of cycles across it, so the join is silent.
 */
object Music {

    internal const val RATE = 22050
    private const val BPM = 68f
    private const val BARS = 12

    @Volatile
    var enabled: Boolean = true
        set(value) {
            field = value
            if (value) resume() else stopNow()
        }

    private var track: AudioTrack? = null
    private var building = false
    private var wanted = false

    /** Set once a device has refused to give us an AudioTrack, so we stop asking. */
    private var unavailable = false
    private val handler = Handler(Looper.getMainLooper())
    private val pauseSoon = Runnable { pauseNow() }

    /** Called when a screen comes forward. Safe to call repeatedly. */
    fun resume() {
        handler.removeCallbacks(pauseSoon)
        wanted = true
        if (!enabled) return
        val existing = track
        if (existing != null) {
            if (existing.playState != AudioTrack.PLAYSTATE_PLAYING) existing.play()
            return
        }
        if (building) return
        building = true
        // Rendering half a minute of audio is not main-thread work.
        Thread({
            val pcm = render()
            handler.post {
                building = false
                if (!wanted || !enabled || unavailable) return@post
                val built = build(pcm)
                if (built == null) {
                    // The device will not give us an AudioTrack. Stop asking - retrying
                    // on every screen change would just fail on every screen change.
                    unavailable = true
                    Log.w("Warforge", "music unavailable on this device")
                    return@post
                }
                track = built.also { it.play() }
            }
        }, "warforge-music").apply { isDaemon = true }.start()
    }

    /**
     * Called when a screen goes away. Deferred, because moving between the hangar and a
     * build passes through a moment with no activity resumed and the music should not
     * stutter every time the player opens something.
     */
    fun pause() {
        wanted = false
        handler.removeCallbacks(pauseSoon)
        handler.postDelayed(pauseSoon, 700)
    }

    private fun pauseNow() {
        if (wanted) return
        track?.takeIf { it.playState == AudioTrack.PLAYSTATE_PLAYING }?.pause()
    }

    private fun stopNow() {
        handler.removeCallbacks(pauseSoon)
        track?.let {
            it.stop()
            it.release()
        }
        track = null
    }

    /**
     * Builds the looping track, or returns null if this device will not give us one.
     *
     * Every call here can fail and one of them does in practice: a MODE_STATIC track
     * needs its whole buffer - about 1.3 MB for half a minute of 22 kHz mono - out of a
     * pool of shared audio memory, and `Builder.build()` throws
     * UnsupportedOperationException when there is not enough. It ran on the main thread
     * inside a posted Runnable, so on a device that refused, background music took the
     * entire game down on launch.
     *
     * Music is decoration. It is never worth a crash.
     */
    private fun build(pcm: ShortArray): AudioTrack? = try {
        buildOrThrow(pcm)
    } catch (e: Exception) {
        Log.w("Warforge", "could not start music", e)
        null
    }

    private fun buildOrThrow(pcm: ShortArray): AudioTrack {
        val bytes = pcm.size * 2
        val audio = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setBufferSizeInBytes(bytes)
            .setTransferMode(AudioTrack.MODE_STATIC)
            .build()
        // Past this point the track exists and has to be released if anything fails,
        // or the audio memory it just claimed is gone until the process dies.
        try {
            audio.write(pcm, 0, pcm.size)
            audio.setLoopPoints(0, pcm.size, -1)
            audio.setVolume(0.32f)
        } catch (e: Exception) {
            audio.release()
            throw e
        }
        return audio
    }

    // ----------------------------------------------------------------------
    // Synthesis
    // ----------------------------------------------------------------------

    /** D minor: the scale the whole piece is drawn from, as semitones from D. */
    private val SCALE = intArrayOf(0, 2, 3, 5, 7, 8, 10)

    /** Two bars each: Dm, B flat, F, C - round and round. */
    private val CHORDS = arrayOf(
        intArrayOf(0, 3, 7), intArrayOf(-4, 0, 3), intArrayOf(3, 7, 10), intArrayOf(-2, 2, 5),
        intArrayOf(0, 3, 7), intArrayOf(-4, 0, 3),
    )

    /** Visible for testing: the synthesis touches no Android APIs. */
    internal fun render(): ShortArray {
        val beat = 60f / BPM
        val seconds = beat * 4 * BARS
        val n = (seconds * RATE).toInt()
        val out = FloatArray(n)
        val random = Random(20250912)

        drone(out, seconds, -24, 0.20f)          // D, two octaves down
        drone(out, seconds, -17, 0.11f)          // the fifth above it
        pads(out, seconds, beat)
        pulse(out, n, beat)
        taps(out, n, beat, random)
        motif(out, n, beat, random)

        val pcm = ShortArray(n)
        for (i in 0 until n) {
            // Soft clip: loud moments round off instead of tearing.
            val x = out[i]
            val y = x / (1f + kotlin.math.abs(x) * 0.7f)
            pcm[i] = (y * 26000f).coerceIn(-32000f, 32000f).roundToInt().toShort()
        }
        return pcm
    }

    /** Frequency of a semitone offset from D3, snapped to a whole number of loop cycles. */
    private fun tone(semitones: Int, seconds: Float): Float {
        val hz = 146.83f * Math.pow(2.0, semitones / 12.0).toFloat()
        val cycles = (hz * seconds).roundToInt().coerceAtLeast(1)
        return cycles / seconds
    }

    private fun drone(out: FloatArray, seconds: Float, semitones: Int, gain: Float) {
        val f = tone(semitones, seconds)
        val wobble = 2f / seconds                 // one slow swell per loop
        for (i in out.indices) {
            val t = i / RATE.toFloat()
            val swell = 0.72f + 0.28f * sin(2 * PI * wobble * t).toFloat()
            out[i] += (sin(2 * PI * f * t) * gain * swell).toFloat()
            out[i] += (sin(2 * PI * f * 2 * t) * gain * 0.22f * swell).toFloat()
        }
    }

    /** A chord bed, two bars apiece, breathing in and out. */
    private fun pads(out: FloatArray, seconds: Float, beat: Float) {
        val barSamples = (beat * 4 * RATE).toInt()
        for (bar in 0 until BARS) {
            val chord = CHORDS[(bar / 2) % CHORDS.size]
            val start = bar * barSamples
            val length = barSamples
            for (note in chord) {
                val f = tone(note, seconds)
                for (k in 0 until length) {
                    val i = start + k
                    if (i >= out.size) break
                    val phase = k / length.toFloat()
                    // Slow in, slow out, so successive bars overlap rather than step.
                    val env = sin(PI * phase).toFloat()
                    val t = i / RATE.toFloat()
                    out[i] += (sin(2 * PI * f * t) * 0.085f * env).toFloat()
                }
            }
        }
    }

    /**
     * Adds a decaying event, wrapping its tail back to the start of the loop.
     *
     * A thump or a tap near the end of the buffer would otherwise be cut off mid-decay,
     * and the loop would then click once per pass - the most obvious flaw generated
     * audio can have.
     */
    private inline fun ring(out: FloatArray, start: Int, length: Int, sample: (Float) -> Float) {
        val n = out.size
        for (k in 0 until length) {
            out[(start + k) % n] += sample(k / RATE.toFloat())
        }
    }

    /** A soft heartbeat on the first and third beat of each bar. */
    private fun pulse(out: FloatArray, n: Int, beat: Float) {
        val step = (beat * RATE).toInt()
        var i = 0
        var index = 0
        while (i < n) {
            if (index % 2 == 0) {
                val strong = index % 4 == 0
                val gain = if (strong) 0.5f else 0.32f
                ring(out, i, (0.34f * RATE).toInt()) { t ->
                    val env = exp((-t * 11f).toDouble()).toFloat()
                    // Pitch falls away, which is what makes a thump read as a thump.
                    val f = 62f * exp((-t * 5f).toDouble()).toFloat() + 34f
                    (sin(2 * PI * f * t) * gain * env).toFloat()
                }
            }
            i += step
            index++
        }
    }

    /** Occasional metal on metal, somewhere off in the workshop. */
    private fun taps(out: FloatArray, n: Int, beat: Float, random: Random) {
        val step = (beat * RATE).toInt()
        var index = 0
        var i = 0
        while (i < n) {
            if (index % 8 == 3 || (index % 8 == 6 && random.nextFloat() < 0.55f)) {
                val pitch = 900f + random.nextFloat() * 700f
                ring(out, i, (0.22f * RATE).toInt()) { t ->
                    val env = exp((-t * 26f).toDouble()).toFloat()
                    val body = sin(2 * PI * pitch * t) + 0.5 * sin(2 * PI * pitch * 2.76 * t)
                    (body * 0.06f * env).toFloat()
                }
            }
            i += step
            index++
        }
    }

    /** A few notes, sparse enough to stay out of the way. */
    private fun motif(out: FloatArray, n: Int, beat: Float, random: Random) {
        val seconds = n / RATE.toFloat()
        var i = (beat * 8 * RATE).toInt()
        val gap = (beat * 3 * RATE).toInt()
        var step = 0
        while (i < n) {
            if (random.nextFloat() < 0.6f) {
                val degree = SCALE[random.nextInt(SCALE.size)]
                val f = tone(degree + 12, seconds)
                // Phase runs from the note's own start, so a wrapped tail stays smooth.
                ring(out, i, (beat * 1.8f * RATE).toInt()) { t ->
                    val env = (1f - exp((-t * 30f).toDouble()).toFloat()) *
                        exp((-t * 2.1f).toDouble()).toFloat()
                    ((sin(2 * PI * f * t) * 0.07f + sin(2 * PI * f * 2 * t) * 0.02f) * env).toFloat()
                }
            }
            i += gap
            step++
        }
    }
}
