"""
Inside the MiG-15bis.

A centrifugal-flow jet wrapped in the smallest airframe that would hold it. The intake
splits round the cockpit and joins again behind it, so the pilot sits inside the duct -
and the guns come out on a tray that winches down to the ground for reloading, because
there is no room to get at them any other way.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

NOSE, TAIL = 5.05, -5.05

MODULES = ("engine", "intake_duct", "pilot", "cannon", "gun_pack", "fuel",
           "airbrakes", "armour",
           "oxygen", "ejection")


def build():
    k = intkit.Kit()

    # Klimov VK-1: a developed Rolls-Royce Nene, sold to the USSR in 1947 by a British
    # government that thought it would not matter. Centrifugal, so it is short and fat
    # rather than long and thin - which is why the fuselage is a barrel.
    k.jet("engine", (-2.30, 0.0, 0.10), length=2.60, radius=0.50, stages=1, extras=[
        wf.cylinder("engine", (-1.50, 0.0, 0.10), 0.62, 0.50, "X", 22, k.trim),
        wf.cylinder("engine", (-4.30, 0.0, 0.18), 0.34, 1.20, "X", 20, k.dark),
    ])

    # The duct: round the nose, split either side of the cockpit, back together behind it.
    parts = [
        wf.cylinder("intake_duct", (4.10, 0.0, -0.02), 0.42, 1.50, "X", 20, k.body),
    ]
    for sign in (-1, 1):
        for i in range(5):
            x = 3.10 - i * 0.62
            y = sign * (0.10 + 0.22 * min(i, 3))
            parts.append(wf.cylinder("intake_duct", (x, y, 0.0), 0.28, 0.60, "X", 14, k.body))
    parts.append(wf.cylinder("intake_duct", (0.20, 0.0, 0.06), 0.44, 0.80, "X", 18, k.body))
    k.done("intake_duct", parts, bevel=0.014)

    k.seats([("pilot", (1.40, 0.0, 0.50), 1.0)])

    # One 37 mm and two 23 mm. Enough to destroy a bomber in a single pass, and almost
    # impossible to hit a fighter with - the two calibres have different trajectories.
    parts = [
        wf.cylinder("cannon", (4.30, -0.22, -0.58), 0.062, 1.70, "X", 12, k.body),
        wf.box("cannon", (3.20, -0.22, -0.58), (0.60, 0.16, 0.18), k.dark),
    ]
    for dy in (0.16, 0.30):
        parts.append(wf.cylinder("cannon", (4.20, dy, -0.58), 0.045, 1.50, "X", 10, k.body))
        parts.append(wf.box("cannon", (3.30, dy, -0.58), (0.50, 0.13, 0.15), k.dark))
    k.done("cannon", parts)

    # The tray the whole gun pack sits on, and the winches that lower it.
    parts = [wf.box("gun_pack", (3.70, 0.0, -0.76), (1.60, 0.66, 0.14), k.body)]
    for dx in (-0.60, 0.60):
        parts.append(wf.cylinder("gun_pack", (3.70 + dx, 0.0, -0.60), 0.05, 0.30, "Z", 10,
                                 k.trim))
    for dy in (-0.20, 0.20):
        parts.append(wf.box("gun_pack", (3.30, dy, -0.66), (0.70, 0.18, 0.20), k.trim))
    k.done("gun_pack", parts)

    k.box_module("fuel", [((-0.60, 0.0, 0.20), (1.60, 1.00, 0.70)),
                          ((-3.60, 0.0, 0.30), (1.10, 0.80, 0.50))],
                 cylinders=[((-0.60, 0.0, 0.60), 0.07, 0.12, "Z")])

    parts = []
    for sign in (-1, 1):
        parts.append(wf.box("airbrakes", (-3.00, sign * 0.62, 0.10), (0.80, 0.06, 0.56),
                            k.body))
        parts.append(wf.cylinder("airbrakes", (-2.70, sign * 0.54, 0.10), 0.05, 0.40,
                                 "X", 10, k.trim))
    k.done("airbrakes", parts, bevel=0.010)

    k.drum("oxygen", [(0.60, 0.34, 0.62), (0.60, -0.34, 0.62)], 0.085, 0.62, axis="X")

    # The seat, the rails it runs up, and the cartridge that fires it.
    parts = [
        wf.box("ejection", (1.00, 0.0, 0.56), (0.32, 0.52, 0.92), k.body),
        wf.box("ejection", (0.76, 0.0, 0.72), (0.10, 0.44, 1.10), k.trim),
        wf.cylinder("ejection", (0.76, 0.0, 0.20), 0.06, 0.40, "Z", 10, k.dark),
        wf.box("ejection", (1.12, 0.0, 1.06), (0.26, 0.40, 0.14), k.trim),
    ]
    k.done("ejection", parts, bevel=0.012)

    k.plates("armour", [((2.30, 0.0, 0.46), (0.012, 0.60, 0.64), 0),
                        ((0.70, 0.0, 0.52), (0.012, 0.56, 0.60), 0),
                        ((2.60, 0.0, 0.86), (0.065, 0.40, 0.30), -28)])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=11.0, floor=-2.0, distance=0.5)
