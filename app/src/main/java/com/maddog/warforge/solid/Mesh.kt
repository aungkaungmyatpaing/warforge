package com.maddog.warforge.solid

import com.maddog.warforge.poly.Palette
import com.maddog.warforge.poly.Poly
import com.maddog.warforge.poly.Role
import com.maddog.warforge.poly.shade
import kotlin.math.sqrt

/** Growable float array. Avoids boxing several hundred thousand vertex components. */
class FloatBuf(initial: Int = 512) {
    var data = FloatArray(initial)
        private set
    var size = 0
        private set

    fun add(v: Float) {
        if (size == data.size) data = data.copyOf(data.size * 2)
        data[size++] = v
    }

    fun add(a: Float, b: Float, c: Float) { add(a); add(b); add(c) }

    fun toArray(): FloatArray = data.copyOf(size)
}

/** Growable int array, one entry per triangle. */
class IntBuf(initial: Int = 128) {
    var data = IntArray(initial)
        private set
    var size = 0
        private set

    fun add(v: Int) {
        if (size == data.size) data = data.copyOf(data.size * 2)
        data[size++] = v
    }
}

/**
 * Triangle soup with per-vertex normals.
 *
 * Normals are per *vertex*, not per face, which is what separates a curved surface from
 * a faceted one: a gun barrel or a road wheel hands the shader normals that sweep round
 * the circumference, so it shades as a cylinder instead of as a ring of flat plates.
 * Flat surfaces simply pass the same face normal three times.
 *
 * Colour is deliberately not baked in: each triangle carries its role and tone, and
 * [toVertexArray] resolves them against a palette at upload time. That way one mesh can
 * be drawn in any vehicle's livery, and the material response - how glossy, how metallic
 * - is looked up from the role at the same moment.
 *
 * Positions are in a right-handed world where **+X is forward (toward the nose), +Y is
 * up, +Z is out of the vehicle's right side**. The blueprint's y axis points down, so
 * callers pass 2D coordinates through [wy].
 */
class Mesh {

    private val pos = FloatBuf()
    private val nrm = FloatBuf()
    private val roles = IntBuf()
    private val tones = IntBuf()

    /**
     * Per-vertex colour multiplier, or null when the mesh is plain paint.
     *
     * Three floats a vertex, baked in Blender: camouflage patches, mud up the lower
     * hull, paint worn off the edges. It multiplies rather than replaces, so the vehicle
     * is still coloured from its own palette - the mesh only says how the surface varies
     * across itself. Left null for the procedurally built meshes, which have nowhere
     * near enough vertices to carry it and would only pay the memory.
     */
    private var tints: FloatBuf? = null

    var minX = Float.MAX_VALUE; private set
    var minY = Float.MAX_VALUE; private set
    var minZ = Float.MAX_VALUE; private set
    var maxX = -Float.MAX_VALUE; private set
    var maxY = -Float.MAX_VALUE; private set
    var maxZ = -Float.MAX_VALUE; private set

    companion object {
        /** Blueprint y (down-positive) to world y (up-positive). */
        fun wy(blueprintY: Float): Float = -blueprintY

        /**
         * How much of the hand-authored 2D tone survives into 3D.
         *
         * The tones encode a fake side-on light for the flat view. In 3D there is a real
         * one, so nearly all of it has to go - what is left is just enough to keep two
         * adjacent panels of the same colour from merging.
         */
        const val TONE_IN_3D = 0.12f

        /** Floats per vertex: position, normal, colour with alpha, specular, gloss. */
        const val STRIDE = 12
    }

    val triangleCount: Int get() = roles.size
    val isEmpty: Boolean get() = roles.size == 0

    /** True when the mesh carries baked surface variation. */
    val hasTints: Boolean get() = tints != null

    /** Multiplier at [vertex], or white. Three floats, r/g/b. */
    fun tintAt(vertex: Int, out: FloatArray) {
        val buf = tints
        if (buf == null || vertex * 3 + 2 >= buf.size) {
            out[0] = 1f; out[1] = 1f; out[2] = 1f
            return
        }
        out[0] = buf.data[vertex * 3]
        out[1] = buf.data[vertex * 3 + 1]
        out[2] = buf.data[vertex * 3 + 2]
    }

    /**
     * The tint buffer, created on first use and back-filled with white for every
     * triangle already added. Meshes are usually part tinted and part not - a Blender
     * export mixed with hand-built modules - so the two have to coexist.
     */
    private fun tintBuf(): FloatBuf {
        tints?.let { return it }
        val buf = FloatBuf(maxOf(512, roles.size * 9 + 9))
        repeat(roles.size * 9) { buf.add(1f) }
        tints = buf
        return buf
    }

    /**
     * Raw triangle data, for anything that needs to shade the mesh itself rather than
     * hand it to GL - the offline preview renderer, chiefly. Nine floats per triangle in
     * [positions] and [normals]; read no further than `triangleCount * 9`.
     */
    val positions: FloatArray get() = pos.data
    val normals: FloatArray get() = nrm.data
    fun roleAt(triangle: Int): Int = roles.data[triangle]
    fun toneAt(triangle: Int): Int = tones.data[triangle]

    /** Adds a triangle, computing one flat face normal from the winding. */
    fun tri(
        ax: Float, ay: Float, az: Float,
        bx: Float, by: Float, bz: Float,
        cx: Float, cy: Float, cz: Float,
        role: Int, tone: Int,
    ) {
        val ux = bx - ax; val uy = by - ay; val uz = bz - az
        val vx = cx - ax; val vy = cy - ay; val vz = cz - az
        var nx = uy * vz - uz * vy
        var ny = uz * vx - ux * vz
        var nz = ux * vy - uy * vx
        val len = sqrt(nx * nx + ny * ny + nz * nz)
        if (len < 1e-6f) return                    // degenerate sliver, skip it
        nx /= len; ny /= len; nz /= len
        triN(
            ax, ay, az, nx, ny, nz,
            bx, by, bz, nx, ny, nz,
            cx, cy, cz, nx, ny, nz,
            role, tone,
        )
    }

    /** Adds a triangle with a normal supplied for each corner - the smooth-shading path. */
    fun triN(
        ax: Float, ay: Float, az: Float, anx: Float, any: Float, anz: Float,
        bx: Float, by: Float, bz: Float, bnx: Float, bny: Float, bnz: Float,
        cx: Float, cy: Float, cz: Float, cnx: Float, cny: Float, cnz: Float,
        role: Int, tone: Int,
    ) {
        // A collapsed triangle draws nothing but would still stretch the bounding box,
        // which throws off camera framing and picking.
        if (isDegenerate(ax, ay, az, bx, by, bz, cx, cy, cz)) return
        pos.add(ax, ay, az); nrm.add(anx, any, anz)
        pos.add(bx, by, bz); nrm.add(bnx, bny, bnz)
        pos.add(cx, cy, cz); nrm.add(cnx, cny, cnz)
        roles.add(role)
        tones.add(tone)
        grow(ax, ay, az); grow(bx, by, bz); grow(cx, cy, cz)
    }

    /** [triN] with a colour multiplier at each corner. */
    fun triNC(
        ax: Float, ay: Float, az: Float, anx: Float, any: Float, anz: Float, ac: FloatArray,
        bx: Float, by: Float, bz: Float, bnx: Float, bny: Float, bnz: Float, bc: FloatArray,
        cx: Float, cy: Float, cz: Float, cnx: Float, cny: Float, cnz: Float, cc: FloatArray,
        role: Int, tone: Int,
    ) {
        // Claimed before the triangle is added, so the back-fill counts only the
        // triangles that came before it.
        val buf = tintBuf()
        val before = roles.size
        triN(
            ax, ay, az, anx, any, anz,
            bx, by, bz, bnx, bny, bnz,
            cx, cy, cz, cnx, cny, cnz,
            role, tone,
        )
        // A degenerate triangle is dropped, and must not leave a tint behind it.
        if (roles.size == before) return
        buf.add(ac[0], ac[1], ac[2])
        buf.add(bc[0], bc[1], bc[2])
        buf.add(cc[0], cc[1], cc[2])
    }

    /**
     * Adds a triangle whose winding is corrected so its normal points along
     * ([dx], [dy], [dz]). Used wherever the outward direction is known but the source
     * polygon's winding is not - which is most of the 2D art, since it was authored for
     * a painter that does not care.
     */
    fun triFacing(
        ax: Float, ay: Float, az: Float,
        bx: Float, by: Float, bz: Float,
        cx: Float, cy: Float, cz: Float,
        dx: Float, dy: Float, dz: Float,
        role: Int, tone: Int,
    ) {
        val ux = bx - ax; val uy = by - ay; val uz = bz - az
        val vx = cx - ax; val vy = cy - ay; val vz = cz - az
        val nx = uy * vz - uz * vy
        val ny = uz * vx - ux * vz
        val nz = ux * vy - uy * vx
        if (nx * dx + ny * dy + nz * dz < 0f) {
            tri(ax, ay, az, cx, cy, cz, bx, by, bz, role, tone)
        } else {
            tri(ax, ay, az, bx, by, bz, cx, cy, cz, role, tone)
        }
    }

    /** Two triangles facing ([dx],[dy],[dz]). Corners in order around the quad. */
    fun quadFacing(
        ax: Float, ay: Float, az: Float,
        bx: Float, by: Float, bz: Float,
        cx: Float, cy: Float, cz: Float,
        dx2: Float, dy2: Float, dz2: Float,
        dx: Float, dy: Float, dz: Float,
        role: Int, tone: Int,
    ) {
        triFacing(ax, ay, az, bx, by, bz, cx, cy, cz, dx, dy, dz, role, tone)
        triFacing(ax, ay, az, cx, cy, cz, dx2, dy2, dz2, dx, dy, dz, role, tone)
    }

    /**
     * A quad with a normal at every corner: the smooth-shaded counterpart of
     * [quadFacing], used to bridge the rings of a cylinder or a solid of revolution.
     */
    fun quadN(
        ax: Float, ay: Float, az: Float, anx: Float, any: Float, anz: Float,
        bx: Float, by: Float, bz: Float, bnx: Float, bny: Float, bnz: Float,
        cx: Float, cy: Float, cz: Float, cnx: Float, cny: Float, cnz: Float,
        dx: Float, dy: Float, dz: Float, dnx: Float, dny: Float, dnz: Float,
        role: Int, tone: Int,
    ) {
        triN(
            ax, ay, az, anx, any, anz, bx, by, bz, bnx, bny, bnz,
            cx, cy, cz, cnx, cny, cnz, role, tone,
        )
        triN(
            ax, ay, az, anx, any, anz, cx, cy, cz, cnx, cny, cnz,
            dx, dy, dz, dnx, dny, dnz, role, tone,
        )
    }

    private fun isDegenerate(
        ax: Float, ay: Float, az: Float,
        bx: Float, by: Float, bz: Float,
        cx: Float, cy: Float, cz: Float,
    ): Boolean {
        val ux = bx - ax; val uy = by - ay; val uz = bz - az
        val vx = cx - ax; val vy = cy - ay; val vz = cz - az
        val nx = uy * vz - uz * vy
        val ny = uz * vx - ux * vz
        val nz = ux * vy - uy * vx
        return nx * nx + ny * ny + nz * nz < 1e-8f
    }

    private fun grow(x: Float, y: Float, z: Float) {
        if (x < minX) minX = x; if (x > maxX) maxX = x
        if (y < minY) minY = y; if (y > maxY) maxY = y
        if (z < minZ) minZ = z; if (z > maxZ) maxZ = z
    }

    /** Appends another mesh, optionally mirrored through the z = 0 plane. */
    fun addAll(other: Mesh, mirrorZ: Boolean = false) {
        val p = other.pos.data
        val n = other.nrm.data
        val c = other.tints?.data
        if (c != null) tintBuf()
        for (t in 0 until other.roles.size) {
            val i = t * 9
            val before = roles.size
            if (mirrorZ) {
                // Negating z reverses handedness, so swap two corners to keep the
                // winding - and with it the outward normal - correct.
                triN(
                    p[i], p[i + 1], -p[i + 2], n[i], n[i + 1], -n[i + 2],
                    p[i + 6], p[i + 7], -p[i + 8], n[i + 6], n[i + 7], -n[i + 8],
                    p[i + 3], p[i + 4], -p[i + 5], n[i + 3], n[i + 4], -n[i + 5],
                    other.roles.data[t], other.tones.data[t],
                )
            } else {
                triN(
                    p[i], p[i + 1], p[i + 2], n[i], n[i + 1], n[i + 2],
                    p[i + 3], p[i + 4], p[i + 5], n[i + 3], n[i + 4], n[i + 5],
                    p[i + 6], p[i + 7], p[i + 8], n[i + 6], n[i + 7], n[i + 8],
                    other.roles.data[t], other.tones.data[t],
                )
            }
            if (roles.size == before) continue
            tints?.let { buf ->
                when {
                    c == null -> repeat(9) { buf.add(1f) }
                    // Mirroring swapped the second and third corners; their tints follow.
                    mirrorZ -> for (k in intArrayOf(0, 6, 3)) {
                        buf.add(c[i + k], c[i + k + 1], c[i + k + 2])
                    }
                    else -> for (k in 0 until 9) buf.add(c[i + k])
                }
            }
        }
    }

    /**
     * Flattens the solid back to a side-on silhouette.
     *
     * Modules are authored in 3D only, but the parts tray and the hangar thumbnails are
     * flat, so they need some profile to draw. Projecting the triangles onto the x/y
     * plane gives one for free, and shading by the face normal keeps it from reading as
     * a plain coloured blob.
     */
    fun toProfilePolys(): List<Poly> {
        val out = ArrayList<Poly>(roles.size)
        for (t in 0 until roles.size) {
            val i = t * 9
            // Faces pointing away from the viewer are hidden by the ones in front.
            if (nrm.data[i + 2] < 0f) continue
            val tone = tones.data[t] + when {
                nrm.data[i + 1] > 0.5f -> 2        // top faces catch the light
                nrm.data[i + 1] < -0.5f -> -2
                else -> 0
            }
            out += Poly(
                floatArrayOf(
                    pos.data[i], -pos.data[i + 1],
                    pos.data[i + 3], -pos.data[i + 4],
                    pos.data[i + 6], -pos.data[i + 7],
                ),
                tone, roles.data[t],
            )
        }
        return out
    }

    /**
     * Interleaved `x,y,z, nx,ny,nz, r,g,b,a, spec,gloss` per vertex - one GL buffer
     * carrying both the shape and how its surface answers light.
     */
    fun toVertexArray(
        palette: Palette,
        alpha: Float = 1f,
        /** Per-vertex ambient occlusion from [AmbientOcclusion.bake], or null. */
        ao: FloatArray? = null,
    ): FloatArray {
        val out = FloatArray(roles.size * 3 * STRIDE)
        val tint = FloatArray(3)
        var v = 0
        for (t in 0 until roles.size) {
            val role = roles.data[t]
            val color = shade(palette.colorFor(role), (tones.data[t] * TONE_IN_3D).toInt())
            val r = ((color shr 16) and 0xFF) / 255f
            val g = ((color shr 8) and 0xFF) / 255f
            val b = (color and 0xFF) / 255f
            val spec = specularOf(role)
            val gloss = glossOf(role)
            val a = if (role == Role.GLASS) alpha * 0.72f else alpha
            for (k in 0 until 3) {
                val vertex = t * 3 + k
                val i = vertex * 3
                val occ = ao?.getOrNull(vertex) ?: 1f
                tintAt(vertex, tint)
                out[v++] = pos.data[i]
                out[v++] = pos.data[i + 1]
                out[v++] = pos.data[i + 2]
                out[v++] = nrm.data[i]
                out[v++] = nrm.data[i + 1]
                out[v++] = nrm.data[i + 2]
                out[v++] = r * occ * tint[0]
                out[v++] = g * occ * tint[1]
                out[v++] = b * occ * tint[2]
                out[v++] = a
                // Occlusion kills the highlight too: a crevice does not glint.
                // Worn paint is brighter *and* shinier - that is what bare metal is.
                out[v++] = spec * occ * occ * (0.6f + 0.4f * tint[0])
                out[v++] = gloss
            }
        }
        return out
    }

    /**
     * How brightly a material answers a highlight. Bare metal and glass are shiny, paint
     * is satin at best, and rubber and track steel are almost dead matte - getting this
     * ordering right does more for realism than any amount of extra geometry.
     */
    private fun specularOf(role: Int): Float = when (role) {
        Role.METAL -> 0.62f
        Role.GLASS -> 0.95f
        Role.DARK -> 0.06f
        Role.ACCENT -> 0.14f
        else -> 0.17f
    }

    /** Tightness of that highlight. */
    private fun glossOf(role: Int): Float = when (role) {
        Role.METAL -> 44f
        Role.GLASS -> 110f
        Role.DARK -> 6f
        else -> 15f
    }
}
