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

/** MiG-15, opened up. Centreline near y = 298. */
object Mig15Lore {

    private val modules = listOf(
        module(
            "engine", "Klimov VK-1", ModuleKind.ENGINE,
            detail = "Centrifugal turbojet · 26.5 kN",
            info = "A Rolls-Royce Nene, copied. Britain sold the Soviet Union twenty-five of "
                + "its most advanced jet engines in 1946 on the understanding they were for "
                + "civil research; the Soviets reverse-engineered them, enlarged the design, "
                + "and put it in a fighter that shot down British and American aircraft four "
                + "years later.",
        ) {
            revolveX(Role.BODY, 0, 16, 300f, 300f, 44f, 520f, 300f, 48f)
            cylinderZ(Role.TRIM, 2, 330f, 300f, 46f, -46f, 46f, 16)
        },
        module(
            "intake_duct", "Intake trunking", ModuleKind.AVIONICS,
            detail = "Split round the cockpit",
            info = "Air enters the nose and splits into two ducts that pass either side of "
                + "the pilot before rejoining ahead of the compressor. A nose intake is "
                + "simple and efficient - and it uses up the one place a radar would have to "
                + "go, which is why the MiG-15 never had one.",
        ) {
            box3(Role.BODY, 0, 560f, 268f, -56f, 800f, 300f, -26f)
            box3(Role.BODY, 0, 560f, 268f, 26f, 800f, 300f, 56f)
        },
        module(
            "pilot", "Pilot", ModuleKind.CREW,
            detail = "One, on an ejection seat",
            info = "The MiG-15 had an ejection seat from the start. At 900 km/h a pilot "
                + "cannot climb out against the airflow, and the jet age made the seat a "
                + "necessity rather than a refinement.",
        ) { crewman(700f, 300f, 0f) },
        module(
            "cannon", "37 mm and 23 mm cannon", ModuleKind.GUN,
            detail = "1 × N-37, 2 × NR-23",
            info = "An anti-bomber battery. One 37 mm shell will take a wing off a B-29, "
                + "which is exactly what the aeroplane was designed to do. Against fighters "
                + "it is poor: the two calibres have different trajectories, so a pilot "
                + "aiming with one is missing with the other.",
        ) {
            box3(Role.BODY, 0, 740f, 336f, -30f, 860f, 356f, -8f)
            box3(Role.BODY, 0, 740f, 336f, 8f, 860f, 356f, 30f)
        },
        module(
            "gun_pack", "Gun pack", ModuleKind.AMMO,
            detail = "Lowered on a winch for reloading",
            info = "All three guns and their ammunition sit on a single tray that winches "
                + "down out of the nose. Rearming takes minutes rather than an hour - a "
                + "maintenance idea years ahead of its opponents.",
        ) { box3(Role.BODY, 0, 730f, 330f, -34f, 830f, 366f, 34f) },
        module(
            "fuel", "Fuselage fuel", ModuleKind.FUEL,
            detail = "1,400 L internal",
            info = "Wrapped round the engine duct. The MiG-15's endurance is poor - about an "
                + "hour - but it was designed to defend its own airspace, where that is "
                + "enough.",
        ) { fuelCell(520f, 262f, -52f, 700f, 296f, 52f) },
        module(
            "airbrakes", "Air brakes", ModuleKind.AVIONICS,
            detail = "Rear fuselage, hydraulic",
            info = "A jet with no propeller has almost no drag when the throttle closes, so "
                + "it will not slow down. Air brakes are what let a jet fighter stop "
                + "overshooting its target.",
        ) {
            box3(Role.BODY, 0, 320f, 266f, -50f, 400f, 286f, 50f)
        },
        module(
            "armour", "Pilot armour", ModuleKind.ARMOUR,
            detail = "Armoured seat back and screen",
            info = "Plate behind the seat and a bulletproof windscreen. Soviet design put "
                + "protection where it counted and accepted the weight; the aeroplane was "
                + "built to trade fire with bombers that shot back.",
        ) { box3(Role.BODY, 0, 650f, 258f, -26f, 660f, 320f, 26f) },
        module(
            "oxygen", "Oxygen system", ModuleKind.LIFE,
            detail = "Two bottles · pressure demand",
            info = "The MiG fights at 15,000 metres, where an unpressurised man is "
                + "unconscious in fifteen seconds. The cockpit is pressurised as well, "
                + "but only to the equivalent of 7,000 metres - the oxygen mask is not "
                + "optional.",
        ) {
            cylinderZ(Role.BODY, 0, 430f, 320f, 14f, -30f, 30f, 10)
            cylinderZ(Role.BODY, 0, 460f, 320f, 14f, -30f, 30f, 10)
        },
        module(
            "ejection", "Ejection seat", ModuleKind.ESCAPE,
            detail = "Cartridge-fired · canopy jettison first",
            info = "One of the first fitted to a production fighter. It throws the seat "
                + "clear on a gun cartridge and the pilot then has to get out of it "
                + "himself, which at 900 km/h is a great deal to ask.",
        ) {
            box3(Role.BODY, 0, 496f, 286f, -22f, 540f, 340f, 22f)
            box3(Role.TRIM, 2, 490f, 276f, -18f, 520f, 292f, 18f)
        },
    )

    private val history = """
## The engine Britain gave away

In 1946 the British government approved the sale of twenty-five Rolls-Royce Nene engines
to the Soviet Union, on the understanding that they were for civil research. The Nene was
then the most advanced centrifugal turbojet in the world. The Soviets reverse-engineered
it as the RD-45, enlarged it as the Klimov VK-1, and put it into a swept-wing fighter.

Four years later that fighter was shooting down British and American aircraft over Korea.
Stalin is supposed to have asked what fool would sell his secrets. The story is probably
apocryphal; the engines were real.

## The swept wing

German wartime research on swept wings was captured by both sides in 1945 and acted on by
both. Sweeping the wing back delays the drag rise as an aircraft approaches the speed of
sound - the difference between a jet that runs out of thrust at 0.8 Mach and one that does
not.

The MiG-15 appeared over Korea in November 1950 and made every straight-wing jet in the
American inventory obsolete overnight. The F-80s and F-84s could not live with it. The
answer was the F-86 Sabre, which had its own swept wing from the same German research.

## What it was built to kill

Not fighters - bombers. The armament is one 37 mm and two 23 mm cannon, a battery chosen
to take the wing off a B-29 in one pass. Against a manoeuvring fighter it is poor: the two
calibres have noticeably different trajectories, so a pilot who aims with one misses with
the other, and the rate of fire is low.

The Sabre carried six .50 calibre machine guns - much lighter, far faster-firing, and far
better at hitting a jinking fighter. Each aeroplane was armed for the job its designers
expected.

## Inside

Open the fuselage and the layout is dictated by the nose intake: air enters at the front
and splits into two ducts that run either side of the pilot before rejoining at the
compressor. It is simple and efficient, and it uses up the only place a radar antenna
could sensibly go. The MiG-15 never carried one, and neither did the Sabre until later
marks.

The guns and their ammunition sit on a single tray that winches down out of the nose for
rearming - a piece of maintenance thinking years ahead of anything its opponents had.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "1"),
        Spec("Empty weight", "3,630 kg"),
        Spec("Loaded weight", "5,044 kg"),
        Spec("Wingspan", "10.08 m"),
        Spec("Engine", "Klimov VK-1, 26.5 kN"),
        Spec("Top speed", "1,075 km/h"),
        Spec("Service ceiling", "15,500 m"),
        Spec("Range", "1,200 km"),
        Spec("Armament", "1 × 37 mm, 2 × 23 mm"),
        Spec("Built", "~18,000 (all variants)"),
    )

    private val quiz = listOf(
        Question(
            "Where did the MiG-15's engine design come from?",
            listOf(
                "Captured German jet engines",
                "Rolls-Royce Nenes sold to the Soviet Union in 1946",
                "An indigenous Soviet design",
                "American engines supplied under lend-lease",
            ),
            1,
            "Britain sold twenty-five Nenes for 'civil research'. They were reverse-engineered "
                + "as the RD-45, enlarged into the Klimov VK-1, and were shooting down British "
                + "aircraft over Korea four years later.",
        ),
        Question(
            "What does sweeping a wing back achieve?",
            listOf(
                "More lift at low speed",
                "It delays the drag rise as the aircraft approaches the speed of sound",
                "Better roll rate",
                "A shorter takeoff run",
            ),
            1,
            "German wartime research, captured by both sides in 1945. Swept wings are why the "
                + "MiG-15 made every straight-wing jet in the American inventory obsolete "
                + "overnight.",
        ),
        Question(
            "Why was the MiG-15's cannon armament poor against fighters?",
            listOf(
                "It carried too little ammunition",
                "The 37 mm and 23 mm have different trajectories, so aiming with one misses with the other",
                "The guns could not be fired together",
                "They jammed at high altitude",
            ),
            1,
            "It was a bomber-killing battery - one 37 mm shell takes a wing off a B-29. The "
                + "Sabre's six fast-firing .50s were far better suited to hitting a jinking "
                + "fighter.",
        ),
        Question(
            "Why does a nose-intake jet have no radar?",
            listOf(
                "Radar was too heavy for a light fighter",
                "The intake occupies the only place an antenna could go",
                "Soviet radar was not small enough",
                "Radar interfered with the engine",
            ),
            1,
            "Air enters the nose and splits round the cockpit. It is simple and efficient, "
                + "and it uses up the nose. Later fighters moved the intakes to the sides or "
                + "under the fuselage precisely to free it.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
