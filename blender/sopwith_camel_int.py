"""
Inside the Sopwith Camel.

The whole aeroplane is in the first two metres: engine, guns, fuel, pilot, all packed
ahead of the centre of gravity. That is what gave it a turn nobody could follow and a
take-off that killed more trainees than the Germans did - the rotary engine is a
gyroscope weighing 150 kg and turning at 1,250 rpm.
"""
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

MODULES = ("engine", "fuel", "pilot", "guns", "ammo", "structure", "controls")


def build():
    k = intkit.Kit()

    # Clerget 9B: nine cylinders, and the whole engine spins with the propeller. Total
    # loss lubrication - castor oil, thrown out of the cowl and inhaled by the pilot.
    k.radial("engine", (2.02, 0.0, 0.02), cylinders=9, radius=0.42, bore=0.12, extras=[
        wf.cylinder("engine", (2.26, 0.0, 0.02), 0.14, 0.16, "X", 14, k.trim),
        wf.cylinder("engine", (1.74, 0.0, 0.02), 0.17, 0.12, "X", 14, k.dark),
    ])

    # Fuel and oil behind the pilot's back, between him and the tail.
    k.box_module("fuel", [((-0.92, 0.0, 0.18), (0.56, 0.46, 0.40))],
                 cylinders=[((-0.92, 0.24, 0.44), 0.05, 0.08, "Z")])

    k.seats([("pilot", (-0.32, 0.0, 0.08), 1.0)])

    # Two Vickers, synchronised, sitting right in front of the pilot's face - which is
    # where the hump comes from and where the aeroplane's name comes from.
    parts = []
    for dy in (-0.14, 0.14):
        parts.append(wf.box("guns", (1.26, dy, 0.60), (1.00, 0.10, 0.10), k.body))
        parts.append(wf.box("guns", (0.70, dy, 0.58), (0.32, 0.13, 0.16), k.dark))
        parts.append(wf.cylinder("guns", (1.90, dy, 0.60), 0.028, 0.30, "X", 10, k.trim))
    parts.append(wf.box("guns", (1.84, 0.0, 0.76), (0.04, 0.14, 0.14), k.metal))
    k.done("guns", parts)

    k.box_module("ammo", [((0.44, s * 0.16, 0.44), (0.34, 0.16, 0.26)) for s in (-1, 1)],
                 cylinders=[((0.62, s * 0.16, 0.52), 0.03, 0.24, "X") for s in (-1, 1)])

    # The airframe: four ash longerons with wire cross-bracing, and nothing else. The
    # whole structure of a 1917 fighter weighs less than the engine bolted to the front.
    parts = []
    for dy, dz in ((-0.30, 0.26), (0.30, 0.26), (-0.26, -0.26), (0.26, -0.26)):
        parts.append(wf.box("structure", (-0.60, dy, dz), (3.60, 0.05, 0.05), k.body))
    for x in (-2.40, -1.60, -0.80, 0.10, 0.90):
        parts.append(wf.box("structure", (x, 0.0, 0.0), (0.05, 0.60, 0.56), k.trim))
    parts.append(wf.box("structure", (0.28, 0.0, 1.10), (0.20, 2.60, 0.06), k.trim))
    parts.append(wf.box("structure", (0.14, 0.0, -0.30), (0.20, 2.40, 0.06), k.trim))
    k.done("structure", parts, bevel=0.006, segments=1)

    # Stick, rudder bar and the control cables running back inside the fuselage.
    parts = [
        wf.cylinder("controls", (0.04, 0.0, 0.22), 0.022, 0.44, "Z", 8, k.body),
        wf.box("controls", (0.58, 0.0, -0.24), (0.08, 0.44, 0.05), k.body),
    ]
    for dy in (-0.16, 0.16):
        parts.append(wf.cylinder("controls", (-1.70, dy, -0.06), 0.010, 3.10, "X", 6, k.trim))
        parts.append(wf.cylinder("controls", (-1.70, dy, 0.20), 0.010, 3.10, "X", 6, k.trim))
    k.done("controls", parts, bevel=0.004, segments=1)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=7.0, floor=-1.5, distance=0.4)
