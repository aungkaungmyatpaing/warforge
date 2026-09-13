package com.naymyo.warforge

import com.naymyo.warforge.data.BP_H
import com.naymyo.warforge.data.BP_W
import com.naymyo.warforge.data.Catalog
import com.naymyo.warforge.data.VehicleDef
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Developer tool, not an assertion: writes every vehicle out as SVG under
 * `app/build/preview/` so the hand-authored low-poly geometry can be eyeballed without
 * an emulator. Running it also smoke-tests that every builder completes.
 *
 * `android.graphics.Color` is stubbed to 0 in unit tests, so the tone maths from
 * `poly/Poly.kt` is mirrored here in pure Kotlin.
 */
class PreviewGenerator {

    private fun shadeHex(color: Int, tone: Int): String {
        var r = (color shr 16) and 0xFF
        var g = (color shr 8) and 0xFF
        var b = color and 0xFF
        if (tone != 0) {
            val f = (abs(tone) * 0.075f).coerceAtMost(0.85f)
            val target = if (tone > 0) 255 else 0
            r = (r + (target - r) * f).roundToInt().coerceIn(0, 255)
            g = (g + (target - g) * f).roundToInt().coerceIn(0, 255)
            b = (b + (target - b) * f).roundToInt().coerceIn(0, 255)
        }
        return "#%02X%02X%02X".format(r, g, b)
    }

    private fun body(v: VehicleDef): String = buildString {
        for (part in v.sorted) {
            for (poly in part.polys) {
                val pts = (poly.pts.indices step 2).joinToString(" ") {
                    "${poly.pts[it]},${poly.pts[it + 1]}"
                }
                append("""<polygon points="$pts" fill="${shadeHex(v.palette.colorFor(poly.role), poly.tone)}"/>""")
                append('\n')
            }
        }
    }

    @Test
    fun writeSvgPreviews() {
        val dir = File("build/preview").apply { mkdirs() }

        for (v in Catalog.all) {
            File(dir, "${v.id}.svg").writeText(
                """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${BP_W.toInt()} ${BP_H.toInt()}">
<rect width="100%" height="100%" fill="#0F1720"/>
${body(v)}<text x="16" y="30" fill="#C9D8E4" font-family="monospace" font-size="22">${v.name} (${v.year})</text>
</svg>"""
            )
        }

        // One contact sheet, three columns, so the whole catalogue fits on a screen.
        val cols = 3
        val rows = (Catalog.campaign.size + cols - 1) / cols
        val cellW = BP_W + 30f
        val cellH = BP_H + 60f
        val sheet = buildString {
            append("""<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 ${(cols * cellW).toInt()} ${(rows * cellH).toInt()}">""")
            append("""<rect width="100%" height="100%" fill="#0B1118"/>""")
            for ((i, v) in Catalog.campaign.withIndex()) {
                val x = (i % cols) * cellW + 15f
                val y = (i / cols) * cellH + 15f
                append("""<g transform="translate($x,$y)">""")
                append("""<rect width="${BP_W.toInt()}" height="${BP_H.toInt()}" fill="#0F1720" stroke="#24323F"/>""")
                append(body(v))
                append("""<text x="14" y="${(BP_H + 30).toInt()}" fill="#C9D8E4" font-family="monospace" font-size="26">${v.name}</text>""")
                append("""<text x="14" y="${(BP_H + 56).toInt()}" fill="#7B92A5" font-family="monospace" font-size="20">${v.era.label} · ${v.branch.label} · ${v.country} · ${v.partCount} parts</text>""")
                append("</g>")
            }
            append("</svg>")
        }
        File(dir, "contact-sheet.svg").writeText(sheet)
        println("preview written to ${dir.absolutePath}")

        println("--- solid budget ---")
        var total = 0
        for (v in Catalog.campaign) {
            val parts = v.parts.sumOf { it.mesh.triangleCount }
            val modules = v.modules.sumOf { it.mesh.triangleCount }
            total += parts + modules
            println("%-22s %6d tris  (%d parts + %d internals)".format(v.id, parts + modules, parts, modules))
        }
        println("total %d triangles across %d vehicles".format(total, Catalog.campaign.size))
    }
}
