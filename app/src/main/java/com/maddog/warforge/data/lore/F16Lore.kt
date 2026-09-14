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

/** F-16C Fighting Falcon, opened up. Centreline near y = 298. */
object F16Lore {

    private val modules = listOf(
        module(
            "engine", "General Electric F110", ModuleKind.ENGINE,
            detail = "Afterburning turbofan · 129 kN",
            info = "More thrust than the aircraft weighs. An F-16 at combat weight can "
                + "accelerate vertically - it does not climb so much as leave.",
        ) {
            revolveX(Role.BODY, 0, 16, 250f, 300f, 46f, 520f, 300f, 52f)
            cylinderZ(Role.TRIM, 2, 300f, 300f, 50f, -50f, 50f, 16)
        },
        module(
            "fbw", "Fly-by-wire flight control", ModuleKind.AVIONICS,
            detail = "Quadruplex digital, 4 computers",
            info = "The most important thing in the aircraft. The F-16 is deliberately "
                + "unstable in pitch - its centre of lift is ahead of its centre of gravity, "
                + "so left alone it would tumble. Four computers correct it dozens of times a "
                + "second. Instability is what makes it turn: a stable aeroplane spends lift "
                + "holding itself straight, an unstable one spends all of it manoeuvring.",
        ) {
            box3(Role.BODY, 0, 620f, 258f, -30f, 700f, 286f, 30f)
            box3(Role.BODY, 0, 560f, 262f, -26f, 620f, 282f, 26f)
        },
        module(
            "radar", "AN/APG-68 radar", ModuleKind.AVIONICS,
            detail = "Pulse-Doppler, multi-mode",
            info = "In the nose, ahead of everything. Pulse-Doppler can pick a low-flying "
                + "target out of ground clutter by its motion - the capability that finally "
                + "made look-down, shoot-down possible and changed how air war is fought.",
        ) { revolveX(Role.BODY, 0, 14, 846f, 292f, 30f, 900f, 296f, 12f) },
        module(
            "pilot", "Pilot", ModuleKind.CREW,
            detail = "Reclined 30°, side-stick",
            info = "The seat is tilted back 30 degrees so the pilot can take 9g without "
                + "blacking out - reclining raises the height blood has to be pumped by less. "
                + "There is no centre stick: the controls are a force-sensing side-stick on "
                + "the right console, because at 9g an arm cannot be lifted off an armrest.",
        ) { crewman(714f, 296f, 0f) },
        module(
            "canopy", "Bubble canopy", ModuleKind.OPTICS,
            detail = "Frameless polycarbonate",
            info = "One piece, with no arch across it. The F-16 pilot has an all-round view "
                + "no earlier fighter offered - and in a close fight, seeing the other "
                + "aircraft first is still what decides it.",
        ) { box3(Role.GLASS, 2, 676f, 248f, -28f, 820f, 292f, 28f) },
        module(
            "cannon", "M61A1 Vulcan", ModuleKind.GUN,
            detail = "20 mm, six barrels, 6,000 rpm",
            info = "A rotary cannon in the port wing root, firing a hundred rounds a second. "
                + "Five hundred rounds gives about five seconds of fire - a gun designed on "
                + "the assumption that the firing opportunity is very brief.",
        ) {
            box3(Role.BODY, 0, 640f, 268f, -70f, 760f, 288f, -40f)
        },
        module(
            "ammo", "Ammunition drum", ModuleKind.AMMO,
            detail = "511 rounds, linkless feed",
            info = "A drum behind the cockpit with a linkless feed: spent cases return to the "
                + "drum rather than being thrown overboard, where they could be swallowed by "
                + "the intake.",
        ) { cylinderZ(Role.BODY, 0, 600f, 282f, 30f, -34f, 34f, 14) },
        module(
            "fuel", "Internal fuel", ModuleKind.FUEL,
            detail = "3,200 L",
            info = "In the fuselage and wings. Short-legged for its class - the F-16 was "
                + "conceived as a cheap day fighter and grew into a strike aircraft, so it "
                + "usually flies with drop tanks.",
        ) {
            fuelCell(430f, 262f, -50f, 620f, 296f, 50f)
            fuelCell(480f, 322f, 90f, 560f, 342f, 210f)
            fuelCell(480f, 322f, -210f, 560f, 342f, -90f)
        },
        module(
            "ejection", "ACES II ejection seat", ModuleKind.ESCAPE,
            detail = "Zero-zero capable",
            info = "Works from a standing start on the runway - zero altitude, zero speed. A "
                + "rocket, not a cartridge, and a sequencer that chooses its trajectory from "
                + "the aircraft's speed and attitude.",
        ) { box3(Role.BODY, 0, 690f, 250f, -22f, 746f, 320f, 22f) },
        module(
            "intake", "Ventral intake", ModuleKind.AVIONICS,
            detail = "Fixed geometry, under the fuselage",
            info = "Under the fuselage rather than in the nose, which frees the nose for "
                + "radar. Fixed geometry, with no moving ramps - simpler, lighter and "
                + "cheaper, at the cost of top speed above Mach 2.",
        ) { box3(Role.BODY, 0, 560f, 330f, -42f, 790f, 388f, 42f) },
        module(
            "oxygen", "On-board oxygen generator", ModuleKind.LIFE,
            detail = "Molecular sieve · from engine bleed air",
            info = "No bottles at all: a sieve separates oxygen out of the engine's own "
                + "bleed air as the aeroplane flies. Nothing to run out of, nothing to "
                + "refill between sorties, and nothing to explode when it is hit.",
        ) {
            box3(Role.BODY, 0, 470f, 286f, -30f, 530f, 322f, 30f)
            cylinderZ(Role.TRIM, 1, 500f, 284f, 14f, -22f, 22f, 10)
        },
    )

    private val history = """
## The Fighter Mafia

The F-16 began as an argument. A group inside the US Air Force - John Boyd, Pierre
Sprey, Everest Riccioni and others - held that fighters had become too big, too
complicated and too expensive, and that what won air combat was energy and turn rate, not
radar and missiles. They wanted a small, cheap, light day fighter.

The Air Force did not want one. It got built anyway, as a technology demonstrator that
outperformed everything it flew against, and the resulting aircraft became the most widely
produced fighter of its generation: over 4,600 built, flown by more than twenty-five air
forces.

## Deliberately unstable

The F-16 is the first production fighter designed to be aerodynamically unstable. Its
centre of lift sits ahead of its centre of gravity, so if the computers stopped it would
tumble out of the sky in about half a second.

The reason is that a stable aircraft spends part of its lift holding its own nose up. An
unstable one spends all of it turning. Four flight-control computers correct the
instability dozens of times a second, and the pilot never feels it - he feels an aeroplane
that turns harder than physics ought to allow.

## Designed round the pilot

Open the cockpit and everything is arranged around sustaining 9g. The seat is reclined
30 degrees, which shortens the distance blood must be pumped upward and buys the pilot
consciousness he would otherwise lose. There is no centre stick, because at 9g an arm
cannot be lifted off an armrest - the controls are a force-sensing side-stick that barely
moves. The canopy is a single frameless bubble, because seeing the other aircraft first
still decides close fights.

## Radar in the nose

Moving the intake under the fuselage freed the nose for a radar antenna, and the AN/APG-68
is a pulse-Doppler set that can pick a low-flying target out of ground clutter by its
Doppler shift. Look-down, shoot-down changed air warfare: an aircraft could no longer hide
by flying low.

## What it became

The cheap lightweight day fighter grew a bigger radar, more fuel, more pylons and a
ground-attack role, and ended up doing everything. That is the usual fate of a good
airframe - and a quiet defeat for the argument that started it.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "1"),
        Spec("Empty weight", "8,570 kg"),
        Spec("Max takeoff", "19,190 kg"),
        Spec("Wingspan", "9.96 m"),
        Spec("Engine", "GE F110, 129 kN with afterburner"),
        Spec("Thrust to weight", "> 1.0 at combat weight"),
        Spec("Top speed", "Mach 2.0"),
        Spec("Service ceiling", "15,240 m"),
        Spec("g limit", "+9.0"),
        Spec("Cannon", "20 mm M61A1, 511 rounds"),
        Spec("Built", "4,600+"),
    )

    private val quiz = listOf(
        Question(
            "Why is the F-16 built to be aerodynamically unstable?",
            listOf(
                "To reduce drag at supersonic speed",
                "A stable aircraft spends lift holding itself straight; an unstable one spends it all turning",
                "To make it harder to detect on radar",
                "To allow a smaller tail",
            ),
            1,
            "Its centre of lift is ahead of its centre of gravity, so it would tumble in half "
                + "a second without the flight computers. That is the point: instability buys "
                + "turn performance.",
        ),
        Question(
            "Why is the pilot's seat reclined 30 degrees?",
            listOf(
                "To fit under a low canopy",
                "To shorten the distance blood must be pumped, so the pilot can take 9g",
                "To improve the view over the nose",
                "To make room for the ejection rocket",
            ),
            1,
            "Reclining reduces the vertical distance between heart and brain, which buys "
                + "consciousness at high g. The side-stick exists for the same reason: at 9g an "
                + "arm cannot be lifted off an armrest.",
        ),
        Question(
            "What did moving the intake under the fuselage make possible?",
            listOf(
                "A shorter takeoff run",
                "A radar antenna in the nose",
                "Better high-altitude performance",
                "A larger internal fuel load",
            ),
            1,
            "A nose intake - like the MiG-15's - uses up the only sensible place for a radar "
                + "antenna. The ventral intake frees it for the APG-68.",
        ),
        Question(
            "What does pulse-Doppler radar add over earlier sets?",
            listOf(
                "Longer detection range",
                "It can pick a low-flying target out of ground clutter by its motion",
                "It cannot be jammed",
                "It works in any weather",
            ),
            1,
            "Look-down, shoot-down. Before it, an aircraft could hide from radar simply by "
                + "flying low against the ground return.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
