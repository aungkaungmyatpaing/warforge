"""
Inside the Centurion Mk 3.

A Rolls-Royce Meteor: a Merlin with the supercharger taken off and detuned to run on
tank petrol. 650 hp in a hull designed to carry armour and a gun without choosing between
them - which is what the word "main battle tank" was invented to describe.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

RING_X = -0.30

MODULES = ("engine", "cooling", "transmission", "suspension", "driver", "commander",
           "gunner", "loader", "breech", "ammo_hull", "fuel", "optics", "armour_front",
           "turret_drive", "radio", "fire_suppression")


def build():
    k = intkit.Kit()

    k.piston("engine", (-2.10, 0.0, 0.90), cylinders=12, vee=60.0, banks=2, bore=0.137,
             extras=[
                 wf.cylinder("engine", (-2.94, 0.0, 0.90), 0.32, 0.24, "X", 18, k.dark),
                 wf.box("engine", (-2.10, 0.0, 1.32), (1.00, 0.34, 0.22), k.trim),
                 wf.cylinder("engine", (-1.30, 0.0, 1.10), 0.15, 0.34, "X", 14, k.trim),
             ])

    parts = []
    for sign in (-1, 1):
        y = sign * 1.05
        parts.append(wf.box("cooling", (-2.10, y, 1.00), (1.30, 0.18, 0.76), k.body))
        for i in range(9):
            parts.append(wf.box("cooling", (-2.68 + i * 0.145, y, 1.00), (0.05, 0.20, 0.70),
                                k.trim))
        parts.append(wf.cylinder("cooling", (-1.34, y, 1.00), 0.26, 0.12, "X", 16, k.dark))
    k.done("cooling", parts, bevel=0.008, segments=1)

    # Merritt-Brown: the gearbox that lets a tank pivot on the spot, because the two
    # tracks are geared together rather than braked apart.
    k.gearbox("transmission", (-3.24, 0.0, 0.86), (0.70, 1.50, 0.62), bell=0.30,
              brakes=[((-3.24, s * 0.94, 0.86), 0.26, 0.26, "Y") for s in (-1, 1)])

    # Horstmann bogies: two wheels on a horizontal coil spring, bolted to the outside of
    # the hull, so a damaged unit could be unbolted and replaced in the field.
    parts = []
    for bx in (-2.30, -0.30, 1.70):
        for sign in (-1, 1):
            y = sign * 1.14
            parts.append(wf.box("suspension", (bx, y, 0.66), (1.10, 0.24, 0.28), k.body))
            parts.append(wf.cylinder("suspension", (bx, y, 0.66), 0.13, 0.90, "X", 14, k.trim))
            for dx in (-0.38, 0.38):
                parts.append(wf.box("suspension", (bx + dx, y, 0.50), (0.14, 0.22, 0.34),
                                    k.dark))
    k.done("suspension", parts, bevel=0.010)

    k.seats([
        ("driver", (2.70, -0.46, 1.00), 1.0),
        ("commander", (-0.40, 0.48, 1.74), 1.0),
        ("gunner", (0.10, 0.50, 1.70), 1.0),
        ("loader", (-0.20, -0.56, 1.70), -1.0),
    ])

    k.breech("breech", (0.60, 0.0, 1.98), calibre=0.084, house=(0.60, 0.46, 0.52),
             barrel_len=0.90)

    k.rounds("ammo_hull", [
        ((1.30, -0.70, 0.72), 8, (0.0, 0.20, 0.0), 4, (-0.34, 0.0, 0.0)),
    ], 0.084, 0.86,
        frames=[((0.79, 0.0, 0.30), (1.30, 1.60, 0.05)),
                ((0.79, 0.0, 1.16), (1.30, 1.60, 0.04))])

    k.box_module("fuel", [((-1.10, s * 1.08, 1.14), (1.60, 0.40, 0.54)) for s in (-1, 1)],
                 cylinders=[((-1.10, s * 1.08, 1.44), 0.07, 0.10, "Z") for s in (-1, 1)])

    k.optics("optics",
             scopes=[((0.94, 0.34, 2.10), 0.055, 0.70, "X")],
             blocks=[((-0.44 + 0.30 * c, 0.48 + 0.30 * s, 2.60), (0.07, 0.07, 0.09))
                     for c, s in ((1, 0), (0.5, 0.87), (-0.5, 0.87), (-1, 0),
                                  (-0.5, -0.87), (0.5, -0.87))])

    parts = [
        wf.box("turret_drive", (RING_X - 0.30, 0.0, 1.28), (0.70, 0.80, 0.40), k.body),
        wf.cylinder("turret_drive", (RING_X - 0.30, 0.0, 1.56), 0.26, 0.24, "Z", 16, k.trim),
        wf.cylinder("turret_drive", (RING_X, 0.0, 1.60), 0.92, 0.07, "Z", 28, k.trim),
    ]
    for i in range(26):
        a = 2 * math.pi * i / 26
        parts.append(wf.box("turret_drive",
                            (RING_X + 0.92 * math.cos(a), 0.92 * math.sin(a), 1.60),
                            (0.05, 0.05, 0.06), k.trim))
    k.done("turret_drive", parts, bevel=0.008, segments=1)

    k.box_module("radio", [((RING_X - 1.50, 0.0, 2.06), (0.62, 1.00, 0.44)),
                           ((RING_X - 1.50, 0.0, 1.62), (0.62, 1.00, 0.36))],
                 cylinders=[((RING_X - 1.18, 0.0, 2.06), 0.05, 0.08, "X")])

    k.drum("fire_suppression", [(-2.30, 0.52, 1.30), (-2.62, 0.52, 1.30)], 0.17, 0.52,
           axis="Z")

    # 76 mm at 58 degrees on the glacis, and 152 mm on the cast turret front - by 1945
    # the front of a tank had stopped being a plate and become a wedge.
    k.plates("armour_front", [
        ((2.76, 0.0, 1.26), (0.076, 2.90, 0.90), -32),
        ((1.70, 0.0, 1.96), (0.152, 1.60, 0.80), 0),
    ])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=8.0, floor=-0.2)
