"""
Inside USS Nimitz.

Two reactors drive four shafts and, just as importantly, make the fresh water and the
steam for the catapults. Everything else below the flight deck is arranged round two
volumes: a hangar two decks high in the middle of the ship, and nine million litres of
jet fuel kept as far from it as the hull allows.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 166.4, -166.4
KEEL, DECK, HANGAR = -11.30, 20.20, 8.00

MODULES = ("reactors", "turbines", "hangar", "catapults", "arrestor", "angled_deck",
           "island", "avgas", "magazine", "crew", "ciws",
           "steering")


def build():
    k = intkit.Kit()

    # Two A4W reactors, each in its own shielded compartment, far enough apart that one
    # hit cannot reach both.
    parts = []
    for x in (14.0, -22.0):
        parts.append(wf.cylinder("reactors", (x, 0.0, -3.0), 3.4, 9.0, "Z", 24, k.body))
        parts.append(wf.cylinder("reactors", (x, 0.0, -3.0), 5.0, 10.0, "Z", 26, k.trim))
        parts.append(wf.box("reactors", (x, 0.0, -3.0), (16.0, 16.0, 14.0), k.dark))
        for sign in (-1, 1):
            parts.append(wf.cylinder("reactors", (x, sign * 5.0, 2.6), 1.5, 6.0, "Z", 16,
                                     k.body))
    k.done("reactors", parts, bevel=0.14)

    parts = []
    for x in (-2.0, -40.0):
        for sign, dy in ((-1, 13.0), (1, 13.0), (-1, 5.0), (1, 5.0)):
            parts += wf.steam_turbine("turbines", (x, sign * dy, -3.4), (18.0, 6.0, 5.6),
                                      block=k.body, shaft=k.metal)
    for sign, dy in ((-1, 13.0), (1, 13.0), (-1, 5.0), (1, 5.0)):
        parts.append(wf.cylinder("turbines", (-100.0, sign * dy, -5.0), 1.0, 70.0, "X",
                                 16, k.metal))
    k.done("turbines", parts, bevel=0.12)

    # The hangar: 208 metres long, two decks high, and split by three steel fire doors
    # that can drop in seconds.
    parts = [wf.box("hangar", (-6.0, 0.0, 13.4), (208.0, 33.0, 8.0), k.body)]
    for x in (56.0, -14.0, -84.0):
        parts.append(wf.box("hangar", (x, 0.0, 13.4), (1.2, 33.0, 8.0), k.trim))
    for i in range(9):
        x = 78.0 - i * 20.0
        for sign in (-1, 1):
            parts.append(wf.box("hangar", (x, sign * 9.0, 11.4), (14.0, 12.0, 3.4), k.dark))
    k.done("hangar", parts, bevel=0.16)

    # Four steam catapults: a pair of slotted cylinders under the deck, fed by the
    # reactors, that throw 30 tonnes to 150 knots in two seconds.
    parts = []
    for x, y, ang in ((110.0, -13.0, 0.0), (110.0, 3.0, 0.0),
                      (-52.0, -20.0, 9.0), (-32.0, -26.0, 9.0)):
        for dy in (-0.9, 0.9):
            tube = wf.cylinder("catapults", (x, y + dy, DECK - 1.8), 0.55, 94.0, "X", 16,
                               k.body)
            if ang:
                wf.rotate(tube, z=ang)
            parts.append(tube)
        trough = wf.box("catapults", (x, y, DECK - 2.6), (94.0, 3.2, 1.4), k.trim)
        if ang:
            wf.rotate(trough, z=ang)
        parts.append(trough)
        parts.append(wf.box("catapults", (x - 48.0, y, DECK - 3.4), (8.0, 5.0, 3.0), k.dark))
    k.done("catapults", parts, bevel=0.10)

    # Four arrestor wires and the hydraulic engines below them, which take the same
    # energy out again in sixty metres.
    parts = []
    for i in range(4):
        x = -95.0 + i * 14.0
        wire = wf.cylinder("arrestor", (x, -8.0, DECK + 0.1), 0.06, 34.0, "Y", 10, k.metal)
        wf.rotate(wire, z=9)
        parts.append(wire)
        parts.append(wf.cylinder("arrestor", (x, -8.0, DECK - 3.0), 1.1, 12.0, "Y", 16,
                                 k.body))
        parts.append(wf.box("arrestor", (x, -8.0, DECK - 4.6), (4.0, 14.0, 2.0), k.trim))
    k.done("arrestor", parts, bevel=0.08)

    # The angled deck itself: eight degrees off the centreline, so an aircraft that
    # misses the wires flies off the side and goes round again instead of hitting the
    # aircraft parked forward. It is the single best idea in carrier aviation.
    parts = []
    strip = wf.box("angled_deck", (-40.0, -8.0, DECK - 0.6), (180.0, 16.0, 1.0), k.body)
    wf.rotate(strip, z=9)
    parts.append(strip)
    for i in range(6):
        rib = wf.box("angled_deck", (-110.0 + i * 30.0, -8.0, DECK - 1.4), (3.0, 17.0, 1.6),
                     k.trim)
        wf.rotate(rib, z=9)
        parts.append(rib)
    k.done("angled_deck", parts, bevel=0.10)

    parts = []
    for c in ((-8.0, 24.0, DECK + 6.0), (-8.0, 27.0, DECK + 6.0), (-12.0, 25.0, DECK + 10.0),
              (-6.0, 26.0, DECK + 14.0), (-14.0, 26.0, DECK + 3.0)):
        parts += wf.figure("island", c, facing=-1.0, seated=False,
                           body=k.trim, kit=k.dark, helmet=k.dark)
    parts.append(wf.box("island", (-8.0, 26.0, DECK + 8.0), (30.0, 9.0, 16.0), k.body))
    k.done("island", parts, bevel=0.14)

    # Jet fuel: nine million litres, in tanks pushed out to the hull sides where a
    # torpedo reaches them before it reaches the reactors.
    k.box_module("avgas", [((x, s * 17.0, -4.0), (40.0, 5.0, 9.0))
                           for x in (60.0, 20.0, -60.0) for s in (-1, 1)],
                 cylinders=[((60.0, s * 17.0, 1.0), 0.40, 0.80, "Z") for s in (-1, 1)])

    parts = []
    for x in (76.0, -70.0):
        parts.append(wf.box("magazine", (x, 0.0, -6.0), (26.0, 20.0, 6.0), k.body))
        for row in range(4):
            for i in range(5):
                parts.append(wf.cylinder("magazine", (x - 9.0 + row * 6.0, -6.0 + i * 3.0,
                                                      -6.0), 0.25, 3.6, "X", 12, k.trim))
    k.done("magazine", parts, bevel=0.06, segments=1)

    k.crowd("crew", [(40.0, 6.0, 11.0), (40.0, -6.0, 11.0), (10.0, 10.0, 11.0),
                     (-30.0, 8.0, 11.0), (-60.0, -8.0, 11.0), (70.0, 0.0, 11.0),
                     (20.0, 0.0, -2.0), (-20.0, 12.0, -2.0)], seated=False)

    # Four rudders on hydraulic rams, in two separate compartments so that flooding one
    # still leaves the ship steerable.
    parts = []
    for sign in (-1, 1):
        parts.append(wf.box("steering", (-140.0, sign * 9.0, -5.0), (18.0, 12.0, 6.0),
                            k.body))
        parts.append(wf.cylinder("steering", (-146.0, sign * 9.0, -3.0), 1.6, 10.0, "Z",
                                 18, k.trim))
        for dz in (-1.6, 1.6):
            parts.append(wf.cylinder("steering", (-138.0, sign * 9.0, -5.0 + dz), 0.8, 9.0,
                                     "X", 14, k.dark))
    k.done("steering", parts, bevel=0.10)

    parts = []
    for x, sign in ((-130.0, -1), (-130.0, 1), (130.0, -1), (60.0, 1)):
        y = sign * 24.0
        z = DECK - 2.0
        parts.append(wf.cylinder("ciws", (x, y, z), 1.2, 2.2, "Z", 16, k.body))
        parts.append(wf.sphere("ciws", (x, y, z + 2.0), 1.1, 16, k.trim))
        gun = wf.cylinder("ciws", (x + sign * 0.4, y, z + 1.2), 0.34, 2.2, "X", 12, k.dark)
        wf.rotate(gun, y=-20)
        parts.append(gun)
    k.done("ciws", parts, bevel=0.08)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=180.0, floor=-12.0, distance=5.0)
