"""
Inside the T-34-85.

Worth opening straight after the Panzer IV, because the layouts answer the same question
differently. The T-34 puts the engine *and* the gearbox at the back, so there is no
driveshaft running the length of the hull - and with nothing under the fighting
compartment floor the whole tank sits lower on the same suspension travel.

It pays for that elsewhere: the fuel is in the sponsons, inside the fighting compartment
with the crew, and the transmission at the far end of the hull is the one thing on the
tank nobody could fix in the field.
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

RING_X = 0.20

MODULES = ("engine", "cooling", "transmission", "final_drive", "driver", "hull_gunner",
           "commander", "gunner", "loader", "breech", "ammo_floor", "ammo_ready",
           "fuel", "optics", "armour_front", "armour_turret",
           "turret_drive", "radio")


def engine(body, trim, dark, metal):
    """
    V-2-34: 38.8 litres of aluminium V12 diesel, 500 hp.

    Diesel is the whole argument. It burns at a temperature that will not light from a
    hit the way petrol does, it gives half again the range on the same tankful, and in
    1940 nobody else had one in a medium tank.
    """
    parts = wf.piston_engine("engine", (-1.82, 0.0, 0.92), cylinders=12, vee=60.0,
                             bore=0.150, banks=2, block=body, head=trim, pipe=dark)
    # The aluminium crankcase, and the blower and injection pump on top of the vee.
    parts.append(wf.box("engine", (-1.82, 0.0, 1.22), (0.70, 0.26, 0.22), trim))
    parts.append(wf.cylinder("engine", (-1.30, 0.0, 1.16), 0.13, 0.34, "X", 14, dark))
    parts.append(wf.cylinder("engine", (-2.48, 0.0, 0.92), 0.30, 0.22, "X", 18, dark))
    for dy in (-0.62, 0.62):
        parts.append(wf.box("engine", (-1.82, dy, 0.52), (1.30, 0.10, 0.11), dark))
    # Air cleaners: two big cyclone drums standing above the engine.
    for dy in (-0.46, 0.46):
        parts.append(wf.cylinder("engine", (-1.24, dy, 1.24), 0.17, 0.44, "Z", 14, trim))
    for p in parts:
        wf.finish(p, 0.010, 2)
    return wf.join("engine", parts)


def cooling(body, trim, dark):
    """Two radiators either side of the engine, with a single big fan on the flywheel."""
    parts = []
    for sign in (-1, 1):
        y = sign * 0.86
        parts.append(wf.box("cooling", (-1.82, y, 1.00), (1.20, 0.16, 0.80), body))
        for i in range(8):
            parts.append(wf.box("cooling", (-2.34 + i * 0.15, y, 1.00), (0.05, 0.18, 0.74),
                                trim))
    # The fan sits on the flywheel behind the engine and blows through both radiators.
    parts.append(wf.cylinder("cooling", (-2.62, 0.0, 0.92), 0.40, 0.14, "X", 20, dark))
    for i in range(8):
        a = 2 * math.pi * i / 8
        blade = wf.box("cooling", (-2.62, 0.26 * math.cos(a), 0.92 + 0.26 * math.sin(a)),
                       (0.06, 0.13, 0.30), dark)
        wf.rotate(blade, x=math.degrees(a) + 22)
        parts.append(blade)
    for p in parts:
        wf.finish(p, 0.008, 1)
    return wf.join("cooling", parts)


def transmission(body, trim, dark, metal):
    """
    A five-speed box at the very back, behind the engine.

    Rear drive is what keeps the hull low, and it is also why a T-34 gearbox change meant
    lifting the roof off: nothing about it could be reached from inside.
    """
    parts = wf.gearbox("transmission", (-2.86, 0.0, 0.86), (0.62, 1.20, 0.60),
                       block=body, shaft=metal, bell=0.26)
    for sign in (-1, 1):
        parts.append(wf.cylinder("transmission", (-2.86, sign * 0.78, 0.86), 0.24, 0.26,
                                 "Y", 16, trim))
    # The clutch between engine and box, and the linkage forward to the driver.
    parts.append(wf.cylinder("transmission", (-2.54, 0.0, 0.86), 0.26, 0.22, "X", 16, dark))
    parts.append(wf.box("transmission", (-0.20, 0.30, 0.60), (5.00, 0.05, 0.05), metal))
    return wf.join("transmission", [wf.finish(p, 0.010, 2) for p in parts])


def final_drive(body, trim, metal):
    """Rear sprockets, and the reduction gears driving them."""
    parts = []
    for sign in (-1, 1):
        y = sign * 1.00
        parts.append(wf.cylinder("final_drive", (-2.66, y, 0.56), 0.32, 0.36, "Y", 20, body))
        parts.append(wf.cylinder("final_drive", (-2.66, sign * 1.22, 0.56), 0.19, 0.22,
                                 "Y", 14, trim))
        parts.append(wf.cylinder("final_drive", (-2.66, y, 0.56), 0.23, 0.38, "Y", 16, trim))
        parts.append(wf.cylinder("final_drive", (-2.86, y, 0.70), 0.12, 0.30, "Y", 12, metal))
    return wf.join("final_drive", [wf.finish(p, 0.010, 2) for p in parts])


def crew(body, kit, helmet):
    """
    Five: two in the hull, three in the turret.

    Three in the turret is the whole point of the -85. The T-34-76 had two, so the
    commander aimed the gun as well as commanding, and a tank whose commander is looking
    through a gunsight is a tank that cannot see anything else.
    """
    seats = (
        ("driver", (2.24, 0.45, 0.86), 1.0),
        ("hull_gunner", (2.24, -0.50, 0.86), 1.0),
        ("commander", (RING_X - 0.42, 0.44, 1.54), 1.0),
        ("gunner", (RING_X + 0.34, 0.50, 1.50), 1.0),
        ("loader", (RING_X + 0.12, -0.56, 1.50), -1.0),
    )
    for name, centre, facing in seats:
        parts = wf.figure(name, centre, facing=facing, seated=True,
                          body=body, kit=kit, helmet=helmet)
        parts.append(wf.box(name, (centre[0] + 0.02 * facing, centre[1], centre[2] - 0.09),
                            (0.40, 0.44, 0.08), kit))
        parts.append(wf.box(name, (centre[0] - 0.20 * facing, centre[1], centre[2] + 0.24),
                            (0.08, 0.42, 0.44), kit))
        for p in parts:
            wf.finish(p, 0.012, 2, smooth_angle=52)
        wf.join(name, parts)


def breech(body, trim, dark, metal):
    """85 mm ZiS-S-53 breech, recoil gear and the guard round it."""
    parts = [
        wf.box("breech", (RING_X + 0.62, 0.0, 1.94), (0.58, 0.44, 0.50), body),
        wf.box("breech", (RING_X + 0.32, 0.0, 1.94), (0.11, 0.46, 0.54), trim),
        wf.box("breech", (RING_X + 0.32, 0.0, 1.76), (0.10, 0.38, 0.20), dark),
    ]
    parts.append(wf.cylinder("breech", (RING_X + 1.24, 0.0, 1.94), 0.15, 0.96, "X", 16, trim))
    parts.append(wf.cylinder("breech", (RING_X + 1.20, 0.0, 2.16), 0.085, 1.00, "X", 12, dark))
    parts.append(wf.cylinder("breech", (RING_X + 1.20, 0.0, 1.72), 0.085, 1.00, "X", 12, dark))
    for dy in (-0.33, 0.33):
        parts.append(wf.box("breech", (RING_X + 0.48, dy, 1.94), (0.86, 0.05, 0.64), metal))
    parts.append(wf.box("breech", (RING_X + 0.06, 0.0, 1.94), (0.05, 0.66, 0.64), metal))
    parts.append(wf.box("breech", (RING_X + 0.18, 0.0, 1.58), (0.36, 0.42, 0.28), dark))
    parts.append(wf.cylinder("breech", (RING_X + 0.28, 0.46, 1.66), 0.14, 0.05, "Y", 14, metal))
    return wf.join("breech", [wf.finish(p, 0.012, 2) for p in parts])


def ammunition(case, tip, rack_mat):
    """
    Sixty rounds of 85 mm: most of them in bins under the fighting compartment floor.

    Floor stowage is safer than sponson stowage - it is below anything a side hit will
    reach - but every round has to be dug out from under the crew's feet, which is why a
    T-34's rate of fire fell away as the ready rounds ran out.
    """
    floor = []
    for row in range(4):
        floor += wf.rack("ammo_floor", (0.60 - row * 0.42, -0.62, 0.58), 8,
                         (0.0, 0.165, 0.0), 0.100, 0.72, case=case, tip=tip)
    for row in range(4):
        x = 0.60 - row * 0.42
        floor.append(wf.box("ammo_floor", (x, 0.0, 0.24), (0.34, 1.44, 0.05), rack_mat))
        floor.append(wf.box("ammo_floor", (x, 0.0, 0.96), (0.34, 1.44, 0.04), rack_mat))
    for p in floor:
        wf.finish(p, 0.006, 1)
    wf.join("ammo_floor", floor)

    # Ready rounds clipped round the turret wall and in the bustle.
    ready = []
    for sign in (-1, 1):
        for i in range(4):
            ready += wf.shell("ammo_ready",
                              (RING_X - 0.70 + i * 0.22, sign * 0.86, 1.96),
                              0.100, 0.72, case=case, tip=tip)
        ready.append(wf.box("ammo_ready", (RING_X - 0.37, sign * 0.96, 1.96),
                            (0.98, 0.05, 0.62), rack_mat))
    for i in range(4):
        ready += wf.shell("ammo_ready", (RING_X - 1.00, -0.30 + i * 0.20, 1.86),
                          0.100, 0.72, case=case, tip=tip)
    for p in ready:
        wf.finish(p, 0.006, 1)
    wf.join("ammo_ready", ready)


def fuel(body, trim):
    """
    Diesel in the sponsons - 540 litres inside the fighting compartment, with the crew.

    It is the price of putting the ammunition under the floor: something has to go in the
    sponsons, and on a T-34 it is the fuel. Diesel will not flash the way petrol does,
    which is the only reason the arrangement was survivable at all.
    """
    parts = []
    for sign in (-1, 1):
        y = sign * 1.10
        for x, length in ((0.90, 1.00), (-0.30, 0.90)):
            parts.append(wf.box("fuel", (x, y, 1.18), (length, 0.42, 0.52), body))
            parts.append(wf.cylinder("fuel", (x, y, 1.46), 0.07, 0.10, "Z", 12, trim))
        parts.append(wf.box("fuel", (0.30, y, 1.18), (0.05, 0.38, 0.48), trim))
    return wf.join("fuel", [wf.finish(p, 0.012, 2) for p in parts])


def optics(body, trim, glass):
    """TSh-16 gunner's telescope and the commander's cupola vision blocks."""
    parts = [
        wf.cylinder("optics", (RING_X + 0.66, 0.32, 2.06), 0.055, 0.70, "X", 12, body),
        wf.box("optics", (RING_X + 0.34, 0.32, 2.06), (0.15, 0.15, 0.15), trim),
        wf.cylinder("optics", (RING_X + 1.02, 0.32, 2.06), 0.05, 0.10, "X", 12, glass),
    ]
    for i in range(5):
        a = math.radians(140 + i * 20)
        parts.append(wf.box("optics",
                            (RING_X - 0.36 + 0.28 * math.cos(a), 0.44 + 0.28 * math.sin(a),
                             2.38), (0.07, 0.07, 0.09), glass))
    return wf.join("optics", [wf.finish(p, 0.006, 1) for p in parts])


def armour(body, trim):
    """
    45 mm at sixty degrees on the glacis, 90 mm cast on the turret front.

    Drawn to scale against the Panzer IV's 80 mm vertical plate. Sloping 45 mm back to
    sixty degrees makes a shot travel 90 mm of steel to get through it and gives it a far
    better chance of glancing off - the same protection for half the weight, which is why
    every tank since has sloped its front.
    """
    front = [
        wf.box("armour_front", (2.48, 0.0, 1.30), (0.045, 2.00, 1.32), body),
        wf.box("armour_front", (2.92, 0.0, 0.70), (0.045, 1.90, 0.54), body),
    ]
    wf.rotate(front[0], y=-30)
    for p in front:
        wf.finish(p, 0.006, 1)
    wf.join("armour_front", front)

    turret = [
        wf.box("armour_turret", (RING_X + 1.24, 0.0, 2.00), (0.090, 1.30, 0.70), trim),
        wf.box("armour_turret", (RING_X - 0.40, 1.00, 2.00), (2.20, 0.075, 0.70), trim),
        wf.box("armour_turret", (RING_X - 0.40, -1.00, 2.00), (2.20, 0.075, 0.70), trim),
    ]
    for p in turret:
        wf.finish(p, 0.006, 1)
    wf.join("armour_turret", turret)


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
    final_drive(body, trim, metal)
    crew(body, trim, dark)
    breech(body, trim, dark, metal)
    ammunition(body, trim, dark)
    fuel(body, trim)
    optics(body, trim, glass)
    armour(body, trim)

    parts = [
        wf.box("turret_drive", (RING_X - 0.20, 0.0, 1.24), (0.62, 0.72, 0.38), body),
        wf.cylinder("turret_drive", (RING_X - 0.20, 0.0, 1.50), 0.24, 0.22, "Z", 16, trim),
        wf.cylinder("turret_drive", (RING_X, 0.0, 1.64), 0.80, 0.07, "Z", 28, trim),
    ]
    for i in range(24):
        a = 2 * math.pi * i / 24
        parts.append(wf.box("turret_drive",
                            (RING_X + 0.80 * math.cos(a), 0.80 * math.sin(a), 1.64),
                            (0.05, 0.05, 0.06), trim))
    for p in parts:
        wf.finish(p, 0.008, 1)
    wf.join("turret_drive", parts)

    parts = [
        wf.box("radio", (RING_X - 0.96, 0.82, 1.92), (0.44, 0.26, 0.26), body),
        wf.box("radio", (RING_X - 0.96, 0.82, 2.20), (0.44, 0.26, 0.22), body),
        wf.box("radio", (RING_X - 0.76, 0.82, 1.92), (0.04, 0.20, 0.20), trim),
        wf.box("radio", (RING_X - 0.76, 0.82, 2.20), (0.04, 0.20, 0.16), trim),
    ]
    for p in parts:
        wf.finish(p, 0.008, 2)
    wf.join("radio", parts)

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
