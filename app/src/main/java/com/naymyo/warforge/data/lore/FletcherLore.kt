package com.naymyo.warforge.data.lore

import com.naymyo.warforge.data.Lore
import com.naymyo.warforge.data.ModuleKind
import com.naymyo.warforge.data.Question
import com.naymyo.warforge.data.Spec
import com.naymyo.warforge.data.crewman
import com.naymyo.warforge.data.fuelCell
import com.naymyo.warforge.data.module
import com.naymyo.warforge.data.shellRack
import com.naymyo.warforge.poly.Role
import com.naymyo.warforge.solid.box3
import com.naymyo.warforge.solid.cylinderZ

/** Fletcher-class destroyer, opened up. Waterline near y = 432, deck near y = 340. */
object FletcherLore {

    private val modules = listOf(
        module(
            "turbines", "Geared steam turbines", ModuleKind.ENGINE,
            detail = "2 shafts · 60,000 shp",
            info = "60,000 horsepower in a 2,100-tonne hull - the highest power density of "
                + "anything afloat. Thirty-eight knots, and the ability to hold it, is what "
                + "lets a destroyer screen a fleet, run down a submarine contact and still "
                + "get back into station.",
        ) {
            box3(Role.BODY, 0, 300f, 372f, -66f, 470f, 430f, 66f)
            box3(Role.BODY, 0, 520f, 372f, -66f, 690f, 430f, 66f)
        },
        module(
            "boilers", "Babcock boilers", ModuleKind.ENGINE,
            detail = "4 oil-fired, 43 bar",
            info = "Machinery is arranged in two separate units - boiler, turbine, boiler, "
                + "turbine - so that one torpedo hit amidships cannot stop the ship. Unit "
                + "machinery is a survivability idea, not an efficiency one.",
        ) {
            cylinderZ(Role.TRIM, 2, 400f, 396f, 34f, -60f, 60f, 14)
            cylinderZ(Role.TRIM, 2, 620f, 396f, 34f, -60f, 60f, 14)
        },
        module(
            "guns", "5-inch/38 mounts", ModuleKind.GUN,
            detail = "5 × 5 in/38 dual purpose",
            info = "The best destroyer gun of the war. Dual purpose - it engages ships and "
                + "aircraft with the same mount - and when coupled to the Mk 37 director and "
                + "proximity-fuzed shells it became the most effective anti-aircraft weapon "
                + "afloat.",
        ) {
            cylinderZ(Role.BODY, 0, 790f, 340f, 34f, -34f, 34f, 14)
            cylinderZ(Role.BODY, 0, 214f, 340f, 34f, -34f, 34f, 14)
        },
        module(
            "director", "Mk 37 director", ModuleKind.OPTICS,
            detail = "Radar-directed fire control",
            info = "One instrument aims every gun. Radar ranging plus an analogue computer "
                + "that solves the gunnery problem continuously - the American advantage that "
                + "mattered most at night, when Japanese optics were otherwise better.",
        ) { box3(Role.BODY, 0, 552f, 264f, -30f, 620f, 300f, 30f) },
        module(
            "torpedoes", "Torpedo tubes", ModuleKind.MAGAZINE,
            detail = "10 × 21 in Mk 15",
            info = "Two quintuple mounts amidships. American torpedoes were notoriously "
                + "defective for the first two years of the war - running deep, with "
                + "magnetic exploders that fired early and contact exploders that crushed "
                + "on impact - and the Bureau of Ordnance denied it for eighteen months.",
        ) {
            for (i in 0 until 2) {
                box3(Role.BODY, 0, 400f + i * 20f, 310f, -26f, 460f + i * 20f, 334f, 26f)
            }
        },
        module(
            "sonar", "QC sonar and depth charges", ModuleKind.AVIONICS,
            detail = "Hull sonar, racks and K-guns",
            info = "Sonar finds the submarine; depth charges rolled off the stern and fired "
                + "sideways from K-guns try to bracket it. The attacking ship loses contact "
                + "in the final run because its own sonar cannot look under its own bow - "
                + "which is why submarines dived under attackers.",
        ) {
            cylinderZ(Role.BODY, 0, 860f, 424f, 20f, -22f, 22f, 12)
            for (i in 0 until 4) cylinderZ(Role.BODY, 0, 140f + i * 22f, 330f, 10f, -30f, 30f, 10)
        },
        module(
            "magazine", "5-inch magazines", ModuleKind.MAGAZINE,
            detail = "Below the waterline",
            info = "Handling rooms below the waterline, hoists to each mount. A destroyer "
                + "has no armour worth the name, so depth is the only protection its "
                + "ammunition gets.",
        ) {
            shellRack(740f, 462f, 0f, 6, r = 9f, len = 58f, spacing = 22f, halfZ = 34f)
            shellRack(180f, 462f, 0f, 6, r = 9f, len = 58f, spacing = 22f, halfZ = 34f)
        },
        module(
            "fuel", "Fuel oil", ModuleKind.FUEL,
            detail = "492 t",
            info = "Range 6,500 nautical miles at 15 knots - but a destroyer at 30 knots "
                + "burns fuel at a ruinous rate, and refuelling at sea from a fleet oiler is "
                + "what made sustained Pacific operations possible at all.",
        ) {
            fuelCell(300f, 430f, -66f, 690f, 452f, 66f)
        },
        module(
            "bridge", "Bridge", ModuleKind.CREW,
            detail = "329 officers and men",
            info = "Over three hundred men in a hull 114 m long and 12 m wide. Destroyers "
                + "were wet, violently uncomfortable in any sea, and known as tin cans for "
                + "the thickness of their plating.",
        ) {
            crewman(590f, 306f, -20f)
            crewman(590f, 306f, 20f)
        },
        module(
            "aa", "40 mm and 20 mm AA", ModuleKind.GUN,
            detail = "Bofors and Oerlikon",
            info = "Anti-aircraft armament grew through the war as the threat did, and grew "
                + "again when kamikaze attacks began - by 1945 Fletchers were topheavy with "
                + "guns their designers never planned for.",
        ) {
            cylinderZ(Role.BODY, 0, 300f, 330f, 16f, -16f, 16f, 10)
            cylinderZ(Role.BODY, 0, 640f, 314f, 16f, -16f, 16f, 10)
        },
        module(
            "steering", "Steering gear", ModuleKind.POWER,
            detail = "Electro-hydraulic · single rudder",
            info = "A destroyer lives by turning: she dodges shells by manoeuvring and "
                + "she fires torpedoes by pointing the whole ship. The steering gear is "
                + "the busiest machinery aboard and it is right aft, where a hit that "
                + "reaches it takes the ship out of the fight.",
        ) {
            box3(Role.BODY, 0, 118f, 372f, -30f, 176f, 412f, 30f)
            cylinderZ(Role.TRIM, 1, 140f, 370f, 14f, -22f, 22f, 12)
        },
    )

    private val history = """
## The right ship

175 Fletchers were built - more than any other destroyer class in history - because the
design got everything roughly right at once. A flush deck for strength, 38 knots, five
5-inch dual-purpose guns, ten torpedo tubes, and enough margin in the hull to absorb the
radar, anti-aircraft guns and electronics that the war kept adding.

That margin is the quiet reason for their success. Most warships are obsolete before they
wear out because there is nowhere to put the new equipment. A Fletcher in 1945 carried
several times the topweight of a Fletcher in 1942 and still floated.

## The 5-inch/38

Probably the best naval gun of its size ever made. It was dual purpose - the same mount
engages ships and aircraft - and when it was coupled to the Mk 37 director, radar ranging,
and the proximity-fuzed shell of 1943, it became the most effective anti-aircraft weapon
afloat. A shell that explodes when it passes near an aircraft, rather than requiring a
direct hit, multiplied effectiveness several times over.

## Unit machinery

Open the hull and the layout is deliberate: boiler room, engine room, boiler room, engine
room, in that order. Splitting the machinery into two independent units means a single
torpedo amidships cannot stop the ship. It costs length and complexity; it buys the chance
of getting home.

## The torpedo scandal

For the first two years of the war American torpedoes ran too deep, their magnetic
exploders fired early, and their contact exploders crushed without firing when they hit
squarely. Submarine and destroyer crews reported it constantly. The Bureau of Ordnance
denied it for eighteen months and blamed the crews. It is one of the worst institutional
failures of the war.

## Afterwards

Fletchers served on into the 1970s under half a dozen flags. USS Cassin Young is preserved
at Boston, and USS Kidd at Baton Rouge is the only one restored to her wartime
configuration.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "329"),
        Spec("Displacement", "2,500 t full load"),
        Spec("Length", "114.8 m"),
        Spec("Beam", "12.1 m"),
        Spec("Machinery", "Geared turbines, 60,000 shp"),
        Spec("Speed", "38 knots"),
        Spec("Range", "6,500 nmi at 15 knots"),
        Spec("Main armament", "5 × 5 in/38 DP"),
        Spec("Torpedoes", "10 × 21 in"),
        Spec("Built", "175"),
    )

    private val quiz = listOf(
        Question(
            "Why is a Fletcher's machinery arranged boiler-engine-boiler-engine?",
            listOf(
                "It is more fuel efficient",
                "Two independent units mean one torpedo amidships cannot stop the ship",
                "It balances the ship better",
                "It shortens the propeller shafts",
            ),
            1,
            "Unit machinery is a survivability idea. It costs length and complexity and buys "
                + "the chance of steaming home after a hit that would otherwise be fatal.",
        ),
        Question(
            "What made the 5-inch/38 so effective against aircraft?",
            listOf(
                "Its rate of fire alone",
                "Dual-purpose mounting, radar-directed fire control and proximity-fuzed shells",
                "Its exceptional muzzle velocity",
                "Its ability to fire guided rounds",
            ),
            1,
            "The gun was good; the system was what mattered. A proximity fuze explodes near "
                + "an aircraft rather than needing a direct hit, and that alone multiplied "
                + "effectiveness several times over.",
        ),
        Question(
            "What was wrong with American torpedoes early in the war?",
            listOf(
                "They were too slow to catch a warship",
                "They ran too deep, and both exploders failed - and the Bureau of Ordnance denied it for eighteen months",
                "Their range was far shorter than advertised",
                "They could not be fired from destroyers",
            ),
            1,
            "Running deep, magnetic exploders firing early, contact exploders crushing on "
                + "impact. Crews reported it constantly and were blamed for it - one of the "
                + "worst institutional failures of the war.",
        ),
        Question(
            "Why did the Fletcher design stay useful for thirty years?",
            listOf(
                "Its armour was unusually thick",
                "Spare margin in the hull, so new radar, guns and electronics could be added",
                "Its engines never wore out",
                "It was cheap enough to replace annually",
            ),
            1,
            "Most warships are obsolete before they wear out because there is nowhere to put "
                + "the new equipment. A 1945 Fletcher carried several times the topweight of a "
                + "1942 one and still floated.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
