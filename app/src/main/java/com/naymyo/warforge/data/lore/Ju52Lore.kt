package com.naymyo.warforge.data.lore

import com.naymyo.warforge.data.Lore
import com.naymyo.warforge.data.ModuleKind
import com.naymyo.warforge.data.Question
import com.naymyo.warforge.data.Spec
import com.naymyo.warforge.data.crewman
import com.naymyo.warforge.data.fuelCell
import com.naymyo.warforge.data.module
import com.naymyo.warforge.poly.Role
import com.naymyo.warforge.solid.box3
import com.naymyo.warforge.solid.cylinderZ
import com.naymyo.warforge.solid.revolveX

/** Junkers Ju 52/3m, opened up. Centreline near y = 292. */
object Ju52Lore {

    private val modules = listOf(
        module(
            "eng_nose", "BMW 132 radial (nose)", ModuleKind.ENGINE,
            detail = "27.7 L nine-cylinder radial · 725 hp",
            info = "A licence-built Pratt & Whitney Hornet. Three of these give 2,175 hp - "
                + "modest for an 11-tonne aeroplane, which is why the Ju 52 is famously slow "
                + "and equally famously able to get off a short, soft field.",
        ) { revolveX(Role.BODY, 0, 16, 766f, 292f, 46f, 812f, 292f, 50f) },
        module(
            "eng_wing", "Wing engines", ModuleKind.ENGINE,
            detail = "2 × BMW 132",
            info = "Hung under the wing in nacelles. Three engines meant an aeroplane could "
                + "keep flying on two, which in the 1930s - before engines were reliable - "
                + "was the difference between an airline and an adventure.",
        ) {
            revolveX(Role.BODY, 0, 14, 640f, 358f, 34f, 676f, 358f, 38f)
            cylinderZ(Role.BODY, 0, 0f, 0f, 0f, 0f, 0f, 6)
            box3(Role.BODY, 0, 560f, 338f, 150f, 668f, 384f, 210f)
            box3(Role.BODY, 0, 560f, 338f, -210f, 668f, 384f, -150f)
        },
        module(
            "structure", "Corrugated duralumin", ModuleKind.ARMOUR,
            detail = "Junkers Lamellenblech skin",
            info = "The corrugations are structure, not decoration: a corrugated sheet "
                + "carries load along its length, so the skin itself takes the bending and "
                + "the frame beneath it can be light. It costs drag - about 15 km/h - and "
                + "buys an airframe that is very nearly impossible to break.",
        ) {
            for (i in 0 until 9) {
                box3(Role.BODY, 1, 220f + i * 60f, 244f, -56f, 226f + i * 60f, 344f, 56f)
            }
        },
        module(
            "pilot", "Pilot", ModuleKind.CREW,
            detail = "Left seat",
            info = "Two pilots side by side in a glazed cockpit, with a radio operator "
                + "behind. The Ju 52 was designed as an airliner and the flight deck shows it.",
        ) { crewman(736f, 296f, -26f) },
        module(
            "copilot", "Co-pilot", ModuleKind.CREW,
            detail = "Right seat",
            info = "On the paratroop and transport runs of 1940-45 the second pilot was often "
                + "the man watching for fighters, because the Ju 52 had almost no defensive "
                + "armament and no speed to run.",
        ) { crewman(736f, 296f, 26f) },
        module(
            "cargo", "Cargo and troop bay", ModuleKind.MAGAZINE,
            detail = "17 troops or 1,500 kg",
            info = "The whole point of the aeroplane. A bay large enough for seventeen "
                + "paratroopers, a dismantled gun, or a load of wounded - and a door big "
                + "enough to get them out fast.",
        ) { box3(Role.BODY, 0, 300f, 256f, -50f, 620f, 336f, 50f) },
        module(
            "fuel", "Wing fuel tanks", ModuleKind.FUEL,
            detail = "2,400 L",
            info = "In the thick cantilever wing, away from the cabin. Range about 1,000 km, "
                + "which is what made the type useful for supply flights nobody else could "
                + "attempt.",
        ) {
            fuelCell(470f, 326f, 90f, 556f, 346f, 320f)
            fuelCell(470f, 326f, -320f, 556f, 346f, -90f)
        },
        module(
            "radio", "FuG radio", ModuleKind.RADIO,
            detail = "Transmitter, receiver, direction finder",
            info = "Behind the flight deck, with a dedicated operator. Direction-finding "
                + "equipment was what made scheduled airline flying possible before radar.",
        ) { box3(Role.BODY, 0, 660f, 260f, -40f, 720f, 300f, 40f) },
    )

    private val history = """
## Aunt Ju

The Ju 52 was designed as an airliner and spent the 1930s being one: Lufthansa, Swissair,
British Airways, and airlines from Bolivia to Finland. Corrugated skin, three radial
engines, fixed undercarriage, a cabin for seventeen. Slow, safe and almost unbreakable.

Then Germany went to war and the same aeroplane carried the Condor Legion to Spain,
dropped paratroopers on Eben-Emael and Crete, and flew supplies into Stalingrad. Around
4,800 were built and the Luftwaffe never had enough of them.

## Why corrugated

Hugo Junkers' Lamellenblech skin is the aeroplane's signature and its structural principle.
A corrugated sheet carries load along its corrugations, so the skin can be part of the
structure rather than a covering over it. That lets the internal frame be far lighter than
it would otherwise need to be.

The cost is drag - perhaps 15 km/h - and by the mid-1930s smooth stressed skins had
overtaken it. The benefit is an airframe that essentially cannot be broken, which is why
Ju 52s were still flying commercial routes in the 1980s.

## Three engines

Before engines were reliable, three of them meant an aeroplane could lose one and keep
flying. Every serious airliner of the late 1920s and early 1930s was a trimotor for this
reason - the Ford Trimotor, the Fokker F.VII, the Ju 52. Once engines became dependable,
the extra drag and maintenance stopped being worth it, and the type disappeared.

## Stalingrad

The Ju 52's limits were exposed in the winter of 1942-43. The Luftwaffe promised to supply
a surrounded army of 250,000 men by air, at a rate of 500 tonnes a day. The Ju 52 carries
about 1.5 tonnes. The arithmetic never worked, the weather made it worse, and nearly 500
transports were lost trying.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "2-3"),
        Spec("Capacity", "17 troops or 1,500 kg"),
        Spec("Empty weight", "5,720 kg"),
        Spec("Loaded weight", "11,030 kg"),
        Spec("Wingspan", "29.25 m"),
        Spec("Engines", "3 × BMW 132, 725 hp each"),
        Spec("Cruise speed", "211 km/h"),
        Spec("Range", "1,000 km"),
        Spec("Service ceiling", "5,490 m"),
        Spec("Built", "~4,835"),
    )

    private val quiz = listOf(
        Question(
            "Why is the Ju 52's skin corrugated?",
            listOf(
                "To shed rain and prevent corrosion",
                "The corrugations carry load, so the skin is structure and the frame can be light",
                "To improve airflow over the wing",
                "To make the panels easier to stamp",
            ),
            1,
            "A corrugated sheet carries load along its length. Junkers made the skin part of "
                + "the structure, at a cost of about 15 km/h in drag and a gain of an airframe "
                + "that barely wears out.",
        ),
        Question(
            "Why did 1930s airliners have three engines?",
            listOf(
                "To carry more fuel",
                "Engines were unreliable, and three meant losing one was survivable",
                "To balance the aircraft in a crosswind",
                "Regulations required it over water",
            ),
            1,
            "Reliability, not power. Once engines became dependable the extra drag and "
                + "maintenance stopped being worth it and the trimotor vanished.",
        ),
        Question(
            "Why did the Stalingrad airlift fail?",
            listOf(
                "Soviet fighters destroyed the transports on the ground",
                "The arithmetic never worked: 500 tonnes a day needed from aircraft carrying 1.5 tonnes each",
                "The airfields were captured in the first week",
                "There was no fuel for the aircraft",
            ),
            1,
            "The Luftwaffe promised 500 tonnes a day to 250,000 men. A Ju 52 carries about "
                + "1.5 tonnes. Weather and losses made a promise that was already arithmetically "
                + "impossible very much worse.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
