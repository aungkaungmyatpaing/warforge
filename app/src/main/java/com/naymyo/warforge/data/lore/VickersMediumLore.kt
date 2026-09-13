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
 * Vickers Medium Mk II, opened up.
 *
 * Notable for putting its engine at the *front*, which nobody repeated - and for being
 * the first tank fast enough that armies had to invent a way of using speed. Hull floor
 * near y = 424, turret roof near y = 216.
 */
object VickersMediumLore {

    private val modules = listOf(
        module(
            "engine", "Armstrong Siddeley V8", ModuleKind.ENGINE,
            detail = "Air-cooled V8 · 90 hp",
            info = "Air-cooled, and mounted at the front rather than the rear. It saved the "
                + "weight and vulnerability of a radiator and coolant system, and it put the "
                + "engine block between the enemy and the crew. It also filled the fighting "
                + "compartment with heat and noise, and nobody built a tank this way again.",
        ) {
            box3(Role.BODY, 0, 650f, 346f, -70f, 810f, 420f, 70f)
            cylinderZ(Role.DARK, -2, 820f, 380f, 14f, -16f, 16f, 12)
        },
        module(
            "transmission", "Gearbox", ModuleKind.TRANSMISSION,
            detail = "Four speeds, clutch-and-brake steering",
            info = "Behind the engine at the front, driving the front sprockets. Clutch-and-"
                + "brake steering wastes power and wears the brakes, but it is simple enough "
                + "that a 1925 workshop could keep it going.",
        ) { box3(Role.BODY, 0, 570f, 372f, -60f, 650f, 420f, 60f) },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front centre, beside the engine",
            info = "Sits alongside a running air-cooled V8, which is exactly as unpleasant "
                + "as it sounds.",
        ) { crewman(600f, 398f, -50f) },
        module(
            "commander", "Commander", ModuleKind.CREW,
            detail = "Turret, under the cupola",
            info = "The Vickers was the first British tank with a commander who could see "
                + "out properly - and the first fast enough that seeing out mattered.",
        ) { crewman(470f, 292f, 0f) },
        module(
            "gunner", "Gunner", ModuleKind.CREW,
            detail = "Turret left",
            info = "Works the 3-pounder. A three-man turret in 1925 was well ahead of its "
                + "time; the Soviets were still building two-man turrets sixteen years later.",
        ) { crewman(540f, 292f, -44f) },
        module(
            "loader", "Loader", ModuleKind.CREW,
            detail = "Turret right",
            info = "Also works one of the turret machine guns. A Vickers Medium carried "
                + "between four and six of them.",
        ) { crewman(530f, 292f, 46f, facing = -1f) },
        module(
            "hull_gunner", "Hull machine gunner", ModuleKind.CREW,
            detail = "Left of the driver",
            info = "The fifth crewman, firing a Vickers gun from the hull side. British "
                + "doctrine of the period expected tanks to fight infantry far more often "
                + "than other tanks.",
        ) { crewman(420f, 398f, 60f) },
        module(
            "breech", "3-pounder breech", ModuleKind.GUN,
            detail = "47 mm QF",
            info = "A light gun for a light job: knocking out machine-gun posts and light "
                + "armour. It would have been hopeless against a 1940 tank, which is why the "
                + "design was obsolete by then.",
        ) {
            box3(Role.BODY, 0, 596f, 236f, -24f, 664f, 268f, 24f)
            cylinderZ(Role.DARK, 1, 664f, 252f, 12f, -14f, 14f, 12)
        },
        module(
            "ammo", "3-pdr rounds", ModuleKind.AMMO,
            detail = "110 rounds",
            info = "Racked low round the fighting compartment, under the turret basket.",
        ) { shellRack(400f, 420f, 0f, 8, r = 8f, len = 50f, spacing = 20f, halfZ = 34f) },
        module(
            "fuel", "Fuel tanks", ModuleKind.FUEL,
            detail = "170 L",
            info = "Good for about 190 km, which for 1925 was a remarkable radius of action - "
                + "the first tank that could be used operationally rather than tactically.",
        ) { fuelCell(230f, 360f, -60f, 330f, 418f, 60f) },
        module(
            "armour", "Riveted plate", ModuleKind.ARMOUR,
            detail = "6.25 mm",
            info = "Proof against rifle fire and nothing else. The Vickers traded protection "
                + "for speed on the theory that a tank that could not be caught did not need "
                + "to be armoured - a theory the 1940 campaign in France disposed of.",
        ) { box3(Role.BODY, 0, 828f, 344f, -108f, 848f, 420f, 108f) },
        module(
            "turret_drive", "Hand traverse", ModuleKind.POWER,
            detail = "Shoulder and handwheel",
            info = "No power at all: the gunner winds the turret round by hand, and for a "
                + "quick shift he braces against a pad and shoves. Fine against infantry, "
                + "hopeless against anything that moves.",
        ) {
            cylinderZ(Role.BODY, 0, 500f, 356f, 30f, -26f, 26f, 12)
            box3(Role.TRIM, 1, 470f, 348f, -18f, 500f, 366f, 18f)
        },
        module(
            "radio", "No.9 wireless set", ModuleKind.RADIO,
            detail = "Command tanks only",
            info = "A heavy set with an aerial that had to be cranked up while stopped. "
                + "Most Mediums had none and were controlled by flag signals from the "
                + "troop leader.",
        ) {
            box3(Role.BODY, 0, 300f, 344f, 60f, 366f, 392f, 96f)
        },
    )

    private val history = """
## Fast enough to matter

Every tank of the First World War walked. The Vickers Medium of 1923 could do 30 km/h on a
road, and that single fact changed what a tank was for. A machine that crawls at 6 km/h is
a piece of siege equipment; one that can cross twenty miles in a day is a weapon of
manoeuvre, and armies had to invent the doctrine to match.

## The Experimental Mechanised Force

Britain got there first. In 1927 and 1928 the War Office assembled the Experimental
Mechanised Force on Salisbury Plain - Vickers Mediums, armoured cars, motorised infantry
and towed artillery, all radio-linked, exercising as one formation. It was the first
mechanised combined-arms force anywhere in the world, and the exercises were watched
closely by foreign observers.

Britain then disbanded it and spent the 1930s building infantry tanks. Among the attentive
readers of the published results were Heinz Guderian and Mikhail Tukhachevsky, and the
ideas came back in 1939 and 1941 in other hands.

## The engine at the front

Open the hull and the oddity is immediate: the engine is at the *front*, an air-cooled V8
alongside the driver. It removed the weight and vulnerability of a radiator, and it put a
block of iron between the enemy and the crew. It also made the fighting compartment hot
and deafening, and put the gearbox and final drives where a frontal hit would find them.
No later tank in this game repeats it.

## Three men in a turret, in 1925

The other thing the Vickers got right long before anyone else was the turret crew.
Commander, gunner and loader, each with one job. The Soviet Union was still building
two-man turrets in 1941 and paying for it; the Vickers had solved that problem sixteen
years earlier and nobody noticed.

## What it was not

Six millimetres of riveted plate. The Vickers was built on the theory that a fast tank
does not need armour because it will not be caught, which held up exactly as long as it
took for anti-tank guns to become common. By 1939 the survivors were training vehicles.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "5"),
        Spec("Combat weight", "13.4 t"),
        Spec("Length", "5.33 m"),
        Spec("Height", "2.68 m"),
        Spec("Engine", "Armstrong Siddeley V8, 90 hp"),
        Spec("Power to weight", "6.7 hp/t"),
        Spec("Road speed", "30 km/h"),
        Spec("Range", "190 km"),
        Spec("Main gun", "3-pdr (47 mm)"),
        Spec("Machine guns", "4-6 Vickers"),
        Spec("Armour", "6.25 mm"),
        Spec("Built", "~200"),
    )

    private val quiz = listOf(
        Question(
            "What did the Vickers Medium's speed make possible?",
            listOf(
                "Crossing wider trenches",
                "Manoeuvre warfare, and the doctrine to go with it",
                "Operating without infantry support",
                "Fighting other tanks on equal terms",
            ),
            1,
            "30 km/h turned the tank from siege equipment into a weapon of manoeuvre. Britain's "
                + "Experimental Mechanised Force of 1927 was the first formation built round "
                + "that idea - and its published results were read carefully in Germany and "
                + "the Soviet Union.",
        ),
        Question(
            "Where is the Vickers Medium's engine, and why is that unusual?",
            listOf(
                "At the rear, like most tanks",
                "At the front, air-cooled, alongside the driver",
                "Under the turret floor",
                "Split between two units either side of the hull",
            ),
            1,
            "Front-mounted and air-cooled. It saved a radiator and put iron between the enemy "
                + "and the crew, at the cost of a hot, deafening fighting compartment. Nobody "
                + "repeated it.",
        ),
        Question(
            "What did the Vickers get right in 1925 that the Soviets were still getting wrong in 1941?",
            listOf(
                "Sloped armour",
                "A three-man turret, so the commander only commands",
                "A diesel engine",
                "Wide tracks for soft ground",
            ),
            1,
            "Commander, gunner and loader with one job each. The T-34-76 still had the "
                + "commander aiming the gun sixteen years later.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
