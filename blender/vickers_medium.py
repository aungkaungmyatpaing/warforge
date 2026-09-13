"""
Vickers Medium Mk II, at full size.

The first tank anyone built that was meant to go somewhere rather than just cross a
trench: sprung suspension, a proper turret, and thirty miles an hour at a time when the
opposition managed walking pace. The armour was thin enough to be useless by 1939, which
is the price it paid for the speed.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

GREEN = (0.24, 0.26, 0.18)
STEEL = (0.33, 0.34, 0.35)
TRACK_STEEL = (0.17, 0.17, 0.18)
GLASS = (0.10, 0.14, 0.16)

NOSE = 2.66
REAR = -2.66
FLOOR = 0.30
HULL_TOP = 1.14
ROOF = 1.78
TURRET_BASE = 1.78
TURRET_TOP = 2.52
CUPOLA_TOP = 2.82
HULL_HALF = 0.86
TRACK_IN = 0.86
TRACK_OUT = 1.26
SKIRT_Y = 1.39
SPROCKET = (2.28, 0.56, 0.30)
IDLER = (-2.34, 0.50, 0.26)

TRACK_PATH = [
    (2.20, 0.06), (-2.26, 0.06), (-2.62, 0.30), (-2.58, 0.70),
    (-2.10, 0.96), (2.06, 0.96), (2.56, 0.80), (2.58, 0.32),
]


def hull(paint, metal, dark):
    parts = []
    profile = [
        (NOSE, 0.62), (2.20, 0.94), (0.60, 1.06), (-1.40, 1.06),
        (REAR, 0.86), (REAR, 0.36), (-2.10, 0.24), (2.10, 0.24), (NOSE, 0.40),
    ]
    parts.append(wf.finish(wf.prism("hull", profile, -HULL_HALF, HULL_HALF, paint), 0.02, 2))
    for x in (NOSE - 0.10, REAR + 0.10):
        parts.append(wf.finish(
            wf.box("toweye", (x, 0, 0.44), (0.18, 0.18, 0.16), metal), 0.015, 2))
    return wf.join("hull", parts)


def superstructure(paint, glass, metal, dark):
    """The tall boxy fighting compartment, with the driver's hood standing proud of it."""
    parts = []
    box_ = wf.frustum("super", (0.10, 0, (HULL_TOP + ROOF) * 0.5),
                      (4.60, HULL_HALF * 2), (4.10, HULL_HALF * 1.82), ROOF - HULL_TOP, paint)
    wf.slice_xz(box_, (2.40, HULL_TOP), (1.90, ROOF), drop=(NOSE + 1.0, ROOF))
    parts.append(wf.finish(box_, 0.024, 2))
    # Driver's hood, offset to the right as it was on the real thing.
    parts.append(wf.finish(
        wf.box("hood", (2.00, -0.28, ROOF + 0.14), (0.86, 0.66, 0.30), paint), 0.03, 2))
    parts.append(wf.finish(
        wf.rotate(wf.box("visor", (2.40, -0.28, ROOF + 0.16), (0.06, 0.44, 0.16), dark), y=-16),
        0.008, 1))
    # Side doors and their hinges.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("door", (-0.30, sign * (HULL_HALF - 0.02), (HULL_TOP + ROOF) * 0.5),
                   (1.00, 0.06, 0.52), paint), 0.012, 2))
    # Engine deck aft, with louvres.
    parts.append(wf.finish(
        wf.box("engdeck", (-1.90, 0, ROOF - 0.14), (1.30, HULL_HALF * 1.7, 0.10), paint),
        0.02, 2))
    for i in range(5):
        parts.append(wf.finish(
            wf.box("louvre", (-2.30 + i * 0.20, 0, ROOF - 0.04), (0.06, 1.20, 0.08), dark),
            0.008, 1))
    parts.append(wf.finish(
        wf.cylinder("exhaust", (-1.00, HULL_HALF + 0.10, ROOF - 0.30), 0.10, 2.20, "X", 14, dark),
        0.014, 2))
    return wf.join("super", parts)


def turret(paint, metal, glass, dark):
    """A plain cylinder with a flat roof - cast turrets were still years away."""
    parts = []
    drum = wf.cylinder("turret", (0.44, 0, (TURRET_BASE + TURRET_TOP) * 0.5), 0.82,
                       TURRET_TOP - TURRET_BASE, "Z", 24, paint)
    parts.append(wf.finish(drum, 0.03, 3, smooth_angle=44))
    # Rear bustle for the wireless.
    parts.append(wf.finish(
        wf.box("bustle", (-0.46, 0, TURRET_BASE + 0.34), (0.42, 0.82, 0.60), paint), 0.04, 3))
    # Vision slits round the front.
    for i in range(5):
        a = math.radians(-52 + i * 26)
        parts.append(wf.finish(
            wf.rotate(wf.box("slit", (0.44 + 0.82 * math.cos(a), 0.82 * math.sin(a),
                                      TURRET_TOP - 0.22), (0.05, 0.16, 0.07), dark),
                      z=math.degrees(a)), 0.006, 1))
    return wf.join("turret", parts)


def gun(metal, paint):
    """3-pounder 47 mm, in a small mantlet."""
    parts = [
        wf.finish(wf.box("mantlet", (1.22, 0, 2.14), (0.22, 0.46, 0.40), paint), 0.06, 4),
        wf.finish(wf.cylinder("collar", (1.44, 0, 2.14), 0.09, 0.26, "X", 14, metal), 0.01, 2),
        wf.finish(wf.cone("barrel", (2.16, 0, 2.14), 0.062, 0.050, 1.30, "X", 14, metal),
                  0.006, 2),
    ]
    return wf.join("gun", parts)


def cupola(paint, glass, metal):
    parts = [
        wf.finish(wf.cylinder("cupola", (0.20, 0, TURRET_TOP + 0.14), 0.30, 0.24, "Z", 18, paint),
                  0.02, 3),
        wf.finish(wf.cylinder("lid", (0.20, 0, CUPOLA_TOP - 0.02), 0.31, 0.05, "Z", 18, paint),
                  0.012, 2),
    ]
    for i in range(4):
        a = math.radians(30 + i * 60)
        parts.append(wf.finish(
            wf.box("slit", (0.20 + 0.30 * math.cos(a), 0.30 * math.sin(a), TURRET_TOP + 0.16),
                   (0.05, 0.05, 0.06), glass), 0.006, 1))
    return wf.join("cupola", parts)


def machine_guns(metal, paint):
    """Vickers guns in the turret sides - the Mk II carried four of them."""
    parts = []
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.sphere("mgmount", (0.30, sign * 0.80, 2.10), 0.15, 14, paint), 0.012, 2))
        parts.append(wf.finish(
            wf.cone("mg", (0.30, sign * 1.06, 2.10), 0.035, 0.026, 0.40, "Y", 10, metal),
            0.005, 1))
    parts.append(wf.finish(
        wf.sphere("bowmg", (2.40, 0.30, ROOF - 0.16), 0.15, 14, paint), 0.012, 2))
    parts.append(wf.finish(
        wf.cone("bowgun", (2.70, 0.30, ROOF - 0.16), 0.035, 0.026, 0.40, "X", 10, metal),
        0.005, 1))
    return wf.join("mg", parts)


def stowage(trim, metal):
    parts = [
        wf.finish(wf.box("stow", (-2.40, 0, ROOF - 0.06), (0.52, 1.10, 0.42), trim), 0.03, 2),
        wf.finish(wf.box("jack", (-2.62, 0.42, 0.86), (0.14, 0.16, 0.52), metal), 0.014, 2),
    ]
    for i in range(3):
        parts.append(wf.finish(
            wf.box("strap", (-2.56 + i * 0.16, 0, ROOF + 0.14), (0.05, 1.12, 0.05), metal),
            0.008, 1))
    return wf.join("stow", parts)


def skirts(paint, metal, dark):
    """The armoured skirt over the suspension, with its five access doors."""
    parts = []
    for i in range(5):
        x = -1.90 + i * 0.95
        parts.append(wf.finish(
            wf.box("skirt", (x, SKIRT_Y, 0.66), (0.88, 0.05, 0.76), paint), 0.014, 2))
        parts.append(wf.finish(
            wf.cylinder("porthole", (x, SKIRT_Y + 0.02, 0.66), 0.13, 0.06, "Y", 14, dark),
            0.012, 2))
    parts.append(wf.finish(
        wf.box("skirtrail", (0.0, SKIRT_Y - 0.04, 1.08), (5.00, 0.08, 0.10), metal), 0.02, 2))
    return wf.mirror_y(wf.join("skirt", parts))


def running_gear(metal, dark, paint):
    """Five sprung bogies of two wheels a side, behind the skirt."""
    parts = []
    y = (TRACK_IN + TRACK_OUT) * 0.5
    sx, sz, sr = SPROCKET
    parts.append(wf.cylinder("sprocket", (sx, y, sz), sr, 0.26, "Y", 20, metal))
    for i in range(14):
        a = 2 * math.pi * i / 14
        parts.append(wf.box("tooth", (sx + sr * math.cos(a), y, sz + sr * math.sin(a)),
                            (0.05, 0.22, 0.05), metal))
    ix, iz, ir = IDLER
    parts.append(wf.cylinder("idler", (ix, y, iz), ir, 0.24, "Y", 18, metal))
    for i in range(5):
        bx = -1.80 + i * 0.90
        parts.append(wf.box("bogie", (bx, y - 0.14, 0.44), (0.66, 0.10, 0.10), paint))
        for dx in (-0.22, 0.22):
            parts.append(wf.cylinder("roadwheel", (bx + dx, y, 0.20), 0.16, 0.22, "Y", 14, dark))
    for i in range(4):
        parts.append(wf.cylinder("returnroller", (-1.40 + i * 0.94, y - 0.06, 0.96), 0.08, 0.18,
                                 "Y", 10, metal))
    for p in parts:
        wf.finish(p, 0.010, 1)
    return wf.mirror_y(wf.join("wheels", parts))


def tracks(dark):
    run = wf.track_run("track", TRACK_PATH, (0.13, TRACK_OUT - TRACK_IN, 0.045), dark)
    run.location.y = (TRACK_IN + TRACK_OUT) * 0.5
    return wf.mirror_y(run)


PALETTE_BODY = (0x4C / 255, 0x52 / 255, 0x38 / 255)
FADED = (1.18, 1.15, 1.06)
SHADED = (0.76, 0.80, 0.70)
PARTS = ("hull", "track", "wheels", "skirt", "super", "turret", "gun", "cupola", "mg", "stow")


def weathering():
    for name in ("hull", "super", "turret", "gun", "cupola", "skirt", "stow", "mg"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.30)
        wf.weather(obj, camo=(FADED, SHADED), cover=(0.26, 0.26), dirt=0.52, wear=0.30,
                   scale=1.05, seed=41, ground=0.0, top=3.6)
    for name in ("track", "wheels"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.66, wear=0.34, seed=43, ground=0.0, top=1.5)


def build():
    wf.reset()
    paint = wf.role("BODY", GREEN, roughness=0.76)
    metal = wf.role("METAL", STEEL, roughness=0.44, metallic=0.80)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.64, metallic=0.40)
    trim = wf.role("TRIM", GREEN, roughness=0.80)
    glass = wf.role("GLASS", GLASS, roughness=0.26, metallic=0.25)

    hull(paint, metal, dark)
    superstructure(paint, glass, metal, dark)
    turret(paint, metal, glass, dark)
    gun(metal, paint)
    cupola(paint, glass, metal)
    machine_guns(metal, paint)
    stowage(trim, metal)
    skirts(paint, metal, dark)
    running_gear(metal, dark, paint)
    tracks(dark)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(5.6)
        wf.camera(azimuth=38, elevation=18)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
