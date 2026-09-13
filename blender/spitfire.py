"""
Supermarine Spitfire Mk Vb, at full size.

One shape carries this aeroplane: the elliptical wing. It was chosen to get the thinnest
possible section round four cannon and still hold the lift, it was a misery to build, and
it is the reason a Spitfire is recognisable in silhouette at a distance no other fighter
of its generation is. Everything here is arranged so that planform is right first.

Object names are the part ids the game drags around.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


# Preview colours only; the app paints from the vehicle's palette.
DARK_GREEN = (0.13, 0.15, 0.09)
STEEL = (0.34, 0.35, 0.36)
TYRE = (0.07, 0.07, 0.08)
GLASS = (0.26, 0.34, 0.36)
SKY = (0.62, 0.66, 0.52)

# ---------------------------------------------------------------------------
# Dimensions, in metres. Length 9.12, span 11.23, height 3.86.
# x = 0 is the middle of the aeroplane, +x toward the spinner. z = 0 is the thrust line.
# ---------------------------------------------------------------------------
NOSE = 2.53              # where the cowling takes over from the fuselage
COWL_TIP = 3.99
SPINNER = 4.27
TAIL = -4.17
RUDDER_TAIL = -4.56

HALF_SPAN = 5.61
ROOT_LE = 1.62
ROOT_CHORD = 2.48
DIHEDRAL = 6.0

WING_Z = -0.30           # a low-wing monoplane: the root sits at the fuselage floor
TAILPLANE_HALF = 1.60
FIN_TOP = 1.42

PROP_R = 1.64


def fuselage(paint, metal, glass):
    """
    Monocoque fuselage: an oval section that swells over the wing and tapers to a knife
    edge at the sternpost, with the spine faired in behind the cockpit.
    """
    parts = []
    # (x, half-width, half-height, centreline offset)
    stations = [
        (TAIL, 0.05, 0.16, 0.06),
        (-3.60, 0.18, 0.30, 0.05),
        (-2.60, 0.33, 0.46, 0.02),
        (-1.40, 0.45, 0.60, 0.00),
        (-0.30, 0.50, 0.66, -0.01),
        (0.70, 0.49, 0.64, -0.02),
        (1.60, 0.45, 0.58, -0.02),
        (2.53, 0.40, 0.50, -0.02),
    ]
    sections = [
        (x, wf.oval(hy, hz, 18, centre_z=cz, flatten_bottom=0.22))
        for x, hy, hz, cz in stations
    ]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint), 0.010, 2, smooth_angle=52))

    # Spine behind the cockpit, which is what gives the Mk V its humped back.
    spine = [
        (-3.40, wf.oval(0.10, 0.05, 10, centre_z=0.30)),
        (-2.40, wf.oval(0.16, 0.10, 10, centre_z=0.40)),
        (-1.40, wf.oval(0.22, 0.16, 10, centre_z=0.52)),
        (-0.70, wf.oval(0.26, 0.20, 10, centre_z=0.58)),
    ]
    parts.append(wf.finish(wf.loft("spine", spine, paint), 0.008, 2, smooth_angle=52))

    # Sternpost and the aerial mast.
    parts.append(wf.finish(
        wf.cone("aerial", (-1.70, 0, 0.96), 0.030, 0.008, 0.62, "Z", 8, metal), 0.004, 1))
    return wf.join("fuselage", parts)


def wing_stations():
    """
    The elliptical planform, as stations from root to tip.

    Shared rather than rebuilt, because the roundels, the cannon and the radiators all
    have to sit *on* this wing - and placing them by eye put them floating half a metre
    off it in every direction.
    """
    stations = wf.ellipse_stations(
        HALF_SPAN, ROOT_LE, ROOT_CHORD, thickness=0.13, steps=12,
        sweep=2.0, dihedral=DIHEDRAL, tip_chord=0.45,
    )
    # Round the tip off instead of running the ellipse out to a point.
    tip = stations[-1]
    stations.append((HALF_SPAN + 0.13, tip[1] + tip[2] * 0.30, tip[2] * 0.44, 0.16,
                     tip[4] + 0.02))
    return [(y, le, c, t, z + WING_Z) for y, le, c, t, z in stations]


def wing_at(y):
    """Leading edge, chord and centreline height of the wing at span |y|."""
    stations = wing_stations()
    span = abs(y)
    for (y0, le0, c0, _, z0), (y1, le1, c1, _, z1) in zip(stations, stations[1:]):
        if span <= y1 or (y1, le1, c1) == (stations[-1][0], stations[-1][1], stations[-1][2]):
            t = 0.0 if y1 <= y0 else min(1.0, max(0.0, (span - y0) / (y1 - y0)))
            return (le0 + (le1 - le0) * t, c0 + (c1 - c0) * t, z0 + (z1 - z0) * t)
    return (stations[-1][1], stations[-1][2], stations[-1][4])


def wing_surface(y, upper=True):
    """Height of the wing skin at span |y|, near the thickest point."""
    _, chord, z = wing_at(y)
    half = 0.13 * chord * 0.5
    return z + (half if upper else -half)


def wings(paint, metal):
    """The elliptical wing, with six degrees of dihedral and a rounded tip."""
    panel = wf.wing("wing", wing_stations(), paint)
    parts = [wf.finish(panel, 0.008, 2, smooth_angle=56)]

    # Wing root fairing, where the wing meets the fuselage.
    fair = [
        (ROOT_LE + 0.20, wf.oval(0.56, 0.18, 12, centre_z=WING_Z + 0.02)),
        (0.40, wf.oval(0.68, 0.26, 12, centre_z=WING_Z + 0.04)),
        (ROOT_LE - ROOT_CHORD - 0.22, wf.oval(0.50, 0.15, 12, centre_z=WING_Z + 0.02)),
    ]
    parts.append(wf.finish(wf.loft("fairing", fair, paint), 0.012, 2, smooth_angle=52))
    return wf.mirror_y(wf.join("wing", parts))


def cowling(paint, metal, dark):
    """Merlin cowling: the long nose, the exhaust stacks and the carburettor intake."""
    parts = []
    sections = [
        (NOSE, wf.oval(0.40, 0.50, 18, centre_z=-0.02)),
        (3.10, wf.oval(0.38, 0.46, 18, centre_z=0.01)),
        (3.62, wf.oval(0.34, 0.40, 18, centre_z=0.02)),
        (COWL_TIP, wf.oval(0.26, 0.30, 18, centre_z=0.02)),
    ]
    parts.append(wf.finish(wf.loft("cowl", sections, paint), 0.010, 2, smooth_angle=52))

    # Six ejector exhaust stubs a side - the sound of the thing.
    for i in range(6):
        x = 2.66 + i * 0.21
        for y in (-0.39, 0.39):
            parts.append(wf.finish(
                wf.rotate(wf.box("exhaust", (x, y, 0.13), (0.15, 0.10, 0.11), dark), y=-14),
                0.012, 2))
    # Carburettor intake under the nose.
    parts.append(wf.finish(
        wf.box("intake", (3.10, 0, -0.46), (1.10, 0.26, 0.20), paint), 0.05, 3))
    return wf.join("cowl", parts)


def propeller(metal, dark):
    """Rotol constant-speed three-blader, 3.28 m across."""
    parts = []
    spinner = [
        (SPINNER - 0.44, wf.oval(0.24, 0.24, 16)),
        (SPINNER - 0.16, wf.oval(0.21, 0.21, 16)),
        (SPINNER + 0.02, wf.oval(0.13, 0.13, 16)),
        (SPINNER + 0.20, wf.oval(0.01, 0.01, 16)),
    ]
    parts.append(wf.finish(wf.loft("spinner", spinner, metal), 0.008, 2, smooth_angle=56))

    for i in range(3):
        blade = wf.wing("blade", [
            (0.16, 0.14, 0.30, 0.22, 0.0),
            (0.60, 0.10, 0.34, 0.14, 0.0),
            (1.10, 0.06, 0.30, 0.10, 0.0),
            (PROP_R, 0.02, 0.14, 0.09, 0.0),
        ], dark)
        wf.bake(blade)
        # Stand it up, twist it, then swing it round the hub.
        blade.location = (SPINNER - 0.26, 0, 0)
        wf.rotate(blade, x=90 + i * 120, y=-24)
        parts.append(wf.finish(blade, 0.006, 2, smooth_angle=56))
    return wf.join("prop", parts)


def canopy(glass, paint):
    """Sliding hood, windscreen and the armoured glass panel in front of it."""
    parts = []
    sections = [
        (-1.40, wf.oval(0.20, 0.10, 14, centre_z=0.62)),
        (-0.90, wf.oval(0.34, 0.24, 14, centre_z=0.62)),
        (-0.30, wf.oval(0.38, 0.30, 14, centre_z=0.62)),
        (0.34, wf.oval(0.36, 0.26, 14, centre_z=0.62)),
        (0.64, wf.oval(0.30, 0.18, 14, centre_z=0.62)),
    ]
    parts.append(wf.finish(wf.loft("canopy", sections, glass), 0.010, 2, smooth_angle=56))
    # Windscreen frame and its flat armoured centre panel.
    parts.append(wf.finish(
        wf.rotate(wf.box("screen", (0.60, 0, 0.80), (0.26, 0.44, 0.06), paint), y=34),
        0.008, 2))
    for y in (-0.30, 0.30):
        parts.append(wf.finish(
            wf.rotate(wf.box("frame", (0.46, y, 0.76), (0.30, 0.03, 0.30), paint), y=34),
            0.005, 1))
    return wf.join("canopy", parts)


def tailplane(paint):
    """Tailplane and elevators."""
    stations = [
        (0.00, -3.10, 1.16, 0.11, 0.10),
        (0.70, -3.16, 0.98, 0.11, 0.10),
        (1.24, -3.24, 0.76, 0.11, 0.10),
        (TAILPLANE_HALF, -3.34, 0.34, 0.12, 0.10),
    ]
    return wf.mirror_y(wf.finish(wf.wing("tail", stations, paint), 0.008, 2, smooth_angle=56))


def fin(paint, dark):
    """Fin and rudder."""
    shape = wf.fin("fin", [
        (0.02, -2.98, 1.44, 0.12, 0.0),
        (0.52, -3.12, 1.24, 0.11, 0.0),
        (1.00, -3.34, 0.96, 0.10, 0.0),
        (FIN_TOP, -3.60, 0.56, 0.10, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.008, 2, smooth_angle=56)]
    parts.append(wf.finish(
        wf.box("rudderpost", (-3.98, 0, 0.62), (0.05, 0.06, 1.14), dark), 0.006, 1))
    return wf.join("fin", parts)


def gear(metal, dark, paint):
    """Main undercarriage: the narrow track that made a Spitfire awkward on the ground."""
    parts = []
    for y in (-1.02, 1.02):
        sign = 1 if y > 0 else -1
        under = wing_surface(y, upper=False)
        leg = wf.rotate(wf.cylinder("leg", (1.02, y, under - 0.44), 0.075, 1.00, "Z", 12, metal),
                        y=-12, x=sign * 8)
        parts.append(wf.finish(leg, 0.008, 2))
        parts.append(wf.finish(
            wf.cylinder("tyre", (0.86, y + sign * 0.08, under - 0.98), 0.34, 0.17, "Y", 20, dark),
            0.03, 3))
        parts.append(wf.finish(
            wf.cylinder("hub", (0.86, y + sign * 0.08, under - 0.98), 0.13, 0.20, "Y", 12, metal),
            0.01, 2))
        # The door that hangs off the leg.
        parts.append(wf.finish(
            wf.rotate(wf.box("gdoor", (1.14, y - sign * 0.10, under - 0.46), (0.50, 0.04, 0.86), paint),
                      y=-12), 0.006, 1))
    return wf.join("gear", parts)


def tailwheel(metal, dark):
    parts = [
        wf.finish(wf.cylinder("leg", (-3.86, 0, -0.18), 0.045, 0.34, "Z", 10, metal), 0.006, 1),
        wf.finish(wf.cylinder("tyre", (-3.90, 0, -0.36), 0.14, 0.09, "Y", 16, dark), 0.02, 2),
    ]
    return wf.join("tailwheel", parts)


def guns(metal, dark):
    """Two 20 mm Hispano cannon and four .303 Brownings - the 'b' wing."""
    parts = []
    for sign in (-1, 1):
        y = sign * 1.94
        le, chord, z = wing_at(y)
        # The cannon stands out ahead of the leading edge, on the wing's own centreline.
        parts.append(wf.finish(
            wf.cone("cannon", (le + 0.46, y, z), 0.045, 0.036, 1.10, "X", 14, dark),
            0.005, 2))
        # The blister over its drum magazine, on top of the wing behind the spar.
        parts.append(wf.finish(
            wf.cylinder("blister", (le - 0.60, y, wing_surface(y) - 0.02), 0.12, 0.66,
                        "X", 14, metal), 0.04, 3))
        for dy in (0.62, 0.98):
            gy = y + sign * dy
            gle, _, gz = wing_at(gy)
            parts.append(wf.finish(
                wf.cone("browning", (gle + 0.08, gy, gz), 0.022, 0.018, 0.34, "X", 10, dark),
                0.004, 1))
    return wf.join("guns", parts)


def radiator(trim, dark):
    """
    Radiator under the starboard wing, oil cooler under the port - and they are
    different sizes, which is one of the small asymmetries people notice.
    """
    parts = []
    for y, half in ((-1.30, 0.30), (1.30, 0.20)):
        le, chord, _ = wing_at(y)
        top = wing_surface(y, upper=False)
        front, back = le - chord * 0.20, le - chord * 0.92
        depth = 0.40 if half > 0.25 else 0.30

        def ring(width, drop, lip):
            return [(y - width, top - lip), (y + width, top - lip),
                    (y + width, top - drop), (y - width, top - drop)]

        duct = wf.loft("rad", [
            (front, ring(half * 0.86, depth * 0.55, 0.02)),
            (front - chord * 0.22, ring(half, depth, 0.0)),
            (back + 0.20, ring(half, depth * 0.94, 0.0)),
            (back, ring(half * 0.80, depth * 0.50, 0.04)),
        ], trim)
        parts.append(wf.finish(duct, 0.030, 3))
        # The matrix showing through the intake.
        parts.append(wf.finish(
            wf.box("matrix", (front - 0.03, y, top - depth * 0.34),
                   (0.04, half * 1.5, depth * 0.5), dark), 0.006, 1))
    return wf.join("radiator", parts)


# ---------------------------------------------------------------------------
# Surface
# ---------------------------------------------------------------------------

# Day Fighter Scheme, 1941: Dark Green and Ocean Grey above, Medium Sea Grey below.
#
# All three are held as multipliers over the app's BODY colour, which is a mid olive -
# so the green *darkens* it and the greys lighten and cool it. Working them out from the
# wrong base is how the first attempt came out uniformly pale: every multiplier was
# above one, and the aeroplane ended up the colour of its own undersides.
PALETTE_BODY = (0x5C / 255, 0x6B / 255, 0x4C / 255)
DARK_GREEN_PAINT = (0.66, 0.72, 0.60)
OCEAN_GREY = (1.16, 1.07, 1.54)
SEA_GREY = (1.72, 1.55, 2.21)
MARK_BLUE = (0.09, 0.14, 0.34)
MARK_RED = (0.48, 0.09, 0.10)
MARK_WHITE = (0.86, 0.86, 0.84)

PARTS = ("fuselage", "wing", "cowl", "prop", "canopy", "tail", "fin",
         "gear", "tailwheel", "guns", "radiator", "roundel", "stripe")


def weathering():
    """
    Disruptive camouflage on top, one flat grey underneath.

    An aeroplane wears differently from a tank: no mud, but exhaust staining down the
    fuselage, gun gas along the wing, and the paint worn back to metal where ground crew
    and pilots put their feet and hands.
    """
    upper = ("fuselage", "wing", "cowl", "canopy", "tail", "fin", "guns")
    for name in upper:
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.30)
        # Ocean Grey over Dark Green, in the broad sweeping pattern of the day scheme,
        # and the grey undersides where the noise happens to fall low.
        # Two colours in broad sweeping bands, which is how the scheme was sprayed -
        # very little of the base is left showing between them.
        wf.weather(obj, camo=(OCEAN_GREY, DARK_GREEN_PAINT), cover=(0.44, 0.44),
                   dirt=0.0, wear=0.24, scale=2.6, seed=5, ground=0.0, top=1.0)

    for name in ("gear", "tailwheel", "radiator", "prop"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.30, wear=0.34, seed=8, ground=-1.6, top=0.4)


def underside():
    """
    Medium Sea Grey below the waterline of the camouflage.

    Applied after the disruptive pattern, and by height rather than by noise: the
    dividing line on a fighter is a hard one drawn along the fuselage side, not a
    boundary that wanders.
    """
    for name in ("fuselage", "wing", "cowl", "tail", "guns"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        mesh = obj.data
        col = mesh.color_attributes.get("Col")
        if col is None:
            continue
        bpy.context.view_layer.update()
        to_world = obj.matrix_world
        for i, vert in enumerate(mesh.vertices):
            co = to_world @ vert.co
            facing = (to_world.to_3x3() @ vert.normal).z
            # Anything that faces downward, plus the fuselage sides below the line the
            # RAF actually drew - low on the fuselage, not at the wing.
            under = facing < -0.58 or (co.z < -0.40 and abs(co.y) < 0.55)
            if under:
                r, g, b = (c / wf.TINT_RANGE for c in SEA_GREY)
                col.data[i].color = (r, g, b, 1.0)


def markings(paint):
    """Roundels above and below the wings and on the fuselage, and the sky band."""
    blue = wf.tint_for(MARK_BLUE, PALETTE_BODY)
    red = wf.tint_for(MARK_RED, PALETTE_BODY)
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    sky = wf.tint_for(SKY, PALETTE_BODY)
    added = {"fuselage": [], "wing": []}

    def disc(host, centre, radius, tint, axis, depth=0.014):
        ring = wf.cylinder("roundel", centre, radius, depth, axis, 28, paint)
        added[host].append(wf.flat_tint(ring, tint))

    # Fuselage roundel, both sides. Type A1: blue, white, red - the yellow outer ring the
    # palette has no room for is left off. Cut from the fuselage skin rather than laid on
    # it, so each ring curves round the body instead of standing proud in the middle.
    body = bpy.data.objects.get("fuselage")
    if body is not None:
        for sign in (-1, 1):
            for radius, tint, swell in ((0.40, blue, 1.008), (0.26, white, 1.022),
                                        (0.13, red, 1.036)):
                barrel = wf.cylinder("cut", (-2.25, sign * 0.9, 0.00), radius, 1.4, "Y", 28)
                added["fuselage"].append(wf.flat_tint(
                    wf.decal("roundel", body, barrel, paint,
                             centre=(-2.25, 0.0, 0.00), swell=swell),
                    tint))

    # Wing roundels, above and below, port and starboard - laid on the skin itself.
    for sign in (-1, 1):
        y = sign * 2.90
        le, chord, _ = wing_at(y)
        x = le - chord * 0.46
        for up in (1, -1):
            base = wing_surface(y, upper=up > 0)
            for radius, tint, out in ((0.60, blue, 0.0), (0.39, white, 0.006),
                                      (0.19, red, 0.012)):
                disc("wing", (x, y, base + up * out), radius, tint, "Z")

    for host, items in added.items():
        obj = bpy.data.objects.get(host)
        if obj is not None and items:
            wf.join(host, [obj] + items)

    # Sky band round the rear fuselage - an identification marking every RAF fighter
    # carried from late 1940, and the quickest way to date the aeroplane.
    if body is not None:
        slab = wf.box("cut", (-3.30, 0, 0.02), (0.42, 3.0, 3.0))
        wf.flat_tint(wf.decal("stripe", body, slab, paint,
                              centre=(-3.30, 0.0, 0.02), swell=1.010), sky)


def build():
    wf.reset()
    paint = wf.role("BODY", DARK_GREEN, roughness=0.46)
    metal = wf.role("METAL", STEEL, roughness=0.34, metallic=0.85)
    dark = wf.role("DARK", TYRE, roughness=0.70, metallic=0.30)
    trim = wf.role("TRIM", DARK_GREEN, roughness=0.50)
    glass = wf.role("GLASS", GLASS, roughness=0.10, metallic=0.20)

    fuselage(paint, metal, glass)
    wings(paint, metal)
    cowling(paint, metal, dark)
    propeller(metal, dark)
    canopy(glass, paint)
    tailplane(paint)
    fin(paint, dark)
    gear(metal, dark, paint)
    tailwheel(metal, dark)
    guns(metal, dark)
    radiator(trim, dark)

    weathering()
    underside()
    markings(paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(9.0, floor_z=-1.9)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=24)
    if len(args) > 1:
        wf.export_glb(args[1])
