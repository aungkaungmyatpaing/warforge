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
 * Panzer IV Ausf. H, opened up.
 *
 * Coordinates line up with the hull in `Ground.panzerIV()`: the floor sits at y = 462,
 * the superstructure roof at y = 336, and the nose is at high x. The layout is the
 * German standard of the period - engine at the back, transmission and final drives at
 * the front, and a driveshaft running between them under the fighting compartment floor,
 * which is exactly why the tank is as tall as it is.
 */
object PanzerIVLore {

    private val modules = listOf(
        module(
            "engine", "Maybach HL 120 TRM", ModuleKind.ENGINE,
            detail = "11.9 L V12 petrol · 300 hp",
            info = "A petrol V12 in an armoured box at the rear. 300 hp moved 25 tonnes at "
                + "about 12 hp per tonne - adequate, never generous, and the reason late "
                + "Panzer IVs with extra armour felt sluggish.",
        ) {
            box3(Role.BODY, 0, 196f, 356f, -84f, 372f, 452f, 84f)
            box3(Role.TRIM, 2, 210f, 344f, -60f, 358f, 356f, 60f)      // air cleaner
            box3(Role.DARK, -2, 240f, 452f, -40f, 330f, 462f, 40f)     // sump
        },
        module(
            "cooling", "Radiators and fans", ModuleKind.RADIATOR,
            detail = "Two radiators, side-mounted",
            info = "Coolant radiators flank the engine, fed by fans off the crankshaft. "
                + "The grilles above them are a weak spot: a hit there stops the tank "
                + "without ever touching the crew.",
        ) {
            box3(Role.BODY, 0, 186f, 356f, 86f, 372f, 430f, 104f)
            box3(Role.BODY, 0, 186f, 356f, -104f, 372f, 430f, -86f)
        },
        module(
            "transmission", "SSG 76 gearbox", ModuleKind.TRANSMISSION,
            detail = "6 forward, 1 reverse",
            info = "Front-mounted, so the driver shifts a lever beside his own knee. "
                + "Putting the gearbox at the front makes the tank easy to steer and "
                + "maintain, but it means a frontal penetration lands in the "
                + "transmission - and in the driver.",
        ) {
            box3(Role.BODY, 0, 716f, 396f, -78f, 836f, 456f, 78f)
            box3(Role.TRIM, 1, 760f, 386f, -30f, 812f, 396f, 30f)
        },
        module(
            "driveshaft", "Driveshaft", ModuleKind.DRIVE,
            detail = "Rear engine to front gearbox",
            info = "The shaft runs the length of the hull under the turret floor. Everything "
                + "above it - the fighting compartment, the turret ring, the commander's "
                + "head - is raised by its thickness. Rear-drive designs like the T-34 "
                + "avoided this and came out lower.",
        ) {
            box3(Role.BODY, 0, 372f, 424f, -16f, 716f, 448f, 16f)
        },
        module(
            "final_drive", "Final drives and sprockets", ModuleKind.DRIVE,
            detail = "Front sprocket",
            info = "The last gear reduction before the track. German final drives were "
                + "chronically overloaded as tanks put on weight, and a stripped final "
                + "drive immobilised more Panzers than enemy guns did.",
        ) {
            cylinderZ(Role.BODY, 0, 806f, 448f, 40f, 84f, 116f, 10)
            cylinderZ(Role.BODY, 0, 806f, 448f, 40f, -116f, -84f, 10)
        },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front left",
            info = "Sits ahead of the turret ring with the gearbox between his feet, "
                + "steering with two tillers. His vision port is the thinnest part of the "
                + "glacis, which is why it became a favourite aiming mark.",
        ) { crewman(736f, 408f, -54f) },
        module(
            "radio_op", "Radio operator / bow gunner", ModuleKind.CREW,
            detail = "Front right",
            info = "Works the radio and the hull machine gun. Every German tank carried a "
                + "radio from the start of the war - a genuine tactical advantage over "
                + "opponents whose tanks talked by signal flag.",
        ) { crewman(736f, 408f, 54f) },
        module(
            "commander", "Commander", ModuleKind.CREW,
            detail = "Turret rear, under the cupola",
            info = "Raised in the cupola with all-round vision. A commander who only has to "
                + "command - rather than also aim the gun, as in a Soviet two-man turret - "
                + "finds targets first, and finding first usually decides the fight.",
        ) { crewman(432f, 338f, 0f) },
        module(
            "gunner", "Gunner", ModuleKind.CREW,
            detail = "Turret left of the gun",
            info = "Sits left of the breech behind the TZF sight, with elevation and "
                + "traverse handwheels to either hand.",
        ) { crewman(506f, 342f, -56f) },
        module(
            "loader", "Loader", ModuleKind.CREW,
            detail = "Turret right of the gun",
            info = "Stands to the right of the breech. The 75 mm round weighs about 12 kg; "
                + "a good loader kept the gun firing every six or seven seconds until the "
                + "ready rounds ran out.",
        ) { crewman(556f, 342f, 60f, facing = -1f) },
        module(
            "breech", "7.5 cm KwK 40 breech", ModuleKind.GUN,
            detail = "L/48, semi-automatic vertical block",
            info = "The breech and recoil guard take up most of the turret. The gun that "
                + "made the Panzer IV matter: the long L/48 could kill a T-34 at a "
                + "kilometre, where the original short L/24 howitzer could not do it at any "
                + "range.",
        ) {
            box3(Role.BODY, 0, 604f, 282f, -30f, 690f, 326f, 30f)
            box3(Role.TRIM, -1, 560f, 288f, -26f, 604f, 318f, 26f)     // recoil guard
            cylinderZ(Role.DARK, 1, 690f, 304f, 15f, -18f, 18f, 10)
        },
        module(
            "ammo_sponson", "Sponson ammunition", ModuleKind.AMMO,
            detail = "87 rounds total",
            info = "Most of the ammunition lies in the hull sponsons, above the tracks and "
                + "behind 30 mm of side armour. Any side penetration reaches it. This is "
                + "the single biggest reason a Panzer IV that was hit tended to burn.",
        ) {
            shellRack(408f, 438f, 86f, 10)
            shellRack(408f, 438f, -86f, 10)
        },
        module(
            "ammo_floor", "Floor stowage", ModuleKind.AMMO,
            detail = "Ready rounds",
            info = "A second group under the turret floor, slower to reach but safer than "
                + "the sponsons.",
        ) { shellRack(470f, 458f, 0f, 6, r = 8f, len = 52f, spacing = 20f, halfZ = 26f) },
        module(
            "fuel", "Fuel tanks", ModuleKind.FUEL,
            detail = "470 L, three tanks",
            info = "Under the fighting compartment floor, between the crew and the ground - "
                + "a deliberately awkward place to hit. 470 litres gave roughly 200 km on "
                + "roads and about half that across country.",
        ) {
            fuelCell(398f, 448f, -76f, 560f, 462f, -18f)
            fuelCell(398f, 448f, 18f, 560f, 462f, 76f)
            fuelCell(566f, 450f, -40f, 660f, 462f, 40f)
        },
        module(
            "radio", "Fu 5 radio set", ModuleKind.RADIO,
            detail = "Transmitter and receiver",
            info = "Mounted above the gearbox on the radio operator's side. Voice to other "
                + "tanks in the platoon, morse for anything further.",
        ) { box3(Role.BODY, 0, 700f, 348f, 44f, 772f, 388f, 92f) },
        module(
            "optics", "TZF 5f gun sight", ModuleKind.OPTICS,
            detail = "2.5x articulated telescope",
            info = "The gunner's telescope, geared to the gun so it moves with it. German "
                + "optics were the best of the war, and a clearer sight is worth as much as "
                + "a bigger gun.",
        ) { box3(Role.BODY, 0, 600f, 286f, -44f, 684f, 302f, -32f) },
        module(
            "armour_front", "Frontal armour", ModuleKind.ARMOUR,
            detail = "80 mm, near vertical",
            info = "Ausf. H thickened the glacis to a solid 80 mm plate. Thick, but barely "
                + "sloped - the Panzer IV was designed before anyone appreciated how much "
                + "free protection a sloped plate gives you, and it never got the chance to "
                + "change.",
        ) {
            box3(Role.BODY, 0, 836f, 340f, -104f, 852f, 392f, 104f)
            box3(Role.BODY, 0, 840f, 392f, -104f, 856f, 458f, 104f)
        },
        module(
            "armour_side", "Side armour", ModuleKind.ARMOUR,
            detail = "30 mm, plus 5 mm Schürzen",
            info = "Only 30 mm on the flank. The bolted-on Schürzen skirts were not extra "
                + "armour in any real sense - they were there to make a Soviet anti-tank "
                + "rifle round tumble and break up before it reached the hull.",
        ) {
            box3(Role.BODY, 0, 200f, 392f, 104f, 840f, 458f, 112f)
            box3(Role.BODY, 0, 200f, 392f, -112f, 840f, 458f, -104f)
        },
        module(
            "turret_drive", "Hydraulic turret traverse", ModuleKind.POWER,
            detail = "Driven off the engine · 360 degrees in 25 s",
            info = "A pump driven from the engine, so the turret only turns while the "
                + "engine runs - and the gunner still has a handwheel for the last few "
                + "degrees, because hydraulics are too coarse to lay a gun with.",
        ) {
            box3(Role.BODY, 0, 470f, 352f, -40f, 540f, 396f, 40f)
            cylinderZ(Role.TRIM, 1, 505f, 350f, 26f, -30f, 30f, 12)
        },
    )

    private val history = """
## The tank that fought the whole war

Germany went to war in 1939 with the Panzer IV as its "support" tank: a slow, thinly
armoured vehicle with a short 7.5 cm howitzer, meant to blow up bunkers and gun positions
while the faster Panzer III did the tank fighting. By 1945 it was the only German tank
design still in production that had been there on day one - and it had swapped roles
entirely with the tank it was built to support.

## What the T-34 changed

Meeting the T-34 and the KV-1 in June 1941 was a shock the whole German tank programme
never fully recovered from. Neither the Panzer III's 5 cm nor the Panzer IV's stubby 7.5 cm
could reliably penetrate them. The answer was the Ausf. F2 of 1942: the same tank with a
long 7.5 cm KwK 40 L/43, and then the L/48 gun of the Ausf. G onward. Overnight the
support tank became the best tank killer Germany had in quantity.

## Reading the layout

Open the hull and the design is immediately legible. Engine at the back, gearbox and final
drives at the front, and a driveshaft joining them along the floor. That shaft forces the
turret floor upward, which is why the Panzer IV stands 2.68 m tall against the T-34's
2.45 m - and a taller tank is a tank that is seen and hit first.

The payoff is a roomy, well-shaped turret with a proper five-man crew: commander, gunner
and loader in the turret, driver and radio operator in the hull. The Soviet answer put
only two men in the turret, so the T-34's commander had to aim the gun as well as run the
tank. In an even fight that division of labour was worth more than a few millimetres of
plate.

## The cost of a flat plate

Ausf. H carried 80 mm on the nose - as thick as anything short of a Tiger - but almost
vertical. The T-34's 45 mm sloped at 60 degrees gave a comparable line-of-sight thickness
for a fraction of the weight, and deflected shot besides. And the flanks stayed at 30 mm
all war, with the ammunition stowed right behind them in the sponsons.

## What became of it

Around 8,500 were built, more than any other German tank, and the chassis went on to carry
the Sturmgeschütz IV, the Jagdpanzer IV, the Wirbelwind and the Hummel. Syria was still
fighting Panzer IVs against Israeli Centurions on the Golan Heights in 1967, a quarter of a
century after they left the factory.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "5"),
        Spec("Combat weight", "25.0 t"),
        Spec("Length (hull)", "5.92 m"),
        Spec("Height", "2.68 m"),
        Spec("Engine", "Maybach HL 120 TRM, 300 hp"),
        Spec("Power to weight", "12 hp/t"),
        Spec("Road speed", "42 km/h"),
        Spec("Range (road)", "200 km"),
        Spec("Main gun", "7.5 cm KwK 40 L/48"),
        Spec("Ammunition", "87 rounds"),
        Spec("Armour, front", "80 mm"),
        Spec("Armour, side", "30 mm"),
        Spec("Built", "~8,500 (all marks)"),
    )

    private val quiz = listOf(
        Question(
            "Why is the Panzer IV noticeably taller than a T-34?",
            listOf(
                "A driveshaft runs under the turret floor to a front gearbox",
                "Its turret ring is larger",
                "The suspension sits higher off the ground",
                "The commander needed a taller cupola",
            ),
            0,
            "Engine at the back, gearbox at the front, and a shaft between them - everything "
                + "above that shaft is lifted by its thickness. The T-34 put engine and "
                + "transmission together at the rear and came out 23 cm lower.",
        ),
        Question(
            "Where is most of the ammunition stowed?",
            listOf(
                "In the turret bustle",
                "In the hull sponsons, above the tracks",
                "Under the engine deck",
                "In the driver's compartment",
            ),
            1,
            "The sponsons, behind just 30 mm of side armour. A flank penetration reaches the "
                + "rounds directly, which is why side hits so often set a Panzer IV on fire.",
        ),
        Question(
            "What were the Schürzen side skirts actually for?",
            listOf(
                "Extra armour against tank guns",
                "Keeping mud off the running gear",
                "Breaking up anti-tank rifle rounds before they reached the hull",
                "Hiding the tank's outline from aircraft",
            ),
            2,
            "5 mm of plate stops nothing a tank gun fires. It was enough to make a Soviet "
                + "anti-tank rifle bullet tumble and shatter, and to set off shaped charges "
                + "early.",
        ),
        Question(
            "What changed the Panzer IV from a support tank into Germany's main tank killer?",
            listOf(
                "Thicker frontal armour",
                "A more powerful engine",
                "Replacing the short 7.5 cm howitzer with the long KwK 40",
                "Adding a fifth crewman",
            ),
            2,
            "The short L/24 was a bunker-busting howitzer. The long L/43 and L/48 of 1942 "
                + "onward could kill a T-34 at a kilometre, and the Panzer IV took over the "
                + "anti-tank role from the Panzer III.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
