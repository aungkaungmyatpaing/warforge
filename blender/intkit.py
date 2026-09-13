"""
Shared scaffolding for the X-ray layer.

The internals of nineteen vehicles are mostly the same handful of things arranged
differently: an engine of some type, a gearbox, some seats, some ammunition, some fuel,
and a few plates. This holds the arrangement so each vehicle's script can be a table of
where things go rather than four hundred lines of boxes.

Object names are *module* ids. The app paints each one from its own colour-coded palette,
so the roles here only pick shades within whatever colour a module turns out to be.
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
LENS = (0.50, 0.60, 0.62)


class Kit:
    """The five roles every internals script uses, made once."""

    def __init__(self):
        wf.reset()
        self.body = wf.role("BODY", MAIN, roughness=0.55, metallic=0.30)
        self.trim = wf.role("TRIM", ACCENT, roughness=0.50, metallic=0.35)
        self.dark = wf.role("DARK", SHADE, roughness=0.62, metallic=0.25)
        self.metal = wf.role("METAL", BRIGHT, roughness=0.40, metallic=0.70)
        self.glass = wf.role("GLASS", LENS, roughness=0.14, metallic=0.20)

    # -- finishing -------------------------------------------------------

    def done(self, name, parts, bevel=0.010, segments=2, angle=38):
        """Bevels everything and joins it under the module's id."""
        flat = []
        for p in parts:
            (flat.extend if isinstance(p, list) else flat.append)(p)
        for p in flat:
            wf.finish(p, bevel, segments, angle)
        return wf.join(name, flat)

    def occlude(self, names, distance=0.5):
        wf.occlude([n for n in names if bpy.data.objects.get(n)], isolate=True,
                   distance=distance)

    # -- the recurring pieces --------------------------------------------

    def piston(self, name, centre, cylinders, vee=60.0, bore=0.125, banks=2, extras=()):
        parts = wf.piston_engine(name, centre, cylinders=cylinders, vee=vee, bore=bore,
                                 banks=banks, block=self.body, head=self.trim,
                                 pipe=self.dark)
        parts += list(extras)
        return self.done(name, parts)

    def radial(self, name, centre, cylinders=9, radius=0.55, bore=0.16, rows=1, extras=()):
        parts = wf.radial_engine(name, centre, cylinders=cylinders, radius=radius,
                                 bore=bore, rows=rows, block=self.body, head=self.trim)
        parts += list(extras)
        return self.done(name, parts)

    def jet(self, name, centre, length, radius, stages=7, extras=()):
        parts = wf.turbojet(name, centre, length=length, radius=radius, stages=stages,
                            block=self.body, hot=self.dark, cold=self.trim)
        parts += list(extras)
        return self.done(name, parts)

    def box_module(self, name, boxes, cylinders=()):
        """The catch-all: a module that genuinely is a few boxes - a tank, a locker."""
        parts = [wf.box(name, c, s, self.body) for c, s in boxes]
        parts += [wf.cylinder(name, c, r, l, a, 16, self.trim) for c, r, l, a in cylinders]
        return self.done(name, parts)

    def gearbox(self, name, centre, size, bell=0.0, brakes=()):
        parts = wf.gearbox(name, centre, size, block=self.body, shaft=self.metal, bell=bell)
        for c, r, w, axis in brakes:
            parts.append(wf.cylinder(name, c, r, w, axis, 16, self.trim))
        return self.done(name, parts)

    def seats(self, entries, seated=True, chair=True):
        """[entries] is (module id, centre, facing) - one object per crew position."""
        for name, centre, facing in entries:
            parts = wf.figure(name, centre, facing=facing, seated=seated,
                              body=self.body, kit=self.trim, helmet=self.dark)
            if chair and seated:
                parts.append(wf.box(name, (centre[0] + 0.02 * facing, centre[1],
                                           centre[2] - 0.09), (0.40, 0.44, 0.08), self.trim))
                parts.append(wf.box(name, (centre[0] - 0.20 * facing, centre[1],
                                           centre[2] + 0.24), (0.08, 0.42, 0.44), self.trim))
            self.done(name, parts, bevel=0.012, angle=52)

    def crowd(self, name, centres, facing=1.0, seated=False):
        """Several figures under one module id - a bridge crew, a gun crew."""
        parts = []
        for centre in centres:
            parts += wf.figure(name, centre, facing=facing, seated=seated,
                               body=self.body, kit=self.trim, helmet=self.dark)
        return self.done(name, parts, bevel=0.012, angle=52)

    def rounds(self, name, racks, calibre, length, frames=()):
        """[racks] is (origin, count, step, rows, row_step)."""
        parts = []
        for origin, count, step, rows, row_step in racks:
            parts += wf.rack(name, origin, count, step, calibre, length,
                             case=self.body, tip=self.trim, rows=rows, row_step=row_step)
        parts += [wf.box(name, c, s, self.trim) for c, s in frames]
        return self.done(name, parts, bevel=0.006, segments=1)

    def breech(self, name, centre, calibre, house, barrel_len, recoil=True, guard=True):
        """A gun breech: the block, the cradle, the recoil gear and the guard behind it."""
        cx, cy, cz = centre
        parts = [
            wf.box(name, (cx, cy, cz), house, self.body),
            wf.box(name, (cx - house[0] * 0.5, cy, cz),
                   (house[0] * 0.18, house[1] * 1.06, house[2] * 1.08), self.trim),
            wf.box(name, (cx - house[0] * 0.5, cy, cz - house[2] * 0.34),
                   (house[0] * 0.16, house[1] * 0.86, house[2] * 0.40), self.dark),
        ]
        parts.append(wf.cylinder(name, (cx + house[0] * 0.5 + barrel_len * 0.5, cy, cz),
                                 calibre * 1.6, barrel_len, "X", 16, self.trim))
        if recoil:
            for dz in (calibre * 2.4, -calibre * 2.4):
                parts.append(wf.cylinder(name, (cx + house[0] * 0.5 + barrel_len * 0.48,
                                                cy, cz + dz),
                                         calibre * 0.9, barrel_len * 1.05, "X", 12, self.dark))
        if guard:
            for dy in (-house[1] * 0.72, house[1] * 0.72):
                parts.append(wf.box(name, (cx - house[0] * 0.30, cy + dy, cz),
                                    (house[0] * 1.5, 0.05, house[2] * 1.3), self.metal))
            parts.append(wf.box(name, (cx - house[0] * 1.05, cy, cz),
                                (0.05, house[1] * 1.5, house[2] * 1.3), self.metal))
        return self.done(name, parts, bevel=0.012)

    def plates(self, name, entries):
        """Armour, at its real thickness. [entries] is (centre, size, tilt_y)."""
        parts = []
        for centre, size, tilt in entries:
            plate = wf.box(name, centre, size, self.body)
            if tilt:
                wf.rotate(plate, y=tilt)
            parts.append(plate)
        return self.done(name, parts, bevel=0.006, segments=1)

    def optics(self, name, scopes=(), blocks=()):
        """[scopes] is (centre, radius, length, axis); [blocks] is (centre, size)."""
        parts = []
        for centre, r, length, axis in scopes:
            parts.append(wf.cylinder(name, centre, r, length, axis, 12, self.body))
            head = list(centre)
            head[0] += length * 0.5 if axis == "X" else 0.0
            parts.append(wf.cylinder(name, tuple(head), r * 0.9, r * 1.6, axis, 12, self.glass))
        parts += [wf.box(name, c, s, self.glass) for c, s in blocks]
        return self.done(name, parts, bevel=0.006, segments=1)

    def drum(self, name, centres, radius, length, axis="X", caps=True):
        """Cylindrical things - fuel cells, boilers' drums, torpedo bodies."""
        parts = [wf.cylinder(name, c, radius, length, axis, 18, self.body) for c in centres]
        if caps:
            for c in centres:
                end = list(c)
                end[0 if axis == "X" else (1 if axis == "Y" else 2)] += length * 0.5
                parts.append(wf.cylinder(name, tuple(end), radius * 1.06, length * 0.06,
                                         axis, 18, self.trim))
        return self.done(name, parts)


def render_and_export(modules, args, size=6.0, floor=-0.4, azimuth=38, distance=0.5):
    """The tail every internals script ends with."""
    wf.occlude([n for n in modules if bpy.data.objects.get(n)], isolate=True,
               distance=distance)
    if args:
        wf.studio(size, floor_z=floor)
        wf.camera(azimuth=azimuth, elevation=20)
        wf.render(args[0], samples=16)
    if len(args) > 1:
        wf.export_glb(args[1])
