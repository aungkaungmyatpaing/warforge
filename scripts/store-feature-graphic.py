"""
Renders the 1024x500 Play Store feature graphic.

    /Applications/Blender.app/Contents/MacOS/Blender --background \
      --python scripts/store-feature-graphic.py

Uses the app's own 3D render as the hero art rather than the flat launcher icon, because
the vehicles are what the game is: a silhouette of a tank says "a game with a tank in
it", and a Panzer IV in its real camouflage says what this one actually looks like.

Blender is here for the same reason as in store-screenshots.py - it is the only thing on
this machine that can draw text into an image.
"""
import os

import bpy

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
HERO = os.path.join(ROOT, "app", "build", "render3d", "panzer_iv-close.png")
OUT = os.path.join(ROOT, "playstore", "feature-graphic-1024x500.png")

W, H = 1024, 500
VIEW_W = 2.0                       # landscape: ortho_scale is the width
VIEW_H = VIEW_W * H / W

BG_LEFT = (0.030, 0.048, 0.068, 1.0)
BG_RIGHT = (0.052, 0.078, 0.104, 1.0)
INK = (0.93, 0.95, 0.96, 1.0)
GOLD = (0.91, 0.65, 0.23, 1.0)

FONT_BOLD = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
FONT_REG = "/System/Library/Fonts/Supplemental/Arial.ttf"


def emission(name, colour, image=None):
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    nodes, links = mat.node_tree.nodes, mat.node_tree.links
    nodes.clear()
    out = nodes.new("ShaderNodeOutputMaterial")
    emit = nodes.new("ShaderNodeEmission")
    if image is not None:
        tex = nodes.new("ShaderNodeTexImage")
        tex.image = image
        tex.interpolation = "Cubic"
        links.new(tex.outputs["Color"], emit.inputs["Color"])
        links.new(emit.outputs["Emission"], out.inputs["Surface"])
        mat.surface_render_method = "DITHERED"
        return mat

    emit.inputs[0].default_value = colour[:3] + (1.0,)
    if len(colour) > 3 and colour[3] < 1.0:
        # A translucent scrim: mix the emission with transparency so the hero shows
        # through it rather than being cut off by a hard edge.
        mix = nodes.new("ShaderNodeMixShader")
        clear = nodes.new("ShaderNodeBsdfTransparent")
        mix.inputs[0].default_value = colour[3]
        links.new(clear.outputs[0], mix.inputs[1])
        links.new(emit.outputs["Emission"], mix.inputs[2])
        links.new(mix.outputs[0], out.inputs["Surface"])
        mat.surface_render_method = "BLENDED"
        return mat

    links.new(emit.outputs["Emission"], out.inputs["Surface"])
    mat.surface_render_method = "DITHERED"
    return mat


def plane(name, cx, cy, w, h, mat, z=0.0):
    bpy.ops.mesh.primitive_plane_add(size=1, location=(cx, cy, z))
    obj = bpy.context.object
    obj.name = name
    obj.scale = (w, h, 1)
    obj.data.materials.append(mat)
    return obj


def text(body, cx, cy, size, font, colour, align="LEFT"):
    bpy.ops.object.text_add(location=(cx, cy, 0.2))
    obj = bpy.context.object
    obj.data.body = body
    obj.data.size = size
    obj.data.align_x = align
    obj.data.align_y = "CENTER"
    if os.path.exists(font):
        obj.data.font = bpy.data.fonts.load(font)
    obj.data.materials.append(emission("t", colour))
    return obj


def main():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    scene = bpy.context.scene
    scene.render.engine = "BLENDER_EEVEE_NEXT"
    scene.render.resolution_x, scene.render.resolution_y = W, H
    scene.render.resolution_percentage = 100
    scene.eevee.taa_render_samples = 16
    scene.view_settings.view_transform = "Standard"
    scene.view_settings.look = "None"
    scene.render.image_settings.color_mode = "RGB"

    bpy.ops.object.camera_add(location=(0, 0, 10))
    cam = bpy.context.object
    cam.data.type = "ORTHO"
    cam.data.ortho_scale = VIEW_W          # landscape, so the width is the larger side
    scene.camera = cam

    left, right = -VIEW_W / 2, VIEW_W / 2

    # One flat ground, the same navy the app render already has behind the vehicle, so
    # the two meet with no visible seam and no scrim is needed to hide one.
    plane("bg", 0, 0, VIEW_W, VIEW_H, emission("bg", BG_RIGHT), z=-1)

    # Hero on the right, bled off the edge, clear of the type.
    if os.path.exists(HERO):
        img = bpy.data.images.load(HERO)
        hero_h = VIEW_H * 1.16
        hero_w = hero_h * (img.size[0] / img.size[1])
        plane("hero", 0.46, -VIEW_H * 0.02, hero_w, hero_h,
              emission("hero", None, image=img), z=-0.5)

    # Wordmark and strapline, left.
    text("WARFORGE", left + 0.10, VIEW_H * 0.18, 0.150, FONT_BOLD, INK)
    plane("rule", left + 0.10 + 0.32, VIEW_H * 0.015, 0.64, 0.007,
          emission("rule", GOLD), z=0.2)
    text("Build it. Then see inside it.", left + 0.10, -VIEW_H * 0.16, 0.060,
         FONT_REG, GOLD)

    scene.render.filepath = OUT
    scene.render.image_settings.file_format = "PNG"
    bpy.ops.render.render(write_still=True)
    print("WROTE", OUT)


if __name__ == "__main__":
    main()
