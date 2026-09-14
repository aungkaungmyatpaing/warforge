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
import com.maddog.warforge.solid.revolveX
import com.maddog.warforge.solid.sphere

/** B-17G Flying Fortress, opened up. Centreline near y = 294. */
object B17Lore {

    private val modules = listOf(
        module(
            "eng_inboard", "Wright R-1820 (inboard)", ModuleKind.ENGINE,
            detail = "29.9 L nine-cylinder radial · 1,200 hp",
            info = "Turbo-supercharged, which is the whole reason the B-17 could operate at "
                + "7,600 m. The turbocharger uses exhaust gas to cram thin air into the "
                + "cylinders, and without it the engines would lose most of their power "
                + "before the bomber reached the altitude the flak could not.",
        ) { revolveX(Role.BODY, 0, 14, 626f, 330f, 34f, 664f, 330f, 38f) },
        module(
            "eng_outboard", "Wright R-1820 (outboard)", ModuleKind.ENGINE,
            detail = "2 more per side",
            info = "Four engines on a bomber are not about speed. They are about surviving "
                + "the loss of one or two - and B-17s came home on two engines, on one, and "
                + "in one famous case with a whole tailplane missing.",
        ) { revolveX(Role.BODY, 0, 14, 486f, 332f, 30f, 520f, 332f, 34f) },
        module(
            "turbo", "Turbo-superchargers", ModuleKind.ENGINE,
            detail = "General Electric, exhaust-driven",
            info = "Under each nacelle, spinning at 21,000 rpm on the engine's own exhaust. "
                + "American turbocharger production was a genuine industrial advantage; "
                + "nobody else fielded them in quantity.",
        ) {
            box3(Role.BODY, 0, 600f, 352f, 150f, 660f, 372f, 200f)
            box3(Role.BODY, 0, 600f, 352f, -200f, 660f, 372f, -150f)
        },
        module(
            "bombbay", "Bomb bay", ModuleKind.MAGAZINE,
            detail = "2,700 kg maximum",
            info = "A vertical rack amidships, on the centre of gravity so the aeroplane's "
                + "balance does not change as it empties. The load is small for the size of "
                + "the aircraft - a Lancaster carried two and a half times as much - because "
                + "the B-17 spent its weight on guns, armour and altitude instead.",
        ) {
            for (i in 0 until 4) {
                cylinderZ(Role.BODY, 0, 480f + i * 34f, 330f, 13f, -26f, 26f, 10)
            }
        },
        module(
            "pilot", "Pilot and co-pilot", ModuleKind.CREW,
            detail = "Flight deck, side by side",
            info = "Two pilots, because a mission was eight hours of formation flying with "
                + "no autopilot help in the bomber stream and a great deal of muscle needed "
                + "on the controls.",
        ) {
            crewman(742f, 300f, -24f)
            crewman(742f, 300f, 24f)
        },
        module(
            "bombardier", "Bombardier and navigator", ModuleKind.CREW,
            detail = "In the glazed nose",
            info = "The bombardier flies the aeroplane through the Norden bombsight for the "
                + "last minutes of the run - the sight is coupled to the autopilot, so on "
                + "the bomb run he, not the pilot, is in control.",
        ) {
            crewman(842f, 300f, -18f)
            crewman(800f, 302f, 18f)
        },
        module(
            "gunners", "Waist and top gunners", ModuleKind.CREW,
            detail = "Ten crew in all",
            info = "Thirteen .50 calibre guns need men to work them. A B-17 crew is ten: two "
                + "pilots, bombardier, navigator, engineer, radio operator and four gunners - "
                + "and every one of them is a target.",
        ) {
            crewman(700f, 300f, 0f)
            crewman(430f, 302f, -30f)
            crewman(400f, 302f, 30f)
        },
        module(
            "ball_turret", "Ball turret", ModuleKind.GUN,
            detail = "2 × .50 in, retractable",
            info = "The gunner curls into a sphere hung under the fuselage, too small to "
                + "wear a parachute in. If the hydraulics failed with the turret facing down, "
                + "he could not get out - and on a belly landing there was nothing anyone "
                + "could do for him.",
        ) {
            sphere(Role.BODY, 0, 380f, 356f, 0f, 34f, 14, 8)
            crewman(380f, 366f, 0f, half = 12f)
        },
        module(
            "guns", "Thirteen .50 calibre Brownings", ModuleKind.GUN,
            detail = "Nose, top, ball, waist, tail",
            info = "The 'Fortress' name is the doctrine: enough guns, in tight enough "
                + "formation, that bombers could defend themselves without escort. The theory "
                + "failed badly over Schweinfurt in 1943 and was abandoned once the P-51 "
                + "could escort all the way.",
        ) {
            box3(Role.BODY, 0, 716f, 236f, -16f, 790f, 252f, 16f)
            box3(Role.BODY, 0, 130f, 274f, -14f, 190f, 302f, 14f)
        },
        module(
            "fuel", "Wing tanks", ModuleKind.FUEL,
            detail = "10,600 L",
            info = "Self-sealing, which is why so many B-17s came home holed. A layer of "
                + "untreated rubber swells when petrol touches it and closes the hole.",
        ) {
            fuelCell(500f, 322f, 130f, 600f, 344f, 420f)
            fuelCell(500f, 322f, -420f, 600f, 344f, -130f)
        },
        module(
            "oxygen", "Oxygen system", ModuleKind.AVIONICS,
            detail = "Bottles for ten men, eight hours",
            info = "At 7,600 m an unsupplied man loses useful consciousness in a couple of "
                + "minutes. A severed oxygen line was as lethal as a cannon shell, and "
                + "frostbite at −40 °C in an unheated aeroplane cost more casualties than "
                + "many people expect.",
        ) {
            for (i in 0 until 3) cylinderZ(Role.BODY, 0, 460f + i * 30f, 300f, 11f, -34f, 34f, 10)
        },
        module(
            "armour", "Armour plate and flak jackets", ModuleKind.ARMOUR,
            detail = "Behind pilots and gunners",
            info = "Plates behind the pilots' seats and at the gun positions. A bomber cannot "
                + "be armoured all over and stay in the air, so the steel goes exactly where "
                + "the irreplaceable parts are.",
        ) {
            box3(Role.BODY, 0, 716f, 268f, -34f, 726f, 320f, 34f)
        },
        module(
            "electrics", "Electrical system", ModuleKind.POWER,
            detail = "24 V · generators on all four engines",
            info = "Almost everything on a Fortress is electric rather than hydraulic: "
                + "turrets, flaps, undercarriage, bomb doors. Nothing to leak and catch "
                + "fire when it is hit, and it goes on working with an engine or two shot "
                + "away - which is a large part of why they came home.",
        ) {
            box3(Role.BODY, 0, 430f, 288f, -40f, 500f, 322f, 40f)
            box3(Role.TRIM, 2, 500f, 296f, -24f, 512f, 314f, 24f)
        },
    )

    private val history = """
## A promise that did not hold

The B-17 was built on a theory: that bombers flying in tight formation, bristling with
heavy machine guns, could fight their way to a target in daylight and back without escort.
Hence the name. Hence thirteen guns on the G model, and hence ten men aboard to work them.

The theory was tested over Schweinfurt and Regensburg in August and October 1943. Of 291
bombers sent on the second Schweinfurt raid, 60 did not come back and 17 more were written
off - a loss rate that would have destroyed the force in a month. Unescorted daylight
bombing stopped until the P-51 Mustang could go all the way to Berlin and back.

## Why it could fly so high

Turbo-superchargers. Each engine's exhaust spins a turbine at 21,000 rpm that crams thin
air into the cylinders, so the B-17 keeps its power at 7,600 m where the flak is less
accurate and most fighters are struggling. American turbocharger production was a genuine
industrial advantage that nobody else matched in quantity.

## The bomb load

For an aircraft of its size the B-17 carried remarkably little: about 2,700 kg against a
Lancaster's 6,300 kg. The difference went into guns, armour, altitude and a tenth crewman.
Whether that was the right trade was argued through the whole war and is argued still.

## Coming home

What the B-17 was genuinely extraordinary at was absorbing damage. Bombers returned with
engines gone, control surfaces shot away, whole sections of fuselage missing. Self-sealing
tanks closed bullet holes with swelling rubber. The airframe simply refused to fall apart.

## The ball turret

Open the fuselage and the thing that stops you is the sphere hung under it. The gunner
curls up inside, too cramped to wear a parachute. If the hydraulics failed with the turret
pointing down he could not get out, and on a belly landing nothing could be done for him.
It was the most dangerous position on the aeroplane, and the aeroplane needed it.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "10"),
        Spec("Empty weight", "16,391 kg"),
        Spec("Max takeoff", "29,710 kg"),
        Spec("Wingspan", "31.62 m"),
        Spec("Engines", "4 × Wright R-1820, 1,200 hp"),
        Spec("Top speed", "462 km/h"),
        Spec("Service ceiling", "10,850 m"),
        Spec("Range (with bombs)", "3,219 km"),
        Spec("Bomb load", "2,700 kg"),
        Spec("Defensive guns", "13 × .50 in"),
        Spec("Built", "12,731"),
    )

    private val quiz = listOf(
        Question(
            "What does the 'Fortress' in Flying Fortress refer to?",
            listOf(
                "Its armour plating",
                "The doctrine that bombers with enough guns could fly unescorted in daylight",
                "Its ability to absorb battle damage",
                "The reinforced bomb bay structure",
            ),
            1,
            "Thirteen heavy machine guns and tight formation flying were supposed to make "
                + "escorts unnecessary. Schweinfurt in October 1943 - 60 of 291 bombers lost - "
                + "ended that theory.",
        ),
        Question(
            "Why could the B-17 operate at 7,600 m when many aircraft could not?",
            listOf(
                "A pressurised cabin",
                "Turbo-superchargers driven by the engines' own exhaust",
                "An exceptionally large wing",
                "Lightweight construction",
            ),
            1,
            "Each turbo spins at 21,000 rpm on exhaust gas and crams thin air into the "
                + "cylinders. American turbocharger production in quantity was an advantage "
                + "nobody else matched.",
        ),
        Question(
            "Why did the B-17 carry a smaller bomb load than a Lancaster?",
            listOf(
                "Its bomb bay was structurally weaker",
                "The weight went into guns, armour, altitude and a tenth crewman instead",
                "American bombs were heavier",
                "It flew shorter ranges",
            ),
            1,
            "2,700 kg against 6,300 kg. The difference bought defensive armament and altitude "
                + "for daylight precision bombing - a trade argued about ever since.",
        ),
        Question(
            "What made the ball turret the worst position on the aircraft?",
            listOf(
                "It was the coldest place in the bomber",
                "The gunner could not fit a parachute in it, and could be trapped if the hydraulics failed",
                "It had the least effective field of fire",
                "It was the first position fighters attacked",
            ),
            1,
            "Curled into a sphere under the fuselage with no room for a parachute. With the "
                + "turret jammed facing down, the gunner could not get out at all.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
