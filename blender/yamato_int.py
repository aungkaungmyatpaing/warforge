"""
Inside Yamato.

Three 46 cm turrets, each weighing 2,700 tonnes - more than a whole destroyer - standing
on barbettes that reach down past the armoured deck to magazines at the very bottom of
the ship. The armour scheme is "all or nothing": everything that matters is inside one
enormous armoured box and everything outside it is unprotected, because armour thin
enough to spread everywhere is armour that stops nothing.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 131.5, -131.5
KEEL, DECK = -10.40, 9.80

MODULES = ("turbines", "boilers", "turret_a", "magazine", "pagoda", "rangefinder",
           "armour_belt", "fuel", "aa", "torpedo_bulge",
           "steering")


def deck_at(x):
    t = x / BOW
    if t >= 0:
        return DECK + 4.4 * t ** 2.4
    return DECK - 3.2 * (-t) ** 1.5


def build():
    k = intkit.Kit()

    parts = []
    for x in (-28.0, -48.0):
        for sign, dy in ((-1, 12.0), (1, 12.0), (-1, 5.0), (1, 5.0)):
            parts += wf.steam_turbine("turbines", (x, sign * dy, -4.0), (16.0, 6.0, 5.4),
                                      block=k.body, shaft=k.metal)
    k.done("turbines", parts, bevel=0.08)

    # Twelve Kampon boilers in four rooms. 150,000 shaft horsepower to move 72,000 tonnes
    # at 27 knots - which is slow, and was accepted because the guns came first.
    parts = []
    for x in (0.0, -14.0):
        for sign in (-1, 1):
            for dy in (6.0, 14.0):
                parts += wf.boiler("boilers", (x, sign * dy, -2.6), (12.0, 6.0, 8.0),
                                   block=k.body, tube=k.trim)
    k.done("boilers", parts, bevel=0.08)

    # One turret, drawn all the way to the bottom of the ship. The barbette is 560 mm
    # thick and the hoist inside it is the only way the shells get up.
    parts = [
        wf.box("turret_a", (83.2, 0.0, deck_at(83.2) + 3.6), (20.0, 19.0, 6.2), k.body),
        wf.cylinder("turret_a", (83.2, 0.0, 1.0), 8.8, 16.0, "Z", 26, k.trim),
        wf.cylinder("turret_a", (83.2, 0.0, -7.0), 7.0, 5.0, "Z", 22, k.body),
    ]
    for dy in (-5.3, 0.0, 5.3):
        parts.append(wf.cylinder("turret_a", (83.2, dy, -1.0), 1.1, 20.0, "Z", 12, k.dark))
        parts.append(wf.cylinder("turret_a", (95.0, dy, deck_at(83.2) + 3.8), 0.86, 20.0,
                                 "X", 14, k.metal))
    k.done("turret_a", parts, bevel=0.20)

    parts = []
    for x in (83.2, 58.1, -89.1):
        parts.append(wf.box("magazine", (x, 0.0, -8.4), (16.0, 22.0, 4.4), k.body))
        for row in range(4):
            parts += wf.rack("magazine", (x - 6.0 + row * 4.0, -8.0, -9.2), 8,
                             (0.0, 2.3, 0.0), 0.460, 2.00, case=k.trim, tip=k.dark)
    k.done("magazine", parts, bevel=0.05, segments=1)

    # The pagoda is a column of fire-control positions stacked on one armoured tube: the
    # tube carries the wiring and the men, and everything hung off it is just plating.
    parts = [wf.cylinder("pagoda", (-2.2, 0.0, deck_at(-7.5) + 12.0), 2.6, 26.0, "Z", 20,
                         k.body)]
    parts += [p for c in ((-4.0, -3.0, deck_at(-7.5) + 4.0), (-4.0, 3.0, deck_at(-7.5) + 4.0),
                          (-2.0, 0.0, deck_at(-7.5) + 12.0), (-2.2, -2.0, deck_at(-7.5) + 20.0),
                          (-2.2, 2.0, deck_at(-7.5) + 20.0))
              for p in wf.figure("pagoda", c, facing=1.0, seated=False,
                                 body=k.trim, kit=k.dark, helmet=k.dark)]
    k.done("pagoda", parts, bevel=0.10)

    # The 15-metre rangefinder: the largest optical instrument ever put to sea, and the
    # thing radar made obsolete within four years of her commissioning.
    parts = [
        wf.cylinder("rangefinder", (-2.2, 0.0, deck_at(-7.5) + 27.6), 0.9, 15.2, "Y", 18,
                    k.body),
        wf.box("rangefinder", (-2.2, 0.0, deck_at(-7.5) + 27.6), (2.6, 3.0, 2.2), k.trim),
    ]
    for sign in (-1, 1):
        parts.append(wf.cylinder("rangefinder", (-2.2, sign * 7.6, deck_at(-7.5) + 27.6),
                                 0.8, 0.4, "Y", 14, k.glass))
    k.done("rangefinder", parts, bevel=0.08)

    # 410 mm belt, inclined 20 degrees, and a 200 mm armoured deck over it. The belt is
    # only as long as the magazines and machinery - all or nothing.
    parts = []
    for sign in (-1, 1):
        belt = wf.box("armour_belt", (0.0, sign * 17.0, -1.0), (128.0, 0.410, 7.0), k.body)
        wf.rotate(belt, x=sign * 20)
        parts.append(belt)
    parts.append(wf.box("armour_belt", (0.0, 0.0, 4.0), (128.0, 32.0, 0.200), k.trim))
    for x in (96.0, -96.0):
        parts.append(wf.box("armour_belt", (x, 0.0, -1.0), (0.300, 30.0, 7.0), k.body))
    k.done("armour_belt", parts, bevel=0.04, segments=1)

    k.box_module("fuel", [((-20.0, s * 15.0, -6.0), (80.0, 5.0, 6.0)) for s in (-1, 1)]
                 + [((40.0, s * 12.0, -6.4), (40.0, 4.0, 5.0)) for s in (-1, 1)],
                 cylinders=[((-20.0, s * 15.0, -2.4), 0.30, 0.60, "Z") for s in (-1, 1)])

    parts = []
    for x in (-18.0, -45.0, -62.0, 8.0):
        for sign in (-1, 1):
            y = sign * 12.0
            z = deck_at(x) + 7.4
            parts.append(wf.box("aa", (x, y, z + 0.7), (2.2, 2.6, 1.4), k.body))
            for dy in (-0.7, 0.0, 0.7):
                parts.append(wf.cylinder("aa", (x + 2.0, y + dy, z + 1.6), 0.13, 3.4,
                                         "X", 8, k.metal))
            parts.append(wf.cylinder("aa", (x, y, z - 1.2), 3.0, 2.4, "Z", 16, k.trim))
    k.done("aa", parts, bevel=0.06)

    # Two rudders in line, one behind the other - which is what turns a 263-metre ship
    # in a tighter circle than anything else her size could manage.
    parts = [
        wf.box("steering", (-108.0, 0.0, -6.0), (16.0, 14.0, 5.0), k.body),
        wf.cylinder("steering", (-112.0, 0.0, -4.0), 1.6, 9.0, "Z", 18, k.trim),
        wf.cylinder("steering", (-122.0, 0.0, -4.4), 1.1, 8.0, "Z", 16, k.trim),
        wf.cylinder("steering", (-102.0, 0.0, -6.0), 2.2, 5.0, "X", 18, k.dark),
    ]
    k.done("steering", parts, bevel=0.08)

    # The anti-torpedo bulge: a void space outboard of the belt that lets a warhead
    # expand into nothing before it reaches anything that matters.
    parts = []
    for sign in (-1, 1):
        parts.append(wf.box("torpedo_bulge", (0.0, sign * 18.6, -5.4), (150.0, 2.6, 9.0),
                            k.body))
        for i in range(14):
            parts.append(wf.box("torpedo_bulge", (-70.0 + i * 10.5, sign * 18.6, -5.4),
                                (0.4, 2.8, 9.2), k.trim))
    k.done("torpedo_bulge", parts, bevel=0.06, segments=1)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=140.0, floor=-11.0, distance=4.0)
