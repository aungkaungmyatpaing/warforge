"""
MiG-15bis, at full size.

A nose intake, 35 degrees of sweep and a barrel of a fuselage wrapped round a copy of a
Rolls-Royce Nene. It out-climbed and out-gunned everything the UN had over Korea until
the Sabre arrived, and 18,000 were built.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

ALUMINIUM = (0.60, 0.62, 0.64)
STEEL = (0.38, 0.39, 0.40)
TYRE = (0.07, 0.07, 0.08)
GLASS = (0.26, 0.34, 0.36)

# Length 10.10, span 10.08, height 3.70.
NOSE = 5.05
TAIL = -5.05
HALF_SPAN = 5.04
WING_Z = -0.24
SWEEP = 35.0
ROOT_LE = 0.90
ROOT_CHORD = 2.62
FIN_TOP = 2.10


def wing_stations():
    out = []
    plan = [(0.00, 1.00), (1.70, 0.86), (3.40, 0.70), (4.70, 0.54), (HALF_SPAN, 0.34)]
    for y, frac in plan:
        chord = ROOT_CHORD * frac
        le = ROOT_LE - y * math.tan(math.radians(SWEEP))
        out.append((y, le, chord, 0.10, WING_Z + y * math.tan(math.radians(-2.0))))
    return out


def wing_at(y):
    stations = wing_stations()
    span = abs(y)
    for (y0, le0, c0, _, z0), (y1, le1, c1, _, z1) in zip(stations, stations[1:]):
        if span <= y1:
            t = (span - y0) / max(1e-6, y1 - y0)
            return (le0 + (le1 - le0) * t, c0 + (c1 - c0) * t, z0 + (z1 - z0) * t)
    last = stations[-1]
    return (last[1], last[2], last[4])


def fuselage(paint, metal, glass):
    """A fat tube with the jet pipe running straight through it, nose to tail."""
    parts = []
    stations = [
        (TAIL, 0.40, 0.40, 0.24),
        (-4.20, 0.52, 0.54, 0.20),
        (-2.80, 0.66, 0.70, 0.12),
        (-1.20, 0.76, 0.82, 0.04),
        (0.40, 0.80, 0.86, 0.00),
        (2.00, 0.78, 0.82, -0.02),
        (3.40, 0.70, 0.72, -0.02),
        (4.40, 0.60, 0.60, -0.02),
        (NOSE - 0.10, 0.52, 0.52, -0.02),
    ]
    sections = [(x, wf.oval(hy, hz, 20, centre_z=cz)) for x, hy, hz, cz in stations]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint), 0.016, 2, smooth_angle=52))
    # Airbrakes on the rear fuselage sides.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("airbrake", (-3.00, sign * 0.62, 0.10), (0.80, 0.06, 0.56), paint),
            0.02, 2))
    # Dorsal spine to the fin root.
    spine = [
        (-1.00, wf.oval(0.14, 0.06, 8, centre_z=0.82)),
        (-2.60, wf.oval(0.16, 0.14, 8, centre_z=0.78)),
        (-4.10, wf.oval(0.16, 0.24, 8, centre_z=0.66)),
    ]
    parts.append(wf.finish(wf.loft("spine", spine, paint), 0.02, 2, smooth_angle=48))
    return wf.join("fuselage", parts)


def intake(paint, metal, dark):
    """The nose intake, with its splitter for the radar-less nose cone."""
    parts = []
    lip = wf.cylinder("intake", (NOSE - 0.06, 0, -0.02), 0.54, 0.30, "X", 26, paint)
    wf.cut(lip, wf.cylinder("cut", (NOSE - 0.06, 0, -0.02), 0.44, 0.60, "X", 26))
    parts.append(wf.finish(lip, 0.02, 3))
    parts.append(wf.finish(
        wf.cylinder("duct", (NOSE - 0.70, 0, -0.02), 0.44, 1.10, "X", 22, dark), 0.02, 2))
    # The splitter down the middle of the intake, dividing it round the cockpit.
    parts.append(wf.finish(
        wf.box("splitter", (NOSE - 0.50, 0, -0.02), (1.00, 0.08, 0.86), paint), 0.02, 2))
    return wf.join("intake", parts)


def wings(paint, metal):
    parts = [wf.finish(wf.wing("wing", wing_stations(), paint), 0.010, 2, smooth_angle=56)]
    return wf.mirror_y(wf.join("wing", parts))


def fences(metal, paint):
    """
    Wing fences: two a side, and they are the giveaway. A swept wing stalls from the tip
    inwards, and the fence is the cheap fix - a plate that stops the boundary layer
    walking outboard.
    """
    parts = []
    for sign in (-1, 1):
        for y_at in (2.30, 3.70):
            y = sign * y_at
            le, chord, z = wing_at(y)
            parts.append(wf.finish(
                wf.box("fence", (le - chord * 0.45, y, z + 0.12),
                       (chord * 0.80, 0.03, 0.20), paint), 0.008, 1))
    return wf.join("fence", parts)


def tailplane(paint):
    """Mid-set on the fin, swept like the wing."""
    stations = [
        (0.00, -3.30, 1.30, 0.10, 1.44),
        (0.80, -3.58, 1.08, 0.10, 1.46),
        (1.52, -3.86, 0.84, 0.10, 1.48),
        (1.92, -4.04, 0.52, 0.11, 1.48),
    ]
    return wf.mirror_y(wf.finish(wf.wing("tail", stations, paint), 0.010, 2, smooth_angle=56))


def tailfin(paint, dark):
    shape = wf.fin("fin", [
        (0.42, -2.70, 2.70, 0.11, 0.0),
        (1.10, -3.10, 2.20, 0.10, 0.0),
        (1.70, -3.50, 1.60, 0.10, 0.0),
        (FIN_TOP, -3.84, 1.00, 0.11, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.012, 2, smooth_angle=56)]
    parts.append(wf.finish(
        wf.box("rudderpost", (-4.50, 0, 1.20), (0.06, 0.08, 1.60), dark), 0.008, 1))
    return wf.join("fin", parts)


def nozzle(metal, dark):
    parts = [
        wf.finish(wf.cylinder("nozzle", (TAIL + 0.14, 0, 0.24), 0.40, 0.34, "X", 22, metal),
                  0.02, 2),
        wf.finish(wf.cylinder("jetpipe", (TAIL - 0.10, 0, 0.24), 0.34, 0.30, "X", 22, dark),
                  0.02, 2),
    ]
    return wf.join("nozzle", parts)


def canopy(glass, paint, metal):
    parts = [
        wf.finish(wf.box("windscreen", (2.30, 0, 0.86), (0.50, 0.62, 0.34), glass), 0.10, 3),
    ]
    hood = [
        (2.00, wf.oval(0.36, 0.28, 14, centre_z=0.80)),
        (1.30, wf.oval(0.40, 0.34, 14, centre_z=0.82)),
        (0.50, wf.oval(0.36, 0.28, 14, centre_z=0.82)),
        (0.05, wf.oval(0.24, 0.14, 14, centre_z=0.80)),
    ]
    parts.append(wf.finish(wf.loft("canopy", hood, glass), 0.016, 2, smooth_angle=56))
    parts.append(wf.finish(
        wf.box("frame", (2.04, 0, 0.84), (0.06, 0.76, 0.40), paint), 0.01, 1))
    return wf.join("canopy", parts)


def gear_nose(metal, dark, paint):
    parts = [
        wf.finish(wf.cylinder("noseleg", (3.30, 0, -1.00), 0.07, 1.10, "Z", 12, metal), 0.01, 2),
        wf.finish(wf.cylinder("nosetyre", (3.30, 0, -1.58), 0.28, 0.14, "Y", 18, dark),
                  0.03, 3),
        wf.finish(wf.box("nosedoor", (3.30, 0.18, -0.76), (0.86, 0.05, 0.50), paint), 0.01, 1),
    ]
    return wf.join("gear_nose", parts)


def gear_main(metal, dark, paint):
    parts = []
    for sign in (-1, 1):
        y = sign * 1.70
        le, chord, z = wing_at(abs(y))
        parts.append(wf.finish(
            wf.rotate(wf.cylinder("mainleg", (le - chord * 0.4, y, z - 0.70), 0.08, 1.10,
                                  "Z", 12, metal), x=sign * 6), 0.01, 2))
        parts.append(wf.finish(
            wf.cylinder("maintyre", (le - chord * 0.4, y, z - 1.34), 0.36, 0.18, "Y", 20, dark),
            0.03, 3))
        parts.append(wf.finish(
            wf.box("maindoor", (le - chord * 0.4, y - sign * 0.16, z - 0.56),
                   (0.80, 0.05, 0.60), paint), 0.01, 1))
    return wf.join("gear_main", parts)


def guns(metal, dark, paint):
    """One 37 mm and two 23 mm, all under the nose on a tray that winched down."""
    parts = [
        wf.finish(wf.box("guntray", (3.70, 0, -0.62), (1.40, 0.60, 0.24), paint), 0.04, 2),
        wf.finish(wf.cone("n37", (4.70, -0.22, -0.60), 0.062, 0.050, 1.30, "X", 12, dark),
                  0.008, 2),
    ]
    for dy in (0.16, 0.30):
        parts.append(wf.finish(
            wf.cone("nr23", (4.60, dy, -0.60), 0.045, 0.036, 1.10, "X", 10, dark), 0.006, 1))
    return wf.join("guns", parts)


def tanks(paint, metal):
    """Slipper tanks under the wings - a MiG-15 rarely flew without them."""
    parts = []
    for sign in (-1, 1):
        y = sign * 2.90
        le, chord, z = wing_at(abs(y))
        body = [
            (le + 0.40, wf.oval(0.04, 0.04, 12, centre_z=z - 0.44)),
            (le + 0.10, wf.oval(0.22, 0.22, 12, centre_z=z - 0.46)),
            (le - 1.10, wf.oval(0.26, 0.26, 12, centre_z=z - 0.46)),
            (le - 2.10, wf.oval(0.16, 0.16, 12, centre_z=z - 0.44)),
            (le - 2.40, wf.oval(0.03, 0.03, 12, centre_z=z - 0.44)),
        ]
        parts.append(wf.finish(
            wf.loft("tank", [(x, [(yy + y, zz) for yy, zz in pts]) for x, pts in body], paint),
            0.02, 3, smooth_angle=56))
        parts.append(wf.finish(
            wf.box("pylon", (le - 0.60, y, z - 0.26), (0.60, 0.08, 0.30), paint), 0.02, 2))
    return wf.join("tank", parts)


PALETTE_BODY = (0xB4 / 255, 0xB8 / 255, 0xBC / 255)
PANEL_LIGHT = (1.08, 1.08, 1.09)
PANEL_DARK = (0.88, 0.89, 0.91)
MARK_RED = (0.56, 0.09, 0.09)

PARTS = ("fuselage", "intake", "wing", "fence", "tail", "fin", "nozzle", "canopy",
         "gear_nose", "gear_main", "guns", "tank", "star")


def weathering():
    for name in ("fuselage", "wing", "tail", "fin", "intake", "tank", "fence"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.34)
        wf.weather(obj, camo=(PANEL_LIGHT, PANEL_DARK), cover=(0.28, 0.28), dirt=0.0,
                   wear=0.22, scale=1.5, seed=89, ground=-1.8, top=1.4)
    for name in ("gear_nose", "gear_main", "nozzle", "guns", "canopy"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.28, wear=0.30, seed=97, ground=-1.8, top=1.0)


def markings(paint):
    """Red stars on the wings, the fin and the rear fuselage."""
    red = wf.tint_for(MARK_RED, PALETTE_BODY)
    added = []
    for sign in (-1, 1):
        y = sign * 3.30
        le, chord, z = wing_at(abs(y))
        for up in (1, -1):
            added.append(wf.flat_tint(
                wf.star("star", (le - chord * 0.50, y, z + up * 0.07), 0.52, 0.02, paint),
                red))
    body = bpy.data.objects.get("fuselage")
    if body is not None:
        for sign in (-1, 1):
            barrel = wf.cylinder("cut", (-3.20, sign * 1.2, 0.10), 0.40, 1.6, "Y", 22)
            added.append(wf.flat_tint(
                wf.decal("star", body, barrel, paint, centre=(-3.20, 0.0, 0.10), swell=1.008),
                red))
    wf.join("star", added)


def build():
    wf.reset()
    paint = wf.role("BODY", ALUMINIUM, roughness=0.30, metallic=0.55)
    metal = wf.role("METAL", STEEL, roughness=0.34, metallic=0.85)
    dark = wf.role("DARK", TYRE, roughness=0.70, metallic=0.30)
    glass = wf.role("GLASS", GLASS, roughness=0.10, metallic=0.20)

    fuselage(paint, metal, glass)
    intake(paint, metal, dark)
    wings(paint, metal)
    fences(metal, paint)
    tailplane(paint)
    tailfin(paint, dark)
    nozzle(metal, dark)
    canopy(glass, paint, metal)
    gear_nose(metal, dark, paint)
    gear_main(metal, dark, paint)
    guns(metal, dark, paint)
    tanks(paint, metal)

    weathering()
    markings(paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(11.0, floor_z=-2.0)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
