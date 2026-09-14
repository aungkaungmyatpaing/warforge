package com.maddog.warforge

import com.maddog.warforge.ui.Music
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Guards on the synthesised score. Nothing here checks that it is any good - only that
 * it is audible, that it does not clip, and that the loop joins without a click, which
 * are the three ways procedural audio usually goes wrong.
 */
class MusicTest {

    private val pcm by lazy { Music.render() }

    @Test
    fun `the loop is a sensible length`() {
        val seconds = pcm.size / Music.RATE.toFloat()
        assertTrue("loop is $seconds s", seconds in 20f..90f)
    }

    @Test
    fun `it is audible but not loud`() {
        var sum = 0.0
        var peak = 0
        for (s in pcm) {
            sum += s.toDouble() * s
            peak = maxOf(peak, abs(s.toInt()))
        }
        val rms = sqrt(sum / pcm.size)
        assertTrue("silent (rms $rms)", rms > 800)
        assertTrue("too loud (rms $rms)", rms < 12000)
        assertTrue("clipping (peak $peak)", peak < 32700)
    }

    /**
     * The end runs straight into the beginning, so a mismatch there is a click once per
     * loop - the most obvious flaw a generated track can have.
     */
    @Test
    fun `the loop joins without a click`() {
        val last = pcm[pcm.size - 1].toInt()
        val first = pcm[0].toInt()
        assertEquals("step of ${abs(last - first)} at the loop point", 0f, (last - first) / 32768f, 0.07f)

        // And the slope should match too, or the join reads as a kink.
        val slopeOut = pcm[pcm.size - 1] - pcm[pcm.size - 2]
        val slopeIn = pcm[1] - pcm[0]
        assertTrue("slope jumps at the loop point", abs(slopeOut - slopeIn) < 2600)
    }

    @Test
    fun `there is no long silence`() {
        // A gap longer than a bar would read as the music having stopped.
        val window = Music.RATE * 3
        var quiet = 0
        var longest = 0
        for (s in pcm) {
            if (abs(s.toInt()) < 120) {
                quiet++
                longest = maxOf(longest, quiet)
            } else {
                quiet = 0
            }
        }
        assertTrue("silent for ${longest / Music.RATE.toFloat()} s", longest < window)
    }
}
