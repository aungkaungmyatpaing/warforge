"""
Inside SM U-9.

Two propulsion systems, because there is no one engine that works in both places: heavy
paraffin engines for the surface, electric motors and a hundred tonnes of lead-acid cells
for underwater. The batteries are under the deck plates the crew walk on, and when
seawater reaches them they give off chlorine.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 28.70, -28.70

MODULES = ("diesels", "motors", "batteries", "fuel", "torpedo_fwd", "control",
           "crew", "ballast", "periscope", "pressure_hull",
           "atmosphere")


def build():
    k = intkit.Kit()

    # Körting paraffin engines, not diesels: they smoked so heavily that the boat could
    # be seen from miles away, which is exactly why every navy went to diesel after this.
    parts = []
    for sign in (-1, 1):
        parts += wf.piston_engine("diesels", (-10.5, sign * 0.85, -1.10), cylinders=6,
                                  banks=1, bore=0.19, block=k.body, head=k.trim, pipe=k.dark)
        parts.append(wf.cylinder("diesels", (-13.6, sign * 0.85, -1.10), 0.34, 0.30, "X",
                                 16, k.dark))
    k.done("diesels", parts, bevel=0.012)

    parts = []
    for sign in (-1, 1):
        parts.append(wf.cylinder("motors", (-16.4, sign * 0.85, -1.10), 0.52, 2.60, "X",
                                 20, k.body))
        parts.append(wf.cylinder("motors", (-16.4, sign * 0.85, -1.10), 0.30, 2.80, "X",
                                 16, k.trim))
        parts.append(wf.cylinder("motors", (-18.2, sign * 0.85, -1.10), 0.14, 1.40, "X",
                                 12, k.metal))
    k.done("motors", parts, bevel=0.014)

    # The cells: rows of them under the deck plates, forward and aft of the control room.
    parts = []
    for x0, x1 in ((-6.0, 2.0), (6.0, 13.0)):
        n = int((x1 - x0) / 0.9)
        for i in range(n):
            for sign in (-1, 1):
                parts.append(wf.box("batteries", (x0 + i * 0.9, sign * 0.70, -1.90),
                                    (0.72, 0.56, 0.70), k.body))
                parts.append(wf.box("batteries", (x0 + i * 0.9, sign * 0.70, -1.52),
                                    (0.60, 0.44, 0.08), k.trim))
    k.done("batteries", parts, bevel=0.010, segments=1)

    k.box_module("fuel", [((-6.0, s * 1.60, -1.90), (7.0, 0.70, 1.10)) for s in (-1, 1)],
                 cylinders=[((-6.0, s * 1.60, -1.30), 0.10, 0.20, "Z") for s in (-1, 1)])

    # Two tubes forward, two aft, and six torpedoes for four tubes.
    parts = []
    for x, sign_x in ((21.0, 1), (-21.0, -1)):
        for dy in (-0.70, 0.70):
            parts.append(wf.cylinder("torpedo_fwd", (x, dy, -1.10), 0.28, 6.0, "X", 16,
                                     k.body))
            parts.append(wf.cylinder("torpedo_fwd", (x + sign_x * 3.2, dy, -1.10), 0.32,
                                     0.30, "X", 16, k.trim))
    for dy in (-0.70, 0.70):
        parts.append(wf.cylinder("torpedo_fwd", (14.0, dy, -1.70), 0.255, 5.2, "X", 14,
                                 k.trim))
    k.done("torpedo_fwd", parts, bevel=0.02)

    k.crowd("control", [(0.4, 0.6, -0.90), (0.4, -0.6, -0.90), (-1.4, 0.0, -0.90),
                        (1.6, 0.0, -0.90)], seated=False)
    k.crowd("crew", [(8.0, 0.7, -0.90), (8.0, -0.7, -0.90), (10.5, 0.0, -0.90),
                     (-8.0, 0.8, -0.70), (-8.0, -0.8, -0.70), (-12.0, 0.0, -0.70),
                     (16.0, 0.0, -0.90)], seated=False)

    # Ballast and trim tanks: what actually makes it a submarine rather than a boat.
    parts = []
    for sign in (-1, 1):
        parts.append(wf.box("ballast", (-2.0, sign * 2.05, -1.30), (16.0, 0.90, 1.90),
                            k.body))
        parts.append(wf.cylinder("ballast", (-2.0, sign * 2.05, -0.30), 0.13, 0.30, "Z",
                                 12, k.trim))
    for x in (19.0, -19.0):
        parts.append(wf.box("ballast", (x, 0.0, -1.90), (5.0, 2.40, 1.00), k.trim))
    k.done("ballast", parts, bevel=0.03)

    parts = []
    for x, height in ((0.55, 4.60), (-0.55, 3.90)):
        parts.append(wf.cylinder("periscope", (x, 0.0, 1.80), 0.075, 5.6, "Z", 10, k.body))
        parts.append(wf.box("periscope", (x, 0.0, -1.10), (0.26, 0.26, 1.20), k.trim))
        parts.append(wf.box("periscope", (x, 0.0, 4.40), (0.20, 0.14, 0.18), k.glass))
    k.done("periscope", parts, bevel=0.010, segments=1)

    # Potash cartridges and oxygen flasks: what the boat breathes once it has dived.
    parts = []
    for i in range(5):
        parts.append(wf.box("atmosphere", (-2.0 + i * 1.6, 1.30, -0.30),
                            (1.20, 0.34, 0.40), k.body))
    for dy in (-1.30, -1.60):
        for x in (5.0, 6.4):
            parts.append(wf.cylinder("atmosphere", (x, dy, -0.40), 0.13, 1.10, "X", 12,
                                     k.trim))
    parts.append(wf.cylinder("atmosphere", (0.0, 0.0, 0.40), 0.07, 12.0, "X", 8, k.dark))
    k.done("atmosphere", parts, bevel=0.014)

    # The pressure hull: a circular tube, because a circle is the only section that
    # resists outside pressure without wanting to fold.
    parts = []
    for i in range(13):
        x = -24.0 + i * 4.0
        r = 2.2 * math.sqrt(max(0.05, 1 - (x / 30.0) ** 2))
        parts.append(wf.cylinder("pressure_hull", (x, 0.0, -1.05), r, 0.18, "X", 22, k.body))
    for x in (13.5, -13.5):
        parts.append(wf.cylinder("pressure_hull", (x, 0.0, -1.05), 2.0, 0.24, "X", 22,
                                 k.trim))
    k.done("pressure_hull", parts, bevel=0.02, segments=1)
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=32.0, floor=-4.0, distance=1.2)
