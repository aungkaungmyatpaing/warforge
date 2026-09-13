"""
Inside the Mark IV.

There is no bulkhead. The engine stands in the middle of the crew compartment with eight
men round it, unsilenced, in an unventilated steel box, and the exhaust leaks. Crews
passed out from carbon monoxide as a matter of routine, and that - not the armour, not
the guns - is the fact the X-ray view of this vehicle exists to show.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

MODULES = ("engine", "radiator", "transmission", "commander", "driver",
           "gearsman_l", "gearsman_r", "gunner_l", "gunner_r", "gun_breech",
           "ammo", "fuel", "armour",
           "ventilation")


def build():
    k = intkit.Kit()

    # Daimler-Foster: 105 hp from 16 litres, six cylinders in a row, out in the open
    # where the crew work. Everything else in the hull is arranged around it.
    k.piston("engine", (0.10, 0.0, 1.06), cylinders=6, banks=1, bore=0.155, extras=[
        wf.cylinder("engine", (-0.90, 0.0, 1.06), 0.30, 0.24, "X", 18, k.dark),   # flywheel
        wf.box("engine", (0.10, 0.0, 1.62), (1.20, 0.30, 0.20), k.trim),          # exhaust
        wf.cylinder("engine", (1.10, 0.0, 1.06), 0.16, 0.30, "X", 14, k.trim),
    ])
    k.box_module("radiator", [((-1.42, 0.0, 1.12), (0.24, 1.10, 0.90))],
                 cylinders=[((-1.16, 0.0, 1.12), 0.30, 0.16, "X")])

    # A primary gearbox, then a secondary box each side worked by its own man: to turn
    # the tank, one of them shifts his box down and the machine slews round it.
    k.gearbox("transmission", (-1.90, 0.0, 0.92), (0.70, 0.80, 0.60), bell=0.26,
              brakes=[((-2.40, s * 0.70, 0.86), 0.26, 0.28, "Y") for s in (-1, 1)])

    k.seats([
        ("driver", (2.30, -0.34, 1.88), 1.0),
        ("commander", (2.30, 0.34, 1.88), 1.0),
        ("gearsman_l", (-1.70, 0.60, 1.00), -1.0),
        ("gearsman_r", (-1.70, -0.60, 1.00), -1.0),
        ("gunner_l", (1.10, 1.74, 1.10), 1.0),
        ("gunner_r", (1.10, -1.74, 1.10), 1.0),
    ])

    # Two 6-pounder breeches, one in each sponson, pointing out sideways.
    parts = []
    for sign in (-1, 1):
        y = sign * 1.66
        parts.append(wf.box("gun_breech", (1.44, y, 1.24), (0.34, 0.30, 0.34), k.body))
        parts.append(wf.box("gun_breech", (1.26, y, 1.24), (0.08, 0.32, 0.36), k.trim))
        parts.append(wf.cylinder("gun_breech", (1.86, y, 1.24), 0.10, 0.60, "X", 14, k.trim))
        parts.append(wf.box("gun_breech", (1.20, y, 1.06), (0.36, 0.34, 0.16), k.dark))
    k.done("gun_breech", parts)

    # 6-pounder rounds racked along the sponson walls.
    racks = []
    for sign in (-1, 1):
        racks.append(((0.30, sign * 1.80, 1.42), 8, (0.18, 0.0, 0.0), 2,
                      (0.09, sign * -0.14, 0.0)))
    k.rounds("ammo", racks, 0.057, 0.44,
             frames=[((0.94, s * 1.86, 1.16), (1.60, 0.20, 0.05)) for s in (-1, 1)])

    k.box_module("fuel", [((2.60, s * 0.66, 1.06), (0.60, 0.50, 0.60)) for s in (-1, 1)],
                 cylinders=[((2.60, s * 0.66, 1.40), 0.06, 0.10, "Z") for s in (-1, 1)])

    # There is no ventilation. What there is instead: an exhaust pipe running through the
    # compartment the crew are sitting in, and two small louvres in the roof.
    parts = [
        wf.cylinder("ventilation", (0.10, 0.0, 1.80), 0.09, 2.60, "X", 12, k.body),
        wf.cylinder("ventilation", (-1.30, 0.0, 1.94), 0.07, 0.70, "X", 10, k.trim),
    ]
    for dy in (-0.50, 0.50):
        parts.append(wf.box("ventilation", (0.60, dy, 2.40), (0.50, 0.26, 0.06), k.trim))
        for i in range(4):
            parts.append(wf.box("ventilation", (0.42 + i * 0.12, dy, 2.44),
                                (0.04, 0.24, 0.05), k.dark))
    k.done("ventilation", parts, bevel=0.008, segments=1)

    # 12 mm all round: proof against rifle fire and nothing else. It is the thinnest
    # armour of any vehicle here, and in 1917 it was enough.
    k.plates("armour", [
        ((3.40, 0.0, 1.30), (0.012, 1.90, 1.40), 0),
        ((0.10, 1.94, 1.30), (5.60, 0.012, 1.40), 0),
        ((0.10, -1.94, 1.30), (5.60, 0.012, 1.40), 0),
        ((0.10, 0.0, 2.44), (5.60, 1.90, 0.012), 0),
    ])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=8.0, floor=-0.2)
