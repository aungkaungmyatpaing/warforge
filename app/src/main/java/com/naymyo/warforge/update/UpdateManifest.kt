package com.naymyo.warforge.update

import org.json.JSONObject

/**
 * What the published update manifest says.
 *
 * The file lives in the project's GitHub repository and is fetched raw over HTTPS, so
 * shipping a new version is a commit and nothing else - there is no server to run, no
 * database, and nothing that can go down and take the game with it.
 *
 * Deliberately tolerant: every field except the version code has a default, because a
 * manifest written by hand months from now should not be able to break an installed app.
 */
class UpdateManifest(
    /** The newest build published to the store. */
    val latestVersionCode: Int,
    /** What that build calls itself, for showing to the player. */
    val latestVersionName: String,
    /**
     * The oldest build still allowed to run.
     *
     * Raising this forces an update rather than suggesting one. It exists for the case
     * where an old build is actively broken - a save format that changed, say - and is
     * meant to stay at 1 almost always.
     */
    val minSupportedVersionCode: Int,
    /** What is new, in English and in Burmese. Either may be blank. */
    val notes: String,
    val notesMy: String,
    /** Where to send the player. Normally the game's Play Store listing. */
    val url: String,
) {
    /**
     * The package id inside a Play Store listing URL, if this is one.
     *
     * Used to open the Play app directly with `market://`; when it is absent the plain
     * URL is opened in a browser instead, which still works.
     */
    val playPackage: String?
        get() = Regex("[?&]id=([A-Za-z0-9_.]+)").find(url)?.groupValues?.get(1)

    companion object {
        /**
         * Parses a manifest, or returns null if it is not one.
         *
         * Never throws. A truncated download, an HTML error page served instead of JSON,
         * or a hand-edited file with a trailing comma all end up here, and the right
         * answer to every one of them is to say nothing and try again tomorrow.
         */
        fun parse(json: String): UpdateManifest? = try {
            val o = JSONObject(json)
            val latest = o.getInt("latestVersionCode")
            if (latest <= 0) null else UpdateManifest(
                latestVersionCode = latest,
                latestVersionName = o.optString("latestVersionName", latest.toString()),
                minSupportedVersionCode = o.optInt("minSupportedVersionCode", 0),
                notes = o.optString("notes", ""),
                notesMy = o.optString("notesMy", ""),
                url = o.optString("url", ""),
            )
        } catch (e: Exception) {
            null
        }
    }
}
