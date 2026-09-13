"""
USS Nimitz, at full size.

333 metres of hull with a 76-metre flight deck laid diagonally across it, so that
aircraft can land at the same time as others are catapulted off the bow. Everything
above the waterline is arranged round that one idea, including putting the entire
superstructure in a single island jammed against the starboard edge.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

HAZE = (0.28, 0.31, 0.35)
DECK_GREY = (0.17, 0.18, 0.19)
STEEL = (0.40, 0.42, 0.44)
DARK = (0.10, 0.11, 0.12)
GLASS = (0.18, 0.26, 0.30)

BOW = 166.4
STERN = -166.4
BEAM_HALF = 20.40          # 40.8 m at the waterline
KEEL = -11.30
DECK = 20.20               # flight deck height above the waterline
HANGAR = 8.00              # hangar deck
FLIGHT_HALF = 38.40        # 76.8 m across the flight deck
ANGLE = 9.0                # the angled deck, in degrees off the centreline


def hull_deck(x):
    t = x / BOW
    if t >= 0:
        return HANGAR + 2.6 * t ** 2.2
    return HANGAR - 1.4 * (-t) ** 1.6


def beam_at(x):
    t = x / BOW
    if t > 0.80:
        return BEAM_HALF * max(0.05, 1.0 - ((t - 0.80) / 0.20) ** 1.3 * 0.96)
    if t < -0.84:
        return BEAM_HALF * max(0.34, 1.0 - ((-t - 0.84) / 0.16) ** 1.2 * 0.70)
    return BEAM_HALF * (1.0 - 0.07 * t * t)


def hull(paint, dark, metal):
    """The hull proper - everything below the hangar deck, and a bulbous bow."""
    parts = []
    sections = []
    for x in (STERN, -158.0, -140.0, -110.0, -70.0, -30.0, 0.0, 30.0, 70.0, 110.0,
              138.0, 152.0, 162.0, BOW):
        t = abs(x / BOW)
        flare = 0.16 * max(0.0, x / BOW) ** 1.5
        fullness = 0.97 - 0.30 * max(0.0, t - 0.52) / 0.48
        sections.append((x, wf.hull_section(beam_at(x), hull_deck(x), KEEL, flare=flare,
                                            fullness=fullness, camber=0.4,
                                            marks=(-1.2, 0.8))))
    parts.append(wf.finish(wf.loft("hull", sections, paint), 0.30, 3, smooth_angle=50))
    bulb = [
        (140.0, wf.oval(2.8, 2.8, 14, centre_z=-8.0)),
        (154.0, wf.oval(4.0, 4.0, 14, centre_z=-7.6)),
        (166.0, wf.oval(3.4, 3.4, 14, centre_z=-7.4)),
        (174.0, wf.oval(0.6, 0.6, 14, centre_z=-7.2)),
    ]
    parts.append(wf.finish(wf.loft("bulb", bulb, paint), 0.20, 3, smooth_angle=52))
    # The hangar sides, carrying the deck out to its full width.
    for sign in (-1, 1):
        side = []
        for x in (-150.0, -100.0, -40.0, 20.0, 80.0, 130.0, 152.0):
            half = min(FLIGHT_HALF * 0.62, beam_at(x) * 1.32)
            side.append((x, [(sign * beam_at(x) * 0.98, hull_deck(x)),
                             (sign * half, hull_deck(x) + 1.2),
                             (sign * half, DECK),
                             (sign * beam_at(x) * 0.98, DECK)]))
        parts.append(wf.finish(wf.loft("hangarside", side, paint), 0.24, 2, smooth_angle=46))
    for sign, dy in ((-1, 13.0), (1, 13.0), (-1, 5.0), (1, 5.0)):
        y = sign * dy
        parts.append(wf.finish(
            wf.cylinder("shaft", (-138.0, y, KEEL + 3.0), 1.00, 30.0, "X", 12, metal),
            0.10, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-156.0, -8.0, KEEL + 5.0), (7.0, 1.0, 9.0), metal), 0.20, 2))
    parts.append(wf.finish(
        wf.box("rudder", (-156.0, 8.0, KEEL + 5.0), (7.0, 1.0, 9.0), metal), 0.20, 2))
    return wf.join("hull", parts)


def flight_deck(trim, metal, dark):
    """
    The flight deck: a rectangle over the hull, with the angled landing area running out
    over the port side and the bow sponsons for the waist catapults.
    """
    parts = []
    slab = [
        (STERN - 2.0, [(-24.0, DECK - 1.2), (30.0, DECK - 1.2), (30.0, DECK), (-24.0, DECK)]),
        (-140.0, [(-33.0, DECK - 1.4), (33.0, DECK - 1.4), (33.0, DECK), (-33.0, DECK)]),
        (-60.0, [(-38.4, DECK - 1.6), (33.0, DECK - 1.6), (33.0, DECK), (-38.4, DECK)]),
        (20.0, [(-30.0, DECK - 1.6), (33.0, DECK - 1.6), (33.0, DECK), (-30.0, DECK)]),
        (110.0, [(-24.0, DECK - 1.4), (26.0, DECK - 1.4), (26.0, DECK), (-24.0, DECK)]),
        (150.0, [(-17.0, DECK - 1.2), (17.0, DECK - 1.2), (17.0, DECK), (-17.0, DECK)]),
        (BOW - 2.0, [(-8.0, DECK - 1.0), (8.0, DECK - 1.0), (8.0, DECK), (-8.0, DECK)]),
    ]
    parts.append(wf.finish(wf.loft("deck", slab, trim), 0.20, 2, smooth_angle=42))
    # Deck-edge safety nets all round.
    for x in range(-150, 155, 10):
        for sign in (-1, 1):
            half = 33.0 if -140 < x < 60 else 20.0
            parts.append(wf.finish(
                wf.rotate(wf.box("net", (float(x), sign * (half + 1.4), DECK - 1.1),
                                 (8.0, 2.6, 0.12), metal), x=sign * 18), 0.04, 1))
    return wf.join("deck", parts)


def markings(trim, dark, paint):
    """
    The deck markings: the angled landing area, the centreline and the big white 68.

    These are painted lines, so they are geometry laid a few centimetres proud - and they
    are what makes a grey rectangle read as a flight deck.
    """
    parts = []
    white = wf.tint_for((0.86, 0.86, 0.84), PALETTE_BODY)
    yellow = wf.tint_for((0.72, 0.62, 0.16), PALETTE_BODY)

    # Angled landing strip, running aft-to-port.
    strip = wf.box("angled", (-40.0, -8.0, DECK + 0.04), (180.0, 16.0, 0.08), trim)
    wf.rotate(strip, z=ANGLE)
    parts.append(wf.flat_tint(strip, white))
    for i in range(4):
        cross = wf.box("arrest", (-95.0 + i * 14.0, -8.0, DECK + 0.06), (0.9, 15.0, 0.08), trim)
        wf.rotate(cross, z=ANGLE)
        parts.append(wf.flat_tint(cross, yellow))
    # Bow catapult tracks, straight along the deck.
    for y in (-13.0, 3.0):
        parts.append(wf.flat_tint(
            wf.box("cattrack", (110.0, y, DECK + 0.05), (110.0, 1.0, 0.08), trim), white))
    # The hull number on the bow.
    parts.append(wf.flat_tint(
        wf.numerals("deckno", "68", (138.0, 0.0, DECK + 0.06), 11.0, 0.08, trim,
                    stroke=0.18), white))
    return wf.join("markings", parts)


def island(paint, glass, metal, dark):
    """The island: tall, narrow, and hard against the starboard deck edge."""
    parts = []
    x, y = -8.0, 26.0
    base = DECK
    levels = [(0.0, 32.0, 10.0, 5.0), (1.0, 26.0, 9.0, 4.4), (2.0, 20.0, 8.0, 4.0),
              (2.5, 15.0, 7.0, 3.6), (3.0, 11.0, 6.0, 3.4)]
    z = base
    for dx, length, width, height in levels:
        parts.append(wf.finish(
            wf.box("islandlevel", (x + dx, y, z + height * 0.5), (length, width, height),
                   paint), 0.30, 3))
        if length > 24:
            for i in range(6):
                parts.append(wf.finish(
                    wf.box("window", (x + dx - length * 0.5 + 0.2, y - 3.0 + i * 1.2,
                                      z + height * 0.66),
                           (0.30, 0.90, 0.90), glass), 0.05, 2))
        z += height
    # Primary flight control, hanging off the after end and overlooking the deck.
    parts.append(wf.finish(
        wf.box("pricom", (x - 14.0, y - 4.6, base + 12.0), (7.0, 3.0, 3.0), paint), 0.24, 3))
    # Funnel uptakes at the after end of the island.
    for dy in (-2.4, 2.4):
        parts.append(wf.finish(
            wf.box("uptake", (x - 11.0, y + dy, z + 2.4), (6.0, 3.4, 5.0), dark), 0.24, 3))
    return wf.join("island", parts)


def mast(metal, dark):
    parts = []
    x, y = -8.0, 26.0
    base = DECK + 20.4
    parts.append(wf.finish(
        wf.box("mastbase", (x + 1.0, y, base + 3.0), (5.0, 5.0, 6.0), metal), 0.20, 3))
    parts.append(wf.finish(
        wf.cone("pole", (x + 1.0, y, base + 12.0), 0.60, 0.22, 12.0, "Z", 10, metal),
        0.05, 2))
    for z, half in ((base + 9.0, 6.0), (base + 15.0, 3.6)):
        parts.append(wf.finish(
            wf.cylinder("yard", (x + 1.0, y, z), 0.22, half * 2, "Y", 8, metal), 0.04, 1))
    return wf.join("mast", parts)


def radar(metal, dark, glass):
    """SPS-48 and SPS-49 air search, and the four SPY faces if she has been through SLEP."""
    parts = []
    x, y = -8.0, 26.0
    base = DECK + 20.4
    parts.append(wf.finish(
        wf.rotate(wf.box("sps48", (x + 2.0, y, base + 8.6), (0.6, 6.4, 6.4), metal), y=-10),
        0.10, 2))
    # SPS-49's big curved mattress, turning above it.
    parts.append(wf.finish(
        wf.box("sps49", (x + 1.0, y, base + 17.4), (0.5, 7.6, 2.2), metal), 0.08, 2))
    for i in range(7):
        parts.append(wf.finish(
            wf.box("dipole", (x + 1.3, y - 3.0 + i * 1.0, base + 17.4), (0.16, 0.14, 2.0),
                   metal), 0.03, 1))
    # Planar arrays on the island faces.
    for dx, dy, rot in ((6.0, -4.8, 0), (-12.0, -4.8, 0)):
        parts.append(wf.finish(
            wf.box("spyface", (x + dx, y + dy, DECK + 9.0), (4.4, 0.4, 4.4), dark), 0.10, 2))
    return wf.join("radar", parts)


def sponsons(paint, metal, dark):
    """The gun and boat sponsons hung off the hull below the deck edge."""
    parts = []
    for x in (-120.0, -60.0, 40.0, 120.0):
        for sign in (-1, 1):
            if sign > 0 and -40.0 < x < 30.0:
                continue                       # the island occupies that stretch
            y = sign * (min(FLIGHT_HALF * 0.62, beam_at(x) * 1.32) + 2.0)
            parts.append(wf.finish(
                wf.box("sponson", (x, y, DECK - 3.6), (14.0, 5.0, 2.6), paint), 0.20, 2))
            parts.append(wf.finish(
                wf.rotate(wf.box("bracket", (x, y - sign * 2.0, DECK - 5.8),
                                 (12.0, 4.0, 0.6), paint), x=sign * 40), 0.10, 1))
    return wf.join("sponsons", parts)


def ciws(metal, dark, paint):
    """Phalanx mounts and the Sea Sparrow launchers on the sponsons."""
    parts = []
    for x, sign in ((-130.0, -1), (-130.0, 1), (130.0, -1), (60.0, 1)):
        y = sign * (min(FLIGHT_HALF * 0.62, beam_at(x) * 1.32) + 2.0)
        z = DECK - 2.0
        parts.append(wf.finish(
            wf.cylinder("ciwsbase", (x, y, z), 1.20, 2.20, "Z", 16, paint), 0.14, 2))
        parts.append(wf.finish(
            wf.sphere("radome", (x, y, z + 2.0), 1.10, 16, paint), 0.10, 3))
        parts.append(wf.finish(
            wf.rotate(wf.cylinder("gatling", (x + sign * 0.4, y, z + 1.2), 0.34, 2.20,
                                  "X", 12, dark), y=-20), 0.05, 2))
    return wf.join("ciws", parts)


def jet_deck(metal, dark, paint):
    """Jet blast deflectors: the big hinged plates that come up behind a catapult."""
    parts = []
    for x, y in ((78.0, -13.0), (78.0, 3.0), (-58.0, -18.0), (-38.0, -24.0)):
        plate = wf.box("jbd", (x, y, DECK + 1.2), (3.4, 15.0, 2.6), metal)
        wf.rotate(plate, y=-38)
        parts.append(wf.finish(plate, 0.14, 2))
    return wf.join("jet_deck", parts)


def elevators(trim, metal, dark):
    """Four deck-edge lifts: three to starboard, one to port, aft of the angled deck."""
    parts = []
    for x, sign in ((64.0, 1), (-16.0, 1), (-86.0, 1), (-50.0, -1)):
        half = min(FLIGHT_HALF * 0.62, beam_at(x) * 1.32)
        y = sign * (half + 7.0)
        parts.append(wf.finish(
            wf.box("elevator", (x, y, DECK - 0.4), (24.0, 15.0, 1.2), trim), 0.12, 2))
        parts.append(wf.finish(
            wf.box("elevatorlip", (x, y + sign * 7.4, DECK - 0.4), (24.0, 0.5, 1.4), metal),
            0.06, 1))
    return wf.join("elevator", parts)


def catapults(metal, dark, trim):
    """The four catapult shuttles and their track slots."""
    parts = []
    for x, y, ang in ((110.0, -13.0, 0.0), (110.0, 3.0, 0.0),
                      (-52.0, -20.0, ANGLE), (-32.0, -26.0, ANGLE)):
        track = wf.box("cattrack", (x, y, DECK + 0.10), (92.0, 1.6, 0.16), dark)
        if ang:
            wf.rotate(track, z=ang)
        parts.append(wf.finish(track, 0.05, 1))
        parts.append(wf.finish(
            wf.box("shuttle", (x - 34.0, y, DECK + 0.26), (1.6, 1.2, 0.34), metal), 0.05, 1))
    return wf.join("cats", parts)


def screws(metal):
    parts = []
    for sign, dy in ((-1, 13.0), (1, 13.0), (-1, 5.0), (1, 5.0)):
        y = sign * dy
        parts.append(wf.finish(
            wf.cylinder("hub", (-153.0, y, KEEL + 3.0), 1.40, 2.0, "X", 16, metal), 0.10, 2))
        for i in range(5):
            a = 2 * math.pi * i / 5
            blade = wf.box("blade", (-153.4, y + 2.8 * math.cos(a), KEEL + 3.0 + 2.8 * math.sin(a)),
                           (1.4, 3.4, 0.34), metal)
            wf.rotate(blade, x=math.degrees(a), y=24)
            parts.append(wf.finish(blade, 0.08, 2))
    return wf.join("screws", parts)


PALETTE_BODY = (0x5F / 255, 0x6A / 255, 0x72 / 255)
GREY_LIGHT = (1.10, 1.10, 1.11)
GREY_DARK = (0.86, 0.87, 0.90)
BOOT = (0.28, 0.28, 0.30)
HULL_RED = (1.30, 0.52, 0.40)
DECK_TINT = (0.44, 0.45, 0.47)

PARTS = ("hull", "deck", "markings", "island", "mast", "radar", "sponsons", "ciws",
         "jet_deck", "elevator", "cats", "screws")


def weathering():
    for name in ("hull", "island", "sponsons", "ciws", "jet_deck", "elevator"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=4.00)
        wf.weather(obj, camo=(GREY_LIGHT, GREY_DARK), cover=(0.24, 0.24), dirt=0.28,
                   wear=0.24, scale=20.0, seed=151, ground=KEEL, top=34.0)
    for name in ("mast", "radar", "screws", "cats"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.10, wear=0.30, seed=157, ground=KEEL, top=60.0)


def flight_deck_paint():
    """
    The non-skid surface: a dark, matte grey quite unlike the ship's side, and scuffed
    black where the aircraft actually land.
    """
    obj = bpy.data.objects.get("deck")
    if obj is None:
        return
    wf.densify(obj, max_edge=5.0)
    wf.weather(obj, camo=None, dirt=0.0, wear=0.16, seed=163, ground=KEEL, top=60.0)
    col = obj.data.color_attributes.get("Col")
    if col is None:
        return
    tint = tuple(c / wf.TINT_RANGE for c in DECK_TINT)
    bpy.context.view_layer.update()
    to_world = obj.matrix_world
    for i, vert in enumerate(obj.data.vertices):
        if (to_world.to_3x3() @ vert.normal).z > 0.4:
            col.data[i].color = (tint[0], tint[1], tint[2], 1.0)


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
        tint = HULL_RED if z < -1.2 else (BOOT if z < 0.8 else None)
        if tint is None:
            continue
        r, g, b = (c / wf.TINT_RANGE for c in tint)
        col.data[i].color = (r, g, b, 1.0)


def build():
    wf.reset()
    paint = wf.role("BODY", HAZE, roughness=0.74)
    metal = wf.role("METAL", STEEL, roughness=0.46, metallic=0.72)
    dark = wf.role("DARK", DARK, roughness=0.78, metallic=0.25)
    trim = wf.role("TRIM", DECK_GREY, roughness=0.90)
    glass = wf.role("GLASS", GLASS, roughness=0.16, metallic=0.30)

    hull(paint, dark, metal)
    flight_deck(trim, metal, dark)
    island(paint, glass, metal, dark)
    mast(metal, dark)
    radar(metal, dark, glass)
    sponsons(paint, metal, dark)
    ciws(metal, dark, paint)
    jet_deck(metal, dark, paint)
    elevators(trim, metal, dark)
    catapults(metal, dark, trim)
    screws(metal)

    weathering()
    flight_deck_paint()
    waterline()
    markings(trim, dark, paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(180.0)
        wf.camera(azimuth=54, elevation=22)
        wf.render(args[0], samples=16)
    if len(args) > 1:
        wf.export_glb(args[1])
