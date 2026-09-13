#!/usr/bin/env bash
#
# Creates the release signing key and wires it into ~/.gradle/gradle.properties.
#
#     scripts/create-keystore.sh
#
# You type the password; it goes to keytool and to your own gradle.properties and
# nowhere else. Nothing here is committed - gradle.properties lives in your home
# directory and *.jks is in .gitignore.
#
# READ THIS FIRST
# ---------------
# Google Play identifies an app by the key it was signed with, for as long as the app
# exists. If you lose this file you cannot update the app, ever - you would have to
# publish a new listing under a new package name and abandon every player you had.
#
# Two things follow from that:
#   1. Back the .jks up somewhere that is not this laptop.
#   2. Turn on Play App Signing in the Console (it is the default for new apps). Google
#      then holds the real key and this one becomes an *upload* key, which can be reset
#      if it is lost. Do that and the paragraph above stops being frightening.
set -euo pipefail
cd "$(dirname "$0")/.."

KEYSTORE="${KEYSTORE:-$HOME/keys/warforge.jks}"
ALIAS="${ALIAS:-warforge}"
GP="$HOME/.gradle/gradle.properties"

if [ -f "$KEYSTORE" ]; then
  echo "A keystore already exists at $KEYSTORE"
  echo "Refusing to overwrite it - that would destroy your ability to update the app."
  echo "Delete it yourself first if you are certain it was never used to publish."
  exit 1
fi

command -v keytool >/dev/null || { echo "keytool not found - install a JDK" >&2; exit 1; }

mkdir -p "$(dirname "$KEYSTORE")"
chmod 700 "$(dirname "$KEYSTORE")"

echo "Creating $KEYSTORE"
echo
echo "keytool will ask for a password and for your name and organisation."
echo "The name fields are cosmetic; the password is not. Use a long one and put it in"
echo "your password manager before you continue."
echo

# keytool prompts for the password itself, on the terminal. It is never passed as an
# argument, so it cannot end up in your shell history or in the process list.
keytool -genkeypair -v \
  -keystore "$KEYSTORE" \
  -alias "$ALIAS" \
  -keyalg RSA -keysize 4096 \
  -validity 10000

chmod 600 "$KEYSTORE"
echo
echo "Created. Now telling Gradle where it is."
echo

# Re-ask for the passwords so they can be written to gradle.properties. Typed with
# `read -s`, so they are not echoed and not stored in history.
read -r -s -p "Keystore password (again, for gradle.properties): " STORE_PASS; echo
read -r -s -p "Key password (press Enter if it is the same): " KEY_PASS; echo
KEY_PASS="${KEY_PASS:-$STORE_PASS}"

mkdir -p "$(dirname "$GP")"
touch "$GP"
chmod 600 "$GP"

# Replace any previous block rather than appending a second one.
tmp=$(mktemp)
grep -v -E '^warforge(StoreFile|StorePassword|KeyAlias|KeyPassword)=' "$GP" > "$tmp" || true
{
  echo ""
  echo "# Warforge release signing - created $(date +%Y-%m-%d). Never commit this file."
  echo "warforgeStoreFile=$KEYSTORE"
  echo "warforgeStorePassword=$STORE_PASS"
  echo "warforgeKeyAlias=$ALIAS"
  echo "warforgeKeyPassword=$KEY_PASS"
} >> "$tmp"
mv "$tmp" "$GP"
chmod 600 "$GP"

unset STORE_PASS KEY_PASS

echo
echo "Done."
echo "  keystore          $KEYSTORE   (chmod 600)"
echo "  gradle properties $GP         (chmod 600)"
echo
echo "Verify the key:"
echo "  keytool -list -v -keystore $KEYSTORE -alias $ALIAS"
echo
echo "Then build the bundle:"
echo "  ./gradlew bundleRelease"
echo
echo "BACK UP $KEYSTORE NOW. Losing it means never being able to update the app."
