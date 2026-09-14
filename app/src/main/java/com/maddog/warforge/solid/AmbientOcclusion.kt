package com.maddog.warforge.solid

import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Bakes per-vertex ambient occlusion over a whole vehicle.
 *
 * This is the single biggest thing separating a flat-shaded model from one that looks
 * solid. Without it a turret sits on a hull with no seam, a road wheel floats inside its
 * track, and every recess is exactly as bright as the panel beside it. With it, parts
 * darken where they meet - and the eye reads them as touching.
 *
 * The method is deliberately coarse: voxelise every triangle of the vehicle into an
 * occupancy grid, then march a handful of rays out of each vertex and count how many run
 * into something. Coarse is the point - it runs in a few tens of milliseconds when a
 * vehicle is opened, and at this scale nobody can tell it from the real thing.
 */
object AmbientOcclusion {

    /** Cells along the model's longest axis. */
    private const val RESOLUTION = 56

    /** Rays per vertex. */
    private const val RAYS = 9

    /** How far a ray looks, as a fraction of the model's longest axis. */
    private const val REACH = 0.20f

    /** How dark full occlusion gets. */
    private const val STRENGTH = 0.80f

    /**
     * Returns one occlusion value per vertex of each mesh, in the same order the meshes
     * were given, ready to be multiplied into the vertex colours.
     */
    fun bake(meshes: List<Mesh>): List<FloatArray> {
        val grid = Grid.of(meshes) ?: return meshes.map { FloatArray(it.triangleCount * 3) { 1f } }
        return meshes.map { mesh ->
            // A mesh that arrived from Blender already has ray-traced occlusion folded
            // into its vertex colours, and a far better one than this. It still goes
            // into the grid above, because it has to darken the parts around it - it
            // just does not need sampling again, which is the expensive half.
            if (mesh.hasTints) FloatArray(mesh.triangleCount * 3) { 1f }
            else occlude(mesh, grid)
        }
    }

    private fun occlude(mesh: Mesh, grid: Grid): FloatArray {
        val count = mesh.triangleCount * 3
        val out = FloatArray(count)
        val pos = mesh.positions
        val nrm = mesh.normals
        val reach = grid.span * REACH
        val step = grid.cell * 0.85f
        val steps = max(3, (reach / step).toInt())

        for (v in 0 until count) {
            val px = pos[v * 3]
            val py = pos[v * 3 + 1]
            val pz = pos[v * 3 + 2]
            val nx = nrm[v * 3]
            val ny = nrm[v * 3 + 1]
            val nz = nrm[v * 3 + 2]

            var hits = 0
            for (r in 0 until RAYS) {
                val d = hemisphereRay(nx, ny, nz, r, v)
                // Start clear of the surface the vertex belongs to.
                var t = grid.cell * 1.2f
                var blocked = false
                for (s in 0 until steps) {
                    if (grid.occupied(px + d[0] * t, py + d[1] * t, pz + d[2] * t)) {
                        blocked = true
                        break
                    }
                    t += step
                }
                if (blocked) hits++
            }
            out[v] = 1f - (hits.toFloat() / RAYS) * STRENGTH
        }
        return out
    }

    /**
     * A direction in the hemisphere about the normal. The sequence is deterministic but
     * decorrelated between neighbouring vertices, which turns banding into a fine grain
     * the eye reads as surface rather than as error.
     */
    private fun hemisphereRay(nx: Float, ny: Float, nz: Float, ray: Int, vertex: Int): FloatArray {
        // Orthonormal frame about the normal: cross it with whichever axis it is least
        // parallel to, so the result is never degenerate.
        val ax = if (kotlin.math.abs(nx) < 0.9f) 1f else 0f
        val ay = 1f - ax
        var tx = ny * 0f - nz * ay
        var ty = nz * ax - nx * 0f
        var tz = nx * ay - ny * ax
        var len = sqrt(tx * tx + ty * ty + tz * tz)
        if (len < 1e-4f) { tx = 0f; ty = 1f; tz = 0f; len = 1f }
        tx /= len; ty /= len; tz /= len
        val bx = ny * tz - nz * ty
        val by = nz * tx - nx * tz
        val bz = nx * ty - ny * tx

        val jitter = ((vertex * 2654435761L) ushr 13).toInt() and 0xFF
        val phi = (ray + jitter / 256f) * (2.399963f)          // golden-angle spiral
        val cosTheta = sqrt(1f - (ray + 0.5f) / RAYS)          // cosine-weighted
        val sinTheta = sqrt(1f - cosTheta * cosTheta)
        val c = cos(phi) * sinTheta
        val s = sin(phi) * sinTheta
        return floatArrayOf(
            nx * cosTheta + tx * c + bx * s,
            ny * cosTheta + ty * c + by * s,
            nz * cosTheta + tz * c + bz * s,
        )
    }

    /** Coarse occupancy grid over the whole vehicle. */
    private class Grid(
        val minX: Float, val minY: Float, val minZ: Float,
        val cell: Float, val nx: Int, val ny: Int, val nz: Int, val span: Float,
        val bits: BooleanArray,
    ) {
        fun occupied(x: Float, y: Float, z: Float): Boolean {
            val i = ((x - minX) / cell).toInt()
            val j = ((y - minY) / cell).toInt()
            val k = ((z - minZ) / cell).toInt()
            if (i < 0 || j < 0 || k < 0 || i >= nx || j >= ny || k >= nz) return false
            return bits[(k * ny + j) * nx + i]
        }

        companion object {
            fun of(meshes: List<Mesh>): Grid? {
                var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE; var minZ = Float.MAX_VALUE
                var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE; var maxZ = -Float.MAX_VALUE
                for (m in meshes) {
                    if (m.isEmpty) continue
                    minX = min(minX, m.minX); maxX = max(maxX, m.maxX)
                    minY = min(minY, m.minY); maxY = max(maxY, m.maxY)
                    minZ = min(minZ, m.minZ); maxZ = max(maxZ, m.maxZ)
                }
                if (minX > maxX) return null
                val span = maxOf(maxX - minX, maxY - minY, maxZ - minZ).coerceAtLeast(1f)
                val cell = span / RESOLUTION
                // A margin cell all round keeps surface samples off the very edge.
                val nx = ((maxX - minX) / cell).toInt() + 3
                val ny = ((maxY - minY) / cell).toInt() + 3
                val nz = ((maxZ - minZ) / cell).toInt() + 3
                val grid = Grid(
                    minX - cell, minY - cell, minZ - cell, cell, nx, ny, nz, span,
                    BooleanArray(nx * ny * nz),
                )
                for (m in meshes) grid.fill(m)
                return grid
            }
        }

        /**
         * Marks the cells a mesh passes through. Each triangle is sampled at its corners,
         * edge midpoints and centre, which is enough at this cell size to leave no holes
         * a ray could slip through.
         */
        private fun fill(mesh: Mesh) {
            val p = mesh.positions
            for (t in 0 until mesh.triangleCount) {
                val i = t * 9
                val ax = p[i]; val ay = p[i + 1]; val az = p[i + 2]
                val bx = p[i + 3]; val by = p[i + 4]; val bz = p[i + 5]
                val cx = p[i + 6]; val cy = p[i + 7]; val cz = p[i + 8]
                mark(ax, ay, az); mark(bx, by, bz); mark(cx, cy, cz)
                mark((ax + bx) / 2f, (ay + by) / 2f, (az + bz) / 2f)
                mark((bx + cx) / 2f, (by + cy) / 2f, (bz + cz) / 2f)
                mark((cx + ax) / 2f, (cy + ay) / 2f, (cz + az) / 2f)
                mark((ax + bx + cx) / 3f, (ay + by + cy) / 3f, (az + bz + cz) / 3f)
            }
        }

        private fun mark(x: Float, y: Float, z: Float) {
            val i = ((x - minX) / cell).toInt()
            val j = ((y - minY) / cell).toInt()
            val k = ((z - minZ) / cell).toInt()
            if (i < 0 || j < 0 || k < 0 || i >= nx || j >= ny || k >= nz) return
            bits[(k * ny + j) * nx + i] = true
        }
    }
}
