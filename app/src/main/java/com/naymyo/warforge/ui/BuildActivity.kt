package com.naymyo.warforge.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import com.naymyo.warforge.R
import com.naymyo.warforge.ads.AdsManager
import com.naymyo.warforge.data.Catalog
import com.naymyo.warforge.data.Progress
import com.naymyo.warforge.data.VehicleDef
import com.naymyo.warforge.databinding.ActivityBuildBinding
import com.naymyo.warforge.game.LevelState

/** One build: the blueprint, the tray, and the victory card. */
class BuildActivity : AdHostActivity() {

    private lateinit var binding: ActivityBuildBinding
    private lateinit var progress: Progress
    private lateinit var vehicle: VehicleDef
    private lateinit var level: LevelState
    private lateinit var sfx: Sfx

    override val adContainer: FrameLayout? get() = binding.adSlot

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBuildBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = Progress(this)
        sfx = Sfx(this).apply { enabled = progress.soundEnabled }

        val id = intent.getStringExtra(EXTRA_VEHICLE)
        vehicle = id?.let(Catalog::byId) ?: progress.nextVehicle()

        binding.back.setOnClickListener { finish() }
        binding.restart.setOnClickListener { startLevel() }
        binding.hintBtn.setOnClickListener { useHint() }
        binding.hintLabel.text = getString(R.string.hint_cost, HINT_COST)

        binding.boardView.onPlaced = { item ->
            sfx.snap()
            binding.partLabel.text = item.name
            showPart(item)
            updateProgress()
        }
        binding.boardView.onMistake = {
            sfx.error()
            binding.partLabel.text = getString(R.string.drag_hint)
        }
        binding.partCard.setOnClickListener { binding.partCard.visibility = View.GONE }
        binding.partScroll.maxHeightPx =
            (resources.displayMetrics.heightPixels * 0.18f).toInt()
        binding.boardView.onSelected = { item ->
            binding.partLabel.text =
                listOf(item.name, item.slot.detail).filter { it.isNotBlank() }.joinToString("  ·  ")
            showPart(item)
        }
        binding.boardView.onStageChanged = { stage ->
            sfx.lock()
            binding.partCard.visibility = View.GONE
            binding.partLabel.text = stage?.let { "${it.label} — ${it.hint}" }
                ?: getString(R.string.drag_hint)
        }
        binding.boardView.onComplete = { finishLevel() }

        binding.victory.victoryRoot.visibility = View.GONE
        startLevel()
    }

    // ----------------------------------------------------------------------

    /**
     * Builds the level off the main thread. Working out where every slot sits means
     * tessellating every part and module, which is far too slow to do inline.
     */
    private fun startLevel() {
        binding.victory.victoryRoot.visibility = View.GONE
        binding.partLabel.setText(R.string.loading_model)
        binding.progress.max = 100
        binding.progress.progress = 0
        val target = vehicle
        Thread({
            val state = LevelState(target, Catalog.decoysFor(target, target.decoys))
            runOnUiThread {
                if (isFinishing || isDestroyed || vehicle !== target) return@runOnUiThread
                level = state
                binding.boardView.showGhosts = progress.assistEnabled
                binding.boardView.setLevel(state)
                binding.partLabel.setText(R.string.drag_hint)
                updateProgress()
            }
        }, "warforge-level").apply { isDaemon = true }.start()
    }

    /** Shows what a piece is and why it sits where it does. */
    private fun showPart(item: LevelState.Item) {
        val slot = item.slot
        if (item.decoy) {
            // Naming a decoy would give the game away.
            binding.partCard.visibility = View.GONE
            return
        }
        binding.partName.text = slot.name
        binding.partDetail.text = slot.detail
        binding.partDetail.visibility = if (slot.detail.isBlank()) View.GONE else View.VISIBLE
        binding.partInfo.text = slot.info
        binding.partInfo.visibility = if (slot.info.isBlank()) View.GONE else View.VISIBLE
        binding.partCard.visibility =
            if (slot.detail.isBlank() && slot.info.isBlank()) View.GONE else View.VISIBLE
        binding.partScroll.scrollTo(0, 0)
    }

    private fun updateProgress() {
        if (::level.isInitialized) binding.progress.progress = (level.progress * 100).toInt()
    }

    private fun useHint() {
        if (!::level.isInitialized || level.isComplete) return
        if (!progress.spend(HINT_COST)) {
            offerRewardedHint()
            return
        }
        level.hintTarget()?.let { binding.boardView.flashHint(it) }
        binding.partLabel.text = level.peekTarget()?.name ?: ""
    }

    /** Out of coins: offer to watch an ad rather than dead-ending the player. */
    private fun offerRewardedHint() {
        if (!AdsManager.isRewardedReady) {
            Toast.makeText(this, R.string.not_enough_coins, Toast.LENGTH_SHORT).show()
            return
        }
        AdsManager.showRewarded(this) { earned ->
            if (earned) {
                progress.coins += HINT_COST * 2
                level.hintTarget()?.let { binding.boardView.flashHint(it) }
            }
        }
    }

    private fun finishLevel() {
        sfx.win()
        val stars = level.stars()
        val reward = level.coinReward()
        val firstTime = !progress.isComplete(vehicle.id)
        progress.record(vehicle.id, stars, reward)
        AdsManager.onLevelCompleted()

        val v = binding.victory
        v.vName.text = vehicle.name
        v.vThumb.vehicle = vehicle
        v.vFact.text = vehicle.fact
        v.vReward.text = "+$reward coins   ·   ${level.mistakes} misplaced   ·   " +
            "${level.totalToFit} pieces"

        v.vStars.removeAllViews()
        for (s in 1..3) {
            val star = ImageView(this).apply {
                setImageResource(R.drawable.ic_star)
                imageTintList = ColorStateList.valueOf(
                    getColor(if (s <= stars) R.color.star_on else R.color.star_off)
                )
            }
            v.vStars.addView(
                star,
                LinearLayout.LayoutParams(dp(30), dp(30)).apply { marginEnd = dp(6) },
            )
        }

        val next = Catalog.campaign.getOrNull(Catalog.indexOf(vehicle.id) + 1)
        if (next != null && firstTime) {
            v.vFact.text = "${vehicle.fact}\n\n" + getString(R.string.unlocked_next, next.name)
        }
        v.vNext.visibility = if (next == null) View.GONE else View.VISIBLE
        v.vNext.setOnClickListener {
            next ?: return@setOnClickListener
            AdsManager.maybeShowInterstitial(this, progress.builtCount) {
                startActivity(
                    Intent(this, BuildActivity::class.java)
                        .putExtra(EXTRA_VEHICLE, next.id)
                )
                finish()
            }
        }
        v.vMuseum.setOnClickListener { startActivity(MuseumActivity.intent(this, vehicle.id)) }
        v.vHangar.setOnClickListener { finish() }
        v.victoryRoot.visibility = View.VISIBLE
        v.victoryRoot.alpha = 0f
        v.victoryRoot.animate().alpha(1f).setDuration(260).start()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    override fun onDestroy() {
        sfx.release()
        super.onDestroy()
    }

    companion object {
        const val EXTRA_VEHICLE = "vehicle_id"
        private const val HINT_COST = 25
    }
}
