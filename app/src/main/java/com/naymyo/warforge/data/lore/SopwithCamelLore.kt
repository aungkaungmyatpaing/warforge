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

/**
 * Sopwith Camel, opened up.
 *
 * Almost everything heavy - engine, guns, ammunition, fuel and pilot - is packed into the
 * first two metres of the aeroplane. That concentration is the whole story of how it
 * flew. Centreline near y = 296, nose at high x.
 */
object SopwithCamelLore {

    private val modules = listOf(
        module(
            "engine", "Clerget 9B rotary", ModuleKind.ENGINE,
            detail = "16.3 L nine-cylinder rotary · 130 hp",
            info = "A rotary engine: the crankshaft is bolted to the airframe and the whole "
                + "cylinder block spins with the propeller. A spinning 25 kg mass at 1,250 "
                + "rpm is a gyroscope, and its precession is what made the Camel turn right "
                + "faster than anything alive and try to kill anyone who turned left "
                + "carelessly.",
        ) {
            revolveX(Role.BODY, 0, 16, 716f, 300f, 46f, 762f, 300f, 50f)
            for (i in 0 until 7) {
                val a = (2.0 * Math.PI * i / 7).toFloat()
                cylinderZ(
                    Role.METAL, -1,
                    744f + 40f * kotlin.math.cos(a), 300f + 40f * kotlin.math.sin(a),
                    12f, -14f, 14f, 10,
                )
            }
        },
        module(
            "fuel", "Fuel and oil tanks", ModuleKind.FUEL,
            detail = "168 L petrol, 18 L castor oil",
            info = "Behind the engine and directly in front of the pilot. A rotary engine "
                + "throws its total-loss lubricant straight out of the cylinders, so a Camel "
                + "pilot flew through a fine spray of castor oil for two hours - with the "
                + "predictable digestive consequences.",
        ) { fuelCell(636f, 268f, -32f, 706f, 324f, 32f) },
        module(
            "pilot", "Pilot", ModuleKind.CREW,
            detail = "One, directly behind the guns",
            info = "Sits with the fuel in front of him, the guns above his knees and no "
                + "parachute - the RFC did not issue them to pilots, on the argument that "
                + "they would encourage men to leave repairable aircraft.",
        ) { crewman(560f, 302f, 0f) },
        module(
            "guns", "Twin Vickers .303", ModuleKind.GUN,
            detail = "2 × .303 in, synchronised",
            info = "Both guns sit on the fuselage top, directly ahead of the pilot's face, "
                + "firing through the propeller arc via interrupter gear. The hump their "
                + "fairing makes is what gave the aeroplane its name.",
        ) {
            box3(Role.BODY, 0, 640f, 252f, -20f, 790f, 268f, -6f)
            box3(Role.BODY, 0, 640f, 252f, 6f, 790f, 268f, 20f)
        },
        module(
            "ammo", "Ammunition boxes", ModuleKind.AMMO,
            detail = "2 × 500 rounds",
            info = "Belt feed from boxes under the gun breeches - within the pilot's reach, "
                + "because he was also his own armourer when a gun jammed, at 4,000 m, "
                + "in a fight, with a mallet.",
        ) { box3(Role.BODY, 0, 620f, 268f, -26f, 690f, 296f, 26f) },
        module(
            "structure", "Wooden box girder", ModuleKind.ARMOUR,
            detail = "Spruce and ash, wire braced",
            info = "The fuselage is a wooden frame trued up with bracing wires and covered "
                + "in doped linen. It is light, cheap, repairable with glue and tape - and "
                + "offers the pilot no protection whatever.",
        ) {
            box3(Role.BODY, 0, 240f, 282f, -16f, 640f, 288f, -16f)
            box3(Role.BODY, 0, 240f, 306f, 16f, 640f, 312f, 16f)
        },
        module(
            "controls", "Control cables", ModuleKind.AVIONICS,
            detail = "Cables to rudder, elevator and ailerons",
            info = "Wires running the length of the fuselage over pulleys. Nothing is "
                + "duplicated: one cable cut by a bullet takes an axis of control with it.",
        ) {
            box3(Role.BODY, 0, 210f, 284f, -8f, 560f, 288f, -4f)
            box3(Role.BODY, 0, 210f, 306f, 4f, 560f, 310f, 8f)
        },
    )

    private val history = """
## A gyroscope with wings

The Camel's reputation rests on one piece of physics. Its Clerget is a rotary: the
crankshaft is fixed to the airframe and the entire engine spins with the propeller. Twenty-
five kilograms of iron turning at 1,250 rpm is a gyroscope, and a gyroscope resists being
turned - it precesses instead, pushing at ninety degrees to the force applied.

For a Camel pilot that meant a right turn was astonishingly quick and a left turn was slow.
Experienced pilots turned three-quarters of the way right rather than a quarter left. It
also meant that a careless application of power at low speed could flick the aeroplane
into a spin from which there was no room to recover, and the Camel killed a great many of
its own pilots in training.

## The concentration of weight

Open the fuselage and the other half of the story is visible. Engine, guns, ammunition,
fuel and pilot are packed into the first two metres. A concentrated mass has a small
moment of inertia, and an aeroplane with a small moment of inertia changes direction fast.
The Camel was unstable in every axis - and instability, in a dogfighter, is another word
for agility.

## 1,294

The Camel is credited with more aerial victories than any other Allied aircraft of the
war. It arrived in mid-1917, when the Albatros fighters had held the advantage for a
year, and took it back.

## What the pilot did not have

No parachute: the RFC refused to issue them to pilots, on the argument that a man with a
parachute might abandon a repairable aeroplane. No armour, no radio, no oxygen worth the
name. Two guns directly in front of his face, fed by belts he cleared himself with a
mallet when they jammed, which was often. And a fuel tank between his knees and the engine.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "1"),
        Spec("Empty weight", "422 kg"),
        Spec("Loaded weight", "659 kg"),
        Spec("Wingspan", "8.53 m"),
        Spec("Engine", "Clerget 9B rotary, 130 hp"),
        Spec("Top speed", "185 km/h"),
        Spec("Service ceiling", "5,790 m"),
        Spec("Endurance", "2.5 hours"),
        Spec("Armament", "2 × .303 in Vickers"),
        Spec("Victories credited", "1,294"),
        Spec("Built", "5,490"),
    )

    private val quiz = listOf(
        Question(
            "Why did the Camel turn right so much faster than left?",
            listOf(
                "The rudder was offset",
                "Gyroscopic precession from its spinning rotary engine",
                "The guns were mounted to the right of the centreline",
                "Asymmetric wing rigging",
            ),
            1,
            "The whole engine spins with the propeller - 25 kg at 1,250 rpm is a gyroscope, "
                + "and it pushes at ninety degrees to any force applied. Pilots learned to "
                + "turn three-quarters right rather than a quarter left.",
        ),
        Question(
            "Why is all the Camel's weight packed into the first two metres?",
            listOf(
                "To balance the long tail",
                "A small moment of inertia makes the aircraft change direction quickly",
                "To keep the centre of gravity ahead of the wing",
                "It was the only place the structure was strong enough",
            ),
            1,
            "Engine, guns, ammunition, fuel and pilot are all concentrated at the nose. "
                + "Concentrated mass means low inertia, and low inertia means agility - at the "
                + "cost of being unstable in every axis.",
        ),
        Question(
            "What did a rotary engine do to its pilot?",
            listOf(
                "Deafened him with unmuffled exhaust",
                "Sprayed him with the castor oil it used as total-loss lubricant",
                "Vibrated so badly it loosened the airframe",
                "Filled the cockpit with exhaust fumes",
            ),
            1,
            "A rotary throws its lubricant straight out of the cylinders, so the pilot flew "
                + "through a fine mist of castor oil - a powerful laxative - for two hours at "
                + "a time.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
