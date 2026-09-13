package com.naymyo.warforge.data

import com.naymyo.warforge.data.parts.Air
import com.naymyo.warforge.data.parts.Ground
import com.naymyo.warforge.data.parts.Naval
import kotlin.random.Random

/**
 * Every buildable vehicle, plus the order the campaign hands them out in.
 *
 * The list is built once and cached: the part geometry is pure data, so a vehicle is
 * safe to share between the hangar list, the board and the victory card.
 */
object Catalog {

    /** Author order, grouped by branch. */
    val all: List<VehicleDef> by lazy { Ground.all() + Air.all() + Naval.all() }

    /**
     * Campaign order: era by era, and inside an era one ground unit, then one aircraft,
     * then one ship, so the player never builds three tanks in a row.
     */
    val campaign: List<VehicleDef> by lazy {
        val out = ArrayList<VehicleDef>(all.size)
        for (era in Era.entries) {
            val byBranch = Branch.entries.map { b -> all.filter { it.era == era && it.branch == b } }
            var i = 0
            while (byBranch.any { i < it.size }) {
                for (list in byBranch) list.getOrNull(i)?.let(out::add)
                i++
            }
        }
        out
    }

    fun byId(id: String): VehicleDef? = all.firstOrNull { it.id == id }

    fun indexOf(id: String): Int = campaign.indexOfFirst { it.id == id }

    fun eras(): List<Era> = Era.entries.filter { era -> campaign.any { it.era == era } }

    fun inEra(era: Era): List<VehicleDef> = campaign.filter { it.era == era }

    /**
     * Picks [count] parts from *other* vehicles to salt the tray with. Parts whose name
     * already appears on the real vehicle are skipped - a second part called "Turret"
     * would be unfair rather than tricky, since the player cannot tell them apart by
     * name and both look plausible.
     */
    fun decoysFor(vehicle: VehicleDef, count: Int, random: Random = Random.Default): List<PartDef> {
        if (count <= 0) return emptyList()
        val taken = vehicle.parts.map { it.name }.toHashSet()
        val pool = all
            .filter { it.id != vehicle.id }
            .flatMap { it.loose }
            .filter { it.name !in taken && it.width > 40f && it.height > 20f }
        if (pool.isEmpty()) return emptyList()
        return pool.shuffled(random).distinctBy { it.name }.take(count)
    }
}
