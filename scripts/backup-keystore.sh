#!/usr/bin/env bash
#
# Makes an encrypted, verifiable backup of the release signing key.
#
#     scripts/backup-keystore.sh                  # create a backup
#     scripts/backup-keystore.sh --verify FILE    # prove a backup actually restores
#
# Why encrypted: the keystore alone is useless without its password, but a plain copy on
# a cloud drive is still a key somebody else could try passwords against at their leisure.
#
# Why verifiable: an unverified backup is not a backup. The --verify mode decrypts into a
# temporary directory, asks keytool to read the key out of it, and throws the copy away.
# That is the only way to know the file you are keeping is the file you think it is.
set -euo pipefail

KEYSTORE="${KEYSTORE:-$HOME/keys/warforge.jks}"
ALIAS="${ALIAS:-warforge}"

verify() {
  local archive="$1"
  [ -f "$archive" ] || { echo "No such file: $archive" >&2; exit 1; }
  local tmp
  tmp=$(mktemp -d)
  trap 'rm -rf "$tmp"' EXIT

  echo "Decrypting $archive"
  echo "Enter the ARCHIVE password (the one you chose when making the backup):"
  openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 -in "$archive" -out "$tmp/restored.jks"

  echo
  echo "Checking the restored file is a working keystore."
  echo "Enter the KEYSTORE password (the one you chose when making the key):"
  keytool -list -v -keystore "$tmp/restored.jks" -alias "$ALIAS" | \
    grep -E "Alias name|Valid from|Certificate fingerprints|SHA-256" || true

  echo
  echo "Fingerprint of the restored copy:"
  shasum -a 256 "$tmp/restored.jks" | awk '{print "  " $1}'
  if [ -f "$KEYSTORE" ]; then
    echo "Fingerprint of the live keystore:"
    shasum -a 256 "$KEYSTORE" | awk '{print "  " $1}'
    if [ "$(shasum -a 256 < "$tmp/restored.jks")" = "$(shasum -a 256 < "$KEYSTORE")" ]; then
      echo
      echo "MATCH. This backup restores exactly the key you are signing with."
    else
      echo
      echo "MISMATCH - this backup is NOT the key you are currently signing with." >&2
      exit 1
    fi
  fi
}

if [ "${1:-}" = "--verify" ]; then
  verify "${2:?usage: scripts/backup-keystore.sh --verify <archive>}"
  exit 0
fi

[ -f "$KEYSTORE" ] || { echo "No keystore at $KEYSTORE" >&2; exit 1; }

STAMP=$(date +%Y%m%d)
OUT="$HOME/Desktop/warforge-keystore-$STAMP.enc"

echo "Backing up $KEYSTORE"
echo
echo "Choose a password for the ARCHIVE. It can be the same as the keystore password,"
echo "but a different one means a stolen archive is useless even to somebody who has"
echo "already seen the keystore password."
echo

openssl enc -aes-256-cbc -pbkdf2 -iter 600000 -salt -in "$KEYSTORE" -out "$OUT"
chmod 600 "$OUT"

cat > "$HOME/Desktop/warforge-keystore-$STAMP.txt" <<TXT
Warforge Android release signing key
Backed up $(date "+%Y-%m-%d %H:%M")

Archive        warforge-keystore-$STAMP.enc
Encryption     openssl aes-256-cbc, pbkdf2, 600000 iterations
Keystore alias $ALIAS
Key            RSA 4096
SHA-256 of the keystore file:
  $(shasum -a 256 "$KEYSTORE" | awk '{print $1}')

Restore:
  openssl enc -d -aes-256-cbc -pbkdf2 -iter 600000 \\
    -in warforge-keystore-$STAMP.enc -out warforge.jks

Then put warforge.jks back at ~/keys/ and set the four warforge* properties in
~/.gradle/gradle.properties.

WITHOUT THE KEYSTORE PASSWORD THIS FILE IS USELESS. Keep that password in a
password manager, not next to this file.
TXT

echo
echo "Written to your Desktop:"
echo "  warforge-keystore-$STAMP.enc   (the key, encrypted)"
echo "  warforge-keystore-$STAMP.txt   (how to restore it)"
echo
echo "Now, in order:"
echo "  1. Verify it:  scripts/backup-keystore.sh --verify $OUT"
echo "  2. Copy BOTH files to at least two places that are not this laptop."
echo "  3. Put the keystore password in your password manager."
echo "  4. Delete them from the Desktop once they are safely elsewhere."
