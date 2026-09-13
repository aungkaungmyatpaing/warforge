"""
Mark IV, Male, at full size.

The first tank shape anybody pictures, and it is a shape dictated entirely by a problem:
to cross a German trench the track had to come right over the nose, so the hull became a
rhomboid with the track running round the *outside* of it, and the guns had to go in
sponsons on the sides because there was no roof left to put a turret on.

Object names are the part ids the game drags around.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


KHAKI = (0.30, 0.27, 0.17)
STEEL = (0.32, 0.33, 0.34)
TRACK_STEEL = (0.17, 0.17, 0.18)
GLASS = (0.10, 0.14, 0.16)

# ---------------------------------------------------------------------------
# Dimensions, in metres. 8.05 long, 3.91 wide over the sponsons, 2.49 high.
# ---------------------------------------------------------------------------
NOSE = 4.02
REAR = -4.02
GROUND = 0.0
TOP = 2.49
HULL_HALF = 0.98         # the body between the track frames
TRACK_IN = 0.98
TRACK_OUT = 1.63         # 0.65 m track shoe
SPONSON_OUT = 1.96       # 3.91 m over the sponsons

# The rhomboid: the track runs round this outline, in the x/z plane.
TRACK_PATH = [
    (3.62, 1.86), (2.30, 2.30), (-1.40, 2.30), (-3.10, 1.92),
    (-3.96, 1.16), (-3.96, 0.52), (-3.30, 0.10), (2.20, 0.10),
    (3.46, 0.42), (3.90, 1.06),
]


def hull(paint, metal, dark, glass):
    """The rhomboid body, plate by plate, with its rivet lines and the cab cut in later."""
    parts = []
    profile = [
        (3.50, 1.76), (2.30, 2.20), (-1.40, 2.20), (-3.02, 1.84),
        (-3.84, 1.14), (-3.84, 0.56), (-3.22, 0.20), (2.16, 0.20),
        (3.36, 0.50), (3.78, 1.06),
    ]
    body = wf.prism("hull", profile, -HULL_HALF, HULL_HALF, paint)
    parts.append(wf.finish(body, 0.024, 2))

    # Riveted strapping along every plate joint - a Mark IV is held together by rivets
    # and it shows from every angle.
    for x0, z0, x1, z1 in ((3.50, 1.76, 2.30, 2.20), (2.30, 2.20, -1.40, 2.20),
                           (-1.40, 2.20, -3.02, 1.84), (-3.22, 0.20, 2.16, 0.20)):
        n = max(3, int(math.hypot(x1 - x0, z1 - z0) / 0.34))
        for i in range(n + 1):
            t = i / n
            for y in (-HULL_HALF, HULL_HALF):
                parts.append(wf.finish(
                    wf.sphere("rivet", (x0 + (x1 - x0) * t, y, z0 + (z1 - z0) * t), 0.035,
                              8, metal), 0.006, 1))

    # Towing shackles and the exhaust stub.
    for x in (3.66, -3.70):
        parts.append(wf.finish(
            wf.box("shackle", (x, 0, 0.72), (0.22, 0.24, 0.18), metal), 0.02, 2))
    return wf.join("hull", parts)


def cab(paint, glass, metal):
    """The driver's cab on the front deck, with its armoured vision slits."""
    parts = [
        wf.finish(wf.box("cab", (2.10, 0, 2.48), (1.60, 1.36, 0.62), paint), 0.03, 2),
        wf.finish(wf.box("cabroof", (2.10, 0, 2.82), (1.72, 1.48, 0.08), paint), 0.02, 2),
    ]
    for dy in (-0.34, 0.34):
        parts.append(wf.finish(
            wf.box("slit", (2.88, dy, 2.56), (0.05, 0.34, 0.09), glass), 0.008, 1))
    parts.append(wf.finish(
        wf.box("slit", (2.10, 0.69, 2.56), (0.40, 0.05, 0.09), glass), 0.008, 1))
    parts.append(wf.finish(
        wf.box("slit", (2.10, -0.69, 2.56), (0.40, 0.05, 0.09), glass), 0.008, 1))
    # Rivet strapping round the cab.
    for i in range(9):
        for dy in (-0.69, 0.69):
            parts.append(wf.finish(
                wf.sphere("rivet", (1.34 + i * 0.19, dy, 2.74), 0.030, 8, metal), 0.005, 1))
    return wf.join("cab", parts)


def sponsons(paint, metal, glass):
    """
    The side sponsons - the Male's are big enough to carry a naval 6-pounder, and they
    are why the tank was 3.9 m wide and had to be unbolted to go on a railway wagon.
    """
    parts = []
    for sign in (-1, 1):
        y = sign * (TRACK_OUT + 0.16)
        shell = [
            (1.60, [(y * 0.86, 0.84), (y * 1.18, 0.84), (y * 1.18, 1.70), (y * 0.86, 1.70)]),
            (0.90, [(y * 0.86, 0.72), (y * 1.24, 0.72), (y * 1.24, 1.78), (y * 0.86, 1.78)]),
            (-0.60, [(y * 0.86, 0.72), (y * 1.24, 0.72), (y * 1.24, 1.78), (y * 0.86, 1.78)]),
            (-1.30, [(y * 0.86, 0.86), (y * 1.14, 0.86), (y * 1.14, 1.66), (y * 0.86, 1.66)]),
        ]
        parts.append(wf.finish(wf.loft("sponson", shell, paint), 0.10, 3, smooth_angle=44))
        # Machine-gun port aft of the gun, and rivets round the sponson mouth.
        parts.append(wf.finish(
            wf.cylinder("port", (-0.90, y * 1.24, 1.28), 0.14, 0.12, "Y", 12, metal), 0.02, 2))
        for i in range(7):
            parts.append(wf.finish(
                wf.sphere("rivet", (1.40 - i * 0.44, y * 1.22, 1.80), 0.032, 8, metal),
                0.005, 1))
    return wf.join("sponson", parts)


def sponson_guns(metal, paint):
    """Two 6-pounder 57 mm naval guns, one a side, in ball mountings."""
    parts = []
    for sign in (-1, 1):
        y = sign * (TRACK_OUT + 0.16) * 1.12
        parts.append(wf.finish(
            wf.sphere("mount", (1.44, y, 1.24), 0.30, 18, metal), 0.02, 3))
        parts.append(wf.finish(
            wf.cone("barrel", (2.36, y, 1.24), 0.085, 0.070, 1.70, "X", 14, metal), 0.008, 2))
        parts.append(wf.finish(
            wf.cylinder("collar", (1.72, y, 1.24), 0.115, 0.30, "X", 14, metal), 0.01, 2))
    return wf.join("sponson_gun", parts)


def running_gear(metal, dark, paint):
    """The track frames and the rollers the track runs on - mostly hidden inside them."""
    parts = []
    for sign in (-1, 1):
        y = sign * (TRACK_IN + TRACK_OUT) * 0.5
        # The frame plate the track runs round.
        frame = wf.prism("frame", [
            (3.46, 1.62), (2.20, 2.06), (-1.40, 2.06), (-2.94, 1.72),
            (-3.72, 1.08), (-3.72, 0.62), (-3.14, 0.30), (2.10, 0.30),
            (3.24, 0.58), (3.62, 1.08),
        ], y - 0.30, y + 0.30, paint)
        parts.append(wf.finish(frame, 0.016, 2))
        # Idler at the nose, drive sprocket at the tail.
        parts.append(wf.finish(
            wf.cylinder("idler", (3.34, y, 1.02), 0.52, 0.60, "Y", 20, metal), 0.02, 2))
        parts.append(wf.finish(
            wf.cylinder("sprocket", (-3.62, y, 0.86), 0.46, 0.60, "Y", 18, metal), 0.02, 2))
        for i in range(10):
            parts.append(wf.finish(
                wf.cylinder("roller", (-2.90 + i * 0.58, y, 0.28), 0.11, 0.52, "Y", 10, metal),
                0.01, 1))
    return parts


def tracks(dark):
    """
    The track itself, right round the rhomboid. On a Mark IV this is the whole
    silhouette, not a detail at the bottom of it.
    """
    run = wf.track_run("track", TRACK_PATH, (0.22, TRACK_OUT - TRACK_IN, 0.075), dark)
    run.location.y = (TRACK_IN + TRACK_OUT) * 0.5
    return wf.mirror_y(run)


def beam(trim, metal):
    """
    The unditching beam, carried on rails over the roof and shackled to the tracks when
    the tank bellied out. Oak, and one of the few wooden things on any tank.
    """
    parts = [
        wf.finish(wf.box("beam", (0.30, 0, 2.56), (2.90, 0.30, 0.30), trim), 0.03, 2),
    ]
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("rail", (0.10, sign * 0.74, 2.34), (6.40, 0.09, 0.09), metal), 0.02, 2))
        for x in (-2.40, -0.60, 1.20):
            parts.append(wf.finish(
                wf.box("railfoot", (x, sign * 0.74, 2.26), (0.10, 0.12, 0.16), metal),
                0.015, 1))
        parts.append(wf.finish(
            wf.box("chain", (0.30, sign * 0.20, 2.72), (0.20, 0.06, 0.06), metal), 0.01, 1))
    return wf.join("beam", parts)


def exhaust(dark, metal):
    """Silencer along the roof, which is where it had to go on a tank with no engine bay."""
    parts = [
        wf.finish(wf.cylinder("exhaust", (-1.30, 0, 2.42), 0.16, 1.90, "X", 16, dark),
                  0.02, 2),
        wf.finish(wf.cylinder("pipe", (-2.36, 0, 2.52), 0.075, 0.70, "X", 12, dark), 0.01, 2),
    ]
    for x in (-0.60, -1.90):
        parts.append(wf.finish(
            wf.box("bracket", (x, 0, 2.26), (0.10, 0.34, 0.16), metal), 0.015, 1))
    return wf.join("exhaust", parts)


def machine_gun(metal, paint):
    """Lewis gun in the cab front."""
    parts = [
        wf.finish(wf.sphere("mgmount", (2.86, 0, 2.44), 0.17, 14, paint), 0.015, 2),
        wf.finish(wf.cone("mg", (3.34, 0, 2.44), 0.045, 0.035, 0.70, "X", 12, metal), 0.006, 1),
        wf.finish(wf.cylinder("drum", (3.10, 0, 2.56), 0.11, 0.06, "Y", 12, metal), 0.01, 1),
    ]
    return wf.join("mg", parts)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

PALETTE_BODY = (0x6E / 255, 0x63 / 255, 0x3E / 255)
MUD_BROWN = (0.82, 0.76, 0.64)
FADED = (1.16, 1.13, 1.06)

PARTS = ("hull", "track", "cab", "sponson", "sponson_gun", "beam", "exhaust", "mg")


def weathering():
    """
    Mud, and then more mud.

    A Mark IV lived in ground that had been shelled for three years. Nothing on this
    vehicle stayed the colour it was painted, and the mud reaches the roof rather than
    stopping at the running gear.
    """
    for name in ("hull", "cab", "sponson", "beam", "sponson_gun", "mg"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.34)
        wf.weather(obj, camo=(FADED, MUD_BROWN), cover=(0.28, 0.30), dirt=0.72,
                   wear=0.30, scale=1.15, seed=31, ground=0.0, top=4.6)
    for name in ("track", "exhaust"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.78, wear=0.34, seed=33, ground=0.0, top=3.0)


def build():
    wf.reset()
    paint = wf.role("BODY", KHAKI, roughness=0.82)
    metal = wf.role("METAL", STEEL, roughness=0.46, metallic=0.78)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.66, metallic=0.40)
    trim = wf.role("TRIM", KHAKI, roughness=0.86)
    glass = wf.role("GLASS", GLASS, roughness=0.30, metallic=0.20)

    body = hull(paint, metal, dark, glass)
    wf.join("hull", [body] + running_gear(metal, dark, paint))
    cab(paint, glass, metal)
    sponsons(paint, metal, glass)
    sponson_guns(metal, paint)
    tracks(dark)
    beam(trim, metal)
    exhaust(dark, metal)
    machine_gun(metal, paint)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(8.0)
        wf.camera(azimuth=38, elevation=18)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
