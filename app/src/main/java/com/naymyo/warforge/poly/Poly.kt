package com.naymyo.warforge.poly

import android.graphics.Color
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Low-poly rendering primitives.
 *
 * Everything in the game is drawn as flat-shaded convex facets. A facet never has a
 * gradient: the faceted silhouette plus per-facet tone IS the low-poly look, so the
 * renderer stays a plain `drawPath` with a solid fill and needs no assets at all.
 */

/** Colour slots a facet can pull from. The vehicle's [Palette] resolves them. */
object Role {
    const val BODY = 0
    const val DARK = 1      // tracks, tyres, shadowed underside
    const val METAL = 2     // gun barrels, props, exhausts
    const val GLASS = 3     // canopies, bridge windows
    const val ACCENT = 4    // roundels, stripes, unit markings
    const val TRIM = 5      // secondary hull colour / camo blotches
    const val COUNT = 6
}

/**
 * One flat-shaded convex facet.
 *
 * @param pts x0,y0,x1,y1,... in blueprint space (1000 x 620, y down).
 * @param tone lightness step in roughly -4..4; the renderer blends toward white for
 *   positive values and toward black for negative ones.
 */
class Poly(val pts: FloatArray, val tone: Int, val role: Int = Role.BODY)

/** The five/six colours a vehicle draws with. */
data class Palette(
    val body: Int,
    val dark: Int,
    val metal: Int,
    val glass: Int,
    val accent: Int,
    val trim: Int = body,
) {
    fun colorFor(role: Int): Int = when (role) {
        Role.DARK -> dark
        Role.METAL -> metal
        Role.GLASS -> glass
        Role.ACCENT -> accent
        Role.TRIM -> trim
        else -> body
    }

    companion object {
        /** `#RRGGBB` convenience so catalog files stay readable. */
        fun of(body: Long, dark: Long, metal: Long, glass: Long, accent: Long, trim: Long = body) =
            Palette(
                (0xFF000000L or body).toInt(), (0xFF000000L or dark).toInt(),
                (0xFF000000L or metal).toInt(), (0xFF000000L or glass).toInt(),
                (0xFF000000L or accent).toInt(), (0xFF000000L or trim).toInt(),
            )
    }
}

/** Step in lightness applied per tone unit. Kept small so facets read as one material. */
private const val TONE_STEP = 0.055f

/** Blends [color] toward white (tone > 0) or black (tone < 0). */
fun shade(color: Int, tone: Int): Int {
    if (tone == 0) return color
    val f = (abs(tone) * TONE_STEP).coerceAtMost(0.85f)
    val target = if (tone > 0) 255 else 0
    fun mix(c: Int) = (c + (target - c) * f).roundToInt().coerceIn(0, 255)
    return Color.rgb(mix(Color.red(color)), mix(Color.green(color)), mix(Color.blue(color)))
}

// ---------------------------------------------------------------------------
// Shape building DSL
// ---------------------------------------------------------------------------

/** Direction the scene is lit from, used by [ShapeBuilder.autoTone]. Up-left. */
const val TWO_PI = (2.0 * Math.PI).toFloat()

private const val LIGHT_X = -0.45f
private const val LIGHT_Y = -0.89f

/**
 * Collects facets for one part. Coordinates are absolute blueprint coordinates, which
 * means a part's polygons already sit where they belong on the finished vehicle - the
 * assembly board only has to hide them until the player drops the part in.
 */
open class ShapeBuilder {
    val polys = ArrayList<Poly>()

    /** Raw facet. Points must wind consistently and stay convex. */
    fun face(role: Int, tone: Int, vararg c: Float) {
        polys += Poly(c, tone, role)
    }

    /**
     * A quad split into two triangles one tone apart, which is what keeps flat panels
     * from looking like vector art. Corners in order, clockwise.
     */
    fun quad(
        role: Int, tone: Int,
        x0: Float, y0: Float, x1: Float, y1: Float,
        x2: Float, y2: Float, x3: Float, y3: Float,
        split: Int = 1,
    ) {
        face(role, tone, x0, y0, x1, y1, x2, y2)
        face(role, tone - split, x0, y0, x2, y2, x3, y3)
    }

    /**
     * Fan-triangulates an outline into facets shaded by height, so a hand-drawn hull or
     * fuselage silhouette turns low-poly on its own. The outline must stay convex (a fan
     * from vertex 0 would overlap otherwise) - concave shapes are built from two slabs.
     *
     * @param contrast how far the top and bottom facets drift from [tone].
     */
    fun slab(role: Int, tone: Int, vararg pts: Float, contrast: Int = 2) {
        val n = pts.size / 2
        if (n < 3) return
        var top = Float.MAX_VALUE
        var bottom = -Float.MAX_VALUE
        for (i in 0 until n) {
            val y = pts[i * 2 + 1]
            if (y < top) top = y
            if (y > bottom) bottom = y
        }
        val span = (bottom - top).takeIf { it > 0.01f } ?: 1f
        for (i in 1 until n - 1) {
            val ax = pts[0]; val ay = pts[1]
            val bx = pts[i * 2]; val by = pts[i * 2 + 1]
            val cx = pts[(i + 1) * 2]; val cy = pts[(i + 1) * 2 + 1]
            val f = ((ay + by + cy) / 3f - top) / span      // 0 at the top, 1 at the bottom
            face(role, tone + ((0.5f - f) * 2f * contrast).roundToInt(), ax, ay, bx, by, cx, cy)
        }
    }

    /** Axis-aligned box, faceted. */
    fun box(role: Int, tone: Int, x: Float, y: Float, w: Float, h: Float) =
        quad(role, tone, x, y, x + w, y, x + w, y + h, x, y + h)

    /**
     * A regular polygon - the low-poly stand-in for every circle in the game (road
     * wheels, tyres, prop bosses, radar domes). Each wedge is lit independently.
     */
    fun ngon(
        role: Int, tone: Int, cx: Float, cy: Float, r: Float,
        sides: Int = 18, rotation: Float = 0f, lit: Boolean = true,
    ) {
        val step = (2.0 * Math.PI / sides).toFloat()
        for (i in 0 until sides) {
            val a0 = rotation + step * i
            val a1 = rotation + step * (i + 1)
            val x0 = cx + r * cos(a0); val y0 = cy + r * sin(a0)
            val x1 = cx + r * cos(a1); val y1 = cy + r * sin(a1)
            val t = if (lit) tone + autoTone((x0 + x1) * 0.5f - cx, (y0 + y1) * 0.5f - cy, r) else tone
            face(role, t, cx, cy, x0, y0, x1, y1)
        }
    }

    /**
     * Hollow faceted ring - tyres, track belts, gun shields. [sweep] under a full turn
     * gives an arc, which is how a track run bends around its idler and sprocket.
     * Angles are in radians with y pointing down: 0 is forward, -PI/2 is up.
     */
    fun ring(
        role: Int, tone: Int, cx: Float, cy: Float, rOuter: Float, rInner: Float,
        sides: Int = 10, start: Float = 0f, sweep: Float = TWO_PI,
    ) {
        val step = sweep / sides
        for (i in 0 until sides) {
            val a0 = start + step * i
            val a1 = start + step * (i + 1)
            val c0 = cos(a0); val s0 = sin(a0); val c1 = cos(a1); val s1 = sin(a1)
            val t = tone + autoTone((c0 + c1) * 0.5f, (s0 + s1) * 0.5f, 1f)
            quad(
                role, t,
                cx + rOuter * c0, cy + rOuter * s0, cx + rOuter * c1, cy + rOuter * s1,
                cx + rInner * c1, cy + rInner * s1, cx + rInner * c0, cy + rInner * s0,
                split = 0,
            )
        }
    }

    /**
     * A tapered tube along a spine - fuselages, gun barrels, funnels, torpedoes.
     * [spine] is x,y,radius triples; consecutive rings are bridged with an upper and a
     * lower facet so the tube keeps a visible crease down its length.
     */
    fun tube(role: Int, tone: Int, vararg spine: Float) {
        var i = 0
        while (i + 5 < spine.size) {
            val x0 = spine[i]; val y0 = spine[i + 1]; val r0 = spine[i + 2]
            val x1 = spine[i + 3]; val y1 = spine[i + 4]; val r1 = spine[i + 5]
            quad(role, tone + 1, x0, y0 - r0, x1, y1 - r1, x1, y1, x0, y0, split = 0)
            quad(role, tone - 1, x0, y0, x1, y1, x1, y1 + r1, x0, y0 + r0, split = 0)
            i += 3
        }
    }

    /** Lightness a facet picks up from its outward direction. */
    private fun autoTone(dx: Float, dy: Float, scale: Float): Int {
        if (scale <= 0f) return 0
        val nx = dx / scale
        val ny = dy / scale
        return ((nx * LIGHT_X + ny * LIGHT_Y) * 2.4f).roundToInt().coerceIn(-3, 3)
    }
}

/** Builds the facet list for one part. */
fun shape(block: ShapeBuilder.() -> Unit): List<Poly> = ShapeBuilder().apply(block).polys

/** Mirrors facets horizontally about [axis] - lets one builder serve left/right pairs. */
fun List<Poly>.mirrorX(axis: Float): List<Poly> = map { p ->
    val out = FloatArray(p.pts.size)
    // Reverse point order as well, so the winding survives the flip.
    val n = p.pts.size / 2
    for (i in 0 until n) {
        val src = (n - 1 - i) * 2
        out[i * 2] = 2f * axis - p.pts[src]
        out[i * 2 + 1] = p.pts[src + 1]
    }
    Poly(out, p.tone, p.role)
}

/** Translates facets. Used to lay the same part out in the tray and on the board. */
fun List<Poly>.translated(dx: Float, dy: Float): List<Poly> = map { p ->
    val out = FloatArray(p.pts.size)
    for (i in p.pts.indices) out[i] = p.pts[i] + if (i % 2 == 0) dx else dy
    Poly(out, p.tone, p.role)
}
