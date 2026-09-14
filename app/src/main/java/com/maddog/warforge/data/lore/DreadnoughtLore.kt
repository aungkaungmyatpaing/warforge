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

/** HMS Dreadnought, opened up. Waterline near y = 432, main deck near y = 344. */
object DreadnoughtLore {

    private val modules = listOf(
        module(
            "turbines", "Parsons steam turbines", ModuleKind.ENGINE,
            detail = "4 shafts · 23,000 shp",
            info = "The first capital ship in the world driven by turbines. A reciprocating "
                + "engine shakes itself apart above about 18 knots and needs constant "
                + "attention; a turbine spins smoothly and can hold full power for days. "
                + "Dreadnought could cruise at a speed her predecessors could only sprint at.",
        ) {
            box3(Role.BODY, 0, 300f, 380f, -70f, 520f, 440f, 70f)
            cylinderZ(Role.TRIM, 2, 340f, 410f, 26f, -66f, 66f, 14)
            cylinderZ(Role.TRIM, 2, 440f, 410f, 26f, -66f, 66f, 14)
        },
        module(
            "boilers", "Babcock & Wilcox boilers", ModuleKind.ENGINE,
            detail = "18 coal-fired boilers",
            info = "Eighteen boilers burning coal, shovelled by hand. Coaling ship took the "
                + "whole crew a full day and left everything and everyone black. Oil firing, "
                + "which needed no stokers at all, arrived a few years later and changed "
                + "navies as much as the turbine did.",
        ) {
            box3(Role.BODY, 0, 520f, 372f, -76f, 700f, 444f, 76f)
        },
        module(
            "turret_a", "Forward 12-inch turret", ModuleKind.GUN,
            detail = "2 × 12 in, 850 kg shells",
            info = "Ten 12-inch guns in five turrets, and nothing smaller that mattered. "
                + "'All-big-gun' is the whole idea of the ship: at the ranges gunnery had "
                + "reached, you can only correct your fire if every splash comes from the "
                + "same calibre.",
        ) {
            box3(Role.BODY, 0, 720f, 306f, -60f, 830f, 350f, 60f)
            cylinderZ(Role.BODY, 0, 772f, 350f, 48f, -48f, 48f, 16)
        },
        module(
            "magazine", "Main magazines", ModuleKind.MAGAZINE,
            detail = "Below the waterline, flash-tight",
            info = "Cordite and shells are stowed below the waterline under armour, and "
                + "raised to the guns through flash-tight doors. The doors are the point: at "
                + "Jutland three British battlecruisers blew up because their crews had "
                + "propped them open to load faster.",
        ) {
            shellRack(730f, 466f, 0f, 6, r = 12f, len = 74f, spacing = 28f, halfZ = 40f)
            shellRack(200f, 466f, 0f, 6, r = 12f, len = 74f, spacing = 28f, halfZ = 40f)
        },
        module(
            "coal", "Coal bunkers", ModuleKind.FUEL,
            detail = "2,900 tonnes",
            info = "Stacked along the ship's sides, where they double as protection: coal is "
                + "a surprisingly effective absorber of shell fragments, and every bunker "
                + "emptied made the ship a little more vulnerable as well as a little lighter.",
        ) {
            fuelCell(300f, 380f, 76f, 700f, 446f, 96f)
            fuelCell(300f, 380f, -96f, 700f, 446f, -76f)
        },
        module(
            "bridge", "Bridge and conning tower", ModuleKind.CREW,
            detail = "Captain and navigating party",
            info = "The conning tower is the most heavily armoured part of the ship after "
                + "the turrets - 280 mm of plate round a handful of men, because losing the "
                + "command team loses the ship.",
        ) {
            crewman(640f, 300f, -20f)
            crewman(640f, 300f, 20f)
            box3(Role.BODY, 0, 600f, 286f, -34f, 690f, 340f, 34f)
        },
        module(
            "fire_control", "Fire control", ModuleKind.OPTICS,
            detail = "Spotting top and range clocks",
            info = "A spotter high on the mast measures the fall of shot and passes "
                + "corrections down to the turrets. Centralised director firing - one man "
                + "aiming every gun - was the innovation that made long-range gunnery work, "
                + "and it arrived just after this ship.",
        ) { box3(Role.BODY, 0, 578f, 168f, -24f, 632f, 200f, 24f) },
        module(
            "armour_belt", "Main belt", ModuleKind.ARMOUR,
            detail = "279 mm amidships",
            info = "A band of Krupp cemented armour along the waterline over the machinery "
                + "and magazines, tapering to nothing at the ends. Armour only the parts "
                + "whose loss sinks you - the 'all or nothing' principle every later "
                + "battleship followed.",
        ) {
            box3(Role.BODY, 0, 280f, 400f, 92f, 740f, 452f, 104f)
            box3(Role.BODY, 0, 280f, 400f, -104f, 740f, 452f, -92f)
        },
        module(
            "torpedo", "Torpedo tubes", ModuleKind.MAGAZINE,
            detail = "5 × 18 in, submerged",
            info = "Battleships carried torpedo tubes into the 1940s despite never usefully "
                + "firing one. They were a flooding risk and took up armoured volume - a "
                + "piece of doctrine that outlived its justification by forty years.",
        ) {
            cylinderZ(Role.BODY, 0, 860f, 420f, 16f, 60f, 100f, 12)
            cylinderZ(Role.BODY, 0, 860f, 420f, 16f, -100f, -60f, 12)
        },
        module(
            "steering", "Steering gear", ModuleKind.POWER,
            detail = "Twin rudders · steam steering engine",
            info = "A steam engine right aft turns the rudder stock; the wheel on the "
                + "bridge only works a telemotor that tells it what to do. It is the one "
                + "piece of machinery that can lose a ship on its own - Bismarck was sunk "
                + "because a torpedo jammed hers.",
        ) {
            box3(Role.BODY, 0, 120f, 380f, -40f, 200f, 430f, 40f)
            cylinderZ(Role.TRIM, 1, 150f, 378f, 18f, -30f, 30f, 12)
        },
    )

    private val history = """
## A ship that made everything else obsolete

HMS Dreadnought was laid down in October 1905 and commissioned in December 1906 - fourteen
months, an extraordinary pace even now. The day she entered service every other battleship
in the world, including Britain's own, became second-class. The word "dreadnought" stopped
being a name and became a category.

## All big guns

Before her, battleships carried a few large guns and a mixed battery of medium ones. That
made sense at 3,000 m. It stopped making sense as gunnery ranges pushed past 10,000 m,
because correcting fire means watching where your shells land - and if splashes are coming
from three different calibres you cannot tell which are yours.

Dreadnought carried ten 12-inch guns and nothing else that mattered. Every splash was the
same, so every salvo could be corrected. Fisher's insight was about spotting, not about
weight of fire.

## Turbines

The second innovation was quieter and mattered as much. Parsons steam turbines replaced
reciprocating engines, which shake themselves apart above about 18 knots and need constant
nursing. Dreadnought could hold 21 knots indefinitely - she could cruise at a speed her
predecessors could only sprint at, and keep it up for days.

## The paradox

Dreadnought also halved the Royal Navy's advantage overnight. Britain had a commanding
lead in pre-dreadnought battleships, and by making them obsolete she reset the count to
near zero - handing Germany a chance to compete that it would never have had otherwise.
The naval race that followed is one of the roads into 1914.

## Inside

Open the hull and the layout is dictated by two things: the guns and the coal. Magazines
sit below the waterline under armour, feeding the turrets through flash-tight doors -
doors that, at Jutland ten years later, three British battlecruiser crews propped open to
load faster, and were destroyed for it.

Coal is stacked along the sides where it doubles as splinter protection, and had to be
shovelled aboard by the entire crew over a full filthy day. Oil firing, which arrived a
few years later, ended that and changed navies as profoundly as the turbine had.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "~700"),
        Spec("Displacement", "18,120 t"),
        Spec("Length", "160.6 m"),
        Spec("Beam", "25.0 m"),
        Spec("Machinery", "Parsons turbines, 23,000 shp"),
        Spec("Speed", "21 knots"),
        Spec("Range", "6,620 nmi at 10 knots"),
        Spec("Main armament", "10 × 12 in"),
        Spec("Armour belt", "279 mm"),
        Spec("Commissioned", "1906"),
    )

    private val quiz = listOf(
        Question(
            "Why did an all-big-gun battleship matter?",
            listOf(
                "More guns meant a heavier broadside",
                "At long range you can only correct fire if every splash is the same calibre",
                "Large guns had longer range than medium ones",
                "Mixed batteries needed too many crew",
            ),
            1,
            "Spotting, not weight of fire. With three calibres falling together a spotter "
                + "cannot tell which splashes are his, and at 10,000 m correcting fire is the "
                + "whole business of gunnery.",
        ),
        Question(
            "What did steam turbines give Dreadnought?",
            listOf(
                "A higher top speed than any other ship",
                "The ability to hold high speed indefinitely, rather than sprint",
                "Lower fuel consumption",
                "The ability to burn oil instead of coal",
            ),
            1,
            "Reciprocating engines shake themselves apart above about 18 knots. A turbine "
                + "runs smoothly for days, so 21 knots became a cruising speed rather than a "
                + "dash.",
        ),
        Question(
            "How did Dreadnought weaken Britain's position?",
            listOf(
                "She cost so much that other ships were cancelled",
                "By making every existing battleship obsolete she reset the naval count to near zero",
                "Her design was copied by Germany before she was launched",
                "She proved too expensive to build in numbers",
            ),
            1,
            "Britain's overwhelming lead was in pre-dreadnoughts. Making them obsolete handed "
                + "Germany a chance to compete from a standing start.",
        ),
        Question(
            "Why were flash-tight magazine doors so important?",
            listOf(
                "They kept the cordite dry",
                "They stop a turret fire flashing down into the magazine and destroying the ship",
                "They sealed the ship against flooding",
                "They kept the magazines cool",
            ),
            1,
            "At Jutland three British battlecruisers blew up because their crews had propped "
                + "the doors open to increase the rate of fire. A turret hit then flashed "
                + "straight down to the magazine.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
