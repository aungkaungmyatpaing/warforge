package com.naymyo.warforge.ads

import android.app.Activity
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.naymyo.warforge.BuildConfig

/**
 * Every AdMob call in the app goes through here.
 *
 * Formats and where they fire:
 *  - Banner       : anchored adaptive banner, always on at the bottom of the game screen
 *  - Interstitial : between levels, rate-limited (see [INTERSTITIAL_EVERY_N_LEVELS])
 *  - Rewarded     : opt-in only -- extra glass, hint, and "undo" refills
 *  - App open     : handled by [AppOpenAdManager], shown on return to foreground
 *
 * Nothing loads until the UMP consent flow says we may request ads.
 */
object AdsManager {

    private const val TAG = "AdsManager"
    private const val INTERSTITIAL_EVERY_N_LEVELS = 3
    private const val FIRST_INTERSTITIAL_AT_LEVEL = 4
    private const val MIN_MS_BETWEEN_INTERSTITIALS = 65_000L
    private const val MAX_RETRIES = 5

    private val main = Handler(Looper.getMainLooper())

    @Volatile private var initialised = false
    @Volatile var adsAllowed = false
        private set

    /** True while any full-screen format owns the screen; stops two ads colliding. */
    @Volatile var isShowingFullScreenAd = false
        internal set

    private var interstitial: InterstitialAd? = null
    private var interstitialLoading = false
    private var interstitialRetries = 0
    private var lastInterstitialAt = 0L
    private var levelsSinceInterstitial = 0

    private var rewarded: RewardedAd? = null
    private var rewardedLoading = false
    private var rewardedRetries = 0

    // ---------------------------------------------------------------------------
    // init
    // ---------------------------------------------------------------------------

    fun initialize(context: Context, consentGranted: Boolean) {
        adsAllowed = consentGranted
        if (!consentGranted || initialised) return
        initialised = true

        MobileAds.setRequestConfiguration(
            RequestConfiguration.Builder()
                // The theme references alcohol, so keep the ads served next to it in the
                // same bracket -- and keep Play's content rating consistent with it.
                .setMaxAdContentRating(RequestConfiguration.MAX_AD_CONTENT_RATING_MA)
                .setTagForChildDirectedTreatment(
                    RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_FALSE
                )
                .setTagForUnderAgeOfConsent(RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_FALSE)
                .setTestDeviceIds(TEST_DEVICE_IDS)
                .build()
        )

        // Initialisation does disk + network work; keep it off the UI thread.
        Thread {
            MobileAds.initialize(context.applicationContext) {
                main.post {
                    Log.i(TAG, "MobileAds initialised")
                    preload(context)
                }
            }
        }.start()
    }

    fun preload(context: Context) {
        loadInterstitial(context)
        loadRewarded(context)
    }

    private fun request(): AdRequest = AdRequest.Builder().build()

    // ---------------------------------------------------------------------------
    // Banner -- anchored adaptive, the size Google recommends for a bottom bar
    // ---------------------------------------------------------------------------

    fun attachBanner(activity: Activity, container: FrameLayout) {
        if (!adsAllowed) return
        container.removeAllViews()

        val adView = AdView(activity)
        adView.adUnitId = BuildConfig.AD_BANNER
        adView.setAdSize(adaptiveSize(activity, container))
        adView.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        adView.adListener = object : AdListener() {
            override fun onAdFailedToLoad(error: LoadAdError) {
                Log.w(TAG, "banner failed: ${error.code} ${error.message}")
            }
        }
        container.addView(adView)
        adView.loadAd(request())
    }

    private fun adaptiveSize(activity: Activity, container: FrameLayout): AdSize {
        val density = activity.resources.displayMetrics.density
        var widthPx = container.width.toFloat()
        if (widthPx <= 0f) widthPx = activity.resources.displayMetrics.widthPixels.toFloat()
        val widthDp = (widthPx / density).toInt()
        return AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(activity, widthDp)
    }

    fun destroyBanner(container: FrameLayout) {
        for (i in 0 until container.childCount) {
            (container.getChildAt(i) as? AdView)?.destroy()
        }
        container.removeAllViews()
    }

    fun pauseBanner(container: FrameLayout) {
        for (i in 0 until container.childCount) (container.getChildAt(i) as? AdView)?.pause()
    }

    fun resumeBanner(container: FrameLayout) {
        for (i in 0 until container.childCount) (container.getChildAt(i) as? AdView)?.resume()
    }

    // ---------------------------------------------------------------------------
    // Interstitial
    // ---------------------------------------------------------------------------

    private fun loadInterstitial(context: Context) {
        if (!adsAllowed || interstitialLoading || interstitial != null) return
        interstitialLoading = true

        InterstitialAd.load(
            context.applicationContext,
            BuildConfig.AD_INTERSTITIAL,
            request(),
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitial = ad
                    interstitialLoading = false
                    interstitialRetries = 0
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitial = null
                    interstitialLoading = false
                    Log.w(TAG, "interstitial failed: ${error.code} ${error.message}")
                    retry(interstitialRetries++) { loadInterstitial(context) }
                }
            }
        )
    }

    /** Tells the caller whether this level boundary is an interstitial slot. */
    fun shouldShowInterstitial(level: Int): Boolean {
        if (!adsAllowed) return false
        if (level < FIRST_INTERSTITIAL_AT_LEVEL) return false
        if (SystemClock.elapsedRealtime() - lastInterstitialAt < MIN_MS_BETWEEN_INTERSTITIALS) return false
        return levelsSinceInterstitial + 1 >= INTERSTITIAL_EVERY_N_LEVELS
    }

    fun onLevelCompleted() { levelsSinceInterstitial++ }

    /**
     * Shows the interstitial when one is ready and the cap allows it, otherwise calls
     * [onDone] straight away. The callback always fires exactly once, so the caller can
     * simply advance to the next level from it.
     */
    fun maybeShowInterstitial(activity: Activity, level: Int, onDone: () -> Unit) {
        val ad = interstitial
        if (ad == null || !shouldShowInterstitial(level)) {
            loadInterstitial(activity)
            onDone()
            return
        }

        var finished = false
        val finish = {
            if (!finished) { finished = true; isShowingFullScreenAd = false; onDone() }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdDismissedFullScreenContent() {
                interstitial = null
                lastInterstitialAt = SystemClock.elapsedRealtime()
                levelsSinceInterstitial = 0
                loadInterstitial(activity)
                finish()
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "interstitial show failed: ${error.message}")
                interstitial = null
                loadInterstitial(activity)
                finish()
            }

            override fun onAdShowedFullScreenContent() { isShowingFullScreenAd = true }
        }
        ad.show(activity)
    }

    // ---------------------------------------------------------------------------
    // Rewarded -- always opt-in, always gives the reward before the caller continues
    // ---------------------------------------------------------------------------

    private fun loadRewarded(context: Context) {
        if (!adsAllowed || rewardedLoading || rewarded != null) return
        rewardedLoading = true

        RewardedAd.load(
            context.applicationContext,
            BuildConfig.AD_REWARDED,
            request(),
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewarded = ad
                    rewardedLoading = false
                    rewardedRetries = 0
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewarded = null
                    rewardedLoading = false
                    Log.w(TAG, "rewarded failed: ${error.code} ${error.message}")
                    retry(rewardedRetries++) { loadRewarded(context) }
                }
            }
        )
    }

    val isRewardedReady: Boolean get() = rewarded != null

    /**
     * [onResult] receives true only when the user actually earned the reward.
     * When no ad is available it fires false immediately so the UI can explain why.
     */
    fun showRewarded(activity: Activity, onResult: (Boolean) -> Unit) {
        val ad = rewarded
        if (ad == null) {
            loadRewarded(activity)
            onResult(false)
            return
        }

        var earned = false
        var delivered = false
        val deliver = { value: Boolean ->
            if (!delivered) { delivered = true; isShowingFullScreenAd = false; onResult(value) }
        }

        ad.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdShowedFullScreenContent() { isShowingFullScreenAd = true }

            override fun onAdDismissedFullScreenContent() {
                rewarded = null
                loadRewarded(activity)
                deliver(earned)
            }

            override fun onAdFailedToShowFullScreenContent(error: AdError) {
                Log.w(TAG, "rewarded show failed: ${error.message}")
                rewarded = null
                loadRewarded(activity)
                deliver(false)
            }
        }
        ad.show(activity) { earned = true }
    }

    // ---------------------------------------------------------------------------

    /** Exponential backoff: 2s, 4s, 8s ... capped, then give up until the next trigger. */
    private fun retry(attempt: Int, block: () -> Unit) {
        if (attempt >= MAX_RETRIES) return
        val delay = 2000L shl attempt.coerceAtMost(4)
        main.postDelayed(block, delay)
    }

    /**
     * Put YOUR device's id here while testing so you see test ads on a release build too.
     * Run once and copy the hash the SDK prints: "Use RequestConfiguration.Builder
     * .setTestDeviceIds(Arrays.asList("33BE2250B43518CCDA7DE426D04EE231"))".
     */
    private val TEST_DEVICE_IDS = listOf<String>(
        // "33BE2250B43518CCDA7DE426D04EE231",
    )
}
