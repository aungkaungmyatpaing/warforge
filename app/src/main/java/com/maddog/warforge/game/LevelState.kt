package com.maddog.warforge.game

import com.maddog.warforge.data.PartDef
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.poly.Poly
import kotlin.math.hypot
import kotlin.random.Random

/**
 * Rules for one build. Holds what is fitted, what is still in the tray, and how cleanly
 * the player is doing - no drawing and no Android types, so it unit-tests on the JVM.
 *
 * A "slot" is anything that can be fitted: a bolted-on part or an internal module. The
 * two behave identically once they are in the tray, which is what lets a build run all
 * the way from the engine out to the antenna without the rules knowing the difference.
 */
class LevelState(
    val vehicle: VehicleDef,
    decoys: List<PartDef> = emptyList(),
    random: Random = Random.Default,
) {

    /** One fittable thing, flattened out of either a part or a module. */
    class Slot(
        val id: String,
        val name: String,
        val stage: Stage,
        val detail: String,
        val info: String,
        /** Flat art for the tray and the blueprint view. */
        val polys: List<Poly>,
        /** Centre of the solid, in world coordinates, for the 3D board. */
        val wx: Float, val wy: Float, val wz: Float,
        /** Centre of the flat profile, in blueprint coordinates. */
        val bx: Float, val by: Float,
        val width: Float, val height: Float,
        /** Modules live under the skin; parts are bolted onto it. */
        val internal: Boolean,
    )

    /** One tray entry. Decoys look the part but fit nowhere on this vehicle. */
    class Item(val slot: Slot, val decoy: Boolean) {
        val id: String get() = slot.id
        val name: String get() = slot.name
    }

    /** Every stage this vehicle has anything in, in build order. */
    val stages: List<Stage> = Stage.of(vehicle)

    private val allItems: MutableList<Item>
    private val placedIds = HashSet<String>()

    var stageIndex = 0
        private set
    var mistakes = 0
        private set
    var hintsUsed = 0
        private set

    init {
        val real = ArrayList<Item>()
        for (module in vehicle.modules) {
            real += Item(slotOf(module), false)
        }
        for (part in vehicle.loose) {
            real += Item(slotOf(part), false)
        }
        // Decoys are spread across the stages so no single stage becomes the hard one.
        val stagePool = stages.ifEmpty { listOf(Stage.ARMAMENT) }
        val fakes = decoys.mapIndexed { i, part ->
            Item(slotOf(part, stage = stagePool[i % stagePool.size], decoy = true), true)
        }
        allItems = (real + fakes).shuffled(random).toMutableList()
        vehicle.parts.filter { it.preplaced }.forEach { placedIds += it.id }
        advanceStage()
    }

    private fun slotOf(part: PartDef, stage: Stage? = null, decoy: Boolean = false): Slot {
        val mesh = part.mesh
        return Slot(
            id = if (decoy) "decoy_${part.id}_${part.name.hashCode()}" else part.id,
            name = part.name,
            stage = stage ?: Stage.stageOf(part),
            detail = "",
            info = part.info,
            polys = part.polys,
            wx = (mesh.minX + mesh.maxX) * 0.5f,
            wy = (mesh.minY + mesh.maxY) * 0.5f,
            wz = (mesh.minZ + mesh.maxZ) * 0.5f,
            bx = part.homeX, by = part.homeY,
            width = part.width, height = part.height,
            internal = false,
        )
    }

    private fun slotOf(module: com.maddog.warforge.data.ModuleDef): Slot {
        val mesh = module.mesh
        return Slot(
            id = "mod_" + module.id,
            name = module.name,
            stage = Stage.stageOf(module),
            detail = module.detail,
            info = module.info,
            polys = mesh.toProfilePolys(),
            wx = (mesh.minX + mesh.maxX) * 0.5f,
            wy = (mesh.minY + mesh.maxY) * 0.5f,
            wz = (mesh.minZ + mesh.maxZ) * 0.5f,
            bx = (mesh.minX + mesh.maxX) * 0.5f,
            by = -(mesh.minY + mesh.maxY) * 0.5f,
            width = mesh.maxX - mesh.minX,
            height = mesh.maxY - mesh.minY,
            internal = true,
        )
    }

    // ----------------------------------------------------------------------

    val stage: Stage? get() = stages.getOrNull(stageIndex)

    /** What the tray shows: everything left to fit in the current stage. */
    val tray: List<Item>
        get() = allItems.filter { it.slot.stage == stage }

    /** Slots still empty in the current stage - the ghosts the player aims at. */
    fun openSlots(): List<Slot> = tray.filter { !it.decoy }.map { it.slot }

    /** Everything fitted so far, for the renderer. */
    fun placedSlots(): Set<String> = placedIds

    fun isPlaced(id: String): Boolean = id in placedIds

    val totalToFit: Int = vehicle.modules.size + vehicle.partCount
    val remaining: Int get() = allItems.count { !it.decoy }
    val isComplete: Boolean get() = allItems.none { !it.decoy }

    val progress: Float
        get() = if (totalToFit == 0) 1f else (totalToFit - remaining).toFloat() / totalToFit

    /**
     * Fits [item] into the slot the player dropped it on.
     *
     * The view decides which slot was aimed at - in 3D that is a screen-space test, in
     * 2D a blueprint-space one - and this only has to judge whether it was the right one.
     */
    fun tryPlace(item: Item, targetSlotId: String?): Boolean {
        if (item.decoy || targetSlotId != item.slot.id || isPlaced(item.slot.id)) {
            mistakes++
            return false
        }
        placedIds += item.slot.id
        allItems.remove(item)
        advanceStage()
        return true
    }

    /** Drops straight onto a slot's own position - used by tests and by the hint. */
    fun tryPlace(item: Item, x: Float, y: Float): Boolean {
        val target = openSlots().minByOrNull { hypot(x - it.bx, y - it.by) }
        val within = target != null &&
            hypot(x - target.bx, y - target.by) <= snapRadius(target)
        return tryPlace(item, if (within) target!!.id else null)
    }

    /** Blueprint-space tolerance, scaled off the slot but never tighter than a thumb. */
    fun snapRadius(slot: Slot): Float =
        maxOf(MIN_SNAP, maxOf(slot.width, slot.height) * 0.55f)

    /** Moves on once the current stage is empty of real work. */
    private fun advanceStage() {
        while (stageIndex < stages.size && allItems.none { !it.decoy && it.slot.stage == stages[stageIndex] }) {
            // Decoys left over from a finished stage go with it.
            allItems.removeAll { it.decoy && it.slot.stage == stages[stageIndex] }
            stageIndex++
        }
    }

    /** The next slot the player ought to fit, used by the hint button. */
    fun hintTarget(): Item? {
        val real = tray.filter { !it.decoy }
        if (real.isEmpty()) return null
        hintsUsed++
        return real.maxByOrNull { it.slot.width * it.slot.height }
    }

    /** Peeks at the same target without charging a hint. */
    fun peekTarget(): Item? =
        tray.filter { !it.decoy }.maxByOrNull { it.slot.width * it.slot.height }

    /**
     * Three stars for a clean build, and the allowance grows with the part count so a
     * 30-piece Panzer IV is not judged like a 7-piece Renault.
     */
    fun stars(): Int {
        val allowance = totalToFit / 4
        val cost = mistakes + hintsUsed * 2
        return when {
            cost <= allowance -> 3
            cost <= allowance + totalToFit / 2 + 2 -> 2
            else -> 1
        }
    }

    /** Coins paid out on completion. Difficulty pays, sloppiness does not. */
    fun coinReward(): Int =
        (totalToFit * 4 + stars() * 20 - mistakes * 2).coerceAtLeast(10)

    private companion object {
        const val MIN_SNAP = 62f
    }
}
