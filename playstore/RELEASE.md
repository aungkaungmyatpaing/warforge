# Publishing Warforge

Everything the Play Console asks for, and the order to do it in.

---

## 1. One-time setup

### 1.1 Create the signing key

Google identifies an app by the key it was first signed with, **for as long as the app
exists**. Lose this file and you can never update the app again — you would have to
publish a new listing under a new package name and leave every existing player behind.
Back it up somewhere you would back up a password.

```bash
mkdir -p ~/keys
keytool -genkeypair -v -keystore ~/keys/warforge.jks -alias warforge \
  -keyalg RSA -keysize 4096 -validity 10000
```

Then put the details in `~/.gradle/gradle.properties` — **not** in the project, which is
in git:

```properties
warforgeStoreFile=/Users/<you>/keys/warforge.jks
warforgeStorePassword=<the password you chose>
warforgeKeyAlias=warforge
warforgeKeyPassword=<the key password you chose>
```

> Turning on **Play App Signing** in the Console (recommended, and the default for new
> apps) means Google holds the final signing key and this one becomes your *upload* key,
> which can be reset if it is lost. Do that and the paragraph above stops being frightening.

### 1.2 Real AdMob ids

Debug builds always use Google's test ids — clicking your own live ads gets the account
banned. Release builds read the real ones from the same file:

```properties
admobAppId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
admobBanner=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admobInterstitial=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admobRewarded=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
```

### 1.3 Host the privacy policy — DONE

Play will not accept an app that shows ads without one. The policy is published from
`docs/` on this repository, so updating it is a commit like anything else:

**https://aungkaungmyatpaing.github.io/warforge/**

`docs/index.html` is the published copy and `playstore/privacy-policy.html` is the source
it was taken from; change the source, copy it across, and push.

One switch has to be thrown by hand the first time, in the GitHub web UI:

*Settings → Pages → Source: **Deploy from a branch** → Branch: **main** / **/docs** → Save*

Pages takes a minute or two to build the first time.

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
./gradlew testDebugUnitTest     # 65 tests
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
| App name, short and full description (en-US) | `playstore/listing-en.md` |
| Same, Burmese (my-MM) | `playstore/listing-my.md` |
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

- [ ] `./gradlew testDebugUnitTest` — all green
- [ ] `./gradlew bundleRelease` produces a **signed** bundle (not `-unsigned`)
- [ ] Install the release build on a real device and play one vehicle end to end
- [ ] Real AdMob ids in the release build, test ids in debug
- [ ] Privacy policy URL loads
- [ ] `version.json` matches the version you are about to publish
- [ ] Screenshots contain no test ads and no debug overlays
