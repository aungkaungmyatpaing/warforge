"""
Junkers Ju 52/3m, at full size.

Corrugated duralumin, three engines and a fixed undercarriage - obsolete-looking in 1932
and still flying transport in 1945. The corrugation is not decoration: it is the
structure, and it is the one thing that has to be right or the aeroplane is unrecognisable.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

DURAL = (0.52, 0.53, 0.54)
STEEL = (0.36, 0.37, 0.38)
TYRE = (0.08, 0.08, 0.09)
GLASS = (0.24, 0.32, 0.34)

# Length 18.90, span 29.25, height 4.52.
NOSE = 9.45
TAIL = -9.45
HALF_SPAN = 14.62
WING_Z = -0.72
ROOT_LE = 2.10
ROOT_CHORD = 4.60
ENGINE_Y = 5.30
PROP_R = 1.45
RIPPLES = 26             # corrugations round the fuselage


def fuselage(paint, metal, glass):
    """Slab-sided and corrugated, with a squared-off nose for the third engine."""
    parts = []
    stations = [
        (TAIL, 0.10, 0.20, 0.34),
        (-7.60, 0.44, 0.60, 0.26),
        (-5.40, 0.80, 1.00, 0.12),
        (-2.60, 1.02, 1.28, 0.02),
        (0.60, 1.06, 1.32, 0.00),
        (3.40, 1.02, 1.26, 0.02),
        (5.80, 0.86, 1.06, 0.06),
        (7.40, 0.62, 0.76, 0.10),
    ]
    sections = [
        (x, wf.oval(hy, hz, 40, centre_z=cz, flatten_bottom=0.26,
                    ripple=0.012, ripples=RIPPLES))
        for x, hy, hz, cz in stations
    ]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint), 0.010, 1, smooth_angle=24))
    return wf.join("fuselage", parts)


def wing_stations():
    """A thick, almost untapered wing with the double-wing flap hung behind it."""
    plan = [
        (0.00, ROOT_LE, ROOT_CHORD),
        (4.00, 2.00, 4.30),
        (ENGINE_Y, 1.94, 4.10),
        (9.60, 1.70, 3.30),
        (13.40, 1.40, 2.40),
        (HALF_SPAN - 0.45, 1.22, 1.80),
        (HALF_SPAN, 1.05, 1.10),
    ]
    return [(y, le, chord, 0.18 - 0.05 * (y / HALF_SPAN),
             WING_Z + y * math.tan(math.radians(2.0)))
            for y, le, chord in plan]


def wing_at(y):
    stations = wing_stations()
    span = abs(y)
    for (y0, le0, c0, _, z0), (y1, le1, c1, _, z1) in zip(stations, stations[1:]):
        if span <= y1:
            t = (span - y0) / max(1e-6, y1 - y0)
            return (le0 + (le1 - le0) * t, c0 + (c1 - c0) * t, z0 + (z1 - z0) * t)
    last = stations[-1]
    return (last[1], last[2], last[4])


def wings(paint, metal):
    parts = [wf.finish(wf.wing("wing", wing_stations(), paint), 0.012, 1, smooth_angle=26)]
    # The trailing "double wing" - Junkers' separate full-span flap and aileron surface.
    for sign in (-1, 1):
        for y0, y1 in ((1.60, 8.00), (8.40, 14.00)):
            mid = (y0 + y1) * 0.5
            le, chord, z = wing_at(mid)
            parts.append(wf.finish(
                wf.box("doublewing", (le - chord - 0.34, sign * mid, z - 0.18),
                       (0.70, y1 - y0, 0.11), paint), 0.02, 1))
            for yy in (y0 + 0.2, mid, y1 - 0.2):
                _, _, zz = wing_at(yy)
                parts.append(wf.finish(
                    wf.box("flaparm", (le - chord + 0.02, sign * yy, zz - 0.14),
                           (0.50, 0.07, 0.07), metal), 0.01, 1))
    # Root fairing.
    fair = [
        (ROOT_LE + 0.40, wf.oval(1.20, 0.30, 12, centre_z=WING_Z + 0.16)),
        (0.20, wf.oval(1.40, 0.42, 12, centre_z=WING_Z + 0.22)),
        (ROOT_LE - ROOT_CHORD - 0.40, wf.oval(1.00, 0.24, 12, centre_z=WING_Z + 0.14)),
    ]
    parts.append(wf.finish(wf.loft("fairing", fair, paint), 0.04, 2, smooth_angle=44))
    return wf.mirror_y(wf.join("wing", parts))


def nose_engine(paint, metal, dark):
    """BMW 132 radial in the nose, hung right on the front of the fuselage."""
    parts = []
    body = [
        (7.40, wf.oval(0.64, 0.78, 20, centre_z=0.10)),
        (8.30, wf.oval(0.74, 0.80, 20, centre_z=0.08)),
        (NOSE - 0.10, wf.oval(0.74, 0.78, 20, centre_z=0.06)),
    ]
    parts.append(wf.finish(wf.loft("eng_nose", body, paint), 0.03, 2, smooth_angle=44))
    ring = wf.cylinder("cowlring", (NOSE - 0.06, 0, 0.06), 0.82, 0.30, "X", 22, metal)
    wf.cut(ring, wf.cylinder("cut", (NOSE - 0.06, 0, 0.06), 0.62, 0.50, "X", 22))
    parts.append(wf.finish(ring, 0.02, 2))
    for i in range(9):
        a = 2 * math.pi * i / 9
        parts.append(wf.finish(
            wf.box("cylinder", (NOSE - 0.26, 0.50 * math.cos(a), 0.06 + 0.50 * math.sin(a)),
                   (0.24, 0.16, 0.16), dark), 0.02, 2))
    # Exhaust collector ring.
    parts.append(wf.finish(
        wf.cylinder("collector", (NOSE + 0.06, 0, 0.06), 0.68, 0.10, "X", 20, dark), 0.02, 2))
    return wf.join("eng_nose", parts)


def wing_engines(paint, metal, dark):
    """
    The two wing engines, slung below and ahead of the leading edge on open frames -
    which is why a Ju 52 looks like it has its engines bolted on as an afterthought.
    """
    parts = []
    for sign in (-1, 1):
        y = sign * ENGINE_Y
        le, chord, z = wing_at(abs(y))
        front = le + 1.40
        nac = [
            (front, wf.oval(0.66, 0.66, 18, centre_z=z - 0.46)),
            (front - 0.70, wf.oval(0.70, 0.70, 18, centre_z=z - 0.48)),
            (le - 0.10, wf.oval(0.60, 0.58, 18, centre_z=z - 0.46)),
            (le - chord * 0.55, wf.oval(0.34, 0.32, 18, centre_z=z - 0.38)),
        ]
        parts.append(wf.finish(
            wf.loft("eng_left", [(x, [(yy + y, zz) for yy, zz in pts]) for x, pts in nac],
                    paint), 0.03, 2, smooth_angle=44))
        ring = wf.cylinder("cowlring", (front - 0.04, y, z - 0.46), 0.76, 0.28, "X", 20, metal)
        wf.cut(ring, wf.cylinder("cut", (front - 0.04, y, z - 0.46), 0.58, 0.46, "X", 20))
        parts.append(wf.finish(ring, 0.02, 2))
        for i in range(9):
            a = 2 * math.pi * i / 9
            parts.append(wf.finish(
                wf.box("cylinder", (front - 0.24, y + 0.46 * math.cos(a),
                                    z - 0.46 + 0.46 * math.sin(a)),
                       (0.22, 0.15, 0.15), dark), 0.02, 2))
        # The strut frame carrying it under the wing.
        for dx, dz in ((0.40, 0.0), (-0.40, 0.0)):
            parts.append(wf.finish(
                wf.box("mount", (le + dx, y, z - 0.22), (0.10, 0.10, 0.44), metal), 0.014, 1))
    return wf.join("eng_left", parts)


def props(name, positions, metal, dark):
    parts = []
    for x, y, z in positions:
        parts.append(wf.finish(
            wf.cylinder("boss", (x, y, z), 0.18, 0.22, "X", 16, metal), 0.02, 2))
        for i in range(2):
            blade = wf.wing("blade", [
                (0.16, 0.16, 0.32, 0.18, 0.0),
                (0.60, 0.13, 0.36, 0.11, 0.0),
                (1.10, 0.09, 0.30, 0.09, 0.0),
                (PROP_R, 0.03, 0.14, 0.08, 0.0),
            ], dark)
            wf.bake(blade)
            blade.location = (x + 0.06, y, z)
            wf.rotate(blade, x=90 + i * 180, y=-20)
            parts.append(wf.finish(blade, 0.006, 2, smooth_angle=56))
    return wf.join(name, parts)


def tailplane(paint):
    stations = [
        (0.00, -7.30, 2.40, 0.13, 0.50),
        (1.80, -7.50, 2.06, 0.13, 0.52),
        (3.30, -7.72, 1.60, 0.13, 0.54),
        (4.10, -7.90, 1.00, 0.14, 0.54),
    ]
    parts = [wf.finish(wf.wing("tail", stations, paint), 0.014, 1, smooth_angle=26)]
    # Strut bracing under the tailplane, which every Ju 52 carried.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.rotate(wf.box("tailstrut", (-7.20, sign * 2.20, 0.16), (0.09, 0.09, 0.90), STRUT[0]),
                      x=sign * 24), 0.012, 1))
    return wf.mirror_y(wf.join("tail", parts))


def tailfin(paint, dark):
    shape = wf.fin("fin", [
        (0.60, -6.90, 2.60, 0.13, 0.0),
        (1.40, -7.20, 2.20, 0.12, 0.0),
        (2.20, -7.60, 1.60, 0.12, 0.0),
        (2.66, -7.90, 1.00, 0.13, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.014, 1, smooth_angle=26)]
    parts.append(wf.finish(
        wf.box("rudderpost", (-8.40, 0, 1.50), (0.07, 0.09, 2.00), dark), 0.01, 1))
    return wf.join("fin", parts)


def gear(metal, dark, paint):
    """Fixed, spatted, and hung on a big faired leg - no retraction anywhere."""
    parts = []
    for sign in (-1, 1):
        y = sign * 3.20
        le, chord, z = wing_at(abs(y))
        parts.append(wf.finish(
            wf.rotate(wf.box("legfairing", (le - 0.40, y, z - 1.10), (0.50, 0.22, 1.70), paint),
                      y=-6), 0.04, 2))
        parts.append(wf.finish(
            wf.rotate(wf.box("dragstrut", (le - 1.40, y, z - 1.00), (1.80, 0.14, 0.20), paint),
                      y=28), 0.03, 2))
        parts.append(wf.finish(
            wf.cylinder("tyre", (le - 0.50, y, z - 2.00), 0.58, 0.26, "Y", 22, dark), 0.05, 3))
        parts.append(wf.finish(
            wf.cylinder("hub", (le - 0.50, y, z - 2.00), 0.22, 0.30, "Y", 14, metal), 0.02, 2))
    return wf.join("gear", parts)


def tailwheel(metal, dark):
    parts = [
        wf.finish(wf.cylinder("tailleg", (-8.00, 0, -0.30), 0.09, 0.70, "Z", 10, metal),
                  0.01, 1),
        wf.finish(wf.cylinder("tailtyre", (-8.00, 0, -0.70), 0.28, 0.14, "Y", 16, dark),
                  0.03, 2),
    ]
    return wf.join("tailwheel", parts)


def windows(glass, paint):
    """Cabin windows down both sides - seventeen passengers in airline trim."""
    parts = []
    for sign in (-1, 1):
        for i in range(6):
            x = 3.00 - i * 1.20
            parts.append(wf.finish(
                wf.box("window", (x, sign * 1.06, 0.36), (0.54, 0.06, 0.50), glass), 0.03, 2))
    return wf.join("windows", parts)


def cockpit(glass, paint, metal):
    parts = [
        wf.finish(wf.box("cockpit", (5.20, 0, 1.14), (2.00, 1.70, 0.62), glass), 0.06, 3),
    ]
    for i in range(4):
        parts.append(wf.finish(
            wf.box("frame", (4.40 + i * 0.54, 0, 1.20), (0.06, 1.76, 0.60), paint), 0.01, 1))
    return wf.join("cockpit", parts)


def door(paint, metal):
    parts = [
        wf.finish(wf.box("door", (-3.20, 1.06, 0.10), (1.10, 0.06, 1.50), paint), 0.02, 2),
        wf.finish(wf.box("handle", (-2.80, 1.12, 0.10), (0.12, 0.06, 0.10), metal), 0.01, 1),
    ]
    return wf.join("door", parts)


PALETTE_BODY = (0x8A / 255, 0x8D / 255, 0x90 / 255)
PANEL_LIGHT = (1.10, 1.10, 1.11)
PANEL_DARK = (0.88, 0.89, 0.91)
STRUT = [None]

PARTS = ("fuselage", "wing", "eng_nose", "prop_nose", "eng_left", "prop_left",
         "tail", "fin", "gear", "tailwheel", "windows", "cockpit", "door")


def weathering():
    for name in ("fuselage", "wing", "eng_nose", "eng_left", "tail", "fin", "gear", "door"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.55)
        wf.weather(obj, camo=(PANEL_LIGHT, PANEL_DARK), cover=(0.28, 0.28), dirt=0.16,
                   wear=0.24, scale=2.2, seed=79, ground=-2.4, top=2.0)
    for name in ("prop_nose", "prop_left", "tailwheel", "cockpit", "windows"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.22, wear=0.28, seed=83, ground=-2.4, top=2.0)


def build():
    wf.reset()
    paint = wf.role("BODY", DURAL, roughness=0.40, metallic=0.45)
    metal = wf.role("METAL", STEEL, roughness=0.38, metallic=0.80)
    dark = wf.role("DARK", TYRE, roughness=0.72, metallic=0.25)
    glass = wf.role("GLASS", GLASS, roughness=0.14, metallic=0.25)
    STRUT[0] = metal

    fuselage(paint, metal, glass)
    wings(paint, metal)
    nose_engine(paint, metal, dark)
    wing_engines(paint, metal, dark)
    props("prop_nose", [(NOSE + 0.34, 0.0, 0.06)], metal, dark)
    side = []
    for sign in (-1, 1):
        y = sign * ENGINE_Y
        le, _, z = wing_at(abs(y))
        side.append((le + 1.76, y, z - 0.46))
    props("prop_left", side, metal, dark)
    tailplane(paint)
    tailfin(paint, dark)
    gear(metal, dark, paint)
    tailwheel(metal, dark)
    windows(glass, paint)
    cockpit(glass, paint, metal)
    door(paint, metal)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(22.0, floor_z=-2.8)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=18)
    if len(args) > 1:
        wf.export_glb(args[1])
