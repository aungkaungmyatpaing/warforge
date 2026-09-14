package com.maddog.warforge.ui

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.maddog.warforge.R
import com.maddog.warforge.data.Catalog
import com.maddog.warforge.data.ModuleDef
import com.maddog.warforge.data.Progress
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.databinding.ActivityMuseumBinding
import com.maddog.warforge.gl.Layer
import com.maddog.warforge.gl.SceneNode
import com.maddog.warforge.gl.ViewMode

/**
 * The museum: one vehicle in 3D, openable, with everything known about it underneath.
 *
 * This is where the game stops being a puzzle and starts being a reference - turn the
 * model, slice it open, tap the engine to find out what it was and why it sat where it
 * did, then read the history.
 */
class MuseumActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMuseumBinding
    private lateinit var vehicle: VehicleDef
    private lateinit var progress: Progress

    private enum class Tab { INTERNALS, HISTORY, SPECS, QUIZ }

    private var tab = Tab.INTERNALS

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMuseumBinding.inflate(layoutInflater)
        setContentView(binding.root)
        progress = Progress(this)

        vehicle = intent.getStringExtra(EXTRA_VEHICLE)?.let(Catalog::byId)
            ?: Catalog.campaign.first()

        binding.title.text = "${vehicle.name}  ·  ${vehicle.year}"
        binding.subtitle.text =
            "${vehicle.country} · ${vehicle.era.label} · ${vehicle.branch.label}"
        binding.back.setOnClickListener { finish() }

        // A note runs from one line to a paragraph; cap it at roughly a third of the
        // viewport so it never buries the model it is describing.
        binding.pickScroll.maxHeightPx = (resources.displayMetrics.heightPixels * 0.24f).toInt()
        binding.pickClose.setOnClickListener {
            binding.pickCard.visibility = View.GONE
            binding.viewport.selectedId = null
        }
        binding.gesture.setText(R.string.loading_model)
        binding.viewport.show(vehicle) { binding.gesture.setText(R.string.inspect_hint) }
        binding.viewport.onPicked = ::showPicked

        buildModeChips()
        buildTabs()
        renderPanel()

        binding.cutSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, value: Int, fromUser: Boolean) {
                binding.viewport.cutFraction = cutFor(value)
            }
            override fun onStartTrackingTouch(sb: SeekBar?) = Unit
            override fun onStopTrackingTouch(sb: SeekBar?) = Unit
        })
    }

    override fun onResume() {
        super.onResume()
        Music.resume()
        binding.viewport.onResume()
    }

    override fun onPause() {
        Music.pause()
        binding.viewport.onPause()
        super.onPause()
    }

    // ----------------------------------------------------------------------

    private fun buildModeChips() {
        val row = binding.modeRow
        row.removeAllViews()
        val modes = listOf(
            ViewMode.SOLID to getString(R.string.mode_solid),
            ViewMode.XRAY to getString(R.string.mode_xray),
            ViewMode.CUTAWAY to getString(R.string.mode_cutaway),
        )
        for ((mode, label) in modes) {
            val active = binding.viewport.mode == mode
            row.addView(
                chip(label, active) {
                    binding.viewport.mode = mode
                    if (mode == ViewMode.CUTAWAY) {
                        binding.cutSlider.visibility = View.VISIBLE
                        // Open on the centreline - the section everyone wants to see.
                        binding.cutSlider.progress = 50
                        binding.viewport.cutFraction = cutFor(50)
                    } else {
                        binding.cutSlider.visibility = View.INVISIBLE
                    }
                    buildModeChips()
                },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(6) },
            )
        }
    }

    private fun buildTabs() {
        val row = binding.tabRow
        row.removeAllViews()
        val tabs = listOf(
            Tab.INTERNALS to getString(R.string.tab_internals),
            Tab.HISTORY to getString(R.string.tab_history),
            Tab.SPECS to getString(R.string.tab_specs),
            Tab.QUIZ to getString(R.string.tab_quiz),
        )
        for ((t, label) in tabs) {
            if (t == Tab.QUIZ && vehicle.quiz.isEmpty()) continue
            row.addView(
                chip(label, tab == t) { tab = t; buildTabs(); renderPanel() },
                LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                ).apply { marginEnd = dp(6) },
            )
        }
    }

    private fun chip(label: String, active: Boolean, onClick: () -> Unit): TextView =
        TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(if (active) 0xFF1A1206.toInt() else getColor(R.color.ink_dim))
            setBackgroundResource(R.drawable.bg_chip)
            isSelected = active
            setPadding(dp(14), dp(7), dp(14), dp(7))
            setOnClickListener { onClick() }
        }

    // ----------------------------------------------------------------------

    private fun showPicked(node: SceneNode?) {
        if (node == null) {
            binding.pickCard.visibility = View.GONE
            return
        }
        binding.pickCard.visibility = View.VISIBLE
        binding.pickCategory.text =
            (if (node.layer == Layer.INTERNAL) node.category else "COMPONENT").uppercase()
        binding.pickName.text = node.name
        binding.pickDetail.text = node.detail
        binding.pickDetail.visibility = if (node.detail.isBlank()) View.GONE else View.VISIBLE
        binding.pickInfo.text = node.info.ifBlank { getString(R.string.no_note_yet) }
        binding.pickScroll.scrollTo(0, 0)
    }

    private fun renderPanel() {
        val panel = binding.panel
        panel.removeAllViews()
        when (tab) {
            Tab.INTERNALS -> renderInternals(panel)
            Tab.HISTORY -> renderHistory(panel)
            Tab.SPECS -> renderSpecs(panel)
            Tab.QUIZ -> renderQuiz(panel)
        }
    }

    private fun renderInternals(panel: LinearLayout) {
        if (vehicle.modules.isEmpty()) {
            panel.addView(body(getString(R.string.no_internals)))
            return
        }
        panel.addView(body(getString(R.string.tap_to_inspect)))
        for (module in vehicle.modules) panel.addView(moduleRow(module))
    }

    /** A legend row that doubles as a shortcut: tapping it selects the module in 3D. */
    private fun moduleRow(module: ModuleDef): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setBackgroundResource(R.drawable.bg_card)
            setPadding(dp(10), dp(9), dp(10), dp(9))
            setOnClickListener {
                binding.viewport.selectedId = "mod_" + module.id
                if (binding.viewport.mode == ViewMode.SOLID) {
                    binding.viewport.mode = ViewMode.XRAY
                    buildModeChips()
                }
                showPicked(
                    SceneNode(
                        "mod_" + module.id, module.name, Layer.INTERNAL, module.kind.label,
                        module.detail, module.info, FloatArray(0), 0f, 0f, 0f, 0f, 0f, 0f,
                    )
                )
            }
        }
        val swatch = View(this).apply {
            backgroundTintList = ColorStateList.valueOf(module.kind.palette.body)
            setBackgroundColor(Color.WHITE)
            backgroundTintList = ColorStateList.valueOf(module.kind.palette.body)
        }
        row.addView(swatch, LinearLayout.LayoutParams(dp(10), dp(34)).apply { marginEnd = dp(10) })

        val column = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        column.addView(TextView(this).apply {
            text = module.name
            setTextColor(getColor(R.color.ink))
            textSize = 13f
        })
        column.addView(TextView(this).apply {
            text = listOf(module.kind.label, module.detail)
                .filter { it.isNotBlank() }.joinToString("  ·  ")
            setTextColor(getColor(R.color.ink_dim))
            textSize = 11f
            typeface = android.graphics.Typeface.MONOSPACE
        })
        row.addView(
            column,
            LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f),
        )

        val wrapper = FrameLayout(this)
        wrapper.addView(
            row,
            FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
            ).apply { bottomMargin = dp(7) },
        )
        return wrapper
    }

    private fun renderHistory(panel: LinearLayout) {
        if (vehicle.history.isBlank()) {
            panel.addView(body(vehicle.fact))
            return
        }
        for (para in vehicle.history.trim().split("\n\n")) {
            if (para.startsWith("## ")) {
                panel.addView(heading(para.removePrefix("## ").trim()))
            } else {
                // The source is hard-wrapped to keep the Kotlin readable; a paragraph is
                // one run of text and the view does its own wrapping.
                panel.addView(body(para.trim().replace('\n', ' ').replace(Regex(" +"), " ")))
            }
        }
    }

    private fun renderSpecs(panel: LinearLayout) {
        if (vehicle.specs.isEmpty()) {
            panel.addView(body(vehicle.fact))
            return
        }
        for (spec in vehicle.specs) {
            val row = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(7), 0, dp(7))
            }
            row.addView(TextView(this).apply {
                text = spec.label
                setTextColor(getColor(R.color.ink_dim))
                textSize = 12f
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f))
            row.addView(TextView(this).apply {
                text = spec.value
                setTextColor(getColor(R.color.ink))
                textSize = 12f
                typeface = android.graphics.Typeface.MONOSPACE
                gravity = Gravity.END
            }, LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.15f))
            panel.addView(row)
            panel.addView(View(this).apply {
                setBackgroundColor(getColor(R.color.stroke))
            }, LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, dp(1)))
        }
    }

    private fun renderQuiz(panel: LinearLayout) {
        panel.addView(QuizView(this).apply { setQuestions(vehicle.quiz) })
    }

    private fun heading(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(getColor(R.color.accent))
        textSize = 12f
        letterSpacing = 0.12f
        typeface = android.graphics.Typeface.MONOSPACE
        setPadding(0, dp(14), 0, dp(4))
    }

    private fun body(text: String) = TextView(this).apply {
        this.text = text
        setTextColor(getColor(R.color.ink_dim))
        textSize = 13f
        setLineSpacing(dp(4).toFloat(), 1f)
        setPadding(0, dp(4), 0, dp(8))
    }

    /**
     * Maps the slider onto a useful slice. The bottom of the raw range cuts the whole
     * vehicle away, which tells the player nothing, so the travel starts a fifth of the
     * way in.
     */
    private fun cutFor(progress: Int): Float = 0.2f + (progress / 100f) * 0.8f

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    companion object {
        const val EXTRA_VEHICLE = "vehicle_id"

        fun intent(context: Context, vehicleId: String) =
            Intent(context, MuseumActivity::class.java).putExtra(EXTRA_VEHICLE, vehicleId)
    }
}
