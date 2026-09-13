"""
M1A2 Abrams, at full size.

Everything about the shape is armour: the turret is a set of flat slabs because Chobham
armour comes in flat blocks, the front is raked because sloping it multiplies the
thickness, and the bustle roof is deliberately weak so that when the ammunition cooks off
the blast goes up through it instead of down into the crew.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

SAND = (0.46, 0.41, 0.30)
STEEL = (0.32, 0.33, 0.34)
TRACK_STEEL = (0.17, 0.17, 0.18)
GLASS = (0.12, 0.17, 0.19)

NOSE = 3.96
REAR = -3.96
FLOOR = 0.43
HULL_TOP = 1.16
DECK = 1.52
TURRET_TOP = 2.44
HULL_HALF = 1.46
TRACK_IN = 1.46
TRACK_OUT = 1.83          # 0.635 m track
SKIRT_Y = 1.86
RING_X = -0.40
TURRET_FRONT = 1.90
TURRET_REAR = -2.40
TURRET_HALF = 1.72
TURRET_ROOF_HALF = 1.60

SPROCKET = (-3.32, 0.62, 0.34)
IDLER = (3.40, 0.52, 0.30)
WHEELS_X = (-2.62, -1.75, -0.88, 0.0, 0.88, 1.75, 2.62)
WHEEL_R = 0.32

TRACK_PATH = [
    (3.24, 0.06), (-3.26, 0.06), (-3.68, 0.36), (-3.66, 0.86),
    (-3.16, 1.12), (3.20, 1.12), (3.72, 0.84), (3.74, 0.32),
]


def hull(paint, metal, dark):
    parts = []
    profile = [
        (NOSE, 0.62), (3.10, 1.10), (-3.30, 1.10), (REAR, 0.94),
        (REAR, 0.43), (-3.10, 0.32), (3.10, 0.32), (NOSE, 0.44),
    ]
    parts.append(wf.finish(wf.prism("hull", profile, -HULL_HALF, HULL_HALF, paint), 0.024, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("finaldrive", (SPROCKET[0] + 0.18, sign * 1.10, SPROCKET[2] + 0.14),
                        0.36, 0.72, "Y", 18, paint), 0.024, 2))
    for y in (-0.90, 0.90):
        parts.append(wf.finish(
            wf.box("towshackle", (REAR - 0.06, y, 0.68), (0.20, 0.16, 0.20), metal), 0.016, 2))
    return wf.join("hull", parts)


def upper(paint, glass, metal, dark):
    """
    A very long, very shallow glacis - the front plate lies down at about 82 degrees
    from the vertical, which is most of why the Abrams looks flat from the side.
    """
    parts = []
    box_ = wf.frustum("upper", (0, 0, (HULL_TOP + DECK) * 0.5),
                      (NOSE - REAR, SKIRT_Y * 2 * 0.96), (NOSE - REAR, SKIRT_Y * 2 * 0.94),
                      DECK - HULL_TOP, paint)
    wf.slice_xz(box_, (NOSE, HULL_TOP - 0.42), (1.20, DECK), drop=(NOSE + 1.0, DECK))
    wf.slice_xz(box_, (REAR, HULL_TOP), (-3.20, DECK), drop=(REAR - 1.0, DECK))
    parts.append(wf.finish(box_, 0.030, 3))

    # Driver's hatch on the centreline, lying in the glacis.
    parts.append(wf.finish(
        wf.rotate(wf.box("drvhatch", (1.96, 0, 1.40), (0.70, 0.72, 0.06), paint), y=-9),
        0.014, 2))
    for dy in (-0.26, 0.0, 0.26):
        parts.append(wf.finish(
            wf.rotate(wf.box("periscope", (1.72, dy, 1.46), (0.16, 0.15, 0.07), glass), y=-9),
            0.008, 1))
    # Engine deck: the big rear grille the turbine exhausts through.
    parts.append(wf.finish(
        wf.box("engdeck", (-2.70, 0, DECK + 0.04), (2.00, 2.90, 0.10), paint), 0.024, 2))
    for i in range(8):
        parts.append(wf.finish(
            wf.box("louvre", (-3.44 + i * 0.21, 0, DECK + 0.12), (0.06, 2.70, 0.09), dark),
            0.008, 1))
    parts.append(wf.finish(
        wf.box("exhaustgrille", (REAR + 0.12, 0, DECK - 0.20), (0.14, 2.10, 0.72), dark),
        0.02, 2))
    # Fenders.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("fender", (0.0, sign * (SKIRT_Y - 0.02), HULL_TOP + 0.06),
                   (7.30, 0.76, 0.06), paint), 0.012, 2))
    return wf.join("upper", parts)


def turret(paint, metal, glass, dark):
    """Flat slabs everywhere: the special armour arrives as blocks and is bolted on."""
    parts = []
    shell = wf.frustum("turret", (RING_X, 0, (DECK + TURRET_TOP) * 0.5),
                       (TURRET_FRONT - TURRET_REAR, TURRET_HALF * 2),
                       (TURRET_FRONT - TURRET_REAR, TURRET_ROOF_HALF * 2),
                       TURRET_TOP - DECK, paint)
    front = RING_X + TURRET_FRONT
    rear = RING_X + TURRET_REAR
    # The wedge front: two big flat cheeks either side of the gun.
    for sign in (-1, 1):
        wf.slice_xy(shell, (front, sign * 0.64), (front - 1.30, sign * TURRET_HALF),
                    drop=(front, sign * TURRET_HALF))
    # The front plates lean back a little; the bustle sides are vertical.
    wf.slice_xz(shell, (front, DECK + 0.10), (front - 0.34, TURRET_TOP),
                drop=(front + 1.0, TURRET_TOP))
    parts.append(wf.finish(shell, 0.035, 2))

    # Gunner's primary sight, armoured box on the roof to starboard of the gun.
    parts.append(wf.finish(
        wf.box("gpsbox", (RING_X + 0.86, -0.58, TURRET_TOP + 0.14), (0.66, 0.58, 0.30), paint),
        0.03, 2))
    parts.append(wf.finish(
        wf.box("gpshead", (RING_X + 1.12, -0.58, TURRET_TOP + 0.16), (0.16, 0.44, 0.22), glass),
        0.02, 2))
    # Loader's hatch, and the roof vent behind the bustle.
    parts.append(wf.finish(
        wf.cylinder("ldrhatch", (RING_X + 0.20, 0.76, TURRET_TOP + 0.04), 0.34, 0.10,
                    "Z", 20, paint), 0.016, 2))
    parts.append(wf.finish(
        wf.cylinder("nbcvent", (RING_X - 1.30, 0.90, TURRET_TOP + 0.08), 0.22, 0.18,
                    "Z", 16, metal), 0.016, 2))
    # Wind sensor mast, right at the back of the roof.
    parts.append(wf.finish(
        wf.cone("windsensor", (RING_X - 2.10, -0.90, TURRET_TOP + 0.46), 0.05, 0.03, 0.80,
                "Z", 8, dark), 0.006, 1))
    return wf.join("turret", parts)


def gun(metal, paint, dark):
    """120 mm M256 smoothbore, in its thermal sleeve with the bore evacuator two-thirds out."""
    parts = []
    face = RING_X + TURRET_FRONT
    parts.append(wf.finish(
        wf.box("mantlet", (face - 0.10, 0, 1.94), (0.40, 0.96, 0.66), paint), 0.05, 3))
    parts.append(wf.finish(
        wf.cylinder("sleeve", (face + 1.20, 0, 1.94), 0.19, 2.30, "X", 20, dark), 0.02, 2))
    parts.append(wf.finish(
        wf.cylinder("evacuator", (face + 2.60, 0, 1.94), 0.26, 0.66, "X", 20, dark), 0.03, 3))
    parts.append(wf.finish(
        wf.cone("barrel", (face + 3.90, 0, 1.94), 0.115, 0.100, 2.00, "X", 18, metal),
        0.008, 2))
    return wf.join("gun", parts)


def bustle(paint, metal, dark):
    """
    The ammunition bustle, with its blow-off panels - the single feature that made the
    Abrams survivable, and the one everybody points at in photographs of burnt-out ones.
    """
    parts = []
    rear = RING_X + TURRET_REAR
    for dy in (-0.80, 0.80):
        parts.append(wf.finish(
            wf.box("blowoff", (rear + 0.90, dy, TURRET_TOP + 0.04), (1.70, 0.72, 0.07), metal),
            0.016, 2))
    parts.append(wf.finish(
        wf.box("bustlerack", (rear - 0.28, 0, 2.00), (0.30, 2.90, 0.80), metal), 0.03, 2))
    for i in range(5):
        parts.append(wf.finish(
            wf.box("rackbar", (rear - 0.46, -1.20 + i * 0.60, 2.00), (0.05, 0.05, 0.78), metal),
            0.010, 1))
    return wf.join("bustle", parts)


def citv(paint, metal, glass):
    """Commander's independent thermal viewer, on its pedestal to port of the hatch."""
    parts = [
        wf.finish(wf.cylinder("citvpost", (RING_X + 0.40, 0.62, TURRET_TOP + 0.24), 0.17, 0.46,
                              "Z", 16, paint), 0.02, 2),
        wf.finish(wf.box("citvhead", (RING_X + 0.40, 0.62, TURRET_TOP + 0.62), (0.44, 0.44, 0.34),
                         paint), 0.04, 3),
        wf.finish(wf.box("citvwindow", (RING_X + 0.62, 0.62, TURRET_TOP + 0.62),
                         (0.05, 0.30, 0.22), glass), 0.012, 1),
    ]
    return wf.join("citv", parts)


def cws(paint, metal, dark):
    """Commander's weapon station: the .50 on its ring mount."""
    parts = [
        wf.finish(wf.cylinder("cwsring", (RING_X - 0.30, 0.62, TURRET_TOP + 0.14), 0.48, 0.26,
                              "Z", 20, paint), 0.02, 2),
        wf.finish(wf.box("cwshatch", (RING_X - 0.30, 0.62, TURRET_TOP + 0.28), (0.72, 0.72, 0.08),
                         paint), 0.016, 2),
        wf.finish(wf.box("cwscradle", (RING_X + 0.02, 0.62, TURRET_TOP + 0.48),
                         (0.30, 0.22, 0.26), metal), 0.02, 2),
        wf.finish(wf.cone("fifty", (RING_X + 0.72, 0.62, TURRET_TOP + 0.52), 0.055, 0.040, 1.30,
                          "X", 12, dark), 0.006, 1),
        wf.finish(wf.box("ammocan", (RING_X - 0.20, 0.62, TURRET_TOP + 0.46),
                         (0.34, 0.24, 0.22), dark), 0.02, 2),
    ]
    # The loader's M240 on its skate mount.
    parts.append(wf.finish(
        wf.cone("m240", (RING_X + 0.80, -0.90, TURRET_TOP + 0.38), 0.035, 0.026, 1.00,
                "X", 10, dark), 0.005, 1))
    return wf.join("cwsmg", parts)


def smoke(dark, metal):
    """Two banks of six smoke grenade dischargers on the turret cheeks."""
    parts = []
    front = RING_X + TURRET_FRONT
    for sign in (-1, 1):
        for row in range(2):
            for i in range(3):
                parts.append(wf.finish(
                    wf.rotate(
                        wf.cylinder("discharger",
                                    (front - 0.66 + i * 0.20, sign * (1.00 + row * 0.02),
                                     1.96 + row * 0.26),
                                    0.085, 0.34, "X", 10, dark),
                        z=sign * 22), 0.012, 2))
    return wf.join("smoke", parts)


def gps_optics(metal, glass, paint):
    """The gunner's auxiliary sight and the driver's vision blocks."""
    parts = [
        wf.finish(wf.box("auxsight", (RING_X + 1.46, -0.30, 2.14), (0.22, 0.22, 0.24), paint),
                  0.02, 2),
        wf.finish(wf.box("auxwindow", (RING_X + 1.56, -0.30, 2.14), (0.04, 0.16, 0.16), glass),
                  0.008, 1),
    ]
    return wf.join("gps", parts)


def skirts(paint, metal, dark):
    """Heavy ballistic skirts forward, thinner sections aft."""
    parts = []
    for i in range(4):
        x = 2.30 - i * 1.36
        parts.append(wf.finish(
            wf.box("skirt", (x, SKIRT_Y, 0.72), (1.30, 0.10, 0.78), paint), 0.02, 2))
    return wf.mirror_y(wf.join("skirt", parts))


def skirts_rear(paint, metal):
    parts = []
    for i in range(2):
        x = -2.42 - i * 1.00
        parts.append(wf.finish(
            wf.box("skirt_r", (x, SKIRT_Y, 0.72), (0.94, 0.06, 0.74), paint), 0.016, 2))
    parts.append(wf.finish(
        wf.box("mudflap", (REAR - 0.10, SKIRT_Y - 0.30, 0.42), (0.10, 0.80, 0.62), metal),
        0.02, 2))
    return wf.mirror_y(wf.join("skirt_r", parts))


def running_gear(metal, dark, paint):
    """Seven road wheels a side on torsion bars, and two return rollers."""
    parts = []
    y = (TRACK_IN + TRACK_OUT) * 0.5
    sx, sz, sr = SPROCKET
    parts.append(wf.cylinder("sprocket", (sx, y, sz), sr - 0.06, 0.36, "Y", 22, metal))
    for i in range(11):
        a = 2 * math.pi * i / 11
        parts.append(wf.box("tooth", (sx + sr * math.cos(a), y, sz + sr * math.sin(a)),
                            (0.07, 0.30, 0.07), metal))
    ix, iz, ir = IDLER
    parts.append(wf.cylinder("idler", (ix, y, iz), ir, 0.32, "Y", 20, metal))
    for x in WHEELS_X:
        parts.append(wf.cylinder("roadwheel", (x, y, WHEEL_R), WHEEL_R, 0.30, "Y", 20, dark))
        parts.append(wf.cylinder("wheelhub", (x, y - 0.04, WHEEL_R), 0.12, 0.34, "Y", 12, metal))
        parts.append(wf.box("swingarm", (x - 0.22, y - 0.20, WHEEL_R + 0.10),
                            (0.46, 0.14, 0.14), paint))
    for x in (-1.30, 1.30):
        parts.append(wf.cylinder("returnroller", (x, y - 0.12, 1.06), 0.11, 0.22, "Y", 12, metal))
    for p in parts:
        wf.finish(p, 0.010, 1)
    return wf.mirror_y(wf.join("wheels", parts))


def tracks(dark):
    run = wf.track_run("track", TRACK_PATH, (0.19, TRACK_OUT - TRACK_IN, 0.055), dark)
    run.location.y = (TRACK_IN + TRACK_OUT) * 0.5
    return wf.mirror_y(run)


PALETTE_BODY = (0x8A / 255, 0x7B / 255, 0x5C / 255)
SAND_LIGHT = (1.14, 1.12, 1.06)
SAND_DARK = (0.82, 0.82, 0.80)
PARTS = ("hull", "track", "wheels", "upper", "skirt", "turret", "gun", "bustle",
         "citv", "cwsmg", "smoke", "gps", "skirt_r")


def weathering():
    """Desert sand, dust everywhere, and worn paint on every edge crews climb over."""
    for name in ("hull", "upper", "turret", "gun", "skirt", "skirt_r", "citv", "gps"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.36)
        wf.weather(obj, camo=(SAND_LIGHT, SAND_DARK), cover=(0.26, 0.26), dirt=0.56,
                   wear=0.30, scale=1.40, seed=53, ground=0.0, top=3.6)
    for name in ("track", "wheels", "bustle", "cwsmg", "smoke"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.66, wear=0.34, seed=59, ground=0.0, top=2.0)


def build():
    wf.reset()
    paint = wf.role("BODY", SAND, roughness=0.84)
    metal = wf.role("METAL", STEEL, roughness=0.46, metallic=0.78)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.66, metallic=0.40)
    glass = wf.role("GLASS", GLASS, roughness=0.20, metallic=0.30)

    hull(paint, metal, dark)
    upper(paint, glass, metal, dark)
    turret(paint, metal, glass, dark)
    gun(metal, paint, dark)
    bustle(paint, metal, dark)
    citv(paint, metal, glass)
    cws(paint, metal, dark)
    smoke(dark, metal)
    gps_optics(metal, glass, paint)
    skirts(paint, metal, dark)
    skirts_rear(paint, metal)
    running_gear(metal, dark, paint)
    tracks(dark)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(8.2)
        wf.camera(azimuth=38, elevation=17)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
