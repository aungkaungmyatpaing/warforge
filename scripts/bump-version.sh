#!/usr/bin/env bash
# Bumps the app version in app/build.gradle.kts.
#
#   scripts/bump-version.sh 1.1        -> versionCode + 1, versionName 1.1
#
# The version lives in exactly one place. version.json is generated from it by the
# publish-version workflow, so there is nothing to keep in sync by hand.
set -euo pipefail
cd "$(dirname "$0")/.."

name="${1:-}"
if [ -z "$name" ]; then
  echo "usage: scripts/bump-version.sh <versionName>   e.g. 1.1" >&2
  exit 2
fi

gradle=app/build.gradle.kts
code=$(grep -oE 'versionCode[[:space:]]*=[[:space:]]*[0-9]+' "$gradle" | grep -oE '[0-9]+')
next=$((code + 1))

# BSD and GNU sed disagree about -i, so write to a temp file and move it back.
tmp=$(mktemp)
sed -E "s/(versionCode[[:space:]]*=[[:space:]]*)[0-9]+/\1$next/; \
        s/(versionName[[:space:]]*=[[:space:]]*\")[^\"]+(\")/\1$name\2/" "$gradle" > "$tmp"
mv "$tmp" "$gradle"

echo "versionCode $code -> $next"
echo "versionName -> $name"
echo
echo "Next:"
echo "  1. ./gradlew bundleRelease          # build the .aab"
echo "  2. upload it to the Play Console and roll it out"
echo "  3. once it is LIVE:  git tag v$name && git push origin v$name"
echo "     (that runs the publish-version workflow, which updates version.json)"
