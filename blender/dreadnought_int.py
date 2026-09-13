"""
Inside HMS Dreadnought.

Turbines instead of reciprocating engines is half of what made her new. The other half is
below the waterline: ten 12-inch guns need ten magazines, and every one of them is a room
full of cordite under the armoured deck, connected to the turret above by a hoist. Jutland
taught the Royal Navy what happens when the flash doors on that hoist are left open.
"""
import math
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import wf  # noqa: E402
import intkit  # noqa: E402

BOW, STERN = 80.3, -80.3
KEEL, DECK = -8.10, 6.40

MODULES = ("turbines", "boilers", "turret_a", "magazine", "coal", "bridge",
           "fire_control", "armour_belt", "torpedo",
           "steering")


def build():
    k = intkit.Kit()

    # Parsons turbines on four shafts, direct-drive - no reduction gearing yet, so the
    # propellers turn at turbine speed and are inefficient, and she is still faster than
    # anything else afloat.
    parts = []
    for sign, dy in ((-1, 7.4), (1, 7.4), (-1, 2.6), (1, 2.6)):
        parts += wf.steam_turbine("turbines", (-28.0, sign * dy, -3.0), (16.0, 5.0, 4.4),
                                  block=k.body, shaft=k.metal)
    k.done("turbines", parts, bevel=0.06)

    # Eighteen Babcock boilers in three rooms. Coal-fired, hand-stoked, and the reason
    # her crew is eight hundred strong.
    parts = []
    for i, x in enumerate((6.0, -6.0, -18.0)):
        for sign in (-1, 1):
            for dy in (3.4, 8.2):
                parts += wf.boiler("boilers", (x, sign * dy, -2.4), (9.0, 4.0, 6.0),
                                   block=k.body, tube=k.trim)
    k.done("boilers", parts, bevel=0.06)

    # One turret, drawn all the way down: gunhouse, barbette, working chamber and the
    # hoist into the magazine. That column is the vulnerable path.
    parts = [
        wf.box("turret_a", (53.4, 0.0, deck_at(53.4) + 2.0), (11.0, 9.0, 3.4), k.body),
        wf.cylinder("turret_a", (53.4, 0.0, 1.6), 4.4, 8.0, "Z", 22, k.trim),
        wf.cylinder("turret_a", (53.4, 0.0, -3.4), 3.4, 3.6, "Z", 20, k.body),
    ]
    for dy in (-1.5, 1.5):
        parts.append(wf.cylinder("turret_a", (53.4, dy, 0.0), 0.8, 12.0, "Z", 12, k.dark))
        parts.append(wf.cylinder("turret_a", (57.0, dy, deck_at(53.4) + 2.2), 0.42, 10.0,
                                 "X", 14, k.metal))
    k.done("turret_a", parts, bevel=0.10)

    # Magazines and shell rooms, all of them below the waterline.
    parts = []
    for x in (53.4, 8.0, -40.0, -58.0):
        parts.append(wf.box("magazine", (x, 0.0, -5.6), (9.0, 14.0, 4.0), k.body))
        for row in range(3):
            parts += wf.rack("magazine", (x - 3.0 + row * 3.0, -5.0, -6.4), 6,
                             (0.0, 2.0, 0.0), 0.305, 1.20, case=k.trim, tip=k.dark)
    k.done("magazine", parts, bevel=0.04, segments=1)

    # Coal bunkers along the sides - and they are armour as well as fuel: 2,900 tonnes
    # of coal between the sea and the boiler rooms stops a lot of splinters.
    parts = []
    for sign in (-1, 1):
        for x in (10.0, -4.0, -18.0, -30.0):
            parts.append(wf.box("coal", (x, sign * 10.4, -1.6), (12.0, 3.2, 7.0), k.body))
    k.done("coal", parts, bevel=0.06)

    k.crowd("bridge", [(31.0, -1.6, deck_at(31.0) + 9.6), (31.0, 1.6, deck_at(31.0) + 9.6),
                       (33.0, 0.0, deck_at(31.0) + 9.6), (29.0, -2.6, deck_at(31.0) + 9.6),
                       (29.0, 2.6, deck_at(31.0) + 9.6)], seated=False)

    # The spotting top up the tripod, and the transmitting station deep in the hull that
    # it telephones its ranges down to.
    parts = [
        wf.cylinder("fire_control", (20.0, 0.0, deck_at(20.0) + 22.0), 1.9, 1.8, "Z", 16,
                    k.body),
        wf.box("fire_control", (20.0, 0.0, deck_at(20.0) + 23.4), (1.2, 5.0, 0.5), k.trim),
        wf.box("fire_control", (16.0, 0.0, -4.6), (6.0, 7.0, 2.6), k.body),
    ]
    for i in range(4):
        parts.append(wf.box("fire_control", (14.0 + i * 1.4, 0.0, -4.6), (0.4, 6.0, 2.2),
                            k.trim))
    k.done("fire_control", parts, bevel=0.05)

    # 11 inches at the waterline, tapering away at the ends. It protects the machinery
    # and the magazines and nothing else - everything above it is unarmoured.
    parts = []
    for sign in (-1, 1):
        parts.append(wf.box("armour_belt", (0.0, sign * 12.2, 0.2), (96.0, 0.279, 5.0),
                            k.body))
        parts.append(wf.box("armour_belt", (52.0, sign * 11.2, 0.2), (24.0, 0.152, 4.4),
                            k.trim))
        parts.append(wf.box("armour_belt", (-54.0, sign * 10.4, 0.2), (26.0, 0.102, 4.0),
                            k.trim))
    parts.append(wf.box("armour_belt", (0.0, 0.0, 2.6), (96.0, 24.0, 0.044), k.trim))
    k.done("armour_belt", parts, bevel=0.02, segments=1)

    # Steering gear right aft, below the waterline: a steam engine, the rudder stock it
    # turns, and the hand gear for when it fails.
    parts = [
        wf.box("steering", (-68.0, 0.0, -5.0), (8.0, 7.0, 3.4), k.body),
        wf.cylinder("steering", (-70.0, 0.0, -4.0), 0.9, 6.0, "Z", 16, k.trim),
        wf.cylinder("steering", (-64.0, 0.0, -5.0), 1.4, 3.0, "X", 16, k.dark),
        wf.box("steering", (-70.0, 0.0, -2.2), (5.0, 0.8, 0.8), k.metal),
    ]
    k.done("steering", parts, bevel=0.06)

    # Five submerged 18-inch tubes. Every capital ship carried them and none of them
    # ever hit anything, which took thirty years to admit.
    parts = []
    for x, sign in ((40.0, -1), (40.0, 1), (-24.0, -1), (-24.0, 1)):
        y = sign * 9.0
        parts.append(wf.cylinder("torpedo", (x, y, -4.6), 0.24, 8.0, "Y", 14, k.body))
        parts.append(wf.cylinder("torpedo", (x, sign * 12.0, -4.6), 0.30, 0.6, "Y", 14,
                                 k.trim))
    parts.append(wf.cylinder("torpedo", (-70.0, 0.0, -4.6), 0.24, 8.0, "X", 14, k.body))
    k.done("torpedo", parts, bevel=0.03)
    return MODULES


def deck_at(x):
    t = x / BOW
    if t >= 0:
        return DECK + 3.0 * t ** 2.6
    return DECK - 1.2 * (-t) ** 1.6


if __name__ == "__main__":
    args = sys.argv[sys.argv.index("--") + 1:] if "--" in sys.argv else []
    build()
    intkit.render_and_export(MODULES, args, size=88.0, floor=-9.0, distance=3.0)
