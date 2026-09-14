# Publishing Warforge

Everything the Play Console asks for, and the order to do it in.

---

## 1. One-time setup

### 1.1 Create the signing key — DONE

Google identifies an app by the key it was first signed with, **for as long as the app
exists**. Lose this file and you can never update the app again — you would have to
publish a new listing under a new package name and leave every existing player behind.
Back it up somewhere you would back up a password.

Done with `scripts/create-keystore.sh`, which prompts for the password rather than taking
it as an argument, so it never reaches the shell history or the process list.

    keystore   ~/keys/warforge.jks          RSA 4096, valid to 2054-01-29  (chmod 600)
    signing    ~/.gradle/gradle.properties  four warforge* properties      (chmod 600)

Two fingerprints, and they are not interchangeable:

| | |
|---|---|
| **Keystore file** `10ac7da2812a3ceb5e555d101b574e2015d69e20e44e239f9c7939ba637f0522` | SHA-256 of the `.jks` itself. Use it to check a backup copy is byte-identical. `shasum -a 256 ~/keys/warforge.jks` |
| **Certificate** `07:70:DB:C1:E5:63:B4:A5:87:27:66:44:04:3B:BB:09:BA:D8:A4:4B:6B:C9:A7:01:F7:5D:4A:85:23:0D:D7:4A` | SHA-256 of the signing certificate. This is what Play Console shows under *App integrity*, and what to compare when checking you are signing with the right key. |

Verify it at any time:

```bash
keytool -list -v -keystore ~/keys/warforge.jks -alias warforge
```

**Back it up.** `scripts/backup-keystore.sh` writes an AES-256 encrypted copy plus a
plain-text note saying how to restore it, and `--verify` proves a given archive really
does restore — an unverified backup is not a backup:

```bash
scripts/backup-keystore.sh
scripts/backup-keystore.sh --verify ~/Desktop/warforge-keystore-YYYYMMDD.enc
```

Keep the archive in at least two places that are not this laptop, and the keystore
password in a password manager — not beside the archive.

> Turning on **Play App Signing** in the Console (recommended, and the default for new
> apps) means Google holds the final signing key and this one becomes your *upload* key,
> which can be reset if it is lost. Do that and the paragraph above stops being frightening.

### 1.2 Real AdMob ids — DONE

Set in `~/.gradle/gradle.properties`, publisher `ca-app-pub-7351566691124059`. Only the
three formats the app actually implements are configured — banner, interstitial and
rewarded. There is no native placement and no app-open placement, so those ids are
deliberately not set.

Debug builds use Google's test ids for **both** the app id and the unit ids; clicking
your own live ads is invalid traffic and gets the AdMob account closed. `AdConfigTest`
fails the build if a debug variant ever names a live unit.

### 1.3 Host the privacy policy — DONE

Play will not accept an app that shows ads without one. The policy is published from
`docs/` on this repository, so updating it is a commit like anything else:

**https://aungkaungmyatpaing.github.io/warforge/**

`docs/index.html` is the published copy and `playstore/privacy-policy.html` is the source
it was taken from; change the source, copy it across, and push.

Published straight from the branch — set once, in the GitHub web UI:

*Settings → Pages → Source: **Deploy from a branch** → **main** / **/docs** → Save*

Nothing else is needed: a push that changes `docs/` is republished within a minute or so.

> If it ever serves 404 on every path at once, the cause is almost always that Pages is
> disabled rather than that the folder is wrong — the settings page says so in as many
> words, and the branch dropdown will be sitting on **None**.

### 1.4 Turn on the update check

Once the repository is on GitHub, point the app at the manifest — again in
`~/.gradle/gradle.properties`:

```properties
updateManifestUrl=https://raw.githubusercontent.com/<user>/<repo>/main/version.json
```

Left unset, the app simply never checks and never touches the network for it.

---

## 2. Every release

```bash
scripts/bump-version.sh 1.1     # versionCode +1, versionName 1.1
./gradlew testDebugUnitTest     # 68 tests
./gradlew bundleRelease         # -> app/build/outputs/bundle/release/app-release.aab
```

Upload the `.aab` in *Play Console → Production → Create new release*, roll it out, and
**once it is actually live**:

```bash
git tag v1.1 && git push origin v1.1
```

That runs the `publish-version` workflow, which rewrites `version.json` from the version
in `app/build.gradle.kts` and commits it. Installed copies pick it up within six hours and
offer the update.

> Do it in that order. Publishing the manifest first would send players to a store listing
> that has not changed yet.

### Forcing an update

Only when an old build is genuinely broken. Run the workflow by hand from the Actions tab
and set **minSupportedVersionCode** to the oldest build you want to keep working. Anything
older gets a dialog it cannot dismiss.

---

## 3. Store listing — what to paste where

| Console field | File |
|---|---|
| App name, short and full description | `playstore/listing-en.md` |
| App icon (512×512) | `playstore/icon-512.png` |
| Feature graphic (1024×500) | `playstore/feature-graphic-1024x500.png` |
| Phone screenshots (2–8) | `playstore/screenshots/*.png` |
| Privacy policy URL | the GitHub Pages address from 1.3 |

---

## 4. Data safety form

Answer it like this. It describes **AdMob**, because the app itself collects nothing.

- **Does your app collect or share any of the required user data types?** → **Yes**
- Data types collected:
  - *Device or other IDs* → **Collected** and **Shared**
    - Purpose: **Advertising or marketing**
    - Collected by a **third party** (Google AdMob)
    - **Not** processed ephemerally · **Not** required (the app works offline)
  - *Location → Approximate location* → **Collected** and **Shared**
    - Purpose: **Advertising or marketing** — derived from IP address by AdMob
  - *App activity → App interactions* → **Collected** and **Shared**
    - Purpose: **Advertising or marketing** — ad impressions and taps
- **Is all data encrypted in transit?** → **Yes**
- **Do you provide a way to request data deletion?** → **Yes** — uninstalling removes
  everything stored on the device; the advertising ID is reset from Android settings.

Nothing else is collected. There is no account, no analytics and no crash reporting.

## 5. Content rating questionnaire

Category: **Game → Educational / Simulation**

- Violence: **No** — the vehicles are assembled and examined; nothing is fired at
  anything and nothing is destroyed.
- Sexuality, language, controlled substances, gambling: **No**
- User-generated content, sharing, chat: **No**
- Does the app share the user's location? **No** (AdMob's coarse IP-based location is
  declared in Data safety, not here)
- Ads: **Yes, the app contains ads** — this also sets the "Contains ads" badge

Expected outcome: PEGI 3 / ESRB Everyone, or PEGI 7 at most.

## 6. Other Console sections

- **Ads** → *Yes, my app contains ads*
- **Target audience** → 13+ (do **not** opt into Designed for Families; an app with
  AdMob and a 13+ audience is the simpler path)
- **Government app** → No
- **Financial features** → None
- **App access** → All functionality is available without restrictions (no login)

---

## 7. Pre-launch checklist

- [x] `./gradlew testDebugUnitTest` — 68 green
- [x] `./gradlew bundleRelease` produces a signed bundle — `jarsigner -verify` says
      *jar verified*, and the APK verifies under APK Signature Scheme v2
- [x] Release build installs and runs: models load under R8, no crash
- [x] **Real AdMob ids** — release carries the live publisher, debug carries test ids
- [x] Privacy policy URL loads — https://aungkaungmyatpaing.github.io/warforge/
- [ ] Settings sheet shows "Privacy options" on an EEA/UK device (or with UMP debug
      geography forced). Google requires the consent form to stay reachable for as long
      as the app is installed, wherever one was shown in the first place.
- [x] `version.json` matches the version being published (1.0 / code 1)
- [x] Screenshots contain no ads and no debug overlays
- [ ] Play App Signing enabled in the Console
- [ ] `~/keys/warforge.jks` backed up somewhere that is not this laptop
