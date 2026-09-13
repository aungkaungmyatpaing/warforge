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

/** Nimitz-class carrier, opened up. Waterline near y = 432, flight deck near y = 330. */
object NimitzLore {

    private val modules = listOf(
        module(
            "reactors", "2 × A4W reactors", ModuleKind.REACTOR,
            detail = "~550 MW thermal each",
            info = "Two reactors driving four shafts. They were designed to run for about "
                + "twenty years between refuellings, and the ship carries no fuel of her own "
                + "at all - the 9 million litres aboard are for the aircraft.",
        ) {
            cylinderZ(Role.BODY, 0, 420f, 400f, 44f, -70f, 70f, 18)
            cylinderZ(Role.BODY, 0, 560f, 400f, 44f, -70f, 70f, 18)
        },
        module(
            "turbines", "Geared steam turbines", ModuleKind.ENGINE,
            detail = "4 shafts · 260,000 shp",
            info = "260,000 horsepower to move 100,000 tonnes at over 30 knots. Speed is not "
                + "vanity: a carrier turning into wind at 30 knots adds that to the airflow "
                + "over the deck, and that is the difference between launching a loaded "
                + "aircraft and not.",
        ) { box3(Role.BODY, 0, 300f, 380f, -80f, 680f, 448f, 80f) },
        module(
            "hangar", "Hangar deck", ModuleKind.MAGAZINE,
            detail = "Two-thirds of the flight deck area",
            info = "Under the flight deck, divided by fireproof doors into three bays so a "
                + "fire in one does not take the ship. About half the air wing is stored "
                + "here; the rest lives on deck in all weathers.",
        ) { box3(Role.BODY, 0, 200f, 346f, -84f, 840f, 394f, 84f) },
        module(
            "catapults", "Steam catapults", ModuleKind.AVIONICS,
            detail = "4 × C13-2",
            info = "A steam-driven piston under the deck accelerates a 25-tonne aircraft to "
                + "270 km/h in about two seconds - roughly 4g. Without a catapult a carrier "
                + "can launch only light aircraft, and the invention of the steam catapult "
                + "is what let jets go to sea at all.",
        ) {
            box3(Role.BODY, 0, 700f, 326f, -8f, 950f, 338f, 8f)
            box3(Role.BODY, 0, 560f, 332f, 40f, 790f, 344f, 56f)
        },
        module(
            "arrestor", "Arrestor gear", ModuleKind.AVIONICS,
            detail = "4 wires, hydraulic",
            info = "Four cables across the deck, each able to stop a 25-tonne aircraft in "
                + "100 m. Every landing is a controlled crash at full throttle - the pilot "
                + "goes to military power the instant the wheels touch, in case the hook "
                + "misses.",
        ) {
            for (i in 0 until 4) box3(Role.BODY, 0, 200f + i * 34f, 324f, -70f, 206f + i * 34f, 330f, 70f)
        },
        module(
            "angled_deck", "Angled deck", ModuleKind.ARMOUR,
            detail = "British invention, 1952",
            info = "Landing along a deck angled about 9 degrees off the ship's axis means a "
                + "pilot who misses the wires flies off over open water instead of into the "
                + "aircraft parked forward. Before it, a crash barrier was the only thing "
                + "between a missed approach and a catastrophe. It is the single most "
                + "important carrier innovation after the catapult.",
        ) { box3(Role.BODY, 0, 240f, 320f, -92f, 700f, 330f, 20f) },
        module(
            "island", "Island superstructure", ModuleKind.CREW,
            detail = "Bridge, flag bridge, air control",
            info = "Everything the ship needs above deck crammed into the smallest possible "
                + "footprint on the starboard side, because every square metre of island is "
                + "a square metre not available for aircraft.",
        ) {
            crewman(420f, 276f, 40f)
            crewman(440f, 276f, 56f)
            box3(Role.BODY, 0, 376f, 248f, 20f, 492f, 322f, 76f)
        },
        module(
            "avgas", "Aviation fuel", ModuleKind.FUEL,
            detail = "9 million litres of JP-5",
            info = "The ship's own fuel is nuclear; all of this is for the aircraft. JP-5 is "
                + "specified with a high flash point precisely because it has to be stored "
                + "and pumped aboard a ship, and it is stored in tanks surrounded by "
                + "seawater ballast.",
        ) {
            fuelCell(300f, 420f, -80f, 700f, 456f, 80f)
        },
        module(
            "magazine", "Aviation ordnance", ModuleKind.MAGAZINE,
            detail = "Deep magazines, armoured lifts",
            info = "Bombs and missiles stowed low under armour, raised by dedicated weapons "
                + "lifts. Ordnance handling is the most dangerous routine work aboard - the "
                + "1967 Forrestal fire, which killed 134 men, began with a rocket firing "
                + "accidentally on deck.",
        ) {
            box3(Role.BODY, 0, 380f, 450f, -60f, 620f, 478f, 60f)
        },
        module(
            "crew", "Ship's company and air wing", ModuleKind.CREW,
            detail = "~5,700 total",
            info = "Around 3,200 running the ship and 2,500 running the aircraft - a small "
                + "town at sea, with its own hospital, dentist, post office, chapel, shops "
                + "and television station. The average age is about twenty.",
        ) {
            crewman(300f, 380f, -40f)
            crewman(340f, 380f, 40f)
            crewman(700f, 380f, 0f)
        },
        module(
            "ciws", "Point defence", ModuleKind.GUN,
            detail = "Phalanx CIWS and Sea Sparrow",
            info = "A carrier's defence is its air wing and its escorts; the guns on the "
                + "deck edge are the last few seconds of it. Phalanx is a radar-directed "
                + "20 mm Gatling that tracks its own shells onto the target.",
        ) {
            cylinderZ(Role.BODY, 0, 740f, 340f, 15f, -15f, 15f, 12)
            cylinderZ(Role.BODY, 0, 236f, 340f, 15f, -15f, 15f, 12)
        },
        module(
            "steering", "Steering gear", ModuleKind.POWER,
            detail = "Four rudders · hydraulic rams",
            info = "A carrier steers to put wind over the deck as much as to go anywhere, "
                + "so the gear runs constantly while aircraft are flying. Four rams, in "
                + "two compartments, so that flooding one still leaves her steerable.",
        ) {
            box3(Role.BODY, 0, 110f, 380f, -50f, 210f, 440f, 50f)
            cylinderZ(Role.TRIM, 1, 150f, 378f, 22f, -38f, 38f, 12)
        },
    )

    private val history = """
## A movable piece of sovereign territory

A Nimitz-class carrier displaces about 100,000 tonnes, carries 60 to 70 aircraft and about
5,700 people, and needs refuelling roughly once every twenty years. It can be anywhere in
the world within a fortnight and needs nobody's permission to be there. That combination -
not the aircraft, not the tonnage - is what the ship is for.

Ten were built between 1968 and 2009. No other navy has operated anything comparable, and
the cost is why: a carrier is not one ship but a battle group, an air wing, and a training
pipeline measured in decades.

## Three British inventions

Almost everything that makes a modern carrier work came from the Royal Navy in the 1950s,
at a point when Britain could no longer afford to build the ships themselves.

**The angled deck.** Landing along a deck angled about nine degrees off the ship's axis
means a pilot who misses the wires simply flies off over the sea and comes round again.
Before it, a crash barrier was all that stood between a missed approach and the aircraft
parked forward.

**The steam catapult.** A piston under the deck accelerating a 25-tonne aircraft to
270 km/h in two seconds. Without it, jets could not have gone to sea at all.

**The mirror landing aid.** An optical glide path the pilot flies himself, instead of
following the hand signals of a man standing on the deck.

## The most dangerous workplace

Open the ship and the hazards are everywhere: 9 million litres of aviation fuel, deep
magazines of ordnance, jet engines at full power a metre from people working on foot, and
arrestor cables under enormous tension. The 1967 fire aboard USS Forrestal, which killed
134 men, started when a rocket fired accidentally on a crowded deck.

The flight deck of a carrier at work remains one of the most dangerous industrial
environments on earth, run largely by people in their early twenties.

## What is below

Two reactors, four shafts, 260,000 horsepower, and a hangar occupying two-thirds of the
flight deck's area, split into three bays by fireproof doors so one fire cannot take the
ship. The vessel's own fuel is nuclear; every litre of the 9 million aboard is for the
aircraft.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "~5,700 with air wing"),
        Spec("Displacement", "~100,000 t full load"),
        Spec("Length", "333 m"),
        Spec("Flight deck width", "76.8 m"),
        Spec("Reactors", "2 × A4W"),
        Spec("Machinery", "4 shafts, 260,000 shp"),
        Spec("Speed", "30+ knots"),
        Spec("Aircraft", "60-70"),
        Spec("Catapults", "4 steam"),
        Spec("Aviation fuel", "9 million litres"),
        Spec("Built", "10"),
    )

    private val quiz = listOf(
        Question(
            "What problem does the angled deck solve?",
            listOf(
                "It gives a longer landing run",
                "A pilot who misses the arrestor wires flies off over the sea instead of into parked aircraft",
                "It allows launching and landing at once",
                "It reduces the ship's turning circle",
            ),
            1,
            "Before the angled deck, a crash barrier was the only thing between a missed "
                + "approach and the aircraft parked forward. A British invention of 1952, and "
                + "the most important after the catapult.",
        ),
        Question(
            "Why does a carrier need to steam at over 30 knots?",
            listOf(
                "To escape submarines",
                "Turning into wind adds its speed to the airflow over the deck",
                "To keep up with its escorts",
                "To generate power for the catapults",
            ),
            1,
            "Thirty knots of ship speed is thirty knots of extra airflow over the deck, and "
                + "that is the difference between launching a fully loaded aircraft and not.",
        ),
        Question(
            "What is the 9 million litres of fuel aboard for?",
            listOf(
                "The ship's boilers",
                "The aircraft - the ship itself is nuclear powered",
                "The escort ships",
                "Emergency diesel generators",
            ),
            1,
            "The reactors run for about twenty years between refuellings. Every litre aboard "
                + "is JP-5 for the air wing, specified with a high flash point precisely "
                + "because it has to be stored in a ship.",
        ),
        Question(
            "What does a steam catapult actually do to an aircraft?",
            listOf(
                "Pulls it along a rail at constant speed",
                "Accelerates 25 tonnes to 270 km/h in about two seconds - roughly 4g",
                "Launches it vertically off the deck",
                "Provides thrust until the engines light",
            ),
            1,
            "Two seconds from a standing start to flying speed. Without it a carrier can only "
                + "launch light aircraft, and jets could never have gone to sea.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
