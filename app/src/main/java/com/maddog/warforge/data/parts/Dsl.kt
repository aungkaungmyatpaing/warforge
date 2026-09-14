package com.maddog.warforge.data.parts

import com.maddog.warforge.data.PartDef
import com.maddog.warforge.poly.Poly
import com.maddog.warforge.poly.ShapeBuilder
import com.maddog.warforge.solid.Mesh
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.cylinderPair
import com.maddog.warforge.solid.cylinderZ
import com.maddog.warforge.solid.extrude
import com.maddog.warforge.solid.extrudePair
import com.maddog.warforge.solid.revolveX

/** Ground line every ground vehicle rests on, in blueprint space. */
const val GROUND_Y = 512f

/**
 * Builds a part's 2D silhouette and its 3D solid from the same calls.
 *
 * Everything inherited from [ShapeBuilder] still just draws a flat profile; this class
 * adds the missing third dimension. Any profile left over at the end is extruded between
 * the part's default depths, so an existing 2D part becomes a solid with no edit at all,
 * and only the shapes that genuinely are not slabs - gun tubes, road wheels - need to
 * say so.
 */
class SolidBuilder(
    private val defaultBack: Float,
    private val defaultFront: Float,
    private val defaultMirrored: Boolean = false,
) : ShapeBuilder() {

    /**
     * Tessellation is recorded, not run: building every vehicle's solids at startup cost
     * sixteen seconds before the hangar appeared, and all the hangar ever needs is the
     * flat silhouette. [buildMesh] does the work, once, when a vehicle is actually opened
     * in 3D.
     */
    private val ops = ArrayList<Mesh.() -> Unit>()

    /** How far into [polys] has already been turned into geometry. */
    private var consumed = 0

    /**
     * Extrudes whatever [block] draws between depths [back] and [front].
     * With [mirrored], the result is copied to the other side of the vehicle - one call
     * gives both track runs, both wheels, both wings.
     */
    fun depth(back: Float, front: Float, mirrored: Boolean = false, block: SolidBuilder.() -> Unit) {
        flush()
        val start = polys.size
        this.block()
        solidify(start, back, front, mirrored)
    }

    /** 3D-aware primitives: they build their own solid, so nothing else may extrude them. */
    private inline fun direct(build: () -> Unit) {
        flush()
        build()
        consumed = polys.size
    }

    /**
     * A tapered tube that is genuinely round in 3D - gun barrels, fuselages, funnels,
     * masts, torpedoes. [spine] is x, y, radius triples, exactly as [tube] takes.
     */
    fun bar(role: Int, tone: Int, sides: Int = 20, vararg spine: Float) = direct {
        tube(role, tone, *spine)
        ops += { revolveX(role, tone, sides, *spine) }
    }

    /**
     * A disc lying across the vehicle - road wheels, tyres, prop bosses, hatch rings.
     * Mirrored by default, since wheels come in pairs.
     */
    fun disc(
        role: Int, tone: Int, cx: Float, cy: Float, r: Float,
        inner: Float, outer: Float, sides: Int = 20, mirrored: Boolean = true,
        rotation: Float = 0f, silhouette: Boolean = true,
    ) = direct {
        // The flat view keeps a readable facet count; the solid is smooth-shaded and
        // can afford - and needs - many more segments.
        if (silhouette) ngon(role, tone, cx, cy, r, sides.coerceAtMost(18), rotation)
        val solidSides = maxOf(sides, 20)
        ops += {
            if (mirrored) cylinderPair(role, tone, cx, cy, r, inner, outer, solidSides, rotation)
            else cylinderZ(role, tone, cx, cy, r, inner, outer, solidSides, rotation)
        }
    }

    /** A box with an explicit depth of its own. */
    fun block(
        role: Int, tone: Int, x: Float, y: Float, w: Float, h: Float,
        back: Float, front: Float,
    ) = direct {
        box(role, tone, x, y, w, h)
        ops += { box3(role, tone, x, y, back, x + w, y + h, front) }
    }

    /**
     * Geometry with no 2D counterpart - blades swept into the depth plane, internal
     * detail - or whose silhouette was already drawn by a [flat] block.
     */
    fun solid(build: Mesh.() -> Unit) = direct { ops += build }

    /** Blueprint-only decoration: drawn on the flat view, contributes no solid. */
    fun flat(block: SolidBuilder.() -> Unit) = direct { block() }

    private fun solidify(from: Int, back: Float, front: Float, mirrored: Boolean) {
        if (polys.size > from) {
            val slice: List<Poly> = ArrayList(polys.subList(from, polys.size))
            ops += {
                if (mirrored) extrudePair(slice, back, front) else extrude(slice, back, front)
            }
        }
        consumed = polys.size
    }

    /** Extrudes anything still pending at the part's default depth. */
    fun flush() = solidify(consumed, defaultBack, defaultFront, defaultMirrored)

    /** Runs the recorded tessellation. Called once, lazily, per part. */
    fun buildMesh(): Mesh = Mesh().also { m -> for (op in ops) m.op() }
}

/**
 * Declares one draggable part.
 *
 * @param z draw order, back to front.
 * @param pre marks the foundation piece that starts on the board.
 * @param depth half-thickness; the profile is extruded between -depth and +depth.
 * @param inner together with [outer], extrudes a slab standing off the centreline
 *   instead - side skirts, sponsons, fuel drums. Usually with [mirrored].
 * @param mirrored copies the solid to the other side of the vehicle.
 *
 * Any of this is overridden inside the block by [SolidBuilder.depth] sections, and the
 * 3D-aware builders (gun tubes, wheels, wings) set their own depths regardless.
 */
fun part(
    id: String, name: String, z: Int, pre: Boolean = false, depth: Float = 90f,
    inner: Float = Float.NaN, outer: Float = Float.NaN, mirrored: Boolean = false,
    info: String = "",
    block: SolidBuilder.() -> Unit,
): PartDef {
    val back = if (inner.isNaN()) -depth else inner
    val front = if (outer.isNaN()) depth else outer
    val builder = SolidBuilder(back, front, mirrored || !inner.isNaN())
    builder.block()
    builder.flush()
    return PartDef(id, name, builder.polys, z, pre, builder::buildMesh, info)
}
