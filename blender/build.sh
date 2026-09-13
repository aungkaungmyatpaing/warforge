#!/usr/bin/env bash
# Re-exports every Blender model into the app's assets.
#
# Models are scripted rather than saved as .blend files, so this is the build step that
# turns them into something the game can load.
set -euo pipefail

BLENDER="${BLENDER:-/Applications/Blender.app/Contents/MacOS/Blender}"
HERE="$(cd "$(dirname "$0")" && pwd)"
ASSETS="$HERE/../app/src/main/assets/models"
PREVIEWS="${PREVIEWS:-$HERE/../app/build/blender}"

mkdir -p "$ASSETS" "$PREVIEWS"

for script in "$HERE"/*.py; do
    name="$(basename "$script" .py)"
    case "$name" in
        wf|intkit|inspect|smoke) continue ;;
    esac
    echo "--- $name"
    "$BLENDER" --background --python "$script" -- \
        "$PREVIEWS/$name.png" "$ASSETS/$name.glb" | grep -E "EXPORTED|Error" || true
done

echo "models in $ASSETS"
ls -lh "$ASSETS"
