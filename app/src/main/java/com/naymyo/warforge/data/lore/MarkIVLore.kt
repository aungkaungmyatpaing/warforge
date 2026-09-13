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

/**
 * Mark IV, opened up.
 *
 * The layout that makes every later tank look civilised: no bulkhead anywhere, so the
 * engine runs unsilenced in the middle of the crew, and eight men work around it in
 * heat, noise and exhaust. Hull floor near y = 500, roof near y = 280, nose at high x.
 */
object MarkIVLore {

    private val modules = listOf(
        module(
            "engine", "Daimler-Foster 6-cylinder", ModuleKind.ENGINE,
            detail = "16 L petrol · 105 hp",
            info = "Sits uncovered in the middle of the fighting compartment. No bulkhead, "
                + "no silencer, no exhaust extraction: the crew worked beside a running "
                + "engine in about 50 °C, breathing its fumes and their own gun gases. Men "
                + "were regularly carried out unconscious after an action.",
        ) {
            box3(Role.BODY, 0, 386f, 336f, -66f, 566f, 452f, 66f)
            box3(Role.TRIM, 2, 400f, 320f, -30f, 470f, 336f, 30f)
            cylinderZ(Role.DARK, -2, 300f, 300f, 17f, -20f, 20f, 14)
        },
        module(
            "radiator", "Radiator and fan", ModuleKind.RADIATOR,
            detail = "Water-cooled, belt-driven fan",
            info = "Behind the engine, drawing air the length of the hull. The tank had no "
                + "ventilation of its own worth the name - the radiator fan was most of it.",
        ) { box3(Role.BODY, 0, 330f, 336f, -50f, 386f, 440f, 50f) },
        module(
            "transmission", "Primary gearbox", ModuleKind.TRANSMISSION,
            detail = "Two speeds, plus secondary gears each side",
            info = "Changing gear needed the driver and both gearsmen acting together on "
                + "hand signals - nobody could hear anything. A turn meant stopping one "
                + "track's secondary gearbox while the other pulled the tank round.",
        ) { box3(Role.BODY, 0, 250f, 396f, -56f, 356f, 470f, 56f) },
        module(
            "commander", "Commander", ModuleKind.CREW,
            detail = "Front right, beside the driver",
            info = "Worked the brakes while the driver worked the gears. Command and "
                + "driving were one job split between two men, with a third and fourth at "
                + "the secondary gearboxes behind them.",
        ) { crewman(690f, 356f, 44f) },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front left",
            info = "Steered with the primary gearbox and the tank's own weight. A Mark IV "
                + "took four men to drive it in anything but a straight line.",
        ) { crewman(690f, 356f, -44f) },
        module(
            "gearsman_l", "Left gearsman", ModuleKind.CREW,
            detail = "Rear left, on the secondary gearbox",
            info = "One of two men whose entire job was to change the gear on their side of "
                + "the tank when the driver signalled - by banging on the engine block with "
                + "a spanner, because speech was impossible.",
        ) { crewman(330f, 420f, -66f, facing = -1f) },
        module(
            "gearsman_r", "Right gearsman", ModuleKind.CREW,
            detail = "Rear right, on the secondary gearbox",
            info = "The other half of the steering crew, working blind on signals from the "
                + "front of the tank. Neither gearsman can see out; both spend the action "
                + "watching for a hand signal they cannot hear called.",
        ) { crewman(330f, 420f, 66f, facing = -1f) },
        module(
            "gunner_l", "Port sponson gunner", ModuleKind.CREW,
            detail = "Left sponson",
            info = "A 'Male' Mark IV carried a 6-pounder in each sponson; a 'Female' "
                + "carried machine guns instead, to sweep infantry off the tank. Most "
                + "companies mixed the two.",
        ) { crewman(520f, 376f, -108f) },
        module(
            "gunner_r", "Starboard sponson gunner", ModuleKind.CREW,
            detail = "Right sponson",
            info = "Fires the other 6-pounder. Loading it while the tank pitched over "
                + "shell holes was a two-man job in a space neither man could stand up in.",
        ) { crewman(520f, 376f, 108f) },
        module(
            "gun_breech", "6-pounder breech", ModuleKind.GUN,
            detail = "57 mm QF, one per sponson",
            info = "A naval gun on a naval mounting, chosen because it already existed. "
                + "Effective against strongpoints and, later, against the first German "
                + "tanks.",
        ) {
            box3(Role.BODY, 0, 560f, 340f, 88f, 640f, 384f, 124f)
            box3(Role.BODY, 0, 560f, 340f, -124f, 640f, 384f, -88f)
        },
        module(
            "ammo", "6-pdr ammunition", ModuleKind.AMMO,
            detail = "332 rounds",
            info = "Racked along the hull sides at the crew's elbows - there was nowhere "
                + "else for it in a hull with an engine down the middle.",
        ) {
            shellRack(600f, 460f, 84f, 6, r = 9f, len = 58f, spacing = 22f, halfZ = 14f)
            shellRack(600f, 460f, -84f, 6, r = 9f, len = 58f, spacing = 22f, halfZ = 14f)
        },
        module(
            "fuel", "Fuel tanks", ModuleKind.FUEL,
            detail = "320 L, gravity fed",
            info = "Carried high and forward on the early marks, where a hit would spill "
                + "petrol onto the crew. The Mark IV moved them outside the hull at the "
                + "rear - one of the changes that made it the first genuinely usable tank.",
        ) { fuelCell(200f, 350f, -60f, 262f, 420f, 60f) },
        module(
            "armour", "Riveted plate", ModuleKind.ARMOUR,
            detail = "12 mm front, 8 mm side",
            info = "Enough to stop a rifle bullet, which was the whole requirement. Against "
                + "the German K round - a hardened core fired from an ordinary rifle - it "
                + "was not, and the rivets themselves became shrapnel when struck.",
        ) {
            box3(Role.BODY, 0, 830f, 300f, -108f, 870f, 400f, 108f)
        },
        module(
            "ventilation", "No ventilation", ModuleKind.LIFE,
            detail = "None fitted",
            info = "The engine runs unsilenced and unenclosed in the same box as the crew, "
                + "and the exhaust leaks. Interior temperatures reached 50 C and carbon "
                + "monoxide did the rest: crews were routinely carried out unconscious "
                + "after actions in which nobody had shot at them.",
        ) {
            box3(Role.BODY, 0, 420f, 268f, -70f, 560f, 286f, 70f)
            box3(Role.TRIM, 2, 470f, 260f, -30f, 510f, 268f, 30f)
        },
    )

    private val history = """
## An answer to the machine gun

By 1915 the Western Front had settled into a problem with no obvious solution: barbed
wire, machine guns and artillery could stop any number of infantry, and the ground behind
the front was churned to a state no wheel could cross. The tank was the answer - an
armoured box on tracks that could cross a trench, flatten wire, and absorb rifle fire.

The rhomboid shape everyone recognises comes straight from that job. Running the track
all the way round a very long, very tall frame let the tank climb out of a shell hole and
span a 3 m trench. It was not designed to look like that; it was the shape the problem
demanded.

## Cambrai

The Mark I of 1916 arrived in dribs and drabs and achieved little. The Mark IV of 1917
arrived in hundreds. At Cambrai on 20 November 1917, 476 of them went forward on firm
ground behind a surprise bombardment and tore a six-mile hole in the Hindenburg Line in a
morning - an advance that had previously cost months and hundreds of thousands of
casualties.

The breakthrough was not exploited and most of the ground was lost within ten days. But
the demonstration was made, and every army in the world took note.

## Inside

Open the hull and the thing that stops you is that there is nothing between the crew and
the engine. A 105 hp Daimler runs uncovered in the middle of the compartment: no
bulkhead, no silencer, no exhaust extraction. Eight men worked around it at around 50 °C,
breathing exhaust and cordite, unable to hear each other speak.

Steering took four of them. The driver worked the primary gearbox and the brakes, the
commander helped, and two gearsmen sat at secondary gearboxes - one per track - changing
gear on hand signals or on a spanner banged against the engine block.

## The rivets

Twelve millimetres of riveted plate stopped a rifle bullet, which was all that was asked
of it. Germany's answer was the K round, a hardened core fired from an ordinary rifle, and
against that the Mark IV was not proof. Worse, a strike that did not penetrate could still
shear the heads off rivets inside the hull and send them round the compartment - which is
why welding replaced riveting as soon as anyone could manage it.

## What it started

The Mark IV is the reason every later vehicle in this game exists. It also set the
precedent that a tank's shape follows the obstacle it is built to cross - and when the
obstacle changed from a trench to another tank, the shape changed with it.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "8"),
        Spec("Combat weight", "28.4 t (Male)"),
        Spec("Length", "8.05 m"),
        Spec("Height", "2.49 m"),
        Spec("Engine", "Daimler-Foster, 105 hp"),
        Spec("Power to weight", "3.7 hp/t"),
        Spec("Road speed", "6 km/h"),
        Spec("Range", "56 km"),
        Spec("Trench crossing", "3.0 m"),
        Spec("Armament (Male)", "2 × 6-pdr, 3 × Lewis"),
        Spec("Armour", "12 mm front, 8 mm side"),
        Spec("Built", "1,220"),
    )

    private val quiz = listOf(
        Question(
            "Why is a Mark IV shaped like a rhomboid?",
            listOf(
                "To deflect shellfire",
                "To let the track climb out of shell holes and span a wide trench",
                "To fit the engine in the middle",
                "To give the sponson guns a clear arc",
            ),
            1,
            "The shape is the trench-crossing requirement made solid: a long, tall track run "
                + "spans a 3 m gap and climbs a near-vertical face. When tanks stopped being "
                + "built to cross trenches, the shape went away.",
        ),
        Question(
            "How many men did it take to steer a Mark IV?",
            listOf("One", "Two", "Four", "Eight"),
            2,
            "Driver, commander and two gearsmen. The driver and commander worked the primary "
                + "gearbox and brakes; the gearsmen changed the secondary gearbox on their own "
                + "side of the tank when signalled - usually by a spanner banged on the engine, "
                + "since nobody could hear anything.",
        ),
        Question(
            "What made the inside of a Mark IV so dangerous to its own crew?",
            listOf(
                "The ammunition was stored above head height",
                "An uncovered engine with no bulkhead, silencer or exhaust extraction",
                "The fuel was carried inside the crew compartment",
                "The armour was too thin to stop machine-gun fire",
            ),
            1,
            "Around 50 °C, deafening noise, and a compartment full of exhaust and gun gases. "
                + "Crews were regularly carried out unconscious after an action.",
        ),
        Question(
            "Why did armies stop riveting tank hulls?",
            listOf(
                "Rivets were slower to manufacture than welds",
                "A non-penetrating hit could still shear rivet heads off inside the hull",
                "Riveted plate could not be sloped",
                "Rivets rusted in the field",
            ),
            1,
            "The rivet heads became shrapnel inside the crew compartment even when the plate "
                + "held. Welding removed that failure mode entirely.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
