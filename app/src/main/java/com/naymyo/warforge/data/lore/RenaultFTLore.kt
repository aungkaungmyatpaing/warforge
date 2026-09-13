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
 * Renault FT, opened up.
 *
 * The first tank whose insides are arranged the way every tank since has been: engine at
 * the back behind a firewall, crew forward, gun in a turret that turns. Hull floor near
 * y = 476, turret roof near y = 238.
 */
object RenaultFTLore {

    private val modules = listOf(
        module(
            "engine", "Renault 4-cylinder", ModuleKind.ENGINE,
            detail = "4.5 L petrol · 39 hp",
            info = "A lorry engine at the rear, behind an armoured firewall - the first time "
                + "a tank's crew had been separated from its engine. After the Mark IV's "
                + "open engine bay this was not a refinement, it was the difference between "
                + "a crew that could work and one that could not.",
        ) {
            box3(Role.BODY, 0, 310f, 350f, -56f, 420f, 452f, 56f)
            cylinderZ(Role.DARK, -2, 300f, 336f, 14f, -18f, 18f, 12)
        },
        module(
            "firewall", "Engine bulkhead", ModuleKind.ARMOUR,
            detail = "Steel firewall",
            info = "The plate that made the layout work. Noise, heat and fumes stay behind "
                + "it; the crew compartment in front is habitable. Every tank in this game "
                + "built after 1918 has one.",
        ) { box3(Role.BODY, 0, 428f, 340f, -58f, 438f, 470f, 58f) },
        module(
            "transmission", "Gearbox and clutch", ModuleKind.TRANSMISSION,
            detail = "Four speeds",
            info = "Between the engine and the rear sprockets. Drive at the back, idler at "
                + "the front - the reverse of the Panzer IV twenty-five years later, and the "
                + "same arrangement as the T-34.",
        ) { box3(Role.BODY, 0, 300f, 420f, -46f, 380f, 470f, 46f) },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front, low in the hull",
            info = "Sits almost on the floor with his head at the level of the turret ring, "
                + "steering with two levers. On a two-man tank he is also the mechanic, the "
                + "loader's assistant and, when the commander is busy fighting, the only man "
                + "watching where they are going.",
        ) { crewman(576f, 430f, 0f) },
        module(
            "commander", "Commander and gunner", ModuleKind.CREW,
            detail = "In the turret, standing",
            info = "Aims, loads and fires the gun, commands the tank, and navigates. Every "
                + "problem of the one-man turret starts here, and armies spent the next "
                + "thirty years slowly admitting it.",
        ) { crewman(470f, 340f, 0f) },
        module(
            "gun", "37 mm Puteaux SA-18", ModuleKind.GUN,
            detail = "37 mm, hand-cranked traverse",
            info = "Short and slow, but in a turret that could point anywhere. A machine-gun "
                + "version was built in equal numbers; a platoon usually mixed them.",
        ) {
            box3(Role.BODY, 0, 480f, 276f, -22f, 546f, 310f, 22f)
            cylinderZ(Role.DARK, 1, 546f, 292f, 12f, -14f, 14f, 12)
        },
        module(
            "ammo", "37 mm rounds", ModuleKind.AMMO,
            detail = "237 rounds",
            info = "Stowed round the fighting compartment within the commander's reach, "
                + "because there is nobody else to pass him one.",
        ) { shellRack(470f, 466f, 0f, 7, r = 7f, len = 44f, spacing = 18f, halfZ = 30f) },
        module(
            "fuel", "Fuel tank", ModuleKind.FUEL,
            detail = "95 L",
            info = "Behind the firewall with the engine, where a fire stays away from the "
                + "crew. Good for about 60 km, which was as far as anyone expected a tank to "
                + "go before it broke.",
        ) { fuelCell(430f, 350f, -50f, 490f, 404f, 50f) },
        module(
            "armour", "Riveted plate", ModuleKind.ARMOUR,
            detail = "22 mm front, 16 mm side",
            info = "Thicker than a Mark IV's despite weighing a quarter as much - the payoff "
                + "for a small tank. Enough against rifle and machine-gun fire, which is what "
                + "it would meet.",
        ) {
            box3(Role.BODY, 0, 656f, 360f, -58f, 676f, 460f, 58f)
        },
        module(
            "turret_drive", "Shoulder traverse", ModuleKind.POWER,
            detail = "The commander's shoulder",
            info = "There is no mechanism. The commander leans against the turret wall and "
                + "pushes it round with his body, then holds it there while he aims and "
                + "fires - which is also why he is standing rather than sitting.",
        ) {
            cylinderZ(Role.BODY, 0, 474f, 352f, 34f, -30f, 30f, 12)
            box3(Role.TRIM, 1, 450f, 340f, -22f, 474f, 364f, 22f)
        },
    )

    private val history = """
## The tank everyone else copied

The British invented the tank. The French invented the tank everyone still builds.

Louis Renault's design of 1917 weighed 6.7 tonnes against the Mark IV's 28, carried two
men instead of eight, and cost a fraction as much. More importantly, it put the engine at
the back behind a firewall, the crew in front of it, and the gun in a turret that turned
through 360 degrees. That arrangement - engine aft, crew forward, turret on top - is the
layout of almost every tank built since.

## Numbers over size

The French did not build a few hundred FTs. They built over 3,000, and planned for 12,000.
The doctrine that went with them was equally modern: not a handful of land battleships,
but swarms of small cheap tanks working directly with the infantry, in numbers large
enough that losses did not matter.

The Americans built their own as the M1917 and used it to found their tank corps - one
of whose junior officers was a young George Patton.

## The one-man turret

The FT's flaw is the one the Soviets were still paying for in 1941: one man in the turret
doing three jobs. The commander aims, loads, fires, commands and navigates. He cannot do
any of them well while doing the others, and he certainly cannot watch the battlefield.

It took until the T-34-85 and the three-man turret for that argument to be settled for
good, and the FT is where it starts.

## Afterwards

FTs were still in French service in 1940 and were captured in numbers. Others fought in
Spain, in China, in Poland. A few were still on strength somewhere into the 1950s. For a
design finished in 1917 that is an extraordinary run - and the reason is simply that
Renault got the arrangement right the first time.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "2"),
        Spec("Combat weight", "6.7 t"),
        Spec("Length (with tail)", "5.00 m"),
        Spec("Height", "2.14 m"),
        Spec("Engine", "Renault 4-cyl, 39 hp"),
        Spec("Power to weight", "5.8 hp/t"),
        Spec("Road speed", "7 km/h"),
        Spec("Range", "65 km"),
        Spec("Main gun", "37 mm SA-18 (or 8 mm MG)"),
        Spec("Ammunition", "237 rounds"),
        Spec("Armour", "22 mm front, 16 mm side"),
        Spec("Built", "3,694"),
    )

    private val quiz = listOf(
        Question(
            "What layout did the Renault FT establish that tanks still use?",
            listOf(
                "Tracks running right around the hull",
                "Engine at the rear behind a bulkhead, crew forward, turret on top",
                "Guns in side sponsons",
                "A one-man turret",
            ),
            1,
            "Engine aft, crew forward, fully rotating turret. Almost every tank built since "
                + "1918 is a variation on this arrangement.",
        ),
        Question(
            "Why did the firewall matter so much?",
            listOf(
                "It stopped fuel fires reaching the crew",
                "It separated the crew from the engine's noise, heat and exhaust",
                "It added structural strength to the hull",
                "It shielded the ammunition from the engine",
            ),
            1,
            "A Mark IV's crew worked in the open beside a running engine at about 50 °C, and "
                + "were regularly carried out unconscious. The FT's bulkhead made a tank "
                + "somewhere a crew could actually function.",
        ),
        Question(
            "What is the enduring weakness of the FT's turret?",
            listOf(
                "It could not turn fully round",
                "One man had to command, aim, load and fire all at once",
                "It was too small for a useful gun",
                "It had no hatch",
            ),
            1,
            "The same flaw the T-34-76 still had in 1941. A commander looking through a gun "
                + "sight is not looking at the battlefield, and the side that sees first "
                + "usually wins.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
