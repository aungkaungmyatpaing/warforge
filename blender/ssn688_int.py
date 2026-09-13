"""
Inside a Los Angeles-class boat.

A reactor means the boat never has to surface, so there is no diesel, no battery bank
worth the name, and no snorkel - everything U-9 needed two propulsion systems for is done
by one. What replaces them is quieting: rafted machinery, sound isolation mounts and
anechoic tiles, because against a nuclear boat detection is the only weapon that matters.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 55.15, -55.15
RADIUS = 5.05

MODULES = ("reactor", "steam_plant", "quieting", "sonar", "torpedo_room", "vls",
           "control", "crew", "pressure_hull",
           "atmosphere")


def build():
    k = intkit.Kit()

    # S6G: a pressurised-water reactor inside a shielded compartment nobody enters at
    # sea. The shielding is most of what you can see of it.
    parts = [
        wf.cylinder("reactor", (-6.0, 0.0, -1.0), 1.9, 5.0, "Z", 22, k.body),
        wf.cylinder("reactor", (-6.0, 0.0, -1.0), 2.8, 5.6, "Z", 24, k.trim),
        wf.box("reactor", (-6.0, 0.0, -1.0), (9.0, 8.0, 8.0), k.dark),
    ]
    for sign in (-1, 1):
        parts.append(wf.cylinder("reactor", (-6.0, sign * 2.6, 1.2), 0.9, 3.6, "Z", 16,
                                 k.body))
        parts.append(wf.cylinder("reactor", (-6.0, sign * 2.6, -3.0), 0.5, 4.0, "X", 12,
                                 k.metal))
    k.done("reactor", parts, bevel=0.08)

    # Two steam turbines on a raft, geared to one shaft. The raft is the quieting: the
    # machinery is not bolted to the hull, it floats on mounts inside it.
    parts = []
    for sign in (-1, 1):
        parts += wf.steam_turbine("steam_plant", (-20.0, sign * 1.8, -0.6), (9.0, 3.0, 3.2),
                                  block=k.body, shaft=k.metal)
    parts.append(wf.cylinder("steam_plant", (-30.0, 0.0, -0.6), 0.9, 14.0, "X", 16, k.metal))
    parts.append(wf.box("steam_plant", (-20.0, 0.0, -2.8), (16.0, 7.0, 0.8), k.trim))
    k.done("steam_plant", parts, bevel=0.06)

    # Sound isolation mounts under the raft, and the tile layer on the outside.
    parts = []
    for i in range(8):
        x = -27.0 + i * 2.0
        for sign in (-1, 1):
            parts.append(wf.cylinder("quieting", (x, sign * 2.4, -3.4), 0.34, 1.0, "Z",
                                     12, k.body))
    for i in range(6):
        x = -13.0 + i * 2.4
        parts.append(wf.cylinder("quieting", (x, 0.0, -3.6), 0.30, 0.9, "Z", 12, k.body))
    for i in range(9):
        x = -34.0 + i * 8.0
        parts.append(wf.cylinder("quieting", (x, 0.0, 0.0), RADIUS * 0.99, 6.0, "X", 24,
                                 k.trim))
    k.done("quieting", parts, bevel=0.05, segments=1)

    # The BQQ-5 sphere fills the bow. That is why the torpedo tubes are amidships and
    # angled out - there is no room for them in the nose.
    parts = [
        wf.sphere("sonar", (46.0, 0.0, -0.2), 3.6, 22, k.body),
        wf.cylinder("sonar", (40.0, 0.0, -0.2), 2.4, 3.0, "X", 20, k.trim),
        wf.box("sonar", (34.0, 0.0, -0.2), (6.0, 5.0, 4.0), k.dark),
    ]
    parts.append(wf.box("sonar", (-8.0, -4.80, 1.4), (58.0, 0.44, 0.44), k.trim))
    k.done("sonar", parts, bevel=0.08)

    parts = []
    for sign in (-1, 1):
        for dz in (-0.9, 0.9):
            tube = wf.cylinder("torpedo_room", (28.0, sign * 3.4, dz), 0.32, 9.0, "X", 14,
                               k.body)
            wf.rotate(tube, z=sign * 8)
            parts.append(tube)
            parts.append(wf.cylinder("torpedo_room", (22.0, sign * 2.6, dz), 0.267, 6.2,
                                     "X", 14, k.trim))
    parts.append(wf.box("torpedo_room", (24.0, 0.0, -0.2), (14.0, 7.0, 4.4), k.dark))
    k.done("torpedo_room", parts, bevel=0.05)

    # Twelve vertical tubes outside the pressure hull, forward of the sonar sphere.
    parts = []
    for i in range(6):
        for sign in (-1, 1):
            x = 40.0 - i * 1.60
            parts.append(wf.cylinder("vls", (x, sign * 2.10, -3.0), 0.38, 5.0, "Z", 14,
                                     k.body))
            parts.append(wf.cylinder("vls", (x, sign * 2.10, -0.6), 0.44, 0.24, "Z", 14,
                                     k.trim))
    k.done("vls", parts, bevel=0.04)

    k.crowd("control", [(16.0, 1.2, 1.4), (16.0, -1.2, 1.4), (13.0, 0.0, 1.4),
                        (18.5, 1.6, 1.4), (18.5, -1.6, 1.4)], seated=False)
    k.crowd("crew", [(8.0, 2.2, -1.2), (8.0, -2.2, -1.2), (4.0, 0.0, -1.2),
                     (11.0, 1.6, -3.0), (11.0, -1.6, -3.0), (1.0, 2.0, 1.2),
                     (-34.0, 0.0, 0.4)], seated=False)

    # Electrolysers, amine scrubbers and the burners that finish the job. A nuclear boat
    # makes its own air, so the only thing that runs out is food.
    parts = [
        wf.box("atmosphere", (2.0, 2.6, -2.4), (6.0, 3.0, 3.2), k.body),
        wf.cylinder("atmosphere", (2.0, 2.6, -0.4), 0.9, 2.6, "Z", 16, k.trim),
        wf.box("atmosphere", (2.0, -2.6, -2.4), (6.0, 3.0, 3.2), k.body),
    ]
    for i in range(4):
        parts.append(wf.cylinder("atmosphere", (0.0 + i * 1.4, -2.6, -1.0), 0.42, 2.4,
                                 "Z", 14, k.trim))
    parts.append(wf.cylinder("atmosphere", (-6.0, 0.0, 2.2), 0.18, 26.0, "X", 10, k.dark))
    k.done("atmosphere", parts, bevel=0.06)

    parts = []
    for i in range(12):
        x = -44.0 + i * 8.5
        r = 4.9 if abs(x) < 30 else 4.9 * math.sqrt(max(0.06, 1 - ((abs(x) - 30) / 26) ** 2))
        parts.append(wf.cylinder("pressure_hull", (x, 0.0, 0.0), r, 0.30, "X", 24, k.body))
    for x in (32.0, -30.0):
        parts.append(wf.cylinder("pressure_hull", (x, 0.0, 0.0), 4.8, 0.50, "X", 24, k.trim))
    k.done("pressure_hull", parts, bevel=0.04, segments=1)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=60.0, floor=-6.0, distance=2.0)
