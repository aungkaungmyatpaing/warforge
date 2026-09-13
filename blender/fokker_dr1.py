"""
Fokker Dr.I triplane, at full size.

Three short wings instead of two long ones: the span comes down, the roll rate goes up,
and the thick cantilever section needs almost no bracing - which is why a Dr.I has one
strut a side and a Camel has a cat's cradle. It climbed and turned better than anything
else in 1917 and was slower than all of it.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

STREAK = (0.36, 0.13, 0.11)
LINEN = (0.60, 0.55, 0.40)
STEEL = (0.34, 0.35, 0.36)
TIMBER = (0.36, 0.27, 0.16)
DARKWOOD = (0.16, 0.12, 0.07)

NOSE = 2.40
TAIL = -3.37
TOP_HALF = 3.60          # 7.20 m upper span
MID_HALF = 3.16
LOW_HALF = 2.83
TOP_Z = 1.42
MID_Z = 0.38
LOW_Z = -0.56
CHORD = 1.00
PROP_R = 1.32


def fuselage(paint, metal, wood, dark):
    """Welded steel tube under fabric: flat sides, a rounded turtledeck, and a slab nose."""
    parts = []
    stations = [
        (TAIL, 0.05, 0.10, 0.26),
        (-2.50, 0.16, 0.24, 0.20),
        (-1.50, 0.26, 0.34, 0.12),
        (-0.50, 0.34, 0.42, 0.04),
        (0.50, 0.36, 0.44, 0.00),
        (1.40, 0.36, 0.44, 0.00),
        (NOSE - 0.36, 0.34, 0.42, 0.00),
    ]
    sections = [(x, wf.oval(hy, hz, 12, centre_z=cz, flatten_bottom=0.16))
                for x, hy, hz, cz in stations]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint), 0.014, 2, smooth_angle=40))
    parts.append(wf.finish(
        wf.cylinder("coaming", (-0.20, 0, 0.44), 0.28, 0.09, "Z", 16, wood), 0.018, 2))
    parts.append(wf.finish(
        wf.cylinder("cockpit", (-0.20, 0, 0.38), 0.24, 0.12, "Z", 16, dark), 0.01, 1))
    # Headrest fairing behind the cockpit.
    parts.append(wf.finish(
        wf.sphere("headrest", (-0.62, 0, 0.42), 0.20, 14, paint), 0.02, 2))
    return wf.join("fuselage", parts)


def triplane_wing(name, z, half, chord, le, paint, cut_centre=0.0):
    stations = [
        (0.00, le, chord, 0.135, z),
        (half * 0.60, le, chord, 0.130, z),
        (half - 0.26, le - 0.03, chord * 0.98, 0.125, z),
        (half, le - 0.16, chord * 0.70, 0.130, z),
    ]
    panel = wf.wing(name, stations, paint)
    if cut_centre:
        wf.cut(panel, wf.box("cut", (le - chord * 0.40, 0, z), (chord * 0.7, cut_centre, 0.6)))
    return wf.mirror_y(wf.finish(panel, 0.006, 2, smooth_angle=54))


def top_wing(paint):
    return triplane_wing("top_wing", TOP_Z, TOP_HALF, CHORD, 0.72, paint, cut_centre=0.44)


def mid_wing(paint):
    return triplane_wing("mid_wing", MID_Z, MID_HALF, CHORD, 0.74, paint)


def low_wing(paint):
    return triplane_wing("low_wing", LOW_Z, LOW_HALF, CHORD, 0.70, paint)


def struts(wood, metal, paint):
    """
    One interplane strut a side, well outboard, plus the cabanes. Fokker's thick wing
    carried its own loads, so the aeroplane needed almost no bracing - which is exactly
    what made people distrust it at first.
    """
    parts = []
    for sign in (-1, 1):
        y = sign * 2.62
        for z0, z1 in ((LOW_Z, MID_Z), (MID_Z, TOP_Z)):
            parts.append(wf.finish(
                wf.box("strut", (0.30, y, (z0 + z1) * 0.5), (0.13, 0.05, z1 - z0), wood),
                0.012, 2))
    # Cabane struts from the fuselage up to the top wing.
    for sign in (-1, 1):
        for dx in (0.60, -0.14):
            parts.append(wf.finish(
                wf.rotate(wf.box("cabane", (dx, sign * 0.26, (TOP_Z + 0.42) * 0.5),
                                 (0.09, 0.05, TOP_Z - 0.42), metal),
                          x=-sign * 10), 0.010, 2))
    # The axle wing, which is a fourth aerofoil in all but name.
    parts.append(wf.finish(
        wf.box("axlewing", (0.52, 0, -1.30), (0.50, 1.86, 0.09), paint), 0.02, 2))
    return wf.join("struts", parts)


def engine(metal, paint, dark):
    """Oberursel Ur.II rotary, in a full cowl with a scalloped lower opening."""
    parts = []
    cowl = wf.cylinder("cowl", (NOSE - 0.22, 0, 0.0), 0.44, 0.50, "X", 22, metal)
    wf.cut(cowl, wf.cylinder("cut", (NOSE - 0.22, 0, 0.0), 0.36, 0.66, "X", 22))
    wf.cut(cowl, wf.box("cut", (NOSE - 0.10, 0, -0.48), (0.60, 0.44, 0.36)))
    parts.append(wf.finish(cowl, 0.02, 3))
    parts.append(wf.finish(
        wf.cylinder("cowlface", (NOSE - 0.02, 0, 0.0), 0.44, 0.06, "X", 22, metal), 0.02, 2))
    wf.cut(parts[-1], wf.cylinder("cut", (NOSE - 0.02, 0, 0.10), 0.20, 0.30, "X", 16))
    for i in range(9):
        a = 2 * math.pi * i / 9
        parts.append(wf.finish(
            wf.cylinder("cylinder", (NOSE - 0.28, 0.25 * math.cos(a), 0.25 * math.sin(a)),
                        0.085, 0.26, "X", 10, dark), 0.014, 2))
    return wf.join("engine", parts)


def propeller(wood, dark):
    parts = [wf.finish(wf.cylinder("boss", (NOSE + 0.10, 0, 0.0), 0.11, 0.14, "X", 14, dark),
                       0.014, 2)]
    for i in range(2):
        blade = wf.wing("blade", [
            (0.10, 0.13, 0.26, 0.16, 0.0),
            (0.58, 0.11, 0.28, 0.10, 0.0),
            (1.02, 0.08, 0.23, 0.08, 0.0),
            (PROP_R, 0.02, 0.10, 0.08, 0.0),
        ], dark)
        wf.bake(blade)
        blade.location = (NOSE + 0.12, 0, 0.0)
        wf.rotate(blade, x=90 + i * 180, y=-18)
        parts.append(wf.finish(blade, 0.005, 2, smooth_angle=56))
    return wf.join("prop", parts)


def tailplane(paint):
    """The Dr.I's tailplane is a wide, almost triangular plank - very hard to mistake."""
    stations = [
        (0.00, -2.52, 0.96, 0.09, 0.06),
        (0.60, -2.60, 0.80, 0.09, 0.06),
        (1.10, -2.74, 0.56, 0.09, 0.06),
        (1.32, -2.86, 0.32, 0.10, 0.06),
    ]
    return wf.mirror_y(wf.finish(wf.wing("tail", stations, paint), 0.006, 2, smooth_angle=54))


def rudder(paint, metal):
    """The comma-shaped balanced rudder, with no fixed fin ahead of it at all."""
    shape = wf.fin("rudder", [
        (0.02, -2.66, 0.80, 0.09, 0.0),
        (0.34, -2.72, 0.86, 0.09, 0.0),
        (0.66, -2.86, 0.74, 0.09, 0.0),
        (0.86, -3.04, 0.44, 0.10, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.006, 2, smooth_angle=54)]
    parts.append(wf.finish(
        wf.box("post", (-2.96, 0, 0.40), (0.04, 0.05, 0.82), metal), 0.005, 1))
    return wf.join("rudder", parts)


def gear(metal, wood, dark, paint):
    parts = []
    for sign in (-1, 1):
        for lean in (0.30, -0.26):
            parts.append(wf.finish(
                wf.rotate(wf.box("gearleg", (0.52 + lean, sign * 0.40, -0.92),
                                 (0.07, 0.05, 0.84), metal),
                          y=math.degrees(math.atan2(lean, 0.84)),
                          x=sign * 14), 0.010, 2))
        parts.append(wf.finish(
            wf.cylinder("tyre", (0.52, sign * 0.86, -1.34), 0.30, 0.10, "Y", 18, dark),
            0.024, 3))
    parts.append(wf.finish(
        wf.cylinder("axle", (0.52, 0, -1.34), 0.04, 1.76, "Y", 10, metal), 0.008, 1))
    return wf.join("gear", parts)


def skid(wood, metal):
    parts = [
        wf.finish(wf.rotate(wf.box("skid", (-2.96, 0, -0.30), (0.66, 0.07, 0.10), wood), y=20),
                  0.012, 2),
        wf.finish(wf.box("skidshoe", (-3.20, 0, -0.46), (0.24, 0.09, 0.05), metal), 0.008, 1),
    ]
    return wf.join("skid", parts)


def guns(metal, dark):
    """Two Spandau LMG 08/15 on the cowl."""
    parts = []
    for dy in (-0.13, 0.13):
        parts.append(wf.finish(
            wf.box("jacket", (1.42, dy, 0.50), (1.02, 0.09, 0.09), dark), 0.014, 2))
        parts.append(wf.finish(
            wf.cone("muzzle", (2.04, dy, 0.50), 0.028, 0.022, 0.28, "X", 10, metal), 0.005, 1))
        parts.append(wf.finish(
            wf.box("breech", (0.84, dy, 0.48), (0.30, 0.12, 0.15), dark), 0.02, 2))
    return wf.join("guns", parts)


PALETTE_BODY = (0x8E / 255, 0x33 / 255, 0x2C / 255)
STREAK_DARK = (0.70, 0.72, 0.66)
STREAK_LIGHT = (1.22, 1.16, 1.06)
MARK_BLACK = (0.09, 0.09, 0.10)
MARK_WHITE = (0.88, 0.86, 0.82)

PARTS = ("fuselage", "top_wing", "mid_wing", "low_wing", "struts", "engine", "prop",
         "tail", "rudder", "gear", "skid", "guns", "cross")


def weathering():
    """
    Streaked olive over the factory finish - Fokker painted it on with a brush in wavy
    bands, and no two aircraft matched.
    """
    for name in ("fuselage", "top_wing", "mid_wing", "low_wing", "tail", "rudder"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.24)
        wf.weather(obj, camo=(STREAK_LIGHT, STREAK_DARK), cover=(0.24, 0.26), dirt=0.0,
                   wear=0.22, scale=0.70, seed=71, ground=0.0, top=1.0)
    for name in ("engine", "gear", "guns", "struts", "prop", "skid"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.36, wear=0.32, seed=73, ground=-1.6, top=0.4)


def markings(paint):
    """Balkenkreuz on the wings and the fuselage sides."""
    black = wf.tint_for(MARK_BLACK, PALETTE_BODY)
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    added = []

    # Wing crosses lie flat, fuselage crosses stand on the side - same shape, different
    # plane, which is all the axis argument is for.
    for sign in (-1, 1):
        y = sign * 2.50
        for z, up in ((TOP_Z, 1), (LOW_Z, -1)):
            base = z + up * 0.09
            added.append(wf.flat_tint(
                wf.cross("cross", (0.22, y, base), 0.42, 0.17, 0.022, paint, axis="Z"), white))
            added.append(wf.flat_tint(
                wf.cross("cross", (0.22, y, base + up * 0.012), 0.34, 0.12, 0.022, paint,
                         axis="Z"), black))
    for sign in (-1, 1):
        y = sign * 0.37
        added.append(wf.flat_tint(
            wf.cross("cross", (-1.40, y + sign * 0.012, 0.10), 0.30, 0.13, 0.02, paint), white))
        added.append(wf.flat_tint(
            wf.cross("cross", (-1.40, y + sign * 0.026, 0.10), 0.24, 0.09, 0.02, paint), black))
    wf.join("cross", added)


def build():
    wf.reset()
    paint = wf.role("BODY", STREAK, roughness=0.74)
    metal = wf.role("METAL", STEEL, roughness=0.40, metallic=0.82)
    dark = wf.role("DARK", DARKWOOD, roughness=0.76, metallic=0.20)
    wood = wf.role("TRIM", TIMBER, roughness=0.56)

    fuselage(paint, metal, wood, dark)
    top_wing(paint)
    mid_wing(paint)
    low_wing(paint)
    struts(wood, metal, paint)
    engine(metal, paint, dark)
    propeller(wood, dark)
    tailplane(paint)
    rudder(paint, metal)
    gear(metal, wood, dark, paint)
    skid(wood, metal)
    guns(metal, dark)

    weathering()
    markings(paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(7.0, floor_z=-1.8)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
