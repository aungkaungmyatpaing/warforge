import os, sys
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import bpy, wf
from mathutils import Vector
import panzer_iv

panzer_iv.build()
print("%-14s %7s %7s %7s   %7s %7s %7s" % ("object", "x0", "x1", "y0", "y1", "z0", "z1"))
for obj in sorted(bpy.context.scene.objects, key=lambda o: o.name):
    if obj.type != "MESH":
        continue
    lo = [1e9]*3; hi = [-1e9]*3
    for c in obj.bound_box:
        w = obj.matrix_world @ Vector(c)
        for i in range(3):
            lo[i] = min(lo[i], w[i]); hi[i] = max(hi[i], w[i])
    print("%-14s %7.2f %7.2f %7.2f   %7.2f %7.2f %7.2f  tris=%d" % (
        obj.name, lo[0], hi[0], lo[1], hi[1], lo[2], hi[2], len(obj.data.polygons)))
