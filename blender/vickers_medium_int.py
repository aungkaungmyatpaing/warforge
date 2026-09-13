"""
Inside the Vickers Medium Mk II.

Engine at the *front*, offset to the right, with the driver sitting beside it rather than
behind it. That is why the hull is so tall and so boxy: the crew are working alongside
the machinery instead of over it, and the turret has to clear the lot.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

MODULES = ("engine", "transmission", "driver", "commander", "gunner", "loader",
           "hull_gunner", "breech", "ammo", "fuel", "armour",
           "turret_drive", "radio")


def build():
    k = intkit.Kit()

    # Armstrong Siddeley air-cooled V8, 90 hp. Air-cooled, so there is no radiator and
    # no coolant to lose - and no way to quieten it either.
    k.piston("engine", (1.70, 0.24, 0.74), cylinders=8, vee=90.0, banks=2, bore=0.125,
             extras=[
                 wf.cylinder("engine", (1.10, 0.24, 0.74), 0.22, 0.18, "X", 16, k.dark),
                 wf.box("engine", (1.70, 0.24, 1.12), (0.80, 0.40, 0.16), k.trim),
             ])

    k.gearbox("transmission", (0.70, 0.24, 0.66), (0.60, 0.60, 0.44), bell=0.22,
              brakes=[((2.28, s * 0.56, 0.60), 0.24, 0.24, "Y") for s in (-1, 1)])

    k.seats([
        ("driver", (1.80, -0.44, 1.16), 1.0),
        ("hull_gunner", (2.10, 0.34, 1.30), 1.0),
        ("commander", (0.10, 0.0, 1.96), 1.0),
        ("gunner", (0.60, 0.40, 1.92), 1.0),
        ("loader", (0.30, -0.46, 1.92), -1.0),
    ])

    k.breech("breech", (0.86, 0.0, 2.14), calibre=0.047, house=(0.34, 0.30, 0.32),
             barrel_len=0.42, recoil=False)

    k.rounds("ammo", [
        ((-0.30, s * 0.62, 1.34), 7, (0.20, 0.0, 0.0), 2, (0.10, s * -0.14, 0.0))
        for s in (-1, 1)
    ], 0.047, 0.36,
        frames=[((0.35, s * 0.68, 1.14), (1.60, 0.18, 0.04)) for s in (-1, 1)])

    k.box_module("fuel", [((-1.90, 0.0, 1.10), (0.70, 1.20, 0.44))],
                 cylinders=[((-1.90, 0.40, 1.36), 0.06, 0.10, "Z")])

    parts = [
        wf.cylinder("turret_drive", (0.44, 0.0, 1.84), 0.72, 0.06, "Z", 26, k.body),
        wf.cylinder("turret_drive", (0.20, 0.44, 2.00), 0.15, 0.05, "Y", 14, k.trim),
        wf.box("turret_drive", (0.06, 0.0, 1.94), (0.18, 0.30, 0.20), k.trim),
    ]
    k.done("turret_drive", parts, bevel=0.006, segments=1)

    k.box_module("radio", [((-1.10, 0.60, 1.44), (0.50, 0.30, 0.36))],
                 cylinders=[((-0.85, 0.60, 1.44), 0.04, 0.06, "X")])

    # 6.25 mm on the sides. Thin enough that a heavy machine gun went through it, which
    # is what ended the fast-and-lightly-armoured idea.
    k.plates("armour", [
        ((2.62, 0.0, 0.80), (0.0083, 1.60, 0.60), 0),
        ((0.0, 0.86, 1.30), (5.00, 0.00625, 0.90), 0),
        ((0.0, -0.86, 1.30), (5.00, 0.00625, 0.90), 0),
    ])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=5.6, floor=-0.2, distance=0.4)
