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

/**
 * M1A2 Abrams, opened up.
 *
 * Everything here is arranged around one idea the earlier tanks in this game never had:
 * that the crew should survive being hit. Hull floor near y = 462, turret roof near
 * y = 258.
 */
object AbramsLore {

    private val modules = listOf(
        module(
            "engine", "AGT1500 gas turbine", ModuleKind.ENGINE,
            detail = "Gas turbine · 1,500 hp",
            info = "A helicopter engine in a tank. Enormous power for its size, almost "
                + "vibration-free, starts in any cold, runs on anything that burns - and "
                + "drinks about 40 litres per hour standing still, because a turbine has no "
                + "idle worth the name. Quiet enough on the move that crews call it the "
                + "whispering death.",
        ) {
            box3(Role.BODY, 0, 156f, 352f, -86f, 380f, 452f, 86f)
            cylinderZ(Role.TRIM, 2, 200f, 372f, 30f, -60f, 60f, 16)
        },
        module(
            "transmission", "Allison X-1100", ModuleKind.TRANSMISSION,
            detail = "4 forward, 2 reverse, hydrokinetic",
            info = "Bolted directly to the turbine as one removable powerpack. A trained "
                + "crew can pull the whole assembly and drop a replacement in under an hour, "
                + "which is how an army keeps tanks running rather than repairing them.",
        ) { box3(Role.BODY, 0, 150f, 396f, -80f, 250f, 460f, 80f) },
        module(
            "final_drive", "Final drives", ModuleKind.DRIVE,
            detail = "Rear sprocket",
            info = "Drive at the rear, as on the T-34 and for the same reason: no shaft has "
                + "to run under the fighting compartment, so the tank sits lower.",
        ) {
            cylinderZ(Role.BODY, 0, 175f, 446f, 40f, 86f, 118f, 12)
            cylinderZ(Role.BODY, 0, 175f, 446f, 40f, -118f, -86f, 12)
        },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front centre, reclined",
            info = "Lies back almost horizontally on the hull centreline. Reclining him cuts "
                + "the height of the front of the tank by a good 30 cm, and every centimetre "
                + "of height is a centimetre an enemy gunner can see.",
        ) { crewman(800f, 430f, 0f) },
        module(
            "commander", "Commander", ModuleKind.CREW,
            detail = "Turret right, under the CITV",
            info = "Has his own independent thermal sight that can search while the gunner "
                + "engages. 'Hunter-killer': the commander finds the next target while the "
                + "current one is still being shot at, and hands it straight to the gun.",
        ) { crewman(400f, 336f, 44f) },
        module(
            "gunner", "Gunner", ModuleKind.CREW,
            detail = "Turret right, ahead of the commander",
            info = "Works a thermal sight and a laser rangefinder feeding a ballistic "
                + "computer that knows the ammunition type, the crosswind, the barrel wear "
                + "and the tank's own motion. First-round hit probability at 2,000 m is "
                + "better than nine in ten.",
        ) { crewman(484f, 336f, 44f) },
        module(
            "loader", "Loader", ModuleKind.CREW,
            detail = "Turret left",
            info = "The last human loader on a Western tank of this class, and deliberately "
                + "so: the US Army judged a fourth pair of hands worth more for maintenance "
                + "and security than an autoloader saves in weight.",
        ) { crewman(470f, 336f, -46f, facing = -1f) },
        module(
            "breech", "120 mm M256 breech", ModuleKind.GUN,
            detail = "Rheinmetall L/44 smoothbore",
            info = "A German gun built under licence. Smoothbore because the ammunition that "
                + "matters is a fin-stabilised dart of depleted uranium, and rifling would "
                + "only slow it down.",
        ) {
            box3(Role.BODY, 0, 596f, 282f, -34f, 706f, 330f, 34f)
            box3(Role.TRIM, -1, 540f, 288f, -30f, 596f, 324f, 30f)
            cylinderZ(Role.DARK, 1, 706f, 304f, 18f, -22f, 22f, 12)
        },
        module(
            "ammo_bustle", "Bustle ammunition", ModuleKind.AMMO,
            detail = "34 ready rounds, blow-out panels",
            info = "The single most important thing in this tank. Rounds live in the turret "
                + "bustle behind a sliding armoured door, under roof panels designed to be "
                + "the weakest part of the structure. A hit that sets the ammunition off "
                + "blows the roof off and vents the fire upward - away from the crew, who "
                + "walk out. Every earlier tank in this game stowed its rounds where a "
                + "penetration killed everybody.",
        ) {
            shellRack(300f, 330f, 44f, 6, r = 11f, len = 76f, spacing = 26f, halfZ = 22f)
            box3(Role.TRIM, 2, 280f, 256f, -60f, 460f, 266f, 60f)
        },
        module(
            "ammo_hull", "Hull ammunition", ModuleKind.AMMO,
            detail = "8 rounds, armoured box",
            info = "A smaller reserve low in the hull behind its own blow-out panel, "
                + "transferred to the bustle when there is a lull.",
        ) { shellRack(520f, 456f, 0f, 4, r = 10f, len = 62f, spacing = 24f, halfZ = 26f) },
        module(
            "fuel", "Fuel cells", ModuleKind.FUEL,
            detail = "1,900 L",
            info = "Nearly two tonnes of it, in armoured cells separated from the crew - a "
                + "turbine's appetite has to be fed. Even so the Abrams is good for only "
                + "about 425 km on roads.",
        ) {
            fuelCell(250f, 396f, -100f, 460f, 458f, -40f)
            fuelCell(250f, 396f, 40f, 460f, 458f, 100f)
            fuelCell(600f, 400f, -70f, 760f, 458f, 70f)
        },
        module(
            "citv", "Commander's thermal viewer", ModuleKind.OPTICS,
            detail = "Independent 360° thermal",
            info = "Turns independently of the turret. Thermal imaging is why coalition "
                + "tanks in 1991 engaged at ranges at which their opponents could not see "
                + "them at all, through smoke and at night.",
        ) { box3(Role.BODY, 0, 470f, 206f, -18f, 546f, 248f, 18f) },
        module(
            "gps", "Gunner's primary sight", ModuleKind.OPTICS,
            detail = "Thermal, laser rangefinder, ballistic computer",
            info = "Laser ranges the target, the computer solves the problem, the stabiliser "
                + "holds the lay. The gunner's job is to put the reticle on the target and "
                + "fire; everything else is done for him.",
        ) { box3(Role.BODY, 0, 612f, 240f, -46f, 692f, 270f, -28f) },
        module(
            "armour_front", "Chobham composite", ModuleKind.ARMOUR,
            detail = "Composite, with depleted uranium mesh",
            info = "Layers of ceramic tile, steel and - from the M1A1HA - a depleted uranium "
                + "mesh, in a spaced array. Against a shaped charge the ceramic shatters the "
                + "jet; against a dart the layers shear it. Estimated protection against a "
                + "kinetic round is equivalent to something like 600 mm of steel, at a "
                + "fraction of the weight that would cost.",
        ) {
            box3(Role.BODY, 0, 660f, 262f, -88f, 706f, 344f, 88f)
            box3(Role.BODY, 0, 846f, 350f, -110f, 886f, 400f, 110f)
        },
        module(
            "armour_side", "Skirt armour", ModuleKind.ARMOUR,
            detail = "Heavy forward, light aft",
            info = "Heavy composite over the forward half of the running gear, thin plate "
                + "over the rest. Nobody can armour a whole tank equally; the Abrams spends "
                + "its weight on the arc it expects to be shot from.",
        ) {
            box3(Role.BODY, 0, 420f, 380f, 112f, 878f, 444f, 128f)
            box3(Role.BODY, 0, 420f, 380f, -128f, 878f, 444f, -112f)
        },
        module(
            "turret_drive", "Electric turret drive", ModuleKind.POWER,
            detail = "All-electric · 360 degrees in 9 s",
            info = "The M1A2 took the hydraulics out of the turret entirely. Nine seconds "
                + "round, no fluid to burn, and the gun stays on target while the tank "
                + "drives over rough ground.",
        ) {
            box3(Role.BODY, 0, 440f, 350f, -50f, 520f, 398f, 50f)
            cylinderZ(Role.TRIM, 1, 480f, 348f, 30f, -38f, 38f, 12)
        },
        module(
            "radio", "SINCGARS radios", ModuleKind.RADIO,
            detail = "Frequency-hopping · two sets",
            info = "Hops frequency a hundred times a second so it cannot be jammed or "
                + "direction-found. The M1A2 also passes target data between tanks "
                + "digitally, which is what the 'A2' mostly is.",
        ) {
            box3(Role.BODY, 0, 296f, 318f, -66f, 372f, 368f, 66f)
            box3(Role.TRIM, 2, 372f, 328f, -46f, 380f, 358f, 46f)
        },
        module(
            "nbc", "NBC overpressure system", ModuleKind.LIFE,
            detail = "Filtered air, positive pressure",
            info = "Blows filtered air into the crew compartment fast enough to keep it "
                + "at higher pressure than outside, so contamination is pushed out "
                + "through every gap instead of leaking in. The crew fight without masks.",
        ) {
            box3(Role.BODY, 0, 250f, 330f, 40f, 330f, 386f, 110f)
            cylinderZ(Role.TRIM, 1, 290f, 328f, 22f, 46f, 104f, 12)
        },
    )

    private val history = """
## Designed after a failure

The Abrams exists because the MBT-70 did not. That joint American-German project of the
1960s tried to put everything anyone could imagine into one tank - a driver in the turret,
a missile-firing main gun, a hydropneumatic suspension - and collapsed under its own
ambition and cost. What followed was deliberately conservative in layout and radical only
where it mattered: armour, fire control, and crew survival.

## Three ideas

**Chobham armour.** Not steel but a spaced array of ceramic tiles, steel and (from 1988) a
depleted uranium mesh. A shaped-charge jet is shattered by the ceramic; a kinetic dart is
sheared by the layers. The protection is worth several times its weight in plate, and it
is the reason a 63-tonne tank can be protected like a 200-tonne one.

**A gas turbine.** 1,500 hp from a helicopter engine, almost no vibration, instant cold
starts, and an appetite that has defined American logistics ever since.

**Blow-out ammunition stowage.** This is the one that matters. The rounds sit in the turret
bustle behind a sliding armoured door, under roof panels built to be the weakest part of
the tank. When the ammunition is hit it burns upward through those panels and out. The
crew, behind the door, survive. Every earlier tank in this game stowed its ammunition
where a penetration killed everyone aboard.

## 1991

In Operation Desert Storm, M1A1s engaged Iraqi T-72s at ranges beyond 2,500 m, through
smoke and at night, using thermal sights their opponents did not have. The exchange ratio
was not close. No Abrams was lost to enemy tank fire in that war; the handful destroyed
were lost to mines, friendly fire and deliberate scuttling.

It is worth being careful about what that proves. It was a very one-sided engagement
against export-model tanks with poorer ammunition, poorer crews and no thermal sights.
What it does demonstrate is that seeing first and hitting first, which decided the fights
between Panzer IVs and T-34s in 1943, still decides them.

## Reading the layout

Open the hull and the priorities are legible. The driver lies back almost flat, which
takes 30 cm off the height of the tank's front. Engine and gearbox come out together as
one powerpack in under an hour. The fuel is in armoured cells away from the crew. And the
ammunition is behind a door, under a roof designed to fail.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "4"),
        Spec("Combat weight", "63 t"),
        Spec("Length (hull)", "7.93 m"),
        Spec("Height", "2.44 m"),
        Spec("Engine", "AGT1500 turbine, 1,500 hp"),
        Spec("Power to weight", "23.8 hp/t"),
        Spec("Road speed", "67 km/h (governed)"),
        Spec("Range (road)", "425 km"),
        Spec("Main gun", "120 mm M256 smoothbore"),
        Spec("Ammunition", "42 rounds"),
        Spec("Armour", "Chobham composite + DU mesh"),
        Spec("Built", "10,000+ (all marks)"),
    )

    private val quiz = listOf(
        Question(
            "What happens when an Abrams' ammunition is hit?",
            listOf(
                "It is inert until loaded and does not detonate",
                "Blow-out panels vent the fire upward, away from the crew behind an armoured door",
                "An automatic system floods the compartment",
                "The turret is blown clear of the hull",
            ),
            1,
            "Rounds live in the bustle behind a sliding door, under roof panels built to be "
                + "the weakest part of the tank. The fire goes up and out; the crew walk away. "
                + "Every earlier tank in this game killed its crew in the same situation.",
        ),
        Question(
            "What is Chobham armour?",
            listOf(
                "A very hard single steel plate",
                "A spaced array of ceramic tiles, steel and depleted uranium mesh",
                "Reactive blocks that explode outward",
                "Sloped plate with an air gap behind it",
            ),
            1,
            "The ceramic shatters a shaped-charge jet; the layered array shears a kinetic "
                + "dart. It protects like several times its weight in plate, which is how a "
                + "63-tonne tank gets 600 mm-equivalent frontal protection.",
        ),
        Question(
            "What is the gas turbine's main drawback?",
            listOf(
                "It vibrates badly",
                "It cannot start in cold weather",
                "Fuel consumption - about 40 litres an hour even standing still",
                "It cannot run on diesel",
            ),
            2,
            "A turbine has no useful idle. The Abrams carries 1,900 litres and still manages "
                + "only about 425 km, and keeping it fuelled has shaped American logistics "
                + "for forty years.",
        ),
        Question(
            "Why does the driver lie almost flat?",
            listOf(
                "To fit the transmission above him",
                "So the front of the tank can be 30 cm lower",
                "To protect him from mine blast",
                "To make room for hull ammunition",
            ),
            1,
            "Height is what gets a tank seen and hit. Reclining the driver takes 30 cm off "
                + "the nose - the same argument that made the T-34's rear transmission worth "
                + "having.",
        ),
        Question(
            "Why is the M256 a smoothbore rather than a rifled gun?",
            listOf(
                "Smoothbores are cheaper to make",
                "Its main round is a fin-stabilised dart, which rifling would only slow",
                "Rifling wears out too quickly at 120 mm",
                "It fires guided missiles",
            ),
            1,
            "Armour-piercing fin-stabilised discarding sabot rounds are stabilised by their "
                + "fins, not by spin. Rifling would rob them of velocity for no gain.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
