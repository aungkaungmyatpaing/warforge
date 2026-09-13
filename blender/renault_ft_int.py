"""
Inside the Renault FT.

Two men, an engine behind a firewall, and a turret that turns: the layout every tank
since has used. It is worth seeing how small it is - the fighting compartment is barely
larger than the two men standing in it, and the commander has no seat, only a canvas
sling, because there is no room for a chair.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

MODULES = ("engine", "firewall", "transmission", "driver", "commander", "gun",
           "ammo", "fuel", "armour",
           "turret_drive")


def build():
    k = intkit.Kit()

    # Renault 4-cylinder, 39 hp from 4.5 litres. Enough for 7 km/h, which was walking
    # pace, which was all that mattered when the infantry were walking too.
    k.piston("engine", (-1.05, 0.0, 0.70), cylinders=4, banks=1, bore=0.105, extras=[
        wf.cylinder("engine", (-1.55, 0.0, 0.70), 0.20, 0.16, "X", 16, k.dark),
        wf.cylinder("engine", (-0.55, 0.0, 0.70), 0.11, 0.20, "X", 12, k.trim),
        wf.box("engine", (-1.05, 0.0, 1.02), (0.70, 0.20, 0.14), k.trim),
    ])

    # The firewall: a plate between the crew and the engine, and the single biggest
    # improvement in crew survival anybody made in 1917.
    k.plates("firewall", [((-0.34, 0.0, 0.72), (0.020, 0.92, 0.86), 0)])

    k.gearbox("transmission", (-1.74, 0.0, 0.62), (0.40, 0.60, 0.40), bell=0.18,
              brakes=[((-1.94, s * 0.40, 0.58), 0.17, 0.18, "Y") for s in (-1, 1)])

    k.seats([("driver", (0.90, 0.0, 0.62), 1.0)])
    k.seats([("commander", (0.20, 0.0, 1.16), 1.0)], chair=False)

    k.breech("gun", (0.46, 0.0, 1.60), calibre=0.037, house=(0.28, 0.24, 0.26),
             barrel_len=0.34, recoil=False)

    k.rounds("ammo", [((0.56, -0.34, 0.94), 6, (0.0, 0.135, 0.0), 3, (-0.15, 0.0, 0.0))],
             0.037, 0.20,
             frames=[((0.31, 0.0, 0.80), (0.42, 0.82, 0.04))])

    k.box_module("fuel", [((-0.52, 0.0, 0.92), (0.26, 0.70, 0.40))],
                 cylinders=[((-0.52, 0.26, 1.14), 0.05, 0.08, "Z")])

    # The turret is turned by the commander leaning on it: a shoulder pad, a hand grip
    # and a race of ball bearings, and nothing else at all.
    parts = [
        wf.cylinder("turret_drive", (0.16, 0.0, 1.30), 0.44, 0.06, "Z", 24, k.body),
        wf.box("turret_drive", (-0.18, 0.0, 1.52), (0.14, 0.34, 0.22), k.trim),
        wf.cylinder("turret_drive", (0.34, 0.30, 1.44), 0.05, 0.16, "Y", 10, k.trim),
    ]
    for i in range(18):
        a = 2 * math.pi * i / 18
        parts.append(wf.sphere("turret_drive",
                               (0.16 + 0.44 * math.cos(a), 0.44 * math.sin(a), 1.30),
                               0.028, 8, k.metal))
    k.done("turret_drive", parts, bevel=0.005, segments=1)

    # 22 mm at the front, 16 on the sides. Riveted, so a hit that did not penetrate could
    # still send the rivet heads flying round inside.
    k.plates("armour", [
        ((1.98, 0.0, 0.80), (0.022, 0.92, 0.70), -34),
        ((0.0, 0.48, 0.72), (3.40, 0.016, 0.80), 0),
        ((0.0, -0.48, 0.72), (3.40, 0.016, 0.80), 0),
        ((0.62, 0.0, 1.62), (0.022, 0.90, 0.56), 0),
    ])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=5.0, floor=-0.2, distance=0.35)
