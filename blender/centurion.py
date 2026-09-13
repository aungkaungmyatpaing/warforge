"""
Centurion Mk 3, at full size.

Designed to fight in 1945 and still fighting in 1973 - the first tank that stopped
choosing between armour, gun and mobility and simply carried all three. The Horstmann
bogies and the long low cast turret are what you pick it out by.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

GREEN = (0.19, 0.22, 0.16)
STEEL = (0.33, 0.34, 0.35)
TRACK_STEEL = (0.17, 0.17, 0.18)
GLASS = (0.10, 0.15, 0.17)

NOSE = 3.91
REAR = -3.91
FLOOR = 0.42
HULL_TOP = 1.02
DECK = 1.52
TURRET_TOP = 2.42
CUPOLA_TOP = 2.94
HULL_HALF = 1.16
TRACK_IN = 1.16
TRACK_OUT = 1.70          # 0.54 m track
SKIRT_Y = 1.76
RING_X = -0.30
TURRET_FRONT = 1.55
TURRET_REAR = -1.75
TURRET_HALF = 1.24
TURRET_ROOF_HALF = 1.02

SPROCKET = (-3.28, 0.62, 0.34)
IDLER = (3.36, 0.54, 0.30)
BOGIES = (-2.30, -0.30, 1.70)
WHEEL_R = 0.28

TRACK_PATH = [
    (3.20, 0.06), (-3.22, 0.06), (-3.62, 0.36), (-3.60, 0.86),
    (-3.10, 1.10), (3.16, 1.10), (3.66, 0.86), (3.68, 0.34),
]


def hull(paint, metal, dark):
    parts = []
    profile = [
        (NOSE, 0.72), (3.20, 1.00), (-3.30, 1.00), (REAR, 0.86),
        (REAR, 0.42), (-3.10, 0.30), (3.10, 0.30), (NOSE, 0.46),
    ]
    parts.append(wf.finish(wf.prism("hull", profile, -HULL_HALF, HULL_HALF, paint), 0.024, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("finaldrive", (SPROCKET[0] + 0.20, sign * 0.86, SPROCKET[2] + 0.12),
                        0.34, 0.66, "Y", 18, paint), 0.024, 2))
    for x in (NOSE - 0.08, REAR + 0.08):
        for y in (-0.62, 0.62):
            parts.append(wf.finish(
                wf.box("toweye", (x, y, 0.60), (0.18, 0.12, 0.22), metal), 0.016, 2))
    return wf.join("hull", parts)


def upper(paint, glass, metal, dark):
    """Glacis, driver's hatch, and the long engine deck aft."""
    parts = []
    box_ = wf.frustum("upper", (0, 0, (HULL_TOP + DECK) * 0.5),
                      (NOSE - REAR, SKIRT_Y * 2 * 0.92), (NOSE - REAR, SKIRT_Y * 2 * 0.86),
                      DECK - HULL_TOP, paint)
    wf.slice_xz(box_, (NOSE, HULL_TOP), (2.70, DECK), drop=(NOSE + 1.0, DECK))
    wf.slice_xz(box_, (REAR, HULL_TOP), (-3.30, DECK), drop=(REAR - 1.0, DECK))
    parts.append(wf.finish(box_, 0.028, 3))

    parts.append(wf.finish(
        wf.rotate(wf.box("drvhatch", (2.86, -0.46, 1.40), (0.66, 0.62, 0.06), paint), y=-40),
        0.014, 2))
    for dy in (-0.16, 0.16):
        parts.append(wf.finish(
            wf.rotate(wf.box("periscope", (2.72, -0.46 + dy, 1.50), (0.14, 0.14, 0.08), glass),
                      y=-40), 0.008, 1))
    # Engine deck grilles and the exhausts either side.
    parts.append(wf.finish(
        wf.box("engdeck", (-2.40, 0, DECK + 0.04), (1.90, 1.60, 0.08), paint), 0.02, 2))
    for i in range(7):
        parts.append(wf.finish(
            wf.box("louvre", (-3.10 + i * 0.22, 0, DECK + 0.10), (0.06, 1.50, 0.08), dark),
            0.008, 1))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("exhaust", (-2.40, sign * 1.44, DECK + 0.12), 0.15, 1.70, "X", 16, dark),
            0.02, 2))
    # Fenders and the track guards over them.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("fender", (0.0, sign * (SKIRT_Y - 0.02), HULL_TOP + 0.04),
                   (7.20, 0.66, 0.05), paint), 0.012, 2))
    return wf.join("upper", parts)


def turret(paint, metal, glass, dark):
    """The long cast turret: low, rounded everywhere, and heavily bevelled."""
    parts = []
    shell = wf.frustum("turret", (RING_X, 0, (DECK + TURRET_TOP) * 0.5),
                       (TURRET_FRONT - TURRET_REAR, TURRET_HALF * 2),
                       (TURRET_FRONT - TURRET_REAR, TURRET_ROOF_HALF * 2),
                       TURRET_TOP - DECK, paint)
    front = RING_X + TURRET_FRONT
    rear = RING_X + TURRET_REAR
    for sign in (-1, 1):
        wf.slice_xy(shell, (front, sign * 0.52), (front - 0.90, sign * TURRET_HALF),
                    drop=(front, sign * TURRET_HALF))
        wf.slice_xy(shell, (rear, sign * 0.74), (rear + 0.62, sign * TURRET_HALF),
                    drop=(rear, sign * TURRET_HALF))
    wf.slice_xz(shell, (front - 0.30, DECK), (front - 0.62, TURRET_TOP),
                drop=(front + 1.0, TURRET_TOP))
    parts.append(wf.finish(shell, 0.09, 5))
    # Loader's hatch and the roof vent.
    parts.append(wf.finish(
        wf.cylinder("ldrhatch", (RING_X - 0.30, -0.46, TURRET_TOP + 0.02), 0.30, 0.08,
                    "Z", 20, paint), 0.014, 2))
    parts.append(wf.finish(
        wf.cylinder("vent", (RING_X - 0.90, 0.0, TURRET_TOP + 0.04), 0.14, 0.12, "Z", 16, metal),
        0.012, 2))
    return wf.join("turret", parts)


def gun(metal, paint):
    """20-pounder, with the counterweight at the muzzle that balanced the stabiliser."""
    parts = []
    face = RING_X + TURRET_FRONT
    parts.append(wf.finish(
        wf.box("mantlet", (face - 0.04, 0, 1.98), (0.36, 0.98, 0.58), paint), 0.13, 6))
    parts.append(wf.finish(
        wf.cylinder("collar", (face + 0.30, 0, 1.98), 0.17, 0.56, "X", 18, paint), 0.02, 2))
    parts.append(wf.finish(
        wf.cone("barrel", (face + 2.10, 0, 1.98), 0.105, 0.088, 3.10, "X", 18, paint),
        0.008, 2))
    parts.append(wf.finish(
        wf.cylinder("counterweight", (face + 3.74, 0, 1.98), 0.135, 0.42, "X", 18, metal),
        0.012, 2))
    return wf.join("gun", parts)


def cupola(paint, glass, metal):
    parts = [
        wf.finish(wf.cylinder("cupola", (RING_X - 0.10, 0.48, TURRET_TOP + 0.16), 0.34, 0.32,
                              "Z", 22, paint), 0.024, 3),
        wf.finish(wf.cylinder("lid", (RING_X - 0.10, 0.48, CUPOLA_TOP - 0.28), 0.35, 0.06,
                              "Z", 22, paint), 0.012, 2),
    ]
    for i in range(6):
        a = math.radians(i * 60)
        parts.append(wf.finish(
            wf.box("vision", (RING_X - 0.10 + 0.34 * math.cos(a), 0.48 + 0.34 * math.sin(a),
                              TURRET_TOP + 0.20), (0.06, 0.10, 0.08), glass), 0.006, 1))
    return wf.join("cupola", parts)


def basket(metal, dark):
    """The stowage basket across the turret rear, always full of crew kit."""
    parts = []
    rear = RING_X + TURRET_REAR
    parts.append(wf.finish(
        wf.box("basketfloor", (rear - 0.42, 0, 1.86), (0.84, 1.90, 0.06), metal), 0.014, 1))
    for dy in (-0.94, 0.94):
        parts.append(wf.finish(
            wf.box("basketside", (rear - 0.42, dy, 2.10), (0.84, 0.05, 0.50), metal), 0.012, 1))
    for i in range(4):
        parts.append(wf.finish(
            wf.box("basketbar", (rear - 0.80, 0, 1.92 + i * 0.16), (0.05, 1.88, 0.05), metal),
            0.010, 1))
    return wf.join("basket", parts)


def machine_gun(metal, paint):
    parts = [
        wf.finish(wf.box("mgmount", (RING_X + 1.10, 0.66, TURRET_TOP + 0.02),
                         (0.34, 0.24, 0.16), paint), 0.02, 2),
        wf.finish(wf.cone("mg", (RING_X + 1.70, 0.66, TURRET_TOP + 0.08), 0.045, 0.032, 0.90,
                          "X", 10, metal), 0.006, 1),
    ]
    return wf.join("mg", parts)


def antenna(dark, metal):
    parts = []
    for dy in (-0.80, 0.80):
        parts.append(wf.finish(
            wf.cylinder("antbase", (RING_X - 1.30, dy, TURRET_TOP + 0.08), 0.07, 0.18,
                        "Z", 10, dark), 0.010, 1))
        parts.append(wf.finish(
            wf.cone("antenna", (RING_X - 1.30, dy, TURRET_TOP + 1.20), 0.022, 0.006, 2.10,
                    "Z", 8, dark), 0.004, 1))
    return wf.join("antenna", parts)


def lights(metal, glass, paint):
    parts = []
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("lampbody", (NOSE - 0.34, sign * 1.20, DECK + 0.12), 0.16, 0.22,
                        "X", 16, paint), 0.014, 2))
        parts.append(wf.finish(
            wf.cylinder("lens", (NOSE - 0.22, sign * 1.20, DECK + 0.12), 0.14, 0.05,
                        "X", 16, glass), 0.008, 1))
        parts.append(wf.finish(
            wf.box("guard", (NOSE - 0.30, sign * 1.20, DECK + 0.30), (0.26, 0.34, 0.05), metal),
            0.008, 1))
    return wf.join("lights", parts)


def skirts(paint, metal):
    parts = []
    for i in range(4):
        x = -2.55 + i * 1.70
        parts.append(wf.finish(
            wf.box("skirt", (x, SKIRT_Y, 0.74), (1.62, 0.05, 0.62), paint), 0.012, 2))
    parts.append(wf.finish(
        wf.box("skirtrail", (0.0, SKIRT_Y - 0.04, 1.04), (7.00, 0.06, 0.06), metal), 0.014, 2))
    return wf.mirror_y(wf.join("skirt", parts))


def running_gear(metal, dark, paint):
    """Three Horstmann bogies a side, each carrying two wheels on a horizontal spring."""
    parts = []
    y = (TRACK_IN + TRACK_OUT) * 0.5
    sx, sz, sr = SPROCKET
    parts.append(wf.cylinder("sprocket", (sx, y, sz), sr - 0.06, 0.34, "Y", 22, metal))
    for i in range(12):
        a = 2 * math.pi * i / 12
        parts.append(wf.box("tooth", (sx + sr * math.cos(a), y, sz + sr * math.sin(a)),
                            (0.07, 0.28, 0.07), metal))
    ix, iz, ir = IDLER
    parts.append(wf.cylinder("idler", (ix, y, iz), ir, 0.30, "Y", 20, metal))
    for bx in BOGIES:
        parts.append(wf.box("bogiecase", (bx, y - 0.22, 0.66), (1.10, 0.26, 0.30), paint))
        parts.append(wf.cylinder("spring", (bx, y - 0.22, 0.66), 0.13, 0.90, "X", 12, metal))
        for dx in (-0.38, 0.38):
            parts.append(wf.cylinder("roadwheel", (bx + dx, y, WHEEL_R), WHEEL_R, 0.28,
                                     "Y", 20, dark))
            parts.append(wf.cylinder("wheelhub", (bx + dx, y - 0.04, WHEEL_R), 0.09, 0.32,
                                     "Y", 12, metal))
    for i in range(4):
        parts.append(wf.cylinder("returnroller", (-2.40 + i * 1.60, y - 0.10, 1.04), 0.10, 0.20,
                                 "Y", 12, metal))
    for p in parts:
        wf.finish(p, 0.010, 1)
    return wf.mirror_y(wf.join("wheels", parts))


def tracks(dark):
    run = wf.track_run("track", TRACK_PATH, (0.16, TRACK_OUT - TRACK_IN, 0.050), dark)
    run.location.y = (TRACK_IN + TRACK_OUT) * 0.5
    return wf.mirror_y(run)


PALETTE_BODY = (0x3E / 255, 0x48 / 255, 0x34 / 255)
FADED = (1.22, 1.18, 1.08)
BLACK_GREEN = (0.62, 0.70, 0.60)
PARTS = ("hull", "track", "wheels", "upper", "skirt", "turret", "gun", "cupola",
         "basket", "mg", "antenna", "lights")


def weathering():
    """NATO green with the black disruptive pattern sprayed over it."""
    for name in ("hull", "upper", "turret", "gun", "cupola", "skirt", "lights", "mg"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.34)
        wf.weather(obj, camo=(FADED, BLACK_GREEN), cover=(0.16, 0.30), dirt=0.50,
                   wear=0.28, scale=1.30, seed=47, ground=0.0, top=3.4)
    for name in ("track", "wheels", "basket", "antenna"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.62, wear=0.34, seed=49, ground=0.0, top=1.8)


def build():
    wf.reset()
    paint = wf.role("BODY", GREEN, roughness=0.80)
    metal = wf.role("METAL", STEEL, roughness=0.44, metallic=0.80)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.64, metallic=0.40)
    glass = wf.role("GLASS", GLASS, roughness=0.22, metallic=0.30)

    hull(paint, metal, dark)
    upper(paint, glass, metal, dark)
    turret(paint, metal, glass, dark)
    gun(metal, paint)
    cupola(paint, glass, metal)
    basket(metal, dark)
    machine_gun(metal, paint)
    antenna(dark, metal)
    lights(metal, glass, paint)
    skirts(paint, metal)
    running_gear(metal, dark, paint)
    tracks(dark)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(8.0)
        wf.camera(azimuth=38, elevation=17)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
