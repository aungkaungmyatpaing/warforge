package com.naymyo.warforge.ads

import android.app.Activity
import android.util.Log
import com.google.android.ump.ConsentDebugSettings
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import com.naymyo.warforge.BuildConfig

/**
 * GDPR / UMP consent gate.
 *
 * AdMob will not serve personalised ads in the EEA/UK until the user has answered the
 * consent form, and Google requires the form to be wired up before you can monetise
 * there at all. Nothing else in the app touches the ads SDK until [ensureConsent]
 * reports that ads may be requested.
 */
class ConsentManager(private val app: android.content.Context) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(app)

    /** True once Google says we are allowed to request ads for this user. */
    val canRequestAds: Boolean get() = consentInformation.canRequestAds()

    /** True when the user must be offered a "Privacy options" entry point in-app. */
    val isPrivacyOptionsRequired: Boolean
        get() = consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED

    /**
     * Asks the UMP SDK for the user's consent status, shows the form when one is
     * required, then calls [onReady] with whether ads can be requested.
     * Safe to call on every cold start -- the SDK caches the answer.
     */
    fun ensureConsent(activity: Activity, onReady: (Boolean) -> Unit) {
        val paramsBuilder = ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)

        // To rehearse the EEA form outside the EEA, put your device's hashed id in
        // DEBUG_DEVICE_HASHES. The SDK prints it to logcat on the first run as
        // "Use ConsentDebugSettings.Builder().addTestDeviceHashedId("ABC123")".
        // Debug geography is ignored on any device not in that list, so leaving the
        // list empty simply means the real geography applies.
        if (BuildConfig.DEBUG && DEBUG_DEVICE_HASHES.isNotEmpty()) {
            val debug = ConsentDebugSettings.Builder(app)
                .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
            DEBUG_DEVICE_HASHES.forEach(debug::addTestDeviceHashedId)
            paramsBuilder.setConsentDebugSettings(debug.build())
        }

        consentInformation.requestConsentInfoUpdate(
            activity,
            paramsBuilder.build(),
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "consent form: ${formError.errorCode} ${formError.message}")
                    }
                    val allowed = consentInformation.canRequestAds()
                    Log.i(TAG, "consent settled, canRequestAds=$allowed")
                    onReady(allowed)
                }
            },
            { requestError ->
                Log.w(TAG, "consent update failed: ${requestError.message}")
                // Do not block the game on a consent-network failure; the SDK keeps the
                // user in a non-personalised state until it succeeds.
                onReady(consentInformation.canRequestAds())
            }
        )
    }

    /** Re-opens the privacy form from a settings button. */
    fun showPrivacyOptions(activity: Activity, onDone: (String?) -> Unit = {}) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity) { error ->
            onDone(error?.message)
        }
    }

    fun reset() = consentInformation.reset()

    private companion object {
        const val TAG = "ConsentManager"

        /** Hashed device ids that should always see the EEA form in debug builds. */
        val DEBUG_DEVICE_HASHES = listOf<String>(
            // "33BE2250B43518CCDA7DE426D04EE231",
        )
    }
}
