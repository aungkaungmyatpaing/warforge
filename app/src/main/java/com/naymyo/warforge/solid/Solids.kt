package com.naymyo.warforge.solid

import com.naymyo.warforge.poly.Poly
import com.naymyo.warforge.poly.TWO_PI
import com.naymyo.warforge.solid.Mesh.Companion.wy
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Turns the game's 2D side-profile art into solids.
 *
 * A tank, a ship's hull, a wing - almost every shape here is a flat profile with a
 * thickness, so extruding the existing facet soup gives a genuine 3D model without
 * re-authoring a single vehicle. Barrels and wheels are the exception and get their own
 * primitives, because a swept rectangle makes a very poor gun tube.
 */

/** Canonical undirected edge, quantised so shared vertices actually compare equal. */
private data class EdgeKey(val ax: Int, val ay: Int, val bx: Int, val by: Int) {
    companion object {
        private const val Q = 16f

        fun of(x0: Float, y0: Float, x1: Float, y1: Float): EdgeKey {
            val ax = Math.round(x0 * Q); val ay = Math.round(y0 * Q)
            val bx = Math.round(x1 * Q); val by = Math.round(y1 * Q)
            return if (ax < bx || (ax == bx && ay <= by)) EdgeKey(ax, ay, bx, by)
            else EdgeKey(bx, by, ax, ay)
        }
    }
}

/** A boundary edge and the direction that points away from the facet that owns it. */
private class Rim(
    val x0: Float, val y0: Float, val x1: Float, val y1: Float,
    val nx: Float, val ny: Float,
    val role: Int, val tone: Int,
)

/**
 * Extrudes a facet soup between depths [z0] and [z1].
 *
 * Side walls are raised only on **boundary** edges - those belonging to exactly one
 * facet. Walling every edge would bury a wall inside the solid at each shared diagonal,
 * which is invisible while the part is opaque but shows up as a mess of internal panels
 * the moment X-ray mode makes it translucent.
 */
fun Mesh.extrude(polys: List<Poly>, z0: Float, z1: Float) {
    val front = maxOf(z0, z1)
    val back = minOf(z0, z1)
    val rims = boundaryEdges(polys)
    // Width of the shading bevel along each edge, held to a fraction of the part so a
    // thin plate does not end up all bevel.
    val bevel = ((front - back) * 0.22f).coerceIn(0.6f, 7f)
    val corners = rimVertices(rims)

    // Caps, fan-triangulated, on both faces. Vertices on the silhouette get their
    // normals tilted outward, which rounds the edge to the eye without changing the
    // outline - the difference between a flat-shaded plate and a machined one.
    for (p in polys) {
        val n = p.pts.size / 2
        if (n < 3) continue
        for (i in 1 until n - 1) {
            capTriangle(p, 0, i, i + 1, front, 1f, corners)
            capTriangle(p, 0, i, i + 1, back, -1f, corners)
        }
    }

    for (rim in rims) {
        val nx = rim.nx
        val ny = -rim.ny
        // Three bands up the wall: the outer two lean their normals toward the cap, so
        // the corner catches a highlight the way a real chamfer does.
        wallBand(rim, back, back + bevel, nx, ny, -BEVEL_LEAN, 0f)
        wallBand(rim, back + bevel, front - bevel, nx, ny, 0f, 0f)
        wallBand(rim, front - bevel, front, nx, ny, 0f, BEVEL_LEAN)
    }
}

/** How far a bevelled edge's normal leans toward the face it meets. */
private const val BEVEL_LEAN = 0.72f

private fun Mesh.capTriangle(
    p: Poly, i0: Int, i1: Int, i2: Int, z: Float, facing: Float,
    corners: Map<Long, FloatArray>,
) {
    fun normalAt(i: Int): FloatArray {
        val lean = corners[vertexKey(p.pts[i * 2], p.pts[i * 2 + 1])]
        return if (lean == null) floatArrayOf(0f, 0f, facing)
        else normalized(lean[0] * BEVEL_LEAN, -lean[1] * BEVEL_LEAN, facing)
    }
    val na = normalAt(i0); val nb = normalAt(i1); val nc = normalAt(i2)
    val ax = p.pts[i0 * 2]; val ay = p.pts[i0 * 2 + 1]
    val bx = p.pts[i1 * 2]; val by = p.pts[i1 * 2 + 1]
    val cx = p.pts[i2 * 2]; val cy = p.pts[i2 * 2 + 1]
    // Winding has to match the face direction, and the source art does not guarantee it.
    // Blueprint y points down, so a clockwise triangle on screen winds the other way here.
    val cross = (bx - ax) * (cy - ay) - (cx - ax) * (by - ay)
    if ((cross < 0f) == (facing > 0f)) {
        triN(
            ax, wy(ay), z, na[0], na[1], na[2],
            bx, wy(by), z, nb[0], nb[1], nb[2],
            cx, wy(cy), z, nc[0], nc[1], nc[2],
            p.role, p.tone,
        )
    } else {
        triN(
            ax, wy(ay), z, na[0], na[1], na[2],
            cx, wy(cy), z, nc[0], nc[1], nc[2],
            bx, wy(by), z, nb[0], nb[1], nb[2],
            p.role, p.tone,
        )
    }
}

/** One horizontal band of an extruded wall, with its normals leaned by [lean0]/[lean1]. */
private fun Mesh.wallBand(
    rim: Rim, zLow: Float, zHigh: Float, nx: Float, ny: Float, lean0: Float, lean1: Float,
) {
    if (zHigh - zLow < 1e-4f) return
    val a = normalized(nx, ny, lean0)
    val b = normalized(nx, ny, lean1)
    quadN(
        rim.x0, wy(rim.y0), zLow, a[0], a[1], a[2],
        rim.x1, wy(rim.y1), zLow, a[0], a[1], a[2],
        rim.x1, wy(rim.y1), zHigh, b[0], b[1], b[2],
        rim.x0, wy(rim.y0), zHigh, b[0], b[1], b[2],
        rim.role, rim.tone,
    )
}

/** Silhouette vertices and the direction each one faces outward, for the cap bevel. */
private fun rimVertices(rims: List<Rim>): Map<Long, FloatArray> {
    val out = HashMap<Long, FloatArray>()
    for (rim in rims) {
        for ((x, y) in listOf(rim.x0 to rim.y0, rim.x1 to rim.y1)) {
            val key = vertexKey(x, y)
            val existing = out[key]
            if (existing == null) {
                out[key] = floatArrayOf(rim.nx, rim.ny)
            } else {
                // A corner belongs to two edges; average their outward directions.
                val n = normalized2(existing[0] + rim.nx, existing[1] + rim.ny)
                existing[0] = n[0]
                existing[1] = n[1]
            }
        }
    }
    return out
}

private fun vertexKey(x: Float, y: Float): Long =
    (Math.round(x * 16f).toLong() shl 32) xor (Math.round(y * 16f).toLong() and 0xFFFFFFFFL)

private fun normalized(x: Float, y: Float, z: Float): FloatArray {
    val l = sqrt(x * x + y * y + z * z).coerceAtLeast(1e-6f)
    return floatArrayOf(x / l, y / l, z / l)
}

private fun normalized2(x: Float, y: Float): FloatArray {
    val l = sqrt(x * x + y * y).coerceAtLeast(1e-6f)
    return floatArrayOf(x / l, y / l)
}

/** Extrudes, then mirrors the result to the other side - track runs, wheels, wings. */
fun Mesh.extrudePair(polys: List<Poly>, zInner: Float, zOuter: Float) {
    val side = Mesh()
    side.extrude(polys, zInner, zOuter)
    addAll(side)
    addAll(side, mirrorZ = true)
}

private fun boundaryEdges(polys: List<Poly>): List<Rim> {
    val seen = HashMap<EdgeKey, Rim?>()
    for (p in polys) {
        val n = p.pts.size / 2
        if (n < 3) continue
        var cx = 0f
        var cy = 0f
        for (i in 0 until n) { cx += p.pts[i * 2]; cy += p.pts[i * 2 + 1] }
        cx /= n; cy /= n
        for (i in 0 until n) {
            val j = (i + 1) % n
            val x0 = p.pts[i * 2]; val y0 = p.pts[i * 2 + 1]
            val x1 = p.pts[j * 2]; val y1 = p.pts[j * 2 + 1]
            val key = EdgeKey.of(x0, y0, x1, y1)
            if (seen.containsKey(key)) {
                seen[key] = null                       // shared: interior, drop it
                continue
            }
            var nx = y1 - y0
            var ny = -(x1 - x0)
            val len = sqrt(nx * nx + ny * ny)
            if (len < 1e-4f) continue
            nx /= len; ny /= len
            // Point the normal away from the facet's own centre.
            if (nx * ((x0 + x1) * 0.5f - cx) + ny * ((y0 + y1) * 0.5f - cy) < 0f) {
                nx = -nx; ny = -ny
            }
            seen[key] = Rim(x0, y0, x1, y1, nx, ny, p.role, p.tone)
        }
    }
    return seen.values.filterNotNull()
}

/**
 * A solid of revolution about a horizontal axis - gun barrels, fuselages, funnels,
 * torpedoes, masts. [stations] is x, y, radius triples in blueprint coordinates, the
 * same spine the 2D `tube` builder takes.
 */
fun Mesh.revolveX(role: Int, tone: Int, sides: Int, vararg stations: Float) {
    if (stations.size < 6) return
    val n = sides.coerceAtLeast(12)
    val step = TWO_PI / n
    var i = 0
    while (i + 5 < stations.size) {
        val x0 = stations[i]; val y0 = wy(stations[i + 1]); val r0 = stations[i + 2]
        val x1 = stations[i + 3]; val y1 = wy(stations[i + 4]); val r1 = stations[i + 5]
        // Taper tilts the surface, so the normal leans along the axis by the slope.
        val dx = x1 - x0
        val dr = r1 - r0
        val slopeLen = sqrt(dx * dx + dr * dr).coerceAtLeast(1e-4f)
        val axial = -dr / slopeLen
        val radial = dx / slopeLen
        for (s in 0 until n) {
            val a0 = step * s
            val a1 = step * (s + 1)
            val c0 = cos(a0); val s0 = sin(a0)
            val c1 = cos(a1); val s1 = sin(a1)
            quadN(
                x0, y0 + r0 * c0, r0 * s0, axial, radial * c0, radial * s0,
                x1, y1 + r1 * c0, r1 * s0, axial, radial * c0, radial * s0,
                x1, y1 + r1 * c1, r1 * s1, axial, radial * c1, radial * s1,
                x0, y0 + r0 * c1, r0 * s1, axial, radial * c1, radial * s1,
                role, tone,
            )
        }
        i += 3
    }
    capRevolution(role, tone, n, stations, 0, -1f)
    capRevolution(role, tone, n, stations, stations.size - 3, 1f)
}

private fun Mesh.capRevolution(
    role: Int, tone: Int, sides: Int, stations: FloatArray, at: Int, dir: Float,
) {
    val x = stations[at]; val y = wy(stations[at + 1]); val r = stations[at + 2]
    if (r <= 0f) return
    val step = TWO_PI / sides
    for (s in 0 until sides) {
        val a0 = step * s
        val a1 = step * (s + 1)
        triFacing(
            x, y, 0f,
            x, y + r * cos(a0), r * sin(a0),
            x, y + r * cos(a1), r * sin(a1),
            dir, 0f, 0f, role, tone,
        )
    }
}

/**
 * A cylinder lying along the depth axis: road wheels, tyres, prop bosses, hatch rings.
 * [cx], [cy] are blueprint coordinates; [z0] and [z1] the two faces.
 */
fun Mesh.cylinderZ(
    role: Int, tone: Int, cx: Float, cy: Float, r: Float, z0: Float, z1: Float,
    sides: Int = 20, rotation: Float = 0f,
) {
    val n = sides.coerceAtLeast(12)
    val step = TWO_PI / n
    val yc = wy(cy)
    for (s in 0 until n) {
        val a0 = rotation + step * s
        val a1 = rotation + step * (s + 1)
        val c0 = cos(a0); val s0 = sin(a0)
        val c1 = cos(a1); val s1 = sin(a1)
        val x0 = cx + r * c0; val y0 = yc - r * s0
        val x1 = cx + r * c1; val y1 = yc - r * s1
        // Radial normals: this is what makes a road wheel read as round rather than
        // as a twenty-sided plate.
        quadN(
            x0, y0, z0, c0, -s0, 0f,
            x1, y1, z0, c1, -s1, 0f,
            x1, y1, z1, c1, -s1, 0f,
            x0, y0, z1, c0, -s0, 0f,
            role, tone,
        )
        triFacing(cx, yc, z1, x0, y0, z1, x1, y1, z1, 0f, 0f, 1f, role, tone)
        triFacing(cx, yc, z0, x0, y0, z0, x1, y1, z0, 0f, 0f, -1f, role, tone)
    }
}

/**
 * A cylinder standing on end - hatch rings, cupola drums, ventilators, funnels seen from
 * above. [y0] and [y1] are blueprint y, so y0 is the top of the object.
 */
fun Mesh.cylinderY(
    role: Int, tone: Int, cx: Float, cz: Float, r: Float, y0: Float, y1: Float,
    sides: Int = 20, rotation: Float = 0f,
) {
    val n = sides.coerceAtLeast(10)
    val step = TWO_PI / n
    val top = wy(minOf(y0, y1))
    val bottom = wy(maxOf(y0, y1))
    for (s in 0 until n) {
        val a0 = rotation + step * s
        val a1 = rotation + step * (s + 1)
        val c0 = cos(a0); val s0 = sin(a0)
        val c1 = cos(a1); val s1 = sin(a1)
        quadN(
            cx + r * c0, bottom, cz + r * s0, c0, 0f, s0,
            cx + r * c1, bottom, cz + r * s1, c1, 0f, s1,
            cx + r * c1, top, cz + r * s1, c1, 0f, s1,
            cx + r * c0, top, cz + r * s0, c0, 0f, s0,
            role, tone,
        )
        triFacing(
            cx, top, cz, cx + r * c0, top, cz + r * s0, cx + r * c1, top, cz + r * s1,
            0f, 1f, 0f, role, tone,
        )
        triFacing(
            cx, bottom, cz, cx + r * c0, bottom, cz + r * s0, cx + r * c1, bottom, cz + r * s1,
            0f, -1f, 0f, role, tone,
        )
    }
}

/** Mirrored pair of cylinders - one wheel per side. */
fun Mesh.cylinderPair(
    role: Int, tone: Int, cx: Float, cy: Float, r: Float, zInner: Float, zOuter: Float,
    sides: Int = 20, rotation: Float = 0f,
) {
    cylinderZ(role, tone, cx, cy, r, zInner, zOuter, sides, rotation)
    cylinderZ(role, tone, cx, cy, r, -zOuter, -zInner, sides, rotation)
}

/**
 * Axis-aligned box in blueprint x/y plus explicit depth. The workhorse for internal
 * modules - engines, ammo racks, fuel cells - which are blocks by nature.
 */
fun Mesh.box3(
    role: Int, tone: Int,
    x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float,
) {
    val ax = minOf(x0, x1); val bx = maxOf(x0, x1)
    val ay = wy(maxOf(y0, y1)); val by = wy(minOf(y0, y1))   // flip: blueprint y is down
    val az = minOf(z0, z1); val bz = maxOf(z0, z1)
    // +Z / -Z
    quadFacing(ax, ay, bz, bx, ay, bz, bx, by, bz, ax, by, bz, 0f, 0f, 1f, role, tone)
    quadFacing(ax, ay, az, bx, ay, az, bx, by, az, ax, by, az, 0f, 0f, -1f, role, tone)
    // +X / -X
    quadFacing(bx, ay, az, bx, ay, bz, bx, by, bz, bx, by, az, 1f, 0f, 0f, role, tone)
    quadFacing(ax, ay, az, ax, ay, bz, ax, by, bz, ax, by, az, -1f, 0f, 0f, role, tone)
    // +Y / -Y
    quadFacing(ax, by, az, bx, by, az, bx, by, bz, ax, by, bz, 0f, 1f, 0f, role, tone)
    quadFacing(ax, ay, az, bx, ay, az, bx, ay, bz, ax, ay, bz, 0f, -1f, 0f, role, tone)
}

/** Faceted ball - crew heads, radar domes, cupola tops. */
fun Mesh.sphere(
    role: Int, tone: Int, cx: Float, cy: Float, cz: Float, r: Float,
    segments: Int = 16, rings: Int = 10,
) {
    val yc = wy(cy)
    val seg = segments.coerceAtLeast(8)
    val rng = rings.coerceAtLeast(5)

    // Unit direction at a grid point: on a sphere that *is* the normal, so smooth
    // shading comes free and the ball has no facets at all.
    fun dir(ring: Int, s: Int): FloatArray {
        val phi = (Math.PI * ring / rng).toFloat()
        val theta = TWO_PI * s / seg
        return floatArrayOf(sin(phi) * cos(theta), cos(phi), sin(phi) * sin(theta))
    }

    for (ring in 0 until rng) {
        for (s in 0 until seg) {
            val a = dir(ring, s)
            val b = dir(ring + 1, s)
            val c = dir(ring + 1, s + 1)
            val d = dir(ring, s + 1)
            quadN(
                cx + r * a[0], yc + r * a[1], cz + r * a[2], a[0], a[1], a[2],
                cx + r * b[0], yc + r * b[1], cz + r * b[2], b[0], b[1], b[2],
                cx + r * c[0], yc + r * c[1], cz + r * c[2], c[0], c[1], c[2],
                cx + r * d[0], yc + r * d[1], cz + r * d[2], d[0], d[1], d[2],
                role, tone,
            )
        }
    }
}
