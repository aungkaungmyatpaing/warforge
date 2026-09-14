package com.maddog.warforge.data.lore

import com.maddog.warforge.data.Lore
import com.maddog.warforge.data.ModuleKind
import com.maddog.warforge.data.Question
import com.maddog.warforge.data.Spec
import com.maddog.warforge.data.crewman
import com.maddog.warforge.data.fuelCell
import com.maddog.warforge.data.module
import com.maddog.warforge.poly.Role
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.cylinderZ

/** SM U-9, opened up. Pressure hull centreline near y = 398. */
object UBoatLore {

    private val modules = listOf(
        module(
            "diesels", "Körting paraffin engines", ModuleKind.ENGINE,
            detail = "2 × 4-cylinder · 1,000 hp total",
            info = "U-9 burned paraffin rather than diesel, and the exhaust left a white "
                + "smoke trail visible for miles - a fatal thing for a boat whose only "
                + "defence is not being seen. Germany switched to diesels for exactly this "
                + "reason, and every submarine since has run on them.",
        ) {
            box3(Role.BODY, 0, 210f, 372f, -40f, 400f, 424f, 40f)
            cylinderZ(Role.TRIM, 2, 250f, 360f, 14f, -34f, 34f, 12)
        },
        module(
            "motors", "Electric motors", ModuleKind.ENGINE,
            detail = "2 × 580 hp, battery driven",
            info = "Submerged, a boat of this era runs on batteries alone. Everything about "
                + "early submarine tactics follows from that: a few hours dived at low speed, "
                + "then the boat must surface to recharge - which is when it is killed.",
        ) { box3(Role.BODY, 0, 400f, 378f, -36f, 500f, 420f, 36f) },
        module(
            "batteries", "Lead-acid battery cells", ModuleKind.FUEL,
            detail = "Under the deck plates",
            info = "The single most dangerous thing in the boat. Seawater reaching a "
                + "lead-acid cell produces chlorine gas, so any flooding in the battery "
                + "compartment threatens to kill the crew long before the boat sinks.",
        ) { fuelCell(400f, 424f, -46f, 620f, 450f, 46f) },
        module(
            "fuel", "Fuel bunkers", ModuleKind.FUEL,
            detail = "Between the hulls",
            info = "Carried outside the pressure hull in the space between it and the outer "
                + "casing - a saddle tank. It uses volume that is no use for anything else "
                + "and keeps flammable liquid outside the living space.",
        ) {
            fuelCell(300f, 430f, 50f, 640f, 452f, 62f)
            fuelCell(300f, 430f, -62f, 640f, 452f, -50f)
        },
        module(
            "torpedo_fwd", "Bow torpedo tubes", ModuleKind.MAGAZINE,
            detail = "2 × 45 cm",
            info = "Six torpedoes aboard in total. On 22 September 1914 U-9 fired them at "
                + "three British armoured cruisers in succession and sank all three in "
                + "seventy-five minutes, killing 1,459 men.",
        ) {
            cylinderZ(Role.BODY, 0, 830f, 388f, 17f, -22f, 22f, 12)
            cylinderZ(Role.BODY, 0, 830f, 410f, 17f, -22f, 22f, 12)
            cylinderZ(Role.TRIM, 1, 700f, 388f, 15f, -20f, 20f, 12)
        },
        module(
            "control", "Control room", ModuleKind.CREW,
            detail = "Under the conning tower",
            info = "Hydroplanes, ballast controls, periscope and chart table in a space about "
                + "the size of a small van. The captain conns from here or from the tower "
                + "above it.",
        ) {
            crewman(500f, 392f, -20f)
            crewman(500f, 392f, 20f)
            box3(Role.BODY, 0, 450f, 352f, -36f, 560f, 410f, 36f)
        },
        module(
            "crew", "Crew space", ModuleKind.CREW,
            detail = "29 officers and men",
            info = "Twenty-nine men in a steel tube with no refrigeration, no fresh water to "
                + "spare, one lavatory that could not be used below a certain depth, and "
                + "bunks shared in shifts. Patrols lasted weeks.",
        ) {
            crewman(640f, 400f, -18f)
            crewman(700f, 400f, 18f)
            crewman(300f, 400f, 0f, facing = -1f)
        },
        module(
            "ballast", "Ballast tanks", ModuleKind.MAGAZINE,
            detail = "Between pressure hull and casing",
            info = "Flood them and the boat sinks; blow them with compressed air and it "
                + "rises. Diving is not done with the planes but with weight - the planes "
                + "only control the angle on the way.",
        ) {
            box3(Role.BODY, 0, 260f, 420f, 46f, 820f, 448f, 60f)
            box3(Role.BODY, 0, 260f, 420f, -60f, 820f, 448f, -46f)
        },
        module(
            "periscope", "Periscopes", ModuleKind.OPTICS,
            detail = "Attack and search",
            info = "The only way to see out submerged, and a wake-maker: a periscope at "
                + "speed leaves a feather of spray that a sharp lookout can spot. Everything "
                + "about submarine attack is a compromise between seeing and being seen.",
        ) {
            cylinderZ(Role.METAL, 1, 490f, 250f, 5f, -5f, 5f, 8)
            cylinderZ(Role.METAL, 0, 512f, 262f, 5f, -5f, 5f, 8)
        },
        module(
            "pressure_hull", "Pressure hull", ModuleKind.ARMOUR,
            detail = "Riveted steel, 50 m test depth",
            info = "A cylinder is the only shape that resists external pressure efficiently, "
                + "which is why every submarine looks like this. Fifty metres was the design "
                + "limit; crews routinely went deeper because the alternative was worse.",
        ) {
            cylinderZ(Role.BODY, 0, 500f, 398f, 48f, -1f, 1f, 20)
        },
        module(
            "atmosphere", "Air purification", ModuleKind.LIFE,
            detail = "Potash cartridges and oxygen flasks",
            info = "Submerged, the boat's air is what it dived with. Potash cartridges "
                + "absorb the carbon dioxide and compressed oxygen is bled in to replace "
                + "what is breathed - and when both are used up the boat surfaces whether "
                + "it is safe to or not.",
        ) {
            box3(Role.BODY, 0, 400f, 380f, -24f, 470f, 412f, 24f)
            cylinderZ(Role.TRIM, 1, 500f, 386f, 12f, -20f, 20f, 10)
        },
    )

    private val history = """
## Seventy-five minutes

On the morning of 22 September 1914, Kapitänleutnant Otto Weddigen's U-9 found three
British armoured cruisers - Aboukir, Hogue and Cressy - patrolling in line off the Dutch
coast. He torpedoed Aboukir. The other two stopped to pick up survivors, believing they
had hit a mine. He torpedoed both.

Three ships and 1,459 men were lost in seventy-five minutes to a single small boat with
six torpedoes and a crew of twenty-nine. Every navy in the world rewrote its doctrine that
week: no stopping for survivors, no steaming in predictable lines, and an urgent search for
a way to detect something underwater.

## What the boat actually was

U-9 is barely a submarine by later standards. She is a surface boat that can hide. Her
paraffin engines drove her at 14 knots on the surface; submerged she ran on batteries at 8
knots for about an hour, or 5 for a few hours. She could not stay down for a day. Diving
was a tactic, not a way of life.

The paraffin engines were a liability in themselves - the exhaust left a white smoke trail
visible for miles, which is an unfortunate quality in a vessel whose whole defence is not
being seen. Germany switched to diesels shortly afterwards, and every submarine since has
used them.

## Inside

Open the hull and what strikes you is how little of it is for the crew. A cylinder of
riveted steel - a cylinder because that is the only efficient shape against external
pressure - with engines at one end, torpedoes at the other, batteries under the floor, and
twenty-nine men in the gaps. No refrigeration, one lavatory unusable below a certain
depth, bunks shared in shifts, and patrols measured in weeks.

The batteries under the deck plates are the hidden danger. Seawater in a lead-acid cell
produces chlorine, so flooding in the battery compartment can kill a crew long before the
boat itself is lost.

## Afterwards

Weddigen was killed in 1915 when his next boat was rammed by a battleship. U-9 survived
the war and was broken up in 1919. The lesson she taught - that something cheap and
invisible can destroy something enormous and expensive - has never stopped being true.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "29"),
        Spec("Displacement", "493 t surfaced / 611 t dived"),
        Spec("Length", "57.4 m"),
        Spec("Machinery", "2 × paraffin, 2 × electric"),
        Spec("Speed", "14 kn surfaced / 8 kn dived"),
        Spec("Range", "3,000 nmi surfaced"),
        Spec("Test depth", "50 m"),
        Spec("Torpedo tubes", "4 (2 bow, 2 stern)"),
        Spec("Torpedoes carried", "6"),
        Spec("Commissioned", "1910"),
    )

    private val quiz = listOf(
        Question(
            "What did U-9 do on 22 September 1914?",
            listOf(
                "Sank a battleship at anchor",
                "Sank three British armoured cruisers in seventy-five minutes",
                "Made the first submerged torpedo attack in history",
                "Blockaded the Thames estuary single-handed",
            ),
            1,
            "Aboukir, Hogue and Cressy - 1,459 men - lost to one small boat. The second and "
                + "third stopped to pick up survivors, and every navy in the world rewrote its "
                + "doctrine that week.",
        ),
        Question(
            "Why were U-9's paraffin engines a serious liability?",
            listOf(
                "They could not be run submerged",
                "Their exhaust left a white smoke trail visible for miles",
                "They caught fire easily",
                "They were too weak for useful surface speed",
            ),
            1,
            "A smoke trail is a fatal quality in a vessel whose only defence is not being "
                + "seen. Germany moved to diesels immediately afterwards.",
        ),
        Question(
            "Why is a submarine's pressure hull a cylinder?",
            listOf(
                "It is the easiest shape to rivet",
                "A cylinder resists external pressure most efficiently for its weight",
                "It gives the best hydrodynamic shape",
                "It maximises internal volume",
            ),
            1,
            "Pressure acts inward from all sides, and a cylinder carries that as pure hoop "
                + "stress. Every submarine ever built is a cylinder inside whatever outer "
                + "shape it needs.",
        ),
        Question(
            "What makes the battery compartment the most dangerous space aboard?",
            listOf(
                "It can explode if overcharged",
                "Seawater reaching lead-acid cells produces chlorine gas",
                "It is directly below the torpedo storage",
                "It cannot be sealed off from the engine room",
            ),
            1,
            "Chlorine kills a crew long before the boat sinks. Flooding anywhere near the "
                + "cells is an emergency of a different order from flooding elsewhere.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
