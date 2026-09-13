"""
SM U-9, at full size.

A 1910 boat with paraffin engines and a crew of twenty-nine. On 22 September 1914 she
sank three British armoured cruisers in an hour, which is the morning the submarine
stopped being a curiosity. The shape is a surface ship with a pressure hull inside it -
the fine casing and the flat deck are for running on the surface, not for diving.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

GREY = (0.26, 0.28, 0.30)
STEEL = (0.38, 0.40, 0.42)
DARK = (0.10, 0.11, 0.12)
BRASS = (0.52, 0.42, 0.18)
GLASS = (0.16, 0.24, 0.28)

# 57.4 long, 6.0 beam, 3.1 draught.
BOW = 28.70
STERN = -28.70
BEAM_HALF = 3.00
KEEL = -3.10
DECK = 1.30              # casing deck, just above the waterline
TOWER_TOP = 4.40


def pressure_hull(paint, dark, metal):
    """The pressure hull: a circular tube, tapering to a point at each end."""
    parts = []
    stations = [
        (STERN, 0.20, 0.20, -1.10),
        (-24.0, 0.90, 0.90, -1.10),
        (-18.0, 1.60, 1.60, -1.05),
        (-9.0, 2.10, 2.10, -1.00),
        (0.0, 2.20, 2.20, -1.00),
        (9.0, 2.10, 2.10, -1.00),
        (17.0, 1.70, 1.70, -1.05),
        (23.0, 1.10, 1.10, -1.10),
        (BOW, 0.22, 0.22, -1.10),
    ]
    sections = [(x, wf.oval(hy, hz, 22, centre_z=cz)) for x, hy, hz, cz in stations]
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.06, 3, smooth_angle=54))
    # Saddle tanks either side amidships.
    for sign in (-1, 1):
        saddle = [
            (-14.0, wf.oval(0.30, 0.50, 12, centre_z=-1.30)),
            (-6.0, wf.oval(0.80, 0.95, 12, centre_z=-1.30)),
            (6.0, wf.oval(0.80, 0.95, 12, centre_z=-1.30)),
            (14.0, wf.oval(0.30, 0.50, 12, centre_z=-1.30)),
        ]
        parts.append(wf.finish(
            wf.loft("saddle", [(x, [(yy + sign * 2.05, zz) for yy, zz in pts])
                               for x, pts in saddle], paint),
            0.08, 3, smooth_angle=50))
    return wf.join("hull", parts)


def casing(paint, metal, dark):
    """
    The outer casing and free-flooding deck. It is not watertight - it is a walkway and
    a fairing, and the slots along its side are there to let the sea in and out.
    """
    parts = []
    sections = []
    for x, half, z in ((STERN + 0.6, 0.55, 0.55), (-22.0, 1.10, 0.90), (-12.0, 1.55, 1.15),
                       (0.0, 1.70, 1.30), (12.0, 1.55, 1.20), (22.0, 1.05, 1.00),
                       (BOW - 0.4, 0.35, 0.75)):
        sections.append((x, [(-half, z - 0.70), (half, z - 0.70), (half, z), (-half, z)]))
    parts.append(wf.finish(wf.loft("casing", sections, paint), 0.06, 3, smooth_angle=46))
    # Free-flooding slots along both sides.
    for sign in (-1, 1):
        for i in range(16):
            x = -21.0 + i * 2.8
            parts.append(wf.finish(
                wf.box("slot", (x, sign * 1.62, 0.92), (1.40, 0.14, 0.26), dark), 0.03, 1))
    # Bow and stern capstans, and the jumping wire from stem to tower.
    parts.append(wf.finish(
        wf.cylinder("capstan", (24.0, 0, 1.20), 0.30, 0.40, "Z", 12, metal), 0.03, 2))
    for x0, x1, z0, z1 in ((26.0, 6.0, 1.10, TOWER_TOP - 0.4),
                           (-6.0, -26.0, TOWER_TOP - 0.4, 1.00)):
        mid = ((x0 + x1) * 0.5, (z0 + z1) * 0.5)
        length = math.hypot(x1 - x0, z1 - z0)
        parts.append(wf.finish(
            wf.rotate(wf.cylinder("jumpingwire", (mid[0], 0, mid[1]), 0.035, length, "X",
                                  8, metal),
                      y=-math.degrees(math.atan2(z1 - z0, x1 - x0))), 0.006, 1))
    return wf.join("casing", parts)


def tower(paint, metal, glass, dark):
    """A small, almost cylindrical conning tower - this is 1910, not 1943."""
    parts = []
    body = [
        (-2.60, wf.oval(0.80, 0.80, 16, centre_z=2.40)),
        (-1.60, wf.oval(1.05, 1.05, 16, centre_z=2.40)),
        (1.60, wf.oval(1.05, 1.05, 16, centre_z=2.40)),
        (2.60, wf.oval(0.80, 0.80, 16, centre_z=2.40)),
    ]
    parts.append(wf.finish(wf.loft("tower", body, paint), 0.12, 3, smooth_angle=48))
    parts.append(wf.finish(
        wf.cylinder("hatch", (0.0, 0, TOWER_TOP - 0.10), 0.42, 0.16, "Z", 14, metal),
        0.03, 2))
    for i in range(6):
        a = 2 * math.pi * i / 6
        parts.append(wf.finish(
            wf.box("scuttle", (1.05 * math.cos(a) * 0.9, 1.05 * math.sin(a) * 0.9, 2.90),
                   (0.20, 0.20, 0.20), glass), 0.02, 1))
    return wf.join("tower", parts)


def bridge(paint, metal, dark):
    """The open bridge round the top of the tower, with its plating and voice pipes."""
    parts = []
    ring = wf.cylinder("bridge", (0.0, 0, TOWER_TOP - 0.45), 1.50, 0.90, "Z", 18, paint)
    wf.cut(ring, wf.cylinder("cut", (0.0, 0, TOWER_TOP - 0.35), 1.30, 1.00, "Z", 18))
    parts.append(wf.finish(ring, 0.05, 2))
    parts.append(wf.finish(
        wf.box("windbreak", (1.40, 0, TOWER_TOP - 0.10), (0.16, 1.70, 0.60), paint), 0.04, 2))
    parts.append(wf.finish(
        wf.cylinder("voicepipe", (-1.10, 0.50, TOWER_TOP - 0.10), 0.09, 0.70, "Z", 10, metal),
        0.014, 1))
    parts.append(wf.finish(
        wf.cylinder("binnacle", (-0.90, -0.55, TOWER_TOP - 0.10), 0.16, 0.50, "Z", 12, metal),
        0.02, 1))
    return wf.join("bridge", parts)


def periscopes(metal, brass):
    parts = []
    for x, height in ((0.55, 4.60), (-0.55, 3.90)):
        parts.append(wf.finish(
            wf.cylinder("periscope", (x, 0, TOWER_TOP + height * 0.5 - 0.4), 0.075, height,
                        "Z", 10, metal), 0.012, 1))
        parts.append(wf.finish(
            wf.box("eyepiece", (x, 0, TOWER_TOP + height - 0.5), (0.24, 0.14, 0.16), brass),
            0.02, 1))
    parts.append(wf.finish(
        wf.cone("wtmast", (-2.20, 0, TOWER_TOP + 2.4), 0.09, 0.03, 5.6, "Z", 8, metal),
        0.010, 1))
    return wf.join("periscope", parts)


def deck_gun(metal, paint, dark):
    """The 8.8 cm deck gun, fitted to the older boats once the war started."""
    parts = [
        wf.finish(wf.cylinder("gunbase", (6.60, 0, 1.42), 0.52, 0.42, "Z", 16, paint),
                  0.05, 2),
        wf.finish(wf.box("cradle", (6.60, 0, 1.86), (0.70, 0.60, 0.52), metal), 0.06, 2),
        wf.finish(wf.cone("barrel", (8.60, 0, 2.02), 0.11, 0.085, 3.40, "X", 14, metal),
                  0.02, 2),
        wf.finish(wf.box("shield", (6.90, 0, 2.10), (0.10, 1.10, 0.90), paint), 0.04, 2),
    ]
    return wf.join("gun", parts)


def planes(metal, paint):
    """Bow and stern hydroplanes, and the rudders."""
    parts = []
    for x, half, chord in ((21.0, 2.60, 1.30), (-22.0, 2.90, 1.50)):
        for sign in (-1, 1):
            parts.append(wf.finish(
                wf.box("plane", (x, sign * (1.4 + half * 0.5), -1.10),
                       (chord, half, 0.16), paint), 0.04, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-26.4, 0, -1.10), (1.40, 0.18, 2.40), paint), 0.05, 2))
    parts.append(wf.finish(
        wf.box("skeg", (-24.0, 0, -2.40), (4.00, 0.30, 0.60), paint), 0.05, 2))
    return wf.join("planes", parts)


def screws(metal):
    parts = []
    for sign in (-1, 1):
        y = sign * 1.15
        parts.append(wf.finish(
            wf.cylinder("shaft", (-23.0, y, -1.30), 0.16, 4.0, "X", 10, metal), 0.02, 2))
        parts.append(wf.finish(
            wf.cylinder("bossing", (-21.5, y, -1.30), 0.34, 3.0, "X", 12, metal), 0.04, 2))
        for i in range(3):
            a = 2 * math.pi * i / 3
            parts.append(wf.finish(
                wf.rotate(wf.box("blade", (-25.4, y + 0.42 * math.cos(a), -1.30 + 0.42 * math.sin(a)),
                                 (0.10, 0.34, 0.80), metal),
                          x=math.degrees(a) + 26), 0.02, 1))
    return wf.join("screw", parts)


def tubes(metal, dark, paint):
    """Two tubes forward, two aft - the whole armament, with six torpedoes aboard."""
    parts = []
    for x, sign_x in ((BOW - 1.2, 1), (STERN + 1.2, -1)):
        for dy in (-0.70, 0.70):
            parts.append(wf.finish(
                wf.cylinder("tubedoor", (x, dy, -1.10), 0.32, 0.24, "X", 14, dark), 0.03, 2))
            parts.append(wf.finish(
                wf.cylinder("tuberim", (x - sign_x * 0.10, dy, -1.10), 0.38, 0.16, "X", 14,
                            metal), 0.02, 2))
    return wf.join("tubes", parts)


def vents(dark, metal):
    """Ballast tank vents along the casing, and the engine exhaust."""
    parts = []
    for sign in (-1, 1):
        for i in range(5):
            x = -12.0 + i * 6.0
            parts.append(wf.finish(
                wf.cylinder("vent", (x, sign * 1.40, 1.36), 0.13, 0.22, "Z", 10, dark),
                0.02, 1))
    parts.append(wf.finish(
        wf.cylinder("exhaust", (-4.20, 1.30, 1.60), 0.26, 0.60, "Z", 12, dark), 0.03, 2))
    return wf.join("vents", parts)


PALETTE_BODY = (0x58 / 255, 0x5F / 255, 0x63 / 255)
GREY_LIGHT = (1.12, 1.12, 1.14)
GREY_DARK = (0.82, 0.83, 0.86)
BOOT = (0.34, 0.34, 0.36)
HULL_RED = (1.26, 0.52, 0.40)

PARTS = ("hull", "casing", "tower", "bridge", "periscope", "gun", "planes", "screw",
         "tubes", "vents")


def weathering():
    for name in ("hull", "casing", "tower", "bridge", "gun", "planes"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.90)
        wf.weather(obj, camo=(GREY_LIGHT, GREY_DARK), cover=(0.24, 0.24), dirt=0.40,
                   wear=0.30, scale=4.0, seed=113, ground=KEEL, top=8.0)
    for name in ("periscope", "screw", "tubes", "vents"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.24, wear=0.34, seed=127, ground=KEEL, top=10.0)


def waterline():
    for name in ("hull", "casing"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        col = obj.data.color_attributes.get("Col")
        if col is None:
            continue
        bpy.context.view_layer.update()
        to_world = obj.matrix_world
        for i, vert in enumerate(obj.data.vertices):
            z = (to_world @ vert.co).z
            tint = HULL_RED if z < -0.55 else (BOOT if z < 0.25 else None)
            if tint is None:
                continue
            r, g, b = (c / wf.TINT_RANGE for c in tint)
            col.data[i].color = (r, g, b, 1.0)


def build():
    wf.reset()
    paint = wf.role("BODY", GREY, roughness=0.76)
    metal = wf.role("METAL", STEEL, roughness=0.44, metallic=0.74)
    dark = wf.role("DARK", DARK, roughness=0.74, metallic=0.30)
    brass = wf.role("ACCENT", BRASS, roughness=0.36, metallic=0.80)
    glass = wf.role("GLASS", GLASS, roughness=0.18, metallic=0.30)

    pressure_hull(paint, dark, metal)
    casing(paint, metal, dark)
    tower(paint, metal, glass, dark)
    bridge(paint, metal, dark)
    periscopes(metal, brass)
    deck_gun(metal, paint, dark)
    planes(metal, paint)
    screws(metal)
    tubes(metal, dark, paint)
    vents(dark, metal)

    weathering()
    waterline()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(32.0, floor_z=-3.4)
        wf.camera(azimuth=50, elevation=18)
        wf.render(args[0], samples=18)
    if len(args) > 1:
        wf.export_glb(args[1])
