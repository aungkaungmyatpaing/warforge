"""
Boeing B-17G Flying Fortress, at full size.

The G is the one everybody pictures: the chin turret under the bombardier's glazing, the
tall fin and dorsal fillet, and bare metal instead of olive drab, because by 1944 the
paint was costing more speed than the camouflage was worth.

Object names are the part ids the game drags around.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


ALUMINIUM = (0.62, 0.64, 0.66)
STEEL = (0.40, 0.41, 0.42)
TYRE = (0.07, 0.07, 0.08)
GLASS = (0.28, 0.36, 0.38)

# ---------------------------------------------------------------------------
# Dimensions, in metres. Length 22.66, span 31.62, height 5.82.
# x = 0 is the middle; +x toward the nose. z = 0 is the fuselage centreline.
# ---------------------------------------------------------------------------
NOSE = 11.33
TAIL = -11.33
HALF_SPAN = 15.81
WING_Z = -0.85
ROOT_LE = 2.89
ROOT_CHORD = 4.82

NACELLE_IN = 4.65
NACELLE_OUT = 8.40
PROP_R = 1.77

TAILPLANE_HALF = 6.39
FIN_TOP = 4.34


def wing_stations():
    """Straight-taper wing, slightly swept and with a rounded tip."""
    plan = [
        (0.00, ROOT_LE, ROOT_CHORD),
        (NACELLE_IN, 2.72, 4.12),
        (NACELLE_OUT, 2.52, 3.32),
        (12.40, 2.26, 2.34),
        (HALF_SPAN - 0.40, 2.06, 1.62),
        (HALF_SPAN, 1.92, 1.10),
    ]
    out = []
    for y, le, chord in plan:
        thick = 0.17 - 0.05 * (y / HALF_SPAN)
        out.append((y, le, chord, thick, WING_Z + y * math.tan(math.radians(4.5))))
    return out


def wing_at(y):
    """Leading edge, chord and centreline height at span |y|."""
    stations = wing_stations()
    span = abs(y)
    for (y0, le0, c0, _, z0), (y1, le1, c1, _, z1) in zip(stations, stations[1:]):
        if span <= y1:
            t = (span - y0) / max(1e-6, y1 - y0)
            return (le0 + (le1 - le0) * t, c0 + (c1 - c0) * t, z0 + (z1 - z0) * t)
    last = stations[-1]
    return (last[1], last[2], last[4])


def wing_surface(y, upper=True):
    le, chord, z = wing_at(y)
    half = 0.16 * chord * 0.5
    return z + (half if upper else -half)


def fuselage(paint, metal, glass):
    """
    A long, almost constant-section tube with a deep belly and a knife-edge sternpost.
    """
    parts = []
    stations = [
        (TAIL, 0.10, 0.22, 0.30),
        (-10.10, 0.36, 0.56, 0.22),
        (-8.00, 0.62, 0.94, 0.10),
        (-5.60, 0.86, 1.20, 0.02),
        (-2.00, 0.96, 1.34, 0.00),
        (2.60, 0.96, 1.34, 0.00),
        (6.20, 0.94, 1.30, 0.02),
        (8.60, 0.92, 1.26, 0.04),
        (9.60, 0.88, 1.14, 0.06),
    ]
    sections = [
        (x, wf.oval(hy, hz, 18, centre_z=cz, flatten_bottom=0.12))
        for x, hy, hz, cz in stations
    ]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint, cap_last=True),
                           0.020, 2, smooth_angle=52))

    # Cockpit glazing, set into the top of the fuselage.
    parts.append(wf.finish(
        wf.box("windscreen", (8.30, 0, 1.10), (1.90, 1.30, 0.52), glass), 0.08, 3))
    for i in range(4):
        parts.append(wf.finish(
            wf.box("cockpitframe", (7.60 + i * 0.46, 0, 1.18), (0.05, 1.34, 0.50), paint),
            0.008, 1))

    # Waist gun positions and their blisters, and the radio hatch.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("waist", (-4.30 - sign * 0.50, sign * 0.94, 0.24), (1.00, 0.14, 0.70), glass),
            0.04, 2))
    parts.append(wf.finish(
        wf.box("radiohatch", (-1.40, 0, 1.30), (1.60, 0.86, 0.10), glass), 0.02, 2))

    # Dorsal fillet: the long fin root fairing that arrived with the E model.
    fillet = [
        (-3.40, wf.oval(0.08, 0.04, 8, centre_z=1.30)),
        (-6.00, wf.oval(0.12, 0.30, 8, centre_z=1.24)),
        (-8.00, wf.oval(0.16, 0.70, 8, centre_z=1.10)),
        (-9.20, wf.oval(0.18, 0.95, 8, centre_z=0.98)),
    ]
    parts.append(wf.finish(wf.loft("fillet", fillet, paint), 0.02, 2, smooth_angle=52))
    return wf.join("fuselage", parts)


def wings(paint, metal):
    panel = wf.wing("wing", wing_stations(), paint)
    parts = [wf.finish(panel, 0.018, 2, smooth_angle=56)]
    # Wing root fairing where the wing meets the belly.
    fair = [
        (ROOT_LE + 0.40, wf.oval(1.10, 0.30, 12, centre_z=WING_Z + 0.10)),
        (0.60, wf.oval(1.30, 0.46, 12, centre_z=WING_Z + 0.16)),
        (ROOT_LE - ROOT_CHORD - 0.50, wf.oval(0.96, 0.26, 12, centre_z=WING_Z + 0.10)),
    ]
    parts.append(wf.finish(wf.loft("fairing", fair, paint), 0.03, 2, smooth_angle=52))
    return wf.mirror_y(wf.join("wing", parts))


def nacelle(name, y, paint, metal, dark, deep):
    """
    One engine nacelle: the cowl, the fairing back over the wing, and on the inboard
    pair the wheel well the main gear folds into.
    """
    le, chord, z = wing_at(y)
    front = le + 1.45
    parts = []
    body = [
        (front, wf.oval(0.70, 0.70, 18, centre_z=z + 0.10)),
        (front - 0.34, wf.oval(0.76, 0.76, 18, centre_z=z + 0.10)),
        (front - 1.20, wf.oval(0.72, 0.74, 18, centre_z=z + 0.06)),
        (le - chord * 0.30, wf.oval(0.56, 0.62, 18, centre_z=z + 0.02)),
        (le - chord * 0.80, wf.oval(0.34, 0.42, 18, centre_z=z - 0.02)),
        (le - chord * 1.10, wf.oval(0.12, 0.18, 18, centre_z=z - 0.02)),
    ]
    parts.append(wf.finish(
        wf.loft(name, [(x, [(yy + y, zz) for yy, zz in pts]) for x, pts in body], paint),
        0.03, 3, smooth_angle=52))

    # The cowl ring, and the cylinder heads showing through it.
    ring = wf.cylinder("cowlring", (front - 0.10, y, z + 0.10), 0.78, 0.36, "X", 22, metal)
    wf.cut(ring, wf.cylinder("cut", (front - 0.10, y, z + 0.10), 0.60, 0.60, "X", 22))
    parts.append(wf.finish(ring, 0.02, 2))
    for i in range(9):
        a = 2 * math.pi * i / 9
        parts.append(wf.finish(
            wf.box("cylinder", (front - 0.30, y + 0.44 * math.cos(a), z + 0.10 + 0.44 * math.sin(a)),
                   (0.20, 0.13, 0.13), dark), 0.02, 2))
    # The reduction gear housing the propeller turns on.
    parts.append(wf.finish(
        wf.cylinder("gearcase", (front + 0.06, y, z + 0.10), 0.22, 0.30, "X", 16, metal),
        0.02, 2))
    # Exhaust and the turbo-supercharger waste gate under the nacelle.
    parts.append(wf.finish(
        wf.box("turbo", (le - chord * 0.10, y, z - 0.56), (1.20, 0.44, 0.26), dark), 0.04, 2))
    return parts


def engines_in(paint, metal, dark):
    parts = []
    for y in (-NACELLE_IN, NACELLE_IN):
        parts += nacelle("eng_in", y, paint, metal, dark, deep=True)
    return wf.join("eng_in", parts)


def engines_out(paint, metal, dark):
    parts = []
    for y in (-NACELLE_OUT, NACELLE_OUT):
        parts += nacelle("eng_out", y, paint, metal, dark, deep=False)
    return wf.join("eng_out", parts)


def props(name, y_positions, metal, dark):
    """Hamilton Standard three-blade constant-speed propellers, 3.54 m across."""
    parts = []
    for y in y_positions:
        le, _, z = wing_at(abs(y))
        hub = le + 1.45 + 0.62
        spinner = [
            (hub - 0.30, wf.oval(0.24, 0.24, 14, centre_z=z + 0.10)),
            (hub, wf.oval(0.20, 0.20, 14, centre_z=z + 0.10)),
            (hub + 0.24, wf.oval(0.04, 0.04, 14, centre_z=z + 0.10)),
        ]
        parts.append(wf.finish(
            wf.loft("spinner", [(x, [(yy + y, zz) for yy, zz in pts]) for x, pts in spinner],
                    metal), 0.01, 2, smooth_angle=56))
        for i in range(3):
            blade = wf.wing("blade", [
                (0.20, 0.16, 0.34, 0.20, 0.0),
                (0.70, 0.12, 0.40, 0.13, 0.0),
                (1.25, 0.08, 0.34, 0.10, 0.0),
                (PROP_R, 0.03, 0.16, 0.09, 0.0),
            ], dark)
            wf.bake(blade)
            blade.location = (hub - 0.10, y, z + 0.10)
            wf.rotate(blade, x=90 + i * 120, y=-22)
            parts.append(wf.finish(blade, 0.008, 2, smooth_angle=56))
    return wf.join(name, parts)


def tailplane(paint):
    stations = [
        (0.00, -7.95, 3.10, 0.13, 0.78),
        (2.40, -8.25, 2.50, 0.12, 0.80),
        (4.60, -8.55, 1.88, 0.12, 0.82),
        (TAILPLANE_HALF - 0.30, -8.85, 1.24, 0.12, 0.84),
        (TAILPLANE_HALF, -8.95, 0.80, 0.13, 0.84),
    ]
    return wf.mirror_y(wf.finish(wf.wing("tail", stations, paint), 0.018, 2, smooth_angle=56))


def tailfin(paint, dark):
    """The tall fin the B-17 got with the E, and the reason its silhouette is unmistakable."""
    shape = wf.fin("fin", [
        (1.00, -7.20, 4.40, 0.12, 0.0),
        (2.20, -7.60, 3.70, 0.11, 0.0),
        (3.30, -8.20, 2.80, 0.11, 0.0),
        (FIN_TOP - 0.24, -8.90, 1.70, 0.11, 0.0),
        (FIN_TOP, -9.30, 1.10, 0.12, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.018, 2, smooth_angle=56)]
    parts.append(wf.finish(
        wf.box("rudderpost", (-10.20, 0, 2.70), (0.10, 0.12, 3.20), dark), 0.01, 1))
    return wf.join("fin", parts)


def nose(glass, metal, paint, dark):
    """
    Bombardier's glazing and, below it, the chin turret that makes this a G.

    The nose picks up the fuselage's own section and runs it forward to a rounded point,
    so the two read as one body. Starting it as a separate cone left a step.
    """
    parts = []
    cone = [
        (9.58, wf.oval(0.88, 1.14, 18, centre_z=0.06, flatten_bottom=0.12)),
        (10.30, wf.oval(0.80, 1.00, 18, centre_z=0.10, flatten_bottom=0.10)),
        (10.90, wf.oval(0.64, 0.78, 18, centre_z=0.14)),
        (NOSE, wf.oval(0.40, 0.48, 18, centre_z=0.16)),
        (NOSE + 0.26, wf.oval(0.14, 0.17, 18, centre_z=0.16)),
    ]
    parts.append(wf.finish(wf.loft("nose", cone, glass), 0.014, 3, smooth_angle=60))
    # Glazing bars round the frames.
    for x, hy, hz, cz in ((10.30, 0.80, 1.00, 0.10), (10.90, 0.64, 0.78, 0.14)):
        ring = wf.cylinder("bar", (x, 0, cz), max(hy, hz) + 0.03, 0.06, "X", 20, paint)
        wf.cut(ring, wf.cylinder("cut", (x, 0, cz), max(hy, hz) - 0.02, 0.20, "X", 20))
        parts.append(wf.finish(ring, 0.008, 1))
    parts.append(wf.finish(
        wf.box("keelbar", (10.60, 0, 0.16), (1.70, 0.05, 1.70), paint), 0.006, 1))

    # Bendix chin turret, faired into the underside rather than hung off it.
    chin = [
        (10.80, wf.oval(0.30, 0.22, 14, centre_z=-0.86)),
        (10.40, wf.oval(0.56, 0.44, 14, centre_z=-0.90)),
        (9.90, wf.oval(0.62, 0.50, 14, centre_z=-0.88)),
        (9.40, wf.oval(0.44, 0.34, 14, centre_z=-0.76)),
    ]
    parts.append(wf.finish(wf.loft("chin", chin, metal), 0.05, 3, smooth_angle=52))
    parts.append(wf.finish(
        wf.cylinder("chinglass", (10.62, 0, -0.88), 0.26, 0.30, "X", 16, glass), 0.02, 2))
    for dy in (-0.22, 0.22):
        parts.append(wf.finish(
            wf.cone("chingun", (11.30, dy, -0.88), 0.05, 0.04, 1.20, "X", 10, dark),
            0.006, 1))
    # Cheek guns, one each side.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cone("cheekgun", (10.40, sign * 0.72, 0.06), 0.05, 0.04, 1.10, "X", 10, dark),
            0.006, 1))
    return wf.join("nose", parts)


def top_turret(glass, metal, dark):
    parts = [
        wf.finish(wf.cylinder("turret_top", (6.87, 0, 1.44), 0.52, 0.30, "Z", 20, metal),
                  0.03, 3),
        wf.finish(wf.sphere("dome", (6.87, 0, 1.52), 0.50, 20, glass), 0.02, 3),
    ]
    for dy in (-0.18, 0.18):
        parts.append(wf.finish(
            wf.cone("gun", (7.60, dy, 1.60), 0.045, 0.035, 1.10, "X", 10, dark), 0.006, 1))
    return wf.join("turret_top", parts)


def ball_turret(glass, metal, dark):
    """Sperry ball turret, slung under the belly - the worst job on the aeroplane."""
    parts = [
        wf.finish(wf.sphere("turret_ball", (-3.86, 0, -1.42), 0.62, 22, metal), 0.02, 3),
        wf.finish(wf.cylinder("ballglass", (-3.86, 0, -1.96), 0.30, 0.24, "Z", 16, glass),
                  0.02, 2),
    ]
    for dy in (-0.22, 0.22):
        parts.append(wf.finish(
            wf.rotate(wf.cone("ballgun", (-3.40, dy, -1.86), 0.04, 0.032, 1.10, "X", 10, dark),
                      y=28), 0.005, 1))
    return wf.join("turret_ball", parts)


def gear(metal, dark, paint):
    """Main gear folds back into the inboard nacelles, leaving half a wheel showing."""
    parts = []
    for sign in (-1, 1):
        y = sign * NACELLE_IN
        le, chord, z = wing_at(y)
        for dy in (-0.36, 0.36):
            parts.append(wf.finish(
                wf.rotate(wf.cylinder("leg", (le - 0.30, y + dy, z - 1.10), 0.10, 1.90,
                                      "Z", 12, metal), y=-6), 0.01, 2))
        parts.append(wf.finish(
            wf.cylinder("tyre", (le - 0.46, y, z - 2.12), 0.62, 0.30, "Y", 22, dark), 0.05, 3))
        parts.append(wf.finish(
            wf.cylinder("hub", (le - 0.46, y, z - 2.12), 0.24, 0.34, "Y", 14, metal), 0.02, 2))
    # Tail wheel.
    parts.append(wf.finish(
        wf.cylinder("tailleg", (-7.40, 0, -1.10), 0.07, 0.70, "Z", 10, metal), 0.008, 1))
    parts.append(wf.finish(
        wf.cylinder("tailtyre", (-7.40, 0, -1.52), 0.28, 0.16, "Y", 16, dark), 0.03, 2))
    return wf.join("gear", parts)


def tail_guns(glass, metal, dark):
    """Cheyenne tail turret, faired into the sternpost."""
    parts = []
    shape = [
        (TAIL + 1.80, wf.oval(0.46, 0.62, 14, centre_z=0.10)),
        (TAIL + 1.00, wf.oval(0.52, 0.66, 14, centre_z=0.10)),
        (TAIL + 0.30, wf.oval(0.46, 0.56, 14, centre_z=0.12)),
        (TAIL, wf.oval(0.30, 0.36, 14, centre_z=0.12)),
    ]
    parts.append(wf.finish(wf.loft("tailgun", shape, metal), 0.03, 3, smooth_angle=56))
    parts.append(wf.finish(
        wf.cylinder("tailglass", (TAIL + 0.16, 0, 0.20), 0.26, 0.22, "X", 16, glass),
        0.02, 2))
    for dy in (-0.16, 0.16):
        parts.append(wf.finish(
            wf.cone("gun", (TAIL - 0.60, dy, 0.04), 0.045, 0.035, 1.30, "X", 10, dark),
            0.006, 1))
    return wf.join("tailgun", parts)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

# Bare metal. The BODY colour is already a light alloy grey, so the variation is panel
# to panel rather than a paint scheme: rolled sheet from different batches never matched,
# and by 1944 nobody was painting over it.
PALETTE_BODY = (0xA9 / 255, 0xAE / 255, 0xB2 / 255)
PANEL_LIGHT = (1.10, 1.10, 1.11)
PANEL_DARK = (0.86, 0.87, 0.89)
ANTI_GLARE = (0.30, 0.32, 0.26)      # olive drab panel ahead of the windscreen
MARK_BLUE = (0.14, 0.20, 0.48)
MARK_WHITE = (0.94, 0.94, 0.93)

PARTS = ("fuselage", "wing", "eng_in", "eng_out", "prop_in", "prop_out", "tail",
         "fin", "nose", "turret_top", "turret_ball", "gear", "tailgun", "star")


def weathering():
    """Panel variation, exhaust staining and worn walkways - no camouflage at all."""
    for name in ("fuselage", "wing", "eng_in", "eng_out", "tail", "fin", "nose"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.60)
        wf.weather(obj, camo=(PANEL_LIGHT, PANEL_DARK), cover=(0.30, 0.30),
                   dirt=0.0, wear=0.22, scale=2.0, seed=3, ground=0.0, top=1.0)

    # Turrets and gun positions: bare metal and perspex, scuffed rather than painted.
    for name in ("gear", "turret_ball", "turret_top", "tailgun", "prop_in", "prop_out"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.34, wear=0.30, seed=6, ground=-3.0, top=0.2)


def anti_glare():
    """The olive drab panel on the nose decking, to keep the sun out of the pilots' eyes."""
    obj = bpy.data.objects.get("fuselage")
    if obj is None:
        return
    mesh = obj.data
    col = mesh.color_attributes.get("Col")
    if col is None:
        return
    bpy.context.view_layer.update()
    to_world = obj.matrix_world
    for i, vert in enumerate(mesh.vertices):
        co = to_world @ vert.co
        facing = (to_world.to_3x3() @ vert.normal).z
        if 8.6 < co.x < 11.2 and facing > 0.25 and abs(co.y) < 0.70 and co.z > 0.30:
            r, g, b = (c / wf.TINT_RANGE for c in ANTI_GLARE)
            col.data[i].color = (r, g, b, 1.0)


def markings(paint):
    """
    Star and bar, 1943 pattern: a white star in a blue disc with a white bar each side.

    Carried on the port upper wing, the starboard lower wing and both sides of the
    fuselage - never on all four wing surfaces, which is the detail most models get wrong.
    """
    blue = wf.tint_for(MARK_BLUE, PALETTE_BODY)
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    added = {"star": []}

    def insignia(centre, radius, axis, up=1):
        """Blue disc, white star, and a bar either side."""
        depth = 0.05
        disc = wf.cylinder("star", centre, radius, depth, axis, 26, paint)
        added["star"].append(wf.flat_tint(disc, blue))
        lift = 0.012 * up if axis == "Z" else 0.0
        shift = 0.012 * up if axis == "Y" else 0.0
        core = (centre[0], centre[1] + shift, centre[2] + lift)
        added["star"].append(wf.flat_tint(
            wf.star("starpoint", core, radius * 0.92, depth, paint), white))
        for side in (-1, 1):
            if axis == "Z":
                bar = wf.box("bar", (core[0], centre[1] + side * radius * 1.5, core[2]),
                             (radius * 0.70, radius * 2.0, depth), paint)
            else:
                bar = wf.box("bar", (core[0] + side * radius * 1.5, core[1], centre[2]),
                             (radius * 2.0, depth, radius * 0.70), paint)
            added["star"].append(wf.flat_tint(bar, white))

    # Port upper wing and starboard lower wing.
    for y, up in ((6.40, 1), (-6.40, -1)):
        le, chord, _ = wing_at(y)
        insignia((le - chord * 0.52, y, wing_surface(y, upper=up > 0) + up * 0.01),
                 1.00, "Z", up)
    # Both sides of the fuselage, aft of the wing.
    for sign in (-1, 1):
        insignia((-5.60, sign * 0.97, 0.12), 0.72, "Y", sign)

    holder = wf.join("star", added["star"])
    return holder


def build():
    wf.reset()
    paint = wf.role("BODY", ALUMINIUM, roughness=0.30, metallic=0.55)
    metal = wf.role("METAL", STEEL, roughness=0.34, metallic=0.85)
    dark = wf.role("DARK", TYRE, roughness=0.70, metallic=0.30)
    glass = wf.role("GLASS", GLASS, roughness=0.10, metallic=0.20)

    fuselage(paint, metal, glass)
    wings(paint, metal)
    engines_in(paint, metal, dark)
    engines_out(paint, metal, dark)
    props("prop_in", (-NACELLE_IN, NACELLE_IN), metal, dark)
    props("prop_out", (-NACELLE_OUT, NACELLE_OUT), metal, dark)
    tailplane(paint)
    tailfin(paint, dark)
    nose(glass, metal, paint, dark)
    top_turret(glass, metal, dark)
    ball_turret(glass, metal, dark)
    gear(metal, dark, paint)
    tail_guns(glass, metal, dark)

    weathering()
    anti_glare()
    markings(paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(24.0, floor_z=-3.6)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
