"""
Renault FT, at full size.

The first tank with the layout every tank since has used: engine at the back, crew in the
middle, and the gun in a turret that turns all the way round. Everything before it was a
box with guns in the sides; everything after it is this.

Object names are the part ids the game drags around.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


HORIZON = (0.32, 0.33, 0.24)
STEEL = (0.33, 0.34, 0.35)
TRACK_STEEL = (0.17, 0.17, 0.18)
TIMBER = (0.34, 0.26, 0.16)
GLASS = (0.10, 0.14, 0.16)

# ---------------------------------------------------------------------------
# Dimensions, in metres. Hull 4.10 long, 1.74 wide, 2.14 high; 5.00 with the tail.
# ---------------------------------------------------------------------------
NOSE = 2.05
REAR = -2.05
HULL_HALF = 0.50
TRACK_IN = 0.50
TRACK_OUT = 0.84         # 0.34 m track
FLOOR = 0.16
DECK = 1.28              # engine deck
TURRET_BASE = 1.32
TURRET_TOP = 1.92
DOME_TOP = 2.14

# Idler big and forward, sprocket small and aft - the FT is the one tank where people
# regularly put this the wrong way round.
IDLER = (1.74, 0.62, 0.56)
SPROCKET = (-1.78, 0.52, 0.30)

TRACK_PATH = [
    (1.60, 0.04), (-1.70, 0.04), (-2.06, 0.34), (-2.02, 0.72),
    (-1.50, 0.94), (0.90, 1.22), (1.84, 1.16), (2.28, 0.84), (2.24, 0.34),
]


def hull(paint, metal, dark):
    """The narrow body that sits between the track frames."""
    parts = []
    profile = [
        (NOSE - 0.10, 0.52), (1.20, 0.92), (0.30, 1.02), (-0.60, 1.02),
        (-1.50, 0.94), (REAR + 0.04, 0.70), (REAR + 0.04, 0.28), (1.30, 0.24),
    ]
    parts.append(wf.finish(wf.prism("hull", profile, -HULL_HALF, HULL_HALF, paint), 0.02, 2))
    # Belly plate and the towing eyes.
    for x in (NOSE - 0.24, REAR + 0.16):
        parts.append(wf.finish(
            wf.box("toweye", (x, 0, 0.40), (0.16, 0.16, 0.14), metal), 0.014, 2))
    return wf.join("hull", parts)


def deck(paint, metal, dark):
    """
    Engine deck at the back and the driver's plates at the front - the FT's crew of two
    sat one behind the other, and the shape of the front says exactly where.
    """
    parts = []
    # Driver's front, with its hinged visor hatch.
    plate = [
        (1.86, [(-0.42, 0.72), (0.42, 0.72), (0.36, 1.16), (-0.36, 1.16)]),
        (1.28, [(-0.48, 0.86), (0.48, 0.86), (0.44, 1.30), (-0.44, 1.30)]),
        (0.66, [(-0.50, 0.98), (0.50, 0.98), (0.50, 1.32), (-0.50, 1.32)]),
    ]
    parts.append(wf.finish(wf.loft("deck", plate, paint), 0.05, 3, smooth_angle=44))
    parts.append(wf.finish(
        wf.rotate(wf.box("visor", (1.72, 0, 1.08), (0.34, 0.52, 0.05), paint), y=-28),
        0.012, 2))
    for dy in (-0.15, 0.15):
        parts.append(wf.finish(
            wf.rotate(wf.box("slit", (1.80, dy, 1.14), (0.04, 0.20, 0.05), dark), y=-28),
            0.006, 1))

    # Engine deck aft, with its louvred cover and exhaust.
    parts.append(wf.finish(
        wf.box("engdeck", (-1.10, 0, 1.06), (1.70, 0.94, 0.16), paint), 0.03, 2))
    for i in range(6):
        parts.append(wf.finish(
            wf.box("louvre", (-1.70 + i * 0.22, 0, 1.16), (0.06, 0.80, 0.07), dark), 0.008, 1))
    parts.append(wf.finish(
        wf.cylinder("exhaust", (-1.86, 0.36, 1.22), 0.10, 0.60, "X", 14, dark), 0.014, 2))
    # Turret ring collar.
    parts.append(wf.finish(
        wf.cylinder("ring", (0.16, 0, TURRET_BASE - 0.06), 0.56, 0.14, "Z", 20, paint),
        0.02, 2))
    return wf.join("deck", parts)


def turret(paint, metal, dark):
    """
    The octagonal cast turret, with the commander's mushroom dome on top - which is the
    hatch, the vision cupola and the only ventilation, all at once.
    """
    parts = []
    shell = []
    for z, half in ((TURRET_BASE, 0.52), (1.60, 0.50), (TURRET_TOP, 0.42)):
        ring = []
        for i in range(8):
            a = math.pi / 8 + 2 * math.pi * i / 8
            ring.append((half * math.cos(a), half * math.sin(a)))
        shell.append((z, ring))
    # Sections stack upward, so build them as x-rings and stand the result on end.
    body = wf.loft("turret", [(z, [(y, zz) for y, zz in ring]) for z, ring in shell], paint)
    wf.bake(body)
    for v in body.data.vertices:
        v.co = (v.co.y + 0.16, v.co.z, v.co.x)
    wf.recalc(body)
    parts.append(wf.finish(body, 0.03, 3, smooth_angle=44))

    # Rear stowage bulge, and the pistol port in it.
    parts.append(wf.finish(
        wf.box("turretbox", (-0.40, 0, 1.60), (0.30, 0.62, 0.44), paint), 0.05, 3))
    parts.append(wf.finish(
        wf.cylinder("port", (-0.54, 0, 1.62), 0.06, 0.08, "X", 10, dark), 0.008, 1))
    return wf.join("turret", parts)


def dome(paint, metal, dark):
    """The mushroom cupola."""
    parts = [
        wf.finish(wf.cylinder("dome", (0.16, 0, TURRET_TOP + 0.06), 0.26, 0.14, "Z", 18, paint),
                  0.03, 3),
        wf.finish(wf.cylinder("domecap", (0.16, 0, DOME_TOP - 0.04), 0.30, 0.07, "Z", 18, paint),
                  0.03, 3),
    ]
    for i in range(6):
        a = 2 * math.pi * i / 6
        parts.append(wf.finish(
            wf.box("domeslit", (0.16 + 0.26 * math.cos(a), 0.26 * math.sin(a), TURRET_TOP + 0.06),
                   (0.05, 0.05, 0.05), dark), 0.006, 1))
    return wf.join("dome", parts)


def gun(metal, paint):
    """37 mm Puteaux SA 18, in its ball mounting."""
    parts = [
        wf.finish(wf.sphere("mount", (0.62, 0, 1.60), 0.24, 18, paint), 0.02, 3),
        wf.finish(wf.cone("barrel", (1.04, 0, 1.60), 0.055, 0.045, 0.72, "X", 14, metal),
                  0.006, 2),
        wf.finish(wf.cylinder("collar", (0.76, 0, 1.60), 0.085, 0.18, "X", 12, metal),
                  0.008, 2),
    ]
    return wf.join("gun", parts)


def tail(trim, metal):
    """
    The wooden tail skid, which is what let a 4-metre tank cross a 2-metre trench and is
    the reason the FT is 5 metres long.
    """
    parts = []
    for sign in (-1, 1):
        y = sign * 0.34
        parts.append(wf.finish(
            wf.rotate(wf.box("tailbeam", (-2.46, y, 0.66), (1.30, 0.14, 0.20), trim), y=14),
            0.02, 2))
    parts.append(wf.finish(
        wf.box("tailcross", (-3.00, 0, 0.52), (0.18, 0.78, 0.16), trim), 0.02, 2))
    parts.append(wf.finish(
        wf.cylinder("tailpivot", (-1.92, 0, 0.80), 0.07, 0.86, "Y", 12, metal), 0.012, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.rotate(wf.cylinder("tailstay", (-2.30, sign * 0.30, 1.02), 0.035, 0.90,
                                  "X", 8, metal), y=34), 0.006, 1))
    return wf.join("tail", parts)


def running_gear(metal, dark, paint):
    """Big front idler, small rear sprocket, and the sprung bogies between them."""
    parts = []
    for sign in (-1, 1):
        y = sign * (TRACK_IN + TRACK_OUT) * 0.5
        ix, iz, ir = IDLER
        parts.append(wf.cylinder("idler", (ix, y, iz), ir, 0.30, "Y", 22, paint))
        parts.append(wf.cylinder("idlerhub", (ix, y - 0.02, iz), ir * 0.35, 0.34, "Y", 14, metal))
        for i in range(8):
            a = 2 * math.pi * i / 8
            parts.append(wf.box("spoke", (ix + ir * 0.62 * math.cos(a), y,
                                          iz + ir * 0.62 * math.sin(a)),
                                (0.07, 0.32, 0.07), metal))
        sx, sz, sr = SPROCKET
        parts.append(wf.cylinder("sprocket", (sx, y, sz), sr, 0.28, "Y", 18, metal))
        for i in range(12):
            a = 2 * math.pi * i / 12
            parts.append(wf.box("tooth", (sx + sr * math.cos(a), y, sz + sr * math.sin(a)),
                                (0.05, 0.24, 0.05), metal))
        # The girder the road wheels hang from, and the wheels themselves.
        parts.append(wf.box("girder", (0.0, y, 0.34), (3.20, 0.16, 0.18), paint))
        for i in range(9):
            parts.append(wf.cylinder("roadwheel", (-1.40 + i * 0.36, y, 0.14), 0.10, 0.26,
                                     "Y", 10, dark))
        for i in range(6):
            parts.append(wf.cylinder("returnroller", (-1.30 + i * 0.52, y, 1.02), 0.05, 0.22,
                                     "Y", 8, metal))
    for p in parts:
        wf.finish(p, 0.010, 1)
    return wf.join("wheels", parts)


def tracks(dark):
    run = wf.track_run("track", TRACK_PATH, (0.14, TRACK_OUT - TRACK_IN, 0.045), dark)
    run.location.y = (TRACK_IN + TRACK_OUT) * 0.5
    return wf.mirror_y(run)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

PALETTE_BODY = (0x5A / 255, 0x5E / 255, 0x42 / 255)
FADED = (1.18, 1.16, 1.08)
MUD = (0.80, 0.74, 0.62)

PARTS = ("hull", "track", "wheels", "turret", "dome", "gun", "tail", "deck")


def weathering():
    for name in ("hull", "deck", "turret", "dome", "gun", "tail"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.24)
        wf.weather(obj, camo=(FADED, MUD), cover=(0.26, 0.28), dirt=0.66, wear=0.30,
                   scale=0.85, seed=37, ground=0.0, top=3.4)
    for name in ("track", "wheels"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.74, wear=0.34, seed=39, ground=0.0, top=1.6)


def build():
    wf.reset()
    paint = wf.role("BODY", HORIZON, roughness=0.80)
    metal = wf.role("METAL", STEEL, roughness=0.46, metallic=0.78)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.66, metallic=0.40)
    trim = wf.role("TRIM", TIMBER, roughness=0.88)
    glass = wf.role("GLASS", GLASS, roughness=0.30, metallic=0.20)

    hull(paint, metal, dark)
    deck(paint, metal, dark)
    turret(paint, metal, dark)
    dome(paint, metal, dark)
    gun(metal, paint)
    tail(trim, metal)
    running_gear(metal, dark, paint)
    tracks(dark)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(5.0)
        wf.camera(azimuth=38, elevation=18)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
