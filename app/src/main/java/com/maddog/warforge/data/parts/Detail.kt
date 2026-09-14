package com.maddog.warforge.data.parts

import com.maddog.warforge.poly.Role
import com.maddog.warforge.poly.TWO_PI
import com.maddog.warforge.solid.Mesh
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.cylinderY
import com.maddog.warforge.solid.revolveX
import com.maddog.warforge.solid.cylinderZ
import com.maddog.warforge.solid.sphere

/**
 * Surface detail.
 *
 * Big shapes tell you what a vehicle is; small ones tell you it is a real object. A hull
 * side is a flat plate until it has a weld bead down it, a row of rivets, a vision port
 * and a tow hook - then the eye has something to measure, and the same plate reads as
 * armour rather than as a polygon.
 *
 * Everything here is 3D only: it is far too fine to show up in the flat blueprint view,
 * and putting it there would only clutter the tray thumbnails.
 */

/** Rivet heads along a line on the vehicle's flank. */
fun SolidBuilder.rivets(
    x0: Float, y0: Float, x1: Float, y1: Float, count: Int, z: Float,
    r: Float = 3.4f, mirrored: Boolean = true, role: Int = Role.BODY,
) = solid {
    for (i in 0 until count) {
        val t = if (count > 1) i / (count - 1f) else 0.5f
        val x = x0 + (x1 - x0) * t
        val y = y0 + (y1 - y0) * t
        sphere(role, 1, x, y, z, r, 8, 5)
        if (mirrored) sphere(role, 1, x, y, -z, r, 8, 5)
    }
}

/** A raised weld bead on the flank. Real welds stand proud of the plate. */
fun SolidBuilder.weld(
    x0: Float, y0: Float, x1: Float, y1: Float, z: Float,
    w: Float = 3.2f, mirrored: Boolean = true,
) = solid {
    val segments = 10
    for (i in 0 until segments) {
        val t0 = i / segments.toFloat()
        val t1 = (i + 1) / segments.toFloat()
        val ax = x0 + (x1 - x0) * t0; val ay = y0 + (y1 - y0) * t0
        val bx = x0 + (x1 - x0) * t1; val by = y0 + (y1 - y0) * t1
        box3(Role.BODY, 1, minOf(ax, bx) - w * 0.5f, minOf(ay, by) - w * 0.5f, z,
            maxOf(ax, bx) + w * 0.5f, maxOf(ay, by) + w * 0.5f, z + w * 0.6f)
        if (mirrored) {
            box3(Role.BODY, 1, minOf(ax, bx) - w * 0.5f, minOf(ay, by) - w * 0.5f, -z - w * 0.6f,
                maxOf(ax, bx) + w * 0.5f, maxOf(ay, by) + w * 0.5f, -z)
        }
    }
}

/** A bolted-down access panel on the flank: recessed face inside a raised frame. */
fun SolidBuilder.panel(
    x: Float, y: Float, w: Float, h: Float, z: Float,
    mirrored: Boolean = true, bolts: Int = 4,
) {
    solid {
        val t = 3.5f
        for (side in if (mirrored) listOf(z, -z - 4f) else listOf(z)) {
            box3(Role.TRIM, 1, x, y, side, x + w, y + t, side + 4f)
            box3(Role.TRIM, 1, x, y + h - t, side, x + w, y + h, side + 4f)
            box3(Role.TRIM, 1, x, y, side, x + t, y + h, side + 4f)
            box3(Role.TRIM, 1, x + w - t, y, side, x + w, y + h, side + 4f)
        }
    }
    if (bolts > 0) {
        rivets(x + 6f, y + 6f, x + w - 6f, y + 6f, bolts, z + 4f, 3f, mirrored)
        rivets(x + 6f, y + h - 6f, x + w - 6f, y + h - 6f, bolts, z + 4f, 3f, mirrored)
    }
}

/** A driver's or gunner's vision block: a dark slit inside a proud armoured rim. */
fun SolidBuilder.visionPort(
    x: Float, y: Float, w: Float, h: Float, z: Float, mirrored: Boolean = false,
) = solid {
    for (side in if (mirrored) listOf(z, -z - 7f) else listOf(z)) {
        box3(Role.BODY, 1, x - 5f, y - 5f, side, x + w + 5f, y + h + 5f, side + 7f)
        box3(Role.DARK, -4, x, y, side + 7f, x + w, y + h, side + 9f)
    }
}

/** A round hatch on a horizontal deck, with its rim and a grab handle. */
fun SolidBuilder.deckHatch(cx: Float, cz: Float, y: Float, r: Float) = solid {
    cylinderY(Role.BODY, 1, cx, cz, r, y - 7f, y + 2f, 18)
    cylinderY(Role.TRIM, 2, cx, cz, r * 0.82f, y - 11f, y - 6f, 18)
    box3(Role.METAL, 0, cx - r * 0.5f, y - 17f, cz - 3f, cx + r * 0.5f, y - 13f, cz + 3f)
}

/** A ventilator dome on a deck or turret roof. */
fun SolidBuilder.ventilator(cx: Float, cz: Float, y: Float, r: Float) = solid {
    cylinderY(Role.BODY, 1, cx, cz, r, y - 12f, y + 2f, 16)
    sphere(Role.BODY, 2, cx, y - 12f, cz, r * 0.95f, 12, 6)
}

/** A grab handle - a U of bar stock, on the flank. */
fun SolidBuilder.grabHandle(
    x: Float, y: Float, w: Float, z: Float, mirrored: Boolean = true,
) = solid {
    val t = 2.2f
    val out = 7f
    // Painted the same colour as the hull, like the real thing - bare metal here reads
    // as chrome trim and pulls the eye straight off the vehicle.
    for (side in if (mirrored) listOf(z, -z - out) else listOf(z)) {
        box3(Role.BODY, -2, x, y, side, x + t * 2f, y + 10f, side + out)
        box3(Role.BODY, -2, x + w - t * 2f, y, side, x + w, y + 10f, side + out)
        box3(Role.BODY, 0, x, y, side + out - t, x + w, y + t * 2f, side + out)
    }
}

/** Towing eye at the nose or tail. */
fun SolidBuilder.towHook(x: Float, y: Float, z: Float, mirrored: Boolean = true) = solid {
    for (side in if (mirrored) listOf(z, -z - 9f) else listOf(z)) {
        box3(Role.BODY, -2, x, y, side, x + 19f, y + 12f, side + 9f)
        box3(Role.DARK, -4, x + 6f, y + 3f, side + 2f, x + 13f, y + 9f, side + 8f)
    }
}

/** Pioneer tools strapped to a deck - shovel, crowbar, axe. */
fun SolidBuilder.pioneerTools(x: Float, y: Float, z: Float, mirrored: Boolean = true) = solid {
    for (side in if (mirrored) listOf(z, -z - 9f) else listOf(z)) {
        box3(Role.TRIM, 0, x, y, side, x + 96f, y + 7f, side + 9f)          // shovel haft
        box3(Role.METAL, 1, x + 96f, y - 6f, side, x + 122f, y + 13f, side + 9f)
        box3(Role.METAL, 0, x + 14f, y + 12f, side, x + 108f, y + 18f, side + 7f)  // crowbar
    }
}

/**
 * Track links laid round the belt path, with guide horns down the centre.
 *
 * The single most valuable piece of detail on a tank: tracks are what the eye follows,
 * and a smooth band round the running gear looks like a rubber belt. [path] is a closed
 * loop of x,y pairs in blueprint space.
 */
fun SolidBuilder.trackLinks(
    inner: Float, outer: Float, linkLength: Float = 30f, vararg path: Float,
) = solid {
    val points = path.size / 2
    if (points < 3) return@solid
    var carry = 0f
    for (i in 0 until points) {
        val j = (i + 1) % points
        val ax = path[i * 2]; val ay = path[i * 2 + 1]
        val bx = path[j * 2]; val by = path[j * 2 + 1]
        val dx = bx - ax; val dy = by - ay
        val len = kotlin.math.sqrt(dx * dx + dy * dy)
        if (len < 1e-3f) continue
        val ux = dx / len; val uy = dy / len
        var t = carry
        while (t < len) {
            val cx = ax + ux * t
            val cy = ay + uy * t
            link(cx, cy, ux, uy, inner, outer, linkLength)
            t += linkLength
        }
        carry = t - len
    }
}

/**
 * The standard dressing for a hull flank: a weld bead along the top joint, a run of
 * fasteners if the vehicle was riveted, grab handles and a tow hook.
 *
 * How a hull was joined is itself a piece of history worth showing. WWI and interwar
 * hulls were riveted, and rivets popped off inside the crew compartment when the plate
 * was struck. The T-34 was welded and cast because the Soviet Union had no time for
 * anything slower. Modern composite armour has neither - just bolted panels.
 */
fun SolidBuilder.hullDressing(
    xRear: Float, xFront: Float, yTop: Float, yBottom: Float, side: Float,
    riveted: Boolean = false, handles: Int = 2, hooks: Boolean = true,
) {
    weld(xRear + 20f, yTop, xFront - 20f, yTop, side)
    if (riveted) {
        rivets(xRear + 30f, yBottom - 12f, xFront - 30f, yBottom - 12f, 14, side, r = 4.5f)
        rivets(xRear + 30f, yTop + 14f, xFront - 30f, yTop + 14f, 14, side, r = 4.5f)
    }
    val span = xFront - xRear
    for (i in 0 until handles) {
        val x = xRear + span * (0.28f + 0.34f * i)
        grabHandle(x, yTop + (yBottom - yTop) * 0.45f, 38f, side)
    }
    if (hooks) {
        towHook(xFront - 34f, yBottom - 34f, side * 0.62f)
        towHook(xRear + 8f, yBottom - 34f, side * 0.62f)
    }
}

/**
 * The centre line of a track run as a closed loop, ready for [trackLinks]: two straight
 * runs joined by an arc at each end, matching what [trackBelt] draws.
 */
fun ovalPath(
    xRear: Float, xFront: Float, yTop: Float, yBot: Float, th: Float, arcSteps: Int = 9,
): FloatArray {
    val r = (yBot - yTop) * 0.5f
    val mid = r - th * 0.5f
    val cy = (yTop + yBot) * 0.5f
    val cxF = xFront - r
    val cxR = xRear + r
    val out = ArrayList<Float>()
    out.add(cxR); out.add(cy - mid)
    out.add(cxF); out.add(cy - mid)
    for (i in 1 until arcSteps) {
        val a = -TWO_PI / 4f + (TWO_PI / 2f) * i / arcSteps
        out.add(cxF + mid * kotlin.math.cos(a))
        out.add(cy + mid * kotlin.math.sin(a))
    }
    out.add(cxF); out.add(cy + mid)
    out.add(cxR); out.add(cy + mid)
    for (i in 1 until arcSteps) {
        val a = TWO_PI / 4f + (TWO_PI / 2f) * i / arcSteps
        out.add(cxR + mid * kotlin.math.cos(a))
        out.add(cy + mid * kotlin.math.sin(a))
    }
    return out.toFloatArray()
}

/**
 * One track link: a shoe standing proud of the belt, with a guide horn on its inner
 * face. Built from explicit corners rather than an axis-aligned box, because half of
 * these sit on the curve round the idler and sprocket.
 */
private fun Mesh.link(
    cx: Float, cy: Float, ux: Float, uy: Float,
    inner: Float, outer: Float, length: Float,
) {
    // Across the run, pointing out of the belt.
    val nx = -uy
    val ny = ux
    val half = length * 0.40f
    val rise = 9f                                  // how far the shoe stands proud
    val sink = 4f                                  // and how far it beds into the belt

    // Four corners of the shoe in the plane of the run.
    val ax = cx - ux * half - nx * sink; val ay = cy - uy * half - ny * sink
    val bx = cx + ux * half - nx * sink; val by = cy + uy * half - ny * sink
    val ex = cx + ux * half + nx * rise; val ey = cy + uy * half + ny * rise
    val fx = cx - ux * half + nx * rise; val fy = cy - uy * half + ny * rise

    for (sign in listOf(1f, -1f)) {
        val z0 = if (sign > 0) inner + 2f else -outer + 2f
        val z1 = if (sign > 0) outer - 2f else -inner - 2f
        fun p(x: Float, y: Float, z: Float) = floatArrayOf(x, Mesh.wy(y), z)
        val a0 = p(ax, ay, z0); val b0 = p(bx, by, z0)
        val e0 = p(ex, ey, z0); val f0 = p(fx, fy, z0)
        val a1 = p(ax, ay, z1); val b1 = p(bx, by, z1)
        val e1 = p(ex, ey, z1); val f1 = p(fx, fy, z1)

        // Outer face, the one that meets the ground.
        quadFacing(
            f0[0], f0[1], f0[2], e0[0], e0[1], e0[2],
            e1[0], e1[1], e1[2], f1[0], f1[1], f1[2],
            nx, -ny, 0f, Role.DARK, 1,
        )
        // Leading and trailing faces of the shoe.
        quadFacing(
            b0[0], b0[1], b0[2], e0[0], e0[1], e0[2],
            e1[0], e1[1], e1[2], b1[0], b1[1], b1[2],
            ux, -uy, 0f, Role.DARK, -1,
        )
        quadFacing(
            a0[0], a0[1], a0[2], f0[0], f0[1], f0[2],
            f1[0], f1[1], f1[2], a1[0], a1[1], a1[2],
            -ux, uy, 0f, Role.DARK, -2,
        )
        // Sides.
        quadFacing(
            a0[0], a0[1], a0[2], b0[0], b0[1], b0[2],
            e0[0], e0[1], e0[2], f0[0], f0[1], f0[2],
            0f, 0f, -1f, Role.DARK, 0,
        )
        quadFacing(
            a1[0], a1[1], a1[2], b1[0], b1[1], b1[2],
            e1[0], e1[1], e1[2], f1[0], f1[1], f1[2],
            0f, 0f, 1f, Role.DARK, 0,
        )
    }

    // Guide horns, one pair down the middle of the belt on the inboard face.
    val hz = (inner + outer) * 0.5f
    for (sign in listOf(1f, -1f)) {
        val z = hz * sign
        box3(
            Role.DARK, 2,
            cx - nx * 12f - 5f, cy - ny * 12f - 5f, z - 6f,
            cx - nx * 12f + 5f, cy - ny * 12f + 5f, z + 6f,
        )
    }
}

// ---------------------------------------------------------------------------
// Aircraft
// ---------------------------------------------------------------------------

/**
 * Former rings standing proud of a fuselage, the way stringers show through a skin that
 * was never quite flush. On fabric aircraft they are the structure; on metal ones they
 * are the panel joints.
 */
fun SolidBuilder.fuselageRings(
    x0: Float, x1: Float, y0: Float, r0: Float, y1: Float = y0, r1: Float = r0,
    count: Int = 6,
) = solid {
    for (i in 0 until count) {
        val t = if (count > 1) i / (count - 1f) else 0.5f
        val x = x0 + (x1 - x0) * t
        val y = y0 + (y1 - y0) * t
        val r = r0 + (r1 - r0) * t
        revolveX(Role.BODY, 1, 18, x - 2f, y, r * 1.008f, x + 2f, y, r * 1.008f)
    }
}

/**
 * Rib tapes across a fabric wing. The doped linen sagged between the ribs, so a WWI
 * wing is a row of shallow scallops - one of the clearest ways to tell a 1917 aeroplane
 * from a 1941 one at a glance.
 */
fun SolidBuilder.fabricRibs(
    xLe: Float, xTe: Float, y: Float, root: Float, span: Float, count: Int,
) = solid {
    val x0 = minOf(xLe, xTe)
    val x1 = maxOf(xLe, xTe)
    for (i in 0 until count) {
        val t = (i + 0.5f) / count
        val z = root + (span - root) * t
        for (side in listOf(z, -z)) {
            box3(Role.BODY, 1, x0 + 6f, y - 13f, side - 2f, x1 - 4f, y - 10f, side + 2f)
        }
    }
}

/** Pitot tube on a wing leading edge or nose. */
fun SolidBuilder.pitot(x: Float, y: Float, len: Float, z: Float) = solid {
    box3(Role.METAL, 0, x, y - 2f, z - 2f, x + len, y + 2f, z + 2f)
    box3(Role.METAL, -1, x + len * 0.55f, y, z - 1.5f, x + len * 0.62f, y + 11f, z + 1.5f)
}

/** Aerial mast and wire from the fin to the fuselage. */
fun SolidBuilder.aerial(xMast: Float, yMast: Float, xTail: Float, yTail: Float) = solid {
    box3(Role.METAL, 0, xMast - 2.5f, yMast - 26f, -2.5f, xMast + 2.5f, yMast, 2.5f)
    val steps = 8
    for (i in 0 until steps) {
        val t0 = i / steps.toFloat()
        val t1 = (i + 1) / steps.toFloat()
        box3(
            Role.DARK, 0,
            minOf(xMast + (xTail - xMast) * t0, xMast + (xTail - xMast) * t1),
            minOf(yMast - 26f + (yTail - (yMast - 26f)) * t0, yMast - 26f + (yTail - (yMast - 26f)) * t1) - 1f,
            -1f,
            maxOf(xMast + (xTail - xMast) * t0, xMast + (xTail - xMast) * t1),
            maxOf(yMast - 26f + (yTail - (yMast - 26f)) * t0, yMast - 26f + (yTail - (yMast - 26f)) * t1) + 1f,
            1f,
        )
    }
}

/** Wingtip navigation light. */
fun SolidBuilder.navLight(x: Float, y: Float, z: Float) = solid {
    sphere(Role.GLASS, 2, x, y, z, 6f, 10, 6)
    sphere(Role.GLASS, 2, x, y, -z, 6f, 10, 6)
}

// ---------------------------------------------------------------------------
// Warships
// ---------------------------------------------------------------------------

/** Planking laid along a wooden weather deck. */
fun SolidBuilder.decking(x0: Float, x1: Float, y: Float, beam: Float, planks: Int = 9) = solid {
    for (i in 0 until planks) {
        val t = (i + 0.5f) / planks
        val z = -beam + 2f * beam * t
        box3(Role.TRIM, if (i % 2 == 0) 1 else 0, x0, y - 2f, z - beam / planks * 0.86f,
            x1, y, z + beam / planks * 0.86f)
    }
}

/** Guard rails: stanchions with two wires run between them. */
fun SolidBuilder.railing(x0: Float, x1: Float, y: Float, beam: Float, posts: Int = 12) = solid {
    val h = 22f
    for (side in listOf(beam, -beam)) {
        for (i in 0 until posts) {
            val x = x0 + (x1 - x0) * (i / (posts - 1f))
            box3(Role.METAL, 0, x - 1.8f, y - h, side - 1.8f, x + 1.8f, y, side + 1.8f)
        }
        box3(Role.METAL, 1, x0, y - h, side - 1.2f, x1, y - h + 2.4f, side + 1.2f)
        box3(Role.METAL, 1, x0, y - h * 0.5f, side - 1.2f, x1, y - h * 0.5f + 2.4f, side + 1.2f)
    }
}

/** A row of scuttles along the hull side. */
fun SolidBuilder.portholes(
    x0: Float, x1: Float, y: Float, beam: Float, count: Int, r: Float = 6f,
) = solid {
    for (i in 0 until count) {
        val x = x0 + (x1 - x0) * (i / (count - 1f).coerceAtLeast(1f))
        for (side in listOf(beam, -beam - 4f)) {
            cylinderZ(Role.METAL, 1, x, y, r, side, side + 4f, 12)
            cylinderZ(Role.GLASS, 2, x, y, r * 0.62f, side + 4f, side + 5f, 12)
        }
    }
}

/** Bollards and a capstan on the forecastle. */
fun SolidBuilder.deckFittings(x: Float, y: Float, beam: Float) = solid {
    for (side in listOf(beam * 0.72f, -beam * 0.72f)) {
        cylinderY(Role.METAL, 0, x, side, 7f, y - 18f, y, 10)
        cylinderY(Role.METAL, 0, x + 34f, side, 7f, y - 18f, y, 10)
    }
    cylinderY(Role.METAL, 1, x + 76f, 0f, 17f, y - 22f, y, 14)
}

/** Accommodation ladder down the ship's side. */
fun SolidBuilder.shipLadder(x: Float, yTop: Float, yBottom: Float, beam: Float) = solid {
    val steps = 7
    for (side in listOf(beam, -beam - 5f)) {
        box3(Role.METAL, 0, x - 2f, yTop, side, x + 2f, yBottom, side + 5f)
        box3(Role.METAL, 0, x + 24f, yTop, side, x + 28f, yBottom, side + 5f)
        for (i in 0 until steps) {
            val sy = yTop + (yBottom - yTop) * (i + 0.5f) / steps
            box3(Role.METAL, 1, x, sy - 1.5f, side, x + 28f, sy + 1.5f, side + 5f)
        }
    }
}
