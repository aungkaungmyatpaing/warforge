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

/**
 * Supermarine Spitfire Mk V, opened up.
 *
 * An aircraft's insides read differently from a tank's: almost everything is arranged
 * around the centre of gravity, and the one crewman sits in the middle of it with the
 * fuel directly in front of him. Coordinates follow `Air.spitfire()`, centreline near
 * y = 296, nose at high x.
 */
object SpitfireLore {

    private val modules = listOf(
        module(
            "engine", "Rolls-Royce Merlin 45", ModuleKind.ENGINE,
            detail = "27 L V12 · 1,470 hp",
            info = "A supercharged 27-litre V12 on bearers ahead of the firewall. The Mk V "
                + "exists because the Merlin 45's single-stage supercharger gave more power "
                + "high up, where the fighting was.",
        ) {
            box3(Role.BODY, 0, 704f, 266f, -30f, 806f, 322f, 30f)
            box3(Role.TRIM, 2, 716f, 258f, -18f, 764f, 266f, 18f)
            revolveX(Role.DARK, 0, 8, 806f, 294f, 14f, 838f, 294f, 11f)   // reduction gear
        },
        module(
            "supercharger", "Supercharger", ModuleKind.ENGINE,
            detail = "Single-stage, two-speed",
            info = "Bolted to the back of the engine, cramming thin air into the cylinders. "
                + "Everything about a fighter's performance above 6,000 m comes down to how "
                + "good its supercharger is - which is exactly where the later two-stage "
                + "Merlins of the Mk IX left the Fw 190 behind.",
        ) { box3(Role.BODY, 0, 686f, 288f, -22f, 712f, 324f, 22f) },
        module(
            "fuel", "Fuselage fuel tanks", ModuleKind.FUEL,
            detail = "386 L in two tanks",
            info = "Two tanks stacked between the engine and the cockpit - directly in front "
                + "of the pilot's knees, behind a firewall. It keeps the fuel on the centre "
                + "of gravity so the aircraft handles the same full or empty, and it is the "
                + "reason burns to the hands and face were the signature Spitfire injury.",
        ) {
            fuelCell(636f, 256f, -26f, 700f, 288f, 26f)
            fuelCell(636f, 288f, -30f, 700f, 316f, 30f)
        },
        module(
            "coolant", "Glycol header tank", ModuleKind.RADIATOR,
            detail = "Above the engine",
            info = "A liquid-cooled engine needs plumbing, and plumbing can be punctured. "
                + "One rifle-calibre hole in the coolant system and a Merlin seizes within "
                + "minutes - the price paid for the slim nose a radial engine could never "
                + "give you.",
        ) { box3(Role.BODY, 0, 744f, 252f, -16f, 800f, 268f, 16f) },
        module(
            "radiator", "Underwing radiator", ModuleKind.RADIATOR,
            detail = "Starboard wing root",
            info = "Slung under the starboard wing, with the oil cooler under the port. "
                + "Hanging them in the airflow costs drag; burying them costs cooling. Every "
                + "liquid-cooled fighter of the war is a different compromise between those "
                + "two.",
        ) {
            box3(Role.BODY, 0, 540f, 328f, 40f, 640f, 368f, 96f)
            box3(Role.BODY, 0, 560f, 328f, -96f, 620f, 356f, -44f)
        },
        module(
            "pilot", "Pilot", ModuleKind.CREW,
            detail = "One, on the centre of gravity",
            info = "Sits over the wing spar with an armoured plate behind the seat and "
                + "another behind his head. Those two plates were added after the first "
                + "combats of 1939 and are credited with saving more pilots than any other "
                + "single change.",
        ) { crewman(576f, 300f, 0f) },
        module(
            "armour", "Seat armour", ModuleKind.ARMOUR,
            detail = "Head and back plate",
            info = "The only armour on the aircraft. A fighter cannot be armoured the way a "
                + "tank is - it would never leave the ground - so protection goes exactly "
                + "where it buys the most: behind the one irreplaceable component.",
        ) {
            box3(Role.BODY, 0, 528f, 254f, -22f, 540f, 322f, 22f)
        },
        module(
            "cannon", "20 mm Hispano cannon", ModuleKind.GUN,
            detail = "2 × 20 mm, 60 rounds each",
            info = "Out in the wings, clear of the propeller arc, which is why they need no "
                + "synchronising gear. It also means they are zeroed to converge at a set "
                + "range - and outside that range a good burst simply brackets the target.",
        ) {
            box3(Role.BODY, 0, 660f, 326f, 110f, 820f, 344f, 134f)
            box3(Role.BODY, 0, 660f, 326f, -134f, 820f, 344f, -110f)
        },
        module(
            "mgs", "Browning .303 machine guns", ModuleKind.GUN,
            detail = "4 × .303 in, 350 rounds each",
            info = "Rifle-calibre guns, effective in 1940 and badly outclassed by 1942. They "
                + "survived alongside the cannon because they never jammed and because their "
                + "tracer told the pilot where he was pointing.",
        ) {
            box3(Role.BODY, 0, 672f, 330f, 168f, 780f, 342f, 250f)
            box3(Role.BODY, 0, 672f, 330f, -250f, 780f, 342f, -168f)
        },
        module(
            "ammo", "Wing ammunition bays", ModuleKind.AMMO,
            detail = "Belt feed, in the wings",
            info = "The thin elliptical wing that made the Spitfire turn so well left very "
                + "little room for guns and belts. Fitting cannon into it at all took years "
                + "of work, and the early drum-fed installations jammed constantly.",
        ) {
            box3(Role.BODY, 0, 600f, 328f, 106f, 660f, 348f, 200f)
            box3(Role.BODY, 0, 600f, 328f, -200f, 660f, 348f, -106f)
        },
        module(
            "radio", "TR.1133 radio", ModuleKind.RADIO,
            detail = "VHF, four channels",
            info = "Behind the pilot. VHF sets replaced the earlier HF ones during 1940 and "
                + "transformed fighter control: clear speech to the ground meant radar plots "
                + "could be turned into interceptions.",
        ) { box3(Role.BODY, 0, 474f, 268f, -22f, 526f, 302f, 22f) },
        module(
            "oxygen", "Oxygen bottle", ModuleKind.AVIONICS,
            detail = "Behind the cockpit",
            info = "Combat happened at 7,000 m and above, where an unsupplied pilot loses "
                + "useful consciousness in a couple of minutes.",
        ) { cylinderZ(Role.BODY, 0, 460f, 296f, 14f, -20f, 20f, 8) },
        module(
            "controls", "Control runs", ModuleKind.AVIONICS,
            detail = "Cables and pushrods to the tail",
            info = "Cables run the length of the fuselage to the elevator and rudder. They "
                + "are the reason a cannon shell into the rear fuselage - which looks like "
                + "empty air - can be fatal.",
        ) {
            box3(Role.BODY, 0, 230f, 286f, -10f, 540f, 292f, -4f)
            box3(Role.BODY, 0, 230f, 300f, 4f, 540f, 306f, 10f)
        },
        module(
            "hydraulics", "Undercarriage hydraulics", ModuleKind.POWER,
            detail = "Engine-driven pump · one selector",
            info = "The first Spitfires had a hand pump, and a new pilot could be spotted "
                + "by the way his aeroplane porpoised after take-off while he changed "
                + "hands on the stick to work it. An engine-driven pump arrived with the "
                + "Mk II and the porpoising stopped.",
        ) {
            box3(Role.BODY, 0, 560f, 296f, -30f, 616f, 330f, 30f)
            cylinderZ(Role.TRIM, 1, 590f, 294f, 12f, -22f, 22f, 10)
        },
    )

    private val history = """
## An aeroplane that should not have worked

R. J. Mitchell's design was too complicated to build, too expensive, and late. The
elliptical wing that gave it its lift distribution and its look was a nightmare on the
shop floor - every rib a different size - and in 1938 Supermarine simply could not make
them fast enough. The Air Ministry seriously considered cancelling it in favour of more
Hurricanes.

## 1940

In the Battle of Britain the Hurricane shot down more aircraft, because there were more
Hurricanes. But the Spitfire could meet the Bf 109 on level terms, and that mattered more
than the tally: Fighter Command could send Spitfires against the escorts and Hurricanes
against the bombers. Take the Spitfire away and the arithmetic stops working.

## The long argument with the 109

For six years the Spitfire and the Bf 109 chased each other's performance. The 109E and
Spitfire I were close to even. The 109F pulled ahead; the Mk V answered. The Fw 190A of
1941 was better than anything the RAF had, and for a year it hurt - until the two-stage
Merlin 61 and the Mk IX of 1942, which restored parity almost overnight.

By the end there were 24 marks, a Griffon engine nearly twice as powerful as the original
Merlin, and an aircraft that weighed half as much again as the one that first flew.

## Reading the layout

Open the fuselage and it is immediately obvious how an aircraft differs from a tank.
Everything heavy - engine, fuel, pilot, guns - is packed around the centre of gravity, and
the rest is hollow. The fuel sits between the engine and the pilot, on the centre of
gravity, so the handling does not change as it burns off; it is also why so many Spitfire
pilots came home with burned hands.

The guns are out in the wings, clear of the propeller. That avoids synchronising gear but
means they converge at one chosen range. And the radiator hangs in the airflow under the
starboard wing - drag traded for cooling, the compromise every liquid-cooled fighter had
to make.

## What is left

More than 20,000 were built. Around 70 are still airworthy, and a Merlin at full throttle
remains one of the loudest arguments for keeping old machines flying.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "1"),
        Spec("Empty weight", "2,297 kg"),
        Spec("Loaded weight", "3,000 kg"),
        Spec("Wingspan", "11.23 m"),
        Spec("Engine", "Merlin 45, 1,470 hp"),
        Spec("Top speed", "605 km/h at 4,000 m"),
        Spec("Service ceiling", "11,300 m"),
        Spec("Range", "756 km"),
        Spec("Armament", "2 × 20 mm, 4 × .303 in"),
        Spec("Fuel", "386 L internal"),
        Spec("Built (Mk V)", "6,487"),
        Spec("Built (all marks)", "20,351"),
    )

    private val quiz = listOf(
        Question(
            "Why is the fuel carried between the engine and the pilot?",
            listOf(
                "There was nowhere else to put it",
                "It sits on the centre of gravity, so handling does not change as it burns off",
                "It keeps the fuel warm",
                "It protects the pilot from the front",
            ),
            1,
            "Weight that moves as it is consumed changes the aircraft's balance in flight. "
                + "Putting the tanks on the centre of gravity avoids that - at the cost of "
                + "a fire directly in front of the pilot, the signature Spitfire injury.",
        ),
        Question(
            "Why are the guns in the wings rather than the nose?",
            listOf(
                "The wings are stronger",
                "To keep them clear of the propeller arc, so no synchronising gear is needed",
                "To balance the aircraft",
                "They fire further apart, covering more sky",
            ),
            1,
            "Guns firing through the propeller need interrupter gear, which costs rate of "
                + "fire. Wing guns avoid it - but they have to be zeroed to converge at one "
                + "chosen range.",
        ),
        Question(
            "What is the drawback of a liquid-cooled engine in a fighter?",
            listOf(
                "It is heavier than an air-cooled one",
                "It cannot be supercharged",
                "A single hole in the coolant system will seize the engine within minutes",
                "It needs a bigger propeller",
            ),
            2,
            "That vulnerability is the trade for a slim, low-drag nose. Radial-engined "
                + "fighters like the Fw 190 could take hits in the cylinders and keep flying.",
        ),
        Question(
            "What restored the Spitfire's edge after the Fw 190 appeared in 1941?",
            listOf(
                "Heavier armament",
                "The two-stage supercharged Merlin 61 of the Mk IX",
                "A lighter airframe",
                "A larger wing",
            ),
            1,
            "The Fw 190A outclassed the Mk V for about a year. The two-stage Merlin restored "
                + "high-altitude performance and parity almost overnight - superchargers, not "
                + "airframes, decided most of that argument.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
