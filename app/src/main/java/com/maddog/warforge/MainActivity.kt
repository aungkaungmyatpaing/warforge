package com.maddog.warforge

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.net.toUri
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.maddog.warforge.data.Catalog
import com.maddog.warforge.data.Era
import com.maddog.warforge.data.Progress
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.databinding.ActivityMainBinding
import com.maddog.warforge.databinding.DialogSettingsBinding
import com.maddog.warforge.databinding.ItemVehicleBinding
import com.maddog.warforge.ui.AdHostActivity
import com.maddog.warforge.ui.BuildActivity
import com.maddog.warforge.ui.Music
import com.maddog.warforge.ui.MuseumActivity
import com.maddog.warforge.ui.UpdatePrompt
import com.maddog.warforge.update.UpdateChecker

/** Published alongside the Play listing; linked from the settings sheet. */
private const val PRIVACY_POLICY = "https://aungkaungmyatpaing.github.io/warforge/"

/** The hangar: pick an era, pick a vehicle, start building. */
class MainActivity : AdHostActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var progress: Progress
    private var era: Era = Era.WWI

    override val adContainer: FrameLayout? get() = binding.adSlot

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        progress = Progress(this)
        era = progress.nextVehicle().era

        buildEraTabs()
        binding.settings.setOnClickListener { showSettings() }
        binding.continueBtn.setOnClickListener { open(progress.nextVehicle()) }

        // Asked once per launch and answered on a worker thread; the result arrives
        // whenever it arrives, and if it never does nothing happens.
        UpdateChecker.check(this) { decision ->
            runOnUiThread { UpdatePrompt.show(this, decision) }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.coins.text = progress.coins.toString()
        binding.subtitle.text = getString(
            R.string.tagline
        ) + "  ·  ${progress.builtCount}/${Catalog.campaign.size}"
        buildEraTabs()
        buildGrid()
    }

    // ----------------------------------------------------------------------

    private fun buildEraTabs() {
        val tabs = binding.eraTabs
        tabs.removeAllViews()
        for (e in Catalog.eras()) {
            val chip = TextView(this).apply {
                text = "${e.label}  ${e.years}"
                textSize = 12f
                setTextColor(
                    if (e == era) 0xFF1A1206.toInt() else getColor(R.color.ink_dim)
                )
                setBackgroundResource(R.drawable.bg_chip)
                isSelected = e == era
                setPadding(dp(16), dp(8), dp(16), dp(8))
                setOnClickListener { era = e; buildEraTabs(); buildGrid() }
            }
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,
            ).apply { marginEnd = dp(8) }
            tabs.addView(chip, lp)
        }
    }

    /** Two cards per row; 21 vehicles is small enough not to need a RecyclerView. */
    private fun buildGrid() {
        val grid = binding.grid
        grid.removeAllViews()
        val vehicles = Catalog.inEra(era)
        var row: LinearLayout? = null
        for ((i, v) in vehicles.withIndex()) {
            if (i % 2 == 0) {
                row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
                grid.addView(
                    row,
                    LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                    ).apply { bottomMargin = dp(10) },
                )
            }
            val card = cardFor(v)
            val lp = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { if (i % 2 == 0) marginEnd = dp(10) }
            row?.addView(card, lp)
        }
        // Keep a lone card on the last row at half width rather than stretched.
        if (vehicles.size % 2 == 1) {
            row?.addView(
                View(this),
                LinearLayout.LayoutParams(0, 1, 1f),
            )
        }
    }

    private fun cardFor(v: VehicleDef): View {
        val item = ItemVehicleBinding.inflate(LayoutInflater.from(this))
        val unlocked = progress.isUnlocked(v.id)
        item.root.setBackgroundResource(
            if (unlocked) R.drawable.bg_card else R.drawable.bg_card_locked
        )
        item.thumb.locked = !unlocked
        item.thumb.vehicle = v
        item.lock.visibility = if (unlocked) View.GONE else View.VISIBLE
        item.branchTag.text = v.branch.label.uppercase()
        item.name.text = if (unlocked) v.name else getString(R.string.locked)
        item.meta.text = if (unlocked) "${v.country} · ${v.year} · ${v.partCount}p" else "· · ·"

        item.stars.removeAllViews()
        val earned = progress.stars(v.id)
        for (s in 1..3) {
            val star = ImageView(this).apply {
                setImageResource(R.drawable.ic_star)
                imageTintList = android.content.res.ColorStateList.valueOf(
                    getColor(if (s <= earned) R.color.star_on else R.color.star_off)
                )
            }
            item.stars.addView(star, LinearLayout.LayoutParams(dp(14), dp(14))
                .apply { marginEnd = dp(3) })
        }

        if (unlocked) item.root.setOnClickListener { open(v) }
        // The museum opens for every vehicle, locked or not: you should be able to study
        // a machine before you have earned the right to build it.
        item.museumBtn.visibility = View.VISIBLE
        item.museumBtn.text = if (v.hasCutaway) "3D ·" + "\u00A0X-RAY" else "3D"
        item.museumBtn.setOnClickListener { startActivity(MuseumActivity.intent(this, v.id)) }
        return item.root
    }

    private fun open(v: VehicleDef) {
        startActivity(
            Intent(this, BuildActivity::class.java)
                .putExtra(BuildActivity.EXTRA_VEHICLE, v.id)
        )
    }

    private fun showSettings() {
        val sheet = BottomSheetDialog(this)
        val b = DialogSettingsBinding.inflate(layoutInflater)
        b.soundSwitch.isChecked = progress.soundEnabled
        b.musicSwitch.isChecked = progress.musicEnabled
        b.assistSwitch.isChecked = progress.assistEnabled
        b.soundSwitch.setOnCheckedChangeListener { _, on -> progress.soundEnabled = on }
        b.musicSwitch.setOnCheckedChangeListener { _, on ->
            progress.musicEnabled = on
            Music.enabled = on
        }
        b.assistSwitch.setOnCheckedChangeListener { _, on -> progress.assistEnabled = on }

        // Reopening the consent form has to be possible for as long as the app is
        // installed, wherever a form was shown in the first place. The UMP SDK decides
        // whether that applies to this user, so the row appears only when it says so.
        b.privacyOptions.isVisible = consent.isPrivacyOptionsRequired
        b.privacyOptions.setOnClickListener {
            consent.showPrivacyOptions(this) { error ->
                if (error != null) Log.w("Warforge", "privacy options form: $error")
            }
        }
        b.privacyPolicy.setOnClickListener {
            try {
                startActivity(Intent(Intent.ACTION_VIEW, PRIVACY_POLICY.toUri()))
            } catch (e: ActivityNotFoundException) {
                // No browser installed; nothing sensible to fall back to.
            }
        }

        b.closeBtn.setOnClickListener { sheet.dismiss() }
        sheet.setContentView(b.root)
        sheet.show()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
