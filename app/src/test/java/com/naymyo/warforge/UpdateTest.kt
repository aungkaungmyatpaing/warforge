package com.naymyo.warforge

import com.naymyo.warforge.update.UpdateDecision
import com.naymyo.warforge.update.UpdateManifest
import com.naymyo.warforge.update.UpdateState
import com.naymyo.warforge.update.decideUpdate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The update policy, checked without a network, a clock or an Activity.
 *
 * Worth testing properly because in production this runs once a day on somebody else's
 * phone and nobody ever watches it work. The failure modes are all quiet ones: nagging a
 * player who said no, or - much worse - leaving everybody on a broken build because a
 * comparison went the wrong way.
 */
class UpdateTest {

    private val hour = 60L * 60L * 1000L
    private val now = 1_700_000_000_000L

    private fun manifest(latest: Int, min: Int = 0) = UpdateManifest(
        latestVersionCode = latest,
        latestVersionName = "1.$latest",
        minSupportedVersionCode = min,
        notes = "", notesMy = "",
        url = "https://play.google.com/store/apps/details?id=com.naymyo.warforge",
    )

    // -- parsing ---------------------------------------------------------

    @Test
    fun `a full manifest parses`() {
        val m = UpdateManifest.parse(
            """
            {
              "latestVersionCode": 7,
              "latestVersionName": "1.3",
              "minSupportedVersionCode": 2,
              "notes": "New vehicles",
              "notesMy": "ယာဉ်အသစ်",
              "url": "https://play.google.com/store/apps/details?id=com.naymyo.warforge"
            }
            """.trimIndent(),
        )!!
        assertEquals(7, m.latestVersionCode)
        assertEquals("1.3", m.latestVersionName)
        assertEquals(2, m.minSupportedVersionCode)
        assertEquals("com.naymyo.warforge", m.playPackage)
    }

    /** Only the version code is required; a hand-edited file must still work. */
    @Test
    fun `a minimal manifest parses`() {
        val m = UpdateManifest.parse("""{"latestVersionCode": 3}""")!!
        assertEquals(3, m.latestVersionCode)
        assertEquals("3", m.latestVersionName)
        assertEquals(0, m.minSupportedVersionCode)
        assertNull(m.playPackage)
    }

    /**
     * Anything that is not a manifest has to come back null rather than throw. A captive
     * portal serving an HTML login page is the common case, and it must not crash a game
     * that was only trying to start.
     */
    @Test
    fun `junk does not parse and does not throw`() {
        for (junk in listOf(
            "", "   ", "not json", "<html><body>Sign in</body></html>",
            "[1,2,3]", "{}", """{"latestVersionCode": "seven"}""",
            """{"latestVersionCode": 0}""", """{"latestVersionCode": -4}""",
            """{"latestVersionCode": 3,""",
        )) {
            assertNull("parsed <$junk>", UpdateManifest.parse(junk))
        }
    }

    // -- policy ----------------------------------------------------------

    @Test
    fun `nothing to say when already current`() {
        assertTrue(decideUpdate(5, manifest(5), UpdateState(), now) is UpdateDecision.Silent)
        assertTrue(decideUpdate(6, manifest(5), UpdateState(), now) is UpdateDecision.Silent)
    }

    @Test
    fun `nothing to say without a manifest`() {
        assertTrue(decideUpdate(1, null, UpdateState(), now) is UpdateDecision.Silent)
    }

    @Test
    fun `a newer version is offered`() {
        val d = decideUpdate(4, manifest(5), UpdateState(), now)
        assertTrue(d is UpdateDecision.Available)
        assertEquals(5, (d as UpdateDecision.Available).manifest.latestVersionCode)
    }

    @Test
    fun `a skipped version is never mentioned again`() {
        val d = decideUpdate(4, manifest(5), UpdateState(skippedVersionCode = 5), now)
        assertTrue(d is UpdateDecision.Silent)
    }

    /** Skipping one version must not silence the next one. */
    @Test
    fun `skipping does not silence later versions`() {
        val d = decideUpdate(4, manifest(6), UpdateState(skippedVersionCode = 5), now)
        assertTrue(d is UpdateDecision.Available)
    }

    @Test
    fun `the player is left alone for a day after being asked`() {
        val recent = UpdateState(lastPromptedAt = now - 3 * hour)
        assertTrue(decideUpdate(4, manifest(5), recent, now) is UpdateDecision.Silent)

        val yesterday = UpdateState(lastPromptedAt = now - 25 * hour)
        assertTrue(decideUpdate(4, manifest(5), yesterday, now) is UpdateDecision.Available)
    }

    /**
     * A clock that has jumped backwards - the player changed the date, or a preference
     * survived a reinstall - must not mute the prompt until the date catches up again.
     */
    @Test
    fun `a clock that went backwards does not silence the prompt`() {
        val future = UpdateState(lastPromptedAt = now + 400L * 24 * hour)
        assertTrue(decideUpdate(4, manifest(5), future, now) is UpdateDecision.Available)
    }

    @Test
    fun `an unsupported build must update`() {
        val d = decideUpdate(1, manifest(5, min = 3), UpdateState(), now)
        assertTrue(d is UpdateDecision.Required)
    }

    /**
     * A forced update outranks both the skip list and the quiet period: those exist to
     * stop nagging, not to let a player stay on a build that has been declared unusable.
     */
    @Test
    fun `a forced update ignores skip and quiet hours`() {
        val muted = UpdateState(skippedVersionCode = 99, lastPromptedAt = now - hour)
        assertTrue(decideUpdate(1, manifest(5, min = 3), muted, now) is UpdateDecision.Required)
    }

    @Test
    fun `exactly the minimum supported version is not forced`() {
        val d = decideUpdate(3, manifest(5, min = 3), UpdateState(), now)
        assertTrue(d is UpdateDecision.Available)
    }

    // -- store link ------------------------------------------------------

    @Test
    fun `the play package is read out of the listing url`() {
        assertEquals(
            "com.naymyo.warforge",
            manifest(1).playPackage,
        )
        assertNull(UpdateManifest.parse("""{"latestVersionCode":1,"url":"https://example.com"}""")!!
            .playPackage)
    }
}
