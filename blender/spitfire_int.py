"""
Inside the Spitfire Mk Vb.

A Merlin, a fuel tank, and a man, in that order along the fuselage - with the tank
directly between the two of them. It was fitted with a fireproof bulkhead and it still
burned pilots, which is why later marks got a self-sealing cover and why the Mk V's
layout is worth looking at rather than admiring.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

DIHEDRAL = 6.0
WING_Z = -0.30

MODULES = ("engine", "supercharger", "fuel", "coolant", "radiator", "pilot", "armour",
           "cannon", "mgs", "ammo", "radio", "oxygen", "controls",
           "hydraulics")


def build():
    k = intkit.Kit()

    # Merlin 45: 27 litres, twelve cylinders at sixty degrees, 1,470 hp on the boost.
    k.piston("engine", (3.10, 0.0, 0.04), cylinders=12, vee=60.0, banks=2, bore=0.137,
             extras=[
                 wf.cylinder("engine", (2.36, 0.0, 0.04), 0.22, 0.20, "X", 18, k.dark),
                 wf.box("engine", (3.10, 0.0, -0.32), (1.00, 0.36, 0.18), k.trim),
                 wf.cylinder("engine", (3.90, 0.0, 0.04), 0.15, 0.26, "X", 14, k.trim),
             ])

    # The single-stage supercharger behind the block: the reason a Merlin keeps its power
    # at altitude, and the thing Rolls-Royce spent the whole war improving.
    k.box_module("supercharger", [((2.34, 0.0, 0.02), (0.46, 0.44, 0.44))],
                 cylinders=[((2.34, 0.0, 0.02), 0.20, 0.50, "X"),
                            ((2.10, 0.0, -0.18), 0.09, 0.30, "X")])

    # 386 litres in two tanks, stacked between the engine and the pilot's knees.
    k.box_module("fuel", [((1.50, 0.0, 0.26), (0.80, 0.58, 0.30)),
                          ((1.50, 0.0, -0.08), (0.80, 0.62, 0.32))],
                 cylinders=[((1.50, 0.22, 0.46), 0.05, 0.08, "Z")])

    k.box_module("coolant", [((2.26, 0.0, 0.34), (0.40, 0.30, 0.20))],
                 cylinders=[((2.00, 0.24, 0.18), 0.035, 1.30, "X"),
                            ((2.00, -0.24, 0.18), 0.035, 1.30, "X")])

    # Radiator under the starboard wing, oil cooler under the port - and they are
    # different sizes, which is the easiest way to tell the wings apart from below.
    parts = []
    for y, half in ((-1.30, 0.26), (1.30, 0.17)):
        z = abs(y) * math.tan(math.radians(DIHEDRAL)) + WING_Z - 0.26
        parts.append(wf.box("radiator", (0.10, y, z), (0.90, half * 2, 0.30), k.body))
        for i in range(6):
            parts.append(wf.box("radiator", (0.44 - i * 0.14, y, z), (0.04, half * 1.8, 0.26),
                                k.trim))
    k.done("radiator", parts, bevel=0.008, segments=1)

    k.seats([("pilot", (-0.36, 0.0, 0.02), 1.0)])

    # 73 lb of armour plate behind the seat and the head - added after the Battle of
    # France, and the single cheapest improvement made to the aeroplane.
    k.plates("armour", [((-0.78, 0.0, 0.14), (0.008, 0.52, 0.62), 0),
                        ((-0.74, 0.0, 0.62), (0.008, 0.36, 0.34), -14)])

    parts = []
    for sign in (-1, 1):
        y = sign * 1.94
        z = abs(y) * math.tan(math.radians(DIHEDRAL)) + WING_Z
        parts.append(wf.cylinder("cannon", (1.10, y, z), 0.055, 1.40, "X", 12, k.body))
        parts.append(wf.cylinder("cannon", (0.50, y, z + 0.06), 0.14, 0.44, "X", 14, k.trim))
        parts.append(wf.box("cannon", (0.16, y, z), (0.30, 0.16, 0.18), k.dark))
    k.done("cannon", parts)

    parts = []
    for sign in (-1, 1):
        for dy in (2.56, 2.92):
            y = sign * dy
            z = abs(y) * math.tan(math.radians(DIHEDRAL)) + WING_Z
            parts.append(wf.cylinder("mgs", (0.80, y, z), 0.028, 0.90, "X", 10, k.body))
            parts.append(wf.box("mgs", (0.30, y, z), (0.24, 0.10, 0.12), k.dark))
    k.done("mgs", parts, bevel=0.006, segments=1)

    k.box_module("ammo", [((0.40, s * 1.70, WING_Z + 1.70 * math.tan(math.radians(DIHEDRAL))),
                           (0.52, 0.26, 0.26)) for s in (-1, 1)]
                 + [((0.30, s * 2.74, WING_Z + 2.74 * math.tan(math.radians(DIHEDRAL))),
                     (0.40, 0.40, 0.14)) for s in (-1, 1)])

    k.box_module("radio", [((-1.30, 0.0, 0.22), (0.44, 0.34, 0.30))],
                 cylinders=[((-1.06, 0.0, 0.22), 0.04, 0.08, "X")])

    k.drum("oxygen", [(-1.00, 0.22, -0.12), (-1.00, -0.22, -0.12)], 0.075, 0.50, axis="X")

    # The pump, the reservoir and the jacks that fold the legs outward into the wing.
    parts = [
        wf.box("hydraulics", (0.70, 0.0, -0.20), (0.36, 0.30, 0.26), k.body),
        wf.cylinder("hydraulics", (0.70, 0.0, 0.06), 0.10, 0.26, "Z", 12, k.trim),
    ]
    for sign in (-1, 1):
        parts.append(wf.cylinder("hydraulics", (0.96, sign * 0.70, -0.44), 0.05, 0.90,
                                 "Y", 10, k.trim))
        parts.append(wf.cylinder("hydraulics", (0.86, sign * 0.30, -0.28), 0.025, 0.90,
                                 "Y", 8, k.dark))
    k.done("hydraulics", parts, bevel=0.008)

    parts = [
        wf.cylinder("controls", (-0.04, 0.0, 0.18), 0.024, 0.46, "Z", 8, k.body),
        wf.box("controls", (0.46, 0.0, -0.26), (0.10, 0.40, 0.05), k.body),
    ]
    for dy in (-0.14, 0.14):
        parts.append(wf.cylinder("controls", (-1.90, dy, -0.10), 0.010, 3.00, "X", 6, k.trim))
    k.done("controls", parts, bevel=0.004, segments=1)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=9.0, floor=-1.9, distance=0.4)
