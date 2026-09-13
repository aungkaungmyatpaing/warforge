"""
Shared helpers for building Warforge vehicles in Blender.

Models are built in real metres with **+X forward (toward the nose), +Y to port, +Z up**,
which is Blender's own convention and survives the glTF export cleanly.

Everything here leans on modifiers rather than hand-placed geometry: a Bevel gives every
edge a real chamfer that catches light, a Boolean cuts a hatch properly instead of
sticking a disc on top, and an Array along a Curve lays a track round its own run. Those
three are the whole reason for authoring here instead of in code.
"""
import bpy
import bmesh
import math
from mathutils import Vector


# ---------------------------------------------------------------------------
# Scene
# ---------------------------------------------------------------------------

def reset():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    bpy.context.scene.unit_settings.system = "METRIC"


def collection(name):
    col = bpy.data.collections.new(name)
    bpy.context.scene.collection.children.link(col)
    return col


# ---------------------------------------------------------------------------
# Materials
# ---------------------------------------------------------------------------

_materials = {}

# The app colours a vehicle from its own palette, so a Blender material's *name* is what
# crosses the boundary, not its colour: the loader maps these onto the same roles the
# procedural geometry uses. Keeping the names in step is what lets one model be drawn in
# any livery, and lets the X-ray legend stay consistent.
ROLES = ("BODY", "DARK", "METAL", "GLASS", "ACCENT", "TRIM")


def role(name, preview_rgb, roughness=0.62, metallic=0.0):
    """A material named for an app role. [preview_rgb] only affects Blender renders."""
    assert name in ROLES, "material name must be one of " + ", ".join(ROLES)
    return material(name, preview_rgb, roughness, metallic)


def material(name, rgb, roughness=0.62, metallic=0.0):
    """A Principled material, cached by name."""
    if name in _materials:
        return _materials[name]
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    bsdf = mat.node_tree.nodes["Principled BSDF"]
    bsdf.inputs["Base Color"].default_value = (*rgb, 1.0)
    bsdf.inputs["Roughness"].default_value = roughness
    bsdf.inputs["Metallic"].default_value = metallic
    _materials[name] = mat
    return mat


def paint(obj, mat):
    obj.data.materials.clear()
    obj.data.materials.append(mat)
    return obj


# ---------------------------------------------------------------------------
# Primitives
# ---------------------------------------------------------------------------

def _new(name, mesh):
    obj = bpy.data.objects.new(name, mesh)
    bpy.context.scene.collection.objects.link(obj)
    return obj


def box(name, center, size, mat=None):
    """
    Axis-aligned box. [center] and [size] are (x, y, z) in metres.

    The geometry is built around the origin and the position goes in the object's
    transform, so a later `rotation_euler` turns the box about *its own* centre. Baking
    the position into the mesh instead makes every rotation a rotation about the world
    origin, which flings the part across the scene.
    """
    mesh = bpy.data.meshes.new(name)
    bm = bmesh.new()
    bmesh.ops.create_cube(bm, size=1.0)
    bmesh.ops.scale(bm, vec=Vector(size), verts=bm.verts)
    bm.to_mesh(mesh)
    bm.free()
    obj = _new(name, mesh)
    obj.location = Vector(center)
    if mat:
        paint(obj, mat)
    return obj


def rotate(obj, x=0.0, y=0.0, z=0.0):
    """Turns a part about its own centre. Degrees."""
    obj.rotation_euler = (math.radians(x), math.radians(y), math.radians(z))
    return obj


def prism(name, profile, y0, y1, mat=None):
    """
    A side profile swept across the vehicle - the workhorse for hulls and
    superstructures. [profile] is a list of (x, z) in metres, wound either way.
    """
    mesh = bpy.data.meshes.new(name)
    bm = bmesh.new()
    verts = [bm.verts.new((x, y0, z)) for x, z in profile]
    face = bm.faces.new(verts)
    res = bmesh.ops.extrude_face_region(bm, geom=[face])
    moved = [v for v in res["geom"] if isinstance(v, bmesh.types.BMVert)]
    bmesh.ops.translate(bm, vec=(0, y1 - y0, 0), verts=moved)
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    bm.to_mesh(mesh)
    bm.free()
    obj = _new(name, mesh)
    if mat:
        paint(obj, mat)
    return obj


def cylinder(name, center, radius, length, axis="Y", verts=32, mat=None):
    """A cylinder lying along [axis]: 'X' fore-and-aft, 'Y' athwartships, 'Z' upright."""
    rot = {
        "X": (0, math.radians(90), 0),
        "Y": (math.radians(90), 0, 0),
        "Z": (0, 0, 0),
    }[axis]
    bpy.ops.mesh.primitive_cylinder_add(
        vertices=verts, radius=radius, depth=length, location=center, rotation=rot,
    )
    obj = bpy.context.object
    obj.name = name
    if mat:
        paint(obj, mat)
    return obj


def cone(name, center, r1, r2, length, axis="X", verts=32, mat=None):
    """A tapered tube - gun barrels, fuselages, funnels."""
    rot = {
        "X": (0, math.radians(90), 0),
        "Y": (math.radians(90), 0, 0),
        "Z": (0, 0, 0),
    }[axis]
    bpy.ops.mesh.primitive_cone_add(
        vertices=verts, radius1=r1, radius2=r2, depth=length,
        location=center, rotation=rot,
    )
    obj = bpy.context.object
    obj.name = name
    if mat:
        paint(obj, mat)
    return obj


def sphere(name, center, radius, segments=24, mat=None):
    bpy.ops.mesh.primitive_uv_sphere_add(
        segments=segments, ring_count=segments // 2, radius=radius, location=center,
    )
    obj = bpy.context.object
    obj.name = name
    if mat:
        paint(obj, mat)
    return obj


# ---------------------------------------------------------------------------
# Lofting: fuselages, wings, ship hulls
# ---------------------------------------------------------------------------

def loft(name, sections, mat=None, cap_first=True, cap_last=True):
    """
    A solid skinned through a series of cross-sections.

    [sections] is a list of (x, points), where points is a list of (y, z) wound the same
    way and the same length in every section. This is how a fuselage, a wing and a ship's
    hull are all actually drawn - as stations on a plan - and doing it any other way means
    approximating a curved body with boxes, which is exactly what these models are trying
    to stop doing.
    """
    mesh = bpy.data.meshes.new(name)
    bm = bmesh.new()
    rings = []
    for x, points in sections:
        rings.append([bm.verts.new((x, y, z)) for y, z in points])
    count = len(rings[0])
    for a, b in zip(rings, rings[1:]):
        for i in range(count):
            j = (i + 1) % count
            # Skip the sliver where a section collapses to a point - a wing tip, or the
            # nose of a fuselage - which would otherwise be a degenerate face.
            va, vb, vc, vd = a[i], a[j], b[j], b[i]
            if (va.co - vb.co).length < 1e-6 and (vc.co - vd.co).length < 1e-6:
                continue
            verts = [v for k, v in enumerate((va, vb, vc, vd))
                     if k == 0 or (v.co - (va, vb, vc, vd)[k - 1].co).length > 1e-6]
            if len(verts) >= 3 and (verts[0].co - verts[-1].co).length > 1e-6:
                try:
                    bm.faces.new(verts)
                except ValueError:
                    pass
    for ring, do in ((rings[0], cap_first), (rings[-1], cap_last)):
        if do and len({v.co.to_tuple(5) for v in ring}) >= 3:
            try:
                bm.faces.new(ring)
            except ValueError:
                pass
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    bm.to_mesh(mesh)
    bm.free()
    obj = _new(name, mesh)
    if mat:
        paint(obj, mat)
    return obj


def oval(half_y, half_z, steps=16, centre_z=0.0, flatten_bottom=0.0, ripple=0.0,
         ripples=0):
    """
    A closed ring of (y, z) for a fuselage station.

    [flatten_bottom] pulls the lower half towards a flat keel, which is what separates a
    fighter's oval section from a plain tube. [ripple] corrugates the section - a Ju 52's
    skin is its whole identity, and corrugation is cheaper as a wobble in the section
    than as geometry laid on top of it.
    """
    points = []
    for i in range(steps):
        a = 2 * math.pi * i / steps
        scale = 1.0 + ripple * math.sin(a * max(1, ripples)) if ripple else 1.0
        y = math.cos(a) * half_y * scale
        z = math.sin(a) * half_z * scale
        if z < 0 and flatten_bottom:
            z *= 1.0 - flatten_bottom * (1.0 - abs(y) / max(1e-6, half_y))
        points.append((y, centre_z + z))
    return points


def airfoil(chord, thickness=0.12, steps=14, camber=0.0):
    """
    Half a symmetric NACA section, as (x, z) from the trailing edge round the nose and
    back. The leading-edge radius is what makes a wing catch light along its span
    instead of reading as a flat plank.
    """
    def half(sign):
        out = []
        for i in range(steps + 1):
            # Cosine spacing, so the points bunch where the curvature is.
            t = 0.5 * (1 - math.cos(math.pi * i / steps))
            yt = 5 * thickness * chord * (
                0.2969 * math.sqrt(t) - 0.1260 * t - 0.3516 * t * t
                + 0.2843 * t ** 3 - 0.1015 * t ** 4
            )
            rise = camber * chord * 4 * t * (1 - t)
            out.append((t * chord, rise + sign * yt))
        return out

    upper = half(1)
    lower = half(-1)
    # Trailing edge, forward along the underside, round the nose, back along the top.
    return [(chord, 0.0)] + lower[::-1][1:-1] + upper[1:]


def wing(name, stations, mat=None, mirror=False):
    """
    A wing, tailplane or fin, skinned through airfoil sections.

    [stations] is a list of (span, leading_edge_x, chord, thickness, z) from root to tip.
    Span runs along y, so a wing is built for the port side and mirrored; a fin passes
    its sections up the z axis instead by swapping the caller's axes.
    """
    sections = []
    for span, le, chord, thick, z in stations:
        section = airfoil(chord, thick)
        # The loft wants (y, z) rings at an x; a wing's sections are the other way up,
        # so each station becomes a ring in the y = span plane. [airfoil] measures back
        # from the leading edge, and +x is toward the nose, so the chord *subtracts* -
        # adding it built the wing pointing forward, out past the propeller.
        sections.append((span, [(le - cx, z + cz) for cx, cz in section]))
    # Sections are stacked along the span, so build them as x-rings and turn the result.
    obj = loft(name, sections, mat)
    _swap_xy(obj)
    return mirror_y(obj) if mirror else obj


def _swap_xy(obj):
    """Turns a span-wise loft into a vehicle-axis one: (span, x, z) -> (x, y, z)."""
    mesh = obj.data
    for v in mesh.vertices:
        v.co = Vector((v.co.y, v.co.x, v.co.z))
    # Swapping two axes reverses handedness, so the winding has to be rebuilt rather
    # than simply flipped - the chordwise direction may have reversed as well.
    bm = bmesh.new()
    bm.from_mesh(mesh)
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    bm.to_mesh(mesh)
    bm.free()
    return obj


def fin(name, stations, mat=None):
    """
    A vertical surface - a fin, a rudder, a ship's mast fairing.

    Same idea as [wing] but the sections stack upward instead of outboard, so the
    stations are (height, leading_edge_x, chord, thickness, y-offset).
    """
    sections = []
    for up, le, chord, thick, y in stations:
        section = airfoil(chord, thick)
        sections.append((up, [(le - cx, y + cz) for cx, cz in section]))
    obj = loft(name, sections, mat)
    bake(obj)
    mesh = obj.data
    # Built as (height, chordwise, thickness); stand it up as (chordwise, thickness,
    # height). This is a three-way rotation of the axes, so handedness is preserved and
    # the winding only needs rebuilding, never flipping.
    for v in mesh.vertices:
        v.co = Vector((v.co.y, v.co.z, v.co.x))
    return recalc(obj)


def hull_section(half, deck, keel, flare=0.0, bilge=0.58, fullness=0.94, camber=0.0,
                 marks=()):
    """
    A closed (y, z) ring for one station of a ship's hull.

    Drawn the way a lines plan is: up from the keel, round the turn of the bilge, out to
    the waterline, then up the side to the deck edge with whatever [flare] the station
    carries. [fullness] is how far out the section still is at the waterline - close to 1
    amidships, much less at the ends, and that single number is most of the difference
    between a destroyer and a barge.

    [marks] are heights at which to plant extra vertices a few millimetres apart. The
    boot topping and the anti-fouling red are painted per vertex, so without a vertex
    exactly on the line the boundary wanders from station to station and the ship ends up
    looking like it has been dipped at an angle.
    """
    rise = deck - keel
    side = [
        (0.0, keel),
        (half * bilge, keel + rise * 0.10),
        (half * fullness, keel + rise * 0.42),
        (half, keel + rise * 0.74),
        (half * (1.0 + flare), deck),
    ]
    for z in marks:
        if not keel < z < deck:
            continue
        for offset in (-0.03, 0.03):
            target = z + offset
            for i in range(len(side) - 1):
                (y0, z0), (y1, z1) = side[i], side[i + 1]
                if z0 < target < z1:
                    t = (target - z0) / (z1 - z0)
                    side.insert(i + 1, (y0 + (y1 - y0) * t, target))
                    break

    ring = [(-y, z) for y, z in side]                       # starboard, keel to deck
    ring += [(0.0, deck + camber)]                          # crown of the deck
    ring += [(y, z) for y, z in reversed(side)][:-1]        # port, deck back down
    return ring


def ellipse_stations(half_span, root_le, root_chord, thickness, steps=10,
                     sweep=0.0, dihedral=0.0, tip_chord=0.10):
    """
    Stations for an elliptical planform - the Spitfire's wing, and the reason people can
    name it from a mile away. Chord follows an ellipse across the span rather than
    tapering in a straight line, so the leading and trailing edges are both curves.
    """
    out = []
    for i in range(steps + 1):
        t = i / steps
        chord = root_chord * math.sqrt(max(0.0, 1 - t * t))
        chord = max(chord, tip_chord)
        span = half_span * t
        # Quarter-chord stays on the sweep line; the ellipse opens either side of it.
        quarter = root_le - root_chord * 0.25 - span * math.tan(math.radians(sweep))
        out.append((span, quarter + chord * 0.25, chord, thickness, span * math.tan(math.radians(dihedral))))
    return out


# ---------------------------------------------------------------------------
# Modifiers
# ---------------------------------------------------------------------------

def plate(name, quad_a, quad_b, mat=None):
    """
    A flat plate from two edges: [quad_a] and [quad_b] are each two (x, z) points, swept
    between y = -half and +half by the caller. Used where a hull face is neither
    axis-aligned nor a simple extrusion - a sloped glacis meeting a vertical plate.
    """
    return prism(name, [quad_a[0], quad_a[1], quad_b[1], quad_b[0]], 0, 0, mat)


def leaf_bogie(name, x, y, z, span, mat=None):
    """
    A leaf-sprung bogie: the pair of road wheels and the spring above them.

    The Panzer IV's eight small wheels in four sprung pairs are one of the things people
    recognise it by, and a row of evenly spaced wheels with nothing between them is the
    single clearest sign of a model built from boxes.
    """
    parts = []
    arm = box(name + "_arm", (x, y, z + 0.16), (span, 0.10, 0.09), mat)
    parts.append(arm)
    for i in range(4):
        leaf = box(name + "_leaf", (x, y, z + 0.24 + i * 0.035),
                   (span * (1.0 - i * 0.13), 0.11, 0.022), mat)
        parts.append(leaf)
    for sign in (-1, 1):
        parts.append(box(name + "_pivot", (x + sign * span * 0.5, y, z + 0.16),
                         (0.09, 0.14, 0.14), mat))
    return parts


def bevel(obj, width=0.018, segments=3, angle=40):
    """
    A real chamfer on every hard edge. This is the single biggest visual difference
    between a model built here and one assembled from raw polygons: a bevelled edge
    catches a highlight, and a perfectly sharp one cannot.
    """
    m = obj.modifiers.new("Bevel", "BEVEL")
    m.width = width
    m.segments = segments
    m.limit_method = "ANGLE"
    m.angle_limit = math.radians(angle)
    m.harden_normals = False
    return obj


def apply_modifiers(obj):
    """
    Flattens the whole modifier stack into the mesh.

    Done through the dependency graph rather than `object.modifier_apply`, because that
    operator needs a context this script does not have and fails *silently* when it does
    not get one - which is how a Mirror that had never run went unnoticed until half a
    tank was missing.
    """
    if not obj.modifiers:
        return obj
    deps = bpy.context.evaluated_depsgraph_get()
    mesh = bpy.data.meshes.new_from_object(obj.evaluated_get(deps))
    obj.modifiers.clear()
    old = obj.data
    obj.data = mesh
    bpy.data.meshes.remove(old)
    return obj


def bake(obj):
    """
    Freezes location, rotation and scale into the mesh, leaving an identity transform.

    Everything downstream - Mirror, Boolean, join - works in the object's *local* space.
    A part still carrying a rotation mirrors about a rotated axis and ends up somewhere
    absurd, which is exactly how the road wheels first came out underneath the tank.
    """
    # Blender recomputes matrix_world lazily, so a location set a moment ago is not in
    # it yet. Reading the stale matrix silently drops the move - which is how the track
    # ended up mirrored about the centreline instead of laid down each side.
    bpy.context.view_layer.update()
    obj.data.transform(obj.matrix_world)
    obj.matrix_world.identity()
    bpy.context.view_layer.update()
    return obj


def cut(target, cutter, operation="DIFFERENCE"):
    """Boolean. The cutter is removed from the scene afterwards."""
    bake(target)
    bake(cutter)
    m = target.modifiers.new("Boolean", "BOOLEAN")
    m.operation = operation
    m.object = cutter
    m.solver = "EXACT"
    apply_modifiers(target)
    bpy.data.objects.remove(cutter, do_unlink=True)
    return target


def _slice(target, p0, p1, axis, drop, size):
    """
    Shared body of the three slice helpers: cuts [target] with the plane through the two
    points, throwing away whichever side [drop] is on.

    Positioning a rotated box by eye is how the first attempt at both of these tanks got
    its sloped plates wrong - the cut looked plausible in a render and was several
    centimetres off everywhere. Here the cutter's face is *solved* to lie on the line,
    so "the glacis runs from the nose at 0.96 up to x 1.91 at the deck" is written down
    as those two points and comes out as those two points.

    And the side to remove is named by a point rather than by a sign, because a sign
    means working out which way a normal ended up pointing - which went wrong on three
    of the first four cuts, each time deleting most of the tank.
    """
    (a0, b0), (a1, b1) = p0, p1
    da, db = a1 - a0, b1 - b0
    length = math.hypot(da, db)
    if length < 1e-6:
        return target
    na, nb = db / length, -da / length
    ma, mb = (a0 + a1) * 0.5, (b0 + b1) * 0.5
    if (drop[0] - ma) * na + (drop[1] - mb) * nb < 0:
        na, nb = -na, -nb
    ca, cb = ma + na * size * 0.5, mb + nb * size * 0.5

    if axis == "xz":          # side-on view: glacis, rear plate, deck steps
        cutter = box("cut", (ca, 0.0, cb), (size, size * 2, size))
        rotate(cutter, y=math.degrees(math.atan2(na, nb)))
    elif axis == "yz":        # head-on view: sloping hull and turret sides
        cutter = box("cut", (0.0, ca, cb), (size * 2, size, size))
        rotate(cutter, x=math.degrees(math.atan2(-na, nb)))
    else:                     # plan view: the corners of a hexagonal turret
        cutter = box("cut", (ca, cb, 0.0), (size, size, size * 2))
        rotate(cutter, z=math.degrees(math.atan2(nb, na)))
    return cut(target, cutter)


def slice_xz(target, p0, p1, drop, size=14.0):
    """Cuts along a line seen from the side. [p0], [p1] and [drop] are (x, z)."""
    return _slice(target, p0, p1, "xz", drop, size)


def slice_yz(target, p0, p1, drop, size=14.0):
    """Cuts along a line seen head-on. [p0], [p1] and [drop] are (y, z)."""
    return _slice(target, p0, p1, "yz", drop, size)


def slice_xy(target, p0, p1, drop, size=14.0):
    """Cuts along a line seen from above. [p0], [p1] and [drop] are (x, y)."""
    return _slice(target, p0, p1, "xy", drop, size)


def frustum(name, center, bottom, top, height, mat=None):
    """
    A box that tapers between two rectangles - the shape of most cast turrets and of any
    hull with sloping sides. [bottom] and [top] are (x, y) extents; [center] is the
    middle of the volume.
    """
    mesh = bpy.data.meshes.new(name)
    bm = bmesh.new()
    hz = height * 0.5
    rings = []
    for (sx, sy), z in ((bottom, -hz), (top, hz)):
        rings.append([
            bm.verts.new((-sx / 2, -sy / 2, z)),
            bm.verts.new((sx / 2, -sy / 2, z)),
            bm.verts.new((sx / 2, sy / 2, z)),
            bm.verts.new((-sx / 2, sy / 2, z)),
        ])
    bm.faces.new(rings[0][::-1])
    bm.faces.new(rings[1])
    for i in range(4):
        j = (i + 1) % 4
        bm.faces.new((rings[0][i], rings[0][j], rings[1][j], rings[1][i]))
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    bm.to_mesh(mesh)
    bm.free()
    obj = _new(name, mesh)
    obj.location = Vector(center)
    if mat:
        paint(obj, mat)
    return obj


def mirror_y(obj):
    """
    Mirrors the mesh across the vehicle's centreline, so one call builds both sides.
    Done in bmesh rather than with a modifier: duplicate the geometry, negate y, and
    flip the winding so the copy faces outward too.
    """
    bake(obj)
    bm = bmesh.new()
    bm.from_mesh(obj.data)
    geom = bm.verts[:] + bm.edges[:] + bm.faces[:]
    dup = bmesh.ops.duplicate(bm, geom=geom)["geom"]
    bmesh.ops.scale(
        bm, vec=Vector((1, -1, 1)),
        verts=[v for v in dup if isinstance(v, bmesh.types.BMVert)],
    )
    bmesh.ops.reverse_faces(
        bm, faces=[f for f in dup if isinstance(f, bmesh.types.BMFace)],
    )
    bm.to_mesh(obj.data)
    bm.free()
    return obj


def smooth(obj, angle=38):
    """Auto-smooth: round where the surface is round, creased where it is not."""
    obj.data.polygons.foreach_set("use_smooth", [True] * len(obj.data.polygons))
    modifier = obj.modifiers.new("Smooth by Angle", "NODES")
    node_group = bpy.data.node_groups.get("Smooth by Angle")
    if node_group is None:
        bpy.ops.object.shade_auto_smooth(angle=math.radians(angle))
        obj.modifiers.remove(modifier)
        return obj
    modifier.node_group = node_group
    modifier["Input_1"] = math.radians(angle)
    apply_modifiers(obj)
    return obj


def finish(obj, bevel_width=0.018, segments=3, smooth_angle=38):
    """Bevel, flatten the modifier stack, smooth and bake the transform."""
    bevel(obj, bevel_width, segments)
    apply_modifiers(obj)
    bake(obj)
    obj.data.polygons.foreach_set("use_smooth", [True] * len(obj.data.polygons))
    return obj


def join(name, objects):
    """Merges parts that belong to one draggable piece."""
    objects = [o for o in objects if o is not None]
    if not objects:
        return None
    for o in objects:
        bake(o)
    bpy.ops.object.select_all(action="DESELECT")
    for o in objects:
        o.select_set(True)
    bpy.context.view_layer.objects.active = objects[0]
    bpy.ops.object.join()
    obj = bpy.context.object
    obj.name = name
    return bake(obj)


def track_run(name, points, link_size, mat=None, closed=True):
    """
    Track links laid round a closed run.

    A curve through [points], a single link, and an Array modifier fitted to the curve
    with a Curve deform on top. This is how tracks are actually made, and it is why they
    bend correctly round the idler instead of being a band with ticks drawn on it.
    """
    curve_data = bpy.data.curves.new(name + "_path", "CURVE")
    curve_data.dimensions = "3D"
    spline = curve_data.splines.new("POLY")
    spline.points.add(len(points) - 1)
    for i, (x, z) in enumerate(points):
        spline.points[i].co = (x, 0.0, z, 1.0)
    spline.use_cyclic_u = closed
    curve = bpy.data.objects.new(name + "_path", curve_data)
    bpy.context.scene.collection.objects.link(curve)

    link = box(name, (0, 0, 0), link_size, mat)
    bevel(link, 0.008, 2)

    arr = link.modifiers.new("Array", "ARRAY")
    arr.fit_type = "FIT_CURVE"
    arr.curve = curve
    arr.relative_offset_displace = (1.06, 0, 0)

    deform = link.modifiers.new("Curve", "CURVE")
    deform.object = curve
    deform.deform_axis = "POS_X"

    apply_modifiers(link)
    bpy.data.objects.remove(curve, do_unlink=True)
    return bake(link)


# ---------------------------------------------------------------------------
# Machinery
#
# What goes inside the vehicle. The X-ray view exists to answer "where is everything and
# how close together is it", so these are built to the right proportions and the right
# count - twelve cylinders on a V12, nine on a radial - rather than to the right castings.
# ---------------------------------------------------------------------------

def piston_engine(name, centre, cylinders=12, vee=60.0, bore=0.125, banks=2,
                  block=None, head=None, pipe=None, axis="X"):
    """
    A piston engine: crankcase, one or two banks of cylinders, heads and exhausts.

    [vee] is the included angle between the banks - 60 or 90 for a V, 180 for a flat,
    and [banks] = 1 with any angle for an inline. The cylinder count is the thing people
    actually recognise, so it is a parameter rather than a constant.
    """
    per_bank = max(1, cylinders // max(1, banks))
    pitch = bore * 1.42
    length = per_bank * pitch
    parts = []
    cx, cy, cz = centre

    # Crankcase and sump.
    parts.append(box(name, (cx, cy, cz - bore * 0.90), (length * 1.06, bore * 2.6, bore * 1.9),
                     block))
    parts.append(box(name, (cx, cy, cz - bore * 1.95), (length * 0.86, bore * 2.1, bore * 0.7),
                     block))

    for b in range(banks):
        side = (-1 if b == 0 else 1) if banks > 1 else 0
        lean = math.radians(vee * 0.5) * side
        offset = math.sin(lean) * bore * 1.5
        rise = math.cos(lean) * bore * 1.5
        for i in range(per_bank):
            x = cx - length * 0.5 + pitch * (i + 0.5)
            pot = cylinder(name, (x, cy + offset, cz + rise), bore * 0.54, bore * 1.5,
                           "Z", 12, block)
            rotate(pot, x=math.degrees(lean))
            parts.append(pot)
        # Head over the bank, and the exhaust manifold outboard of it.
        head_obj = box(name, (cx, cy + offset * 1.44, cz + rise * 1.44),
                       (length, bore * 1.30, bore * 0.86), head or block)
        rotate(head_obj, x=math.degrees(lean))
        parts.append(head_obj)
        manifold = cylinder(name, (cx, cy + offset * 2.30, cz + rise * 1.10),
                            bore * 0.34, length * 0.94, axis, 10, pipe or head or block)
        parts.append(manifold)
        for i in range(per_bank):
            x = cx - length * 0.5 + pitch * (i + 0.5)
            parts.append(cylinder(name, (x, cy + offset * 1.90, cz + rise * 1.26),
                                  bore * 0.22, bore * 0.9, "Y", 8, pipe or head or block))
    return parts


def radial_engine(name, centre, cylinders=9, radius=0.55, bore=0.16, rows=1,
                  block=None, head=None, axis="X"):
    """A radial: a crankcase drum with its cylinders spoked out round it."""
    parts = []
    cx, cy, cz = centre
    parts.append(cylinder(name, centre, radius * 0.44, radius * 0.80, axis, 16, block))
    for r in range(rows):
        dx = (r - (rows - 1) * 0.5) * radius * 0.70
        for i in range(cylinders):
            a = 2 * math.pi * i / cylinders + (math.pi / cylinders if r % 2 else 0.0)
            y = cy + radius * 0.72 * math.cos(a)
            z = cz + radius * 0.72 * math.sin(a)
            pot = cylinder(name, (cx + dx, y, z), bore * 0.5, radius * 0.80, "Z", 10, block)
            rotate(pot, x=math.degrees(a) - 90)
            parts.append(pot)
            fin = box(name, (cx + dx, cy + radius * 0.98 * math.cos(a),
                             cz + radius * 0.98 * math.sin(a)),
                      (bore * 0.9, bore * 0.9, bore * 0.9), head or block)
            rotate(fin, x=math.degrees(a))
            parts.append(fin)
    # Reduction gear and the crankshaft nose.
    parts.append(cylinder(name, (cx + radius * 0.62, cy, cz), radius * 0.30, radius * 0.44,
                          axis, 14, head or block))
    return parts


def turbojet(name, centre, length=3.6, radius=0.42, stages=7, block=None, hot=None,
             cold=None):
    """
    A turbojet: intake, compressor stages, combustion section, turbine and nozzle.

    The proportions are what make it read as a jet - most of the length is compressor,
    the combustor is the fat part, and the turbine is two thin discs at the back.
    """
    parts = []
    cx, cy, cz = centre
    nose = cx + length * 0.5
    parts.append(cone(name, (nose - length * 0.05, cy, cz), radius * 0.30, radius * 0.86,
                      length * 0.10, "X", 18, cold or block))
    # Compressor: a stack of discs getting smaller as the air is squeezed.
    for i in range(stages):
        t = i / max(1, stages - 1)
        x = nose - length * (0.12 + 0.30 * t)
        parts.append(cylinder(name, (x, cy, cz), radius * (0.92 - 0.14 * t),
                              length * 0.030, "X", 18, cold or block))
    parts.append(cylinder(name, (nose - length * 0.30, cy, cz), radius * 0.30,
                          length * 0.44, "X", 12, block))
    # Combustor: the widest section, with the cans round it.
    parts.append(cylinder(name, (nose - length * 0.58, cy, cz), radius * 0.98,
                          length * 0.24, "X", 20, hot or block))
    for i in range(8):
        a = 2 * math.pi * i / 8
        parts.append(cylinder(name, (nose - length * 0.58, cy + radius * 0.70 * math.cos(a),
                                     cz + radius * 0.70 * math.sin(a)),
                              radius * 0.22, length * 0.30, "X", 8, hot or block))
    # Turbine discs and the jet pipe.
    for i in range(2):
        parts.append(cylinder(name, (nose - length * (0.74 + i * 0.05), cy, cz),
                              radius * 0.86, length * 0.030, "X", 18, hot or block))
    parts.append(cone(name, (nose - length * 0.90, cy, cz), radius * 0.76, radius * 0.62,
                      length * 0.24, "X", 18, hot or block))
    return parts


def gearbox(name, centre, size, block=None, shaft=None, bell=0.0, axis="X"):
    """A gearbox: the casing, its ribs, and the shaft going in and out of it."""
    parts = [box(name, centre, size, block)]
    cx, cy, cz = centre
    for i in range(3):
        parts.append(box(name, (cx - size[0] * 0.3 + i * size[0] * 0.3, cy, cz),
                         (size[0] * 0.08, size[1] * 1.06, size[2] * 1.06), block))
    if bell:
        parts.append(cylinder(name, (cx - size[0] * 0.5 - bell * 0.5, cy, cz),
                              size[2] * 0.52, bell, axis, 16, block))
    parts.append(cylinder(name, (cx + size[0] * 0.72, cy, cz), size[2] * 0.16,
                          size[0] * 0.5, axis, 12, shaft or block))
    return parts


def boiler(name, centre, size, block=None, tube=None, axis="Y"):
    """A water-tube boiler: two drums and the bank of tubes between them."""
    parts = []
    cx, cy, cz = centre
    parts.append(cylinder(name, (cx, cy, cz + size[2] * 0.34), size[2] * 0.20, size[1],
                          axis, 14, block))
    for dy in (-size[0] * 0.28, size[0] * 0.28):
        parts.append(cylinder(name, (cx + dy, cy, cz - size[2] * 0.34), size[2] * 0.14,
                              size[1] * 0.9, axis, 12, block))
    for i in range(7):
        x = cx - size[0] * 0.30 + i * size[0] * 0.10
        parts.append(box(name, (x, cy, cz), (size[0] * 0.04, size[1] * 0.86, size[2] * 0.56),
                         tube or block))
    parts.append(box(name, (cx, cy, cz - size[2] * 0.52), (size[0] * 0.8, size[1] * 0.7,
                                                           size[2] * 0.14), tube or block))
    return parts


def steam_turbine(name, centre, size, block=None, shaft=None):
    """HP and LP turbine casings with the reduction gear between them."""
    parts = []
    cx, cy, cz = centre
    parts.append(cone(name, (cx + size[0] * 0.26, cy, cz), size[2] * 0.30, size[2] * 0.48,
                      size[0] * 0.42, "X", 18, block))
    parts.append(cone(name, (cx - size[0] * 0.26, cy, cz), size[2] * 0.34, size[2] * 0.56,
                      size[0] * 0.42, "X", 18, block))
    parts.append(cylinder(name, (cx, cy, cz - size[2] * 0.42), size[2] * 0.40,
                          size[0] * 0.30, "Y", 18, block))
    parts.append(cylinder(name, (cx, cy, cz), size[2] * 0.10, size[0] * 1.2, "X", 12,
                          shaft or block))
    return parts


def figure(name, centre, facing=1.0, seated=True, body=None, kit=None, helmet=None):
    """
    A crew figure.

    Drawn as a person rather than a box on purpose: how tightly five of them are packed
    round a breech, and how little steel is between them and the outside, is most of what
    the X-ray view is for.
    """
    parts = []
    cx, cy, cz = centre                      # centre is the seat, at hip height
    f = facing
    # Torso, tapering to the shoulders.
    parts.append(frustum(name, (cx, cy, cz + 0.28), (0.30, 0.42), (0.34, 0.46), 0.56, body))
    parts.append(sphere(name, (cx + 0.02 * f, cy, cz + 0.72), 0.115, 12, helmet or body))
    parts.append(cylinder(name, (cx + 0.02 * f, cy, cz + 0.60), 0.07, 0.12, "Z", 10, body))
    for side in (-1, 1):
        parts.append(cylinder(name, (cx + 0.08 * f, cy + side * 0.24, cz + 0.36), 0.065,
                              0.34, "Z", 10, kit or body))
        parts.append(cylinder(name, (cx + 0.22 * f, cy + side * 0.22, cz + 0.20), 0.058,
                              0.30, "X", 10, kit or body))
    if seated:
        for side in (-1, 1):
            parts.append(cylinder(name, (cx + 0.22 * f, cy + side * 0.12, cz - 0.02), 0.085,
                                  0.46, "X", 10, body))
            parts.append(cylinder(name, (cx + 0.44 * f, cy + side * 0.12, cz - 0.26), 0.075,
                                  0.46, "Z", 10, body))
            parts.append(box(name, (cx + 0.50 * f, cy + side * 0.12, cz - 0.48),
                             (0.22, 0.11, 0.08), kit or body))
    else:
        for side in (-1, 1):
            parts.append(cylinder(name, (cx, cy + side * 0.12, cz - 0.44), 0.085, 0.86,
                                  "Z", 10, body))
            parts.append(box(name, (cx + 0.06 * f, cy + side * 0.12, cz - 0.88),
                             (0.24, 0.11, 0.08), kit or body))
    return parts


def shell(name, centre, calibre, length, case=None, tip=None, axis="X"):
    """One round: the case, the shoulder and the pointed shell above it."""
    r = calibre * 0.5
    cx, cy, cz = centre
    body = length * 0.62
    parts = [cylinder(name, (cx, cy, cz - length * 0.5 + body * 0.5), r, body, "Z", 10, case)]
    parts.append(cone(name, (cx, cy, cz + length * 0.5 - length * 0.19),
                      r, r * 0.94, length * 0.38 * 0.45, "Z", 10, tip or case))
    parts.append(cone(name, (cx, cy, cz + length * 0.5 - length * 0.06),
                      r * 0.94, r * 0.10, length * 0.26, "Z", 10, tip or case))
    return parts


def rack(name, origin, count, step, calibre, length, case=None, tip=None, rows=1,
         row_step=(0.0, 0.0, 0.0)):
    """A row - or a grid - of rounds stowed together."""
    parts = []
    ox, oy, oz = origin
    for r in range(rows):
        for i in range(count):
            parts += shell(name,
                           (ox + step[0] * i + row_step[0] * r,
                            oy + step[1] * i + row_step[1] * r,
                            oz + step[2] * i + row_step[2] * r),
                           calibre, length, case, tip)
    return parts


# ---------------------------------------------------------------------------
# Weathering
# ---------------------------------------------------------------------------

# Vertex colours are stored divided by this, and the game multiplies it back.
#
# glTF writes COLOR_0 as normalised integers, so anything above 1.0 is clamped away on
# export. Most of the weathering only ever darkens, but a marking has to be able to
# brighten: white paint over dunkelgelb needs a multiplier of about 2.3, and without the
# headroom a Balkenkreuz comes out the same colour as the tank. The cost is half a bit of
# precision on 16-bit channels, which is nothing - and white over the T-34's dark green
# needs about 3.5, which is what set the figure. `GlbLoader` holds the same constant.
TINT_RANGE = 5.0


def _stored(tint):
    """A tint packed into the 0..1 the exporter will accept."""
    return tuple(min(1.0, max(0.0, c / TINT_RANGE)) for c in tint)


def densify(obj, max_edge=0.22):
    """
    Subdivides long edges until none is longer than [max_edge].

    Vertex colours can only vary where there are vertices. A hull side built from four
    corners has nowhere to put a camouflage patch or a gradient of dirt, so the surface
    has to be given somewhere to hold them first.
    """
    bm = bmesh.new()
    bm.from_mesh(obj.data)
    for _ in range(6):
        long_edges = [e for e in bm.edges if e.calc_length() > max_edge]
        if not long_edges:
            break
        bmesh.ops.subdivide_edges(bm, edges=long_edges, cuts=1, use_grid_fill=True)
    bm.to_mesh(obj.data)
    bm.free()
    return obj


def _noise3(x, y, z, seed):
    """Smooth value noise. Deterministic, and good enough for paint and mud."""
    import math as _m

    def hashed(i, j, k):
        n = (i * 374761393 + j * 668265263 + k * 2147483647 + seed * 971) & 0xFFFFFFFF
        n = (n ^ (n >> 13)) * 1274126177 & 0xFFFFFFFF
        return ((n ^ (n >> 16)) & 0xFFFF) / 65535.0

    xi, yi, zi = _m.floor(x), _m.floor(y), _m.floor(z)
    xf, yf, zf = x - xi, y - yi, z - zi
    # Smoothstep, so patches have soft edges rather than a grid.
    u, v, w = (t * t * (3 - 2 * t) for t in (xf, yf, zf))
    total = 0.0
    for dz in (0, 1):
        for dy in (0, 1):
            for dx in (0, 1):
                weight = ((u if dx else 1 - u) * (v if dy else 1 - v) * (w if dz else 1 - w))
                total += hashed(xi + dx, yi + dy, zi + dz) * weight
    return total


def weather(obj, camo=None, cover=(0.22, 0.17), dirt=0.42, wear=0.22, scale=1.5,
            seed=1, ground=0.0, top=2.6):
    """
    Bakes camouflage, dirt and edge wear into a vertex colour *multiplier*.

    A multiplier rather than an absolute colour, because the app still paints the vehicle
    from its own palette - the model supplies the variation, the palette supplies the
    hue. Flat, evenly coloured paint is most of what makes a good model still look like a
    toy; this is the cheapest thing that fixes it.

    @param camo patch tints, or None for a single-colour scheme.
    @param cover fraction of the surface each camouflage colour covers.
    @param dirt how far up the hull the mud reaches, and how strongly.
    @param wear how much paint has worn off the convex edges.
    """
    mesh = obj.data
    layer = mesh.color_attributes.get("Col")
    if layer is None:
        layer = mesh.color_attributes.new(name="Col", type="FLOAT_COLOR", domain="POINT")

    # Convexity per vertex: an edge that sticks out has its neighbours behind it.
    convex = [0.0] * len(mesh.vertices)
    counts = [0] * len(mesh.vertices)
    for edge in mesh.edges:
        a, b = edge.vertices
        va, vb = mesh.vertices[a], mesh.vertices[b]
        d = vb.co - va.co
        if d.length < 1e-6:
            continue
        d.normalize()
        convex[a] += -d.dot(va.normal)
        convex[b] += d.dot(vb.normal)
        counts[a] += 1
        counts[b] += 1

    bpy.context.view_layer.update()          # matrix_world is stale until this runs
    to_world = obj.matrix_world
    world = [to_world @ v.co for v in mesh.vertices]

    field = None
    if camo:
        # Two octaves, so patches have both a broad shape and a ragged edge.
        field = []
        for co in world:
            n = _noise3(co.x / scale, co.y / scale, co.z / scale, seed)
            n += 0.35 * _noise3(co.x / (scale * 0.4), co.y / (scale * 0.4),
                                co.z / (scale * 0.4), seed + 7)
            field.append(n / 1.35)
        # Thresholds come from the distribution rather than being fixed, because value
        # noise clusters hard around its midpoint: a threshold picked by eye either
        # paints the whole vehicle or none of it, and which one depends on the part.
        ranked = sorted(field)
        last = len(ranked) - 1
        high = ranked[min(last, int(last * (1.0 - cover[0])))]
        low = ranked[min(last, int(last * cover[1]))]
        spread = ranked[int(last * 0.84)] - ranked[int(last * 0.16)]
        feather = max(1e-4, spread * 0.13)

    for i, vert in enumerate(mesh.vertices):
        co = world[i]
        tint = [1.0, 1.0, 1.0]

        if camo:
            n = field[i]
            if n > high:
                patch, edge = camo[0], (n - high) / feather
            elif n < low:
                patch, edge = camo[min(1, len(camo) - 1)], (low - n) / feather
            else:
                patch = None
            if patch:
                # Feather the edge so a patch fades in rather than snapping on.
                edge = min(1.0, edge)
                for c in range(3):
                    tint[c] *= 1.0 + (patch[c] - 1.0) * edge

        if dirt > 0:
            height = (co.z - ground) / max(0.001, top - ground)
            mud = max(0.0, 1.0 - height * 2.6) * dirt
            speckle = 0.7 + 0.6 * _noise3(co.x * 3.1, co.y * 3.1, co.z * 3.1, seed + 31)
            mud *= speckle
            # Mud is browner as well as darker.
            tint[0] *= 1.0 - mud * 0.55
            tint[1] *= 1.0 - mud * 0.62
            tint[2] *= 1.0 - mud * 0.78

        if wear > 0 and counts[i]:
            sharp = max(0.0, convex[i] / counts[i])
            rub = min(1.0, sharp * 3.4) * wear
            for c in range(3):
                tint[c] = tint[c] * (1.0 - rub) + 1.28 * rub

        r, g, b = _stored(tint)
        layer.data[i].color = (r, g, b, 1.0)
    return obj


def occlude(names, samples=48, strength=0.85, distance=0.9, isolate=False):
    """
    Ray-traces ambient occlusion over the whole vehicle and folds it into the tint.

    The game can bake its own occlusion, and does for everything it builds procedurally,
    but that is a voxel grid and a handful of rays per vertex decided while the player
    waits. Here there is no hurry and there is a real ray tracer, so a modelled vehicle
    arrives with its creases and corners already shaded and needs none of that work at
    run time - which is most of the wait when a vehicle is opened.

    Must run after [weather], because it multiplies into the same attribute.

    @param distance how far a ray looks before it counts as open sky, in metres.
    @param isolate hides every other object during the bake. Internals live inside a
        closed hull, so baked against it they come out uniformly black - which is the
        opposite of useful in an X-ray view, where the shell is ghosted away and the
        engine is what you are looking at.
    """
    scene = bpy.context.scene
    scene.render.engine = "CYCLES"
    scene.cycles.samples = samples
    scene.cycles.use_denoising = False
    scene.render.bake.target = "VERTEX_COLORS"
    # Occlusion has to be local to be useful. Left unbounded, a ray from the middle of
    # the glacis escapes to the sky and every flat panel bakes pure white - the contact
    # shadows where parts meet are the whole point, and they live within a metre.
    if scene.world is None:
        scene.world = bpy.data.worlds.new("BakeWorld")
    scene.world.light_settings.distance = distance

    objects = [bpy.data.objects.get(n) for n in names]
    objects = [o for o in objects if o is not None]
    if not objects:
        return

    # Cycles bakes into whatever colour attribute is active, so the weathering has to be
    # moved out of the way first and multiplied back in afterwards.
    for obj in objects:
        if "AO" not in obj.data.color_attributes:
            obj.data.color_attributes.new(name="AO", type="FLOAT_COLOR", domain="POINT")
        obj.data.color_attributes.active_color = obj.data.color_attributes["AO"]

    hidden = []
    if isolate:
        chosen = set(objects)
        for other in bpy.context.scene.objects:
            if other.type == "MESH" and other not in chosen and not other.hide_render:
                other.hide_render = True
                hidden.append(other)

    bpy.ops.object.select_all(action="DESELECT")
    for obj in objects:
        obj.select_set(True)
    bpy.context.view_layer.objects.active = objects[0]
    bpy.ops.object.bake(type="AO")

    for other in hidden:
        other.hide_render = False

    for obj in objects:
        ao = obj.data.color_attributes["AO"]
        col = obj.data.color_attributes.get("Col")
        for i, item in enumerate(ao.data):
            # Cycles reports the AO pass linearly; 1 is open sky, 0 is buried.
            shade = 1.0 - (1.0 - min(1.0, max(0.0, item.color[0]))) * strength
            if col is not None:
                c = col.data[i].color
                col.data[i].color = (c[0] * shade, c[1] * shade, c[2] * shade, 1.0)
        obj.data.color_attributes.remove(ao)
        if col is not None:
            obj.data.color_attributes.active_color = obj.data.color_attributes["Col"]
    print("OCCLUDED", len(objects), "parts")


# ---------------------------------------------------------------------------
# Markings
# ---------------------------------------------------------------------------

def tint_for(target, base):
    """
    The multiplier that turns a palette colour into [target].

    Markings cannot be a material, because the app repaints every material from the
    vehicle's own palette - a white cross would come out dunkelgelb. They ride in the
    vertex colour instead, which the app multiplies rather than replaces, so the value
    baked here has to be worked back from the colour the app will supply.
    """
    return tuple(min(TINT_RANGE, t / max(0.02, b)) for t, b in zip(target, base))


def flat_tint(obj, tint):
    """Gives every vertex of [obj] the same colour multiplier."""
    mesh = obj.data
    layer = mesh.color_attributes.get("Col")
    if layer is None:
        layer = mesh.color_attributes.new(name="Col", type="FLOAT_COLOR", domain="POINT")
    r, g, b = _stored(tint)
    for item in layer.data:
        item.color = (r, g, b, 1.0)
    return obj


# Stencil numerals, as the strokes that make up each digit. Each stroke is
# (x, z, width, height) in a 1 x 2 box centred on the origin - a seven-segment layout,
# which is what a stencil cut from sheet metal actually looks like.
_SEGMENTS = {
    "top": (0.0, 1.0, 1.0, 0.0), "mid": (0.0, 0.0, 1.0, 0.0), "bot": (0.0, -1.0, 1.0, 0.0),
    "ul": (-0.5, 0.5, 0.0, 1.0), "ur": (0.5, 0.5, 0.0, 1.0),
    "ll": (-0.5, -0.5, 0.0, 1.0), "lr": (0.5, -0.5, 0.0, 1.0),
}
_DIGITS = {
    "0": ("top", "bot", "ul", "ur", "ll", "lr"),
    "1": ("ur", "lr"),
    "2": ("top", "ur", "mid", "ll", "bot"),
    "3": ("top", "ur", "mid", "lr", "bot"),
    "4": ("ul", "ur", "mid", "lr"),
    "5": ("top", "ul", "mid", "lr", "bot"),
    "6": ("top", "ul", "mid", "ll", "lr", "bot"),
    "7": ("top", "ur", "lr"),
    "8": ("top", "mid", "bot", "ul", "ur", "ll", "lr"),
    "9": ("top", "ul", "ur", "mid", "lr", "bot"),
}


def numerals(name, text, center, height, depth, mat, stroke=0.16, gap=0.28):
    """
    Stencilled numbers, standing a little proud of the plate.

    Soviet tanks carried a tactical number on the turret and nothing else; aircraft and
    ships carry codes and pennant numbers the same way. Painted-on text is the one
    marking that cannot be a shape - it has to be built from strokes.

    Built about the origin and positioned by the object transform, so the caller can lay
    it against a sloping plate with [rotate].
    """
    half = height * 0.5
    unit = half * stroke
    advance = height * (0.5 + gap)
    span = advance * (len(text) - 1)
    parts = []
    for i, ch in enumerate(text):
        x0 = -span * 0.5 + i * advance
        for seg in _DIGITS.get(ch, ()):
            sx, sz, sw, sh = _SEGMENTS[seg]
            parts.append(box(
                name,
                (x0 + sx * half * 0.5, 0.0, sz * half * 0.5),
                (sw * half * 0.5 + unit, depth, sh * half * 0.5 + unit),
                mat,
            ))
    first = parts[0]
    for other in parts[1:]:
        cut(first, other, "UNION")
    first.location = Vector(center)
    return first


def decal(name, host, cutter, mat, centre=(0.0, 0.0, 0.0), swell=1.006):
    """
    A marking that follows the surface it is painted on.

    A flat disc works on a wing and nowhere else: on a fuselage it stands proud in the
    middle and sinks below the skin at its edges, which is exactly how a roundel gives
    away that it was stuck on rather than painted. This takes a copy of the host, keeps
    only the part inside [cutter], and swells it a few millimetres about [centre] so it
    sits just clear of the skin it was cut from.
    """
    patch = host.copy()
    patch.data = host.data.copy()
    patch.name = name
    bpy.context.scene.collection.objects.link(patch)
    cut(patch, cutter, "INTERSECT")
    origin = Vector(centre)
    for v in patch.data.vertices:
        v.co = origin + (v.co - origin) * swell
    if mat:
        paint(patch, mat)
    return patch


def recalc(obj):
    """Rebuilds face winding from the geometry. Needed after any axis permutation."""
    bm = bmesh.new()
    bm.from_mesh(obj.data)
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    bm.to_mesh(obj.data)
    bm.free()
    return obj


def duplicate(name, obj):
    """An independent copy of [obj], linked into the scene."""
    copy = obj.copy()
    copy.data = obj.data.copy()
    copy.name = name
    bpy.context.scene.collection.objects.link(copy)
    return copy


def star(name, center, radius, depth, mat, inner=0.42):
    """
    A five-pointed star, built about the origin so it can be laid on a sloping plate.
    """
    profile = []
    for i in range(10):
        a = math.radians(90 + i * 36)
        r = radius if i % 2 == 0 else radius * inner
        profile.append((r * math.cos(a), r * math.sin(a)))
    obj = prism(name, profile, -depth * 0.5, depth * 0.5, mat)
    obj.location = Vector(center)
    return obj


def cross(name, center, arm, thickness, depth, mat, axis="Y"):
    """
    A bar cross - the plan shape, not the paint.

    Two overlapping slabs. [arm] is the half-length of an arm and [thickness] its half
    width, so a Balkenkreuz is a cross with fat arms rather than four thin lines.
    """
    if axis == "Y":            # on a fuselage side: the cross lies in x/z
        bar = (arm * 2, depth, thickness * 2)
        post = (thickness * 2, depth, arm * 2)
    elif axis == "Z":          # on a wing or a deck: the cross lies in x/y
        bar = (arm * 2, thickness * 2, depth)
        post = (thickness * 2, arm * 2, depth)
    else:                      # head-on: the cross lies in y/z
        bar = (depth, arm * 2, thickness * 2)
        post = (depth, thickness * 2, arm * 2)
    a = box(name, center, bar, mat)
    b = box(name + "_v", center, post, mat)
    cut(a, b, "UNION")
    return a


# ---------------------------------------------------------------------------
# Output
# ---------------------------------------------------------------------------

def studio(target_size=6.0, floor_z=0.0):
    """
    Three-point lighting and a floor, sized to the vehicle.

    [floor_z] drops the floor for anything modelled about its own centreline rather than
    about the ground - an aeroplane's thrust line is z = 0, so half of it is buried
    unless the floor moves down out of the way.
    """
    bpy.ops.mesh.primitive_plane_add(size=target_size * 8, location=(0, 0, floor_z))
    floor = bpy.context.object
    floor.name = "Floor"
    paint(floor, material("Floor", (0.028, 0.033, 0.040), roughness=0.85))

    key = _light("Key", "AREA", (target_size * 0.9, -target_size * 1.1, target_size * 1.2),
                 energy=target_size * target_size * 90, size=target_size)
    key.rotation_euler = (math.radians(50), 0, math.radians(40))
    fill = _light("Fill", "AREA", (-target_size, -target_size * 0.7, target_size * 0.6),
                  energy=target_size * target_size * 24, size=target_size * 1.6)
    fill.rotation_euler = (math.radians(70), 0, math.radians(-55))
    rim = _light("Rim", "AREA", (-target_size * 0.6, target_size * 1.4, target_size * 0.9),
                 energy=target_size * target_size * 45, size=target_size)
    rim.rotation_euler = (math.radians(60), 0, math.radians(200))

    world = bpy.data.worlds.new("World")
    world.use_nodes = True
    world.node_tree.nodes["Background"].inputs[0].default_value = (0.035, 0.042, 0.055, 1)
    world.node_tree.nodes["Background"].inputs[1].default_value = 1.0
    bpy.context.scene.world = world


def _light(name, kind, location, energy, size):
    bpy.ops.object.light_add(type=kind, location=location)
    obj = bpy.context.object
    obj.name = name
    obj.data.energy = energy
    if kind == "AREA":
        obj.data.size = size
    return obj


def bounds():
    """World-space bounding box of every mesh in the scene, ignoring the floor."""
    lo = [1e9] * 3
    hi = [-1e9] * 3
    for obj in bpy.context.scene.objects:
        if obj.type != "MESH" or obj.name == "Floor":
            continue
        for corner in obj.bound_box:
            world = obj.matrix_world @ Vector(corner)
            for i in range(3):
                lo[i] = min(lo[i], world[i])
                hi[i] = max(hi[i], world[i])
    return lo, hi


def camera(azimuth=36, elevation=20, lens=45, margin=1.10):
    """
    Frames whatever is in the scene.

    Width and height are solved separately and the larger distance wins. Using a single
    bounding-sphere radius against the narrower field of view pushes the camera much too
    far back for a shape like a tank, which is long and wide but not tall.
    """
    lo, hi = bounds()
    centre = Vector(((lo[0] + hi[0]) / 2, (lo[1] + hi[1]) / 2, (lo[2] + hi[2]) / 2))
    dx, dy, dz = (hi[i] - lo[i] for i in range(3))

    e = math.radians(elevation)
    half_w = 0.5 * math.sqrt(dx * dx + dy * dy)
    half_h = 0.5 * dz * math.cos(e) + half_w * math.sin(e)

    scene = bpy.context.scene
    aspect = scene.render.resolution_x / max(1, scene.render.resolution_y)
    half_x = math.atan(18.0 / lens)
    half_y = math.atan(18.0 / lens / aspect)
    distance = max(half_w / math.tan(half_x), half_h / math.tan(half_y)) * margin

    bpy.ops.object.camera_add(location=(0, 0, 0))
    cam = bpy.context.object
    cam.data.lens = lens
    a = math.radians(azimuth)
    cam.location = (
        centre.x + distance * math.cos(e) * math.cos(a),
        centre.y - distance * math.cos(e) * math.sin(a),
        centre.z + distance * math.sin(e),
    )
    bpy.ops.object.empty_add(location=centre)
    target = bpy.context.object
    target.name = "CamTarget"
    cam.constraints.new("TRACK_TO").target = target
    scene.camera = cam
    return cam


def render(path, width=1100, height=700, samples=32):
    scene = bpy.context.scene
    scene.render.engine = "BLENDER_EEVEE_NEXT"
    scene.eevee.taa_render_samples = samples
    scene.render.resolution_x = width
    scene.render.resolution_y = height
    scene.render.filepath = path
    scene.render.image_settings.file_format = "PNG"
    scene.view_settings.view_transform = "AgX"
    scene.view_settings.look = "AgX - Medium High Contrast"
    bpy.ops.render.render(write_still=True)
    print("RENDERED", path)


def export_glb(path, only=None):
    """Exports the vehicle. Part names survive as glTF node names."""
    bpy.ops.object.select_all(action="DESELECT")
    for obj in bpy.context.scene.objects:
        if obj.type != "MESH":
            continue
        if obj.name in ("Floor",):
            continue
        if only is not None and obj.name not in only:
            continue
        obj.select_set(True)
    bpy.ops.export_scene.gltf(
        filepath=path,
        export_format="GLB",
        use_selection=True,
        export_apply=True,
        export_yup=True,
        export_normals=True,
        export_materials="EXPORT",
        export_texcoords=False,
        export_vertex_color="NAME",
        export_vertex_color_name="Col",
        export_all_vertex_colors=False,
        export_active_vertex_color_when_no_material=True,
    )
    print("EXPORTED", path)
