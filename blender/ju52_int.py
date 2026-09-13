"""
Inside the Ju 52/3m.

Three engines is the design. In 1932 a single engine failure over water killed everybody
aboard, so Junkers hung one on each wing and a third on the nose - and accepted that the
nose engine sits directly in front of the cockpit and blocks the view forward on landing.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

NOSE = 9.45
WING_Z = -0.72
ENGINE_Y = 5.30

MODULES = ("eng_nose", "eng_wing", "structure", "pilot", "copilot", "cargo",
           "fuel", "radio")


def wing_le(y):
    """Leading edge at span |y| - the same plan the exterior uses."""
    for (y0, le0), (y1, le1) in zip([(0.0, 2.10), (4.0, 2.00), (5.3, 1.94), (9.6, 1.70)],
                                    [(4.0, 2.00), (5.3, 1.94), (9.6, 1.70), (14.6, 1.22)]):
        if abs(y) <= y1:
            t = (abs(y) - y0) / max(1e-6, y1 - y0)
            return le0 + (le1 - le0) * t
    return 1.22


def build():
    k = intkit.Kit()

    # BMW 132: a licence-built Pratt & Whitney Hornet, nine cylinders, 725 hp.
    k.radial("eng_nose", (8.90, 0.0, 0.06), cylinders=9, radius=0.68, bore=0.19, extras=[
        wf.cylinder("eng_nose", (9.30, 0.0, 0.06), 0.22, 0.24, "X", 16, k.trim),
        wf.cylinder("eng_nose", (8.30, 0.0, 0.06), 0.28, 0.20, "X", 16, k.dark),
    ])

    parts = []
    for sign in (-1, 1):
        y = sign * ENGINE_Y
        z = WING_Z + abs(y) * math.tan(math.radians(2.0)) - 0.46
        x = wing_le(y) + 1.10
        parts += wf.radial_engine("eng_wing", (x, y, z), cylinders=9, radius=0.64,
                                  bore=0.18, block=k.body, head=k.trim)
        parts.append(wf.cylinder("eng_wing", (x + 0.40, y, z), 0.20, 0.24, "X", 16, k.trim))
        parts.append(wf.cylinder("eng_wing", (x - 0.60, y, z), 0.26, 0.20, "X", 16, k.dark))
    k.done("eng_wing", parts)

    # The corrugated skin is the structure: Junkers used it as stressed skin rather than
    # hanging fabric on a frame, which is why the aeroplane has almost no internal
    # bracing and could be repaired with a hammer.
    parts = []
    parts.append(wf.box("structure", (0.30, 0.0, WING_Z), (0.44, 28.6, 0.30), k.body))
    parts.append(wf.box("structure", (-1.60, 0.0, WING_Z + 0.06), (0.26, 27.0, 0.20), k.trim))
    for i in range(11):
        y = -13.0 + i * 2.6
        parts.append(wf.box("structure", (0.30, y, WING_Z), (2.60, 0.10, 0.36), k.trim))
    for i in range(9):
        x = -8.0 + i * 1.9
        parts.append(wf.cylinder("structure", (x, 0.0, 0.0), 0.95, 0.10, "X", 20, k.dark))
    k.done("structure", parts, bevel=0.010, segments=1)

    k.seats([("pilot", (5.30, 0.50, 0.66), 1.0), ("copilot", (5.30, -0.50, 0.66), 1.0)])

    # Seventeen seats, or the freight that replaced them. This is the aeroplane that
    # invented the idea that an air force needs transports at all.
    parts = []
    for i in range(6):
        x = 3.00 - i * 1.20
        for sign in (-1, 1):
            parts.append(wf.box("cargo", (x, sign * 0.62, -0.16), (0.50, 0.46, 0.10), k.body))
            parts.append(wf.box("cargo", (x - 0.26, sign * 0.62, 0.18), (0.08, 0.46, 0.58),
                                k.trim))
    parts.append(wf.box("cargo", (0.20, 0.0, -0.56), (7.40, 1.50, 0.08), k.dark))
    k.done("cargo", parts, bevel=0.012)

    k.box_module("fuel", [
        (( wing_le(y) - 1.40, y, WING_Z + 0.04), (1.60, 1.80, 0.30))
        for y in (-7.4, -3.6, 3.6, 7.4)
    ], cylinders=[((wing_le(7.4) - 1.40, s * 7.4, WING_Z + 0.24), 0.07, 0.10, "Z")
                  for s in (-1, 1)])

    k.box_module("radio", [((3.60, 0.72, 0.36), (0.46, 0.30, 0.28)),
                           ((3.60, 0.72, 0.66), (0.46, 0.30, 0.24))],
                 cylinders=[((3.84, 0.72, 0.50), 0.04, 0.06, "X")])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=22.0, floor=-2.8, distance=1.2)
