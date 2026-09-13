"""
Inside the F-16C.

Two things here have no equivalent anywhere else in this collection. The aeroplane is
aerodynamically unstable and is only flyable because four computers move the controls
faster than the airframe can diverge - there is no mechanical path from the stick to the
tail at all. And the seat is reclined thirty degrees, which is what lets a pilot stay
conscious pulling nine g.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

NOSE, TAIL = 7.53, -7.53
WING_Z = -0.30

MODULES = ("engine", "fbw", "radar", "pilot", "canopy", "cannon", "ammo", "fuel",
           "ejection", "intake",
           "oxygen")


def build():
    k = intkit.Kit()

    # F110: an axial-flow turbofan with an afterburner, 29,000 lb of thrust in an
    # aeroplane that weighs 12 tonnes empty. More thrust than weight, which is the
    # whole design.
    k.jet("engine", (-4.00, 0.0, 0.10), length=4.60, radius=0.46, stages=9, extras=[
        wf.cylinder("engine", (-6.60, 0.0, 0.10), 0.50, 1.40, "X", 22, k.dark),
        wf.box("engine", (-4.00, 0.0, 0.66), (2.60, 0.30, 0.22), k.trim),
    ])

    # The flight control computers, and the actuators they drive. Four of them, because
    # an unstable aeroplane with one computer is an aeroplane with one fatal failure.
    parts = []
    for i in range(4):
        parts.append(wf.box("fbw", (-0.40 - i * 0.34, 0.30, 0.44), (0.30, 0.24, 0.30),
                            k.body))
    for x, y, z in ((-4.60, 1.10, -0.16), (-4.60, -1.10, -0.16),
                    (-4.90, 0.0, 1.10), (0.60, 2.60, WING_Z)):
        parts.append(wf.cylinder("fbw", (x, y, z), 0.09, 0.50, "X", 10, k.trim))
        parts.append(wf.box("fbw", (x - 0.30, y, z), (0.24, 0.14, 0.14), k.dark))
    k.done("fbw", parts)

    # APG-68 in the nose: a flat plate array on a two-axis gimbal.
    parts = [
        wf.cylinder("radar", (6.10, 0.0, 0.06), 0.42, 0.14, "X", 22, k.body),
        wf.box("radar", (5.80, 0.0, 0.06), (0.26, 0.66, 0.66), k.trim),
    ]
    for i in range(5):
        parts.append(wf.box("radar", (6.18, 0.0, -0.28 + i * 0.14), (0.04, 0.72, 0.06),
                            k.metal))
    k.done("radar", parts)

    # Reclined thirty degrees. Lying back raises the height a pilot's heart has to pump
    # blood to his brain, and that is worth about one extra g before he blacks out.
    parts = wf.figure("pilot", (2.00, 0.0, 0.34), facing=1.0, seated=True,
                      body=k.body, kit=k.trim, helmet=k.dark)
    seat = wf.box("pilot", (1.60, 0.0, 0.48), (0.30, 0.52, 0.90), k.trim)
    wf.rotate(seat, y=30)
    parts.append(seat)
    k.done("pilot", parts, bevel=0.012, angle=52)

    k.box_module("canopy", [((2.30, 0.0, 0.96), (2.80, 0.90, 0.08))],
                 cylinders=[((0.90, 0.0, 0.86), 0.09, 0.60, "X")])

    # M61 Vulcan: six barrels in the port wing root, 6,000 rounds a minute, and twenty
    # seconds of ammunition.
    parts = [wf.cylinder("cannon", (2.30, 0.86, 0.52), 0.13, 1.90, "X", 16, k.body)]
    for i in range(6):
        a = 2 * math.pi * i / 6
        parts.append(wf.cylinder("cannon", (3.00, 0.86 + 0.075 * math.cos(a),
                                            0.52 + 0.075 * math.sin(a)),
                                 0.026, 1.20, "X", 8, k.trim))
    parts.append(wf.box("cannon", (1.20, 0.86, 0.52), (0.50, 0.30, 0.30), k.dark))
    k.done("cannon", parts)

    k.box_module("ammo", [((0.30, 0.60, 0.30), (1.10, 0.80, 0.70))],
                 cylinders=[((0.30, 0.60, 0.30), 0.34, 1.05, "X")])

    k.box_module("fuel", [((-1.20, 0.0, 0.10), (3.20, 1.60, 0.70)),
                          ((-4.20, 0.0, 0.50), (2.20, 1.00, 0.36))]
                 + [((0.40, s * 1.70, WING_Z + 0.04), (2.40, 1.40, 0.22)) for s in (-1, 1)],
                 cylinders=[((-1.20, 0.0, 0.50), 0.08, 0.12, "Z")])

    # ACES II: a rocket seat that will get a pilot out on the ground at zero speed.
    parts = [
        wf.box("ejection", (1.56, 0.0, 0.52), (0.34, 0.56, 1.00), k.body),
        wf.cylinder("ejection", (1.30, 0.0, 0.90), 0.09, 0.70, "Z", 12, k.trim),
        wf.box("ejection", (1.40, 0.0, 1.16), (0.24, 0.40, 0.16), k.dark),
    ]
    for p in parts[1:]:
        wf.rotate(p, y=30)
    k.done("ejection", parts)

    # No bottles: a sieve makes oxygen out of bleed air, so there is nothing to refill.
    parts = [
        wf.box("oxygen", (0.90, 0.40, 0.30), (0.60, 0.34, 0.34), k.body),
        wf.cylinder("oxygen", (0.90, 0.40, 0.56), 0.11, 0.26, "Z", 12, k.trim),
        wf.cylinder("oxygen", (0.30, 0.40, 0.30), 0.05, 0.70, "X", 10, k.dark),
    ]
    k.done("oxygen", parts, bevel=0.010)

    parts = [wf.box("intake", (2.60, 0.0, -0.82), (3.60, 1.20, 0.64), k.body)]
    for i in range(4):
        parts.append(wf.cylinder("intake", (1.60 - i * 0.90, 0.0, -0.70), 0.42, 0.60,
                                 "X", 18, k.trim))
    k.done("intake", parts, bevel=0.020)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=16.0, floor=-2.8, distance=0.6)
