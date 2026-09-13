"""Toolchain check: build a bevelled, subdivided solid and render it."""
import bpy, sys, os

out = sys.argv[sys.argv.index("--") + 1]

bpy.ops.wm.read_factory_settings(use_empty=True)

bpy.ops.mesh.primitive_cube_add(size=2)
cube = bpy.context.object
bev = cube.modifiers.new("Bevel", "BEVEL")
bev.width = 0.08
bev.segments = 4
sub = cube.modifiers.new("Subsurf", "SUBSURF")
sub.levels = 1
sub.render_levels = 1

mat = bpy.data.materials.new("Steel")
mat.use_nodes = True
bsdf = mat.node_tree.nodes["Principled BSDF"]
bsdf.inputs["Base Color"].default_value = (0.55, 0.50, 0.34, 1)
bsdf.inputs["Roughness"].default_value = 0.55
bsdf.inputs["Metallic"].default_value = 0.2
cube.data.materials.append(mat)

bpy.ops.object.light_add(type="AREA", location=(3, -4, 5))
bpy.context.object.data.energy = 900
bpy.ops.object.camera_add(location=(4.5, -5.5, 3.4), rotation=(1.05, 0, 0.69))
bpy.context.scene.camera = bpy.context.object

scene = bpy.context.scene
scene.render.engine = "BLENDER_EEVEE_NEXT"
scene.render.resolution_x = 640
scene.render.resolution_y = 420
scene.render.filepath = out
scene.render.image_settings.file_format = "PNG"
bpy.ops.render.render(write_still=True)
print("RENDERED", out)
