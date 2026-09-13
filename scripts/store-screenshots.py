"""
Turns raw device captures into captioned store frames.

    /Applications/Blender.app/Contents/MacOS/Blender --background \
      --python scripts/store-screenshots.py

Blender is here only because it is the one thing on this machine that can draw text into
an image: there is no Pillow, no ImageMagick and no pyobjc. It loads each capture as a
textured plane, sets a caption above it, and renders the pair out at the size Play wants.

The captures themselves must be taken with `-PscreenshotMode=true`, which hides the ad
slot - Google rejects listing images that contain advertising.
"""
import os
import sys

import bpy

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RAW = os.path.join(ROOT, "playstore", "screenshots")
OUT = os.path.join(ROOT, "playstore", "listing-screenshots")

W, H = 1080, 1920

# Working units: the camera sees VIEW_H tall and VIEW_W wide, so y runs -1..+1.
VIEW_H = 2.0
VIEW_W = VIEW_H * W / H
BG_TOP = (0.035, 0.055, 0.075, 1.0)
BG_BOTTOM = (0.055, 0.082, 0.110, 1.0)
INK = (0.91, 0.93, 0.95, 1.0)
GOLD = (0.91, 0.65, 0.23, 1.0)

# Eight frames is Play's maximum for phone screenshots, and the order is the order a
# player meets them in: pick a vehicle, build it, then take it apart.
FRAMES = [
    ("01-hangar.png", "Twenty-one vehicles", "1917 to the present day"),
    ("08-build.png", "Assemble it yourself", "Engine first, then everything over it"),
    ("02-museum-solid.png", "Built to real dimensions", "Modelled in 3D, in the markings they wore"),
    ("03-xray.png", "See straight through it", "Engine, crew, ammunition, fuel, armour"),
    ("04-cutaway.png", "Slide it open", "Every module where it really was"),
    ("05-history.png", "Read why", "Not specifications - the arguments behind them"),
    ("06-specs.png", "The numbers that mattered", "Side by side, in context"),
    ("07-quiz.png", "Then test yourself", "Questions drawn from what the cutaway shows"),
]

FONT_BOLD = "/System/Library/Fonts/Supplemental/Arial Bold.ttf"
FONT_REG = "/System/Library/Fonts/Supplemental/Arial.ttf"


def reset():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    scene = bpy.context.scene
    scene.render.engine = "BLENDER_EEVEE_NEXT"
    scene.render.resolution_x = W
    scene.render.resolution_y = H
    scene.render.resolution_percentage = 100
    scene.render.film_transparent = False
    scene.eevee.taa_render_samples = 8
    # No tone mapping: these are flat graphics, and AgX would wash the colours out.
    scene.view_settings.view_transform = "Standard"
    scene.view_settings.look = "None"

    bpy.ops.object.camera_add(location=(0, 0, 10))
    cam = bpy.context.object
    cam.data.type = "ORTHO"
    # ortho_scale is the LARGER sensor dimension, which for a portrait render is the
    # height - not the width. Assuming width is how the first pass came out 1.7x
    # oversized with the caption pushed off the top of the frame.
    cam.data.ortho_scale = VIEW_H
    scene.camera = cam


def emission(name, colour, image=None):
    mat = bpy.data.materials.new(name)
    mat.use_nodes = True
    nodes = mat.node_tree.nodes
    links = mat.node_tree.links
    nodes.clear()
    out = nodes.new("ShaderNodeOutputMaterial")
    emit = nodes.new("ShaderNodeEmission")
    emit.inputs[1].default_value = 1.0
    if image is not None:
        tex = nodes.new("ShaderNodeTexImage")
        tex.image = image
        tex.interpolation = "Cubic"
        links.new(tex.outputs["Color"], emit.inputs["Color"])
    else:
        emit.inputs[0].default_value = colour
    links.new(emit.outputs["Emission"], out.inputs["Surface"])
    # screencap writes RGBA. Left on the default blend mode the capture is composited
    # against whatever is behind it, and the background band shows through it as a seam.
    mat.surface_render_method = "DITHERED"
    return mat


def plane(name, cx, cy, w, h, mat, z=0.0):
    bpy.ops.mesh.primitive_plane_add(size=1, location=(cx, cy, z))
    obj = bpy.context.object
    obj.name = name
    obj.scale = (w, h, 1)
    obj.rotation_euler = (0, 0, 0)
    obj.data.materials.append(mat)
    return obj


def text(body, cx, cy, size, font_path, colour, align="CENTER"):
    bpy.ops.object.text_add(location=(cx, cy, 0.1))
    obj = bpy.context.object
    obj.data.body = body
    obj.data.size = size
    obj.data.align_x = align
    obj.data.align_y = "CENTER"
    if os.path.exists(font_path):
        obj.data.font = bpy.data.fonts.load(font_path)
    obj.data.materials.append(emission("txt", colour))
    return obj


def frame(source, title, subtitle, out_path):
    reset()
    shot_aspect = H / W

    # Background: two bands, the same pair the app's own screens use.
    plane("bg", 0, 0, VIEW_W, VIEW_H, emission("bg", BG_BOTTOM), z=-1)
    # Only behind the caption - never behind the capture, or it shows through.
    plane("bgtop", 0, 0.8575, VIEW_W, 0.285, emission("bgtop", BG_TOP), z=-0.9)

    # Caption band across the top.
    text(title, 0, 0.885, 0.072, FONT_BOLD, INK)
    text(subtitle, 0, 0.800, 0.040, FONT_REG, GOLD)

    # The capture fills everything below the band, as large as it will go.
    top = 0.715
    avail_h = top - (-1.0)
    shot_h = avail_h * 0.97
    shot_w = shot_h / shot_aspect
    if shot_w > VIEW_W * 0.94:
        shot_w = VIEW_W * 0.94
        shot_h = shot_w * shot_aspect
    img = bpy.data.images.load(os.path.join(RAW, source))
    plane("shot", 0, top - shot_h * 0.5, shot_w, shot_h,
          emission("shot", None, image=img))

    bpy.context.scene.render.filepath = out_path
    bpy.context.scene.render.image_settings.file_format = "PNG"
    bpy.context.scene.render.image_settings.color_mode = "RGB"
    bpy.ops.render.render(write_still=True)
    print("WROTE", out_path)


def main():
    os.makedirs(OUT, exist_ok=True)
    missing = [f for f, _, _ in FRAMES if not os.path.exists(os.path.join(RAW, f))]
    if missing:
        print("MISSING captures:", missing)
        sys.exit(1)
    for i, (source, title, subtitle) in enumerate(FRAMES, start=1):
        frame(source, title, subtitle, os.path.join(OUT, "%02d.png" % i))


if __name__ == "__main__":
    main()
