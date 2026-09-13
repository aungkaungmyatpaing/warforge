"""
T-34-85, 1944 production, at full size.

The contrast piece to the Panzer IV, and it is worth seeing them built the same way: the
German tank is flat plates bolted into a box with a turret on top, the Soviet one is
sloped everywhere and its turret is a single casting. Almost every difference people
argue about is visible in the geometry.

Object names are the part ids the game drags around, so the glTF carries them straight
through to the assembly board.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


# Preview colours only. The names of the materials are what crosses into the app, and
# the app paints from the vehicle's own palette.
GREEN = (0.16, 0.19, 0.11)          # 4BO protective green
STEEL = (0.31, 0.32, 0.33)
TRACK_STEEL = (0.18, 0.18, 0.19)
RUBBER = (0.08, 0.08, 0.09)
GLASS = (0.10, 0.16, 0.18)

# ---------------------------------------------------------------------------
# Dimensions, in metres. Hull 6.10 long, 3.00 wide, 2.72 to the top of the cupola.
# ---------------------------------------------------------------------------
HULL_REAR = -3.05
HULL_NOSE = 3.05
FLOOR = 0.42             # belly, and the ground clearance
SPONSON = 0.96           # sponson floor: the shelf the tracks run under
DECK = 1.62              # hull roof
TURRET_TOP = 2.44
CUPOLA_TOP = 2.72

LOWER_HALF = 0.98        # lower hull sides, between the tracks
SPONSON_HALF = 1.50      # half the full 3.00 m width
DECK_HALF = 1.00         # the roof is much narrower than the hull - that is the slope

TRACK_IN = 1.00
TRACK_OUT = 1.50         # 0.50 m track, the width that kept it out of the mud
TRACK_Y = (TRACK_IN + TRACK_OUT) * 0.5

WHEEL_R = 0.415          # 0.83 m road wheels, twice the Panzer IV's
WHEELS_X = (-2.12, -1.02, 0.06, 1.16, 2.26)
SPROCKET = (-2.66, 0.44, 0.30)      # rear drive - the opposite end from the Panzer IV
IDLER = (2.70, 0.42, 0.29)

RING_X = 0.20            # turret ring centre
TURRET_FRONT = 1.40
TURRET_REAR = -1.20
TURRET_HALF = 1.12
TURRET_ROOF_HALF = 0.88


def hull(paint, metal, dark):
    """
    The lower hull: the belly between the tracks, and what hangs off the ends of it.

    Little of this is ever seen - the sponsons and the tracks cover the sides - but the
    final drive housings and the towing eyes read clearly from the rear, and the belly
    has to be there for the tracks to sit against.
    """
    parts = []
    # Side profile, swept between the tracks. The nose plate slopes up and forward.
    body = wf.prism("hull", [
        (HULL_REAR, FLOOR),
        (HULL_REAR, SPONSON),
        (HULL_NOSE, SPONSON),
        (HULL_NOSE, 0.78),
        (2.78, FLOOR),
    ], -LOWER_HALF, LOWER_HALF, paint)
    parts.append(wf.finish(body, 0.022, 2))

    # Final drive housings, where the sprocket shafts come through.
    for y in (-TRACK_Y, TRACK_Y):
        parts.append(wf.finish(
            wf.cylinder("finaldrive", (SPROCKET[0] + 0.10, y * 0.70, SPROCKET[2] + 0.10),
                        0.30, 0.55, "Y", 20, paint), 0.02, 2))

    # Towing eyes: two at each end, and they are how you tell a T-34's nose from its tail.
    for x, sign in ((HULL_NOSE - 0.06, 1), (HULL_REAR + 0.06, -1)):
        for y in (-0.62, 0.62):
            parts.append(wf.finish(
                wf.box("toweye", (x + sign * 0.07, y, 0.62), (0.16, 0.10, 0.22), metal),
                0.015, 2))

    return wf.join("hull", parts)


def upper(paint, glass, metal, dark):
    """
    The upper hull - sloped on every face, which is the whole argument of the design.

    Built as a box and cut back by rotated wedges rather than assembled from plates: a
    Boolean gives the glacis a real edge where it meets the sponson side, and that
    junction is most of what the shape reads as.
    """
    parts = []
    # Sides slope 40 degrees from the vertical, so the roof is a metre narrower than the
    # hull. A frustum states that directly instead of hoping a rotated cutter lands.
    box_ = wf.frustum("upper", (0, 0, (SPONSON + DECK) * 0.5),
                      (HULL_NOSE - HULL_REAR, SPONSON_HALF * 2),
                      (HULL_NOSE - HULL_REAR, DECK_HALF * 2),
                      DECK - SPONSON, paint)

    # Glacis: 60 degrees from the vertical, the single most copied plate in tank design.
    wf.slice_xz(box_, (HULL_NOSE, SPONSON), (1.91, DECK), drop=(HULL_NOSE, DECK))
    # Rear plate, sloped the other way.
    wf.slice_xz(box_, (HULL_REAR, SPONSON), (-2.34, DECK), drop=(HULL_REAR, DECK))
    parts.append(wf.finish(box_, 0.030, 3))

    # --- glacis fittings ---------------------------------------------------
    # Driver's hatch, port side, with its two periscopes. The hatch is in the glacis
    # itself on a T-34 - there is no separate driver's plate.
    hatch = wf.rotate(wf.box("drvhatch", (2.62, 0.42, 1.33), (0.56, 0.50, 0.05), paint),
                      y=-60)
    parts.append(wf.finish(hatch, 0.012, 2))
    for dy in (-0.13, 0.13):
        parts.append(wf.finish(
            wf.rotate(wf.box("periscope", (2.50, 0.42 + dy, 1.42), (0.14, 0.14, 0.07), metal),
                      y=-60), 0.008, 2))

    # Towing shackles and the headlight, starboard of the hatch.
    parts.append(wf.finish(
        wf.cylinder("headlight", (2.74, -0.10, 1.30), 0.10, 0.10, "X", 18, metal), 0.01, 2))
    parts.append(wf.finish(
        wf.cylinder("horn", (2.86, 0.86, 1.12), 0.07, 0.10, "X", 14, metal), 0.008, 2))

    # Spare track links carried on the glacis - almost universal in service.
    for i in range(4):
        parts.append(wf.finish(
            wf.rotate(wf.box("sparelink", (2.30 - i * 0.10, -0.74, 1.44 + i * 0.17),
                             (0.17, 0.50, 0.05), dark), y=-60), 0.01, 1))

    # --- engine deck -------------------------------------------------------
    # Central engine hatch, then the two air outlets either side of it. The louvres sit
    # in a recess cut into the deck: standing proud they read as a ladder bolted on top.
    parts.append(wf.finish(
        wf.box("enghatch", (-1.62, 0, DECK + 0.02), (1.00, 0.86, 0.05), paint), 0.016, 2))
    parts.append(wf.finish(
        wf.box("hatchhinge", (-1.12, 0, DECK + 0.04), (0.07, 0.80, 0.05), metal), 0.008, 1))
    for y in (-0.70, 0.70):
        well = wf.box("grille", (-1.62, y, DECK - 0.05), (0.94, 0.42, 0.14), dark)
        parts.append(wf.finish(well, 0.010, 1))
        for i in range(8):
            parts.append(wf.finish(
                wf.rotate(wf.box("louvre", (-2.02 + i * 0.115, y, DECK - 0.01),
                                 (0.04, 0.40, 0.09), dark), y=30), 0.004, 1))
    # Radiator outlet across the rear deck, in its own recess.
    parts.append(wf.finish(
        wf.box("radwell", (-2.46, 0, DECK - 0.04), (0.40, 1.16, 0.12), dark), 0.010, 1))
    for i in range(5):
        parts.append(wf.finish(
            wf.rotate(wf.box("radslat", (-2.62 + i * 0.085, 0, DECK - 0.01),
                             (0.035, 1.14, 0.08), dark), y=30), 0.004, 1))
    # Fuel filler caps.
    for x, y in ((-0.92, 1.04), (-2.34, 1.04), (-0.92, -1.04), (-2.34, -1.04)):
        parts.append(wf.finish(
            wf.cylinder("filler", (x, y, SPONSON + 0.62), 0.09, 0.06, "Z", 14, metal), 0.006, 1))

    # --- rear plate --------------------------------------------------------
    # Transmission access hatch, and the two exhausts either side of it.
    parts.append(wf.finish(
        wf.rotate(wf.box("transhatch", (HULL_REAR - 0.20, 0, 1.24), (0.62, 0.90, 0.06), paint),
                  y=28), 0.012, 2))
    for y in (-0.88, 0.88):
        parts.append(wf.finish(
            wf.cylinder("exhaust", (HULL_REAR - 0.14, y, 1.16), 0.13, 0.26, "X", 18, dark),
            0.012, 2))
        parts.append(wf.finish(
            wf.box("exguard", (HULL_REAR - 0.24, y, 1.16), (0.05, 0.34, 0.34), metal), 0.01, 1))

    return wf.join("upper", parts)


def turret(paint, metal, glass):
    """
    The cast turret: one piece of steel, so every corner is a radius rather than a weld.

    Cut from a block by four rotated wedges - front, rear and both sides - then bevelled
    hard, which is what separates a casting from a fabricated box at a glance.
    """
    parts = []
    shell = wf.frustum("turret", (RING_X, 0, (DECK + TURRET_TOP) * 0.5),
                       (TURRET_FRONT - TURRET_REAR, TURRET_HALF * 2),
                       (TURRET_FRONT - TURRET_REAR, TURRET_ROOF_HALF * 2),
                       TURRET_TOP - DECK, paint)

    # Hexagonal in plan: the front corners are cut away towards the mantlet, and the
    # rear tapers in as well. Seen from above that six-sided outline is the shape people
    # picture when they picture a T-34.
    front = RING_X + TURRET_FRONT
    rear = RING_X + TURRET_REAR
    for sign in (-1, 1):
        wf.slice_xy(shell, (front, sign * 0.40), (front - 0.78, sign * TURRET_HALF),
                    drop=(front, sign * TURRET_HALF))
        wf.slice_xy(shell, (rear, sign * 0.62), (rear + 0.52, sign * TURRET_HALF),
                    drop=(rear, sign * TURRET_HALF))
    # The front face leans back over the mantlet, and the rear bustle is undercut.
    wf.slice_xz(shell, (front - 0.26, DECK), (front - 0.54, TURRET_TOP),
                drop=(front + 1.0, TURRET_TOP))
    wf.slice_xz(shell, (rear, DECK + 0.16), (rear + 0.30, TURRET_TOP),
                drop=(rear - 1.0, DECK))
    parts.append(wf.finish(shell, 0.075, 5))

    # Loader's hatch, starboard rear of the roof.
    parts.append(wf.finish(
        wf.cylinder("ldrhatch", (RING_X - 0.42, -0.44, TURRET_TOP + 0.01), 0.27, 0.08,
                    "Z", 20, paint), 0.014, 2))
    # Two ventilator domes on the roof - the T-34-85 gained a second one.
    for x, y in ((RING_X - 0.92, 0.0), (RING_X + 0.52, 0.0)):
        parts.append(wf.finish(
            wf.cylinder("vent", (x, y, TURRET_TOP + 0.03), 0.11, 0.10, "Z", 16, metal),
            0.012, 2))
    # Pistol ports, one each side and one in the rear.
    for y in (-TURRET_ROOF_HALF - 0.08, TURRET_ROOF_HALF + 0.08):
        parts.append(wf.finish(
            wf.cylinder("pistolport", (RING_X - 0.62, y, 1.92), 0.08, 0.08, "Y", 14, metal),
            0.008, 2))
    parts.append(wf.finish(
        wf.cylinder("pistolport", (RING_X + TURRET_REAR - 0.16, 0, 1.98), 0.08, 0.10,
                    "X", 14, metal), 0.008, 2))
    # Lifting eyes, cast into the turret front and rear.
    for x in (RING_X + TURRET_FRONT - 0.30, RING_X + TURRET_REAR + 0.24):
        for y in (-0.52, 0.52):
            parts.append(wf.finish(
                wf.box("lifteye", (x, y, TURRET_TOP - 0.02), (0.13, 0.06, 0.13), metal),
                0.012, 2))

    return wf.join("turret", parts)


def gun(metal, paint):
    """
    85 mm ZiS-S-53: a cast mantlet, and a barrel with no muzzle brake.

    The absence is worth knowing: the 85 was recoil-managed by the mounting rather than
    by a brake, which is why a T-34-85 muzzle is a plain ring where a Panther's or a late
    Panzer IV's is a slotted block.
    """
    parts = []
    face = RING_X + TURRET_FRONT
    # A rounded casting rather than a cylinder - a drum leaves two flat discs showing
    # where it meets the turret, and that is the first thing that looks wrong.
    mantlet = wf.box("mantlet", (face - 0.02, 0, 1.94), (0.34, 0.92, 0.54), paint)
    parts.append(wf.finish(mantlet, 0.13, 6))
    parts.append(wf.finish(
        wf.box("mantcheek", (face - 0.22, 0, 1.94), (0.26, 1.00, 0.60), paint), 0.11, 5))

    # Collar where the barrel leaves the mantlet, then the barrel itself.
    parts.append(wf.finish(
        wf.cylinder("collar", (face + 0.24, 0, 1.94), 0.165, 0.44, "X", 20, paint), 0.02, 2))
    parts.append(wf.finish(
        wf.cone("barrel", (face + 1.80, 0, 1.94), 0.116, 0.098, 2.70, "X", 20, paint),
        0.008, 2))
    # The bore, so the muzzle is a ring and not a flat disc.
    muzzle = wf.cylinder("muzzle", (face + 3.22, 0, 1.94), 0.102, 0.16, "X", 20, metal)
    wf.cut(muzzle, wf.cylinder("cut", (face + 3.24, 0, 1.94), 0.043, 0.36, "X", 16))
    parts.append(wf.finish(muzzle, 0.006, 2))

    return wf.join("gun", parts)


def cupola(paint, glass, metal):
    """Commander's cupola: port side of the turret roof, with its two-piece hatch."""
    parts = []
    drum = wf.cylinder("cupola", (RING_X - 0.36, 0.44, TURRET_TOP + 0.11), 0.30, 0.24,
                       "Z", 22, paint)
    parts.append(wf.finish(drum, 0.02, 3))
    # Five vision slits round the front of it.
    for i in range(5):
        a = math.radians(-60 + i * 30)
        parts.append(wf.finish(
            wf.rotate(
                wf.box("slit", (RING_X - 0.36 + 0.30 * math.cos(a),
                                0.44 + 0.30 * math.sin(a), TURRET_TOP + 0.13),
                       (0.06, 0.13, 0.07), glass),
                z=math.degrees(a)), 0.006, 1))
    # The hatch lid, split down the middle and sitting a little proud.
    for dy in (-0.15, 0.15):
        parts.append(wf.finish(
            wf.cylinder("lid", (RING_X - 0.36, 0.44 + dy, TURRET_TOP + 0.25), 0.29, 0.05,
                        "Z", 22, paint), 0.012, 2))
    parts.append(wf.finish(
        wf.cylinder("periscope", (RING_X - 0.56, 0.44, TURRET_TOP + 0.30), 0.07, 0.09,
                    "Z", 12, metal), 0.008, 1))

    return wf.join("cupola", parts)


def machine_gun(metal, paint):
    """Bow DT machine gun, starboard of the driver."""
    parts = [
        wf.finish(wf.sphere("ballmount", (2.56, -0.56, 1.28), 0.20, 20, paint), 0.01, 2),
        wf.finish(wf.cone("mg", (2.86, -0.56, 1.28), 0.042, 0.028, 0.52, "X", 16, metal),
                  0.006, 2),
        # The DT's conical flash hider.
        wf.finish(wf.cone("flash", (3.14, -0.56, 1.28), 0.030, 0.058, 0.10, "X", 16, metal),
                  0.005, 2),
    ]
    return wf.join("mg", parts)


def road_wheel(name, x, y, mat_rubber, mat_metal):
    """One dished road wheel: rubber tyre, and the drilled disc behind it."""
    disc = wf.cylinder(name + "_disc", (x, y - 0.02, WHEEL_R), WHEEL_R - 0.075, 0.19,
                       "Y", 20, mat_metal)
    # Lightening holes - cut, not stuck on. As raised cylinders they read as studs, and
    # the drilled disc is what makes a T-34 wheel a T-34 wheel.
    for i in range(6):
        a = 2 * math.pi * i / 6
        wf.cut(disc, wf.cylinder(
            "cut", (x + (WHEEL_R - 0.21) * math.cos(a), y - 0.02,
                    WHEEL_R + (WHEEL_R - 0.21) * math.sin(a)),
            0.060, 0.40, "Y", 10))
    return [
        wf.cylinder(name, (x, y, WHEEL_R), WHEEL_R, 0.16, "Y", 22, mat_rubber),
        disc,
        wf.cylinder(name + "_hub", (x, y - 0.05, WHEEL_R), 0.085, 0.24, "Y", 14, mat_metal),
    ]


def running_gear(metal, dark, paint):
    """Christie suspension: five big wheels a side, drive at the rear, idler at the front."""
    parts = []
    y = TRACK_Y

    # Drive sprocket: the roller type, which meshes with the track's guide horns.
    sx, sz, sr = SPROCKET
    parts.append(wf.cylinder("sprocket", (sx, y, sz), sr - 0.06, 0.30, "Y", 22, metal))
    for i in range(6):
        a = 2 * math.pi * i / 6
        parts.append(wf.cylinder(
            "roller", (sx + sr * math.cos(a), y, sz + sr * math.sin(a)),
            0.07, 0.34, "Y", 10, metal))

    ix, iz, ir = IDLER
    parts.append(wf.cylinder("idler", (ix, y, iz), ir, 0.26, "Y", 22, metal))
    parts.append(wf.cylinder("idlerhub", (ix, y - 0.03, iz), ir * 0.45, 0.30, "Y", 14, metal))

    for i, x in enumerate(WHEELS_X):
        parts += road_wheel("roadwheel", x, y, dark, metal)
        # The swing arm back to its torsion point inside the hull.
        lean = -22 if i < 2 else 22
        parts.append(wf.rotate(
            wf.box("swingarm", (x + (0.16 if i < 2 else -0.16), y - 0.16, WHEEL_R + 0.10),
                   (0.44, 0.12, 0.12), paint), y=lean))

    for p in parts:
        wf.finish(p, 0.010, 1)
    return wf.mirror_y(wf.join("wheels", parts))


def track_path():
    """
    Closed run round idler, ground, sprocket and back.

    The return run rests straight on the tops of the road wheels - a T-34 has no return
    rollers at all, and that flat sagging top run is one of its clearest signatures.
    """
    pts = []
    sx, sz, sr = SPROCKET
    ix, iz, ir = IDLER
    rs = sr + 0.06
    ri = ir + 0.06
    pts.append((sx, 0.055))
    pts.append((ix, 0.055))
    for i in range(1, 10):
        a = math.radians(-90 + i * (180 / 10))
        pts.append((ix + ri * math.cos(a), iz + ri * math.sin(a)))
    # Over the wheels, front to rear, with a little sag between them.
    for i, x in enumerate(reversed(WHEELS_X)):
        pts.append((x, WHEEL_R * 2 + 0.05))
        if i < len(WHEELS_X) - 1:
            nxt = list(reversed(WHEELS_X))[i + 1]
            pts.append(((x + nxt) * 0.5, WHEEL_R * 2 - 0.03))
    for i in range(1, 10):
        a = math.radians(90 + i * (180 / 10))
        pts.append((sx + rs * math.cos(a), sz + rs * math.sin(a)))
    return pts


def tracks(dark):
    run = wf.track_run("track", track_path(), (0.172, TRACK_OUT - TRACK_IN, 0.052), dark)
    run.location.y = TRACK_Y
    return wf.mirror_y(run)


def fenders(trim, metal):
    """Fenders over the tracks, with the mudguards turned down at each end."""
    parts = []
    parts.append(wf.finish(
        wf.box("fender", (0.0, (TRACK_IN + TRACK_OUT) * 0.5 + 0.04, SPONSON + 0.06),
               (6.30, TRACK_OUT - TRACK_IN + 0.14, 0.035), trim), 0.008, 2))
    # Mudguards, turned down over each end of the track rather than sticking out flat.
    parts.append(wf.finish(
        wf.rotate(wf.box("mudguard", (3.28, TRACK_Y, SPONSON - 0.09),
                         (0.24, TRACK_OUT - TRACK_IN + 0.10, 0.035), trim), y=-70), 0.008, 2))
    parts.append(wf.finish(
        wf.rotate(wf.box("mudguard", (-3.26, TRACK_Y, SPONSON - 0.11),
                         (0.34, TRACK_OUT - TRACK_IN + 0.10, 0.035), trim), y=48), 0.008, 2))
    # Brackets under the fender.
    for x in (-2.40, -1.20, 0.0, 1.20, 2.40):
        parts.append(wf.finish(
            wf.box("bracket", (x, TRACK_OUT + 0.02, SPONSON + 0.01),
                   (0.07, 0.10, 0.10), metal), 0.006, 1))
    return wf.mirror_y(wf.join("fender", parts))


def drums(trim, metal):
    """Two external fuel drums a side, strapped to the rear fenders."""
    parts = []
    for x in (-2.62, -1.82):
        parts.append(wf.finish(
            wf.cylinder("drum", (x, TRACK_Y + 0.04, SPONSON + 0.34), 0.25, 0.68,
                        "X", 20, trim), 0.02, 3))
        # End rims and the strap round the middle.
        for dx in (-0.32, 0.32):
            parts.append(wf.finish(
                wf.cylinder("rim", (x + dx, TRACK_Y + 0.04, SPONSON + 0.34), 0.255, 0.05,
                            "X", 20, trim), 0.008, 2))
        parts.append(wf.finish(
            wf.cylinder("strap", (x, TRACK_Y + 0.04, SPONSON + 0.34), 0.265, 0.05,
                        "X", 20, metal), 0.006, 2))
    return wf.mirror_y(wf.join("drums", parts))


def rails(dark):
    """
    Grab rails on the turret and hull sides.

    Soviet tanks carried infantry into the attack, and the handrails they held on to are
    a detail nobody who has seen a photograph of one would leave off.
    """
    parts = []
    for y in (-TURRET_ROOF_HALF - 0.10, TURRET_ROOF_HALF + 0.10):
        for x in (RING_X - 0.80, RING_X - 0.10, RING_X + 0.58):
            parts.append(wf.finish(
                wf.box("rail", (x, y, 2.02), (0.32, 0.05, 0.05), dark), 0.012, 2))
            for dx in (-0.15, 0.15):
                parts.append(wf.finish(
                    wf.box("railfoot", (x + dx, y + (0.05 if y > 0 else -0.05), 2.02),
                           (0.04, 0.12, 0.04), dark), 0.008, 1))
    for y in (-1.30, 1.30):
        for x in (-1.90, -0.60):
            parts.append(wf.finish(
                wf.box("hullrail", (x, y, DECK - 0.16), (0.34, 0.05, 0.05), dark), 0.012, 2))
    return wf.join("rail", parts)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

# 4BO green was the only colour most T-34s ever wore - no camouflage scheme, just a
# factory coat that faded and wore through. The variation has to come from wear and
# dirt rather than from patches, which is exactly the opposite of the Panzer IV.
PALETTE_BODY = (0x3E / 255, 0x4A / 255, 0x2C / 255)
MARK_WHITE = (0.86, 0.85, 0.80)

PARTS = ("hull", "upper", "turret", "gun", "cupola", "mg",
         "wheels", "track", "fender", "drums", "rail")


def weathering():
    """
    Wear, dirt and a faded coat. No camouflage - a T-34 did not get one.

    Which makes the surface pass harder rather than easier: with no patches to break it
    up, all the variation has to come from how the paint aged and how much mud it was
    driven through, and a T-34 was driven through a great deal of mud.
    """
    # Two shades either side of the base: sun-bleached where it faced the weather, and
    # a fresher, greener coat where it was touched up from a different batch.
    #
    # Both have to shift *hue* and not only brightness. A patch that is purely lighter or
    # darker is indistinguishable from the shading already on a curved hull, and reads as
    # a gradient rather than as paint - which is exactly what the first attempt did.
    fade = ((1.34, 1.30, 1.02), (0.60, 0.74, 0.56))

    # Everything low enough to be thrown mud at.
    for name in ("hull", "upper", "fender", "drums", "mg"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.32)
        wf.weather(obj, camo=fade, cover=(0.30, 0.26), dirt=0.62, wear=0.30,
                   scale=1.25, seed=11, ground=0.0, top=3.4)

    # Turret and gun: the same faded paint, but they stay out of the mud.
    for name in ("turret", "gun", "cupola"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.32)
        wf.weather(obj, camo=fade, cover=(0.30, 0.26), dirt=0.16, wear=0.34,
                   scale=1.10, seed=11, ground=0.0, top=3.4)

    # Unpainted steel: the running gear, and the handrails that get grabbed all day.
    for name, dirt, top in (("wheels", 0.70, 1.6), ("track", 0.64, 1.6), ("rail", 0.10, 2.6)):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=dirt, wear=0.42, seed=13, ground=0.0, top=top)


def markings(paint):
    """A white tactical number on each turret side, and nothing else."""
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    lean = math.degrees(math.atan2(TURRET_HALF - TURRET_ROOF_HALF, TURRET_TOP - DECK))
    added = []
    for sign in (1, -1):
        # On the sloping side, at the height the number actually sits.
        t = (1.96 - DECK) / (TURRET_TOP - DECK)
        y = (TURRET_HALF + (TURRET_ROOF_HALF - TURRET_HALF) * t + 0.012) * sign
        text = "215" if sign < 0 else "512"
        num = wf.numerals("tacnum", text, (RING_X - 0.62, y, 1.96), 0.32, 0.014, paint,
                          stroke=0.20)
        wf.rotate(num, x=sign * lean, z=0)
        added.append(wf.flat_tint(num, white))
    obj = bpy.data.objects.get("turret")
    if obj is not None:
        wf.join("turret", [obj] + added)


def build():
    wf.reset()
    paint = wf.role("BODY", GREEN, roughness=0.78)
    metal = wf.role("METAL", STEEL, roughness=0.42, metallic=0.80)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.62, metallic=0.40)
    trim = wf.role("TRIM", GREEN, roughness=0.80)
    glass = wf.role("GLASS", GLASS, roughness=0.22, metallic=0.30)
    rubber = wf.role("DARK", RUBBER, roughness=0.90)

    hull(paint, metal, dark)
    upper(paint, glass, metal, dark)
    turret(paint, metal, glass)
    gun(metal, paint)
    cupola(paint, glass, metal)
    machine_gun(metal, paint)
    running_gear(metal, rubber, paint)
    tracks(dark)
    fenders(trim, metal)
    drums(trim, metal)
    rails(dark)

    weathering()
    markings(paint)
    wf.occlude(PARTS)


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(6.2)
        wf.camera(azimuth=38, elevation=17)
        wf.render(args[0], samples=24)
    if len(args) > 1:
        wf.export_glb(args[1])
