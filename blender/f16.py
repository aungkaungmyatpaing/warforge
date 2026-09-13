"""
F-16C Fighting Falcon, at full size.

The first production fighter deliberately built unstable: the shape is chosen for lift
and the computer is left to keep it pointing forwards. The blended wing-body, the
strakes running forward from the wing root and the chin intake under the cockpit all
come out of that one decision.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402

GREY = (0.40, 0.43, 0.47)
STEEL = (0.38, 0.39, 0.40)
TYRE = (0.07, 0.07, 0.08)
GLASS = (0.30, 0.36, 0.30)

# Length 15.06 (with probe), span 9.96, height 4.88.
NOSE = 7.53
TAIL = -7.53
HALF_SPAN = 4.98
WING_Z = -0.30
ROOT_LE = 0.20
ROOT_CHORD = 4.20
LE_SWEEP = 40.0
FIN_TOP = 3.10


def wing_stations():
    """A cropped delta: 40 degrees on the leading edge, almost none on the trailing."""
    out = []
    for y, frac in ((0.00, 1.00), (1.60, 0.74), (3.20, 0.48), (4.40, 0.28), (HALF_SPAN, 0.17)):
        le = ROOT_LE - y * math.tan(math.radians(LE_SWEEP))
        chord = ROOT_CHORD * frac
        out.append((y, le, chord, 0.055, WING_Z))
    return out


def wing_at(y):
    stations = wing_stations()
    span = abs(y)
    for (y0, le0, c0, _, z0), (y1, le1, c1, _, z1) in zip(stations, stations[1:]):
        if span <= y1:
            t = (span - y0) / max(1e-6, y1 - y0)
            return (le0 + (le1 - le0) * t, c0 + (c1 - c0) * t, z0 + (z1 - z0) * t)
    last = stations[-1]
    return (last[1], last[2], last[4])


def fuselage(paint, metal, glass):
    """
    Blended wing-body: there is no line where the fuselage stops and the wing starts,
    which is the whole point and the reason the sections have to be lofted rather than
    assembled.
    """
    parts = []
    stations = [
        (TAIL, 0.52, 0.52, 0.12),
        (-6.20, 0.62, 0.66, 0.10),
        (-4.60, 0.78, 0.80, 0.06),
        (-2.80, 0.94, 0.88, 0.00),
        (-1.00, 1.06, 0.92, -0.04),
        (0.80, 1.04, 0.94, -0.06),
        (2.40, 0.88, 0.90, -0.04),
        (3.80, 0.70, 0.76, 0.00),
        (5.00, 0.54, 0.58, 0.04),
        (6.20, 0.34, 0.36, 0.06),
        (NOSE - 0.40, 0.10, 0.10, 0.06),
    ]
    sections = [(x, wf.oval(hy, hz, 22, centre_z=cz, flatten_bottom=0.18))
                for x, hy, hz, cz in stations]
    parts.append(wf.finish(wf.loft("fuselage", sections, paint), 0.030, 3, smooth_angle=56))
    # Dorsal spine behind the canopy.
    spine = [
        (0.60, wf.oval(0.30, 0.10, 10, centre_z=0.86)),
        (-1.60, wf.oval(0.34, 0.22, 10, centre_z=0.84)),
        (-4.00, wf.oval(0.30, 0.24, 10, centre_z=0.76)),
    ]
    parts.append(wf.finish(wf.loft("spine", spine, paint), 0.03, 2, smooth_angle=52))
    return wf.join("fuselage", parts)


def radome(paint, dark, metal):
    parts = [wf.finish(
        wf.loft("radome", [
            (5.00, wf.oval(0.54, 0.58, 20, centre_z=0.04)),
            (6.10, wf.oval(0.40, 0.42, 20, centre_z=0.06)),
            (6.90, wf.oval(0.22, 0.23, 20, centre_z=0.06)),
            (NOSE - 0.34, wf.oval(0.05, 0.05, 20, centre_z=0.06)),
        ], dark), 0.02, 3, smooth_angle=58)]
    # Pitot probe, on the nose where the radome ends.
    parts.append(wf.finish(
        wf.cone("probe", (NOSE + 0.06, 0, 0.06), 0.030, 0.010, 0.70, "X", 8, metal), 0.004, 1))
    return wf.join("radome", parts)


def intake(paint, metal, dark):
    """The chin intake, hung under the forward fuselage on its own splitter plate."""
    parts = []
    body = [
        (3.30, [(-0.62, -0.46), (0.62, -0.46), (0.56, -1.12), (-0.56, -1.12)]),
        (2.20, [(-0.68, -0.44), (0.68, -0.44), (0.62, -1.18), (-0.62, -1.18)]),
        (0.20, [(-0.70, -0.40), (0.70, -0.40), (0.66, -1.16), (-0.66, -1.16)]),
        (-1.60, [(-0.62, -0.34), (0.62, -0.34), (0.58, -1.00), (-0.58, -1.00)]),
    ]
    parts.append(wf.finish(wf.loft("intake", body, paint), 0.16, 4, smooth_angle=50))
    # The mouth, and the dark duct behind it.
    parts.append(wf.finish(
        wf.box("lip", (3.34, 0, -0.79), (0.16, 1.30, 0.72), paint), 0.10, 3))
    parts.append(wf.finish(
        wf.box("duct", (3.10, 0, -0.79), (0.30, 1.06, 0.56), dark), 0.06, 2))
    return wf.join("intake", parts)


def lerx(paint):
    """
    Leading-edge root extensions: the strakes that run forward along the fuselage and
    shed a vortex over the wing at high angles of attack.
    """
    parts = []
    for sign in (-1, 1):
        strake = [
            (ROOT_LE + 0.10, [(sign * 0.94, -0.16), (sign * 1.10, -0.16),
                              (sign * 1.10, -0.02), (sign * 0.94, -0.02)]),
            (1.60, [(sign * 0.88, -0.12), (sign * 1.00, -0.12),
                    (sign * 1.00, 0.00), (sign * 0.88, 0.00)]),
            (3.00, [(sign * 0.70, -0.08), (sign * 0.78, -0.08),
                    (sign * 0.78, 0.02), (sign * 0.70, 0.02)]),
            (4.10, [(sign * 0.50, -0.05), (sign * 0.53, -0.05),
                    (sign * 0.53, 0.02), (sign * 0.50, 0.02)]),
        ]
        parts.append(wf.finish(wf.loft("lerx", strake, paint), 0.03, 3, smooth_angle=54))
    return wf.join("lerx", parts)


def wings(paint):
    return wf.mirror_y(wf.finish(wf.wing("wing", wing_stations(), paint), 0.008, 2,
                                 smooth_angle=58))


def tailplane(paint):
    """All-moving slab tailplanes, well aft and below the wing line."""
    stations = [
        (0.00, -4.20, 2.40, 0.06, -0.16),
        (0.90, -4.60, 1.90, 0.06, -0.16),
        (1.70, -5.00, 1.40, 0.06, -0.16),
        (2.20, -5.26, 0.90, 0.07, -0.16),
    ]
    return wf.mirror_y(wf.finish(wf.wing("tail", stations, paint), 0.010, 2, smooth_angle=58))


def tailfin(paint, dark, metal):
    shape = wf.fin("fin", [
        (0.60, -2.60, 4.00, 0.07, 0.0),
        (1.50, -3.30, 3.10, 0.06, 0.0),
        (2.40, -4.00, 2.20, 0.06, 0.0),
        (FIN_TOP, -4.60, 1.40, 0.07, 0.0),
    ], paint)
    parts = [wf.finish(shape, 0.012, 2, smooth_angle=58)]
    # The ECS inlet at the fin root and the parachute-brake fairing at the base.
    parts.append(wf.finish(
        wf.box("finroot", (-4.60, 0, 0.52), (2.40, 0.30, 0.60), paint), 0.06, 3))
    parts.append(wf.finish(
        wf.box("rwr", (-4.90, 0, FIN_TOP - 0.10), (0.50, 0.16, 0.24), dark), 0.02, 2))
    return wf.join("fin", parts)


def strakes(paint, dark):
    """Ventral fins under the tail, to keep it stable where the fin runs out of authority."""
    parts = []
    for sign in (-1, 1):
        parts.append(wf.finish(
            wf.rotate(wf.box("strake", (-5.20, sign * 0.72, -0.86), (1.70, 0.06, 0.66), paint),
                      x=sign * 26), 0.02, 2))
    return wf.join("strake", parts)


def nozzle(metal, dark):
    parts = [
        wf.finish(wf.cylinder("nozzleouter", (TAIL + 0.44, 0, 0.12), 0.54, 0.70, "X", 24, metal),
                  0.02, 2),
    ]
    petals = wf.cone("petals", (TAIL - 0.16, 0, 0.12), 0.52, 0.44, 0.70, "X", 24, metal)
    parts.append(wf.finish(petals, 0.02, 2))
    parts.append(wf.finish(
        wf.cylinder("jetpipe", (TAIL - 0.30, 0, 0.12), 0.40, 0.30, "X", 20, dark), 0.02, 2))
    return wf.join("nozzle", parts)


def canopy(glass, paint, metal):
    """The one-piece bubble, with no forward frame at all - the whole point of it."""
    parts = []
    hood = [
        (4.00, wf.oval(0.16, 0.12, 16, centre_z=0.36)),
        (3.30, wf.oval(0.42, 0.36, 16, centre_z=0.48)),
        (2.40, wf.oval(0.54, 0.52, 16, centre_z=0.56)),
        (1.20, wf.oval(0.54, 0.50, 16, centre_z=0.58)),
        (0.30, wf.oval(0.42, 0.32, 16, centre_z=0.60)),
        (-0.30, wf.oval(0.22, 0.12, 16, centre_z=0.62)),
    ]
    parts.append(wf.finish(wf.loft("canopy", hood, glass), 0.02, 3, smooth_angle=60))
    parts.append(wf.finish(
        wf.box("sill", (1.90, 0, 0.36), (3.60, 1.10, 0.10), paint), 0.04, 2))
    parts.append(wf.finish(
        wf.box("seat", (1.40, 0, 0.34), (0.70, 0.50, 0.56), metal), 0.06, 2))
    return wf.join("canopy", parts)


def gear_nose(metal, dark, paint):
    parts = [
        wf.finish(wf.cylinder("noseleg", (2.30, 0, -1.60), 0.09, 1.20, "Z", 12, metal), 0.01, 2),
        wf.finish(wf.cylinder("nosetyre", (2.30, 0, -2.26), 0.30, 0.16, "Y", 18, dark),
                  0.03, 3),
        wf.finish(wf.box("nosedoor", (2.60, 0.22, -1.30), (1.10, 0.05, 0.56), paint), 0.01, 1),
    ]
    return wf.join("gear_nose", parts)


def gear_main(metal, dark, paint):
    parts = []
    for sign in (-1, 1):
        y = sign * 1.10
        parts.append(wf.finish(
            wf.cylinder("mainleg", (-0.70, y, -1.50), 0.10, 1.40, "Z", 12, metal), 0.01, 2))
        parts.append(wf.finish(
            wf.cylinder("maintyre", (-0.70, y, -2.30), 0.40, 0.22, "Y", 20, dark), 0.04, 3))
        parts.append(wf.finish(
            wf.box("maindoor", (-0.70, y - sign * 0.24, -1.20), (1.20, 0.05, 0.66), paint),
            0.01, 1))
    return wf.join("gear_main", parts)


def sidewinders(metal, dark, paint):
    """AIM-9 on the wingtip rails - the F-16 carries them right on the tip."""
    parts = []
    for sign in (-1, 1):
        y = sign * (HALF_SPAN + 0.14)
        le, chord, z = wing_at(HALF_SPAN)
        body = [
            (le + 0.90, wf.oval(0.03, 0.03, 10, centre_z=z)),
            (le + 0.70, wf.oval(0.08, 0.08, 10, centre_z=z)),
            (le + 0.20, wf.oval(0.08, 0.08, 10, centre_z=z)),
            (le - 1.70, wf.oval(0.08, 0.08, 10, centre_z=z)),
            (le - 1.90, wf.oval(0.05, 0.05, 10, centre_z=z)),
        ]
        parts.append(wf.finish(
            wf.loft("aim9", [(x, [(yy + y, zz) for yy, zz in pts]) for x, pts in body], paint),
            0.012, 3, smooth_angle=58))
        # Canards forward, fins aft.
        for dx, size in ((le + 0.40, 0.18), (le - 1.50, 0.24)):
            for a in range(4):
                ang = math.radians(45 + a * 90)
                parts.append(wf.finish(
                    wf.rotate(wf.box("fin9", (dx, y + size * 0.6 * math.cos(ang),
                                              z + size * 0.6 * math.sin(ang)),
                                     (0.26, size, 0.02), dark),
                              x=math.degrees(ang)), 0.006, 1))
        parts.append(wf.finish(
            wf.box("rail", (le - 0.60, y - sign * 0.10, z + 0.06), (1.30, 0.10, 0.14), paint),
            0.02, 2))
    return wf.join("aim9", parts)


def tanks(paint, metal):
    """370-gallon tanks on the inboard pylons."""
    parts = []
    for sign in (-1, 1):
        y = sign * 2.40
        le, chord, z = wing_at(abs(y))
        body = [
            (le + 0.30, wf.oval(0.05, 0.05, 14, centre_z=z - 0.66)),
            (le, wf.oval(0.30, 0.30, 14, centre_z=z - 0.68)),
            (le - 2.20, wf.oval(0.34, 0.34, 14, centre_z=z - 0.68)),
            (le - 3.60, wf.oval(0.18, 0.18, 14, centre_z=z - 0.66)),
            (le - 3.90, wf.oval(0.04, 0.04, 14, centre_z=z - 0.66)),
        ]
        parts.append(wf.finish(
            wf.loft("tank", [(x, [(yy + y, zz) for yy, zz in pts]) for x, pts in body], paint),
            0.02, 3, smooth_angle=58))
        parts.append(wf.finish(
            wf.box("pylon", (le - 1.40, y, z - 0.24), (0.90, 0.10, 0.42), paint), 0.02, 2))
    return wf.join("tank", parts)


PALETTE_BODY = (0x7C / 255, 0x84 / 255, 0x8C / 255)
GREY_LIGHT = (1.14, 1.13, 1.12)
GREY_DARK = (0.82, 0.83, 0.86)
MARK_BLUE = (0.16, 0.22, 0.46)
MARK_WHITE = (0.92, 0.92, 0.92)

PARTS = ("fuselage", "intake", "wing", "lerx", "tail", "fin", "strake", "nozzle",
         "canopy", "gear_nose", "gear_main", "aim9", "tank", "radome", "star")


def weathering():
    """
    Two-tone air superiority grey, which is a camouflage scheme made of two greys half a
    shade apart - hard to photograph and very hard to see.
    """
    for name in ("fuselage", "wing", "lerx", "tail", "fin", "intake", "strake", "tank"):
        obj = bpy.data.objects.get(name)
        if obj is None:
            continue
        wf.densify(obj, max_edge=0.44)
        wf.weather(obj, camo=(GREY_LIGHT, GREY_DARK), cover=(0.34, 0.30), dirt=0.0,
                   wear=0.18, scale=2.6, seed=101, ground=-2.6, top=2.0)
    for name in ("gear_nose", "gear_main", "nozzle", "canopy", "aim9", "radome"):
        obj = bpy.data.objects.get(name)
        if obj is not None:
            wf.weather(obj, camo=None, dirt=0.24, wear=0.28, seed=103, ground=-2.6, top=1.4)


def markings(paint):
    blue = wf.tint_for(MARK_BLUE, PALETTE_BODY)
    white = wf.tint_for(MARK_WHITE, PALETTE_BODY)
    added = []
    for sign in (-1, 1):
        y = sign * 3.00
        le, chord, z = wing_at(abs(y))
        up = 1 if sign > 0 else -1
        x = le - chord * 0.50
        disc = wf.cylinder("star", (x, y, z + up * 0.035), 0.42, 0.02, "Z", 24, paint)
        added.append(wf.flat_tint(disc, blue))
        added.append(wf.flat_tint(
            wf.star("starpoint", (x, y, z + up * 0.045), 0.39, 0.02, paint), white))
        for side in (-1, 1):
            added.append(wf.flat_tint(
                wf.box("bar", (x, y + side * 0.63, z + up * 0.045),
                       (0.30, 0.84, 0.02), paint), white))
    wf.join("star", added)


def build():
    wf.reset()
    paint = wf.role("BODY", GREY, roughness=0.52)
    metal = wf.role("METAL", STEEL, roughness=0.34, metallic=0.85)
    dark = wf.role("DARK", TYRE, roughness=0.70, metallic=0.30)
    glass = wf.role("GLASS", GLASS, roughness=0.08, metallic=0.40)

    fuselage(paint, metal, glass)
    radome(paint, dark, metal)
    intake(paint, metal, dark)
    lerx(paint)
    wings(paint)
    tailplane(paint)
    tailfin(paint, dark, metal)
    strakes(paint, dark)
    nozzle(metal, dark)
    canopy(glass, paint, metal)
    gear_nose(metal, dark, paint)
    gear_main(metal, dark, paint)
    sidewinders(metal, dark, paint)
    tanks(paint, metal)

    weathering()
    markings(paint)
    wf.occlude([p for p in PARTS if bpy.data.objects.get(p)])


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(16.0, floor_z=-2.8)
        wf.camera(azimuth=44, elevation=20)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
