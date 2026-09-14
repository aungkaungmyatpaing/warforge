package com.maddog.warforge.data

import com.maddog.warforge.poly.Palette
import com.maddog.warforge.poly.Poly
import com.maddog.warforge.solid.Mesh

/** Blueprint space every vehicle is authored in. The board letterboxes this rect. */
const val BP_W = 1000f
const val BP_H = 620f

enum class Branch(val label: String) {
    GROUND("Ground"),
    AIR("Air"),
    NAVAL("Naval"),
}

enum class Era(val label: String, val years: String) {
    WWI("World War I", "1914-1918"),
    INTERWAR("Interwar", "1919-1938"),
    WWII("World War II", "1939-1945"),
    COLD_WAR("Cold War", "1946-1991"),
    MODERN("Modern", "1992-present"),
}

/**
 * One draggable piece of a vehicle.
 *
 * Facets are stored in final blueprint coordinates, so "assembling" is really just
 * revealing parts in place - no per-part transform is needed once a part is home.
 */
class PartDef(
    val id: String,
    val name: String,
    val polys: List<Poly>,
    /** Draw order, back to front. */
    val z: Int = 0,
    /** Pre-placed parts give the player a foundation to build against. */
    val preplaced: Boolean = false,
    /** Builds the solid form on first use - see [SolidBuilder]. */
    private val meshProvider: () -> Mesh = { Mesh() },
    /** What this component is for, shown when the player inspects it. */
    val info: String = "",
) {
    val minX: Float
    val minY: Float
    val maxX: Float
    val maxY: Float

    init {
        var a = Float.MAX_VALUE; var b = Float.MAX_VALUE
        var c = -Float.MAX_VALUE; var d = -Float.MAX_VALUE
        for (p in polys) {
            var i = 0
            while (i + 1 < p.pts.size) {
                val x = p.pts[i]; val y = p.pts[i + 1]
                if (x < a) a = x; if (x > c) c = x
                if (y < b) b = y; if (y > d) d = y
                i += 2
            }
        }
        if (polys.isEmpty()) { a = 0f; b = 0f; c = 0f; d = 0f }
        minX = a; minY = b; maxX = c; maxY = d
    }

    /**
     * Where the part has to be dropped: the area-weighted centroid of its facets.
     *
     * The bounding-box centre would do for a compact part, but several parts group
     * pieces that sit far apart - an undercarriage is its main legs plus a tail skid -
     * and the box centre of those lands in empty air between them. The centroid lands
     * on the bulk of the part, which is where the player aims.
     */
    val homeX: Float
    val homeY: Float

    init {
        var area = 0.0
        var cx = 0.0
        var cy = 0.0
        for (p in polys) {
            val n = p.pts.size / 2
            // Shoelace over each facet: signed area, and the centroid it contributes.
            var a = 0.0
            var px = 0.0
            var py = 0.0
            for (i in 0 until n) {
                val j = (i + 1) % n
                val x0 = p.pts[i * 2].toDouble(); val y0 = p.pts[i * 2 + 1].toDouble()
                val x1 = p.pts[j * 2].toDouble(); val y1 = p.pts[j * 2 + 1].toDouble()
                val cross = x0 * y1 - x1 * y0
                a += cross
                px += (x0 + x1) * cross
                py += (y0 + y1) * cross
            }
            if (a != 0.0) {
                // Facets may wind either way; magnitude is what weights them.
                val w = kotlin.math.abs(a) * 0.5
                area += w
                cx += px / (3.0 * a) * w
                cy += py / (3.0 * a) * w
            }
        }
        if (area > 0.0) {
            homeX = (cx / area).toFloat()
            homeY = (cy / area).toFloat()
        } else {
            homeX = (minX + maxX) * 0.5f
            homeY = (minY + maxY) * 0.5f
        }
    }

    /** The solid form, tessellated the first time the vehicle is opened in 3D. */
    val mesh: Mesh by lazy(LazyThreadSafetyMode.PUBLICATION) { meshProvider() }

    val width: Float get() = maxX - minX
    val height: Float get() = maxY - minY
}

/** A buildable vehicle: the unit of one level. */
class VehicleDef(
    val id: String,
    val name: String,
    val era: Era,
    val branch: Branch,
    val country: String,
    val year: Int,
    /** One-line fact shown on the victory card. */
    val fact: String,
    val palette: Palette,
    val parts: List<PartDef>,
    /** How many wrong parts from other vehicles get mixed into the tray. */
    val decoys: Int = 0,
    /** Everything the museum knows about this vehicle. */
    val lore: Lore = Lore(),
    /** A Blender-authored model, used in 3D in place of the extruded blueprint. */
    val model: ModelSource? = null,
) {
    /** What is inside: engine, transmission, crew, ammunition, fuel. */
    val modules: List<ModuleDef> get() = lore.modules
    val history: String get() = lore.history
    val specs: List<Spec> get() = lore.specs
    val quiz: List<Question> get() = lore.quiz
    val hasCutaway: Boolean get() = lore.modules.isNotEmpty()

    val loose: List<PartDef> get() = parts.filter { !it.preplaced }
    val sorted: List<PartDef> get() = parts.sortedBy { it.z }
    val partCount: Int get() = loose.size

    /** Drives star thresholds and the campaign ordering. */
    val difficulty: Int get() = loose.size + decoys * 2
}

/**
 * The educational half of a vehicle: what is inside it, what it was, what it could do,
 * and a few questions to make the reader notice. Kept separate from the geometry so the
 * catalogue files stay readable.
 */
class Lore(
    val modules: List<ModuleDef> = emptyList(),
    val history: String = "",
    val specs: List<Spec> = emptyList(),
    val quiz: List<Question> = emptyList(),
)

/** One row of the specification table. */
class Spec(val label: String, val value: String)

/**
 * A quiz question. [answers] holds the options and [correct] indexes the right one;
 * [because] is shown after answering, so a wrong guess still teaches something.
 */
class Question(
    val text: String,
    val answers: List<String>,
    val correct: Int,
    val because: String,
)
