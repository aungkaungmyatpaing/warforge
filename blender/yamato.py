"""
Yamato, at full size.

263 metres, 72,000 tonnes and nine 46 cm guns - the largest battleship ever built, and
built around a silhouette nobody else had: one enormous raked funnel, a pagoda mast
stacked seven levels high, and a hull so beamy it could not use the Panama Canal, which
was rather the point.

Object names are the part ids the game drags around.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


KURE_GREY = (0.26, 0.29, 0.32)
STEEL = (0.40, 0.42, 0.44)
DARK = (0.10, 0.11, 0.12)
DECK_TEAK = (0.42, 0.34, 0.22)
BRASS = (0.62, 0.50, 0.20)
GLASS = (0.18, 0.26, 0.30)

# ---------------------------------------------------------------------------
# Dimensions, in metres. 263 long, 38.9 beam, 10.4 draught.
# x = 0 amidships, +x toward the bow. z = 0 at the waterline.
# ---------------------------------------------------------------------------
BOW = 131.5
STERN = -131.5
BEAM_HALF = 19.45
KEEL = -10.40
DECK = 9.80
DECK_BOW = 14.20
DECK_STERN = 6.60


def deck_line(x):
    t = x / BOW
    if t >= 0:
        return DECK + (DECK_BOW - DECK) * t ** 2.4
    return DECK + (DECK_STERN - DECK) * (-t) ** 1.5


def beam_at(x):
    """Very full amidships - the widest battleship hull ever floated - fining hard aft."""
    t = x / BOW
    if t > 0.80:
        return BEAM_HALF * max(0.05, 1.0 - ((t - 0.80) / 0.20) ** 1.4 * 0.96)
    if t < -0.72:
        return BEAM_HALF * max(0.30, 1.0 - ((-t - 0.72) / 0.28) ** 1.3 * 0.74)
    return BEAM_HALF * (1.0 - 0.10 * t * t)


def hull(paint, dark, metal):
    """Lofted hull, with the bulbous forefoot and the semi-transom stern."""
    parts = []
    sections = []
    xs = [STERN, -126.0, -116.0, -100.0, -80.0, -55.0, -25.0, 0.0, 25.0, 55.0,
          80.0, 100.0, 114.0, 124.0, 129.0, BOW]
    for x in xs:
        t = abs(x / BOW)
        half = beam_at(x)
        deck = deck_line(x)
        keel = KEEL
        if x > 100.0:
            keel = KEEL * (1.0 - 0.35 * (x - 100.0) / (BOW - 100.0))
        flare = 0.20 * max(0.0, x / BOW) ** 1.6
        fullness = 0.97 - 0.34 * max(0.0, t - 0.50) / 0.50
        sections.append((x, wf.hull_section(half, deck, keel, flare=flare,
                                            fullness=fullness, camber=0.55,
                                            marks=(-1.1, 0.7))))
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.24, 3, smooth_angle=50))

    # The bulbous bow, which Yamato carried below the waterline and which is invisible in
    # every photograph of her afloat.
    bulb = [
        (108.0, wf.oval(2.4, 2.4, 14, centre_z=-7.2)),
        (118.0, wf.oval(3.4, 3.4, 14, centre_z=-7.0)),
        (127.0, wf.oval(3.2, 3.2, 14, centre_z=-6.8)),
        (134.0, wf.oval(1.6, 1.6, 14, centre_z=-6.6)),
        (136.5, wf.oval(0.3, 0.3, 14, centre_z=-6.6)),
    ]
    parts.append(wf.finish(wf.loft("bulb", bulb, paint), 0.16, 3, smooth_angle=52))

    # Shafts, screws and the twin rudders.
    for sign, dy in ((-1, 8.2), (1, 8.2), (-1, 3.4), (1, 3.4)):
        y = sign * dy
        parts.append(wf.finish(
            wf.cylinder("shaft", (-108.0, y, KEEL + 2.6), 0.90, 22.0, "X", 12, metal),
            0.10, 2))
        parts.append(wf.finish(
            wf.cylinder("screw", (-119.5, y, KEEL + 2.6), 3.00, 0.80, "X", 16, metal),
            0.08, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-124.0, 0, KEEL + 4.0), (5.0, 0.80, 7.0), metal), 0.14, 2))
    return wf.join("hull", parts)


def deck(trim, metal, dark):
    """Teak-planked weather deck, with the anchor gear forward."""
    parts = []
    xs = [STERN + 2.0, -100.0, -60.0, -20.0, 20.0, 60.0, 95.0, 115.0, 126.0, BOW - 1.5]
    sections = []
    for x in xs:
        half = beam_at(x) * 0.985
        z = deck_line(x)
        crown = 0.55 * (1 - (x / BOW) ** 2)
        sections.append((x, [(-half, z - 0.60), (half, z - 0.60),
                             (half, z + crown), (-half, z + crown)]))
    parts.append(wf.finish(wf.loft("deck", sections, trim), 0.12, 2, smooth_angle=44))

    # Breakwater, anchor capstans and the hawse pipes.
    parts.append(wf.finish(
        wf.rotate(wf.box("breakwater", (100.0, 0, deck_line(100.0) + 1.40),
                         (0.60, beam_at(100.0) * 1.5, 2.80), trim), y=16), 0.10, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("capstan", (116.0, sign * 5.0, deck_line(116.0) + 0.90), 1.30, 1.60,
                        "Z", 14, metal), 0.10, 2))
        parts.append(wf.finish(
            wf.cylinder("anchor", (124.0, sign * beam_at(124.0) * 0.9,
                                   deck_line(124.0) - 3.0), 1.60, 0.60, "Y", 12, dark),
            0.10, 2))
    # Guard rails along the deck edge.
    for x in range(-120, 125, 8):
        half = beam_at(x) * 0.99
        z = deck_line(x)
        for sign in (-1, 1):
            parts.append(wf.finish(
                wf.box("stanchion", (float(x), sign * half, z + 0.90), (0.22, 0.22, 1.60), dark),
                0.04, 1))
    return wf.join("deck", parts)


def main_turret(name, x, base_z, paint, metal, facing=1.0):
    """
    One triple 46 cm turret: 2,700 tonnes apiece, heavier than a whole destroyer.
    """
    parts = []
    face = facing
    house = [
        (x - face * 9.0, [(-8.2, base_z), (8.2, base_z), (7.2, base_z + 5.6), (-7.2, base_z + 5.6)]),
        (x - face * 3.0, [(-9.6, base_z), (9.6, base_z), (8.4, base_z + 6.2), (-8.4, base_z + 6.2)]),
        (x + face * 4.0, [(-9.6, base_z), (9.6, base_z), (8.2, base_z + 6.2), (-8.2, base_z + 6.2)]),
        (x + face * 8.4, [(-8.6, base_z + 0.6), (8.6, base_z + 0.6),
                          (7.4, base_z + 4.8), (-7.4, base_z + 4.8)]),
    ]
    parts.append(wf.finish(wf.loft(name, house, paint), 0.50, 4, smooth_angle=46))
    parts.append(wf.finish(
        wf.cylinder("barbette", (x, 0, base_z - 1.60), 8.8, 3.40, "Z", 24, paint), 0.20, 2))
    # Three 46 cm barrels, 21 m of gun each.
    for dy in (-5.3, 0.0, 5.3):
        parts.append(wf.finish(
            wf.cylinder("sleeve", (x + face * 11.0, dy, base_z + 3.20), 1.35, 6.0, "X", 16, paint),
            0.14, 2))
        parts.append(wf.finish(
            wf.cone("barrel", (x + face * 23.0, dy, base_z + 3.20), 0.86, 0.62, 20.0,
                    "X", 16, metal), 0.06, 2))
    return parts


def turret_a(paint, metal):
    return wf.join("turret_a", main_turret("turret_a", 83.2, deck_line(83.2) + 0.60,
                                           paint, metal))


def turret_b(paint, metal):
    """No.2, superfiring over No.1."""
    base = deck_line(58.1)
    parts = [wf.finish(
        wf.box("bbarbette", (58.1, 0, base + 3.20), (34.0, 26.0, 6.40), paint), 0.60, 3)]
    parts += main_turret("turret_b", 58.1, base + 6.40, paint, metal)
    return wf.join("turret_b", parts)


def turret_c(paint, metal):
    """No.3, trained aft."""
    return wf.join("turret_c", main_turret("turret_c", -89.1, deck_line(-89.1) + 0.60,
                                           paint, metal, facing=-1.0))


def pagoda(paint, glass, metal, dark):
    """
    The pagoda mast: seven levels of bridge, fire control and lookout stacked upward,
    each smaller than the one below. Nothing else afloat looked like it.
    """
    parts = []
    base = deck_line(-7.5)
    levels = [
        (-7.5, 40.0, 24.0, 6.0),
        (-6.0, 28.0, 18.0, 5.0),
        (-4.5, 20.0, 13.0, 4.4),
        (-3.5, 14.0, 10.0, 4.0),
        (-3.0, 10.0, 8.0, 3.6),
        (-2.5, 7.6, 6.4, 3.2),
    ]
    z = base
    for x, length, width, height in levels:
        parts.append(wf.finish(
            wf.box("level", (x, 0, z + height * 0.5), (length, width, height), paint),
            0.40, 3))
        # Bridge windows on the two biggest levels.
        if length > 18:
            for i in range(7):
                parts.append(wf.finish(
                    wf.box("window", (x + length * 0.5 - 0.2, -5.0 + i * 1.7, z + height * 0.62),
                           (0.40, 1.20, 1.10), glass), 0.08, 2))
        z += height
    # Fire-control tower and the masthead above it.
    parts.append(wf.finish(
        wf.cylinder("tower", (-2.2, 0, z + 3.2), 2.6, 6.4, "Z", 18, paint), 0.24, 3))
    parts.append(wf.finish(
        wf.cone("masthead", (-2.2, 0, z + 12.0), 0.60, 0.22, 12.0, "Z", 10, metal), 0.06, 2))
    for zz, half in ((z + 11.0, 9.0), (z + 15.0, 6.0)):
        parts.append(wf.finish(
            wf.cylinder("yard", (-2.2, 0, zz), 0.28, half * 2, "Y", 8, metal), 0.05, 1))
    return wf.join("pagoda", parts)


def funnel(paint, dark, metal):
    """One huge funnel, raked 26 degrees, with the honeycomb cap she was famous for."""
    x = -33.1
    base = deck_line(x) + 9.0
    height = 22.0
    rake = math.radians(26)
    sections = []
    for t in (0.0, 0.5, 1.0):
        z = base + height * t
        shift = height * t * math.tan(rake)
        sections.append((x - shift, wf.oval(8.2 - 1.4 * t, 5.4 - 0.9 * t, 18, centre_z=z)))
    parts = [wf.finish(wf.loft("funnel", sections, paint), 0.30, 3, smooth_angle=48)]
    top = base + height
    tipx = x - height * math.tan(rake)
    parts.append(wf.finish(
        wf.cylinder("cap", (tipx, 0, top + 0.2), 7.0, 0.9, "Z", 20, dark), 0.14, 2))
    # The honeycomb grille across the mouth.
    for i in range(5):
        parts.append(wf.finish(
            wf.box("grille", (tipx - 4.4 + i * 2.2, 0, top + 0.6), (0.5, 8.4, 0.5), dark),
            0.06, 1))
    # Steam pipes up the after side.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.rotate(wf.cylinder("steampipe", (x - height * 0.5 * math.tan(rake), sign * 4.4,
                                                base + height * 0.55), 0.65, height * 1.15,
                                  "Z", 10, metal), y=math.degrees(rake)), 0.06, 1))
    return wf.join("funnel", parts)


def rangefinder(paint, metal, glass):
    """The 15-metre main rangefinder on top of the pagoda - the biggest ever fitted."""
    base = deck_line(-7.5) + 26.2
    parts = [
        wf.finish(wf.box("rangefinder", (-2.2, 0, base + 1.4), (5.0, 15.2, 2.8), paint),
                  0.50, 4),
        wf.finish(wf.cylinder("hood", (-2.2, 0, base + 3.2), 2.2, 4.4, "X", 16, paint),
                  0.20, 3),
    ]
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("lens", (-2.2, sign * 7.6, base + 1.4), 0.80, 0.40, "Y", 14, glass),
            0.06, 2))
    return wf.join("rangefinder", parts)


def secondary(paint, metal):
    """15.5 cm triple turrets - the ones taken off Mogami when she was rebuilt."""
    parts = []
    for x, y, face in ((25.3, 0.0, 1.0), (-52.0, 0.0, -1.0)):
        base = deck_line(x) + 7.2
        house = [
            (x - face * 4.4, [(-3.6, base), (3.6, base), (3.1, base + 3.0), (-3.1, base + 3.0)]),
            (x + face * 1.0, [(-4.2, base), (4.2, base), (3.6, base + 3.3), (-3.6, base + 3.3)]),
            (x + face * 4.2, [(-3.8, base + 0.3), (3.8, base + 0.3),
                              (3.2, base + 2.9), (-3.2, base + 2.9)]),
        ]
        parts.append(wf.finish(wf.loft("secondary", house, paint), 0.30, 4, smooth_angle=46))
        parts.append(wf.finish(
            wf.cylinder("barbette", (x, y, base - 1.2), 3.9, 2.6, "Z", 18, paint), 0.14, 2))
        for dy in (-2.0, 0.0, 2.0):
            parts.append(wf.finish(
                wf.cone("barrel", (x + face * 9.0, dy, base + 1.9), 0.30, 0.22, 9.0,
                        "X", 12, metal), 0.03, 2))
    return wf.join("secondary", parts)


def aa(paint, metal, dark):
    """25 mm triple mounts in their tubs, and the 12.7 cm high-angle guns."""
    parts = []
    for x in (-18.0, -45.0, -62.0, 8.0):
        for sign in (-1, 1):
            y = sign * beam_at(x) * 0.62
            z = deck_line(x) + 7.4
            parts.append(wf.finish(
                wf.cylinder("tub", (x, y, z - 1.0), 3.0, 2.0, "Z", 16, paint), 0.14, 2))
            parts.append(wf.finish(
                wf.box("mount", (x, y, z + 0.7), (2.2, 2.6, 1.4), metal), 0.16, 3))
            for dy in (-0.7, 0.0, 0.7):
                parts.append(wf.finish(
                    wf.rotate(wf.cone("aabarrel", (x + 2.0, y + dy, z + 1.6), 0.13, 0.09, 3.4,
                                      "X", 8, metal), y=-26), 0.03, 1))
    # 12.7 cm HA mounts either side of the funnel.
    for x in (-24.0, -42.0):
        for sign in (-1, 1):
            y = sign * 13.0
            z = deck_line(x) + 8.6
            parts.append(wf.finish(
                wf.box("hamount", (x, y, z), (5.0, 5.4, 3.0), paint), 0.30, 3))
            for dy in (-1.2, 1.2):
                parts.append(wf.finish(
                    wf.rotate(wf.cone("habarrel", (x + 4.4, y + dy, z + 0.9), 0.22, 0.16, 6.0,
                                      "X", 10, metal), y=-20), 0.04, 1))
    return wf.join("aa", parts)


def catapult(metal, trim, paint):
    """Two catapults and the aircraft handling deck right aft."""
    parts = []
    for sign in (-1, 1):
        y = sign * 9.0
        x = -110.0
        z = deck_line(x) + 2.2
        parts.append(wf.finish(
            wf.box("catapult", (x, y, z), (24.0, 2.4, 0.9), metal), 0.10, 2))
        for i in range(5):
            parts.append(wf.finish(
                wf.box("trestle", (x - 10.0 + i * 5.0, y, z - 1.1), (1.0, 2.0, 1.4), metal),
                0.06, 1))
    # Crane at the stern quarter.
    parts.append(wf.finish(
        wf.cylinder("cranepost", (-122.0, 0, deck_line(-122.0) + 4.0), 0.80, 8.0, "Z", 12, metal),
        0.10, 2))
    parts.append(wf.finish(
        wf.rotate(wf.cone("cranejib", (-114.0, 0, deck_line(-114.0) + 9.0), 0.60, 0.26, 18.0,
                          "X", 10, metal), y=-24), 0.06, 2))
    # Aircraft deck plating aft.
    parts.append(wf.finish(
        wf.box("airdeck", (-118.0, 0, deck_line(-118.0) + 0.9), (26.0, 22.0, 0.5), trim),
        0.10, 2))
    return wf.join("catapult", parts)


def crest(accent, metal):
    """The gilded chrysanthemum on the stem - the mark of an Imperial Navy capital ship."""
    parts = []
    x = 126.7
    z = deck_line(x) - 2.2
    parts.append(wf.finish(
        wf.cylinder("chrys", (x, 0, z), 2.4, 0.5, "X", 24, accent), 0.10, 3))
    for i in range(16):
        a = 2 * math.pi * i / 16
        parts.append(wf.finish(
            wf.rotate(wf.box("petal", (x + 0.3, 1.9 * math.cos(a), z + 1.9 * math.sin(a)),
                             (0.4, 0.7, 1.5), accent), x=math.degrees(a)), 0.10, 2))
    return wf.join("chrys", parts)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

# Kure Navy Yard grey, with a teak deck and anti-fouling red below the waterline. The
# teak is the thing people notice: it was linoleum and steel on most navies by 1941, and
# Yamato kept a planked deck to the end.
PALETTE_BODY = (0x69 / 255, 0x73 / 255, 0x7A / 255)
GREY_LIGHT = (1.12, 1.12, 1.13)
GREY_DARK = (0.84, 0.85, 0.88)
BOOT_TOPPING = (0.26, 0.26, 0.28)
HULL_RED = (1.34, 0.54, 0.42)

PARTS = ("hull", "deck", "turret_a", "turret_b", "turret_c", "pagoda", "funnel",
         "rangefinder", "secondary", "aa", "catapult", "chrys")


def weathering():
    for name in ("hull", "deck", "turret_a", "turret_b", "turret_c", "pagoda",
                 "funnel", "secondary", "aa", "catapult"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=3.00)
        wf.weather(obj, camo=(GREY_LIGHT, GREY_DARK), cover=(0.24, 0.24),
                   dirt=0.26, wear=0.24, scale=16.0, seed=23, ground=KEEL, top=26.0)
    for name in ("rangefinder", "chrys"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.06, wear=0.28, seed=29, ground=KEEL, top=60.0)


def waterline():
    """Anti-fouling red below, boot topping at the line."""
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
        if z < -1.1:
            tint = HULL_RED
        elif z < 0.7:
            tint = BOOT_TOPPING
        else:
            continue
        r, g, b = (c / wf.TINT_RANGE for c in tint)
        col.data[i].color = (r, g, b, 1.0)


def teak():
    """The planked deck, which is the one warm colour anywhere on the ship."""
    obj = bpy.data.objects.get("deck")
    if obj is None:
        return
    mesh = obj.data
    col = mesh.color_attributes.get("Col")
    if col is None:
        return
    tint = wf.tint_for(DECK_TEAK, PALETTE_BODY)
    bpy.context.view_layer.update()
    to_world = obj.matrix_world
    for i, vert in enumerate(mesh.vertices):
        facing = (to_world.to_3x3() @ vert.normal).z
        if facing > 0.5:
            r, g, b = (min(1.0, c / wf.TINT_RANGE) for c in tint)
            col.data[i].color = (r, g, b, 1.0)


def build():
    wf.reset()
    paint = wf.role("BODY", KURE_GREY, roughness=0.72)
    metal = wf.role("METAL", STEEL, roughness=0.46, metallic=0.70)
    dark = wf.role("DARK", DARK, roughness=0.74, metallic=0.30)
    trim = wf.role("TRIM", KURE_GREY, roughness=0.78)
    glass = wf.role("GLASS", GLASS, roughness=0.16, metallic=0.30)
    accent = wf.role("ACCENT", BRASS, roughness=0.34, metallic=0.85)

    hull(paint, dark, metal)
    deck(trim, metal, dark)
    turret_a(paint, metal)
    turret_b(paint, metal)
    turret_c(paint, metal)
    pagoda(paint, glass, metal, dark)
    funnel(paint, dark, metal)
    rangefinder(paint, metal, glass)
    secondary(paint, metal)
    aa(paint, metal, dark)
    catapult(metal, trim, paint)
    crest(accent, metal)

    weathering()
    waterline()
    teak()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(140.0)
        wf.camera(azimuth=52, elevation=18)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
