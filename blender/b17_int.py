"""
Inside the B-17G.

Ten men, four engines, and a bomb bay running through the middle of the fuselage with a
catwalk nine inches wide over it. Everything that made the aeroplane famous for coming
home in pieces is here: the crew are spread over twenty metres, so one hit rarely reaches
more than one of them, and the structure keeps flying with a great deal of it missing.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

NACELLE_IN, NACELLE_OUT = 4.65, 8.40
WING_Z = -0.85

MODULES = ("eng_inboard", "eng_outboard", "turbo", "bombbay", "pilot", "bombardier",
           "gunners", "ball_turret", "guns", "fuel", "oxygen", "armour",
           "electrics")


def wing_at(y):
    plan = [(0.0, 2.89, 4.82), (4.65, 2.72, 4.12), (8.40, 2.52, 3.32),
            (12.40, 2.26, 2.34), (15.41, 2.06, 1.62)]
    for (y0, le0, c0), (y1, le1, c1) in zip(plan, plan[1:]):
        if abs(y) <= y1:
            t = (abs(y) - y0) / max(1e-6, y1 - y0)
            return (le0 + (le1 - le0) * t, c0 + (c1 - c0) * t,
                    WING_Z + abs(y) * math.tan(math.radians(4.5)))
    return (2.06, 1.62, WING_Z + 15.41 * math.tan(math.radians(4.5)))


def nacelle_engine(k, name, ys):
    parts = []
    for y in ys:
        le, chord, z = wing_at(y)
        x = le + 0.95
        parts += wf.radial_engine(name, (x, y, z + 0.10), cylinders=9, radius=0.62,
                                  bore=0.17, rows=2, block=k.body, head=k.trim)
        parts.append(wf.cylinder(name, (x + 0.50, y, z + 0.10), 0.22, 0.30, "X", 16, k.trim))
        parts.append(wf.cylinder(name, (x - 0.70, y, z + 0.10), 0.26, 0.24, "X", 16, k.dark))
    return k.done(name, parts)


def build():
    k = intkit.Kit()

    # Wright R-1820 Cyclone: nine cylinders, 1,200 hp, and four of them.
    nacelle_engine(k, "eng_inboard", (-NACELLE_IN, NACELLE_IN))
    nacelle_engine(k, "eng_outboard", (-NACELLE_OUT, NACELLE_OUT))

    # Turbo-superchargers under each nacelle: exhaust-driven, and the reason a Fortress
    # can fly and fight at 25,000 feet where nothing without one can.
    parts = []
    for y in (-NACELLE_OUT, -NACELLE_IN, NACELLE_IN, NACELLE_OUT):
        le, chord, z = wing_at(y)
        parts.append(wf.cylinder("turbo", (le - chord * 0.15, y, z - 0.56), 0.24, 0.36,
                                 "Z", 16, k.body))
        parts.append(wf.cylinder("turbo", (le - chord * 0.15, y, z - 0.34), 0.14, 0.20,
                                 "Z", 12, k.trim))
        parts.append(wf.box("turbo", (le - chord * 0.40, y, z - 0.50), (0.90, 0.24, 0.20),
                            k.dark))
    k.done("turbo", parts)

    # The bomb bay: 2,700 kg on racks either side of a catwalk you edge along sideways.
    parts = []
    for sign in (-1, 1):
        for i in range(5):
            x = 2.00 - i * 0.80
            parts.append(wf.cylinder("bombbay", (x, sign * 0.52, -0.30), 0.17, 1.10,
                                     "Z", 14, k.body))
            parts.append(wf.cone("bombbay", (x, sign * 0.52, -0.92), 0.17, 0.04, 0.26,
                                 "Z", 12, k.trim))
            for a in range(4):
                ang = math.radians(45 + a * 90)
                fin = wf.box("bombbay", (x + 0.13 * math.cos(ang), sign * 0.52,
                                         0.22 + 0.13 * math.sin(ang)),
                             (0.02, 0.16, 0.16), k.trim)
                wf.rotate(fin, x=math.degrees(ang))
                parts.append(fin)
    parts.append(wf.box("bombbay", (0.40, 0.0, -0.96), (4.60, 0.24, 0.05), k.metal))
    for x in (2.40, -2.00):
        parts.append(wf.box("bombbay", (x, 0.0, -0.20), (0.08, 1.40, 1.60), k.dark))
    k.done("bombbay", parts, bevel=0.008, segments=1)

    # Pilot and copilot side by side - one module, two men, which is what `crowd` is for.
    k.crowd("pilot", [(8.00, 0.42, 0.44), (8.00, -0.42, 0.44)], seated=True)

    k.seats([("bombardier", (10.10, 0.0, -0.42), 1.0)], chair=False)

    # Waist, tail and top gunners, spread down the length of the fuselage.
    k.crowd("gunners", [(-4.30, 0.94, -0.10), (-4.30, -0.94, -0.10),
                        (-10.30, 0.0, 0.04), (6.87, 0.0, 0.52)], seated=False)

    # The Sperry ball: a man curled into a sphere under the belly, with no room for a
    # parachute and no way out unless the turret is powered.
    parts = [
        wf.sphere("ball_turret", (-3.86, 0.0, -1.42), 0.60, 20, k.body),
        wf.cylinder("ball_turret", (-3.86, 0.0, -1.96), 0.28, 0.22, "Z", 16, k.glass),
    ]
    parts += wf.figure("ball_turret", (-3.86, 0.0, -1.40), facing=1.0, seated=True,
                       body=k.trim, kit=k.dark, helmet=k.dark)
    k.done("ball_turret", parts, bevel=0.014)

    # Thirteen .50 calibre guns.
    parts = []
    for x, y, z in ((11.30, -0.22, -0.88), (11.30, 0.22, -0.88), (10.40, 0.72, 0.06),
                    (10.40, -0.72, 0.06), (7.60, 0.18, 1.60), (7.60, -0.18, 1.60),
                    (-3.40, 0.24, -1.86), (-3.40, -0.24, -1.86), (-4.80, 1.00, -0.10),
                    (-4.80, -1.00, -0.10), (-11.90, 0.16, 0.04), (-11.90, -0.16, 0.04)):
        parts.append(wf.cylinder("guns", (x, y, z), 0.040, 1.10, "X", 10, k.body))
        parts.append(wf.box("guns", (x - 0.70, y, z), (0.34, 0.14, 0.16), k.dark))
    k.done("guns", parts, bevel=0.006, segments=1)

    k.box_module("fuel", [(( wing_at(y)[0] - wing_at(y)[1] * 0.50, y, wing_at(y)[2]),
                           (1.60, 2.20, 0.44))
                          for y in (-10.6, -6.4, 6.4, 10.6)])

    # Oxygen bottles everywhere: at 25,000 feet a man without one is unconscious in two
    # minutes, so every station has its own supply.
    k.drum("oxygen", [(-6.20, 0.86, -0.30), (-6.20, -0.86, -0.30), (3.60, 0.80, -0.20),
                      (3.60, -0.80, -0.20), (9.00, 0.60, -0.40), (-9.60, 0.40, -0.10)],
           0.10, 0.72, axis="X")

    # Generators on every engine, a battery bay, and the inverters that run the turrets.
    parts = []
    for y in (-8.40, -4.65, 4.65, 8.40):
        le, chord, z = wing_at(y)
        parts.append(wf.cylinder("electrics", (le + 0.30, y, z - 0.18), 0.16, 0.34, "X",
                                 14, k.trim))
    parts.append(wf.box("electrics", (1.60, 0.0, 0.62), (1.30, 0.80, 0.44), k.body))
    for i in range(4):
        parts.append(wf.box("electrics", (1.10 + i * 0.34, 0.0, 0.62), (0.24, 0.70, 0.38),
                            k.trim))
    for x, y, z in ((6.87, 0.0, 1.20), (-3.86, 0.0, -1.00), (-11.30, 0.0, 0.10)):
        parts.append(wf.cylinder("electrics", (x, y, z), 0.13, 0.30, "Z", 12, k.dark))
    k.done("electrics", parts, bevel=0.010)

    k.plates("armour", [((7.20, 0.42, 0.50), (0.010, 0.50, 0.70), 0),
                        ((7.20, -0.42, 0.50), (0.010, 0.50, 0.70), 0),
                        ((-11.60, 0.0, 0.10), (0.012, 0.60, 0.70), 0)])
    return MODULES


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=26.0, floor=-3.6, distance=1.0)
