"""
Los Angeles-class attack submarine, at full size.

A shape with one job: to be quiet. The teardrop hull came from USS Albacore, the anechoic
tiles came later, and the reactor means she never has to surface - so unlike U-9 there is
no casing, no deck, and nothing at all designed for running on top of the water.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

BLACK = (0.10, 0.11, 0.12)
STEEL = (0.36, 0.38, 0.40)
DARK = (0.07, 0.07, 0.08)
GLASS = (0.16, 0.22, 0.26)

# 110.3 long, 10.1 beam.
BOW = 55.15
STERN = -55.15
RADIUS = 5.05
SAIL_BASE = 4.20
SAIL_TOP = 10.60


def hull(paint, dark, metal):
    """
    A body of revolution: parallel middle body, a blunt elliptical bow and a long
    tapering tail to the propeller.
    """
    parts = []
    stations = [
        (STERN, 0.45),
        (-52.0, 1.05),
        (-46.0, 1.90),
        (-38.0, 2.90),
        (-28.0, 3.90),
        (-18.0, 4.60),
        (-6.0, 5.00),
        (10.0, RADIUS),
        (26.0, 4.98),
        (38.0, 4.70),
        (46.0, 3.90),
        (51.0, 2.80),
        (54.0, 1.50),
        (BOW, 0.30),
    ]
    sections = [(x, wf.oval(r, r, 24)) for x, r in stations]
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.10, 3, smooth_angle=58))
    # Free-flood holes along the top of the casing.
    for sign in (-1, 1):
        for i in range(10):
            x = -34.0 + i * 7.0
            parts.append(wf.finish(
                wf.box("floodhole", (x, sign * 1.60, RADIUS - 0.30), (1.60, 0.40, 0.30), dark),
                0.05, 1))
    return wf.join("hull", parts)


def sail(paint, metal, dark):
    """The sail - what a surface ship would call a conning tower, faired into the hull."""
    parts = []
    body = [
        (16.0, [(-0.90, SAIL_BASE), (0.90, SAIL_BASE), (0.80, SAIL_TOP), (-0.80, SAIL_TOP)]),
        (20.0, [(-1.35, SAIL_BASE), (1.35, SAIL_BASE), (1.15, SAIL_TOP), (-1.15, SAIL_TOP)]),
        (28.0, [(-1.40, SAIL_BASE), (1.40, SAIL_BASE), (1.20, SAIL_TOP), (-1.20, SAIL_TOP)]),
        (32.5, [(-1.10, SAIL_BASE), (1.10, SAIL_BASE), (0.90, SAIL_TOP), (-0.90, SAIL_TOP)]),
        (34.5, [(-0.30, SAIL_BASE), (0.30, SAIL_BASE), (0.26, SAIL_TOP), (-0.26, SAIL_TOP)]),
    ]
    parts.append(wf.finish(wf.loft("sail", body, paint), 0.42, 5, smooth_angle=52))
    # The fillet where the sail meets the hull.
    fillet = [
        (14.0, wf.oval(1.10, 0.30, 12, centre_z=RADIUS - 0.30)),
        (24.0, wf.oval(2.10, 0.60, 12, centre_z=RADIUS - 0.30)),
        (34.0, wf.oval(1.20, 0.34, 12, centre_z=RADIUS - 0.30)),
    ]
    parts.append(wf.finish(wf.loft("fillet", fillet, paint), 0.30, 3, smooth_angle=50))
    # Bridge cockpit cut into the top.
    parts.append(wf.finish(
        wf.box("cockpit", (30.0, 0, SAIL_TOP - 0.30), (2.60, 1.70, 0.70), dark), 0.12, 2))
    return wf.join("sail", parts)


def planes(paint, metal):
    """Fairwater planes on the sail, and the cruciform stern planes and rudders."""
    parts = []
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("fairwater", (25.0, sign * 3.20, SAIL_TOP - 2.20), (3.40, 4.80, 0.44),
                   paint), 0.14, 3))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("sternplane", (-44.0, sign * 3.60, -0.10), (5.20, 5.40, 0.50), paint),
            0.16, 3))
    return wf.join("planes", parts)


def rudder(paint, metal):
    """Upper and lower rudders on the centreline."""
    parts = []
    for z, height in ((3.60, 6.40), (-3.60, 6.40)):
        parts.append(wf.finish(
            wf.box("rudder", (-44.0, 0, z), (5.20, 0.50, height), paint), 0.16, 3))
    return wf.join("rudder", parts)


def masts(metal, dark, glass):
    """Periscopes, the snorkel and the ESM and radar masts, all raised."""
    parts = []
    for x, height, r in ((30.6, 6.4, 0.13), (29.0, 5.6, 0.12), (27.2, 4.8, 0.11),
                         (25.6, 4.2, 0.10)):
        parts.append(wf.finish(
            wf.cylinder("mast", (x, 0, SAIL_TOP + height * 0.5 - 0.2), r, height, "Z", 10,
                        metal), 0.02, 1))
    parts.append(wf.finish(
        wf.box("esmhead", (25.6, 0, SAIL_TOP + 3.9), (0.34, 0.34, 0.60), dark), 0.04, 1))
    parts.append(wf.finish(
        wf.box("scopehead", (30.6, 0, SAIL_TOP + 5.9), (0.40, 0.26, 0.40), glass), 0.04, 1))
    return wf.join("masts", parts)


def screw(metal, dark):
    """A seven-bladed skewed screw, behind a shroud - the quietest thing they could build."""
    parts = [
        wf.finish(wf.cone("hubcone", (-56.6, 0, 0), 0.55, 0.16, 2.6, "X", 16, metal), 0.06, 2),
        wf.finish(wf.cylinder("hub", (-54.8, 0, 0), 0.70, 1.4, "X", 16, metal), 0.06, 2),
    ]
    for i in range(7):
        a = 2 * math.pi * i / 7
        blade = wf.box("blade", (-55.2, 1.55 * math.cos(a), 1.55 * math.sin(a)),
                       (0.9, 1.9, 0.14), metal)
        wf.rotate(blade, x=math.degrees(a), y=26)
        parts.append(wf.finish(blade, 0.04, 2))
    return wf.join("screw", parts)


def sonar(dark, paint):
    """The spherical bow array, which is most of what is inside the nose."""
    parts = [
        wf.finish(wf.sphere("sphere", (46.0, 0, -0.20), 3.60, 22, dark), 0.10, 3),
    ]
    # Towed-array fairing down the starboard side.
    parts.append(wf.finish(
        wf.box("towedarray", (-8.0, -4.80, 1.40), (58.0, 0.44, 0.44), paint), 0.14, 2))
    return wf.join("sonar", parts)


def tubes(metal, dark, paint):
    """Four 21-inch tubes, angled out amidships - the bow is full of sonar."""
    parts = []
    for sign in (-1, 1):
        for dz in (-0.9, 0.9):
            parts.append(wf.finish(
                wf.rotate(wf.cylinder("tubedoor", (30.0, sign * 4.70, dz), 0.44, 0.36, "Y",
                                      16, metal), z=sign * 8), 0.05, 2))
            parts.append(wf.finish(
                wf.box("tubeshutter", (30.0, sign * 4.86, dz), (1.30, 0.10, 1.30), paint),
                0.08, 2))
    return wf.join("tubes", parts)


def vls(metal, dark, paint):
    """Twelve vertical launch tubes for Tomahawk, forward of the sonar sphere."""
    parts = []
    for i in range(6):
        for sign in (-1, 1):
            x = 40.0 - i * 1.60
            y = sign * 2.10
            parts.append(wf.finish(
                wf.cylinder("vlscap", (x, y, -4.30), 0.44, 0.24, "Z", 14, metal), 0.04, 2))
    parts.append(wf.finish(
        wf.box("vlsdeck", (36.0, 0, -4.36), (11.0, 5.40, 0.20), paint), 0.06, 1))
    return wf.join("vls", parts)


HULL_STATIONS = [
    (STERN, 0.45), (-52.0, 1.05), (-46.0, 1.90), (-38.0, 2.90), (-28.0, 3.90),
    (-18.0, 4.60), (-6.0, 5.00), (10.0, RADIUS), (26.0, 4.98), (38.0, 4.70),
    (46.0, 3.90), (51.0, 2.80), (54.0, 1.50), (BOW, 0.30),
]


def hull_radius(x):
    """The hull's radius at [x], so things laid on it follow the taper."""
    for (x0, r0), (x1, r1) in zip(HULL_STATIONS, HULL_STATIONS[1:]):
        if x <= x1:
            t = (x - x0) / max(1e-6, x1 - x0)
            return r0 + (r1 - r0) * max(0.0, min(1.0, t))
    return HULL_STATIONS[-1][1]


def tiles(dark, paint):
    """
    Anechoic tiles: rubber squares glued over the hull to absorb active sonar. They are
    the reason a modern boat looks matte black and slightly uneven rather than painted.

    Each belt is sized from the hull's own radius at that station - a constant radius
    stands proud wherever the hull tapers, and reads as a set of rings clamped round it.
    """
    parts = []
    for i in range(11):
        x0 = -40.0 + i * 7.6
        x1 = x0 + 7.2
        r = min(hull_radius(x0), hull_radius(x1)) - 0.06
        if r < 0.6:
            continue
        belt = [
            (x0, wf.oval(r, r, 24)),
            (x1, wf.oval(r, r, 24)),
        ]
        parts.append(wf.finish(wf.loft("tilebelt", belt, dark, cap_first=False,
                                       cap_last=False), 0.04, 1, smooth_angle=58))
    return wf.join("tiles", parts)


PALETTE_BODY = (0x36 / 255, 0x3B / 255, 0x3F / 255)
TILE_DARK = (0.72, 0.72, 0.74)
TILE_LIGHT = (1.14, 1.14, 1.16)

PARTS = ("hull", "sail", "planes", "masts", "rudder", "screw", "sonar", "tubes",
         "vls", "tiles")


def weathering():
    """
    Nothing but wear: no camouflage, no waterline, and the tiles weather unevenly where
    they have been replaced.
    """
    for name in ("hull", "sail", "planes", "rudder", "tiles", "vls", "tubes", "sonar"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=1.60)
        wf.weather(obj, camo=(TILE_LIGHT, TILE_DARK), cover=(0.26, 0.26), dirt=0.24,
                   wear=0.24, scale=6.0, seed=139, ground=-RADIUS, top=14.0)
    for name in ("masts", "screw"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.12, wear=0.34, seed=149, ground=-RADIUS, top=20.0)


def build():
    wf.reset()
    paint = wf.role("BODY", BLACK, roughness=0.88)
    metal = wf.role("METAL", STEEL, roughness=0.42, metallic=0.78)
    dark = wf.role("DARK", DARK, roughness=0.92, metallic=0.10)
    glass = wf.role("GLASS", GLASS, roughness=0.20, metallic=0.30)

    hull(paint, dark, metal)
    tiles(dark, paint)
    sail(paint, metal, dark)
    planes(paint, metal)
    rudder(paint, metal)
    masts(metal, dark, glass)
    screw(metal, dark)
    sonar(dark, paint)
    tubes(metal, dark, paint)
    vls(metal, dark, paint)

    weathering()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(60.0, floor_z=-6.0)
        wf.camera(azimuth=50, elevation=18)
        wf.render(args[0], samples=18)
    if len(args) > 1:
        wf.export_glb(args[1])
