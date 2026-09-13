package com.naymyo.warforge.game

import com.naymyo.warforge.data.ModuleDef
import com.naymyo.warforge.data.ModuleKind
import com.naymyo.warforge.data.PartDef
import com.naymyo.warforge.data.VehicleDef

/**
 * The order a vehicle goes together in.
 *
 * Real vehicles are built from the inside out, and so is this: the powerpack goes in
 * before there is a roof over it, the crew and their ammunition before the armour that
 * protects them, and only then the running gear and the turret. Building in that order
 * is what makes the finished machine legible - you already know what is under the plate
 * because you put it there.
 */
enum class Stage(val label: String, val my: String, val hint: String) {
    POWERPACK(
        "Powerpack", "စက်ပိုင်း",
        "Engine, gearbox and the shafts between them.",
    ),
    FIGHTING(
        "Crew and stowage", "အမှုထမ်းနှင့် ကျည်",
        "Crew stations, ammunition, fuel and the gun breech.",
    ),
    ARMOUR(
        "Armour", "သံချပ်",
        "The plate that goes over everything you just fitted.",
    ),
    HULL(
        "Hull and structure", "ကိုယ်ထည်",
        "Superstructure, decks and the hull plating.",
    ),
    RUNNING(
        "Running gear", "မောင်းနှင်စနစ်",
        "Tracks, wheels, suspension and undercarriage.",
    ),
    ARMAMENT(
        "Armament and fittings", "လက်နက်နှင့် ပစ္စည်း",
        "Turret, guns, optics and everything bolted on last.",
    ),
    ;

    companion object {
        /** Ordered, and only the stages this vehicle actually has anything in. */
        fun of(vehicle: VehicleDef): List<Stage> {
            val used = HashSet<Stage>()
            for (m in vehicle.modules) used += stageOf(m)
            for (p in vehicle.loose) used += stageOf(p)
            return entries.filter { it in used }
        }

        fun stageOf(module: ModuleDef): Stage = when (module.kind) {
            ModuleKind.ENGINE, ModuleKind.TRANSMISSION, ModuleKind.DRIVE,
            ModuleKind.RADIATOR, ModuleKind.REACTOR -> POWERPACK

            // Powered drives and life support go in with the crew: a turret motor and a
            // ventilation fan are both things the fighting compartment is built around.
            ModuleKind.CREW, ModuleKind.AMMO, ModuleKind.FUEL, ModuleKind.RADIO,
            ModuleKind.GUN, ModuleKind.OPTICS, ModuleKind.AVIONICS,
            ModuleKind.MAGAZINE, ModuleKind.ESCAPE, ModuleKind.POWER,
            ModuleKind.LIFE -> FIGHTING

            ModuleKind.ARMOUR -> Stage.ARMOUR
        }

        /**
         * Parts are sorted by their draw order, which already encodes how a vehicle is
         * layered: hull first, then running gear over it, then everything on top.
         */
        fun stageOf(part: PartDef): Stage = when {
            part.z <= 22 -> HULL
            part.z <= 32 -> RUNNING
            part.z <= 38 -> HULL
            else -> ARMAMENT
        }
    }
}
