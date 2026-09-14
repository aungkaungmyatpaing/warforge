package com.maddog.warforge.update

/** What, if anything, to say to the player about updating. */
sealed interface UpdateDecision {

    /** Nothing to do: already current, skipped, or asked too recently. */
    data object Silent : UpdateDecision

    /** A newer build exists. The player may dismiss or skip it. */
    data class Available(val manifest: UpdateManifest) : UpdateDecision

    /** This build is older than the manifest allows. The prompt cannot be dismissed. */
    data class Required(val manifest: UpdateManifest) : UpdateDecision
}

/**
 * The state the app remembers between checks.
 *
 * @param skippedVersionCode a version the player chose to skip, and must never be asked
 *   about again.
 * @param lastPromptedAt when a prompt was last shown, in epoch milliseconds.
 */
class UpdateState(
    val skippedVersionCode: Int = 0,
    val lastPromptedAt: Long = 0L,
)

/**
 * Decides whether to prompt. Pure, so the whole policy is testable without a network,
 * a clock, or an Activity - which is the only way this kind of code stays correct,
 * because in production it runs once a day on somebody else's phone and is never seen.
 *
 * @param currentVersionCode this build, from `BuildConfig.VERSION_CODE`.
 * @param quietHours how long to leave the player alone after an optional prompt.
 */
fun decideUpdate(
    currentVersionCode: Int,
    manifest: UpdateManifest?,
    state: UpdateState,
    now: Long,
    quietHours: Int = 24,
): UpdateDecision {
    if (manifest == null) return UpdateDecision.Silent

    // A forced update outranks everything: not the skip list, not the quiet period.
    // Nothing else in here is allowed to leave a player stuck on a build that cannot work.
    if (currentVersionCode < manifest.minSupportedVersionCode) {
        return UpdateDecision.Required(manifest)
    }
    if (currentVersionCode >= manifest.latestVersionCode) return UpdateDecision.Silent
    if (state.skippedVersionCode >= manifest.latestVersionCode) return UpdateDecision.Silent

    val quiet = quietHours.toLong() * 60L * 60L * 1000L
    // A clock that has gone backwards - a manual time change, or a fresh install with a
    // stale preference - should shorten the wait, never lengthen it forever.
    val since = now - state.lastPromptedAt
    if (state.lastPromptedAt > 0L && since in 0 until quiet) return UpdateDecision.Silent

    return UpdateDecision.Available(manifest)
}
