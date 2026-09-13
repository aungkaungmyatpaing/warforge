package com.naymyo.warforge

import com.naymyo.warforge.data.Catalog
import com.naymyo.warforge.data.ModuleKind
import com.naymyo.warforge.data.VehicleDef
import com.naymyo.warforge.poly.Palette
import com.naymyo.warforge.poly.Role
import com.naymyo.warforge.solid.AmbientOcclusion
import com.naymyo.warforge.solid.GlbLoader
import com.naymyo.warforge.solid.Mesh
import org.junit.Test
import java.io.File
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Developer tool: renders the 3D models on the JVM and writes them as PNGs.
 *
 * It is a deliberate re-implementation of `gl/Shaders.kt` and `gl/OrbitCamera.kt` in
 * plain Kotlin - same camera fit, same three-light rig, same Blinn-Phong and rim terms,
 * same filmic shoulder. Keeping a second copy of the shading maths is a real cost, and
 * it buys a look at the models in seconds instead of a five-minute round trip through an
 * emulator that cannot render them at any useful speed. When the shader changes, this
 * has to change with it.
 */
class Render3DPreview {

    private val width = 900
    private val height = 560

    @Test
    fun renderVehicles() {
        val dir = File("build/render3d").apply { mkdirs() }
        for (vehicle in Catalog.campaign) {
            val surface = render(vehicle)
            PngWriter.write(File(dir, "${vehicle.id}.png"), surface.w, surface.h, surface.toRgb())
        }
        // A close pass on everything modelled in Blender, where the surface detail is
        // actually legible - those are the ones being worked on.
        for (v in Catalog.campaign.filter { it.model != null }) {
            val close = render(v, 1100, 700, zoom = 0.62f, elevation = 0.22f)
            PngWriter.write(File(dir, "${v.id}-close.png"), close.w, close.h, close.toRgb())
        }
        // And an X-ray pass wherever the internals have been modelled, because nothing
        // else in the pipeline ever shows them - they live inside a closed hull.
        for (v in Catalog.campaign.filter { it.model?.internals != null }) {
            val x = render(v, 1100, 700, zoom = 0.62f, elevation = 0.26f, internals = true)
            PngWriter.write(File(dir, "${v.id}-xray.png"), x.w, x.h, x.toRgb())
        }
        contactSheet(dir)
        println("3D previews written to ${dir.absolutePath}")
    }

    // ----------------------------------------------------------------------

    private class Surface(val w: Int, val h: Int) {
        val color = FloatArray(w * h * 3)
        val depth = FloatArray(w * h) { Float.MAX_VALUE }

        fun background() {
            for (y in 0 until h) {
                val t = y.toFloat() / h
                // Matches Backdrop: dark at top and bottom, lifted behind the model.
                val v = when {
                    t < 0.375f -> lerp(0.045f, 0.086f, t / 0.375f)
                    t < 0.675f -> lerp(0.086f, 0.092f, (t - 0.375f) / 0.3f)
                    else -> lerp(0.092f, 0.038f, (t - 0.675f) / 0.325f)
                }
                for (x in 0 until w) {
                    val i = (y * w + x) * 3
                    color[i] = v * 0.85f
                    color[i + 1] = v * 1.06f
                    color[i + 2] = v * 1.34f
                }
            }
        }

        fun toRgb(): ByteArray {
            val out = ByteArray(w * h * 3)
            for (i in out.indices) out[i] = channel(color[i])
            return out
        }

        private fun channel(v: Float) = (v.coerceIn(0f, 1f) * 255f).roundToInt().toByte()
    }

    private fun render(
        vehicle: VehicleDef, w: Int = width, h: Int = height,
        zoom: Float = 1f, azimuth: Float = 0.72f, elevation: Float = 0.28f,
        /** Draw the X-ray modules instead of the shell, each in its own kind colour. */
        internals: Boolean = false,
    ): Surface {
        val surface = Surface(w, h)
        surface.background()

        // Exactly what the app will draw: a Blender model supersedes the extruded
        // blueprint part by part, so the preview has to make the same choice.
        val model = vehicle.model?.let { source ->
            val file = File("src/main/assets/${source.asset}")
            if (file.exists()) file.inputStream().use { GlbLoader.load(it, source::place) }
            else null
        }.orEmpty()
        val inside = vehicle.model?.internals?.let { asset ->
            val file = File("src/main/assets/$asset")
            if (file.exists()) {
                file.inputStream().use { GlbLoader.load(it, vehicle.model!!::place) }
            } else null
        }.orEmpty()

        val meshes = ArrayList<Pair<Mesh, Palette>>()
        if (internals) {
            for (m in vehicle.modules) {
                // Armour is drawn nearly transparent in the app; this renderer has no
                // alpha, so opaque plates would simply wall the rest of it off.
                if (m.kind == ModuleKind.ARMOUR) continue
                val mesh = inside[m.id] ?: m.mesh
                if (!mesh.isEmpty) meshes += mesh to m.kind.palette
            }
        } else {
            for (p in vehicle.parts) {
                val mesh = model[p.id] ?: p.mesh
                if (!mesh.isEmpty) meshes += mesh to vehicle.palette
            }
        }
        if (meshes.isEmpty()) return surface
        val occlusion = AmbientOcclusion.bake(meshes.map { it.first })
        if (System.getenv("AO_STATS") != null) {
            val all = occlusion.flatMap { it.asList() }
            println("%-16s ao min=%.2f avg=%.2f  fully-lit=%.0f%%".format(
                vehicle.id, all.min(), all.average(),
                100.0 * all.count { it > 0.99f } / all.size))
        }

        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
        var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
        for ((mesh, _) in meshes) {
            minX = min(minX, mesh.minX); maxX = max(maxX, mesh.maxX)
            minY = min(minY, mesh.minY); maxY = max(maxY, mesh.maxY)
            minZ = min(minZ, mesh.minZ); maxZ = max(maxZ, mesh.maxZ)
        }

        val cx = (minX + maxX) * 0.5f
        val cy = (minY + maxY) * 0.5f
        val cz = (minZ + maxZ) * 0.5f
        val radius = sqrt(
            ((maxX - minX) * 0.5f).pow(2) +
                ((maxY - minY) * 0.5f).pow(2) +
                ((maxZ - minZ) * 0.5f).pow(2),
        ).coerceAtLeast(1f)

        val aspect = w.toFloat() / h
        val halfY = (42f * 0.5f) * (Math.PI / 180.0).toFloat()
        val halfX = atan(tan(halfY) * aspect)
        val distance = 1.12f / sin(min(halfX, halfY)) * zoom

        val eye = floatArrayOf(
            distance * cos(elevation) * cos(azimuth),
            distance * sin(elevation),
            distance * cos(elevation) * sin(azimuth),
        )
        val f = normalize(floatArrayOf(-eye[0], -eye[1], -eye[2]))
        val s = normalize(cross(f, floatArrayOf(0f, 1f, 0f)))
        val u = cross(s, f)
        val focal = 1f / tan(halfY)

        for ((meshIndex, entry) in meshes.withIndex()) {
            val (mesh, palette) = entry
            val ao = occlusion[meshIndex]
            val pos = mesh.positions
            val nrm = mesh.normals
            for (t in 0 until mesh.triangleCount) {
                val role = mesh.roleAt(t)
                val base = shadeRgb(palette.colorFor(role), mesh.toneAt(t) * Mesh.TONE_IN_3D)
                val spec = specularOf(role)
                val gloss = glossOf(role)

                val vp = Array(3) { FloatArray(3) }      // view space, for rasterising
                val vw = Array(3) { FloatArray(3) }      // world space, for shading
                val vn = Array(3) { FloatArray(3) }
                val occ = FloatArray(3)
                // Baked surface variation - camouflage, mud, worn edges - carried per
                // corner exactly as the GL path carries it in the vertex colour.
                val vt = Array(3) { FloatArray(3) }
                for (k in 0 until 3) {
                    val i = (t * 3 + k) * 3
                    // Model transform: centre and scale to unit radius.
                    val mx = (pos[i] - cx) / radius
                    val my = (pos[i + 1] - cy) / radius
                    val mz = (pos[i + 2] - cz) / radius
                    vw[k][0] = mx; vw[k][1] = my; vw[k][2] = mz
                    val rx = mx - eye[0]; val ry = my - eye[1]; val rz = mz - eye[2]
                    vp[k][0] = rx * s[0] + ry * s[1] + rz * s[2]
                    vp[k][1] = rx * u[0] + ry * u[1] + rz * u[2]
                    vp[k][2] = rx * f[0] + ry * f[1] + rz * f[2]      // depth, positive ahead
                    vn[k][0] = nrm[i]; vn[k][1] = nrm[i + 1]; vn[k][2] = nrm[i + 2]
                    occ[k] = ao[t * 3 + k]
                    mesh.tintAt(t * 3 + k, vt[k])
                }
                rasterise(surface, vp, vw, vn, occ, vt, base, spec, gloss, eye, focal, aspect)
            }
        }
        return surface
    }

    /** Scanline fill with a depth buffer and per-pixel lighting. */
    private fun rasterise(
        surface: Surface, vp: Array<FloatArray>, vw: Array<FloatArray>, vn: Array<FloatArray>,
        occ: FloatArray, vt: Array<FloatArray>, base: FloatArray, spec: Float, gloss: Float,
        eye: FloatArray, focal: Float, aspect: Float,
    ) {
        val sx = FloatArray(3)
        val sy = FloatArray(3)
        val invZ = FloatArray(3)
        for (k in 0 until 3) {
            val z = vp[k][2]
            if (z < 0.01f) return                       // clipped by the near plane
            invZ[k] = 1f / z
            sx[k] = (vp[k][0] * focal / aspect / z * 0.5f + 0.5f) * surface.w
            sy[k] = (0.5f - vp[k][1] * focal / z * 0.5f) * surface.h
        }
        val area = (sx[1] - sx[0]) * (sy[2] - sy[0]) - (sx[2] - sx[0]) * (sy[1] - sy[0])
        if (abs(area) < 1e-6f) return

        val x0 = max(0, floorInt(minOf(sx[0], sx[1], sx[2])))
        val x1 = min(surface.w - 1, ceilInt(maxOf(sx[0], sx[1], sx[2])))
        val y0 = max(0, floorInt(minOf(sy[0], sy[1], sy[2])))
        val y1 = min(surface.h - 1, ceilInt(maxOf(sy[0], sy[1], sy[2])))

        for (py in y0..y1) {
            for (px in x0..x1) {
                val cxp = px + 0.5f
                val cyp = py + 0.5f
                var w0 = ((sx[1] - cxp) * (sy[2] - cyp) - (sx[2] - cxp) * (sy[1] - cyp)) / area
                var w1 = ((sx[2] - cxp) * (sy[0] - cyp) - (sx[0] - cxp) * (sy[2] - cyp)) / area
                var w2 = 1f - w0 - w1
                if (w0 < 0f || w1 < 0f || w2 < 0f) continue

                val z = 1f / (w0 * invZ[0] + w1 * invZ[1] + w2 * invZ[2])
                val idx = py * surface.w + px
                if (z >= surface.depth[idx]) continue
                surface.depth[idx] = z

                // Perspective-correct interpolation of the normal.
                w0 *= invZ[0] * z; w1 *= invZ[1] * z; w2 *= invZ[2] * z
                val n = normalize(
                    floatArrayOf(
                        vn[0][0] * w0 + vn[1][0] * w1 + vn[2][0] * w2,
                        vn[0][1] * w0 + vn[1][1] * w1 + vn[2][1] * w2,
                        vn[0][2] * w0 + vn[1][2] * w1 + vn[2][2] * w2,
                    ),
                )
                val world = floatArrayOf(
                    vw[0][0] * w0 + vw[1][0] * w1 + vw[2][0] * w2,
                    vw[0][1] * w0 + vw[1][1] * w1 + vw[2][1] * w2,
                    vw[0][2] * w0 + vw[1][2] * w1 + vw[2][2] * w2,
                )
                val shadow = occ[0] * w0 + occ[1] * w1 + occ[2] * w2
                val tr = vt[0][0] * w0 + vt[1][0] * w1 + vt[2][0] * w2
                val tg = vt[0][1] * w0 + vt[1][1] * w1 + vt[2][1] * w2
                val tb = vt[0][2] * w0 + vt[1][2] * w1 + vt[2][2] * w2
                val shaded = floatArrayOf(
                    base[0] * shadow * tr, base[1] * shadow * tg, base[2] * shadow * tb,
                )
                val rgb = shadePixel(
                    n, world, shaded, spec * shadow * shadow * (0.6f + 0.4f * tr), gloss, eye,
                )
                val o = idx * 3
                surface.color[o] = rgb[0]
                surface.color[o + 1] = rgb[1]
                surface.color[o + 2] = rgb[2]
            }
        }
    }

    /** Mirrors the fragment shader in `gl/Shaders.kt`. */
    private fun shadePixel(
        normal: FloatArray, worldPos: FloatArray, base: FloatArray,
        spec: Float, gloss: Float, eye: FloatArray,
    ): FloatArray {
        var n = normal
        val v = normalize(
            floatArrayOf(eye[0] - worldPos[0], eye[1] - worldPos[1], eye[2] - worldPos[2]),
        )
        if (dot(n, v) < 0f) n = floatArrayOf(-n[0], -n[1], -n[2])

        val key = normalize(floatArrayOf(-0.42f, 0.78f, 0.46f))
        val fillDir = normalize(floatArrayOf(0.72f, 0.12f, -0.62f))
        val keyTerm = max((dot(n, key) + 0.28f) / 1.28f, 0f)
        val fill = max(dot(n, fillDir), 0f) * 0.30f
        val up = n[1] * 0.5f + 0.5f
        val amb = floatArrayOf(
            lerp(0.105f, 0.185f, up), lerp(0.115f, 0.205f, up), lerp(0.105f, 0.245f, up),
        )
        val h = normalize(floatArrayOf(key[0] + v[0], key[1] + v[1], key[2] + v[2]))
        val hi = max(dot(n, h), 0f).pow(gloss) * spec
        val rim = (1f - max(dot(n, v), 0f)).pow(3f) * 0.24f

        val out = FloatArray(3)
        val rimTint = floatArrayOf(0.40f, 0.48f, 0.58f)
        for (i in 0 until 3) {
            var c = base[i] * (amb[i] + keyTerm * 1.08f + fill) + hi + rimTint[i] * rim
            c = c / (c + 0.94f) * 1.94f
            out[i] = c
        }
        return out
    }

    /** Every vehicle on one sheet, three to a row. */
    private fun contactSheet(dir: File) {
        val cols = 3
        val cellW = 460
        val cellH = 290
        val rows = (Catalog.campaign.size + cols - 1) / cols
        val sheetW = cols * cellW
        val sheetH = rows * cellH
        val sheet = ByteArray(sheetW * sheetH * 3)
        for ((i, v) in Catalog.campaign.withIndex()) {
            val cell = render(v, cellW, cellH).toRgb()
            val ox = (i % cols) * cellW
            val oy = (i / cols) * cellH
            for (y in 0 until cellH) {
                System.arraycopy(
                    cell, y * cellW * 3,
                    sheet, ((oy + y) * sheetW + ox) * 3,
                    cellW * 3,
                )
            }
        }
        PngWriter.write(File(dir, "contact-sheet.png"), sheetW, sheetH, sheet)
    }

    // -- small maths ------------------------------------------------------

    private fun shadeRgb(color: Int, tone: Float): FloatArray {
        var r = ((color shr 16) and 0xFF) / 255f
        var g = ((color shr 8) and 0xFF) / 255f
        var b = (color and 0xFF) / 255f
        val step = abs(tone) * 0.055f
        val target = if (tone > 0) 1f else 0f
        r += (target - r) * step; g += (target - g) * step; b += (target - b) * step
        return floatArrayOf(r, g, b)
    }

    private fun specularOf(role: Int) = when (role) {
        Role.METAL -> 0.62f
        Role.GLASS -> 0.95f
        Role.DARK -> 0.06f
        Role.ACCENT -> 0.14f
        else -> 0.17f
    }

    private fun glossOf(role: Int) = when (role) {
        Role.METAL -> 44f
        Role.GLASS -> 110f
        Role.DARK -> 6f
        else -> 15f
    }

    private fun normalize(v: FloatArray): FloatArray {
        val l = sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]).coerceAtLeast(1e-6f)
        return floatArrayOf(v[0] / l, v[1] / l, v[2] / l)
    }

    private fun cross(a: FloatArray, b: FloatArray) = floatArrayOf(
        a[1] * b[2] - a[2] * b[1], a[2] * b[0] - a[0] * b[2], a[0] * b[1] - a[1] * b[0],
    )

    private fun dot(a: FloatArray, b: FloatArray) = a[0] * b[0] + a[1] * b[1] + a[2] * b[2]

    private fun floorInt(v: Float) = kotlin.math.floor(v).toInt()
    private fun ceilInt(v: Float) = kotlin.math.ceil(v).toInt()
}

private fun lerp(a: Float, b: Float, t: Float) = a + (b - a) * t
