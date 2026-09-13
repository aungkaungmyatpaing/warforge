"""
Fletcher-class destroyer, at full size.

A flush-decked hull with no break in the sheer - which is what the class is named for in
every recognition guide, and what let it carry five 5-inch mounts and ten torpedo tubes
on 114 metres without breaking its back.

Object names are the part ids the game drags around.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


HAZE = (0.28, 0.32, 0.37)
STEEL = (0.42, 0.44, 0.46)
DARK = (0.10, 0.11, 0.12)
GLASS = (0.20, 0.28, 0.32)

# ---------------------------------------------------------------------------
# Dimensions, in metres. 114.7 long, 12.0 beam, 5.3 draught.
# x = 0 amidships, +x toward the bow. z = 0 at the waterline.
# ---------------------------------------------------------------------------
BOW = 57.3
STERN = -57.3
BEAM_HALF = 6.0
KEEL = -5.30
DECK = 4.10              # freeboard amidships
DECK_BOW = 6.70
DECK_STERN = 3.50


def deck_line(x):
    """Sheer: the deck rises toward the bow and falls a little aft."""
    t = x / BOW
    if t >= 0:
        return DECK + (DECK_BOW - DECK) * t ** 2.2
    return DECK + (DECK_STERN - DECK) * (-t) ** 1.6


def beam_at(x):
    """Half-beam, full amidships and fining away to nothing at the stem."""
    t = x / BOW
    if t > 0.82:                                   # the fine entry forward
        return BEAM_HALF * max(0.04, 1.0 - ((t - 0.82) / 0.18) ** 1.5 * 0.98)
    if t < -0.86:                                  # transom stern
        return BEAM_HALF * 0.66
    return BEAM_HALF * (1.0 - 0.16 * t * t)


def hull(paint, dark, metal):
    """Lofted hull, with a bulbous forefoot and a transom stern."""
    parts = []
    sections = []
    xs = [STERN, -54.0, -48.0, -38.0, -26.0, -12.0, 0.0, 12.0, 24.0, 34.0,
          42.0, 48.0, 52.5, 55.5, BOW]
    for x in xs:
        t = abs(x / BOW)
        half = beam_at(x)
        deck = deck_line(x)
        keel = KEEL * (1.0 - 0.55 * max(0.0, t - 0.72) / 0.28) if x > 0 else KEEL
        # Flare grows toward the bow; the stern is nearly wall-sided.
        flare = 0.22 * max(0.0, x / BOW) ** 1.4
        fullness = 0.94 - 0.30 * max(0.0, t - 0.55) / 0.45
        sections.append((x, wf.hull_section(half, deck, keel, flare=flare,
                                            fullness=fullness, camber=0.22,
                                            marks=(-0.55, 0.35))))
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.10, 3, smooth_angle=50))

    # Boot topping: a darker band at the waterline, and the flat of the transom.
    parts.append(wf.finish(
        wf.box("transom", (STERN - 0.10, 0, (KEEL + DECK_STERN) * 0.5),
               (0.30, BEAM_HALF * 1.28, DECK_STERN - KEEL), paint), 0.08, 2))
    # Bilge keels.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.box("bilgekeel", (-4.0, sign * BEAM_HALF * 0.82, KEEL + 1.6),
                   (34.0, 0.16, 0.60), dark), 0.03, 1))
    # Screws and rudders.
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("shaft", (-49.0, sign * 2.2, KEEL + 1.0), 0.34, 7.0, "X", 12, metal),
            0.04, 2))
        parts.append(wf.finish(
            wf.cylinder("screw", (-52.6, sign * 2.2, KEEL + 1.0), 1.70, 0.30, "X", 16, metal),
            0.03, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-54.4, 0, KEEL + 1.9), (2.2, 0.30, 3.2), metal), 0.06, 2))
    return wf.join("hull", parts)


def deck(trim, metal, dark):
    """Weather deck, breakwater and the guard rails round the edge."""
    parts = []
    xs = [STERN + 1.0, -40.0, -20.0, 0.0, 20.0, 36.0, 46.0, 53.0, BOW - 0.6]
    sections = []
    for x in xs:
        half = beam_at(x) * 0.985
        z = deck_line(x)
        sections.append((x, [(-half, z - 0.26), (half, z - 0.26),
                             (half, z + 0.22 * (1 - (x / BOW) ** 2)),
                             (-half, z + 0.22 * (1 - (x / BOW) ** 2))]))
    parts.append(wf.finish(wf.loft("deck", sections, trim), 0.05, 2, smooth_angle=44))

    # Breakwater forward, to throw green water off the forecastle.
    parts.append(wf.finish(
        wf.rotate(wf.box("breakwater", (43.0, 0, deck_line(43.0) + 0.70),
                         (0.30, beam_at(43.0) * 1.5, 1.40), trim), y=18), 0.04, 2))
    # Guard rails: stanchions and three wires, all the way round.
    for x in range(-54, 56, 4):
        half = beam_at(x) * 0.99
        z = deck_line(x)
        for sign in (-1, 1):
            parts.append(wf.finish(
                wf.box("stanchion", (float(x), sign * half, z + 0.62), (0.09, 0.09, 1.10), dark),
                0.02, 1))
    for wire in (0.42, 0.76, 1.08):
        for sign in (-1, 1):
            for x0, x1 in ((-54.0, -20.0), (-20.0, 14.0), (14.0, 44.0)):
                mid = (x0 + x1) * 0.5
                parts.append(wf.finish(
                    wf.box("rail", (mid, sign * beam_at(mid) * 0.99,
                                    deck_line(mid) + wire + 0.10),
                           (x1 - x0, 0.05, 0.05), dark), 0.01, 1))
    return wf.join("deck", parts)


def mount(name, x, base_z, paint, metal, dark, facing=1.0):
    """
    One enclosed 5-inch/38 single mount: the gunhouse and its barrel.

    The 5"/38 was the best dual-purpose gun of the war and every Fletcher carried five,
    so getting the gunhouse shape right matters more here than on most ships.
    """
    parts = []
    house = [
        (x - 2.30, [(-1.90, base_z), (1.90, base_z), (1.70, base_z + 2.40), (-1.70, base_z + 2.40)]),
        (x - 1.20, [(-2.10, base_z), (2.10, base_z), (1.90, base_z + 2.60), (-1.90, base_z + 2.60)]),
        (x + 1.10, [(-2.10, base_z), (2.10, base_z), (1.86, base_z + 2.60), (-1.86, base_z + 2.60)]),
        (x + 2.30, [(-1.60, base_z), (1.60, base_z), (1.30, base_z + 2.30), (-1.30, base_z + 2.30)]),
    ]
    parts.append(wf.finish(wf.loft(name, house, paint), 0.16, 4, smooth_angle=48))
    # Barbette under it.
    parts.append(wf.finish(
        wf.cylinder("barbette", (x, 0, base_z - 0.50), 2.00, 1.10, "Z", 20, paint), 0.06, 2))
    # Barrel, and the sleeve where it leaves the gunhouse.
    tip = x + facing * 6.90
    parts.append(wf.finish(
        wf.cylinder("sleeve", (x + facing * 2.60, 0, base_z + 1.40), 0.40, 1.20, "X", 16, paint),
        0.04, 2))
    parts.append(wf.finish(
        wf.cone("barrel", (x + facing * 4.80, 0, base_z + 1.40), 0.20, 0.15, 4.40, "X", 14, metal),
        0.02, 2))
    return parts


def gun_a(paint, metal, dark):
    return wf.join("gun_a", mount("gun_a", 37.6, deck_line(37.6) + 0.20, paint, metal, dark))


def gun_b(paint, metal, dark):
    """No.2 mount, superfiring over No.1 from its own deckhouse."""
    base = deck_line(27.2)
    parts = [wf.finish(
        wf.box("shelter", (28.6, 0, base + 1.30), (13.0, 7.4, 2.60), paint), 0.16, 3)]
    parts += mount("gun_b", 27.2, base + 2.60, paint, metal, dark)
    return wf.join("gun_b", parts)


def gun_x(paint, metal, dark):
    """The two aft mounts, both trained astern."""
    parts = []
    base = deck_line(-41.4)
    parts.append(wf.finish(
        wf.box("aftshelter", (-37.0, 0, base + 1.20), (12.0, 7.0, 2.40), paint), 0.16, 3))
    parts += mount("gun_x", -41.4, base + 2.40, paint, metal, dark, facing=-1.0)
    parts += mount("gun_x", -31.0, base + 4.60, paint, metal, dark, facing=-1.0)
    return wf.join("gun_x", parts)


def bridge(paint, glass, metal):
    """Bridge structure: the pilot house, the open bridge above it and the director."""
    parts = []
    base = deck_line(4.9)
    parts.append(wf.finish(
        wf.box("deckhouse", (6.0, 0, base + 1.60), (26.0, 9.4, 3.20), paint), 0.20, 3))
    parts.append(wf.finish(
        wf.box("pilothouse", (12.0, 0, base + 4.60), (9.0, 7.6, 2.80), paint), 0.24, 4))
    for i in range(5):
        parts.append(wf.finish(
            wf.box("window", (16.2, -2.6 + i * 1.3, base + 5.20), (0.30, 0.90, 0.80), glass),
            0.04, 2))
    # Open bridge and its splinter shield.
    parts.append(wf.finish(
        wf.box("openbridge", (12.4, 0, base + 6.70), (7.0, 6.4, 1.40), paint), 0.24, 4))
    # Mk 37 director on top - the thing that made the 5-inch mounts worth having.
    parts.append(wf.finish(
        wf.box("director", (9.4, 0, base + 8.40), (3.6, 3.4, 2.10), paint), 0.30, 4))
    parts.append(wf.finish(
        wf.cylinder("radar_mk12", (9.4, 0, base + 9.80), 1.50, 0.24, "Z", 18, metal), 0.04, 2))
    return wf.join("bridge", parts)


def funnel(name, x, paint, dark, metal, height):
    """A raked, oval funnel with its steam pipes and cap grating."""
    base = deck_line(x) + 3.20
    rake = math.radians(7)
    parts = []
    sections = []
    for t in (0.0, 0.45, 1.0):
        z = base + height * t
        shift = height * t * math.tan(rake)
        hy = 2.30 - 0.55 * t
        hz = 1.55 - 0.35 * t
        sections.append((x - shift, wf.oval(hy, hz, 16, centre_z=z)))
    # The loft runs along x, so build it as rings in the y/z plane at each station.
    body = wf.loft(name, [(sx, [(yy, zz) for yy, zz in pts]) for sx, pts in sections], paint)
    parts.append(wf.finish(body, 0.10, 3, smooth_angle=48))
    top = base + height
    parts.append(wf.finish(
        wf.cylinder("cap", (x - height * math.tan(rake), 0, top + 0.05), 1.85, 0.24,
                    "Z", 18, dark), 0.04, 2))
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.cylinder("steampipe", (x - height * 0.5 * math.tan(rake), sign * 1.4,
                                      base + height * 0.62), 0.22, height * 1.1, "Z", 10, metal),
            0.02, 1))
    return wf.join(name, parts)


def mast(metal, dark):
    """Tripod foremast with its yards."""
    parts = []
    base = deck_line(10.4) + 11.0
    for dx, dy in ((1.8, 0.0), (-1.2, 2.0), (-1.2, -2.0)):
        parts.append(wf.finish(
            wf.rotate(wf.cone("leg", (10.4 + dx * 0.5, dy * 0.5, base + 4.0),
                              0.26, 0.16, 9.0, "Z", 10, metal),
                      x=math.degrees(math.atan2(dy, 9.0)),
                      y=-math.degrees(math.atan2(dx, 9.0))), 0.02, 2))
    parts.append(wf.finish(
        wf.cone("topmast", (10.4, 0, base + 11.0), 0.14, 0.06, 6.0, "Z", 8, metal), 0.02, 1))
    for z, half in ((base + 7.6, 4.6), (base + 10.4, 3.0)):
        parts.append(wf.finish(
            wf.cylinder("yard", (10.4, 0, z), 0.10, half * 2, "Y", 8, metal), 0.02, 1))
    return wf.join("mast", parts)


def radar(metal, dark):
    """SC air-search bedstead, and the SG surface-search dish below it."""
    base = deck_line(10.4) + 11.0
    parts = [
        wf.finish(wf.box("scframe", (10.4, 0, base + 13.6), (0.30, 5.0, 2.20), metal), 0.04, 2),
    ]
    for i in range(7):
        parts.append(wf.finish(
            wf.box("scbar", (10.4, -2.2 + i * 0.73, base + 13.6), (0.14, 0.10, 2.10), metal),
            0.02, 1))
    parts.append(wf.finish(
        wf.cylinder("sgdish", (10.4, 0, base + 8.8), 1.10, 0.30, "X", 16, metal), 0.05, 2))
    return wf.join("radar", parts)


def torpedoes(paint, metal, dark):
    """Two quintuple 21-inch mounts on the centreline, between the funnels."""
    parts = []
    for x in (-5.5, -15.9):
        base = deck_line(x) + 3.30
        parts.append(wf.finish(
            wf.cylinder("turntable", (x, 0, base - 0.30), 1.60, 0.60, "Z", 16, paint), 0.05, 2))
        for i in range(5):
            dy = (i - 2) * 0.82
            parts.append(wf.finish(
                wf.cylinder("tube", (x, dy, base + 0.90 + abs(i - 2) * -0.04), 0.40, 7.2,
                            "X", 14, paint), 0.04, 2))
        parts.append(wf.finish(
            wf.box("trainer", (x - 1.0, 0, base + 1.90), (1.2, 1.6, 1.2), metal), 0.10, 3))
    return wf.join("torps", parts)


def aa(paint, metal, dark):
    """40 mm Bofors twins, and the 20 mm Oerlikons along the deck edge."""
    parts = []
    for x, z in ((-29.6, deck_line(-29.6) + 3.4), (17.0, deck_line(17.0) + 5.6)):
        parts.append(wf.finish(
            wf.cylinder("tub", (x, 0, z - 0.40), 2.30, 1.40, "Z", 18, paint), 0.08, 2))
        parts.append(wf.finish(
            wf.box("bofors", (x, 0, z + 0.70), (1.8, 2.2, 1.1), metal), 0.10, 3))
        for dy in (-0.45, 0.45):
            parts.append(wf.finish(
                wf.cone("bofbarrel", (x + 2.2, dy, z + 1.00), 0.12, 0.08, 3.0, "X", 10, metal),
                0.02, 1))
    for x in (-20.0, -8.0, 22.0):
        for sign in (-1, 1):
            y = sign * beam_at(x) * 0.80
            z = deck_line(x) + 0.90
            parts.append(wf.finish(
                wf.cylinder("oerlikontub", (x, y, z - 0.30), 0.90, 1.00, "Z", 14, paint),
                0.05, 2))
            parts.append(wf.finish(
                wf.rotate(wf.cone("oerlikon", (x + 0.8, y, z + 1.10), 0.09, 0.06, 2.0,
                                  "X", 8, metal), y=-14), 0.02, 1))
    return wf.join("aa", parts)


def depth_charges(dark, metal, paint):
    """Stern racks and the K-guns along the quarterdeck."""
    parts = []
    for sign in (-1, 1):
        for i in range(6):
            x = -46.0 - i * 1.5
            parts.append(wf.finish(
                wf.cylinder("charge", (x, sign * 1.5, deck_line(x) + 0.80), 0.44, 0.90,
                            "Z", 12, dark), 0.05, 2))
        parts.append(wf.finish(
            wf.box("rack", (-49.5, sign * 1.5, deck_line(-49.5) + 0.42),
                   (10.0, 1.20, 0.24), metal), 0.03, 1))
    for x in (-34.0, -28.0):
        for sign in (-1, 1):
            y = sign * beam_at(x) * 0.86
            parts.append(wf.finish(
                wf.rotate(wf.cylinder("kgun", (x, y, deck_line(x) + 0.90), 0.34, 2.20,
                                      "Z", 10, metal), x=sign * 42), 0.03, 1))
    return wf.join("depth", parts)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

# Measure 21: navy blue overall, from the boot topping to the masthead. It made a ship
# hard to see from the air and very easy to see against the horizon, which is why the
# Pacific fleet went back to lighter greys in 1945.
PALETTE_BODY = (0x4E / 255, 0x5A / 255, 0x66 / 255)
SEA_BLUE = (0.80, 0.82, 0.88)
DECK_BLUE = (0.62, 0.66, 0.72)
BOOT_TOPPING = (0.34, 0.34, 0.36)
HULL_RED = (1.30, 0.52, 0.40)

PARTS = ("hull", "deck", "gun_a", "gun_b", "gun_x", "bridge", "funnel_f", "funnel_a",
         "mast", "radar", "torps", "aa", "depth")


def weathering():
    """Salt, rust streaks and worn paint - a destroyer at sea was never clean."""
    for name in ("hull", "deck", "gun_a", "gun_b", "gun_x", "bridge",
                 "funnel_f", "funnel_a", "torps", "aa"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=1.30)
        wf.weather(obj, camo=(SEA_BLUE, DECK_BLUE), cover=(0.24, 0.24),
                   dirt=0.30, wear=0.26, scale=7.0, seed=17, ground=KEEL, top=12.0)
    for name in ("mast", "radar", "depth"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.10, wear=0.34, seed=19, ground=KEEL, top=40.0)


def waterline():
    """
    Anti-fouling red below the waterline and a black boot topping at it.

    The single most recognisable thing about any ship's paint, and the one place a flat
    colour would look obviously wrong.
    """
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
        if z < -0.55:
            tint = HULL_RED
        elif z < 0.35:
            tint = BOOT_TOPPING
        else:
            continue
        r, g, b = (c / wf.TINT_RANGE for c in tint)
        col.data[i].color = (r, g, b, 1.0)


def build():
    wf.reset()
    paint = wf.role("BODY", HAZE, roughness=0.70)
    metal = wf.role("METAL", STEEL, roughness=0.44, metallic=0.70)
    dark = wf.role("DARK", DARK, roughness=0.72, metallic=0.30)
    trim = wf.role("TRIM", HAZE, roughness=0.76)
    glass = wf.role("GLASS", GLASS, roughness=0.16, metallic=0.30)

    hull(paint, dark, metal)
    deck(trim, metal, dark)
    gun_a(paint, metal, dark)
    gun_b(paint, metal, dark)
    gun_x(paint, metal, dark)
    bridge(paint, glass, metal)
    funnel("funnel_f", -4.1, paint, dark, metal, height=7.4)
    funnel("funnel_a", -21.7, paint, dark, metal, height=7.0)
    mast(metal, dark)
    radar(metal, dark)
    torpedoes(paint, metal, dark)
    aa(paint, metal, dark)
    depth_charges(dark, metal, paint)

    weathering()
    waterline()
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(60.0)
        wf.camera(azimuth=52, elevation=18)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
