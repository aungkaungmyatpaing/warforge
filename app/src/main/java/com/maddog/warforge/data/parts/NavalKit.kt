package com.maddog.warforge.data.parts

import com.maddog.warforge.poly.Role
import com.maddog.warforge.poly.TWO_PI
import com.maddog.warforge.solid.Mesh
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.revolveX
import kotlin.math.cos
import kotlin.math.sin

/**
 * Warship fittings. Side view, bow to the right, waterline at [WATERLINE].
 *
 * A ship is the most extrusion-friendly shape in the game: hull, deckhouse and funnel
 * are all a profile with a beam, so the depth arguments here are almost all just "how
 * wide is this bit".
 */

/** Sea level in blueprint space. Everything below it is drawn darker. */
const val WATERLINE = 432f

/** Default half-beam. Individual ships override it. */
const val BEAM = 92f

/**
 * A turret with one to three barrels in one gunhouse. Barrels are stacked slightly
 * apart vertically because a pure side view would hide all but the nearest one - in 3D
 * that same spread reads as the guns being side by side.
 */
fun SolidBuilder.navalTurret(
    cx: Float, yBase: Float, w: Float, h: Float, barrels: Int, barrelLen: Float,
    facingRight: Boolean = true, barrelR: Float = 7f, half: Float = 52f,
) {
    val halfW = w * 0.5f
    depth(-half, half) {
        slab(
            Role.BODY, 1,
            cx - halfW, yBase, cx - halfW * 0.82f, yBase - h, cx + halfW * 0.82f, yBase - h,
            cx + halfW, yBase,
            contrast = 2,
        )
        box(Role.DARK, -2, cx - halfW * 0.5f, yBase - h - 5f, w * 0.5f, 6f)
        box(Role.BODY, -1, cx - halfW * 0.7f, yBase, w * 0.7f, 14f)      // barbette
    }
    val muzzleX = if (facingRight) cx + halfW else cx - halfW
    val dir = if (facingRight) 1f else -1f
    val y = yBase - h * 0.55f
    // Guns sit abreast in 3D; the flat view stacks them so none is hidden behind another.
    val spacing = if (barrels > 1) half * 1.2f / (barrels - 1) else 0f
    for (i in 0 until barrels) {
        val off = if (barrels > 1) -half * 0.6f + spacing * i else 0f
        flat {
            tube(Role.METAL, 0, muzzleX, y + 9f * i - 4.5f * (barrels - 1), barrelR,
                muzzleX + dir * barrelLen, y + 9f * i - 4.5f * (barrels - 1), barrelR * 0.82f)
        }
        solid {
            val ring = 20
            val step = TWO_PI / ring
            for (s in 0 until ring) {
                val a0 = step * s
                val a1 = step * (s + 1)
                val yc = Mesh.wy(y)
                quadFacing(
                    muzzleX, yc + barrelR * cos(a0), off + barrelR * sin(a0),
                    muzzleX + dir * barrelLen, yc + barrelR * 0.82f * cos(a0), off + barrelR * 0.82f * sin(a0),
                    muzzleX + dir * barrelLen, yc + barrelR * 0.82f * cos(a1), off + barrelR * 0.82f * sin(a1),
                    muzzleX, yc + barrelR * cos(a1), off + barrelR * sin(a1),
                    0f, (cos(a0) + cos(a1)) * 0.5f, (sin(a0) + sin(a1)) * 0.5f,
                    Role.METAL, 0,
                )
            }
        }
    }
}

/** Raked funnel with a cap band and a wisp of soot at the lip. */
fun SolidBuilder.funnel(
    cx: Float, yBase: Float, yTop: Float, w: Float, rake: Float = 10f, half: Float = 34f,
) = depth(-half, half) {
    val halfW = w * 0.5f
    slab(
        Role.BODY, 1,
        cx - halfW, yBase, cx - halfW * 0.86f + rake, yTop, cx + halfW * 0.86f + rake, yTop,
        cx + halfW, yBase,
        contrast = 2,
    )
    box(Role.DARK, -1, cx - halfW * 0.9f + rake, yTop - 7f, w * 0.9f, 8f)
    box(Role.DARK, -4, cx - halfW * 0.6f + rake, yTop - 3f, w * 0.6f, 5f)
}

/** Lattice or pole mast with yardarms and a lookout platform. */
fun SolidBuilder.mast(
    x: Float, yBase: Float, yTop: Float, lattice: Boolean = false, yards: Int = 2,
) {
    val h = yBase - yTop
    if (lattice) {
        depth(-18f, 18f) {
            quad(Role.METAL, 0, x - 20f, yBase, x - 7f, yTop, x + 7f, yTop, x + 20f, yBase, split = 1)
            var y = yTop + 14f
            var i = 0
            while (y < yBase) {
                val w = 7f + (y - yTop) / h * 13f
                box(Role.DARK, -3, x - w, y, w * 2f, 3.5f)
                if (i % 2 == 0) box(Role.DARK, -3, x - w * 0.5f, y, 3f, 22f)
                y += 22f
                i++
            }
        }
    } else {
        bar(Role.METAL, 1, 14, x, yBase, 5f, x, yTop, 3f)
        flat { quad(Role.METAL, 1, x - 5f, yBase, x - 3f, yTop, x + 3f, yTop, x + 5f, yBase, split = 1) }
    }
    for (i in 0 until yards) {
        val y = yTop + 18f + i * 34f
        val w = 34f + i * 16f
        flat { box(Role.METAL, 0, x - w, y, w * 2f, 4f) }
        solid { box3(Role.METAL, 0, x - 2f, y, -w, x + 2f, y + 4f, w) }
    }
}

/** Air-search radar: the flat "bedspring" array of the 1940s onward. */
fun SolidBuilder.radarArray(cx: Float, cy: Float, w: Float, h: Float) = depth(-w * 0.42f, w * 0.42f) {
    slab(Role.METAL, 1, cx - w * 0.5f, cy - h * 0.5f, cx + w * 0.5f, cy - h * 0.35f, cx + w * 0.5f, cy + h * 0.35f, cx - w * 0.5f, cy + h * 0.5f)
    val n = (w / 11f).toInt().coerceAtLeast(2)
    for (i in 0 until n) box(Role.DARK, -3, cx - w * 0.5f + i * (w / n) + 2f, cy - h * 0.4f, 3f, h * 0.8f)
}

/** Rotating parabolic dish. */
fun SolidBuilder.radarDish(cx: Float, cy: Float, r: Float) = depth(-r * 0.9f, r * 0.9f) {
    slab(Role.METAL, 1, cx - r * 0.3f, cy - r, cx + r * 0.35f, cy - r * 0.6f, cx + r * 0.35f, cy + r * 0.6f, cx - r * 0.3f, cy + r)
    box(Role.METAL, -1, cx - r * 0.5f, cy - 4f, r * 0.5f, 8f)
}

/** Bridge / superstructure block with a row of scuttles. */
fun SolidBuilder.deckHouse(
    x: Float, yBase: Float, w: Float, h: Float, windows: Boolean = true, half: Float = 58f,
) = depth(-half, half) {
    slab(Role.BODY, 1, x, yBase, x + 5f, yBase - h, x + w - 5f, yBase - h, x + w, yBase, contrast = 2)
    if (windows) {
        val n = (w / 30f).toInt().coerceAtLeast(1)
        for (i in 0 until n) box(Role.GLASS, 1, x + 12f + i * 30f, yBase - h + 10f, 18f, 12f)
    }
}

/** Ship's boat in its davits, carried outboard on both beams. */
fun SolidBuilder.lifeboat(cx: Float, cy: Float, len: Float, side: Float = BEAM - 10f) =
    depth(side - 11f, side + 11f, mirrored = true) {
        slab(Role.TRIM, 1, cx - len * 0.5f, cy, cx - len * 0.42f, cy - 12f, cx + len * 0.42f, cy - 12f, cx + len * 0.5f, cy)
        box(Role.METAL, 0, cx - len * 0.42f, cy - 24f, 4f, 14f)
        box(Role.METAL, 0, cx + len * 0.38f, cy - 24f, 4f, 14f)
    }

/** Secondary / anti-aircraft mount. */
fun SolidBuilder.aaMount(cx: Float, yBase: Float, r: Float, barrels: Int = 2) {
    disc(Role.BODY, 0, cx, yBase, r, -r, r, sides = 9, mirrored = false)
    for (i in 0 until barrels) {
        bar(Role.METAL, 0, 14, cx, yBase - r * 0.3f + i * 7f, 3.5f, cx + r * 2.4f, yBase - r * 0.9f + i * 7f, 3f)
    }
}

/** Submarine screw and rudder. */
fun SolidBuilder.screw(cx: Float, cy: Float, r: Float, blades: Int = 5) {
    flat {
        val step = TWO_PI / blades
        for (i in 0 until blades) {
            val a = step * i
            slab(
                Role.METAL, if (i % 2 == 0) 1 else -1,
                cx, cy - 6f, cx + r * cos(a) * 0.5f, cy + r * sin(a),
                cx + r * cos(a + step * 0.5f) * 0.9f, cy + r * sin(a + step * 0.5f),
                cx, cy + 6f,
                contrast = 1,
            )
        }
        ngon(Role.DARK, 0, cx, cy, r * 0.25f, sides = 6)
    }
    solid {
        val yc = Mesh.wy(cy)
        val step = TWO_PI / blades
        for (i in 0 until blades) {
            screwBlade(cx, yc, step * i, r * 0.28f, r, r * 0.42f, r * 0.30f)
        }
        revolveX(Role.DARK, 0, 16, cx - r * 0.3f, cy, r * 0.24f, cx + r * 0.3f, cy, r * 0.2f)
    }
}

/** One screw blade, set at an angle so the disc reads as a propeller from any side. */
private fun Mesh.screwBlade(
    x: Float, yc: Float, a: Float, r0: Float, r1: Float, w0: Float, w1: Float,
) {
    val ca = cos(a); val sa = sin(a)
    fun py(r: Float, w: Float) = yc + r * ca - w * sa
    fun pz(r: Float, w: Float) = r * sa + w * ca
    val pts = arrayOf(
        floatArrayOf(py(r0, -w0), pz(r0, -w0), -5f),
        floatArrayOf(py(r0, w0), pz(r0, w0), 5f),
        floatArrayOf(py(r1, w1), pz(r1, w1), 5f),
        floatArrayOf(py(r1, -w1), pz(r1, -w1), -5f),
    )
    quadFacing(
        x + pts[0][2], pts[0][0], pts[0][1], x + pts[1][2], pts[1][0], pts[1][1],
        x + pts[2][2], pts[2][0], pts[2][1], x + pts[3][2], pts[3][0], pts[3][1],
        1f, 0f, 0f, Role.METAL, 1,
    )
    quadFacing(
        x + pts[0][2] - 4f, pts[0][0], pts[0][1], x + pts[1][2] - 4f, pts[1][0], pts[1][1],
        x + pts[2][2] - 4f, pts[2][0], pts[2][1], x + pts[3][2] - 4f, pts[3][0], pts[3][1],
        -1f, 0f, 0f, Role.METAL, -1,
    )
}

/** Anchor chain and hawse detail at the bow. */
fun SolidBuilder.anchor(x: Float, y: Float, side: Float = BEAM) =
    depth(side - 3f, side + 3f, mirrored = true) {
        ngon(Role.DARK, -2, x, y, 9f, sides = 6)
        box(Role.METAL, 0, x - 22f, y - 3f, 22f, 5f)
    }

/** Boot topping: the dark band along the waterline. */
fun SolidBuilder.waterline(
    xStern: Float, xBow: Float, y: Float = WATERLINE, band: Float = 22f, half: Float = BEAM + 2f,
) = depth(-half, half) {
    box(Role.DARK, -1, xStern, y - band, xBow - xStern, band)
}
