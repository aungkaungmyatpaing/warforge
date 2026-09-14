# Warforge

An Android game about building military vehicles from the inside out, from WWI to the
present day — and then opening them up to see what you built.

* **21 vehicles** across five eras and three branches — ground, air, naval.
* **Build in 3D, from the engine outward.** Powerpack, then crew and ammunition, then
  armour, then running gear, then turret. The board is see-through while the guts go in.
* **X-ray and cutaway** museum view: orbit the finished vehicle, fade the shell, slice it
  down the centreline, tap any component to find out what it is and why it sits there.
* **A reference as much as a game.** All 21 vehicles carry a full cutaway, a history
  article, a specification table, per-component notes and a quiz.
* **Models authored in Blender** with real bevels, booleans and track arrays, exported
  to glTF — falling back to code-generated geometry for vehicles not yet migrated.
* **No art or audio files to license.** The fallback geometry is generated from each
  vehicle's own blueprint, and the score is synthesised at runtime.

```bash
./gradlew assembleDebug          # APK -> app/build/outputs/apk/debug/
./gradlew test                   # rules, catalogue, geometry, model and audio tests
blender/build.sh                 # re-export the Blender models into assets/
```

---

## The game

Each build is one vehicle, assembled in stages from the inside out:

| Stage | |
|---|---|
| **Powerpack** | engine, gearbox, driveshaft, final drives, cooling |
| **Crew and stowage** | crew stations, ammunition, fuel, radio, gun breech |
| **Armour** | the plate that goes over everything just fitted |
| **Hull and structure** | superstructure, decks, hull plating |
| **Running gear** | tracks, wheels, suspension, undercarriage |
| **Armament and fittings** | turret, guns, optics, everything bolted on last |

The foundation part starts on the board; everything else sits shuffled in the tray. Empty
slots show as translucent blue shapes *inside* the vehicle, and the view is automatically
see-through while the guts are going in — you have to look inside to see where the engine
goes. Drag on the model to turn it.

Drag a piece **up** out of the tray onto its slot, or **tap** it and then tap where it
goes; drag **sideways** to scroll the tray. Vehicles without internals start at the hull
stage and play like a straightforward assembly puzzle.

Later vehicles mix **decoy parts** borrowed from other vehicles into the tray. They fit
nowhere, and trying to place one costs you.

### The museum

Every vehicle — locked or not — opens in the museum from its hangar card. Orbit it, pinch
to zoom, and switch between **Solid**, **X-ray** and **Cutaway**. Tap any part or module
to find out what it is. Underneath are four tabs: the internals legend, the history
article, the specification table, and a quiz drawn from the vehicle's own facts.

The museum is deliberately not a reward. You should be able to study a machine before you
have earned the right to build it.

| | |
|---|---|
| **Stars** | 3 for a near-clean build; misplaced parts and hints eat into the allowance, which scales with the part count |
| **Coins** | paid out per build, spent on hints (25 each) or earned back from a rewarded ad |
| **Assist mode** | on by default — outlines every empty slot. Turn it off in settings for a real challenge |
| **Unlocking** | finishing a vehicle unlocks the next one in campaign order |

Campaign order runs era by era and rotates ground → air → naval inside each era, so you
never build three tanks in a row.

---

## Build configuration

Anything account-specific — AdMob ids, the signing key, the update manifest URL — lives in
`~/.gradle/gradle.properties`, never in the repository. `gradle.properties.template` lists
every key, and `./gradlew adConfig` prints what a build actually resolved.

Debug and release are configured separately on purpose:

| | Debug | Release |
|---|---|---|
| AdMob app id and units | Google's test ids, hard-coded and not overridable | from `gradle.properties` |
| Application id | `com.maddog.warforge.debug` | `com.maddog.warforge` |
| Missing ids | irrelevant — the test ids are the point | **the build fails** |

The failure matters more than the convenience. A release that falls back to test ids shows
every player a banner reading "Test Ad" and earns nothing, and there is nothing else in the
pipeline that would notice. `AdConfigTest` covers the other direction: a debug build must
never name a live ad unit, because the person clicking around in it is you, and clicking
your own ads closes the AdMob account.

## Models: Blender

Vehicles are modelled in `blender/`, as Python that drives Blender rather than as .blend
files. Scripts diff, review and re-run; a binary scene does none of those.

All twenty-one are modelled twice: `<vehicle>.py` builds the outside and
`<vehicle>_int.py` builds the X-ray layer, with `wf.py` as the shared toolkit and
`intkit.py` as a thinner one on top of it for internals. `blender/build.sh` re-exports the
lot. Together they come to about 1.4 million triangles and 34 MB of glTF, which packs to
a 16 MB release APK.

The two halves are separate files because they are **occluded separately**. An engine
baked against the hull that encloses it comes out uniformly black, and the X-ray view
ghosts that hull away - so `wf.occlude(..., isolate=True)` hides everything else while it
bakes the internals.

The naming contract is the same idea one level down: an object in `<vehicle>.py` is named
for a **part** id, and an object in `<vehicle>_int.py` for a **module** id. A typo either
way is silent - the app just falls back to the procedural version - so `GlbLoaderTest`
checks both directions, that nothing in a file is unknown to the catalogue and that
nothing in the catalogue has been left behind.

```bash
/Applications/Blender.app/Contents/MacOS/Blender --background \
  --python blender/panzer_iv.py -- /tmp/preview.png app/src/main/assets/models/panzer_iv.glb
```

Three modifiers are the whole reason for authoring here rather than in code:

| | |
|---|---|
| **Bevel** | a real chamfer on every hard edge. A bevelled edge catches a highlight; a perfectly sharp one cannot, and that single difference is most of what separated the old models from these |
| **Boolean** | a vision port is *cut into* the plate, not a dark box parked on top of it |
| **Array along a curve** | track links laid round their own run, bending correctly over the idler |

On top of those, the toolkit grew a primitive per problem as each kind of vehicle
arrived:

| | |
|---|---|
| `frustum` | a box tapering between two rectangles - a cast turret, a hull whose sides slope in as they rise |
| `slice_xz` / `slice_yz` / `slice_xy` | cut along a line given as two points in the side, head-on or plan view, plus a third point naming the side to throw away |
| `loft` | a solid skinned through cross-sections: fuselages, nacelles, funnels, ship hulls |
| `airfoil` / `wing` / `fin` | a real NACA section swept through stations, so a wing catches light along its span instead of reading as a plank |
| `ellipse_stations` | the Spitfire's elliptical planform, as chord following an ellipse about a straight quarter-chord line |
| `hull_section` | one station of a ship, keel to deck edge, with flare and fullness |
| `decal` | a marking cut from the skin it sits on, so a roundel curves round a fuselage |
| `numerals` / `star` / `cross` | stencilled numbers and national markings |

The slice helpers take a *point* rather than a sign because placing a rotated cutter by
eye is how the first pass at both tanks got its sloped plates wrong - the render looked
plausible and the glacis was 40 cm from where it was meant to be - and naming the
discarded side by a sign deleted most of the tank three times out of four.

Three more mistakes are worth knowing before building the next one. A wing's chord
*subtracts* from the leading edge, because +x is toward the nose; adding it builds the
wing pointing forward past the propeller. Swapping two axes reverses handedness and the
winding has to be rebuilt, but rotating three axes does not - flipping normals there
turns a fin into a black sheet. And a paint colour worked out from the wrong palette
base comes out uniformly wrong in a way that looks like a lighting bug.

### Conventions that the pipeline depends on

* **Full size, in metres.** Proportions come out right without fiddling, and the crew
  figures and ammunition can be the size they actually were.
* **+X forward, +Y to port, +Z up** — Blender's own axes, which survive the glTF export.
* **One object per game part, named for its part id.** The names travel through the glTF
  as node names and are what the assembly board drags around. `GlbLoaderTest` fails if a
  model names a part the catalogue does not know.
* **Materials are named for roles** — `BODY`, `DARK`, `METAL`, `GLASS`, `ACCENT`, `TRIM` —
  not for colours. The loader maps the name back to a role and the app paints from the
  vehicle's own palette, so one model can wear any livery and the X-ray colour coding
  stays consistent.
* **Scale and origin are solved, not guessed.** `BlueprintDump` prints each vehicle's
  part ids and where its pre-placed body sits on the blueprint; the model's `scale` is
  its real length over that span, and `originX`/`originY` are set so the *same part*
  lands in the same place. `GlbLoaderTest` fails if it does not, which is how three
  wrong origins were caught rather than noticed later in the museum.

### Surface: camouflage, markings, wear

Geometry alone still reads as a toy, because real armour is never one flat colour. Three
passes run after the shapes are finished, all of them writing into a single vertex colour
attribute that the game multiplies over its own palette:

| | |
|---|---|
| `wf.weather` | camouflage patches, mud thrown up the lower hull, paint rubbed off convex edges. Thresholds come from the *distribution* of the noise, not from fixed numbers, so asking for "22% green" gives 22% green on every part regardless of its size |
| `wf.cross` + `wf.flat_tint` | markings. A Balkenkreuz is geometry a few millimetres proud of the plate, tinted white and black |
| `wf.occlude` | ambient occlusion, ray-traced by Cycles and baked to the same attribute |

Two things make this work rather than just look clever:

* **It multiplies, it does not replace.** The model says how the surface *varies*; the
  palette still says what colour the vehicle is. A camouflaged Panzer IV and a desert one
  are the same mesh.
* **The multiplier can exceed 1.** White paint over dunkelgelb needs about 2.3, and glTF
  normalises integer colour channels to 0..1, so every tint is divided by `TINT_RANGE`
  (2.5) on export and multiplied back in `GlbLoader`. The two constants must match.

Because the occlusion is baked here, the app skips its own bake for these parts -
`AmbientOcclusion` still puts them in the occupancy grid, so they darken the parts around
them, but does not sample them. That is most of the time it takes to open a vehicle.
`ScenePerformanceTest` fails if they ever get sampled again.

Vertex colours need vertices, so `wf.densify` subdivides the painted panels first. It is
also the main thing that decides how big a model ends up: the Panzer IV is ~58k triangles
and 2 MB of glTF, and `GlbLoaderTest` fails above 80k.

### Two traps, both of which caught us

`wf.box` builds its geometry around the origin and puts the position in the object's
transform. Baking the position into the mesh instead makes every later `rotate` a
rotation about the *world* origin, which flings the part across the scene - that is how
the sprocket teeth first ended up scattered in mid-air.

And `bpy.ops.object.modifier_apply` needs a context this kind of script does not have, and
fails **silently** without it. `wf.apply_modifiers` goes through the dependency graph
instead. A Mirror that had never run went unnoticed until half a tank was missing.

## How the fallback art works

There are no images anywhere in this project. A vehicle is authored **once**, as a flat
side profile, and that profile serves twice: drawn directly for hangar cards and tray
thumbnails, and extruded into a solid for the 3D board and the museum.

```
poly/Poly.kt          Poly, Palette, shade(), and the 2D ShapeBuilder DSL
solid/Mesh.kt         triangles with per-vertex normals and materials
solid/Solids.kt       extrude, revolve, cylinders, spheres
solid/AmbientOcclusion.kt   baked per-vertex occlusion over a whole vehicle
data/parts/Dsl.kt     SolidBuilder: one call builds both the profile and the solid
data/parts/*Kit.kt    reusable builders: track belts, airfoils, funnels, turrets
data/parts/Detail.kt  rivets, welds, hatches, vision ports, track links
data/parts/*.kt       the vehicles themselves, as data
gl/                   the renderer: shaders, camera, scene, backdrop
```

### From profile to solid

`SolidBuilder` extends the 2D `ShapeBuilder`, so every existing drawing call still just
draws a profile — and whatever is left unclaimed at the end of a part is extruded between
that part's depths. Only the shapes that genuinely are not slabs have to say so:

| Builder | Gives |
|---|---|
| *(anything 2D)* | extruded between the part's `depth`, with bevelled edges |
| `bar(...)` | a solid of revolution — gun tubes, fuselages, funnels, masts |
| `disc(...)` | a cylinder across the vehicle — road wheels, tyres, prop bosses |
| `block(...)` | a box with its own depth |
| `solid { }` | geometry with no flat counterpart |
| `flat { }` | decoration for the flat view only |

### Loading a model

`GlbLoader` reads a deliberate subset of glTF: positions, normals, indices and a material
name. `ModelSource` carries the three numbers that put a model authored in metres onto a
blueprint measured in its own units, because the internal modules are still placed in
blueprint coordinates and have to end up inside the hull. `GlbLoaderTest` checks that they
do.

### Building a vehicle is expensive - do it off the main thread

Opening a vehicle tessellates every part and module and bakes ambient occlusion over the
lot. On a mid-range phone that is a few hundred milliseconds; on a slow one it was enough
to trip the ANR watchdog and kill the app. Both `InspectSurfaceView.show` and
`BuildActivity.startLevel` therefore build on a worker thread and hand the result back to
the main thread when it is ready, and `Scene.of` keeps the last four vehicles cached.

**Never call `Scene.of` or construct a `LevelState` on the main thread.**

### What stops it looking like flat-shaded low poly

Six things, in rough order of how much each one buys:

1. **Baked ambient occlusion** over the whole vehicle at once, so a turret darkens the
   deck it sits on and a wheel darkens inside its own track.
2. **Smooth normals** on every curved primitive — a barrel shades as a cylinder, not as
   a ring of plates.
3. **Bevelled edges**, done with normals rather than geometry: the outer bands of every
   extruded wall lean their normals toward the face they meet, so corners catch light.
4. **Per-material response** — bare metal and glass take a tight specular highlight,
   paint a soft one, rubber and track steel almost none.
5. **MSAA**, requested at 4x and degraded gracefully. Everything here is straight edges
   meeting at angles; without it they crawl.
6. **Surface detail** from `Detail.kt` — welds, rivets, hatches, vision ports, grab
   handles, tow hooks, track links. A plate is a polygon until it has a weld down it.

Historically, *how* a vehicle was joined is part of what it was, so the detail follows
the record: the Mark IV and the Vickers are riveted, the T-34 welded and cast, the Abrams
bolted composite.

### Blueprint space

Every vehicle is authored in a fixed **1000 × 620** rect (`BP_W` / `BP_H`), nose or bow
pointing **right**. Ground vehicles rest on `GROUND_Y = 512`; ships float on
`WATERLINE = 432`.

Crucially, **a part's polygons are stored in final blueprint coordinates** — the
coordinates it will occupy on the finished vehicle. So "assembling" a vehicle is just
deciding which parts to draw, and testing a drop is a distance check against the part's
own bounding-box centre. No per-part transforms, no slot table to keep in sync.

### Colour

A facet names a **role** (`BODY`, `DARK`, `METAL`, `GLASS`, `ACCENT`, `TRIM`) and a
**tone** in roughly −4…+4. The vehicle's `Palette` resolves the role to a colour and
`shade()` steps it toward white or black. Swapping a palette re-liveries a whole vehicle,
which is also how decoy parts borrowed from other vehicles arrive in the right colours.

### Shape builders

| Builder | Use |
|---|---|
| `slab(role, tone, x0,y0, …)` | fan-triangulates a convex outline, shading facets by height. The workhorse: hulls, turrets, fins |
| `ngon` / `ring` | faceted circles and arcs — road wheels, radar domes, track runs. `ring` takes a `sweep` for partial arcs |
| `tube(role, tone, x,y,r, …)` | tapered spine — fuselages, gun barrels, funnels, torpedoes |
| `quad` / `box` / `face` | explicit facets |
| `beltLoop` | a track belt following an arbitrary outline, for the WWI rhomboids |
| `airfoil` | the edge-on wing section every wing, tailplane and fin is built from |

---

## Adding a vehicle

One function in `data/parts/Ground.kt`, `Air.kt` or `Naval.kt`:

```kotlin
fun myTank() = VehicleDef(
    id = "my_tank", name = "My Tank",
    era = Era.WWII, branch = Branch.GROUND,
    country = "…", year = 1943,
    fact = "One line shown on the victory card.",
    palette = DUNKELGELB,
    parts = listOf(
        part("hull", "Lower hull", z = 20, pre = true) { slab(Role.BODY, 0, …) },
        part("track", "Track belt", z = 30) { trackBelt(166f, 862f, 396f, 500f) },
        part("wheels", "Road wheels", z = 25) { roadWheels(166f, 862f, 448f, 30f, 8) },
        part("turret", "Turret", z = 40) { slab(Role.BODY, 1, …) },
        part("gun", "7.5 cm gun", z = 50) { mainGun(682f, 300f, 236f, 10f, brake = true) },
    ),
    decoys = 2,
)
```

Then add it to that file's `all()`. `Catalog` picks up campaign order, unlocks and the
hangar listing automatically.

**Draw order** is the `z` argument, back to front. The convention used throughout:

| z | |
|---|---|
| 10 | under-hull, stores, gear |
| 20 | hull / fuselage (the `pre = true` foundation) |
| 25 | road wheels — above the hull, below the track |
| 30 | track belt, tail surfaces |
| 40 | turret, superstructure |
| 50 | barrels, masts, engines |
| 60 | canopies, radar, markings |

### Looking at your work

Two developer tools live in the test source set. Neither asserts anything; both exist
because an emulator is a slow way to look at a model.

```bash
./gradlew test --tests '*PreviewGenerator*'   # flat art  -> app/build/preview/*.svg
./gradlew test --tests '*Render3DPreview*'    # solids    -> app/build/render3d/*.png
```

`Render3DPreview` is a small software rasteriser that re-implements `gl/Shaders.kt` and
`gl/OrbitCamera.kt` on the JVM — same camera fit, same light rig, same Blinn-Phong and
rim terms, same filmic shoulder. Keeping a second copy of the shading maths is a real
cost, and it buys a look at every vehicle in about four seconds. **When the shader
changes, that file has to change with it.**

### Checking your work

`./gradlew test` enforces the invariants that hand-authored geometry gets wrong:

* exactly one `pre = true` part, at least five loose ones
* nothing outside the blueprint (a barrel poking past the edge gets clipped on screen)
* every home position reachable on the board
* no degenerate facets
* every vehicle completable by dropping each part on its home

`PreviewGenerator` is a developer tool, not a test — it writes every vehicle to
`app/build/preview/*.svg` plus a `contact-sheet.svg` so you can eyeball the whole
catalogue without an emulator:

```bash
./gradlew test --tests '*PreviewGenerator*' && open app/build/preview/contact-sheet.svg
```

---

## Code map

```
MainActivity            hangar: era chips, vehicle cards, continue
ui/BuildActivity        one build: board, hint, victory card
ui/MuseumActivity       3D exhibit: modes, legend, history, specs, quiz
ui/InspectSurfaceView   the GL viewport; orbit, pinch, tap-to-identify
ui/AdHostActivity       consent -> SDK init -> banner, shared by every screen
ui/VehicleThumbView     finished-vehicle art for cards (silhouette when locked)
ui/QuizView             multiple choice over one vehicle's facts
ui/Sfx                  system tones + haptics on a private thread, no audio assets
ui/Music                the score, synthesised at runtime and looped
ui/MaxHeightScrollView  grows to fit a component note, then stops

game/BuildPlan          the stages, and what belongs in each
game/LevelState         placement rules, mistakes, stars, rewards. Pure JVM, unit-tested
game/Assembly3DView     the board: GL viewport with a tray drawn over it

gl/VehicleRenderer      three draw passes: internals, shell, ghost slots
gl/Scene                nodes, layers, per-node state; bakes occlusion
gl/OrbitCamera          turntable camera that frames any vehicle on any screen
gl/Backdrop             studio gradient and contact shadow
gl/Shaders              the surface and flat programs

solid/GlbLoader         reads the Blender exports
data/ModelSource        where a model sits on the blueprint; ModelStore caches them
data/Model              PartDef, VehicleDef, Lore, Era, Branch, blueprint constants
data/Internals          ModuleKind, ModuleDef, crew figures, shell racks
data/lore/*             history, specs, quizzes and internals per vehicle
data/Catalog            all vehicles, campaign order, decoy selection
data/Progress           SharedPreferences: stars, coins, unlocks, settings
```

### The drag gesture

`Assembly3DView` puts a plain `View` over the GL surface and gives it every touch. A
press in the tray is `PENDING`; past the touch slop it becomes a `DRAG` if the finger
moved mostly vertically, or a tray `SCROLL` if mostly horizontally. A press anywhere else
turns the camera.

A drop lands if it is within about 66dp of the **dragged piece's own slot**, projected to
the screen — deliberately not "whichever slot is nearest". Internals sit on top of one
another (a radiator alongside the engine it cools, a fuel cell under the ammunition) and
project to nearly the same point, so nearest-wins would turn fitting an engine into a
pixel contest with its own cooling system. What the game asks is whether the player knows
*where* the engine goes.

---

## Ads

Wired the same way as `beer-sort-puzzle`, and Google's **test IDs are used by default** —
the project builds and runs safely out of the box.

Banner on both screens, interstitial between builds, rewarded video offered when the
player wants a hint but has no coins. Consent (UMP/GDPR) runs before the SDK initialises,
and nothing about it blocks play: refuse consent and the ad slots just stay empty.

Put real IDs in `~/.gradle/gradle.properties` (not in the repo) before a release build:

```properties
admobAppId=ca-app-pub-XXXXXXXXXXXXXXXX~YYYYYYYYYY
admobBanner=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admobInterstitial=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
admobRewarded=ca-app-pub-XXXXXXXXXXXXXXXX/YYYYYYYYYY
```

Debug builds always use the test IDs regardless — clicking your own live ads gets the
account banned.

---

## Roster

All 21 have a full cutaway, history, specs and quiz.

| Era | Ground | Air | Naval |
|---|---|---|---|
| **WWI** 1914-18 | Mark IV, Renault FT | Sopwith Camel, Fokker Dr.I | HMS Dreadnought, SM U-9 |
| **Interwar** 1919-38 | Vickers Medium Mk II | Junkers Ju 52 | Admiral Graf Spee |
| **WWII** 1939-45 | Panzer IV Ausf. H, T-34-85 | Spitfire Mk V, B-17G | Fletcher-class, Yamato |
| **Cold War** 1946-91 | Centurion Mk 5 | MiG-15 | Los Angeles-class SSN |
| **Modern** 1992- | M1A2 Abrams | F-16C | Nimitz-class carrier |

## Writing the lore

Each vehicle's `data/lore/*.kt` holds four things, and the tests enforce all of them:

* **Modules** — at least five, each with a `detail` line (the number the reader wants)
  and an `info` paragraph (why it is there and what it means for the vehicle).
* **History** — an article in `##`-headed sections. Source is hard-wrapped for
  readability; the museum re-flows each paragraph.
* **Specs** — at least eight rows.
* **Quiz** — at least three questions, each with an explanation that teaches whether the
  reader got it right or wrong.

Two things are worth doing deliberately. Contrast pairs teach more than either vehicle
alone: the Panzer IV and the T-34-85 are the same year and the same job with opposite
answers, and fitting one after the other is the clearest lesson in the game. And the
surface detail follows the record - the Mark IV and Vickers are riveted, the T-34 welded
and cast, the Abrams bolted composite - because how a thing was joined is part of what it
was.

## The score

`ui/Music` synthesises about forty seconds of audio at startup and loops it: a low drone,
a chord turning over every two bars, a slow pulse and the occasional tap of metal. Written
rather than shipped for the same reason as the fallback geometry - nothing to license,
nothing to download, and it retunes by changing a number.

The loop is an exact number of bars, every sustained voice is snapped to a whole number of
cycles across it, and decaying events wrap their tails back to the start. Miss that last
one and the track clicks once per pass, which is exactly what `MusicTest` caught.

## Ideas not yet built

* Timed "scramble" mode and a daily vehicle
* Mirror mode — the blueprint flipped, so muscle memory stops helping
* Exploded-view intro animation before each build
* Armour diagrams: thickness and slope, with a shot line the reader can move
* More of each era: Tiger I, Sherman, P-51, Zero, Type VII, Arleigh Burke, Leopard 2
* **Interiors that can be taken apart.** The X-ray layer shows where everything is; it
  does not yet let the player pull the engine out and look at it
