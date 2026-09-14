package com.maddog.warforge.ui

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AlertDialog
import com.maddog.warforge.R
import com.maddog.warforge.update.UpdateChecker
import com.maddog.warforge.update.UpdateDecision
import com.maddog.warforge.update.UpdateManifest

/**
 * Tells the player a new version is out, and sends them to the store.
 *
 * The app never downloads or installs anything itself - it opens the Play listing and
 * stops there. Side-loading an APK the game fetched for itself would mean asking the
 * player to turn off the protection that stops other apps doing the same thing.
 */
object UpdatePrompt {

    /** Shows the right dialog for [decision], or nothing at all. */
    fun show(activity: Activity, decision: UpdateDecision) {
        if (activity.isFinishing || activity.isDestroyed) return
        when (decision) {
            is UpdateDecision.Silent -> Unit
            is UpdateDecision.Available -> optional(activity, decision.manifest)
            is UpdateDecision.Required -> required(activity, decision.manifest)
        }
    }

    private fun optional(activity: Activity, manifest: UpdateManifest) {
        UpdateChecker.markPrompted(activity)
        AlertDialog.Builder(activity)
            .setTitle(activity.getString(R.string.update_title, manifest.latestVersionName))
            .setMessage(notes(activity, manifest, R.string.update_body))
            .setPositiveButton(R.string.update_now) { _, _ -> openStore(activity, manifest) }
            .setNegativeButton(R.string.update_later, null)
            .setNeutralButton(R.string.update_skip) { _, _ ->
                UpdateChecker.skip(activity, manifest.latestVersionCode)
            }
            .show()
    }

    private fun required(activity: Activity, manifest: UpdateManifest) {
        AlertDialog.Builder(activity)
            .setTitle(R.string.update_required_title)
            .setMessage(notes(activity, manifest, R.string.update_required_body))
            // No dismiss and no cancel button: this build is one the manifest has
            // declared unusable, so "later" is not an option we can honestly offer.
            .setCancelable(false)
            .setPositiveButton(R.string.update_now) { _, _ ->
                openStore(activity, manifest)
                activity.finish()
            }
            .show()
    }

    /** The release notes, or a generic line when the manifest does not supply any. */
    private fun notes(activity: Activity, manifest: UpdateManifest, fallback: Int): String =
        manifest.notes.ifBlank { activity.getString(fallback, manifest.latestVersionName) }

    /**
     * Opens the Play app if it is installed, and the web listing if it is not.
     *
     * The URL comes from the manifest rather than from the build, so a listing that moves
     * - a new package id, a different store - is a one-line change in a file on GitHub
     * rather than an app update that the players who need it most cannot receive.
     */
    private fun openStore(activity: Activity, manifest: UpdateManifest) {
        val pkg = manifest.playPackage
        if (pkg != null) {
            try {
                activity.startActivity(
                    Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$pkg"))
                        .setPackage("com.android.vending")
                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                )
                return
            } catch (e: ActivityNotFoundException) {
                // No Play Store on this device; fall through to the browser.
            }
        }
        val web = manifest.url.takeIf { it.startsWith("https://") } ?: return
        try {
            activity.startActivity(
                Intent(Intent.ACTION_VIEW, Uri.parse(web))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            // No browser either. Nothing sensible left to do.
        }
    }
}
