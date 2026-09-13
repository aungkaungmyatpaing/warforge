package com.naymyo.warforge.update

import android.content.Context
import android.util.Log
import com.naymyo.warforge.BuildConfig
import java.net.HttpURLConnection
import java.net.URL
import javax.net.ssl.HttpsURLConnection

/**
 * Fetches the update manifest from GitHub and remembers what it found.
 *
 * There is no backend. The manifest is a file in the repository, served by
 * `raw.githubusercontent.com`, which is a CDN - so this scales to any number of players
 * without a rate limit, unlike the GitHub REST API, which allows sixty unauthenticated
 * requests an hour *per IP address* and would start failing the moment two players shared
 * a carrier NAT.
 *
 * Everything here fails silently. A player with no signal, a player behind a captive
 * portal, and a player whose copy of the manifest is malformed all get the same result:
 * the game starts normally and nothing is said.
 */
object UpdateChecker {

    private const val PREFS = "warforge_update"
    private const val KEY_SKIPPED = "skipped_version"
    private const val KEY_PROMPTED_AT = "prompted_at"
    private const val KEY_CACHED = "cached_manifest"
    private const val KEY_FETCHED_AT = "fetched_at"

    /** How long a fetched manifest is good for. */
    private const val CACHE_HOURS = 6

    private const val TIMEOUT_MS = 8_000

    /**
     * Looks for an update and calls [onResult] on a worker thread.
     *
     * Uses the cached manifest when it is recent, so opening the game repeatedly in an
     * afternoon costs one request, not one per launch.
     */
    fun check(context: Context, onResult: (UpdateDecision) -> Unit) {
        val url = BuildConfig.UPDATE_MANIFEST_URL
        if (!url.startsWith("https://")) return          // not configured yet
        Thread {
            try {
                onResult(decide(context, url))
            } catch (e: Exception) {
                Log.d("Warforge", "update check failed", e)
            }
        }.apply { isDaemon = true; name = "update-check" }.start()
    }

    private fun decide(context: Context, url: String): UpdateDecision {
        val prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val now = System.currentTimeMillis()
        val age = now - prefs.getLong(KEY_FETCHED_AT, 0L)
        val fresh = age in 0 until CACHE_HOURS * 60L * 60L * 1000L

        val json = if (fresh) prefs.getString(KEY_CACHED, null) else fetch(url)?.also {
            prefs.edit().putString(KEY_CACHED, it).putLong(KEY_FETCHED_AT, now).apply()
        } ?: prefs.getString(KEY_CACHED, null)           // offline: fall back to the cache

        return decideUpdate(
            currentVersionCode = BuildConfig.VERSION_CODE,
            manifest = json?.let(UpdateManifest::parse),
            state = UpdateState(
                skippedVersionCode = prefs.getInt(KEY_SKIPPED, 0),
                lastPromptedAt = prefs.getLong(KEY_PROMPTED_AT, 0L),
            ),
            now = now,
        )
    }

    /** One GET. Returns null on anything other than a 200 with a plausible body. */
    private fun fetch(url: String): String? {
        var connection: HttpURLConnection? = null
        return try {
            connection = (URL(url).openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                instanceFollowRedirects = true
                requestMethod = "GET"
                setRequestProperty("Accept", "application/json")
                setRequestProperty("User-Agent", "Warforge/${BuildConfig.VERSION_NAME}")
            }
            // Plain HTTP would let anyone on the network hand the player a manifest that
            // points somewhere else, and the prompt is a link the player is invited to tap.
            if (connection !is HttpsURLConnection) return null
            if (connection.responseCode != 200) return null
            connection.inputStream.bufferedReader().use { it.readText() }.take(16_384)
        } catch (e: Exception) {
            Log.d("Warforge", "manifest fetch failed", e)
            null
        } finally {
            connection?.disconnect()
        }
    }

    /** Records that a prompt was shown, so the player is not asked again today. */
    fun markPrompted(context: Context) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putLong(KEY_PROMPTED_AT, System.currentTimeMillis()).apply()
    }

    /** Records that the player never wants to hear about this version again. */
    fun skip(context: Context, versionCode: Int) {
        context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putInt(KEY_SKIPPED, versionCode).apply()
    }
}
