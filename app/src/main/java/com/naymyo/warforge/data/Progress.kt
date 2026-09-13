package com.naymyo.warforge.data

import android.content.Context

/**
 * Campaign save state: stars per vehicle, the coin balance that pays for hints, and the
 * unlock chain. Small enough that SharedPreferences is the right tool - there is no
 * schema here worth a database.
 */
class Progress(context: Context) {

    private val prefs = context.applicationContext
        .getSharedPreferences("warforge_progress", Context.MODE_PRIVATE)

    /** 0 = not finished, 1..3 = stars earned on the best run. */
    fun stars(vehicleId: String): Int = prefs.getInt(KEY_STARS + vehicleId, 0)

    fun isComplete(vehicleId: String): Boolean = stars(vehicleId) > 0

    /**
     * Records a finished build. Stars never go down, so replaying for a better run is
     * safe and replaying a mastered vehicle for fun costs nothing.
     */
    fun record(vehicleId: String, stars: Int, coinsEarned: Int) {
        val best = maxOf(stars(vehicleId), stars)
        prefs.edit()
            .putInt(KEY_STARS + vehicleId, best)
            .putInt(KEY_COINS, coins + coinsEarned)
            .apply()
    }

    var coins: Int
        get() = prefs.getInt(KEY_COINS, 0)
        set(value) = prefs.edit().putInt(KEY_COINS, value.coerceAtLeast(0)).apply()

    fun spend(amount: Int): Boolean {
        if (coins < amount) return false
        coins -= amount
        return true
    }

    /**
     * A vehicle opens once the one before it in the campaign is finished. The first two
     * start unlocked so a new player has a choice on the very first screen.
     */
    fun isUnlocked(vehicleId: String): Boolean {
        val i = Catalog.indexOf(vehicleId)
        if (i < 0) return false
        if (i < FREE_AT_START) return true
        return isComplete(Catalog.campaign[i - 1].id)
    }

    /** The vehicle the "Continue" button should drop the player into. */
    fun nextVehicle(): VehicleDef =
        Catalog.campaign.firstOrNull { !isComplete(it.id) && isUnlocked(it.id) }
            ?: Catalog.campaign.first()

    val totalStars: Int get() = Catalog.campaign.sumOf { stars(it.id) }
    val builtCount: Int get() = Catalog.campaign.count { isComplete(it.id) }

    var soundEnabled: Boolean
        get() = prefs.getBoolean(KEY_SOUND, true)
        set(value) = prefs.edit().putBoolean(KEY_SOUND, value).apply()

    var musicEnabled: Boolean
        get() = prefs.getBoolean(KEY_MUSIC, true)
        set(value) = prefs.edit().putBoolean(KEY_MUSIC, value).apply()

    /** Assist mode draws every empty slot as a ghost outline, whatever the era. */
    var assistEnabled: Boolean
        get() = prefs.getBoolean(KEY_ASSIST, true)
        set(value) = prefs.edit().putBoolean(KEY_ASSIST, value).apply()

    private companion object {
        const val KEY_STARS = "stars_"
        const val KEY_COINS = "coins"
        const val KEY_SOUND = "sound"
        const val KEY_MUSIC = "music"
        const val KEY_ASSIST = "assist"
        const val FREE_AT_START = 2
    }
}
