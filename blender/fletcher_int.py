"""
Inside a Fletcher.

Machinery arranged on the unit system: boiler room, engine room, boiler room, engine
room, so that one torpedo cannot take out both halves of the plant. It is why the two
funnels are so far apart, and it is the single most important thing a destroyer designer
can get right.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 57.3, -57.3
KEEL, DECK = -5.30, 4.10

MODULES = ("turbines", "boilers", "guns", "director", "torpedoes", "sonar",
           "magazine", "fuel", "bridge", "aa",
           "steering")


def deck_at(x):
    t = x / BOW
    if t >= 0:
        return DECK + 2.6 * t ** 2.2
    return DECK - 0.6 * (-t) ** 1.6


def build():
    k = intkit.Kit()

    # Geared turbines: HP and LP through a reduction gear, so the turbine can run fast
    # and the propeller slowly. That gearing is what a Dreadnought did not have.
    parts = []
    for x in (-10.0, -26.0):
        for sign in (-1, 1):
            parts += wf.steam_turbine("turbines", (x, sign * 2.2, -2.2), (7.0, 3.0, 3.0),
                                      block=k.body, shaft=k.metal)
    for sign in (-1, 1):
        parts.append(wf.cylinder("turbines", (-40.0, sign * 2.2, -3.0), 0.34, 18.0, "X",
                                 14, k.metal))
    k.done("turbines", parts, bevel=0.04)

    # Four boilers, 600 psi superheated - a pressure nobody had used at sea before and
    # the reason a Fletcher makes 36 knots on 60,000 horsepower.
    parts = []
    for x in (-2.0, -18.0):
        for sign in (-1, 1):
            parts += wf.boiler("boilers", (x, sign * 2.6, -1.6), (6.0, 3.6, 5.2),
                               block=k.body, tube=k.trim)
            parts.append(wf.cylinder("boilers", (x, sign * 2.6, deck_at(x) + 3.0), 0.9,
                                     6.0, "Z", 14, k.dark))
    k.done("boilers", parts, bevel=0.04)

    # Five 5-inch/38 mounts, each drawn down to its handling room.
    parts = []
    for x, face in ((37.6, 1.0), (27.2, 1.0), (-31.0, -1.0), (-41.4, -1.0)):
        base = deck_at(x) + (2.8 if abs(x) < 35 else 0.4)
        parts.append(wf.box("guns", (x, 0.0, base + 1.4), (4.6, 3.8, 2.6), k.body))
        parts.append(wf.cylinder("guns", (x, 0.0, base - 1.2), 1.9, 3.0, "Z", 18, k.trim))
        parts.append(wf.cylinder("guns", (x, 0.0, -2.6), 1.5, 3.0, "Z", 16, k.body))
        parts.append(wf.cylinder("guns", (x + face * 4.4, 0.0, base + 1.4), 0.18, 4.4,
                                 "X", 12, k.metal))
    k.done("guns", parts, bevel=0.08)

    # Mk 37 director and the Mk 1 computer below it - an analogue machine of gears and
    # cams that solves the gunnery problem continuously while the ship manoeuvres.
    parts = [
        wf.box("director", (9.4, 0.0, deck_at(9.4) + 8.4), (3.6, 3.4, 2.1), k.body),
        wf.cylinder("director", (9.4, 0.0, deck_at(9.4) + 9.8), 1.5, 0.24, "Z", 18, k.metal),
        wf.box("director", (8.0, 0.0, -1.8), (3.2, 3.0, 2.2), k.trim),
    ]
    for i in range(4):
        parts.append(wf.box("director", (6.8 + i * 0.8, 0.0, -1.8), (0.3, 2.6, 1.8), k.dark))
    k.done("director", parts, bevel=0.06)

    parts = []
    for x in (-5.5, -15.9):
        base = deck_at(x) + 3.3
        for i in range(5):
            dy = (i - 2) * 0.82
            parts.append(wf.cylinder("torpedoes", (x, dy, base + 0.9), 0.267, 7.0, "X",
                                     14, k.body))
            parts.append(wf.cone("torpedoes", (x + 3.3, dy, base + 0.9), 0.267, 0.10, 0.9,
                                 "X", 12, k.trim))
        parts.append(wf.cylinder("torpedoes", (x, 0.0, base - 0.3), 1.6, 0.6, "Z", 16,
                                 k.trim))
    k.done("torpedoes", parts, bevel=0.03)

    # QCJ sonar in its retractable dome under the forefoot, and the stack that drives it.
    parts = [
        wf.sphere("sonar", (30.0, 0.0, -5.4), 1.1, 18, k.body),
        wf.cylinder("sonar", (30.0, 0.0, -3.4), 0.5, 3.0, "Z", 14, k.trim),
        wf.box("sonar", (26.0, 0.0, -1.0), (3.0, 2.4, 2.2), k.trim),
    ]
    k.done("sonar", parts, bevel=0.06)

    parts = []
    for x in (33.0, -36.0):
        parts.append(wf.box("magazine", (x, 0.0, -3.6), (6.0, 6.0, 2.8), k.body))
        for row in range(3):
            parts += wf.rack("magazine", (x - 2.0 + row * 2.0, -2.0, -4.0), 5,
                             (0.0, 1.0, 0.0), 0.127, 0.70, case=k.trim, tip=k.dark)
    k.done("magazine", parts, bevel=0.03, segments=1)

    k.box_module("fuel", [((-14.0, s * 4.4, -3.4), (46.0, 1.8, 3.2)) for s in (-1, 1)]
                 + [((22.0, 0.0, -3.8), (16.0, 6.0, 2.4))],
                 cylinders=[((-14.0, s * 4.4, -1.6), 0.16, 0.40, "Z") for s in (-1, 1)])

    parts = [
        wf.box("steering", (-48.0, 0.0, -2.6), (5.0, 4.4, 2.4), k.body),
        wf.cylinder("steering", (-50.0, 0.0, -1.8), 0.5, 3.4, "Z", 14, k.trim),
        wf.cylinder("steering", (-45.6, 0.0, -2.6), 0.8, 2.0, "X", 14, k.dark),
        wf.box("steering", (-50.0, 0.0, -0.8), (3.0, 0.5, 0.5), k.metal),
    ]
    k.done("steering", parts, bevel=0.05)

    k.crowd("bridge", [(12.4, -1.4, deck_at(12.4) + 7.4), (12.4, 1.4, deck_at(12.4) + 7.4),
                       (14.0, 0.0, deck_at(12.4) + 7.4), (10.6, -2.2, deck_at(12.4) + 7.4),
                       (10.6, 2.2, deck_at(12.4) + 7.4)], seated=False)

    parts = []
    for x, z in ((-29.6, deck_at(-29.6) + 3.4), (17.0, deck_at(17.0) + 5.6)):
        parts.append(wf.box("aa", (x, 0.0, z + 0.7), (1.8, 2.2, 1.1), k.body))
        for dy in (-0.45, 0.45):
            parts.append(wf.cylinder("aa", (x + 2.0, dy, z + 1.0), 0.10, 3.0, "X", 10,
                                     k.metal))
        parts.append(wf.box("aa", (x, 0.0, z - 1.8), (2.0, 2.4, 2.0), k.trim))
    for x in (-20.0, -8.0, 22.0):
        for sign in (-1, 1):
            y = sign * 4.8
            parts.append(wf.cylinder("aa", (x, y, deck_at(x) + 1.0), 0.09, 2.0, "X", 8,
                                     k.metal))
            parts.append(wf.cylinder("aa", (x - 0.6, y, deck_at(x) + 0.8), 0.30, 0.50,
                                     "Z", 12, k.trim))
    k.done("aa", parts, bevel=0.05)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=60.0, floor=-6.0, distance=2.0)
