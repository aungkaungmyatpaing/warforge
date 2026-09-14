package com.maddog.warforge.data.parts

import com.maddog.warforge.poly.Role
import com.maddog.warforge.poly.TWO_PI

/**
 * Reusable running-gear and fittings for ground vehicles.
 *
 * Every vehicle is drawn in side view with the nose pointing right; the builders here
 * add the depth that turns that profile into a solid. Track runs and wheels are built
 * once and mirrored, so a call describes both sides of the vehicle.
 */

/** Where a tank's running gear sits, as distances out from the centreline. */
const val TRACK_INNER = 104f
const val TRACK_OUTER = 150f
const val WHEEL_INNER = 96f
const val WHEEL_OUTER = 142f

/**
 * A closed track belt: straight top and bottom runs joined by an arc at each end.
 * Hollow, so the road wheels dropped in later show through the middle.
 */
fun SolidBuilder.trackBelt(
    xRear: Float, xFront: Float, yTop: Float, yBot: Float, th: Float = 20f,
    inner: Float = TRACK_INNER, outer: Float = TRACK_OUTER,
) = depth(inner, outer, mirrored = true) {
    val r = (yBot - yTop) * 0.5f
    val cy = (yTop + yBot) * 0.5f
    val cxF = xFront - r
    val cxR = xRear + r
    quad(Role.DARK, 1, cxR, yTop, cxF, yTop, cxF, yTop + th, cxR, yTop + th)   // top run
    quad(Role.DARK, -2, cxR, yBot - th, cxF, yBot - th, cxF, yBot, cxR, yBot)  // bottom run
    ring(Role.DARK, 0, cxF, cy, r, r - th, sides = 7, start = -TWO_PI / 4f, sweep = TWO_PI / 2f)
    ring(Role.DARK, 0, cxR, cy, r, r - th, sides = 7, start = TWO_PI / 4f, sweep = TWO_PI / 2f)
    trackLinks(cxR, cxF, yBot - th, th)
}

/** The tread ticks along the bottom run. Pure texture, but it sells the scale. */
private fun SolidBuilder.trackLinks(x0: Float, x1: Float, y: Float, th: Float) {
    val step = 26f
    var x = x0
    var i = 0
    while (x + step < x1) {
        if (i % 2 == 0) box(Role.DARK, -4, x + 5f, y + th * 0.45f, step - 10f, th * 0.5f)
        x += step
        i++
    }
}

/**
 * Road wheels, drive sprocket (rear) and idler (front) sized to sit inside a
 * [trackBelt] of the same span. Real discs in 3D, not extruded circles.
 */
fun SolidBuilder.roadWheels(
    xRear: Float, xFront: Float, cy: Float, r: Float, count: Int,
    driveR: Float = r * 1.2f, returnRollers: Boolean = false, rollerY: Float = cy - r,
    inner: Float = WHEEL_INNER, outer: Float = WHEEL_OUTER,
) {
    val beltR = r * 1.45f
    val first = xRear + beltR
    val last = xFront - beltR
    sprocket(first, cy, driveR, inner = inner, outer = outer)
    sprocket(last, cy, driveR, inner = inner, outer = outer)
    if (count > 0) {
        val innerFrom = first + driveR + r * 0.9f
        val innerTo = last - driveR - r * 0.9f
        val step = if (count > 1) (innerTo - innerFrom) / (count - 1) else 0f
        for (i in 0 until count) {
            val cx = if (count > 1) innerFrom + step * i else (innerFrom + innerTo) * 0.5f
            disc(Role.METAL, 0, cx, cy, r, inner, outer, sides = 24)
            disc(Role.DARK, 1, cx, cy, r * 0.34f, outer, outer + 5f, sides = 7)
        }
    }
    if (returnRollers) {
        val n = 3
        val from = first + driveR
        val step = (last - driveR - from) / n
        for (i in 0..n) {
            disc(Role.METAL, -1, from + step * i, rollerY, r * 0.32f, inner + 14f, outer - 6f, sides = 8)
        }
    }
}

/** Toothed drive wheel - the teeth are what tell it apart from a road wheel. */
fun SolidBuilder.sprocket(
    cx: Float, cy: Float, r: Float, teeth: Int = 9,
    inner: Float = WHEEL_INNER, outer: Float = WHEEL_OUTER,
) {
    disc(Role.METAL, -1, cx, cy, r, inner, outer, sides = teeth)
    val step = TWO_PI / teeth
    for (i in 0 until teeth) {
        val a = step * i
        val x = cx + r * kotlin.math.cos(a)
        val y = cy + r * kotlin.math.sin(a)
        disc(Role.METAL, -2, x, y, r * 0.16f, inner + 6f, outer - 6f, sides = 5, rotation = a)
    }
    disc(Role.DARK, 1, cx, cy, r * 0.3f, outer, outer + 6f, sides = 7)
}

/** Rubber tyre with a hub, for wheeled vehicles. */
fun SolidBuilder.tyre(
    cx: Float, cy: Float, r: Float, inner: Float = WHEEL_INNER, outer: Float = WHEEL_OUTER,
) {
    disc(Role.DARK, -1, cx, cy, r, inner, outer, sides = 24)
    disc(Role.METAL, 1, cx, cy, r * 0.52f, outer, outer + 6f, sides = 9)
    disc(Role.DARK, 0, cx, cy, r * 0.2f, outer + 6f, outer + 10f, sides = 7)
}

/**
 * Main gun: barrel, mantlet and optional muzzle brake / fume extractor. [x] is where the
 * barrel leaves the mantlet, and the gun always points right.
 */
fun SolidBuilder.mainGun(
    x: Float, y: Float, len: Float, r: Float,
    mantlet: Boolean = true, brake: Boolean = false, extractor: Boolean = false,
    mantletHalf: Float = 54f,
) {
    if (mantlet) depth(-mantletHalf, mantletHalf) {
        slab(Role.METAL, 0, x - 34f, y - r * 3.4f, x + 6f, y - r * 2.2f, x + 6f, y + r * 2.2f, x - 34f, y + r * 3.4f)
    }
    bar(Role.METAL, 0, 24, x, y, r * 1.35f, x + len * 0.35f, y, r, x + len, y, r * 0.88f)
    if (extractor) {
        bar(Role.METAL, 1, 20, x + len * 0.52f, y, r * 1.7f, x + len * 0.68f, y, r * 1.7f)
    }
    if (brake) {
        bar(Role.METAL, 1, 20, x + len - 4f, y, r * 1.9f, x + len + 30f, y, r * 1.9f)
        bar(Role.DARK, -3, 16, x + len + 6f, y, r * 0.7f, x + len + 14f, y, r * 0.7f)
    }
}

/** Commander's cupola with a hatch ring. */
fun SolidBuilder.cupola(
    cx: Float, yBase: Float, w: Float, h: Float, periscope: Boolean = true,
) = depth(-w * 0.5f, w * 0.5f) {
    slab(Role.BODY, 1, cx - w * 0.5f, yBase, cx - w * 0.4f, yBase - h, cx + w * 0.4f, yBase - h, cx + w * 0.5f, yBase)
    box(Role.DARK, 1, cx - w * 0.45f, yBase - h - 6f, w * 0.9f, 7f)
    if (periscope) box(Role.GLASS, 1, cx - w * 0.16f, yBase - h + 5f, w * 0.32f, 8f)
}

/** Pintle machine gun. */
fun SolidBuilder.machineGun(x: Float, y: Float, len: Float = 74f, side: Float = 0f) {
    bar(Role.METAL, -1, 14, x, y, 3.5f, x + len, y, 3f)
    depth(side - 11f, side + 11f) {
        box(Role.DARK, 0, x - 16f, y - 9f, 22f, 18f)
        box(Role.METAL, -2, x - 6f, y + 6f, 8f, 16f)
    }
}

/** Exhaust muffler running along the hull side. */
fun SolidBuilder.exhaust(x: Float, y: Float, len: Float, r: Float = 13f, side: Float = 96f) {
    bar(Role.DARK, 0, 18, x, y, r, x + len + 1f, y, r * 0.9f)
    depth(side - r, side + r) { box(Role.DARK, -3, x, y - r, 4f, r * 2f) }
}

/** Whip antenna with its base. */
fun SolidBuilder.antenna(x: Float, y: Float, h: Float) = depth(-7f, 7f) {
    box(Role.DARK, 0, x - 7f, y - 10f, 14f, 12f)
    quad(Role.METAL, 1, x - 2.5f, y - 10f, x + 2.5f, y - 10f, x + 7f, y - h, x + 4f, y - h, split = 0)
}

/** Stowage box / tool bin. */
fun SolidBuilder.stowage(
    x: Float, y: Float, w: Float, h: Float, back: Float = -70f, front: Float = 70f,
) = depth(back, front) {
    slab(Role.TRIM, 0, x, y, x + w, y - 3f, x + w, y + h, x, y + h)
    box(Role.DARK, -2, x + w * 0.2f, y + h * 0.35f, w * 0.6f, 4f)
}

/** Mudguard / fender running the length of the hull. */
fun SolidBuilder.fender(
    xRear: Float, xFront: Float, y: Float, th: Float = 13f,
    inner: Float = TRACK_INNER - 4f, outer: Float = TRACK_OUTER + 6f,
) = depth(inner, outer, mirrored = true) {
    quad(Role.TRIM, 1, xRear, y, xFront, y - 4f, xFront, y - 4f + th, xRear, y + th)
    box(Role.DARK, -3, xRear, y + th, 18f, 16f)
    box(Role.DARK, -3, xFront - 18f, y + th - 4f, 18f, 16f)
}

/**
 * A track belt that follows an arbitrary convex outline instead of the usual oval -
 * the WWI rhomboids ran their tracks right around the hull, which is the whole
 * silhouette people recognise them by.
 *
 * The inner edge is found by pulling each vertex [th] toward the outline's centroid,
 * which is accurate enough for the broad, roughly regular shapes used here.
 */
fun SolidBuilder.beltLoop(
    th: Float, vararg pts: Float,
    inner: Float = TRACK_INNER, outer: Float = TRACK_OUTER,
) = depth(inner, outer, mirrored = true) {
    val n = pts.size / 2
    if (n >= 3) {
        var cx = 0f
        var cy = 0f
        for (i in 0 until n) { cx += pts[i * 2]; cy += pts[i * 2 + 1] }
        cx /= n; cy /= n
        val innerPts = FloatArray(pts.size)
        for (i in 0 until n) {
            val dx = cx - pts[i * 2]
            val dy = cy - pts[i * 2 + 1]
            val d = kotlin.math.sqrt(dx * dx + dy * dy).coerceAtLeast(0.001f)
            innerPts[i * 2] = pts[i * 2] + dx / d * th
            innerPts[i * 2 + 1] = pts[i * 2 + 1] + dy / d * th
        }
        for (i in 0 until n) {
            val j = (i + 1) % n
            // Facets on the upper half of the loop catch the light, lower ones fall away.
            val tone = if ((pts[i * 2 + 1] + pts[j * 2 + 1]) * 0.5f < cy) 1 else -2
            quad(
                Role.DARK, tone,
                pts[i * 2], pts[i * 2 + 1], pts[j * 2], pts[j * 2 + 1],
                innerPts[j * 2], innerPts[j * 2 + 1], innerPts[i * 2], innerPts[i * 2 + 1],
                split = 0,
            )
        }
    }
}
