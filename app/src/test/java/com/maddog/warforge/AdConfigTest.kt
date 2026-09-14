package com.maddog.warforge

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards the one rule that can get the AdMob account closed.
 *
 * Debug builds must never carry a live ad unit. Clicking your own ads is invalid traffic
 * whether you meant it or not, and the developer doing the clicking is normally the one
 * testing the build. This ran wrong once already - the app id was set in `defaultConfig`,
 * so it applied to both build types, and a debug build came out with the real app id
 * alongside the test unit ids.
 */
class AdConfigTest {

    /** Google's public test ids. Anything under this publisher is safe to click. */
    private val testPublisher = "ca-app-pub-3940256099942544"

    @Test
    fun `debug builds only ever use google's test ad units`() {
        // This test source set is compiled against the debug variant, so BuildConfig here
        // is the debug one.
        for ((name, unit) in listOf(
            "banner" to BuildConfig.AD_BANNER,
            "interstitial" to BuildConfig.AD_INTERSTITIAL,
            "rewarded" to BuildConfig.AD_REWARDED,
        )) {
            assertTrue(
                "debug $name is a live ad unit: $unit",
                unit.startsWith("$testPublisher/"),
            )
        }
    }

    @Test
    fun `the debug application id is suffixed so it cannot replace a store install`() {
        assertTrue(BuildConfig.APPLICATION_ID.endsWith(".debug"))
    }

    /**
     * The update manifest must be fetched over TLS, or anyone on the same network could
     * hand the player a manifest pointing at a store listing of their choosing - and the
     * prompt invites them to tap it.
     */
    @Test
    fun `the update manifest url is https or empty`() {
        val url = BuildConfig.UPDATE_MANIFEST_URL
        assertTrue("manifest url must be https: $url", url.isEmpty() || url.startsWith("https://"))
        assertFalse("manifest url must not be plain http", url.startsWith("http://"))
    }
}
