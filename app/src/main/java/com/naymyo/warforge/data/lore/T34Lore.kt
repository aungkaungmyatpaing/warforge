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
 * T-34-85, opened up.
 *
 * The deliberate counterpart to [PanzerIVLore]. Same year, same job, opposite answers:
 * engine and gearbox together at the rear, sloped plate instead of thick plate, and
 * ammunition under the floor instead of in the sponsons. Fitting one after the other is
 * the clearest lesson in the game.
 *
 * Hull floor sits at y = 464, deck at y = 322, nose at high x.
 */
object T34Lore {

    private val modules = listOf(
        module(
            "engine", "V-2-34 diesel", ModuleKind.ENGINE,
            detail = "38.8 L V12 diesel · 500 hp",
            info = "An aluminium V12 diesel, enormous and light for its power. Diesel does "
                + "not flash the way petrol does, and 500 hp in a 32-tonne tank gave the "
                + "T-34 acceleration nothing German could match until the Panther.",
        ) {
            box3(Role.BODY, 0, 208f, 352f, -86f, 396f, 452f, 86f)
            box3(Role.TRIM, 2, 232f, 340f, -54f, 300f, 352f, 54f)
            box3(Role.DARK, -2, 250f, 452f, -44f, 350f, 464f, 44f)
        },
        module(
            "cooling", "Radiators", ModuleKind.RADIATOR,
            detail = "Twin radiators either side",
            info = "Set either side of the engine under the rear deck grilles. Keeping them "
                + "in the engine bay rather than up front is part of what lets the front of "
                + "the tank be nothing but sloped plate.",
        ) {
            box3(Role.BODY, 0, 198f, 350f, 88f, 380f, 424f, 106f)
            box3(Role.BODY, 0, 198f, 350f, -106f, 380f, 424f, -88f)
        },
        module(
            "transmission", "Five-speed gearbox", ModuleKind.TRANSMISSION,
            detail = "Rear-mounted, with the engine",
            info = "Behind the engine at the very back, driving the rear sprockets. No "
                + "shaft has to run under the crew, so the whole tank sits 23 cm lower than "
                + "a Panzer IV - and a lower tank is harder to see and harder to hit. The "
                + "price was a gearbox the driver often had to change with a mallet.",
        ) {
            box3(Role.BODY, 0, 172f, 386f, -74f, 250f, 456f, 74f)
        },
        module(
            "final_drive", "Rear final drives", ModuleKind.DRIVE,
            detail = "Rear sprocket",
            info = "The drive sprockets are at the back, where a track thrown off is less "
                + "likely to be in the way of the tank's own nose.",
        ) {
            cylinderZ(Role.BODY, 0, 200f, 448f, 42f, 86f, 118f, 10)
            cylinderZ(Role.BODY, 0, 200f, 448f, 42f, -118f, -86f, 10)
        },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front left",
            info = "Behind the famous sloped glacis, with a hatch cut straight through it. "
                + "The hatch is a weakness in an otherwise superb plate - but it is also the "
                + "only way in or out of the front of the tank.",
        ) { crewman(770f, 412f, -54f) },
        module(
            "hull_gunner", "Hull gunner / radio", ModuleKind.CREW,
            detail = "Front right",
            info = "Fires the bow DT machine gun and, on tanks that had one, works the "
                + "radio. Early T-34s carried radios only in command tanks; the rest took "
                + "orders by watching the leader's flags.",
        ) { crewman(770f, 412f, 54f) },
        module(
            "commander", "Commander", ModuleKind.CREW,
            detail = "Turret left, under the cupola",
            info = "The single biggest improvement of the -85. In the earlier T-34-76 the "
                + "commander also had to aim and fire the gun, so he was looking through a "
                + "sight exactly when he needed to be looking at the battlefield. Here he "
                + "commands and nothing else.",
        ) { crewman(444f, 320f, -52f) },
        module(
            "gunner", "Gunner", ModuleKind.CREW,
            detail = "Turret left, ahead of the commander",
            info = "Added with the enlarged turret in 1944, taking the gun off the "
                + "commander's hands at last.",
        ) { crewman(520f, 320f, -54f) },
        module(
            "loader", "Loader", ModuleKind.CREW,
            detail = "Turret right",
            info = "Works the 85 mm rounds, which weigh about 16 kg apiece - and reaches "
                + "most of them out of bins under his own feet.",
        ) { crewman(548f, 320f, 56f, facing = -1f) },
        module(
            "breech", "85 mm ZiS-S-53 breech", ModuleKind.GUN,
            detail = "D-5T / ZiS-S-53, 85 mm",
            info = "An anti-aircraft gun adapted for tank use, chosen because it already "
                + "existed and could kill a Tiger's flank. Fitting it needed a wider turret "
                + "ring, and the wider ring is what finally made room for a third man.",
        ) {
            box3(Role.BODY, 0, 596f, 262f, -32f, 688f, 306f, 32f)
            box3(Role.TRIM, -1, 552f, 268f, -28f, 596f, 300f, 28f)
            cylinderZ(Role.DARK, 1, 688f, 284f, 16f, -20f, 20f, 10)
        },
        module(
            "ammo_floor", "Floor ammunition bins", ModuleKind.AMMO,
            detail = "60 rounds, mostly under the floor",
            info = "Stored in bins under the turret floor with the rubber matting laid over "
                + "them. Safe from a side hit, but a loader who had used the handful of "
                + "ready rounds had to lift the mat and unbolt a lid while the fight went "
                + "on - the rate of fire collapsed.",
        ) {
            shellRack(392f, 460f, -44f, 8, r = 10f, len = 66f, spacing = 24f, halfZ = 14f)
            shellRack(392f, 460f, 44f, 8, r = 10f, len = 66f, spacing = 24f, halfZ = 14f)
        },
        module(
            "ammo_ready", "Ready rounds", ModuleKind.AMMO,
            detail = "In the turret bustle",
            info = "The few rounds the loader can reach without unbolting anything.",
        ) { shellRack(392f, 320f, 60f, 4, r = 9f, len = 58f, spacing = 21f, halfZ = 12f) },
        module(
            "fuel", "Sponson fuel tanks", ModuleKind.FUEL,
            detail = "540 L internal",
            info = "In the fighting compartment sponsons, beside the crew. Diesel made that "
                + "far less dangerous than it sounds - it will not flash from a spark - but "
                + "a fuel tank is still a poor thing to have at your elbow.",
        ) {
            fuelCell(430f, 398f, -100f, 620f, 452f, -62f)
            fuelCell(430f, 398f, 62f, 620f, 452f, 100f)
        },
        module(
            "optics", "TSh-16 gun sight", ModuleKind.OPTICS,
            detail = "4x telescope",
            info = "Serviceable, and noticeably worse than the German equivalent. Soviet "
                + "optical glass was the weak point of an otherwise brutally effective tank.",
        ) { box3(Role.BODY, 0, 600f, 268f, -50f, 676f, 284f, -38f) },
        module(
            "armour_front", "Glacis plate", ModuleKind.ARMOUR,
            detail = "45 mm at 60 degrees",
            info = "The single most influential piece of armour of the war. Laid back at 60 "
                + "degrees, 45 mm of plate presents about 90 mm along the path of a shot and "
                + "deflects a great deal of what does hit it - protection a Panzer IV needed "
                + "80 mm of vertical steel to match, at twice the weight.",
        ) {
            box3(Role.BODY, 0, 790f, 322f, -104f, 872f, 400f, 104f)
        },
        module(
            "armour_turret", "Cast turret armour", ModuleKind.ARMOUR,
            detail = "75 mm front, cast",
            info = "Cast rather than welded, so it could be made fast and in enormous "
                + "numbers by foundries that had never built a tank before.",
        ) {
            box3(Role.BODY, 0, 660f, 250f, -88f, 692f, 322f, 88f)
        },
        module(
            "turret_drive", "Electric turret traverse", ModuleKind.POWER,
            detail = "Electric motor · 360 degrees in 12 s",
            info = "Twice as fast round as a Panzer IV, and it works with the engine "
                + "stopped. The -85's turret is heavy enough that hand traverse alone "
                + "would have been hopeless.",
        ) {
            box3(Role.BODY, 0, 480f, 348f, -46f, 546f, 392f, 46f)
            cylinderZ(Role.TRIM, 1, 513f, 346f, 28f, -34f, 34f, 12)
        },
        module(
            "radio", "9RS transceiver", ModuleKind.RADIO,
            detail = "Every tank, from 1944",
            info = "Early T-34s carried a radio only in the platoon commander's tank and "
                + "the rest followed him by flag. Fitting one to every tank changed what "
                + "a Soviet tank unit could be asked to do more than the bigger gun did.",
        ) {
            box3(Role.BODY, 0, 300f, 330f, 80f, 372f, 380f, 118f)
            box3(Role.TRIM, 2, 372f, 340f, 88f, 380f, 370f, 110f)
        },
    )

    private val history = """
## Designed to be built

The T-34 was not the best-engineered tank of the war and was never meant to be. It was
designed to be built - by unskilled workers, in factories evacuated ahead of the German
advance and rebuilt on bare ground in the Urals, out of materials the Soviet Union
actually had. 84,000 were made. That number is the whole argument.

## The shock of 1941

When the T-34 appeared in June 1941, German crews found their guns bouncing off it. It
was not the thickness - 45 mm is not much - it was the angle. Sloping the plate back 60
degrees gives a shot far more steel to travel through and a good chance of deflecting it
entirely. Every serious tank designed afterwards, on either side, is sloped.

Wide tracks did the same trick for mobility: ground pressure low enough to keep going
through the rasputitsa mud and the snow, where narrow German tracks sank.

## What it cost

Early T-34s were crude in ways that got their crews killed. The two-man turret left the
commander aiming the gun instead of watching the battle. The gearbox was so heavy the
driver kept a mallet beside him. Vision devices were poor, radios rare. A well-drilled
Panzer IV crew would usually see a T-34 first, and seeing first is most of winning.

## The -85 answer

The 1944 T-34-85 fixed the worst of it. A wider turret ring took a bigger cast turret;
the bigger turret took three men, so the commander finally did nothing but command; and
the space took the 85 mm gun, adapted from an anti-aircraft piece, which could kill a
Tiger from the flank and a Panther from much closer than anyone wanted to get.

## Reading the layout

Open the hull and the difference from a Panzer IV is immediate. Engine and gearbox sit
together at the back, so nothing runs under the fighting compartment and the whole tank
is lower. The front is nothing but that sloped plate. And the ammunition is under the
floor rather than in the sponsons - safer from a side hit, but slow enough to reach that
a T-34's rate of fire fell away sharply once the ready rounds were gone.

## Afterwards

T-34s fought in Korea, in the Middle East through the 1970s, in Angola, in Bosnia. Some
were still in service, somewhere, seventy years after the first one left Kharkov.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "5"),
        Spec("Combat weight", "32.2 t"),
        Spec("Length (hull)", "6.10 m"),
        Spec("Height", "2.45 m"),
        Spec("Engine", "V-2-34 diesel, 500 hp"),
        Spec("Power to weight", "15.5 hp/t"),
        Spec("Road speed", "55 km/h"),
        Spec("Range (road)", "300 km"),
        Spec("Main gun", "85 mm ZiS-S-53"),
        Spec("Ammunition", "60 rounds"),
        Spec("Armour, glacis", "45 mm at 60°"),
        Spec("Armour, turret front", "75 mm"),
        Spec("Built", "~84,000 (all marks)"),
    )

    private val quiz = listOf(
        Question(
            "Why does 45 mm of sloped glacis protect as well as much thicker vertical plate?",
            listOf(
                "Sloped plate is made of harder steel",
                "A shot has to travel further through it, and tends to deflect",
                "It spreads the impact over a larger area",
                "It is thicker than it looks from the front",
            ),
            1,
            "At 60 degrees a shot crosses about twice the plate's thickness, and often skids "
                + "off instead of biting. A Panzer IV needed 80 mm of near-vertical steel for "
                + "comparable protection, at far greater weight.",
        ),
        Question(
            "What did the three-man turret of the T-34-85 fix?",
            listOf(
                "It allowed a bigger engine",
                "It freed the commander from also aiming the gun",
                "It carried more ammunition",
                "It made the tank lower",
            ),
            1,
            "In the two-man turret of the T-34-76 the commander was the gunner. He could "
                + "command or he could shoot, not both - and the side that sees first usually "
                + "wins.",
        ),
        Question(
            "Where is most of a T-34's ammunition stowed, and what is the drawback?",
            listOf(
                "In the sponsons - easy to reach but easy to hit",
                "In the turret bustle - safe but small",
                "Under the floor - safe from side hits but slow to reach",
                "Behind the engine - safe but unreachable in action",
            ),
            2,
            "Bins under the turret floor, under rubber matting. Safe from a flank penetration, "
                + "but once the ready rounds were gone the loader had to lift the mat and open "
                + "a lid mid-fight.",
        ),
        Question(
            "Why is the T-34 lower than a Panzer IV?",
            listOf(
                "Smaller road wheels",
                "A shallower hull floor",
                "Engine and gearbox are both at the rear, so no driveshaft runs under the crew",
                "A smaller turret",
            ),
            2,
            "Rear engine plus rear transmission means nothing has to run the length of the "
                + "hull under the fighting compartment. That saves 23 cm of height, and height "
                + "is what gets a tank seen.",
        ),
        Question(
            "Why did a diesel engine matter?",
            listOf(
                "It was more powerful than any petrol engine",
                "Diesel does not flash from a spark, so fuel in the crew compartment is far less dangerous",
                "It was quieter",
                "It needed no cooling system",
            ),
            1,
            "The T-34 carries fuel in the fighting compartment sponsons. With petrol that "
                + "would be close to suicidal; with diesel, which needs compression rather "
                + "than a spark to ignite, it is merely unpleasant.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
