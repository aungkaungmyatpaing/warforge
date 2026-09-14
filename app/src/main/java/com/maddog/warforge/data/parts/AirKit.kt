package com.maddog.warforge.data.parts

import com.maddog.warforge.poly.Role
import com.maddog.warforge.poly.TWO_PI
import com.maddog.warforge.solid.Mesh
import com.maddog.warforge.solid.cylinderPair
import com.maddog.warforge.solid.revolveX
import kotlin.math.cos
import kotlin.math.sin

/**
 * Aircraft fittings. Side view, nose to the right, centreline around y = 300.
 *
 * Flying surfaces are the one place the side profile is not the interesting one: a wing
 * is a thin section swept a long way outboard, so [airfoil] extrudes across the span and
 * mirrors, while a fin keeps the part's own narrow default depth.
 */

/** Half-span a wing reaches by default, and where it leaves the fuselage. */
const val WING_ROOT = 24f
const val WING_SPAN = 370f

/**
 * An airfoil seen close to edge-on: thick at the leading edge, drawn out to a point at
 * the trailing edge. This is the shape every wing, tailplane and fin in the game is
 * made of, which keeps the silhouettes consistent across seventy years of aircraft.
 *
 * @param xLe leading edge, @param xTe trailing edge - xTe may be either side of xLe.
 * @param rise how far the tip climbs above the root, for dihedral or a swept fin.
 */
fun SolidBuilder.airfoil(
    xLe: Float, xTe: Float, y: Float, thickness: Float, rise: Float = 0f,
    role: Int = Role.BODY, tone: Int = 0,
    root: Float = WING_ROOT, span: Float = WING_SPAN, mirrored: Boolean = true,
) = depth(root, span, mirrored) {
    val camber = thickness * 0.55f
    val crest = xLe + (xTe - xLe) * 0.28f      // thickest point sits just aft of the LE
    slab(
        role, tone,
        xLe, y - camber + rise,
        crest, y - thickness + rise,
        xTe, y - thickness * 0.12f + rise,
        xTe, y + thickness * 0.12f + rise,
        xLe, y + camber * 0.7f + rise,
        contrast = 2,
    )
}

/** Tapered fuselage from a spine of x,y,radius triples - round in three dimensions. */
fun SolidBuilder.fuselage(vararg spine: Float) = bar(Role.BODY, 0, 24, *spine)

/** Bubble or framed canopy. [framed] adds the WWII-style glazing bars. */
fun SolidBuilder.canopy(
    x0: Float, x1: Float, yBase: Float, h: Float, framed: Boolean = false, half: Float = 30f,
) = depth(-half, half) {
    val mid = (x0 + x1) * 0.5f
    slab(
        Role.GLASS, 1,
        x0, yBase, x0 + (x1 - x0) * 0.22f, yBase - h, mid + (x1 - x0) * 0.18f, yBase - h,
        x1, yBase - h * 0.35f, x1, yBase,
        contrast = 3,
    )
    if (framed) {
        val step = (x1 - x0) / 4f
        for (i in 1 until 4) box(Role.DARK, 0, x0 + step * i - 2.5f, yBase - h + 2f, 5f, h)
    }
}

/**
 * Propeller. Edge-on in the flat view, but a real radial fan in 3D - blades sweep round
 * the depth plane rather than lying flat, which is the difference between a propeller
 * and a plank when the camera moves off the beam.
 */
fun SolidBuilder.propeller(cx: Float, cy: Float, r: Float, blades: Int = 2, spinner: Float = 20f) {
    val w = 7f + blades * 1.8f
    val lean = r * 0.10f
    flat {
        slab(
            Role.DARK, 1,
            cx - w, cy - 5f, cx + w, cy - 5f,
            cx + w * 0.42f + lean, cy - r, cx - w * 0.30f + lean, cy - r,
            contrast = 1,
        )
        slab(
            Role.DARK, -1,
            cx - w * 0.30f - lean, cy + r, cx + w * 0.42f - lean, cy + r,
            cx + w, cy + 5f, cx - w, cy + 5f,
            contrast = 1,
        )
        ngon(Role.METAL, 1, cx, cy, spinner, sides = 8)
    }
    solid {
        val yc = Mesh.wy(cy)
        for (i in 0 until blades) {
            blade(cx, yc, TWO_PI * i / blades, spinner * 0.8f, r, w, w * 0.55f, 4.5f)
        }
        revolveX(Role.METAL, 1, 20, cx - spinner * 0.6f, cy, spinner * 0.5f, cx, cy, spinner, cx + spinner, cy, spinner * 0.25f)
    }
}

/** One propeller blade, swept to angle [a] about the fuselage axis. */
private fun Mesh.blade(
    x: Float, yc: Float, a: Float, r0: Float, r1: Float, w0: Float, w1: Float, t: Float,
) {
    val cy0 = cos(a); val sy0 = sin(a)
    fun py(r: Float, w: Float) = yc + r * cy0 - w * sy0
    fun pz(r: Float, w: Float) = r * sy0 + w * cy0
    val corners = arrayOf(
        floatArrayOf(py(r0, -w0), pz(r0, -w0)),
        floatArrayOf(py(r0, w0), pz(r0, w0)),
        floatArrayOf(py(r1, w1), pz(r1, w1)),
        floatArrayOf(py(r1, -w1), pz(r1, -w1)),
    )
    for (i in 0 until 4) {
        val j = (i + 1) % 4
        quadFacing(
            x - t, corners[i][0], corners[i][1], x - t, corners[j][0], corners[j][1],
            x + t, corners[j][0], corners[j][1], x + t, corners[i][0], corners[i][1],
            corners[i][0] + corners[j][0] - 2 * yc, 0f, corners[i][1] + corners[j][1],
            Role.DARK, 0,
        )
    }
    quadFacing(
        x + t, corners[0][0], corners[0][1], x + t, corners[1][0], corners[1][1],
        x + t, corners[2][0], corners[2][1], x + t, corners[3][0], corners[3][1],
        1f, 0f, 0f, Role.DARK, 1,
    )
    quadFacing(
        x - t, corners[0][0], corners[0][1], x - t, corners[1][0], corners[1][1],
        x - t, corners[2][0], corners[2][1], x - t, corners[3][0], corners[3][1],
        -1f, 0f, 0f, Role.DARK, -1,
    )
}

/** Air-cooled radial engine: a faceted drum with cylinder heads round its rim. */
fun SolidBuilder.radialEngine(cx: Float, cy: Float, r: Float, half: Float = 26f) {
    disc(Role.DARK, 0, cx, cy, r, -half, half, sides = 10, mirrored = false)
    val step = TWO_PI / 7
    for (i in 0 until 7) {
        val a = step * i - TWO_PI / 4f
        disc(
            Role.METAL, -1, cx + r * 0.82f * cos(a), cy + r * 0.82f * sin(a), r * 0.24f,
            -half - 3f, half + 3f, sides = 5, mirrored = false, rotation = a,
        )
    }
    disc(Role.METAL, 2, cx, cy, r * 0.38f, half, half + 8f, sides = 8, mirrored = false)
}

/** In-line engine cowling with exhaust stubs along the side. */
fun SolidBuilder.cowling(
    x0: Float, x1: Float, cy: Float, r0: Float, r1: Float, exhausts: Int = 6,
) {
    bar(Role.BODY, 1, 22, x0, cy, r0, x1, cy, r1)
    depth(r0 * 0.6f, r0 * 0.95f, mirrored = true) {
        for (i in 0 until exhausts) box(Role.DARK, -2, x0 + 10f + i * 15f, cy + r0 * 0.35f, 10f, 9f)
    }
}

/** Jet intake lip. */
fun SolidBuilder.intake(x: Float, cy: Float, r: Float, depth: Float = 34f) {
    bar(Role.BODY, 1, 22, x, cy, r, x + depth, cy, r * 0.86f)
    disc(Role.DARK, -3, x + 4f, cy, r * 0.78f, -1f, 1f, sides = 10, mirrored = false)
}

/** Afterburner nozzle - the petals read as a stack of dark facets. */
fun SolidBuilder.nozzle(x: Float, cy: Float, r: Float, len: Float = 52f) {
    bar(Role.METAL, 0, 22, x - len, cy, r * 0.86f, x, cy, r)
    disc(Role.DARK, -4, x - len, cy, r * 0.8f, -1f, 1f, sides = 10, mirrored = false)
}

/** Main or tail landing gear: a strut with a wheel on the end. */
fun SolidBuilder.gearLeg(
    x: Float, yTop: Float, yWheel: Float, wheelR: Float, rake: Float = 0f,
    side: Float = 0f, mirrored: Boolean = false,
) {
    val inner = if (mirrored) side - 9f else -9f
    val outer = if (mirrored) side + 9f else 9f
    depth(inner, outer, mirrored) {
        quad(Role.METAL, 0, x - 7f, yTop, x + 7f, yTop, x + rake + 6f, yWheel, x + rake - 6f, yWheel, split = 1)
    }
    disc(
        Role.DARK, -1, x + rake, yWheel, wheelR,
        if (mirrored) side - wheelR * 0.4f else -wheelR * 0.4f,
        if (mirrored) side + wheelR * 0.4f else wheelR * 0.4f,
        sides = 10, mirrored = mirrored,
    )
}

/** Rigging strut between biplane wings. */
fun SolidBuilder.strut(
    x0: Float, y0: Float, x1: Float, y1: Float, w: Float = 7f, side: Float = 150f,
) = depth(side - w, side + w, mirrored = true) {
    quad(Role.TRIM, 0, x0 - w, y0, x0 + w, y0, x1 + w, y1, x1 - w, y1, split = 1)
}

/**
 * National roundel, laid onto the skin as a decal.
 *
 * A flat disc stuck on the side of a round fuselage stands proud at its rim by more than
 * its own thickness - it reads as a badge screwed on rather than as paint. This follows
 * the curve instead: the fuselage is a cylinder about the x axis, so the skin's depth
 * depends only on how far up or down the fuselage a point is, and each horizontal strip
 * of the roundel sits at its own depth.
 *
 * @param skin the fuselage radius where the roundel is painted.
 */
fun SolidBuilder.roundel(cx: Float, cy: Float, r: Float, skin: Float = 46f) {
    flat {
        ngon(Role.ACCENT, -1, cx, cy, r, sides = 14, lit = false)
        ngon(Role.ACCENT, 4, cx, cy, r * 0.62f, sides = 12, lit = false)
        ngon(Role.ACCENT, -2, cx, cy, r * 0.3f, sides = 10, lit = false)
    }
    solid {
        val strips = 14
        for (i in 0 until strips) {
            val v0 = -r + 2f * r * i / strips
            val v1 = -r + 2f * r * (i + 1) / strips
            // Half-width of the roundel at this height, and the skin depth there.
            val h0 = kotlin.math.sqrt((r * r - v0 * v0).coerceAtLeast(0f))
            val h1 = kotlin.math.sqrt((r * r - v1 * v1).coerceAtLeast(0f))
            // Stand the decal clear of the skin by more than the depth buffer's slop,
            // or half of it disappears into the fuselage it is painted on.
            val z0 = kotlin.math.sqrt((skin * skin - v0 * v0).coerceAtLeast(1f)) + 1.6f
            val z1 = kotlin.math.sqrt((skin * skin - v1 * v1).coerceAtLeast(1f)) + 1.6f
            val mid = kotlin.math.abs((v0 + v1) * 0.5f) / r
            // Concentric bands: dark rim, light field, dark centre.
            val tone = when {
                mid > 0.62f -> -1
                mid > 0.30f -> 4
                else -> -2
            }
            for (sign in listOf(1f, -1f)) {
                quadN(
                    cx - h0, Mesh.wy(cy + v0), sign * z0, 0f, v0 / skin, sign * z0 / skin,
                    cx + h0, Mesh.wy(cy + v0), sign * z0, 0f, v0 / skin, sign * z0 / skin,
                    cx + h1, Mesh.wy(cy + v1), sign * z1, 0f, v1 / skin, sign * z1 / skin,
                    cx - h1, Mesh.wy(cy + v1), sign * z1, 0f, v1 / skin, sign * z1 / skin,
                    Role.ACCENT, tone,
                )
            }
        }
    }
}

/** Underwing store: bomb, rocket pod or drop tank. */
fun SolidBuilder.store(
    x: Float, cy: Float, len: Float, r: Float, finned: Boolean = true, side: Float = 0f,
) {
    bar(Role.TRIM, 0, 20, x, cy, r * 0.5f, x + len * 0.25f, cy, r, x + len, cy, r * 0.45f)
    if (finned) depth(side - r, side + r) {
        slab(Role.DARK, 0, x, cy - r, x + len * 0.2f, cy - r * 0.4f, x + len * 0.2f, cy + r * 0.4f, x, cy + r)
    }
}

/** Wing-mounted or nose machine gun / cannon. */
fun SolidBuilder.aircraftGun(x: Float, y: Float, len: Float, r: Float = 5f) {
    bar(Role.METAL, -1, 16, x, y, r, x + len, y, r * 0.8f)
    depth(-r * 2f, r * 2f) { box(Role.DARK, 0, x - 18f, y - r * 2f, 22f, r * 4f) }
}
