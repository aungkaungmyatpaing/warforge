package com.naymyo.warforge.ui

import android.os.Bundle
import android.widget.FrameLayout
import com.naymyo.warforge.BuildConfig
import androidx.appcompat.app.AppCompatActivity
import com.naymyo.warforge.ads.AdsManager
import com.naymyo.warforge.ads.ConsentManager

/**
 * Consent -> SDK init -> banner, shared by both screens.
 *
 * None of it blocks the game: if consent is refused or the network is down the banner
 * slot simply stays empty and play is unaffected.
 */
abstract class AdHostActivity : AppCompatActivity() {

    private lateinit var consent: ConsentManager
    private var flowRunning = false

    /** The container the banner attaches to, or null on screens without one. */
    protected abstract val adContainer: FrameLayout?

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        consent = ConsentManager(applicationContext)
    }

    protected fun startAdsFlow() {
        val container = adContainer ?: return
        // Screenshot builds hide the slot entirely rather than just skipping the request,
        // so the layout closes up and captures come out at the full screen size.
        if (BuildConfig.HIDE_ADS) {
            container.visibility = android.view.View.GONE
            return
        }
        if (AdsManager.adsAllowed || flowRunning) {
            if (AdsManager.adsAllowed) container.post { AdsManager.attachBanner(this, container) }
            return
        }
        flowRunning = true
        consent.ensureConsent(this) { canRequestAds ->
            flowRunning = false
            AdsManager.initialize(applicationContext, canRequestAds)
            if (canRequestAds) {
                AdsManager.preload(applicationContext)
                container.post {
                    if (!isFinishing && !isDestroyed) AdsManager.attachBanner(this, container)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        Music.resume()
        adContainer?.let { AdsManager.resumeBanner(it) }
        startAdsFlow()
    }

    override fun onPause() {
        Music.pause()
        adContainer?.let { AdsManager.pauseBanner(it) }
        super.onPause()
    }

    override fun onDestroy() {
        adContainer?.let { AdsManager.destroyBanner(it) }
        super.onDestroy()
    }
}
