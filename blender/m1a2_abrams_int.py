"""
Inside the M1A2 Abrams.

Two things here exist nowhere else in this collection. The engine is a gas turbine - a
helicopter engine in a tank, which is why an Abrams whistles instead of clattering and
why it drinks fuel at idle. And the ammunition lives behind a sliding blast door with
blow-off panels above it, so that when it cooks off the crew are still alive afterwards.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

RING_X = -0.40

MODULES = ("engine", "transmission", "final_drive", "driver", "commander", "gunner",
           "loader", "breech", "ammo_bustle", "ammo_hull", "fuel", "citv", "gps",
           "armour_front", "armour_side",
           "turret_drive", "radio", "nbc")


def build():
    k = intkit.Kit()

    # AGT-1500: 1,500 hp from a gas turbine the size of a desk, with the recuperator
    # that makes it survivable on fuel wrapped round it.
    extras = [
        wf.box("engine", (-2.60, 0.0, 0.92), (1.70, 1.90, 1.00), k.trim),      # recuperator
        wf.cylinder("engine", (-3.42, 0.0, 0.92), 0.46, 0.30, "X", 20, k.dark),  # exhaust
        wf.cylinder("engine", (-1.60, 0.0, 0.92), 0.34, 0.26, "X", 18, k.trim),  # intake
    ]
    k.jet("engine", (-2.55, 0.0, 0.92), length=1.55, radius=0.36, stages=5, extras=extras)

    # X-1100 automatic: four speeds forward, hydrokinetic, and it steers as well.
    k.gearbox("transmission", (-1.30, 0.0, 0.80), (0.80, 1.60, 0.64), bell=0.34)

    parts = []
    for sign in (-1, 1):
        y = sign * 1.20
        parts.append(wf.cylinder("final_drive", (-3.32, y, 0.62), 0.34, 0.40, "Y", 20, k.body))
        parts.append(wf.cylinder("final_drive", (-3.32, y, 0.62), 0.24, 0.44, "Y", 16, k.trim))
        parts.append(wf.cylinder("final_drive", (-3.32, sign * 1.44, 0.62), 0.20, 0.24,
                                 "Y", 14, k.trim))
        parts.append(wf.cylinder("final_drive", (-2.80, y, 0.76), 0.13, 0.60, "Y", 12, k.metal))
    k.done("final_drive", parts)

    # The driver lies back almost flat on the centreline - the only way to fit a man
    # under a glacis that shallow.
    k.seats([("driver", (1.90, 0.0, 0.82), 1.0)], seated=True)
    k.seats([
        ("commander", (RING_X - 0.30, 0.62, 1.78), 1.0),
        ("gunner", (RING_X + 0.30, 0.60, 1.74), 1.0),
        ("loader", (RING_X + 0.10, -0.72, 1.74), -1.0),
    ])

    k.breech("breech", (RING_X + 0.70, 0.0, 1.94), calibre=0.120, house=(0.76, 0.56, 0.62),
             barrel_len=1.00)

    # Bustle ammunition: 34 rounds behind a sliding blast door, with the blow-off panels
    # in the roof above them. The panels are the point - they are deliberately the
    # weakest thing on the tank so that a fire goes up and out instead of down and in.
    bustle = []
    for row in range(2):
        bustle += wf.rack("ammo_bustle", (RING_X - 1.40 - row * 0.34, -0.86, 1.96), 8,
                          (0.0, 0.245, 0.0), 0.120, 0.98, case=k.body, tip=k.trim)
    bustle.append(wf.box("ammo_bustle", (RING_X - 0.98, 0.0, 1.96), (0.10, 1.90, 0.90),
                         k.metal))          # the blast door
    for dy in (-0.80, 0.80):
        bustle.append(wf.box("ammo_bustle", (RING_X - 1.56, dy, 2.44), (1.70, 0.70, 0.06),
                             k.dark))       # blow-off panels
    k.done("ammo_bustle", bustle, bevel=0.006, segments=1)

    k.rounds("ammo_hull", [
        ((0.70, -0.24, 0.76), 3, (0.0, 0.24, 0.0), 2, (-0.30, 0.0, 0.0)),
    ], 0.120, 0.98,
        frames=[((0.55, 0.0, 0.24), (0.70, 0.80, 0.06)),
                ((0.55, 0.0, 1.28), (0.10, 0.80, 1.00))])

    k.box_module("fuel", [
        ((2.40, s * 1.10, 0.80), (1.90, 0.60, 0.70)) for s in (-1, 1)
    ] + [((-0.30, s * 1.20, 0.80), (1.60, 0.44, 0.66)) for s in (-1, 1)],
        cylinders=[((2.40, s * 1.10, 1.20), 0.08, 0.12, "Z") for s in (-1, 1)])

    k.optics("citv", scopes=[((RING_X + 0.40, 0.62, 2.80), 0.13, 0.34, "X")],
             blocks=[((RING_X + 0.58, 0.62, 2.80), (0.05, 0.28, 0.22))])
    k.optics("gps", scopes=[((RING_X + 0.86, -0.58, 2.44), 0.16, 0.50, "X")],
             blocks=[((RING_X + 1.12, -0.58, 2.44), (0.05, 0.40, 0.22))])

    parts = [
        wf.box("turret_drive", (RING_X - 0.30, 0.0, 1.28), (0.80, 0.90, 0.42), k.body),
        wf.cylinder("turret_drive", (RING_X - 0.30, 0.0, 1.58), 0.30, 0.26, "Z", 16, k.trim),
        wf.cylinder("turret_drive", (RING_X, 0.0, 1.58), 1.10, 0.08, "Z", 30, k.trim),
    ]
    for i in range(30):
        a = 2 * math.pi * i / 30
        parts.append(wf.box("turret_drive",
                            (RING_X + 1.10 * math.cos(a), 1.10 * math.sin(a), 1.58),
                            (0.05, 0.05, 0.07), k.trim))
    k.done("turret_drive", parts, bevel=0.008, segments=1)

    k.box_module("radio", [((RING_X - 1.90, 0.0, 2.10), (0.70, 1.30, 0.46)),
                           ((RING_X - 1.90, 0.0, 1.60), (0.70, 1.30, 0.40))],
                 cylinders=[((RING_X - 1.54, 0.0, 2.10), 0.05, 0.08, "X")])

    # Positive pressure: the blower and its filter bank, sized to push more air in than
    # can leak out anywhere.
    parts = [
        wf.box("nbc", (-1.10, 0.90, 1.16), (0.90, 0.60, 0.56), k.body),
        wf.cylinder("nbc", (-0.62, 0.90, 1.16), 0.22, 0.30, "X", 16, k.trim),
        wf.cylinder("nbc", (-1.70, 0.90, 1.16), 0.16, 0.40, "X", 14, k.dark),
    ]
    for i in range(4):
        parts.append(wf.box("nbc", (-1.10, 0.90, 1.16), (0.16, 0.56, 0.52), k.trim))
        wf.rotate(parts[-1], y=0)
    k.done("nbc", parts, bevel=0.012)

    # Chobham: layers, not a plate - steel over ceramic over steel, arranged so a shaped
    # charge jet is broken up rather than resisted. The thickness is the giveaway.
    front = []
    for i, (off, thick, mat) in enumerate(((0.00, 0.06, k.body), (0.14, 0.18, k.trim),
                                           (0.36, 0.06, k.body))):
        front.append(wf.box("armour_front", (RING_X + 1.90 - off, 0.0, 1.96),
                            (thick, 2.20, 0.86), mat))
    front.append(wf.box("armour_front", (1.60, 0.0, 1.20), (0.10, 2.90, 0.90), k.body))
    wf.rotate(front[-1], y=-82)
    k.done("armour_front", front, bevel=0.006, segments=1)

    k.plates("armour_side", [
        ((0.0, s * 1.80, 0.76), (5.40, 0.070, 0.78), 0) for s in (-1, 1)
    ] + [((RING_X - 0.40, s * 1.66, 1.96), (3.60, 0.050, 0.84), 0) for s in (-1, 1)])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=8.2, floor=-0.2)
