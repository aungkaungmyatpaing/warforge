package com.maddog.warforge.data.lore

import com.maddog.warforge.data.Lore
import com.maddog.warforge.data.ModuleKind
import com.maddog.warforge.data.Question
import com.maddog.warforge.data.Spec
import com.maddog.warforge.data.crewman
import com.maddog.warforge.data.fuelCell
import com.maddog.warforge.data.module
import com.maddog.warforge.data.shellRack
import com.maddog.warforge.poly.Role
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.cylinderZ

/** Admiral Graf Spee, opened up. Waterline near y = 432, main deck near y = 338. */
object GrafSpeeLore {

    private val modules = listOf(
        module(
            "diesels", "MAN diesel engines", ModuleKind.ENGINE,
            detail = "8 diesels · 54,000 shp",
            info = "Diesels, not turbines - almost unique in a warship this size. They give "
                + "a range of 16,000 nautical miles, twice what a turbine ship of the same "
                + "tonnage could manage, which is exactly what a commerce raider needs. They "
                + "also vibrate badly enough to upset the fire control.",
        ) {
            box3(Role.BODY, 0, 330f, 376f, -72f, 600f, 442f, 72f)
            for (i in 0 until 4) {
                cylinderZ(Role.TRIM, 2, 370f + i * 60f, 404f, 22f, -68f, 68f, 12)
            }
        },
        module(
            "turret_fwd", "Forward 28 cm turret", ModuleKind.GUN,
            detail = "3 × 28 cm SK C/28",
            info = "Six 28 cm guns in two triple turrets - battleship calibre on a hull of "
                + "cruiser tonnage. Any cruiser that can catch her cannot hurt her; anything "
                + "that can hurt her cannot catch her. That was the entire design brief.",
        ) {
            box3(Role.BODY, 0, 740f, 292f, -66f, 866f, 340f, 66f)
            cylinderZ(Role.BODY, 0, 800f, 340f, 52f, -52f, 52f, 16)
        },
        module(
            "magazine", "28 cm magazines", ModuleKind.MAGAZINE,
            detail = "Below the waterline",
            info = "Shell rooms and cordite handling rooms below the armoured deck, with "
                + "flash-tight hoists to the turrets - the lesson of Jutland, built in.",
        ) {
            shellRack(750f, 470f, 0f, 6, r = 12f, len = 72f, spacing = 28f, halfZ = 42f)
            shellRack(180f, 470f, 0f, 6, r = 12f, len = 72f, spacing = 28f, halfZ = 42f)
        },
        module(
            "fuel", "Diesel bunkers", ModuleKind.FUEL,
            detail = "3,000 t oil fuel",
            info = "Range 16,000 nautical miles at 18 knots. A raider's job is to stay at sea "
                + "for months without a base, and fuel is what buys that.",
        ) {
            fuelCell(300f, 380f, 74f, 640f, 448f, 92f)
            fuelCell(300f, 380f, -92f, 640f, 448f, -74f)
        },
        module(
            "radar", "FuMO 22 Seetakt", ModuleKind.AVIONICS,
            detail = "Gunnery radar, 1938",
            info = "The first search radar fitted to a major warship anywhere. Crude - it "
                + "gave range and rough bearing - but it is the beginning of the thing that "
                + "would make optical gunnery obsolete within five years.",
        ) { box3(Role.BODY, 0, 580f, 180f, -36f, 650f, 214f, 36f) },
        module(
            "bridge", "Tower bridge", ModuleKind.CREW,
            detail = "Command and fire control",
            info = "A single tower carrying bridge, rangefinders and radar, rather than the "
                + "scattered platforms of an older ship. Concentrating the command team is "
                + "efficient - and puts everything in one place to be hit.",
        ) {
            crewman(600f, 262f, -22f)
            crewman(600f, 262f, 22f)
            box3(Role.BODY, 0, 548f, 230f, -44f, 690f, 300f, 44f)
        },
        module(
            "aircraft", "Arado Ar 196 floatplane", ModuleKind.AVIONICS,
            detail = "Catapult launched",
            info = "A raider's eyes. From 3,000 m an aircraft sees perhaps 100 km of ocean "
                + "against the 20 km visible from the masthead - it multiplies the area a "
                + "single ship can search by twenty.",
        ) { box3(Role.BODY, 0, 296f, 288f, -40f, 400f, 314f, 40f) },
        module(
            "armour_belt", "Belt armour", ModuleKind.ARMOUR,
            detail = "80 mm belt, 140 mm turret face",
            info = "Thin for the size of the guns, because the treaty weight had to go "
                + "somewhere and it went into the armament and the fuel. Against 8-inch "
                + "cruiser shells it is adequate; against anything heavier it is not.",
        ) {
            box3(Role.BODY, 0, 300f, 396f, 92f, 700f, 450f, 100f)
            box3(Role.BODY, 0, 300f, 396f, -100f, 700f, 450f, -92f)
        },
        module(
            "secondary", "15 cm secondary battery", ModuleKind.GUN,
            detail = "8 × 15 cm in single mounts",
            info = "Old-fashioned single mounts along the deck, a compromise forced by "
                + "weight. They contributed little at the River Plate.",
        ) {
            for (i in 0 until 3) {
                box3(Role.BODY, 0, 600f + i * 88f, 318f, -80f, 650f + i * 88f, 340f, 80f)
            }
        },
        module(
            "steering", "Steering gear", ModuleKind.POWER,
            detail = "Electro-hydraulic · single rudder",
            info = "Aft of the after magazine, below the waterline, where nothing but a "
                + "torpedo reaches it. A raider a thousand miles from a dockyard cannot "
                + "afford to lose steering, so the compartment has its own hand gear.",
        ) {
            box3(Role.BODY, 0, 110f, 372f, -40f, 190f, 420f, 40f)
            cylinderZ(Role.TRIM, 1, 140f, 370f, 18f, -30f, 30f, 12)
        },
    )

    private val history = """
## A ship built round a treaty

The Treaty of Versailles limited Germany to warships of 10,000 tonnes. That is cruiser
size, and a cruiser carries 20 cm guns at best. The Deutschland class was the attempt to
get something more useful out of the allowance: six 28 cm guns - battleship calibre - on a
hull that displaced (officially) 10,000 tonnes, with diesel engines for enormous range.

The result was a commerce raider that could outrange and outshoot any cruiser fast enough
to catch her, and outrun any battleship heavy enough to hurt her. The British press called
them pocket battleships; the Germans called them Panzerschiffe. Both names were about
right.

The 10,000 tonne figure, of course, was a lie - Graf Spee displaced about 16,000 tonnes
fully loaded.

## Diesels

Almost no warship of this size ran on diesels. They vibrate badly enough to upset fire
control, and they were unfashionable. What they give is range: 16,000 nautical miles at 18
knots, roughly twice what an equivalent turbine ship could manage. For a raider whose whole
purpose is to stay at sea for months with no base, that is the specification that matters.

## The River Plate

On 13 December 1939, Graf Spee met three British cruisers - Exeter, Ajax and Achilles - off
Uruguay. Her gunnery wrecked Exeter, but the two light cruisers closed and hit her about
twenty times. None of it was fatal, but her fuel processing plant was damaged, and without
it she could not make the voyage home.

Captain Langsdorff put into Montevideo, was given seventy-two hours by a neutral Uruguay,
was fed false intelligence that a British battlecruiser force was waiting outside, and
scuttled his ship in the estuary on 17 December. He shot himself three days later.

## What it shows

The pocket battleship was an elegant answer to an arithmetic problem, and the arithmetic
was the wrong one. A ship that cannot fight anything its own size and cannot be repaired
away from home is not a warship so much as a very expensive nuisance - and one lucky hit
on an auxiliary system was enough to finish her.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "1,150"),
        Spec("Displacement", "16,020 t full load"),
        Spec("Length", "186 m"),
        Spec("Beam", "21.6 m"),
        Spec("Machinery", "8 MAN diesels, 54,000 shp"),
        Spec("Speed", "28.5 knots"),
        Spec("Range", "16,300 nmi at 18 knots"),
        Spec("Main armament", "6 × 28 cm"),
        Spec("Armour belt", "80 mm"),
        Spec("Fate", "Scuttled, Montevideo, 1939"),
    )

    private val quiz = listOf(
        Question(
            "What was the design brief behind a 'pocket battleship'?",
            listOf(
                "The heaviest possible armour on a treaty-legal hull",
                "Outgun anything fast enough to catch her, outrun anything heavy enough to hurt her",
                "The fastest warship afloat",
                "A battleship small enough for the Baltic",
            ),
            1,
            "Battleship-calibre guns on a cruiser hull, with the speed to avoid anything "
                + "bigger. It works only as long as she is never cornered - which is exactly "
                + "what happened at the River Plate.",
        ),
        Question(
            "Why diesels rather than turbines?",
            listOf(
                "They were lighter",
                "Range - 16,000 nautical miles, roughly twice a turbine ship's",
                "They were quieter",
                "Germany had no turbine industry",
            ),
            1,
            "A commerce raider stays at sea for months with no base. Diesels vibrate badly "
                + "enough to upset the fire control, and the range was worth it anyway.",
        ),
        Question(
            "What actually forced Graf Spee out of the fight at the River Plate?",
            listOf(
                "Her main armament was disabled",
                "Damage to her fuel processing plant, without which she could not get home",
                "She had exhausted her ammunition",
                "Her engines were wrecked",
            ),
            1,
            "About twenty hits, none fatal, but one of them ended any chance of the voyage "
                + "home. A raider that cannot be repaired away from a base is finished by "
                + "damage that would barely inconvenience a fleet unit.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
