"""
Inside the Panzer IV Ausf. H.

The X-ray layer: object names are the *module* ids, not the part ids, and the app paints
each one from its own colour-coded palette rather than from the vehicle's livery. So the
roles here only choose shades within whatever colour a module turns out to be.

Everything is in the same metres as `panzer_iv.py`, so the two line up when the shell is
ghosted away. The layout is the argument of the design: engine at the back, gearbox at
the front, and a driveshaft the length of the hull between them - which is why the
fighting compartment floor, the turret ring and the commander's head all sit 30 cm
higher than they do in a T-34.
"""
import bpy
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402


MAIN = (0.55, 0.55, 0.56)
ACCENT = (0.70, 0.70, 0.72)
SHADE = (0.34, 0.34, 0.35)
BRIGHT = (0.80, 0.80, 0.82)

MODULES = ("engine", "cooling", "transmission", "driveshaft", "final_drive",
           "driver", "radio_op", "commander", "gunner", "loader", "breech",
           "ammo_sponson", "ammo_floor", "fuel", "radio", "optics",
           "armour_front", "armour_side",
           "turret_drive")


def engine(body, trim, dark, metal):
    """
    Maybach HL 120 TRM: 11.9 litres, sixty degrees of vee, twelve cylinders.

    The twelve pots are the point. A player who opens the engine bay and counts them has
    learnt something a labelled orange box could never tell them.
    """
    parts = wf.piston_engine("engine", (-2.00, 0.0, 0.86), cylinders=12, vee=60.0,
                             bore=0.125, banks=2, block=body, head=trim, pipe=dark)
    # Air cleaners on top, the flywheel housing aft, and the starter on the side.
    for dy in (-0.34, 0.34):
        parts.append(wf.cylinder("engine", (-1.54, dy, 1.30), 0.13, 0.34, "X", 14, trim))
    parts.append(wf.cylinder("engine", (-2.64, 0.0, 0.86), 0.26, 0.20, "X", 18, dark))
    parts.append(wf.cylinder("engine", (-2.30, 0.52, 0.62), 0.09, 0.30, "X", 12, metal))
    # Oil sump and the bearers it sits on.
    for dy in (-0.56, 0.56):
        parts.append(wf.box("engine", (-2.00, dy, 0.50), (1.40, 0.09, 0.10), dark))
    for p in parts:
        wf.finish(p, 0.010, 2)
    return wf.join("engine", parts)


def cooling(body, trim, dark):
    """Two radiators flanking the engine, each with a crankshaft-driven fan."""
    parts = []
    for sign in (-1, 1):
        y = sign * 0.80
        parts.append(wf.box("cooling", (-2.00, y, 0.96), (1.30, 0.14, 0.72), body))
        for i in range(9):
            parts.append(wf.box("cooling", (-2.58 + i * 0.145, y, 0.96),
                                (0.05, 0.16, 0.66), trim))
        # Fan, and the belt drive to it.
        fan = wf.cylinder("cooling", (-1.28, y, 0.96), 0.24, 0.10, "X", 16, dark)
        parts.append(fan)
        for i in range(6):
            a = 2 * math.pi * i / 6
            blade = wf.box("cooling", (-1.28, y + 0.14 * math.cos(a), 0.96 + 0.14 * math.sin(a)),
                           (0.05, 0.09, 0.22), dark)
            wf.rotate(blade, x=math.degrees(a) + 24)
            parts.append(blade)
        parts.append(wf.cylinder("cooling", (-1.60, y, 1.24), 0.05, 0.70, "X", 8, trim))
    for p in parts:
        wf.finish(p, 0.008, 1)
    return wf.join("cooling", parts)


def transmission(body, trim, dark, metal):
    """
    SSG 76: six forward gears and one reverse, right between the driver's knees.

    Front-mounted so the tank is easy to steer and to service - and so that a frontal
    penetration lands in the gearbox and then in the driver.
    """
    parts = wf.gearbox("transmission", (2.22, 0.0, 0.74), (1.00, 1.30, 0.54),
                       block=body, shaft=metal, bell=0.34)
    # Steering brakes either side, and the tiller linkage back to the driver.
    for sign in (-1, 1):
        y = sign * 0.80
        parts.append(wf.cylinder("transmission", (2.40, y, 0.74), 0.28, 0.30, "Y", 18, trim))
        parts.append(wf.cylinder("transmission", (2.40, sign * 0.96, 0.74), 0.16, 0.16,
                                 "Y", 12, dark))
        parts.append(wf.box("transmission", (2.00, sign * 0.34, 1.02), (0.70, 0.06, 0.06),
                            metal))
    for p in parts:
        wf.finish(p, 0.010, 2)
    return wf.join("transmission", parts)


def driveshaft(body, metal, trim):
    """The shaft from the rear engine to the front gearbox, under the turret floor."""
    parts = [wf.cylinder("driveshaft", (0.30, 0.0, 0.66), 0.075, 3.00, "X", 14, body)]
    for x in (-1.20, -0.20, 0.90, 1.70):
        parts.append(wf.cylinder("driveshaft", (x, 0.0, 0.66), 0.12, 0.12, "X", 12, trim))
    # The floor plates it runs beneath.
    parts.append(wf.box("driveshaft", (0.30, 0.0, 0.80), (3.00, 0.34, 0.04), trim))
    for p in parts:
        wf.finish(p, 0.008, 2)
    return wf.join("driveshaft", parts)


def final_drive(body, trim, metal):
    """The last reduction before the sprocket - and the thing that broke most often."""
    parts = []
    for sign in (-1, 1):
        y = sign * 0.86
        parts.append(wf.cylinder("final_drive", (2.50, y, 0.62), 0.30, 0.34, "Y", 20, body))
        parts.append(wf.cylinder("final_drive", (2.50, sign * 1.06, 0.62), 0.18, 0.20,
                                 "Y", 14, trim))
        # The reduction gears inside it.
        parts.append(wf.cylinder("final_drive", (2.50, y, 0.62), 0.22, 0.36, "Y", 16, trim))
        parts.append(wf.cylinder("final_drive", (2.26, y, 0.70), 0.11, 0.30, "Y", 12, metal))
        parts.append(wf.cylinder("final_drive", (2.50, sign * 0.62, 0.62), 0.09, 0.26,
                                 "Y", 12, metal))
    for p in parts:
        wf.finish(p, 0.010, 2)
    return wf.join("final_drive", parts)


def crew(body, kit, helmet):
    """
    Five men: driver and radio operator in the hull, three in the turret.

    Five is the number that matters. A Panzer IV's commander does nothing but command,
    while a T-34-76's also aims the gun - and that difference decided more engagements
    than the thickness of anybody's armour.
    """
    seats = (
        ("driver", (1.86, 0.47, 0.86), 1.0),
        ("radio_op", (1.86, -0.47, 0.86), 1.0),
        ("commander", (-0.70, 0.0, 1.44), 1.0),
        ("gunner", (-0.10, 0.48, 1.40), 1.0),
        ("loader", (0.34, -0.52, 1.40), -1.0),
    )
    for name, centre, facing in seats:
        parts = wf.figure(name, centre, facing=facing, seated=True,
                          body=body, kit=kit, helmet=helmet)
        # A seat under each of them, except the loader, who stood.
        if name != "loader":
            parts.append(wf.box(name, (centre[0] + 0.02 * facing, centre[1], centre[2] - 0.09),
                                (0.40, 0.44, 0.08), kit))
            parts.append(wf.box(name, (centre[0] - 0.20 * facing, centre[1], centre[2] + 0.24),
                                (0.08, 0.42, 0.46), kit))
        for p in parts:
            wf.finish(p, 0.012, 2, smooth_angle=52)
        wf.join(name, parts)


def breech(body, trim, dark, metal):
    """
    7.5 cm KwK 40 breech, recoil guard and recuperator.

    The breech and its guard take up most of the turret, which is why there is so little
    room left for three men and why the loader has to stand.
    """
    parts = [
        wf.box("breech", (1.02, 0.0, 1.94), (0.52, 0.40, 0.46), body),
        wf.box("breech", (0.74, 0.0, 1.94), (0.10, 0.42, 0.50), trim),      # breech ring
        wf.box("breech", (0.74, 0.0, 1.78), (0.09, 0.36, 0.18), dark),      # falling block
    ]
    # Cradle, recuperator and recoil cylinder above and below the barrel.
    parts.append(wf.cylinder("breech", (1.62, 0.0, 1.94), 0.13, 0.90, "X", 16, trim))
    parts.append(wf.cylinder("breech", (1.56, 0.0, 2.14), 0.075, 0.96, "X", 12, dark))
    parts.append(wf.cylinder("breech", (1.56, 0.0, 1.74), 0.075, 0.96, "X", 12, dark))
    # Recoil guard hooping round behind it, and the spent-case bag.
    for dy in (-0.30, 0.30):
        parts.append(wf.box("breech", (0.88, dy, 1.94), (0.80, 0.05, 0.60), metal))
    parts.append(wf.box("breech", (0.52, 0.0, 1.94), (0.05, 0.62, 0.60), metal))
    parts.append(wf.box("breech", (0.62, 0.0, 1.60), (0.34, 0.40, 0.26), dark))
    # Elevation handwheel on the gunner's side.
    parts.append(wf.cylinder("breech", (0.70, 0.44, 1.68), 0.13, 0.05, "Y", 14, metal))
    for p in parts:
        wf.finish(p, 0.012, 2)
    return wf.join("breech", parts)


def ammunition(case, tip, rack_mat):
    """
    Eighty-seven rounds of 7.5 cm: most in the sponsons, the rest under the turret floor.

    Stowing them along the sponson sides puts the ammunition exactly where a side hit
    arrives, and that is what burns a Panzer IV rather than the fuel.
    """
    sponson = []
    for sign in (-1, 1):
        y = sign * 0.92
        sponson += wf.rack("ammo_sponson", (-0.90, y, 1.26), 11, (0.17, 0.0, 0.0),
                           0.086, 0.66, case=case, tip=tip, rows=2,
                           row_step=(0.085, sign * -0.11, 0.0))
        # An open frame, not a plate: a solid back panel hides the very rounds it is
        # there to hold, which is the opposite of what the X-ray view is for.
        sponson.append(wf.box("ammo_sponson", (0.06, y, 0.90), (2.10, 0.26, 0.05), rack_mat))
        for z in (1.00, 1.52):
            sponson.append(wf.box("ammo_sponson", (0.06, sign * 1.06, z), (2.10, 0.04, 0.05),
                                  rack_mat))
        for i in range(6):
            sponson.append(wf.box("ammo_sponson", (-0.92 + i * 0.40, sign * 1.06, 1.26),
                                  (0.04, 0.04, 0.56), rack_mat))
    for p in sponson:
        wf.finish(p, 0.006, 1)
    wf.join("ammo_sponson", sponson)

    floor = []
    for row in range(3):
        floor += wf.rack("ammo_floor", (-1.10 + row * 0.20, -0.30, 0.62), 6,
                         (0.0, 0.12, 0.0), 0.086, 0.66, case=case, tip=tip)
    floor.append(wf.box("ammo_floor", (-0.90, 0.0, 0.28), (0.80, 0.80, 0.05), rack_mat))
    for p in floor:
        wf.finish(p, 0.006, 1)
    wf.join("ammo_floor", floor)


def fuel(body, trim):
    """470 litres in three tanks under the fighting compartment floor."""
    parts = []
    for x, length in ((-0.30, 0.90), (-1.10, 0.70)):
        parts.append(wf.box("fuel", (x, 0.0, 0.52), (length, 1.30, 0.34), body))
        parts.append(wf.cylinder("fuel", (x, 0.40, 0.72), 0.07, 0.10, "Z", 12, trim))
    parts.append(wf.box("fuel", (-0.70, 0.0, 0.52), (0.06, 1.24, 0.30), trim))
    for p in parts:
        wf.finish(p, 0.012, 2)
    return wf.join("fuel", parts)


def radio(body, trim, dark):
    """Fu 5 transmitter and receiver on the starboard sponson, by the radio operator."""
    parts = [
        wf.box("radio", (1.30, -0.86, 1.20), (0.44, 0.26, 0.24), body),
        wf.box("radio", (1.30, -0.86, 1.46), (0.44, 0.26, 0.22), body),
        wf.box("radio", (1.52, -0.86, 1.20), (0.04, 0.20, 0.18), trim),
        wf.box("radio", (1.52, -0.86, 1.46), (0.04, 0.20, 0.16), trim),
    ]
    for i in range(4):
        parts.append(wf.cylinder("radio", (1.53, -0.94 + i * 0.05, 1.34), 0.018, 0.04,
                                 "X", 8, dark))
    return wf.join("radio", [wf.finish(p, 0.008, 2) for p in parts])


def optics(body, trim, glass):
    """TZF 5f gunner's sight, and the commander's five vision blocks."""
    parts = [
        wf.cylinder("optics", (0.94, 0.30, 2.02), 0.055, 0.62, "X", 12, body),
        wf.box("optics", (0.66, 0.30, 2.02), (0.14, 0.14, 0.14), trim),
        wf.cylinder("optics", (1.26, 0.30, 2.02), 0.05, 0.10, "X", 12, glass),
    ]
    for i in range(5):
        a = math.radians(150 + i * 15)
        parts.append(wf.box("optics", (-0.72 + 0.24 * math.cos(a), 0.24 * math.sin(a), 2.20),
                            (0.07, 0.07, 0.09), glass))
    return wf.join("optics", [wf.finish(p, 0.006, 1) for p in parts])


def armour(body, trim):
    """
    The plates, at their real thickness: 80 mm on the nose, 30 mm on the sides.

    Drawn to scale so the ratio is visible. That is the whole story of 1943 - the front
    got thicker and thicker while the sides stayed where they were, because a tank that
    was armoured all round would not have moved.
    """
    front = [
        wf.box("armour_front", (2.94, 0.0, 0.76), (0.080, 2.20, 0.68), body),
        wf.box("armour_front", (2.30, 0.0, 1.44), (0.080, 2.86, 0.64), body),
        wf.box("armour_front", (1.12, 0.0, 2.00), (0.050, 1.76, 0.50), body),
    ]
    for p in front:
        wf.finish(p, 0.006, 1)
    wf.join("armour_front", front)

    side = []
    for sign in (-1, 1):
        side.append(wf.box("armour_side", (0.0, sign * 1.44, 1.42), (4.40, 0.030, 0.66), trim))
        side.append(wf.box("armour_side", (-0.30, sign * 0.88, 2.00), (2.00, 0.030, 0.50), trim))
    for p in side:
        wf.finish(p, 0.006, 1)
    wf.join("armour_side", side)


def build():
    wf.reset()
    body = wf.role("BODY", MAIN, roughness=0.55, metallic=0.30)
    trim = wf.role("TRIM", ACCENT, roughness=0.50, metallic=0.35)
    dark = wf.role("DARK", SHADE, roughness=0.62, metallic=0.25)
    metal = wf.role("METAL", BRIGHT, roughness=0.40, metallic=0.70)
    glass = wf.role("GLASS", (0.5, 0.6, 0.62), roughness=0.14, metallic=0.20)

    engine(body, trim, dark, metal)
    cooling(body, trim, dark)
    transmission(body, trim, dark, metal)
    driveshaft(body, metal, trim)
    final_drive(body, trim, metal)
    crew(body, trim, dark)
    breech(body, trim, dark, metal)
    ammunition(body, trim, dark)
    fuel(body, trim)
    radio(body, trim, dark)
    optics(body, trim, glass)
    armour(body, trim)

    # Hydraulic traverse: a pump hung off the driveshaft, a motor under the turret floor,
    # and the ring gear it drives. The gunner's handwheel is on the breech.
    parts = [
        wf.box("turret_drive", (-0.10, 0.0, 1.22), (0.60, 0.70, 0.36), body),
        wf.cylinder("turret_drive", (-0.10, 0.0, 1.46), 0.22, 0.18, "Z", 16, trim),
        wf.cylinder("turret_drive", (0.16, 0.0, 1.62), 0.86, 0.07, "Z", 28, trim),
        wf.cylinder("turret_drive", (0.70, 0.0, 1.10), 0.11, 0.26, "X", 12, metal),
    ]
    for i in range(24):
        a = 2 * math.pi * i / 24
        parts.append(wf.box("turret_drive",
                              (0.16 + 0.86 * math.cos(a), 0.86 * math.sin(a), 1.62),
                              (0.05, 0.05, 0.06), trim))
    for p in parts:
        wf.finish(p, 0.008, 1)
    wf.join("turret_drive", parts)

    # Isolated: an engine baked against the hull that encloses it comes out black, and
    # the X-ray view ghosts that hull away.
    wf.occlude([m for m in MODULES if bpy.data.objects.get(m)], isolate=True, distance=0.5)


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    if args:
        wf.studio(6.0, floor_z=-0.4)
        wf.camera(azimuth=38, elevation=20)
        wf.render(args[0], samples=20)
    if len(args) > 1:
        wf.export_glb(args[1])
