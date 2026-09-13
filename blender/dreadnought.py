"""
HMS Dreadnought, 1906, at full size.

She made every battleship afloat obsolete in an afternoon, including Britain's own: ten
12-inch guns where the standard was four, and turbines instead of reciprocating engines.
The one thing she got wrong is visible in the shape - the fore funnel sits between the
legs of the tripod mast, so the spotting top filled with smoke.
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
TEAK = (0.44, 0.36, 0.24)
GLASS = (0.18, 0.26, 0.30)

# 160.6 long, 25.0 beam, 8.1 draught.
BOW = 80.3
STERN = -80.3
BEAM_HALF = 12.50
KEEL = -8.10
DECK = 6.40
DECK_BOW = 9.40
DECK_STERN = 5.20


def deck_line(x):
    t = x / BOW
    if t >= 0:
        return DECK + (DECK_BOW - DECK) * t ** 2.6
    return DECK + (DECK_STERN - DECK) * (-t) ** 1.6


def beam_at(x):
    t = x / BOW
    if t > 0.78:
        return BEAM_HALF * max(0.05, 1.0 - ((t - 0.78) / 0.22) ** 1.3 * 0.96)
    if t < -0.80:
        return BEAM_HALF * max(0.22, 1.0 - ((-t - 0.80) / 0.20) ** 1.2 * 0.82)
    return BEAM_HALF * (1.0 - 0.09 * t * t)


def hull(paint, dark, metal):
    parts = []
    sections = []
    xs = [STERN, -76.0, -68.0, -56.0, -40.0, -20.0, 0.0, 20.0, 40.0, 56.0,
          66.0, 72.0, 77.0, BOW]
    for x in xs:
        t = abs(x / BOW)
        flare = 0.18 * max(0.0, x / BOW) ** 1.5
        fullness = 0.95 - 0.32 * max(0.0, t - 0.52) / 0.48
        sections.append((x, wf.hull_section(beam_at(x), deck_line(x), KEEL, flare=flare,
                                            fullness=fullness, camber=0.36,
                                            marks=(-0.8, 0.5))))
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.20, 3, smooth_angle=50))
    # Ram bow below the waterline - the last generation to carry one.
    ram = [
        (66.0, wf.oval(1.8, 1.8, 12, centre_z=-6.0)),
        (74.0, wf.oval(2.2, 2.2, 12, centre_z=-5.6)),
        (81.0, wf.oval(1.4, 1.4, 12, centre_z=-5.2)),
        (83.5, wf.oval(0.3, 0.3, 12, centre_z=-5.0)),
    ]
    parts.append(wf.finish(wf.loft("ram", ram, paint), 0.14, 3, smooth_angle=52))
    for sign, dy in ((-1, 5.6), (1, 5.6), (-1, 2.2), (1, 2.2)):
        y = sign * dy
        parts.append(wf.finish(
            wf.cylinder("shaft", (-64.0, y, KEEL + 2.0), 0.70, 16.0, "X", 12, metal), 0.08, 2))
        parts.append(wf.finish(
            wf.cylinder("screw", (-72.6, y, KEEL + 2.0), 2.20, 0.55, "X", 16, metal), 0.06, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-76.0, 0, KEEL + 3.0), (3.4, 0.60, 5.0), metal), 0.10, 2))
    return wf.join("hull", parts)


def deck(trim, metal, dark):
    """Teak planking over the armoured deck, and the guard rails round the edge."""
    parts = []
    sections = []
    for x in (STERN + 1.5, -60.0, -30.0, 0.0, 30.0, 55.0, 70.0, BOW - 1.0):
        half = beam_at(x) * 0.985
        z = deck_line(x)
        crown = 0.36 * (1 - (x / BOW) ** 2)
        sections.append((x, [(-half, z - 0.50), (half, z - 0.50),
                             (half, z + crown), (-half, z + crown)]))
    parts.append(wf.finish(wf.loft("deck", sections, trim), 0.10, 2, smooth_angle=44))
    parts.append(wf.finish(
        wf.rotate(wf.box("breakwater", (52.0, 0, deck_line(52.0) + 1.10),
                         (0.40, beam_at(52.0) * 1.4, 2.20), trim), y=16), 0.08, 2))
    for x in range(-76, 80, 6):
        half = beam_at(x) * 0.99
        z = deck_line(x)
        for sign in (-1, 1):
            parts.append(wf.finish(
                wf.box("stanchion", (float(x), sign * half, z + 0.80), (0.16, 0.16, 1.40), dark),
                0.03, 1))
    return wf.join("deck", parts)


def twin_turret(name, x, y, base_z, paint, metal, facing=1.0):
    """One twin 12-inch mounting: a low drum with a sloped face."""
    parts = []
    house = [
        (x - facing * 4.6, [(y - 4.0, base_z), (y + 4.0, base_z),
                            (y + 3.4, base_z + 3.0), (y - 3.4, base_z + 3.0)]),
        (x - facing * 1.0, [(y - 4.6, base_z), (y + 4.6, base_z),
                            (y + 4.0, base_z + 3.2), (y - 4.0, base_z + 3.2)]),
        (x + facing * 3.0, [(y - 4.6, base_z), (y + 4.6, base_z),
                            (y + 3.8, base_z + 3.0), (y - 3.8, base_z + 3.0)]),
        (x + facing * 4.4, [(y - 3.8, base_z + 0.5), (y + 3.8, base_z + 0.5),
                            (y + 3.2, base_z + 2.6), (y - 3.2, base_z + 2.6)]),
    ]
    parts.append(wf.finish(wf.loft(name, house, paint), 0.36, 4, smooth_angle=46))
    parts.append(wf.finish(
        wf.cylinder("barbette", (x, y, base_z - 1.2), 4.4, 2.6, "Z", 22, paint), 0.16, 2))
    for dy in (-1.5, 1.5):
        parts.append(wf.finish(
            wf.cylinder("sleeve", (x + facing * 5.6, y + dy, base_z + 1.8), 0.60, 2.6,
                        "X", 16, paint), 0.08, 2))
        parts.append(wf.finish(
            wf.cone("barrel", (x + facing * 11.6, y + dy, base_z + 1.8), 0.42, 0.32, 10.0,
                    "X", 16, metal), 0.03, 2))
    return parts


def turret_a(paint, metal):
    return wf.join("turret_a", twin_turret("turret_a", 53.4, 0.0, deck_line(53.4) + 0.5,
                                           paint, metal))


def turret_b(paint, metal):
    """The two after mountings, X superfiring over Y."""
    parts = twin_turret("turret_b", -40.0, 0.0, deck_line(-40.0) + 0.5, paint, metal,
                        facing=-1.0)
    base = deck_line(-58.0)
    parts.append(wf.finish(
        wf.box("bardeck", (-58.0, 0, base + 1.6), (18.0, 17.0, 3.2), paint), 0.30, 3))
    parts += twin_turret("turret_b", -58.0, 0.0, base + 3.2, paint, metal, facing=-1.0)
    return wf.join("turret_b", parts)


def turret_p(paint, metal):
    """
    The wing turrets, P and Q, one each beam amidships - which meant half the main
    armament could never fire on the same side, and is why nobody repeated the layout.
    """
    parts = []
    for sign in (-1, 1):
        parts += twin_turret("turret_p", 8.0, sign * 7.4, deck_line(8.0) + 0.5,
                             paint, metal, facing=1.0)
    return wf.join("turret_p", parts)


def bridge(paint, glass, metal):
    parts = []
    base = deck_line(28.0)
    parts.append(wf.finish(
        wf.box("shelterdeck", (26.0, 0, base + 1.8), (30.0, 16.0, 3.6), paint), 0.24, 3))
    parts.append(wf.finish(
        wf.cylinder("conningtower", (32.0, 0, base + 5.6), 3.4, 4.0, "Z", 20, paint),
        0.24, 3))
    parts.append(wf.finish(
        wf.box("bridgedeck", (31.0, 0, base + 8.0), (11.0, 12.0, 1.0), paint), 0.20, 3))
    parts.append(wf.finish(
        wf.box("chartroom", (28.0, 0, base + 9.4), (7.0, 8.0, 2.4), paint), 0.24, 3))
    for i in range(5):
        parts.append(wf.finish(
            wf.box("window", (31.6, -2.6 + i * 1.3, base + 9.8), (0.30, 0.90, 0.80), glass),
            0.04, 2))
    return wf.join("bridge", parts)


def funnel(name, x, paint, dark, metal, height):
    base = deck_line(x) + 5.4
    sections = []
    for t in (0.0, 0.5, 1.0):
        sections.append((x, wf.oval(4.2 - 0.7 * t, 2.8 - 0.5 * t, 16,
                                    centre_z=base + height * t)))
    # Rings at each station at the same x makes a vertical funnel; nudge for the rake.
    sections = [(x - i * 0.6, pts) for i, (sx, pts) in enumerate(sections)]
    parts = [wf.finish(wf.loft(name, sections, paint), 0.16, 3, smooth_angle=48)]
    top = base + height
    parts.append(wf.finish(
        wf.cylinder("cap", (x - 1.2, 0, top + 0.1), 3.6, 0.5, "Z", 18, dark), 0.10, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("steampipe", (x - 0.6, sign * 2.6, base + height * 0.6), 0.36,
                        height * 1.1, "Z", 10, metal), 0.04, 1))
    return wf.join(name, parts)


def mast(name, x, metal, dark, height, tripod, top_z):
    parts = []
    base = deck_line(x) + top_z
    if tripod:
        for dx, dy in ((2.4, 0.0), (-1.6, 2.6), (-1.6, -2.6)):
            parts.append(wf.finish(
                wf.rotate(wf.cone("leg", (x + dx * 0.5, dy * 0.5, base + height * 0.5),
                                  0.38, 0.22, height, "Z", 10, metal),
                          x=math.degrees(math.atan2(dy, height)),
                          y=-math.degrees(math.atan2(dx, height))), 0.03, 2))
    else:
        parts.append(wf.finish(
            wf.cone("pole", (x, 0, base + height * 0.5), 0.42, 0.20, height, "Z", 10, metal),
            0.03, 2))
    parts.append(wf.finish(
        wf.cylinder("top", (x, 0, base + height), 1.9, 1.8, "Z", 16, metal), 0.10, 2))
    parts.append(wf.finish(
        wf.cone("topmast", (x, 0, base + height + 5.0), 0.18, 0.07, 8.0, "Z", 8, metal),
        0.02, 1))
    for z, half in ((base + height * 0.72, 7.0), (base + height + 3.0, 4.0)):
        parts.append(wf.finish(
            wf.cylinder("yard", (x, 0, z), 0.16, half * 2, "Y", 8, metal), 0.03, 1))
    return wf.join(name, parts)


def boats(trim, metal, paint):
    """Ship's boats on their skids amidships, under the derricks."""
    parts = []
    for i, (x, y, length) in enumerate(((0.0, -8.4, 11.0), (0.0, 8.4, 11.0),
                                        (-12.0, -8.0, 8.0), (-12.0, 8.0, 8.0))):
        base = deck_line(x) + 4.6
        body = [
            (x + length * 0.5, wf.oval(0.20, 0.30, 10, centre_z=base + 0.5)),
            (x + length * 0.25, wf.oval(1.10, 0.80, 10, centre_z=base + 0.4)),
            (x - length * 0.25, wf.oval(1.10, 0.80, 10, centre_z=base + 0.4)),
            (x - length * 0.5, wf.oval(0.40, 0.45, 10, centre_z=base + 0.5)),
        ]
        parts.append(wf.finish(
            wf.loft("boat", [(sx, [(yy + y, zz) for yy, zz in pts]) for sx, pts in body], trim),
            0.10, 3, smooth_angle=48))
        for dx in (-length * 0.3, length * 0.3):
            parts.append(wf.finish(
                wf.box("skid", (x + dx, y, base - 0.4), (0.5, 3.0, 0.8), metal), 0.06, 1))
    # Derricks either side of the funnels.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.rotate(wf.cone("derrick", (-4.0, sign * 3.0, deck_line(-4.0) + 10.0),
                              0.30, 0.14, 16.0, "Z", 8, metal), x=sign * 34), 0.03, 1))
    return wf.join("boats", parts)


def casemates(paint, metal, dark):
    """12-pounder anti-torpedo-boat guns, in the superstructure and on the turret roofs."""
    parts = []
    for x in (44.0, 36.0, 20.0, -20.0, -30.0, -48.0):
        for sign in (-1, 1):
            y = sign * beam_at(x) * 0.88
            z = deck_line(x) + 1.4
            parts.append(wf.finish(
                wf.cylinder("shield", (x, y, z), 0.90, 1.20, "Z", 14, paint), 0.08, 2))
            parts.append(wf.finish(
                wf.rotate(wf.cone("12pdr", (x + sign * 0.2, y + sign * 1.4, z + 0.5),
                                  0.14, 0.10, 2.8, "Y", 10, metal), z=sign * 10), 0.02, 1))
    return wf.join("casemate", parts)


def anchors(metal, dark, paint):
    parts = []
    for sign in (-1, 1):
        y = sign * beam_at(70.0) * 0.92
        parts.append(wf.finish(
            wf.box("anchor", (70.0, y, deck_line(70.0) - 3.2), (2.6, 0.40, 2.0), dark),
            0.10, 2))
        parts.append(wf.finish(
            wf.cylinder("hawse", (72.0, y * 0.8, deck_line(72.0) - 1.4), 0.5, 1.2, "X", 12,
                        metal), 0.06, 2))
        parts.append(wf.finish(
            wf.cylinder("capstan", (62.0, sign * 4.0, deck_line(62.0) + 0.7), 1.1, 1.4,
                        "Z", 14, metal), 0.08, 2))
    return wf.join("anchor", parts)


PALETTE_BODY = (0x6E / 255, 0x7A / 255, 0x80 / 255)
GREY_LIGHT = (1.12, 1.12, 1.13)
GREY_DARK = (0.84, 0.85, 0.88)
BOOT = (0.30, 0.30, 0.32)
HULL_RED = (1.32, 0.54, 0.42)

PARTS = ("hull", "deck", "turret_a", "turret_b", "turret_p", "bridge", "funnel_f",
         "funnel_a", "mast_f", "mast_a", "boats", "casemate", "anchor")


def weathering():
    for name in ("hull", "deck", "turret_a", "turret_b", "turret_p", "bridge",
                 "funnel_f", "funnel_a", "boats", "casemate"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=2.20)
        wf.weather(obj, camo=(GREY_LIGHT, GREY_DARK), cover=(0.24, 0.24), dirt=0.30,
                   wear=0.26, scale=11.0, seed=107, ground=KEEL, top=20.0)
    for name in ("mast_f", "mast_a", "anchor"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.10, wear=0.32, seed=109, ground=KEEL, top=44.0)


def waterline():
    obj = bpy.data.objects.get("hull")
    if obj is None:
        return
    mesh = obj.data
    col = mesh.color_attributes.get("Col")
    if col is None:
        return
    bpy.context.view_layer.update()
    to_world = obj.matrix_world
    for i, vert in enumerate(mesh.vertices):
        z = (to_world @ vert.co).z
        tint = HULL_RED if z < -0.8 else (BOOT if z < 0.5 else None)
        if tint is None:
            continue
        r, g, b = (c / wf.TINT_RANGE for c in tint)
        col.data[i].color = (r, g, b, 1.0)


def teak():
    obj = bpy.data.objects.get("deck")
    if obj is None:
        return
    col = obj.data.color_attributes.get("Col")
    if col is None:
        return
    tint = wf.tint_for(TEAK, PALETTE_BODY)
    bpy.context.view_layer.update()
    to_world = obj.matrix_world
    for i, vert in enumerate(obj.data.vertices):
        if (to_world.to_3x3() @ vert.normal).z > 0.5:
            r, g, b = (min(1.0, c / wf.TINT_RANGE) for c in tint)
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
    turret_p(paint, metal)
    bridge(paint, glass, metal)
    funnel("funnel_f", 14.0, paint, dark, metal, height=13.0)
    funnel("funnel_a", -4.0, paint, dark, metal, height=13.0)
    mast("mast_f", 20.0, metal, dark, height=22.0, tripod=True, top_z=8.0)
    mast("mast_a", -26.0, metal, dark, height=18.0, tripod=False, top_z=5.0)
    boats(trim, metal, paint)
    casemates(paint, metal, dark)
    anchors(metal, dark, paint)

    weathering()
    waterline()
    teak()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(88.0)
        wf.camera(azimuth=52, elevation=18)
        wf.render(args[0], samples=18)
    if len(args) > 1:
        wf.export_glb(args[1])
