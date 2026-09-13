package com.naymyo.warforge.data.lore

import com.naymyo.warforge.data.Lore
import com.naymyo.warforge.data.ModuleKind
import com.naymyo.warforge.data.Question
import com.naymyo.warforge.data.Spec
import com.naymyo.warforge.data.crewman
import com.naymyo.warforge.data.module
import com.naymyo.warforge.poly.Role
import com.naymyo.warforge.solid.box3
import com.naymyo.warforge.solid.cylinderZ

/** Los Angeles-class SSN, opened up. Pressure hull centreline near y = 386. */
object LosAngelesLore {

    private val modules = listOf(
        module(
            "reactor", "S6G pressurised water reactor", ModuleKind.REACTOR,
            detail = "~150 MW thermal",
            info = "The thing that changes everything. A nuclear reactor needs no air and no "
                + "refuelling for decades, so the boat never has to surface. The limit on a "
                + "patrol stops being fuel or battery charge and becomes food and the crew's "
                + "endurance.",
        ) {
            cylinderZ(Role.BODY, 0, 420f, 386f, 46f, -46f, 46f, 18)
            box3(Role.TRIM, 2, 380f, 344f, -40f, 460f, 430f, 40f)
        },
        module(
            "steam_plant", "Steam turbines and turbo-generators", ModuleKind.ENGINE,
            detail = "~35,000 shp, single shaft",
            info = "The reactor makes heat; the heat makes steam; the steam turns a turbine. "
                + "Everything is mounted on rafts on flexible mounts, because the enemy is "
                + "not a torpedo but a hydrophone - noise is what kills submarines.",
        ) { box3(Role.BODY, 0, 240f, 356f, -44f, 380f, 420f, 44f) },
        module(
            "quieting", "Sound isolation rafts", ModuleKind.ARMOUR,
            detail = "Machinery on flexible mounts",
            info = "Every rotating machine sits on a raft isolated from the hull, and the "
                + "outside is covered in anechoic tiles that absorb an incoming sonar ping "
                + "and damp the boat's own noise. Quieting, not armour, is submarine "
                + "protection.",
        ) {
            for (i in 0 until 10) {
                box3(Role.BODY, 0, 260f + i * 52f, 340f, 48f, 300f + i * 52f, 430f, 52f)
                box3(Role.BODY, 0, 260f + i * 52f, 340f, -52f, 300f + i * 52f, 430f, -48f)
            }
        },
        module(
            "sonar", "AN/BQQ-5 sonar suite", ModuleKind.AVIONICS,
            detail = "Bow sphere, flank and towed arrays",
            info = "A spherical hydrophone array filling the bow, arrays along the flanks, "
                + "and a towed array streamed astern away from the boat's own noise. A "
                + "submarine fights by listening; the whole front of the boat is an ear.",
        ) {
            cylinderZ(Role.BODY, 0, 920f, 382f, 34f, -34f, 34f, 16)
            box3(Role.BODY, 0, 640f, 360f, 54f, 820f, 400f, 60f)
            box3(Role.BODY, 0, 640f, 360f, -60f, 820f, 400f, -54f)
        },
        module(
            "torpedo_room", "Torpedo room", ModuleKind.MAGAZINE,
            detail = "4 × 21 in tubes, Mk 48 and Tomahawk",
            info = "Tubes amidships rather than in the bow, because the bow is full of "
                + "sonar. A Mk 48 torpedo is guided down a wire from the boat and can be "
                + "re-aimed in flight; Tomahawk gives the submarine a role against targets "
                + "hundreds of kilometres inland.",
        ) {
            cylinderZ(Role.BODY, 0, 840f, 402f, 13f, 20f, 46f, 12)
            cylinderZ(Role.BODY, 0, 840f, 428f, 13f, 20f, 46f, 12)
            cylinderZ(Role.BODY, 0, 840f, 402f, 13f, -46f, -20f, 12)
            cylinderZ(Role.BODY, 0, 840f, 428f, 13f, -46f, -20f, 12)
        },
        module(
            "vls", "Vertical launch tubes", ModuleKind.MAGAZINE,
            detail = "12 × Tomahawk (688i)",
            info = "Twelve tubes forward of the pressure hull in the later boats, so cruise "
                + "missiles do not use up torpedo-room space. It is the change that turned "
                + "an anti-submarine hunter into a land-attack platform.",
        ) {
            for (i in 0 until 6) cylinderZ(Role.BODY, 0, 726f + i * 26f, 350f, 9f, -20f, 20f, 10)
        },
        module(
            "control", "Control room", ModuleKind.CREW,
            detail = "Under the sail",
            info = "No windows anywhere. The boat is driven on instruments and sonar - a "
                + "picture of the ocean assembled from sound alone, by people who spend "
                + "months at a time without seeing daylight.",
        ) {
            crewman(560f, 366f, -22f)
            crewman(560f, 366f, 22f)
            box3(Role.BODY, 0, 500f, 336f, -42f, 640f, 400f, 42f)
        },
        module(
            "crew", "Crew quarters", ModuleKind.CREW,
            detail = "129 officers and men",
            info = "Three-month patrols with no contact and no daylight. Bunks are stacked "
                + "three high among the torpedoes, and on a crowded boat the newest sailors "
                + "share them in shifts.",
        ) {
            crewman(680f, 400f, -20f)
            crewman(720f, 400f, 20f)
            crewman(300f, 400f, 0f, facing = -1f)
        },
        module(
            "pressure_hull", "HY-80 pressure hull", ModuleKind.ARMOUR,
            detail = "Test depth ~450 m",
            info = "High-yield steel, welded, cylindrical. Test depth is published as 'greater "
                + "than 800 feet' and is certainly a good deal more - the real figure is one "
                + "of the most closely held numbers in any navy.",
        ) { cylinderZ(Role.BODY, 0, 500f, 386f, 62f, -1f, 1f, 20) },
        module(
            "atmosphere", "Atmosphere control", ModuleKind.LIFE,
            detail = "Electrolysis, scrubbers, burners",
            info = "The boat makes its own oxygen by splitting seawater, scrubs the "
                + "carbon dioxide out amine-side, and burns off the hydrogen and carbon "
                + "monoxide the crew and their equipment give off. It is why a nuclear "
                + "submarine's endurance is set by how much food it can carry.",
        ) {
            box3(Role.BODY, 0, 420f, 372f, -40f, 520f, 412f, 40f)
            cylinderZ(Role.TRIM, 1, 470f, 370f, 18f, -30f, 30f, 12)
        },
    )

    private val history = """
## Never having to surface

Every submarine before 1955 was a surface ship that could hide for a few hours. Diesels
need air; batteries run down. A boat dived at 5 knots had perhaps a day before it had to
come up, and coming up is when submarines were found and killed.

A nuclear reactor needs no air and no refuelling for decades. Suddenly the limit on a
patrol is food and the crew's endurance, not fuel - and a submarine that never has to
surface is, for the first time, genuinely a submarine rather than a submersible.

## Built to be quiet

Open the hull and almost everything you see is about noise. Machinery sits on rafts on
flexible mounts so vibration never reaches the hull. The outside is covered in anechoic
tiles that absorb an incoming sonar ping and damp the boat's own radiated sound. The
propeller design is classified and has been for fifty years.

This is submarine armour. You cannot armour against a torpedo; you can decline to be
found.

## Built to listen

The other half of the boat is its ears. A spherical hydrophone array fills the bow - which
is why the torpedo tubes had to move amidships - with more arrays along the flanks and a
towed array streamed astern, far enough back to hear past the boat's own noise.

A submarine fights by listening. It builds a picture of an ocean it cannot see, from sound
alone, and the side that hears first usually wins - the same principle that decided tank
fights in 1943, in a different medium.

## What they did

62 Los Angeles-class boats were built between 1976 and 1996, the largest class of nuclear
submarines ever. They spent the Cold War trailing Soviet ballistic missile submarines,
close enough to hear them and quiet enough not to be heard. From 1991 the later boats
carried vertical launch tubes for Tomahawk, and an anti-submarine hunter became a
land-attack platform as well.

## What the crew did

Three months at a time with no daylight, no contact with home, and bunks stacked three
high among the torpedoes. The reactor could run for decades; the people could not.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "129"),
        Spec("Displacement", "6,927 t submerged"),
        Spec("Length", "110.3 m"),
        Spec("Beam", "10.1 m"),
        Spec("Reactor", "S6G pressurised water"),
        Spec("Speed", "20+ knots submerged (official)"),
        Spec("Test depth", "> 450 m"),
        Spec("Torpedo tubes", "4 × 21 in"),
        Spec("VLS (688i)", "12 × Tomahawk"),
        Spec("Built", "62"),
    )

    private val quiz = listOf(
        Question(
            "What did nuclear power actually change about submarines?",
            listOf(
                "It made them faster on the surface",
                "It removed the need to surface at all - the limit became food, not fuel",
                "It allowed much larger torpedoes",
                "It made them cheaper to operate",
            ),
            1,
            "Diesels need air and batteries run down, so earlier boats were surface ships "
                + "that could hide for a few hours. A reactor needs neither, and coming up is "
                + "when submarines are found and killed.",
        ),
        Question(
            "Why are the torpedo tubes amidships rather than in the bow?",
            listOf(
                "To balance the boat",
                "The bow is filled by a spherical sonar array",
                "It shortens the reload path",
                "Bow tubes are noisier",
            ),
            1,
            "A submarine fights by listening, so the whole front of the boat is an ear. The "
                + "sonar sphere gets the bow and the tubes are angled out from the sides.",
        ),
        Question(
            "What is a submarine's equivalent of armour?",
            listOf(
                "A thicker pressure hull",
                "Quieting - isolation rafts, anechoic tiles and a classified propeller",
                "Anti-torpedo decoys",
                "Depth",
            ),
            1,
            "You cannot armour against a torpedo. You can decline to be found, and almost "
                + "everything inside the hull is arranged to stop noise reaching the water.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
