"""
Sopwith Camel F.1, at full size.

Named for the hump over the breeches of its two Vickers guns. Everything heavy - engine,
guns, pilot, fuel - is packed into the first two metres, which made it lethal in a turn
and lethal to its own pilots on take-off; more Camels killed their pilots in training
than were shot down.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

PC10 = (0.28, 0.25, 0.15)
LINEN = (0.62, 0.56, 0.40)
STEEL = (0.34, 0.35, 0.36)
TIMBER = (0.36, 0.27, 0.16)
DARKWOOD = (0.18, 0.13, 0.08)

# Length 5.72, span 8.53, height 2.59. x = 0 mid-length, z = 0 on the thrust line.
NOSE = 2.30
TAIL = -3.42
HALF_SPAN = 4.27
UPPER_Z = 1.10
LOWER_Z = -0.30
CHORD = 1.37
GAP = UPPER_Z - LOWER_Z
PROP_R = 1.30


def fuselage(paint, metal, wood):
    """A flat-sided box girder under fabric, with the famous hump over the guns."""
    parts = []
    stations = [
        (TAIL, 0.04, 0.10, 0.20),
        (-2.60, 0.16, 0.26, 0.16),
        (-1.60, 0.28, 0.36, 0.10),
        (-0.60, 0.38, 0.46, 0.04),
        (0.30, 0.42, 0.50, 0.02),
        (1.20, 0.42, 0.50, 0.02),
        (NOSE - 0.30, 0.40, 0.46, 0.02),
    ]
    sections = [(x, wf.oval(hy, hz, 12, centre_z=cz, flatten_bottom=0.10))
                for x, hy, hz, cz in stations]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint), 0.014, 2, smooth_angle=40))

    # The hump: the fairing over the gun breeches, ahead of the cockpit.
    hump = [
        (0.10, wf.oval(0.30, 0.12, 10, centre_z=0.52)),
        (0.70, wf.oval(0.34, 0.18, 10, centre_z=0.56)),
        (1.40, wf.oval(0.32, 0.16, 10, centre_z=0.54)),
        (1.96, wf.oval(0.26, 0.10, 10, centre_z=0.48)),
    ]
    parts.append(wf.finish(wf.loft("hump", hump, paint), 0.014, 2, smooth_angle=44))
    # Cockpit coaming.
    parts.append(wf.finish(
        wf.cylinder("coaming", (-0.34, 0, 0.50), 0.30, 0.10, "Z", 16, wood), 0.02, 2))
    parts.append(wf.finish(
        wf.cylinder("cockpit", (-0.34, 0, 0.44), 0.26, 0.14, "Z", 16, DARK_ROLE[0]), 0.01, 1))
    return wf.join("fuselage", parts)


def biplane_wing(name, z, span, chord, le, dihedral, paint, cut_centre=0.0):
    """One wing panel, or a pair: a thin high-camber section, as 1917 wings were."""
    stations = [
        (0.00, le, chord, 0.085, z),
        (span * 0.55, le - 0.02, chord, 0.080, z + span * 0.55 * math.tan(math.radians(dihedral))),
        (span - 0.22, le - 0.05, chord * 0.98, 0.078,
         z + (span - 0.22) * math.tan(math.radians(dihedral))),
        (span, le - 0.14, chord * 0.74, 0.080, z + span * math.tan(math.radians(dihedral))),
    ]
    panel = wf.wing(name, stations, paint)
    if cut_centre:
        # The upper wing has a cut-out over the cockpit so the pilot can see up.
        wf.cut(panel, wf.box("cut", (le - chord * 0.42, 0, z), (chord * 0.6, cut_centre, 0.5)))
    return wf.mirror_y(wf.finish(panel, 0.006, 2, smooth_angle=54))


def upper_wing(paint, metal):
    return biplane_wing("upper_wing", UPPER_Z, HALF_SPAN, CHORD, 0.86, 0.0, paint,
                        cut_centre=0.62)


def lower_wing(paint, metal):
    return biplane_wing("lower_wing", LOWER_Z, HALF_SPAN - 0.10, CHORD, 0.72, 5.0, paint)


def struts(wood, metal):
    """
    Interplane struts and the flying wires.

    The wires are what a biplane actually is: the two wings and the struts are a truss,
    and without the bracing it folds up. Leaving them off is the commonest way a biplane
    model looks wrong.
    """
    parts = []
    for sign in (-1, 1):
        for y_at, x_front in ((1.30, 0.80), (2.90, 0.78)):
            y = sign * y_at
            drop = y_at * math.tan(math.radians(5.0))
            for dx in (0.0, -0.92):
                parts.append(wf.finish(
                    wf.rotate(
                        wf.box("strut", (x_front + dx, y, (UPPER_Z + LOWER_Z + drop) * 0.5),
                               (0.10, 0.05, GAP - drop), wood),
                        x=sign * 3.0), 0.012, 2))
            # Cross bracing between the strut pairs.
            for lean in (1, -1):
                parts.append(wf.finish(
                    wf.rotate(
                        wf.box("wire", (x_front - 0.46, y, (UPPER_Z + LOWER_Z + drop) * 0.5),
                               (0.02, 0.02, GAP * 1.06), metal),
                        y=lean * 28), 0.004, 1))
    # Cabane struts, carrying the upper wing over the fuselage.
    for sign in (-1, 1):
        for dx in (0.72, -0.18):
            parts.append(wf.finish(
                wf.rotate(wf.box("cabane", (dx, sign * 0.30, (UPPER_Z + 0.44) * 0.5),
                                 (0.08, 0.05, UPPER_Z - 0.44), wood),
                          x=-sign * 14), 0.010, 2))
    return wf.join("struts", parts)


def engine(metal, paint, dark):
    """Clerget rotary in its horseshoe cowl - open at the bottom to throw the oil out."""
    parts = []
    cowl = wf.cylinder("cowl", (NOSE - 0.16, 0, 0.02), 0.46, 0.44, "X", 22, metal)
    wf.cut(cowl, wf.cylinder("cut", (NOSE - 0.16, 0, 0.02), 0.38, 0.60, "X", 22))
    # The horseshoe: the lower third of the cowl is cut away.
    wf.cut(cowl, wf.box("cut", (NOSE - 0.16, 0, -0.46), (0.70, 0.60, 0.52)))
    parts.append(wf.finish(cowl, 0.02, 3))
    for i in range(9):
        a = 2 * math.pi * i / 9
        parts.append(wf.finish(
            wf.cylinder("cylinder", (NOSE - 0.22, 0.26 * math.cos(a), 0.02 + 0.26 * math.sin(a)),
                        0.09, 0.26, "X", 10, dark), 0.014, 2))
    return wf.join("engine", parts)


def propeller(wood, metal):
    """Two-blade laminated wooden airscrew."""
    parts = [
        wf.finish(wf.cylinder("boss", (NOSE + 0.12, 0, 0.02), 0.11, 0.14, "X", 14, wood),
                  0.014, 2),
    ]
    for i in range(2):
        blade = wf.wing("blade", [
            (0.10, 0.12, 0.24, 0.16, 0.0),
            (0.55, 0.10, 0.26, 0.10, 0.0),
            (1.00, 0.07, 0.22, 0.08, 0.0),
            (PROP_R, 0.02, 0.10, 0.08, 0.0),
        ], wood)
        wf.bake(blade)
        blade.location = (NOSE + 0.14, 0, 0.02)
        wf.rotate(blade, x=90 + i * 180, y=-18)
        parts.append(wf.finish(blade, 0.005, 2, smooth_angle=56))
    return wf.join("prop", parts)


def tailplane(paint):
    stations = [
        (0.00, -2.70, 0.86, 0.075, 0.06),
        (0.70, -2.74, 0.74, 0.075, 0.06),
        (1.18, -2.82, 0.50, 0.080, 0.06),
    ]
    return wf.mirror_y(wf.finish(wf.wing("tail", stations, paint), 0.006, 2, smooth_angle=54))


def rudder(paint, metal):
    shape = wf.fin("rudder", [
        (0.04, -2.86, 0.86, 0.08, 0.0),
        (0.42, -2.92, 0.76, 0.08, 0.0),
        (0.78, -3.06, 0.56, 0.08, 0.0),
        (0.96, -3.20, 0.30, 0.09, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.006, 2, smooth_angle=54)]
    parts.append(wf.finish(
        wf.box("post", (-3.18, 0, 0.44), (0.04, 0.05, 0.90), metal), 0.005, 1))
    return wf.join("rudder", parts)


def gear(metal, wood, dark):
    """V-struts, a straight axle and a plain wooden tailskid."""
    parts = []
    for sign in (-1, 1):
        for lean in (0.34, -0.30):
            parts.append(wf.finish(
                wf.rotate(wf.box("gearleg", (0.62 + lean, sign * 0.44, -0.52),
                                 (0.07, 0.05, 0.92), wood),
                          y=math.degrees(math.atan2(lean, 0.92)),
                          x=sign * 16), 0.010, 2))
        parts.append(wf.finish(
            wf.cylinder("tyre", (0.62, sign * 0.74, -0.98), 0.32, 0.11, "Y", 18, dark),
            0.025, 3))
    parts.append(wf.finish(
        wf.cylinder("axle", (0.62, 0, -0.98), 0.045, 1.56, "Y", 10, metal), 0.008, 1))
    parts.append(wf.finish(
        wf.box("axlefair", (0.62, 0, -0.98), (0.26, 1.30, 0.07), wood), 0.02, 2))
    return wf.join("gear", parts)


def skid(wood, metal):
    parts = [
        wf.finish(wf.rotate(wf.box("skid", (-3.06, 0, -0.28), (0.62, 0.07, 0.10), wood), y=22),
                  0.012, 2),
        wf.finish(wf.box("skidshoe", (-3.28, 0, -0.44), (0.22, 0.09, 0.05), metal), 0.008, 1),
    ]
    return wf.join("skid", parts)


def guns(metal, dark):
    """Two Vickers .303, synchronised through the propeller."""
    parts = []
    for dy in (-0.14, 0.14):
        parts.append(wf.finish(
            wf.box("jacket", (1.30, dy, 0.62), (1.00, 0.10, 0.10), dark), 0.014, 2))
        parts.append(wf.finish(
            wf.cone("muzzle", (1.92, dy, 0.62), 0.030, 0.024, 0.30, "X", 10, metal),
            0.005, 1))
        parts.append(wf.finish(
            wf.box("breech", (0.72, dy, 0.60), (0.32, 0.13, 0.16), dark), 0.02, 2))
    parts.append(wf.finish(
        wf.box("ringsight", (1.86, 0, 0.76), (0.04, 0.14, 0.14), metal), 0.008, 1))
    return wf.join("guns", parts)


PALETTE_BODY = (0x6B / 255, 0x62 / 255, 0x3E / 255)
MARK_BLUE = (0.10, 0.15, 0.34)
MARK_RED = (0.48, 0.10, 0.10)
MARK_WHITE = (0.86, 0.85, 0.80)
LINEN_TINT = (1.30, 1.26, 1.14)
DARK_ROLE = [None]

PARTS = ("fuselage", "upper_wing", "lower_wing", "struts", "engine", "prop",
         "tail", "rudder", "gear", "skid", "guns", "roundel")


def weathering():
    """Doped linen: it sags between the ribs and the dope never went on evenly."""
    for name in ("fuselage", "upper_wing", "lower_wing", "tail", "rudder"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.26)
        wf.weather(obj, camo=(LINEN_TINT, (0.82, 0.84, 0.78)), cover=(0.20, 0.22),
                   dirt=0.0, wear=0.22, scale=0.90, seed=61, ground=0.0, top=1.0)
    for name in ("engine", "gear", "guns", "struts", "prop", "skid"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.34, wear=0.32, seed=67, ground=-1.4, top=0.4)


def markings(paint):
    """RFC roundels on both wings, and the rudder stripes."""
    blue = wf.tint_for(MARK_BLUE, PALETTE_BODY)
    red = wf.tint_for(MARK_RED, PALETTE_BODY)
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    added = []
    for sign in (-1, 1):
        y = sign * 3.10
        for host, z, up in (("upper_wing", UPPER_Z, 1), ("lower_wing", LOWER_Z, -1)):
            base = z + up * 0.06 + (3.10 * math.tan(math.radians(5.0)) if up < 0 else 0)
            for radius, tint, out in ((0.52, blue, 0.0), (0.34, white, 0.006),
                                      (0.17, red, 0.012)):
                disc = wf.cylinder("roundel", (0.20, y, base + up * out), radius, 0.02,
                                   "Z", 24, paint)
                added.append(wf.flat_tint(disc, tint))
    wf.join("roundel", added)


def build():
    wf.reset()
    paint = wf.role("BODY", PC10, roughness=0.72)
    metal = wf.role("METAL", STEEL, roughness=0.40, metallic=0.82)
    dark = wf.role("DARK", DARKWOOD, roughness=0.76, metallic=0.20)
    wood = wf.role("TRIM", TIMBER, roughness=0.56)
    DARK_ROLE[0] = dark

    fuselage(paint, metal, wood)
    upper_wing(paint, metal)
    lower_wing(paint, metal)
    struts(wood, metal)
    engine(metal, paint, dark)
    propeller(wood, metal)
    tailplane(paint)
    rudder(paint, metal)
    gear(metal, wood, dark)
    skid(wood, metal)
    guns(metal, dark)

    weathering()
    markings(paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(8.0, floor_z=-1.5)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
