"""
Inside the Fokker Dr.I.

The thing to look at is the wing spar. Fokker's wings are thick enough to carry their own
loads on an internal box spar, so the triplane needs almost no bracing between its wings -
one strut a side, and that mostly to stop them flapping. Every other aeroplane of 1917 is
a wire-braced truss, and this one is not.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

TOP_Z, MID_Z, LOW_Z = 1.42, 0.38, -0.56

MODULES = ("engine", "structure", "wing_spar", "fuel", "pilot", "guns", "ammo")


def build():
    k = intkit.Kit()

    k.radial("engine", (2.08, 0.0, 0.0), cylinders=9, radius=0.40, bore=0.115, extras=[
        wf.cylinder("engine", (2.30, 0.0, 0.0), 0.13, 0.16, "X", 14, k.trim),
        wf.cylinder("engine", (1.82, 0.0, 0.0), 0.16, 0.12, "X", 14, k.dark),
    ])

    # Welded steel tube, not wood: Fokker's fuselages are a lattice of thin tube, which
    # is lighter, quicker to build and far more tolerant of a bullet through it.
    parts = []
    for dy, dz in ((-0.26, 0.24), (0.26, 0.24), (-0.22, -0.24), (0.22, -0.24)):
        parts.append(wf.cylinder("structure", (-0.60, dy, dz), 0.020, 3.60, "X", 8, k.body))
    for x in (-2.40, -1.70, -1.00, -0.30, 0.40, 1.10):
        parts.append(wf.box("structure", (x, 0.0, 0.0), (0.035, 0.52, 0.50), k.trim))
    for i, x in enumerate((-2.05, -1.35, -0.65, 0.05, 0.75)):
        lean = 26 if i % 2 == 0 else -26
        for dz in (0.24, -0.24):
            brace = wf.cylinder("structure", (x, 0.0, dz), 0.012, 0.80, "X", 6, k.dark)
            wf.rotate(brace, z=lean)
            parts.append(brace)
    k.done("structure", parts, bevel=0.005, segments=1)

    # The box spar inside each wing, and the one strut a side that ties them together.
    parts = []
    for z, half in ((TOP_Z, 3.50), (MID_Z, 3.06), (LOW_Z, 2.73)):
        parts.append(wf.box("wing_spar", (0.34, 0.0, z), (0.20, half * 2, 0.12), k.body))
        parts.append(wf.box("wing_spar", (-0.16, 0.0, z), (0.10, half * 1.9, 0.08), k.trim))
        for sign in (-1, 1):
            for dy in (0.9, 1.9, 2.7):
                parts.append(wf.box("wing_spar", (0.10, sign * dy, z), (0.50, 0.04, 0.08),
                                    k.trim))
    for sign in (-1, 1):
        for z0, z1 in ((LOW_Z, MID_Z), (MID_Z, TOP_Z)):
            parts.append(wf.box("wing_spar", (0.30, sign * 2.62, (z0 + z1) * 0.5),
                                (0.13, 0.05, z1 - z0), k.dark))
    k.done("wing_spar", parts, bevel=0.006, segments=1)

    k.box_module("fuel", [((-0.82, 0.0, 0.14), (0.52, 0.44, 0.36))],
                 cylinders=[((-0.82, 0.22, 0.38), 0.05, 0.08, "Z")])

    k.seats([("pilot", (-0.18, 0.0, 0.02), 1.0)])

    parts = []
    for dy in (-0.13, 0.13):
        parts.append(wf.box("guns", (1.38, dy, 0.48), (1.02, 0.09, 0.09), k.body))
        parts.append(wf.box("guns", (0.82, dy, 0.46), (0.30, 0.12, 0.15), k.dark))
        parts.append(wf.cylinder("guns", (2.02, dy, 0.48), 0.026, 0.28, "X", 10, k.trim))
    k.done("guns", parts)

    k.box_module("ammo", [((0.56, s * 0.15, 0.30), (0.32, 0.15, 0.24)) for s in (-1, 1)])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=7.0, floor=-1.8, distance=0.4)
