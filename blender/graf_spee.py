"""
Admiral Graf Spee, at full size.

A "pocket battleship": cruiser displacement, battleship guns, diesel engines and the legs
to cross an ocean - built to the letter of a treaty that said 10,000 tons and nothing
about what you put on them. She also carried the first radar ever fitted to a warship,
the Seetakt mattress on the front of her tower.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

GREY = (0.30, 0.33, 0.36)
STEEL = (0.40, 0.42, 0.44)
DARK = (0.11, 0.12, 0.13)
GLASS = (0.18, 0.26, 0.30)

BOW = 93.0
STERN = -93.0
BEAM_HALF = 10.85
KEEL = -7.40
DECK = 7.40
DECK_BOW = 10.60
DECK_STERN = 5.60


def deck_line(x):
    t = x / BOW
    if t >= 0:
        return DECK + (DECK_BOW - DECK) * t ** 2.4
    return DECK + (DECK_STERN - DECK) * (-t) ** 1.6


def beam_at(x):
    t = x / BOW
    if t > 0.78:
        return BEAM_HALF * max(0.05, 1.0 - ((t - 0.78) / 0.22) ** 1.4 * 0.96)
    if t < -0.78:
        return BEAM_HALF * max(0.24, 1.0 - ((-t - 0.78) / 0.22) ** 1.2 * 0.80)
    return BEAM_HALF * (1.0 - 0.10 * t * t)


def hull(paint, dark, metal):
    parts = []
    sections = []
    for x in (STERN, -88.0, -78.0, -62.0, -42.0, -20.0, 0.0, 20.0, 42.0, 62.0,
              76.0, 84.0, 90.0, BOW):
        t = abs(x / BOW)
        flare = 0.20 * max(0.0, x / BOW) ** 1.5
        fullness = 0.96 - 0.32 * max(0.0, t - 0.50) / 0.50
        sections.append((x, wf.hull_section(beam_at(x), deck_line(x), KEEL, flare=flare,
                                            fullness=fullness, camber=0.40,
                                            marks=(-0.9, 0.6))))
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.20, 3, smooth_angle=50))
    # Atlantic bow: she was given a raked clipper stem in her 1938 refit.
    parts.append(wf.finish(
        wf.box("stem", (BOW + 0.6, 0, (DECK_BOW + 2.0) * 0.5), (1.4, 0.9, DECK_BOW - 1.0),
               paint), 0.14, 3))
    for sign, dy in ((-1, 4.2), (1, 4.2)):
        y = sign * dy
        parts.append(wf.finish(
            wf.cylinder("shaft", (-76.0, y, KEEL + 1.8), 0.62, 16.0, "X", 12, metal), 0.07, 2))
        parts.append(wf.finish(
            wf.cylinder("screw", (-84.6, y, KEEL + 1.8), 2.10, 0.55, "X", 16, metal), 0.06, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-88.0, 0, KEEL + 2.8), (3.2, 0.55, 4.6), metal), 0.10, 2))
    return wf.join("hull", parts)


def deck(trim, metal, dark):
    parts = []
    sections = []
    for x in (STERN + 2.0, -70.0, -35.0, 0.0, 35.0, 64.0, 82.0, BOW - 1.2):
        half = beam_at(x) * 0.985
        z = deck_line(x)
        crown = 0.40 * (1 - (x / BOW) ** 2)
        sections.append((x, [(-half, z - 0.55), (half, z - 0.55),
                             (half, z + crown), (-half, z + crown)]))
    parts.append(wf.finish(wf.loft("deck", sections, trim), 0.10, 2, smooth_angle=44))
    parts.append(wf.finish(
        wf.rotate(wf.box("breakwater", (62.0, 0, deck_line(62.0) + 1.20),
                         (0.45, beam_at(62.0) * 1.4, 2.40), trim), y=16), 0.08, 2))
    for x in range(-86, 92, 7):
        half = beam_at(x) * 0.99
        z = deck_line(x)
        for sign in (-1, 1):
            parts.append(wf.finish(
                wf.box("stanchion", (float(x), sign * half, z + 0.85), (0.18, 0.18, 1.50), dark),
                0.03, 1))
    return wf.join("deck", parts)


def triple_turret(name, x, base_z, paint, metal, facing=1.0):
    """One triple 28 cm turret - the guns that made her a battleship on paper."""
    parts = []
    house = [
        (x - facing * 6.0, [(-5.2, base_z), (5.2, base_z), (4.6, base_z + 3.8), (-4.6, base_z + 3.8)]),
        (x - facing * 1.6, [(-5.9, base_z), (5.9, base_z), (5.2, base_z + 4.2), (-5.2, base_z + 4.2)]),
        (x + facing * 2.8, [(-5.9, base_z), (5.9, base_z), (5.0, base_z + 4.2), (-5.0, base_z + 4.2)]),
        (x + facing * 5.6, [(-4.8, base_z + 0.7), (4.8, base_z + 0.7),
                            (4.2, base_z + 3.6), (-4.2, base_z + 3.6)]),
    ]
    parts.append(wf.finish(wf.loft(name, house, paint), 0.40, 4, smooth_angle=46))
    parts.append(wf.finish(
        wf.cylinder("barbette", (x, 0, base_z - 1.4), 5.6, 3.0, "Z", 22, paint), 0.18, 2))
    for dy in (-3.2, 0.0, 3.2):
        parts.append(wf.finish(
            wf.cylinder("sleeve", (x + facing * 7.4, dy, base_z + 2.3), 0.80, 3.4, "X", 16,
                        paint), 0.10, 2))
        parts.append(wf.finish(
            wf.cone("barrel", (x + facing * 15.0, dy, base_z + 2.3), 0.42, 0.32, 12.0,
                    "X", 16, metal), 0.03, 2))
    return parts


def turret_a(paint, metal):
    return wf.join("turret_a", triple_turret("turret_a", 60.0, deck_line(60.0) + 0.6,
                                             paint, metal))


def turret_b(paint, metal):
    return wf.join("turret_b", triple_turret("turret_b", -62.0, deck_line(-62.0) + 0.6,
                                             paint, metal, facing=-1.0))


def tower(paint, glass, metal, dark):
    """
    The tower superstructure: a single block rising in steps, with the rangefinder on top
    and the Seetakt radar mattress on its face.
    """
    parts = []
    base = deck_line(10.0)
    levels = [(6.0, 34.0, 17.0, 5.0), (8.0, 26.0, 14.0, 4.4), (10.0, 19.0, 11.0, 4.0),
              (11.5, 13.0, 9.0, 3.6), (12.5, 9.0, 7.0, 3.2)]
    z = base
    for x, length, width, height in levels:
        parts.append(wf.finish(
            wf.box("level", (x, 0, z + height * 0.5), (length, width, height), paint), 0.34, 3))
        if length > 18:
            for i in range(7):
                parts.append(wf.finish(
                    wf.box("window", (x + length * 0.5 - 0.2, -4.0 + i * 1.4, z + height * 0.62),
                           (0.35, 1.00, 0.90), glass), 0.05, 2))
        z += height
    parts.append(wf.finish(
        wf.cylinder("director", (12.5, 0, z + 1.8), 2.6, 3.6, "Z", 18, paint), 0.20, 3))
    parts.append(wf.finish(
        wf.box("rangefinder", (12.5, 0, z + 4.0), (2.6, 10.4, 1.8), paint), 0.30, 3))
    return wf.join("tower", parts)


def funnel(paint, dark, metal):
    """One broad funnel with the German cap - flat-topped, and wider than it is deep."""
    x = -18.0
    base = deck_line(x) + 7.6
    height = 11.0
    sections = []
    for t in (0.0, 0.5, 1.0):
        sections.append((x - t * 1.4, wf.oval(5.4 - 0.9 * t, 3.2 - 0.5 * t, 16,
                                              centre_z=base + height * t)))
    parts = [wf.finish(wf.loft("funnel", sections, paint), 0.20, 3, smooth_angle=48)]
    top = base + height
    parts.append(wf.finish(
        wf.cylinder("cap", (x - 1.5, 0, top + 0.15), 4.8, 0.7, "Z", 18, dark), 0.14, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("steampipe", (x - 0.8, sign * 3.0, base + height * 0.6), 0.40,
                        height * 1.1, "Z", 10, metal), 0.05, 1))
    return wf.join("funnel", parts)


def mast(metal, dark):
    parts = []
    base = deck_line(-2.0) + 22.0
    for dx, dy in ((2.0, 0.0), (-1.4, 2.2), (-1.4, -2.2)):
        parts.append(wf.finish(
            wf.rotate(wf.cone("leg", (-2.0 + dx * 0.5, dy * 0.5, base + 6.0), 0.34, 0.18,
                              13.0, "Z", 10, metal),
                      x=math.degrees(math.atan2(dy, 13.0)),
                      y=-math.degrees(math.atan2(dx, 13.0))), 0.03, 2))
    parts.append(wf.finish(
        wf.cone("topmast", (-2.0, 0, base + 17.0), 0.20, 0.07, 9.0, "Z", 8, metal), 0.02, 1))
    for z, half in ((base + 11.0, 7.0), (base + 16.0, 4.5)):
        parts.append(wf.finish(
            wf.cylinder("yard", (-2.0, 0, z), 0.16, half * 2, "Y", 8, metal), 0.03, 1))
    return wf.join("mast", parts)


def radar(metal, dark):
    """FuMO 22 Seetakt: a flat mattress array on the face of the director."""
    base = deck_line(10.0) + 20.2 + 5.8
    parts = [
        wf.finish(wf.box("mattress", (14.2, 0, base + 4.0), (0.32, 6.0, 2.2), metal), 0.05, 2),
    ]
    for i in range(6):
        parts.append(wf.finish(
            wf.box("dipole", (14.4, -2.4 + i * 0.96, base + 4.0), (0.12, 0.10, 2.0), metal),
            0.02, 1))
    return wf.join("radar", parts)


def secondary(paint, metal):
    """15 cm guns in single shielded mountings along the upper deck."""
    parts = []
    for x in (36.0, 24.0, -36.0, -48.0):
        for sign in (-1, 1):
            y = sign * beam_at(x) * 0.78
            z = deck_line(x) + 3.0
            parts.append(wf.finish(
                wf.cylinder("base", (x, y, z - 0.9), 1.5, 1.4, "Z", 16, paint), 0.10, 2))
            parts.append(wf.finish(
                wf.box("shield", (x, y, z + 0.9), (2.8, 3.0, 2.4), paint), 0.20, 3))
            face = 1.0 if x > 0 else -1.0
            parts.append(wf.finish(
                wf.cone("barrel", (x + face * 4.2, y, z + 1.0), 0.20, 0.15, 5.2, "X", 12,
                        metal), 0.03, 2))
    return wf.join("secondary", parts)


def aa(paint, metal, dark):
    """10.5 cm heavy AA in twin mounts, and the 3.7 cm and 2 cm guns above them."""
    parts = []
    for x in (2.0, -8.0):
        for sign in (-1, 1):
            y = sign * 8.6
            z = deck_line(x) + 9.6
            parts.append(wf.finish(
                wf.cylinder("tub", (x, y, z - 0.9), 2.4, 1.6, "Z", 16, paint), 0.12, 2))
            parts.append(wf.finish(
                wf.box("mount", (x, y, z + 0.8), (2.6, 3.0, 1.8), paint), 0.16, 3))
            for dy in (-0.9, 0.9):
                parts.append(wf.finish(
                    wf.rotate(wf.cone("aabarrel", (x + 3.0, y + dy, z + 1.4), 0.16, 0.11, 4.4,
                                      "X", 10, metal), y=-24), 0.03, 1))
    for x in (-26.0, 16.0):
        for sign in (-1, 1):
            y = sign * 7.0
            z = deck_line(x) + 13.0
            parts.append(wf.finish(
                wf.cylinder("lighttub", (x, y, z - 0.7), 1.4, 1.2, "Z", 14, paint), 0.08, 2))
            for dy in (-0.35, 0.35):
                parts.append(wf.finish(
                    wf.rotate(wf.cone("37mm", (x + 1.4, y + dy, z + 0.6), 0.10, 0.07, 2.6,
                                      "X", 8, metal), y=-30), 0.02, 1))
    return wf.join("aa", parts)


def catapult(metal, trim, paint):
    """The catapult and the Arado 196 amidships - her eyes in the South Atlantic."""
    parts = []
    x = -34.0
    z = deck_line(x) + 9.0
    parts.append(wf.finish(
        wf.box("catapult", (x, 0, z), (24.0, 3.0, 1.1), metal), 0.10, 2))
    for i in range(5):
        parts.append(wf.finish(
            wf.box("trestle", (x - 10.0 + i * 5.0, 0, z - 1.4), (1.2, 2.6, 1.8), metal),
            0.08, 1))
    parts.append(wf.finish(
        wf.cylinder("cranepost", (-48.0, 0, deck_line(-48.0) + 6.0), 0.9, 9.0, "Z", 12, metal),
        0.10, 2))
    parts.append(wf.finish(
        wf.rotate(wf.cone("cranejib", (-40.0, 0, deck_line(-40.0) + 11.0), 0.60, 0.24, 17.0,
                          "X", 10, metal), y=-26), 0.06, 2))
    return wf.join("catapult", parts)


def boats(trim, metal):
    parts = []
    for x, y, length in ((-4.0, -10.0, 10.0), (-4.0, 10.0, 10.0),
                         (-14.0, -10.0, 8.0), (-14.0, 10.0, 8.0)):
        base = deck_line(x) + 7.0
        body = [
            (x + length * 0.5, wf.oval(0.18, 0.28, 10, centre_z=base + 0.5)),
            (x + length * 0.22, wf.oval(1.05, 0.75, 10, centre_z=base + 0.4)),
            (x - length * 0.22, wf.oval(1.05, 0.75, 10, centre_z=base + 0.4)),
            (x - length * 0.5, wf.oval(0.38, 0.42, 10, centre_z=base + 0.5)),
        ]
        parts.append(wf.finish(
            wf.loft("boat", [(sx, [(yy + y, zz) for yy, zz in pts]) for sx, pts in body],
                    trim), 0.10, 3, smooth_angle=48))
        for dx in (-length * 0.3, length * 0.3):
            parts.append(wf.finish(
                wf.box("skid", (x + dx, y, base - 0.5), (0.5, 2.8, 0.9), metal), 0.06, 1))
    return wf.join("boats", parts)


PALETTE_BODY = (0x7B / 255, 0x85 / 255, 0x8A / 255)
GREY_LIGHT = (1.12, 1.12, 1.13)
GREY_DARK = (0.84, 0.85, 0.88)
BOOT = (0.28, 0.28, 0.30)
HULL_RED = (1.30, 0.52, 0.40)

PARTS = ("hull", "deck", "turret_a", "turret_b", "tower", "funnel", "mast", "radar",
         "secondary", "aa", "catapult", "boats")


def weathering():
    for name in ("hull", "deck", "turret_a", "turret_b", "tower", "funnel",
                 "secondary", "aa", "catapult", "boats"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=2.40)
        wf.weather(obj, camo=(GREY_LIGHT, GREY_DARK), cover=(0.24, 0.24), dirt=0.28,
                   wear=0.26, scale=12.0, seed=131, ground=KEEL, top=22.0)
    for name in ("mast", "radar"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.08, wear=0.30, seed=137, ground=KEEL, top=50.0)


def waterline():
    obj = bpy.data.objects.get("hull")
    if obj is None:
        return
    col = obj.data.color_attributes.get("Col")
    if col is None:
        return
    bpy.context.view_layer.update()
    to_world = obj.matrix_world
    for i, vert in enumerate(obj.data.vertices):
        z = (to_world @ vert.co).z
        tint = HULL_RED if z < -0.9 else (BOOT if z < 0.6 else None)
        if tint is None:
            continue
        r, g, b = (c / wf.TINT_RANGE for c in tint)
        col.data[i].color = (r, g, b, 1.0)


def build():
    wf.reset()
    paint = wf.role("BODY", GREY, roughness=0.74)
    metal = wf.role("METAL", STEEL, roughness=0.46, metallic=0.72)
    dark = wf.role("DARK", DARK, roughness=0.74, metallic=0.30)
    trim = wf.role("TRIM", GREY, roughness=0.78)
    glass = wf.role("GLASS", GLASS, roughness=0.16, metallic=0.30)

    hull(paint, dark, metal)
    deck(trim, metal, dark)
    turret_a(paint, metal)
    turret_b(paint, metal)
    tower(paint, glass, metal, dark)
    funnel(paint, dark, metal)
    mast(metal, dark)
    radar(metal, dark)
    secondary(paint, metal)
    aa(paint, metal, dark)
    catapult(metal, trim, paint)
    boats(trim, metal)

    weathering()
    waterline()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(100.0)
        wf.camera(azimuth=52, elevation=18)
        wf.render(args[0], samples=18)
    if len(args) > 1:
        wf.export_glb(args[1])
