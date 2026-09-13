"""
Panzer IV Ausf. H, modelled from its real plate layout.

Every dimension below is the vehicle's own: a 5.92 m hull, 2.88 m wide over the tracks,
2.68 m to the top of the cupola, on 400 mm track. Building to the real figures is what
makes it recognisable - people know this tank by its stepped nose, the superstructure
overhanging the tracks, eight small wheels in four sprung pairs, and the Schürzen hanging
off a rail down each side. Get those right and the rest is detail.

Object names are the part ids the game drags around, so the glTF carries them straight
through to the assembly board.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

# Preview colours only - the app paints from the vehicle's own palette.
DUNKELGELB = (0.40, 0.34, 0.18)
TRIM_RGB = (0.31, 0.27, 0.15)
GUNMETAL = (0.14, 0.15, 0.16)
TRACK_STEEL = (0.075, 0.072, 0.068)
GLASS = (0.04, 0.08, 0.10)

# ---------------------------------------------------------------------------
# The vehicle, in metres
# ---------------------------------------------------------------------------

HULL_REAR = -2.96
HULL_NOSE = 2.96
FLOOR = 0.40
HULL_TOP = 1.08          # top of the lower hull, where the fenders run
ROOF = 1.75              # superstructure roof
TURRET_TOP = 2.25

HULL_HALF = 1.10         # lower hull, inside the tracks
BODY_HALF = 1.44         # superstructure, overhanging them
TRACK_IN = 1.10
TRACK_OUT = 1.50
SKIRT_Y = 1.60

SPROCKET = (2.44, 0.62, 0.32)
IDLER = (-2.52, 0.52, 0.30)
WHEEL_R = 0.235
BOGIES = (-1.74, -0.58, 0.58, 1.74)      # centres of the four sprung pairs
WHEEL_GAP = 0.27                          # each wheel this far from its bogie centre

SUPER_FRONT = 2.28       # driver's plate; the glacis shelf lies ahead of it
SUPER_REAR = -2.10
TURRET_FRONT = 1.10
TURRET_REAR = -1.05
TURRET_HALF = 0.88


def hull(paint, metal, trim):
    """Lower hull: sloped nose, vertical sides, flat engine deck."""
    parts = []
    profile = [
        (HULL_REAR, FLOOR),
        (2.05, FLOOR),
        (HULL_NOSE, 0.78),          # lower nose plate, sloped up and forward
        (HULL_NOSE, HULL_TOP),
        (HULL_REAR, HULL_TOP),
    ]
    parts.append(wf.finish(wf.prism("hull", profile, -HULL_HALF, HULL_HALF, paint), 0.03, 3))

    # Final drive housings: the bulges either side of the nose that carry the sprockets.
    for sign in (1, -1):
        housing = wf.cylinder("finaldrive", (SPROCKET[0], sign * (HULL_HALF - 0.04),
                                             SPROCKET[1]), 0.30, 0.16, "Y", 28, paint)
        parts.append(wf.finish(housing, 0.02, 3))

    # Towing eyes, front and rear.
    for x in (HULL_NOSE - 0.06, HULL_REAR + 0.06):
        for y in (0.66, -0.66):
            eye = wf.box("towhook", (x, y, 0.60), (0.16, 0.09, 0.22), metal)
            hole = wf.cylinder("cut", (x, y, 0.62), 0.05, 0.20, "Y", 16)
            wf.cut(eye, hole)
            parts.append(wf.finish(eye, 0.012, 2))

    # Rear plate fittings: the jack and its wooden block.
    parts.append(wf.finish(
        wf.box("jack", (HULL_REAR - 0.09, -0.70, 0.86), (0.14, 0.16, 0.56), metal), 0.012, 2))
    parts.append(wf.finish(
        wf.box("jackblock", (HULL_REAR - 0.09, 0.70, 0.70), (0.14, 0.22, 0.22), trim), 0.012, 2))

    return wf.join("hull", parts)


def upper(paint, glass, metal, trim, dark):
    """Superstructure, glacis shelf, fenders and the stowage they carry."""
    parts = []

    # The fighting compartment, overhanging the tracks, with the rear sloping down onto
    # the engine deck.
    profile = [
        (SUPER_REAR - 0.30, HULL_TOP),
        (SUPER_FRONT, HULL_TOP),
        (SUPER_FRONT, ROOF),
        (SUPER_REAR, ROOF),
    ]
    body = wf.prism("upper", profile, -BODY_HALF, BODY_HALF, paint)

    # Driver's visor on the left, ball mount aperture on the right.
    wf.cut(body, wf.box("cut", (SUPER_FRONT, 0.56, 1.47), (0.24, 0.36, 0.17)))
    wf.cut(body, wf.cylinder("cut", (SUPER_FRONT, -0.50, 1.44), 0.17, 0.30, "X", 24))
    parts.append(wf.finish(body, 0.025, 3))

    # Visor block and its armoured surround.
    parts.append(wf.finish(
        wf.box("visor", (SUPER_FRONT - 0.02, 0.56, 1.47), (0.12, 0.40, 0.21), paint), 0.012, 2))
    parts.append(wf.finish(
        wf.box("visorslit", (SUPER_FRONT + 0.05, 0.56, 1.47), (0.04, 0.30, 0.05), glass), 0.008, 2))

    # Engine deck, its grilles and hatches.
    deck = wf.box("deck", ((SUPER_REAR - 0.30 + HULL_REAR) * 0.5, 0,
                           HULL_TOP - 0.01), (abs(HULL_REAR - SUPER_REAR + 0.30), 2 * BODY_HALF, 0.06), paint)
    parts.append(wf.finish(deck, 0.02, 3))
    for y in (0.62, -0.62):
        grille = wf.box("grille", (-2.42, y, HULL_TOP + 0.03), (0.66, 0.52, 0.05), metal)
        parts.append(wf.finish(grille, 0.01, 2))
        for i in range(7):
            parts.append(wf.finish(
                wf.box("louvre", (-2.72 + i * 0.10, y, HULL_TOP + 0.06),
                       (0.03, 0.50, 0.03), metal), 0.004, 1))

    # Roof hatches over the driver and radio operator.
    for y in (0.56, -0.50):
        lid = wf.box("roofhatch", (2.00, y, ROOF + 0.01), (0.46, 0.44, 0.06), paint)
        parts.append(wf.finish(lid, 0.015, 2))
        parts.append(wf.finish(
            wf.box("hatchgrip", (2.00, y, ROOF + 0.06), (0.16, 0.04, 0.04), metal), 0.006, 2))

    # Fenders, running the length of the hull outside the tracks.
    for sign in (1, -1):
        fender = wf.box("fender", (0.0, sign * ((TRACK_IN + SKIRT_Y) * 0.5), HULL_TOP + 0.02),
                        (6.10, SKIRT_Y - TRACK_IN, 0.035), trim)
        parts.append(wf.finish(fender, 0.008, 2))
        # Turned-down lip at each end, so the fender reads as pressed steel.
        for x in (2.98, -2.98):
            parts.append(wf.finish(
                wf.box("fenderlip", (x, sign * ((TRACK_IN + SKIRT_Y) * 0.5), HULL_TOP - 0.05),
                       (0.12, SKIRT_Y - TRACK_IN, 0.14), trim), 0.008, 2))

    # Spare track links laid on the glacis shelf - standard practice, and the first
    # thing that tells you a tank has been in the field.
    for i in range(6):
        parts.append(wf.finish(
            wf.box("sparelink", (2.42 + (i % 3) * 0.17, -0.55 + (i // 3) * 0.62,
                                 HULL_TOP + 0.06), (0.15, 0.44, 0.07), dark), 0.01, 2))

    # Pioneer tools along the left fender.
    parts.append(wf.finish(
        wf.box("shovel", (-0.55, 1.34, HULL_TOP + 0.07), (0.96, 0.10, 0.05), metal), 0.008, 2))
    parts.append(wf.finish(
        wf.box("axe", (0.62, 1.34, HULL_TOP + 0.07), (0.70, 0.08, 0.05), metal), 0.008, 2))
    parts.append(wf.finish(
        wf.cylinder("towcable", (0.2, 1.30, HULL_TOP + 0.06), 0.035, 3.4, "X", 12, dark), 0.01, 2))

    # Notek convoy light on the left fender, and the antenna base behind it.
    parts.append(wf.finish(
        wf.cylinder("notek", (2.62, 1.30, HULL_TOP + 0.16), 0.09, 0.18, "Z", 20, metal), 0.012, 2))
    parts.append(wf.finish(
        wf.cone("antenna", (-1.60, 1.34, ROOF + 0.10), 0.020, 0.006, 1.8, "Z", 10, metal), 0.004, 2))

    return wf.join("upper", parts)


def turret(paint, metal, glass):
    """Turret box with inward-sloping sides, side hatches and the rear stowage bin."""
    parts = []

    # Sides slope in slightly toward the roof, and the front narrows to the mantlet.
    top_half = TURRET_HALF - 0.08
    shell = wf.prism(
        "turret",
        [(TURRET_REAR, ROOF), (TURRET_FRONT, ROOF), (TURRET_FRONT, 2.02),
         (TURRET_FRONT - 0.10, TURRET_TOP), (TURRET_REAR + 0.06, TURRET_TOP)],
        -TURRET_HALF, TURRET_HALF, paint,
    )
    # Taper the upper half by cutting the corners away.
    for sign in (1, -1):
        wedge = wf.box("cut", (0, sign * (TURRET_HALF + 0.10), TURRET_TOP), (2.6, 0.24, 0.7))
        wf.rotate(wedge, x=sign * 9)
        wf.cut(shell, wedge)

    # Pistol ports and the gunner's sight aperture.
    wf.cut(shell, wf.cylinder("cut", (-0.42, TURRET_HALF, 1.97), 0.07, 0.40, "Y", 20))
    wf.cut(shell, wf.box("cut", (TURRET_FRONT, -0.26, 1.99), (0.20, 0.12, 0.09)))
    parts.append(wf.finish(shell, 0.03, 3))

    # Split side hatches, one per side, with their hinges.
    for sign in (1, -1):
        for dx in (-0.20, 0.22):
            door = wf.box("turrethatch", (dx, sign * (TURRET_HALF - 0.01), 1.97),
                          (0.40, 0.04, 0.42), paint)
            parts.append(wf.finish(door, 0.012, 2))
        parts.append(wf.finish(
            wf.box("hinge", (-0.44, sign * (TURRET_HALF - 0.01), 1.97),
                   (0.06, 0.07, 0.40), metal), 0.008, 2))

    # Sight aperture glass.
    parts.append(wf.finish(
        wf.box("sight", (TURRET_FRONT + 0.02, -0.26, 1.99), (0.05, 0.10, 0.07), glass), 0.006, 2))

    return wf.join("turret", parts)


def stowage(trim, metal):
    """The Rommelkiste across the turret rear."""
    parts = []
    bin_ = wf.box("stow", (TURRET_REAR - 0.24, 0, 2.02), (0.44, 1.34, 0.40), trim)
    parts.append(wf.finish(bin_, 0.025, 3))
    lid = wf.box("stowlid", (TURRET_REAR - 0.24, 0, 2.23), (0.48, 1.38, 0.04), trim)
    parts.append(wf.finish(lid, 0.012, 2))
    for y in (0.42, -0.42):
        parts.append(wf.finish(
            wf.box("latch", (TURRET_REAR - 0.46, y, 2.02), (0.04, 0.07, 0.26), metal), 0.006, 2))
    return wf.join("stow", parts)


def cupola(paint, glass, metal):
    """Commander's cupola at the rear of the turret roof, with five vision blocks."""
    parts = []
    cx, cy = -0.70, 0.06
    drum = wf.cylinder("cupola", (cx, cy, TURRET_TOP + 0.11), 0.29, 0.24, "Z", 32, paint)
    parts.append(wf.finish(drum, 0.02, 3))
    lid = wf.cylinder("lid", (cx, cy, TURRET_TOP + 0.25), 0.27, 0.05, "Z", 32, paint)
    parts.append(wf.finish(lid, 0.012, 2))
    for i in range(5):
        a = math.radians(-72 + i * 36)
        block = wf.box("visionblock",
                       (cx + 0.29 * math.cos(a), cy + 0.29 * math.sin(a), TURRET_TOP + 0.13),
                       (0.09, 0.10, 0.10), glass)
        parts.append(wf.rotate(block, z=math.degrees(a)))
    # Loader's hatch and the ventilator dome.
    parts.append(wf.finish(
        wf.box("loaderhatch", (0.30, -0.34, TURRET_TOP + 0.02), (0.44, 0.42, 0.05), paint), 0.012, 2))
    dome = wf.cylinder("vent", (0.34, 0.30, TURRET_TOP + 0.05), 0.11, 0.10, "Z", 20, metal)
    parts.append(wf.finish(dome, 0.01, 2))
    return wf.join("cupola", parts)


def gun(metal, paint, dark):
    """7.5 cm KwK 40 L/48: mantlet, barrel and muzzle brake."""
    parts = []
    mantlet = wf.box("mantlet", (TURRET_FRONT + 0.16, 0, 1.96), (0.34, 0.92, 0.48), paint)
    # The mantlet's outer face is rounded, not a slab.
    parts.append(wf.finish(mantlet, 0.09, 5))

    barrel = wf.cone("barrel", (TURRET_FRONT + 1.78, 0, 1.96), 0.078, 0.058, 2.92, "X", 24, paint)
    parts.append(wf.finish(barrel, 0.008, 2))

    brake = wf.cylinder("brake", (TURRET_FRONT + 3.36, 0, 1.96), 0.105, 0.28, "X", 24, dark)
    wf.cut(brake, wf.cylinder("cut", (TURRET_FRONT + 3.36, 0, 1.96), 0.052, 0.40, "X", 24))
    wf.cut(brake, wf.box("cut", (TURRET_FRONT + 3.36, 0, 1.96), (0.09, 0.30, 0.11)))
    parts.append(wf.finish(brake, 0.01, 2))

    return wf.join("gun", parts)


def machine_gun(metal, paint):
    """Bow MG 34 in its Kugelblende ball mount."""
    parts = [
        wf.finish(wf.sphere("ballmount", (SUPER_FRONT - 0.02, -0.50, 1.44), 0.19, 24, paint), 0.01, 2),
        wf.finish(wf.cone("mg", (SUPER_FRONT + 0.30, -0.50, 1.44), 0.045, 0.026, 0.58, "X", 18, metal), 0.006, 2),
    ]
    return wf.join("mg", parts)


def running_gear(metal, dark):
    """Sprocket, idler, four sprung bogies and the return rollers - one side, mirrored."""
    parts = []
    y = (TRACK_IN + TRACK_OUT) * 0.5

    sx, sz, sr = SPROCKET
    parts.append(wf.cylinder("sprocket", (sx, y, sz), sr - 0.05, 0.24, "Y", 22, metal))
    for i in range(16):
        a = 2 * math.pi * i / 16
        tooth = wf.rotate(
            wf.box("tooth", (sx + sr * math.cos(a), y, sz + sr * math.sin(a)),
                   (0.075, 0.20, 0.075), metal),
            y=-math.degrees(a),
        )
        parts.append(tooth)

    ix, iz, ir = IDLER
    parts.append(wf.cylinder("idler", (ix, y, iz), ir, 0.22, "Y", 22, metal))
    parts.append(wf.cylinder("idlerhub", (ix, y - 0.02, iz), ir * 0.42, 0.26, "Y", 14, metal))

    # Four bogies of two wheels, each on its own leaf spring.
    for bx in BOGIES:
        parts += wf.leaf_bogie("bogie", bx, y - 0.20, WHEEL_R + 0.06, 0.62, metal)
        for dx in (-WHEEL_GAP, WHEEL_GAP):
            x = bx + dx
            parts.append(wf.cylinder("roadwheel", (x, y, WHEEL_R), WHEEL_R, 0.24, "Y", 20, dark))
            parts.append(wf.cylinder("wheelrim", (x, y + 0.01, WHEEL_R), WHEEL_R * 0.56, 0.26, "Y", 14, metal))
            parts.append(wf.cylinder("wheelhub", (x, y + 0.03, WHEEL_R), 0.055, 0.28, "Y", 10, metal))

    for i in range(4):
        x = -1.55 + i * 1.00
        parts.append(wf.cylinder("roller", (x, y - 0.08, HULL_TOP - 0.06), 0.095, 0.14, "Y", 14, metal))

    for p in parts:
        wf.finish(p, 0.010, 1)
    return wf.mirror_y(wf.join("wheels", parts))


def track_path():
    """Closed run round sprocket, idler, ground and return, in the x/z plane."""
    pts = []
    sx, sz, sr = SPROCKET
    ix, iz, ir = IDLER
    rs = sr + 0.055
    ri = ir + 0.055
    pts.append((sx, 0.052))
    pts.append((ix, 0.052))
    for i in range(1, 10):
        a = math.radians(-90 - i * (180 / 10))
        pts.append((ix + ri * math.cos(a), iz + ri * math.sin(a)))
    pts.append((ix, HULL_TOP - 0.05))
    pts.append((sx, HULL_TOP - 0.05))
    for i in range(1, 10):
        a = math.radians(90 - i * (180 / 10))
        pts.append((sx + rs * math.cos(a), sz + rs * math.sin(a)))
    return pts


def tracks(dark):
    run = wf.track_run("track", track_path(), (0.160, TRACK_OUT - TRACK_IN, 0.050), dark)
    run.location.y = (TRACK_IN + TRACK_OUT) * 0.5
    return wf.mirror_y(run)


def skirts(trim, metal):
    """Five Schürzen plates a side, hanging off a rail."""
    parts = []
    for i in range(5):
        x = -2.02 + i * 1.02
        plate = wf.box("schurz", (x, SKIRT_Y, 1.10), (0.96, 0.018, 0.64), trim)
        parts.append(wf.finish(plate, 0.006, 2))
        # The hooks the plate hangs on.
        for dx in (-0.30, 0.30):
            parts.append(wf.finish(
                wf.box("hook", (x + dx, SKIRT_Y - 0.03, 1.44), (0.05, 0.05, 0.10), metal), 0.004, 1))
    parts.append(wf.finish(
        wf.box("skirtrail", (0.0, SKIRT_Y - 0.05, 1.45), (5.4, 0.045, 0.045), metal), 0.01, 2))
    for x in (-2.2, -0.7, 0.8, 2.3):
        parts.append(wf.finish(
            wf.box("stay", (x, SKIRT_Y - 0.14, 1.36), (0.05, 0.22, 0.05), metal), 0.006, 2))
    return wf.mirror_y(wf.join("skirt", parts))


def turret_skirts(trim, metal):
    """
    Turret Schürzen: plates down both sides and across the rear, hung from a frame that
    stands clear of the turret roof. The frame is bar stock, not a lid - a plate over the
    roof would bury the cupola and the hatches, which is exactly what it must not do.
    """
    parts = []
    frame_z = 2.32
    left, right = 1.04, -1.04
    back, front = TURRET_REAR - 0.62, TURRET_FRONT - 0.10

    # Frame: one bar down each side, one across the back, and a stub each side at the
    # front so the plates have something to hang from ahead of the mantlet.
    for y in (left, right):
        parts.append(wf.finish(
            wf.box("t_rail", ((back + front) * 0.5, y, frame_z),
                   (front - back, 0.04, 0.04), metal), 0.008, 2))
    parts.append(wf.finish(
        wf.box("t_rail_rear", (back, 0, frame_z), (0.04, 2.08, 0.04), metal), 0.008, 2))
    # Stays down to the turret roof.
    for x in (back + 0.08, (back + front) * 0.5, front - 0.08):
        for y in (left, right):
            parts.append(wf.finish(
                wf.box("t_stay", (x, y, (frame_z + TURRET_TOP) * 0.5),
                       (0.035, 0.035, frame_z - TURRET_TOP), metal), 0.006, 2))

    # Side plates, with a gap where the mantlet sweeps.
    for x, length in ((back + 0.44, 0.78), (back + 1.34, 0.94)):
        for y in (left, right):
            parts.append(wf.finish(
                wf.box("schurz_t", (x, y, 2.00), (length, 0.018, 0.60), trim), 0.006, 2))
    # Rear plate.
    parts.append(wf.finish(
        wf.box("schurz_t_rear", (back - 0.01, 0, 2.00), (0.018, 2.04, 0.60), trim), 0.006, 2))

    return wf.join("schurz_t", parts)


def exhaust(dark, metal):
    """Muffler across the rear plate, with its pipe."""
    parts = [
        wf.finish(wf.cylinder("exhaust", (HULL_REAR - 0.16, 0, 0.86), 0.135, 1.30, "Y", 28, dark), 0.02, 3),
        wf.finish(wf.cylinder("pipe", (HULL_REAR - 0.16, 0.72, 1.10), 0.055, 0.54, "Z", 18, dark), 0.01, 2),
        wf.finish(wf.box("bracket", (HULL_REAR - 0.05, 0, 0.86), (0.12, 0.30, 0.10), metal), 0.008, 2),
    ]
    return wf.join("exhaust", parts)


# Standard 1943-45 field scheme: Dunkelgelb from the factory, with Olivgrun and
# Rotbraun sprayed over it by the crew. Held as multipliers rather than colours, because
# the app paints the vehicle from its own palette - these only say how the patches sit
# against whatever base colour it picks.
OLIVGRUN = (0.44, 0.62, 0.50)
ROTBRAUN = (0.56, 0.35, 0.38)


# Every object the export carries, in the order the game names them.
PARTS = ("hull", "upper", "turret", "stow", "cupola", "gun", "mg",
         "wheels", "track", "skirt", "schurz_t", "exhaust")


def weathering():
    """
    Sprays camouflage on, then mud and worn paint.

    Geometry alone still reads as a toy, because real armour is never one flat colour:
    it is patched paint, dust thrown up the sides, and bare metal where boots and hatches
    have rubbed it back. This is the pass that does more for the look than any further
    plate work would.
    """
    painted = ("hull", "upper", "turret", "cupola", "stow", "skirt", "schurz_t", "mg")

    for name in painted:
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        # Camouflage needs somewhere to live. A hull side is four corners until this runs.
        wf.densify(obj, max_edge=0.34)
        wf.weather(obj, camo=(OLIVGRUN, ROTBRAUN), dirt=0.46, wear=0.24,
                   scale=0.95, seed=4, ground=FLOOR - 0.30, top=TURRET_TOP)

    # Running gear is filthy and unpainted rather than camouflaged, and is dense enough
    # already - subdividing the track would double the whole model for nothing.
    for name, dirt in (("wheels", 0.62), ("track", 0.55), ("exhaust", 0.40)):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=dirt, wear=0.30,
                       scale=1.0, seed=9, ground=0.0, top=1.5)

    # The barrel is painted but sits clear of the mud, and wears at the muzzle.
    obj = bpy.data.objects.get("gun")
    if obj is not None:
        wf.densify(obj, max_edge=0.40)
        wf.weather(obj, camo=(OLIVGRUN, ROTBRAUN), dirt=0.10, wear=0.26,
                   scale=0.95, seed=4, ground=FLOOR, top=TURRET_TOP)


# The app's BODY colour for this vehicle. Markings are worked back from it, because the
# vertex colour multiplies the palette rather than replacing it.
PALETTE_BODY = (154 / 255, 135 / 255, 86 / 255)
MARK_WHITE = (0.88, 0.86, 0.77)
MARK_BLACK = (0.10, 0.10, 0.11)


def markings(trim):
    """
    Balkenkreuze on the Schurzen.

    Run after the weathering, and joined in afterwards, so the camouflage pass does not
    spray over them. A cross is what actually identifies the vehicle at a glance - the
    shape alone could be half a dozen tanks, but nobody mistakes the marking.
    """
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    black = wf.tint_for(MARK_BLACK, PALETTE_BODY)

    def pair(host, x, z, arm, y_out):
        """One cross a side: white plan shape, with a smaller black one laid on it."""
        added = []
        for y in (y_out, -y_out):
            sign = 1.0 if y > 0 else -1.0
            base = wf.cross("kreuz", (x, y + sign * 0.004, z), arm, arm * 0.40, 0.008, trim)
            added.append(wf.flat_tint(base, white))
            # The black is well inside the white, so the outline reads at a distance.
            inner = wf.cross("kreuz_in", (x, y + sign * 0.009, z),
                             arm * 0.70, arm * 0.22, 0.008, trim)
            added.append(wf.flat_tint(inner, black))
        obj = bpy.data.objects.get(host)
        if obj is not None:
            wf.join(host, [obj] + added)

    # Hull Schurzen, third plate back - where the crews actually painted them.
    pair("skirt", 0.02, 1.12, 0.27, SKIRT_Y + 0.010)
    # Turret Schurzen, rear side plate.
    pair("schurz_t", TURRET_REAR + 0.44, 2.00, 0.21, 1.04 + 0.010)


def build():
    wf.reset()
    paint = wf.role("BODY", DUNKELGELB, roughness=0.74)
    metal = wf.role("METAL", GUNMETAL, roughness=0.40, metallic=0.80)
    dark = wf.role("DARK", TRACK_STEEL, roughness=0.60, metallic=0.40)
    trim = wf.role("TRIM", TRIM_RGB, roughness=0.72)
    glass = wf.role("GLASS", GLASS, roughness=0.22, metallic=0.30)

    hull(paint, metal, trim)
    upper(paint, glass, metal, trim, dark)
    turret(paint, metal, glass)
    stowage(trim, metal)
    cupola(paint, glass, metal)
    gun(metal, paint, dark)
    machine_gun(metal, paint)
    running_gear(metal, dark)
    tracks(dark)
    skirts(trim, metal)
    turret_skirts(trim, metal)
    exhaust(dark, metal)
    weathering()
    markings(trim)
    # Last: the markings have to exist before their edges can be shaded, and the
    # occlusion multiplies into the tint everything else has already written.
    wf.occlude(PARTS)


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(6.0)
        wf.camera(azimuth=36, elevation=17)
        wf.render(args[0], samples=24)
    if len(args) > 1:
        wf.export_glb(args[1])
