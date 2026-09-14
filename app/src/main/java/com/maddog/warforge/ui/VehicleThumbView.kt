package com.maddog.warforge.ui

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.poly.shade
import kotlin.math.min

/**
 * Draws a finished vehicle scaled to fit - the hangar card art.
 *
 * Locked vehicles render as a flat silhouette instead of being hidden, so the player can
 * see the shape of what they are working toward without being told the details.
 */
class VehicleThumbView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyle: Int = 0,
) : View(context, attrs, defStyle) {

    var vehicle: VehicleDef? = null
        set(value) { field = value; paths = null; invalidate() }

    /** Silhouette mode for locked entries. */
    var locked: Boolean = false
        set(value) { field = value; paths = null; invalidate() }

    private var paths: List<Pair<Path, Int>>? = null
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }

    private fun build(v: VehicleDef): List<Pair<Path, Int>> {
        val out = ArrayList<Pair<Path, Int>>()
        for (part in v.sorted) {
            for (poly in part.polys) {
                val path = Path()
                path.moveTo(poly.pts[0], poly.pts[1])
                var i = 2
                while (i + 1 < poly.pts.size) { path.lineTo(poly.pts[i], poly.pts[i + 1]); i += 2 }
                path.close()
                out += path to if (locked) LOCKED else shade(v.palette.colorFor(poly.role), poly.tone)
            }
        }
        return out
    }

    override fun onDraw(canvas: Canvas) {
        val v = vehicle ?: return
        val prepared = paths ?: build(v).also { paths = it }

        // Fit the vehicle's own bounds rather than the whole blueprint, so a small
        // fighter fills the card as well as a carrier does.
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
        for (p in v.parts) {
            if (p.minX < minX) minX = p.minX
            if (p.minY < minY) minY = p.minY
            if (p.maxX > maxX) maxX = p.maxX
            if (p.maxY > maxY) maxY = p.maxY
        }
        val w = (maxX - minX).coerceAtLeast(1f)
        val h = (maxY - minY).coerceAtLeast(1f)
        val pad = width * 0.05f
        val s = min((width - pad * 2) / w, (height - pad * 2) / h)

        canvas.save()
        canvas.translate(width * 0.5f, height * 0.5f)
        canvas.scale(s, s)
        canvas.translate(-(minX + maxX) * 0.5f, -(minY + maxY) * 0.5f)
        for ((path, color) in prepared) {
            fill.color = color
            canvas.drawPath(path, fill)
        }
        canvas.restore()
    }

    private companion object {
        val LOCKED = Color.parseColor("#2A3744")
    }
}
