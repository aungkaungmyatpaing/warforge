"""
Inside Admiral Graf Spee.

Eight diesels, and that is the whole ship. Every other capital ship of the period burns
oil in boilers and makes steam; diesels are heavier per horsepower but they use half the
fuel, and half the fuel is 16,000 miles of range - which is what lets a single ship go
raiding in the South Atlantic for months with no base to come home to.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 93.0, -93.0
KEEL, DECK = -7.40, 7.40

MODULES = ("diesels", "turret_fwd", "magazine", "fuel", "radar", "bridge",
           "aircraft", "armour_belt", "secondary",
           "steering")


def deck_at(x):
    t = x / BOW
    if t >= 0:
        return DECK + 3.2 * t ** 2.4
    return DECK - 1.8 * (-t) ** 1.6


def build():
    k = intkit.Kit()

    # Eight MAN double-acting two-strokes, four to a shaft through a hydraulic coupling.
    # They shook the ship badly enough that the fire control had trouble at full speed.
    parts = []
    for i, x in enumerate((-6.0, -14.0, -22.0, -30.0)):
        for sign in (-1, 1):
            parts += wf.piston_engine("diesels", (x, sign * 4.6, -2.8), cylinders=9,
                                      banks=1, bore=0.42, block=k.body, head=k.trim,
                                      pipe=k.dark)
    for sign in (-1, 1):
        parts.append(wf.cylinder("diesels", (-46.0, sign * 4.2, -3.4), 1.2, 22.0, "X",
                                 16, k.metal))
        parts.append(wf.cylinder("diesels", (-34.0, sign * 4.2, -3.4), 2.4, 3.0, "X",
                                 18, k.dark))
    k.done("diesels", parts, bevel=0.05)

    # A triple 28 cm turret, drawn down to its magazine.
    parts = [
        wf.box("turret_fwd", (60.0, 0.0, deck_at(60.0) + 2.6), (13.0, 11.0, 4.2), k.body),
        wf.cylinder("turret_fwd", (60.0, 0.0, 2.0), 5.6, 9.0, "Z", 24, k.trim),
        wf.cylinder("turret_fwd", (60.0, 0.0, -3.4), 4.4, 4.0, "Z", 20, k.body),
    ]
    for dy in (-3.2, 0.0, 3.2):
        parts.append(wf.cylinder("turret_fwd", (60.0, dy, 0.0), 0.7, 13.0, "Z", 12, k.dark))
        parts.append(wf.cylinder("turret_fwd", (67.0, dy, deck_at(60.0) + 2.8), 0.42, 12.0,
                                 "X", 14, k.metal))
    k.done("turret_fwd", parts, bevel=0.12)

    parts = []
    for x in (60.0, -62.0):
        parts.append(wf.box("magazine", (x, 0.0, -5.2), (11.0, 14.0, 3.6), k.body))
        for row in range(3):
            parts += wf.rack("magazine", (x - 3.6 + row * 3.6, -5.0, -5.8), 6,
                             (0.0, 2.0, 0.0), 0.283, 1.10, case=k.trim, tip=k.dark)
    k.done("magazine", parts, bevel=0.04, segments=1)

    k.box_module("fuel", [((-16.0, s * 8.6, -4.0), (44.0, 3.4, 5.0)) for s in (-1, 1)]
                 + [((30.0, s * 7.0, -4.4), (24.0, 3.0, 4.2)) for s in (-1, 1)],
                 cylinders=[((-16.0, s * 8.6, -1.2), 0.22, 0.50, "Z") for s in (-1, 1)])

    # FuMO 22 Seetakt - the first radar ever carried to sea in action, and its
    # transmitter room directly below the aerial.
    parts = [
        wf.box("radar", (14.2, 0.0, deck_at(10.0) + 24.0), (0.32, 6.0, 2.2), k.body),
        wf.box("radar", (12.0, 0.0, deck_at(10.0) + 8.0), (2.6, 3.0, 2.4), k.trim),
    ]
    for i in range(6):
        parts.append(wf.box("radar", (14.4, -2.4 + i * 0.96, deck_at(10.0) + 24.0),
                            (0.12, 0.10, 2.0), k.metal))
    k.done("radar", parts, bevel=0.04)

    k.crowd("bridge", [(12.0, -2.0, deck_at(12.0) + 16.0), (12.0, 2.0, deck_at(12.0) + 16.0),
                       (14.0, 0.0, deck_at(12.0) + 16.0), (10.0, -3.2, deck_at(12.0) + 16.0),
                       (10.0, 3.2, deck_at(12.0) + 16.0)], seated=False)

    # The Arado 196 on its catapult: two crew, a 500 km search radius, and the only way
    # a lone raider finds anything in an ocean.
    parts = [
        wf.box("aircraft", (-34.0, 0.0, deck_at(-34.0) + 10.6), (10.0, 1.4, 1.2), k.body),
        wf.box("aircraft", (-34.0, 0.0, deck_at(-34.0) + 10.8), (1.6, 11.0, 0.30), k.trim),
        wf.cylinder("aircraft", (-29.6, 0.0, deck_at(-34.0) + 10.8), 0.7, 1.6, "X", 16,
                    k.dark),
    ]
    for sign in (-1, 1):
        parts.append(wf.box("aircraft", (-35.0, sign * 2.2, deck_at(-34.0) + 9.4),
                            (4.0, 1.0, 1.0), k.trim))
    k.done("aircraft", parts, bevel=0.08)

    parts = [
        wf.box("steering", (-80.0, 0.0, -4.4), (9.0, 8.0, 3.6), k.body),
        wf.cylinder("steering", (-82.0, 0.0, -3.4), 1.0, 6.0, "Z", 16, k.trim),
        wf.cylinder("steering", (-76.0, 0.0, -4.4), 1.5, 3.4, "X", 16, k.dark),
        wf.box("steering", (-82.0, 0.0, -1.6), (5.4, 0.9, 0.9), k.metal),
    ]
    k.done("steering", parts, bevel=0.06)

    # 80 mm belt: enough to keep out a 6-inch cruiser shell and nothing heavier, which
    # is the bargain a "pocket battleship" makes.
    parts = []
    for sign in (-1, 1):
        parts.append(wf.box("armour_belt", (0.0, sign * 10.5, -0.4), (110.0, 0.080, 5.4),
                            k.body))
        parts.append(wf.box("armour_belt", (0.0, sign * 6.0, -2.4), (110.0, 0.040, 4.0),
                            k.trim))
    parts.append(wf.box("armour_belt", (0.0, 0.0, 2.2), (110.0, 20.0, 0.045), k.trim))
    k.done("armour_belt", parts, bevel=0.02, segments=1)

    parts = []
    for x in (36.0, 24.0, -36.0, -48.0):
        for sign in (-1, 1):
            y = sign * 8.0
            z = deck_at(x) + 3.0
            parts.append(wf.box("secondary", (x, y, z), (3.0, 3.2, 2.4), k.body))
            parts.append(wf.cylinder("secondary", (x + (4.0 if x > 0 else -4.0), y, z),
                                     0.22, 5.0, "X", 12, k.metal))
            parts.append(wf.box("secondary", (x, y, z - 3.4), (2.4, 2.6, 3.0), k.trim))
    k.done("secondary", parts, bevel=0.08)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=100.0, floor=-8.0, distance=3.0)
