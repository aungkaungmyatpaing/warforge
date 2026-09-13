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

/** Yamato, opened up. Waterline near y = 432, armoured deck near y = 332. */
object YamatoLore {

    private val modules = listOf(
        module(
            "turbines", "Kampon geared turbines", ModuleKind.ENGINE,
            detail = "4 shafts · 150,000 shp",
            info = "150,000 horsepower to move 72,000 tonnes at 27 knots. It sounds "
                + "enormous and is barely adequate: the power needed rises with the cube of "
                + "speed, and Yamato was slower than the American fast battleships she was "
                + "built to fight.",
        ) {
            box3(Role.BODY, 0, 300f, 372f, -82f, 620f, 448f, 82f)
        },
        module(
            "boilers", "Kampon boilers", ModuleKind.ENGINE,
            detail = "12 oil-fired",
            info = "Twelve boilers trunked into one enormous raked funnel. Japan had almost "
                + "no domestic oil, which is the strategic contradiction at the heart of this "
                + "ship: a fleet built to fight for oil that needed oil to fight.",
        ) {
            cylinderZ(Role.TRIM, 2, 400f, 390f, 42f, -70f, 70f, 16)
        },
        module(
            "turret_a", "No. 1 460 mm turret", ModuleKind.GUN,
            detail = "3 × 46 cm · 1,460 kg shells",
            info = "The largest naval guns ever mounted. Each turret weighs 2,774 tonnes - "
                + "more than a destroyer - and each shell weighs 1,460 kg and can be thrown "
                + "42 km. At that range the shell is in the air for about ninety seconds, "
                + "during which the target can move a kilometre.",
        ) {
            box3(Role.BODY, 0, 716f, 284f, -76f, 866f, 332f, 76f)
            cylinderZ(Role.BODY, 0, 790f, 332f, 62f, -62f, 62f, 18)
        },
        module(
            "magazine", "46 cm magazines", ModuleKind.MAGAZINE,
            detail = "1,080 rounds, below the waterline",
            info = "Shell rooms and powder handling below the armoured deck. When Yamato "
                + "capsized on 7 April 1945 her forward magazine detonated, and the "
                + "explosion was seen from 160 km away.",
        ) {
            shellRack(730f, 476f, 0f, 6, r = 15f, len = 88f, spacing = 32f, halfZ = 48f)
            shellRack(170f, 476f, 0f, 6, r = 15f, len = 88f, spacing = 32f, halfZ = 48f)
        },
        module(
            "pagoda", "Pagoda bridge", ModuleKind.CREW,
            detail = "Command, fire control, rangefinders",
            info = "The stacked tower carries the bridge, the flag bridge, and a 15 m "
                + "optical rangefinder - the largest ever fitted to a ship. Japanese optics "
                + "were superb, and the war was decided by radar the ship did not have.",
        ) {
            crewman(540f, 240f, -22f)
            crewman(540f, 240f, 22f)
            box3(Role.BODY, 0, 486f, 190f, -50f, 600f, 330f, 50f)
        },
        module(
            "rangefinder", "15 m rangefinder", ModuleKind.OPTICS,
            detail = "Optical, at the masthead",
            info = "Fifteen metres of base length gives extraordinary accuracy at extreme "
                + "range - in daylight, in clear weather. Radar does not care about either, "
                + "and by 1944 American ships were shooting accurately at night through "
                + "smoke at targets they could not see.",
        ) { box3(Role.BODY, 0, 496f, 156f, -60f, 582f, 188f, 60f) },
        module(
            "armour_belt", "Main belt", ModuleKind.ARMOUR,
            detail = "410 mm, inclined 20°",
            info = "The thickest belt ever fitted to a warship, and the turret faces were "
                + "650 mm. She was designed to be immune to her own guns within a band of "
                + "ranges - the standard the era set for itself. No enemy battleship ever "
                + "tested it.",
        ) {
            box3(Role.BODY, 0, 280f, 392f, 92f, 720f, 456f, 110f)
            box3(Role.BODY, 0, 280f, 392f, -110f, 720f, 456f, -92f)
        },
        module(
            "fuel", "Fuel oil", ModuleKind.FUEL,
            detail = "6,300 t",
            info = "On her last sortie she was given only enough for a one-way voyage. There "
                + "was no more to give - and no plan for her to come back.",
        ) {
            fuelCell(280f, 440f, -86f, 660f, 466f, 86f)
        },
        module(
            "aa", "25 mm AA battery", ModuleKind.GUN,
            detail = "Up to 162 barrels by 1945",
            info = "The Type 96 25 mm was slow-training, had a small magazine, and shook too "
                + "much to be accurate. Japan added them by the hundred because there was "
                + "nothing better - and on 7 April 1945 they did almost nothing against 386 "
                + "American aircraft.",
        ) {
            for (i in 0 until 4) {
                cylinderZ(Role.BODY, 0, 300f + i * 60f, 320f, 15f, 60f, 84f, 10)
                cylinderZ(Role.BODY, 0, 300f + i * 60f, 320f, 15f, -84f, -60f, 10)
            }
        },
        module(
            "torpedo_bulge", "Anti-torpedo bulge", ModuleKind.ARMOUR,
            detail = "Multi-layer void and liquid",
            info = "Layers of void and liquid outboard of the citadel, designed to absorb a "
                + "torpedo before it reaches the hull proper. It was designed against 1935 "
                + "torpedoes; ten aerial torpedoes and seven bombs on the same side overwhelmed "
                + "it in two hours.",
        ) {
            box3(Role.BODY, 0, 260f, 430f, 110f, 740f, 470f, 126f)
            box3(Role.BODY, 0, 260f, 430f, -126f, 740f, 470f, -110f)
        },
        module(
            "steering", "Steering gear", ModuleKind.POWER,
            detail = "Main and auxiliary rudders, in line",
            info = "Two rudders one behind the other rather than side by side, which "
                + "turns a 263-metre ship in a smaller circle than anything her size had "
                + "managed - and which also means one torpedo aft can jam both.",
        ) {
            box3(Role.BODY, 0, 96f, 380f, -46f, 190f, 434f, 46f)
            cylinderZ(Role.TRIM, 1, 130f, 378f, 20f, -34f, 34f, 12)
        },
    )

    private val history = """
## The last argument for the battleship

Yamato and her sister Musashi were the largest and most powerful battleships ever built:
72,000 tonnes, nine 46 cm guns firing 1,460 kg shells to 42 km, and a belt of 410 mm
inclined at 20 degrees. Japan built them on a deliberate calculation - it could not match
American industrial output, so each of its ships would have to be individually superior.

The calculation was sound and the premise was obsolete. By the time Yamato commissioned in
December 1941 - eight days after Pearl Harbor, an attack her own navy had just carried out
with aircraft - the battleship was no longer the arbiter of sea power.

## What she actually did

Almost nothing. She spent most of the war at anchor as fleet flagship, consuming oil Japan
could not spare. At Leyte Gulf in October 1944 she finally engaged surface ships - escort
carriers and destroyers - and turned away. Her 46 cm guns were fired at an enemy vessel on
exactly one occasion.

## 7 April 1945

Sent to Okinawa with fuel for a one-way voyage and orders to beach herself as a gun
battery, she was found by American carrier aircraft in the East China Sea. 386 of them
attacked in waves over two hours. She took at least ten torpedoes and seven bombs, all
concentrated on one side so she could not correct the list by counter-flooding, capsized,
and her forward magazine detonated. The explosion was seen 160 km away.

2,498 of her 2,700 crew died. Ten American aircraft were lost.

## Inside

Open the hull and the proportions tell the story. Each 46 cm turret weighs 2,774 tonnes -
more than a destroyer - and the armour is the thickest ever fitted to a ship. All of it is
arranged to win a gunnery duel against another battleship at 30 km, in daylight, in clear
weather.

She carried a 15 m optical rangefinder, the largest ever built, and superb Japanese
optics. She did not carry radar worth the name. By 1944 American ships were shooting
accurately at night, through smoke, at targets they could not see - and optical excellence
had simply stopped being the relevant skill.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "~2,700"),
        Spec("Displacement", "72,800 t full load"),
        Spec("Length", "263 m"),
        Spec("Beam", "38.9 m"),
        Spec("Machinery", "Kampon turbines, 150,000 shp"),
        Spec("Speed", "27 knots"),
        Spec("Main armament", "9 × 46 cm"),
        Spec("Shell weight", "1,460 kg"),
        Spec("Maximum range", "42 km"),
        Spec("Armour belt", "410 mm at 20°"),
        Spec("Sunk", "7 April 1945"),
    )

    private val quiz = listOf(
        Question(
            "Why did Japan build ships as large as Yamato?",
            listOf(
                "To carry the largest possible aircraft complement",
                "It could not match American output, so each ship had to be individually superior",
                "To operate in the shallow waters of the western Pacific",
                "Treaty limits allowed it after 1936",
            ),
            1,
            "A sound calculation from an obsolete premise. By the time she commissioned - "
                + "eight days after Pearl Harbor - the battleship was no longer what decided "
                + "sea power.",
        ),
        Question(
            "What did Yamato have instead of effective radar?",
            listOf(
                "Aircraft spotting only",
                "A 15 m optical rangefinder, the largest ever fitted to a ship",
                "Sound-ranging equipment",
                "Nothing at all",
            ),
            1,
            "Superb optics, useless at night or through smoke. By 1944 American ships were "
                + "shooting accurately at targets they could not see, and optical excellence "
                + "had stopped being the relevant skill.",
        ),
        Question(
            "Why was the damage on 7 April 1945 concentrated on one side?",
            listOf(
                "That was the side facing the aircraft's approach",
                "Deliberately - so the ship could not correct the list by counter-flooding",
                "The other side's defences were stronger",
                "The torpedoes were released from too far out to aim",
            ),
            1,
            "American pilots were briefed to attack one flank. With the flooding all on one "
                + "side, counter-flooding could not level her and she capsized.",
        ),
        Question(
            "How many times did Yamato fire her main guns at an enemy ship?",
            listOf("Never", "Once", "Four times", "Throughout the war"),
            1,
            "Once, at Leyte Gulf in October 1944, against escort carriers and destroyers - "
                + "and then she turned away. The most powerful battleship ever built spent "
                + "the war consuming oil Japan could not spare.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
