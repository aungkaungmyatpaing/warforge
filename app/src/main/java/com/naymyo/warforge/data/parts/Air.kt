package com.naymyo.warforge.data.parts

import com.naymyo.warforge.data.Branch
import com.naymyo.warforge.data.Era
import com.naymyo.warforge.data.ModelSource
import com.naymyo.warforge.data.VehicleDef
import com.naymyo.warforge.data.lore.B17Lore
import com.naymyo.warforge.data.lore.F16Lore
import com.naymyo.warforge.data.lore.FokkerDr1Lore
import com.naymyo.warforge.data.lore.Ju52Lore
import com.naymyo.warforge.data.lore.Mig15Lore
import com.naymyo.warforge.data.lore.SopwithCamelLore
import com.naymyo.warforge.data.lore.SpitfireLore
import com.naymyo.warforge.poly.Palette
import com.naymyo.warforge.poly.Role

/**
 * Aircraft, WWI to the present. Side view, nose to the right, centreline near y = 300.
 *
 * Draw order: 10 lower wing / gear, 20 fuselage, 30 tail, 40 upper wing and struts,
 * 50 engine, 60 canopy and markings.
 */
object Air {

    private val RFC_KHAKI = Palette.of(0x8A7B4E, 0x2B261C, 0x8F8B7C, 0x46555C, 0xB2372B, 0x6E6138)
    private val FOKKER_RED = Palette.of(0x9B3A2C, 0x271B16, 0x8C887A, 0x45535A, 0xE8DCC0, 0x7A2C21)
    private val LUFTHANSA = Palette.of(0x9AA0A4, 0x25282A, 0xB6B9BA, 0x44545C, 0x1F2A33, 0x7C8286)
    private val RAF_GREEN = Palette.of(0x5C6B4C, 0x22261D, 0x9A9A92, 0x4A5A62, 0x2E5C9A, 0x6E6244)
    private val USAAF_BARE = Palette.of(0xA9AEB2, 0x26292C, 0xC6C9CA, 0x4B5B64, 0x2B4C8A, 0x8B9095)
    private val MIG_SILVER = Palette.of(0x9DA4A8, 0x222528, 0xC0C4C6, 0x455660, 0xAE2F26, 0x82898D)
    private val USAF_GREY = Palette.of(0x7D868C, 0x202427, 0xA8AEB2, 0x3F5560, 0x1E3E6E, 0x646D74)

    // -----------------------------------------------------------------------
    // WWI
    // -----------------------------------------------------------------------

    fun sopwithCamel() = VehicleDef(
        id = "sopwith_camel",
        name = "Sopwith Camel",
        era = Era.WWI,
        branch = Branch.AIR,
        country = "United Kingdom",
        year = 1917,
        fact = "Credited with 1,294 kills, more than any other Allied aircraft of the war - "
            + "and vicious enough that it killed plenty of its own pilots in training.",
        palette = RFC_KHAKI,
        // 5.72 m long, drawn 664 units; thrust line at y 300.
        model = ModelSource("models/sopwith_camel.glb", scale = 116.1f, originX = 540f,
                            originY = -292f,
                            internals = "models/sopwith_camel_int.glb"),
        parts = listOf(
            part("fuselage", "Fuselage", z = 20, pre = true) {
                fuselage(196f, 286f, 22f, 320f, 296f, 40f, 560f, 300f, 46f, 720f, 300f, 44f)
                slab(Role.BODY, 1, 196f, 264f, 300f, 258f, 300f, 292f, 196f, 286f)
                // Formers showing through fabric - the giveaway of a 1917 airframe.
                fuselageRings(250f, 560f, 291f, 30f, 300f, 46f, count = 7)
                aerial(232f, 270f, 470f, 258f)
            },
            part("upper_wing", "Upper wing", z = 44) {
                airfoil(642f, 512f, 188f, 20f, span = 392f)
                box(Role.BODY, 2, 512f, 178f, 130f, 6f)
                fabricRibs(642f, 512f, 188f, 40f, 388f, 9)
                navLight(576f, 186f, 386f)
            },
            part("lower_wing", "Lower wing", z = 12) {
                airfoil(632f, 504f, 344f, 18f, span = 380f)
                fabricRibs(632f, 504f, 344f, 40f, 376f, 9)
            },
            part("struts", "Wing struts", z = 40) {
                strut(528f, 196f, 524f, 338f, side = 180f)
                strut(624f, 194f, 620f, 336f, side = 180f)
                strut(560f, 198f, 600f, 300f, w = 4f, side = 330f)
            },
            part("engine", "Clerget rotary", z = 50) {
                radialEngine(744f, 300f, 54f)
            },
            part("prop", "Propeller", z = 58) {
                propeller(782f, 300f, 128f, blades = 2, spinner = 17f)
            },
            part("tail", "Tailplane", z = 30) {
                airfoil(248f, 168f, 268f, 14f, root = 20f, span = 150f)
                slab(Role.BODY, 1, 214f, 272f, 244f, 200f, 196f, 196f, 186f, 268f)
            },
            part("rudder", "Rudder", z = 32, depth = 7f) {
                slab(Role.BODY, 0, 196f, 272f, 186f, 196f, 140f, 208f, 150f, 282f)
            },
            part("gear", "Landing gear", z = 14) {
                gearLeg(600f, 336f, 430f, 30f, rake = 10f)
                gearLeg(690f, 334f, 430f, 30f, rake = -10f)
                box(Role.METAL, 0, 604f, 422f, 92f, 9f)
            },
            part("skid", "Tail skid", z = 14) {
                quad(Role.METAL, -1, 176f, 286f, 190f, 286f, 176f, 342f, 164f, 340f, split = 0)
                box(Role.DARK, -2, 158f, 336f, 26f, 8f)
            },
            part("guns", "Twin Vickers", z = 56) {
                aircraftGun(700f, 254f, 104f)
                aircraftGun(700f, 272f, 104f)
            },
            part("roundel", "RFC roundel", z = 60) { roundel(420f, 298f, 26f, skin = 42.5f) },
        ),
        decoys = 0,
        lore = SopwithCamelLore.lore,
    )

    fun fokkerDr1() = VehicleDef(
        id = "fokker_dr1",
        name = "Fokker Dr.I",
        era = Era.WWI,
        branch = Branch.AIR,
        country = "Germany",
        year = 1917,
        fact = "Three stubby wings gave it a climb and turn rate nothing could follow - "
            + "Manfred von Richthofen scored his last 19 victories in one.",
        palette = FOKKER_RED,
        // 5.77 m long, drawn 647 units; thrust line at y 300.
        model = ModelSource("models/fokker_dr1.glb", scale = 112.1f, originX = 528f,
                            originY = -297f,
                            internals = "models/fokker_dr1_int.glb"),
        parts = listOf(
            part("fuselage", "Fuselage", z = 20, pre = true) {
                fuselage(206f, 290f, 20f, 330f, 300f, 40f, 560f, 304f, 44f, 700f, 302f, 42f)
                fuselageRings(260f, 560f, 295f, 30f, 304f, 44f, count = 6)
                aerial(240f, 274f, 470f, 262f)
            },
            part("top_wing", "Top wing", z = 46) {
                airfoil(624f, 534f, 168f, 18f, span = 308f)
                box(Role.BODY, 2, 534f, 158f, 90f, 6f)
                fabricRibs(624f, 534f, 168f, 36f, 304f, 8)
                navLight(580f, 166f, 302f)
            },
            part("mid_wing", "Middle wing", z = 44) {
                airfoil(616f, 528f, 246f, 17f, span = 266f)
                fabricRibs(616f, 528f, 246f, 36f, 262f, 7)
            },
            part("low_wing", "Bottom wing", z = 12) {
                airfoil(606f, 522f, 340f, 16f, span = 246f)
                fabricRibs(606f, 522f, 340f, 36f, 242f, 6)
            },
            part("struts", "Interplane struts", z = 42) {
                strut(608f, 172f, 604f, 336f, w = 6f, side = 236f)
                strut(548f, 176f, 548f, 262f, w = 5f, side = 236f)
                strut(548f, 262f, 546f, 338f, w = 5f, side = 236f)
            },
            part("engine", "Oberursel rotary", z = 50) {
                radialEngine(724f, 302f, 50f)
                slab(Role.BODY, 1, 700f, 260f, 760f, 268f, 764f, 340f, 700f, 344f)
            },
            part("prop", "Propeller", z = 58) {
                propeller(772f, 302f, 118f, blades = 2, spinner = 15f)
            },
            part("tail", "Tailplane", z = 30) {
                airfoil(252f, 176f, 274f, 14f, root = 20f, span = 140f)
            },
            part("rudder", "Comma rudder", z = 32, depth = 7f) {
                slab(Role.BODY, 0, 206f, 286f, 216f, 214f, 168f, 196f, 140f, 232f, 150f, 288f)
            },
            part("gear", "Undercarriage", z = 14) {
                gearLeg(596f, 338f, 424f, 28f, rake = 12f)
                gearLeg(680f, 336f, 424f, 28f, rake = -12f)
                airfoil(690f, 590f, 412f, 14f, role = Role.TRIM)
            },
            part("skid", "Tail skid", z = 14) {
                quad(Role.METAL, -1, 188f, 292f, 202f, 292f, 190f, 344f, 178f, 342f, split = 0)
                box(Role.DARK, -2, 172f, 338f, 26f, 8f)
            },
            part("guns", "Twin Spandau", z = 56) {
                aircraftGun(686f, 256f, 96f)
                aircraftGun(686f, 274f, 96f)
            },
            part("cross", "Iron cross", z = 60, inner = 44f, outer = 46f, mirrored = true) {
                box(Role.ACCENT, 0, 386f, 272f, 66f, 22f)
                box(Role.ACCENT, 1, 408f, 250f, 22f, 66f)
            },
        ),
        decoys = 1,
        lore = FokkerDr1Lore.lore,
    )

    // -----------------------------------------------------------------------
    // Interwar
    // -----------------------------------------------------------------------

    fun ju52() = VehicleDef(
        id = "ju52",
        name = "Junkers Ju 52",
        era = Era.INTERWAR,
        branch = Branch.AIR,
        country = "Germany",
        year = 1932,
        fact = "Corrugated duralumin skin and three engines made 'Tante Ju' almost impossible "
            + "to break; some were still flying airline routes in the 1980s.",
        palette = LUFTHANSA,
        // 18.90 m long, drawn 750 units; thrust line at y 300.
        model = ModelSource("models/ju52.glb", scale = 39.7f, originX = 519f,
                            originY = -288f,
                            internals = "models/ju52_int.glb"),
        parts = listOf(
            part("fuselage", "Corrugated fuselage", z = 20, pre = true) {
                fuselage(176f, 282f, 26f, 300f, 292f, 54f, 620f, 294f, 60f, 780f, 292f, 42f)
                for (i in 0 until 14) box(Role.DARK, -2, 220f + i * 40f, 246f, 3f, 100f)
                fuselageRings(240f, 700f, 288f, 44f, 294f, 60f, count = 9)
                aerial(250f, 260f, 520f, 246f)
            },
            part("wing", "Cantilever wing", z = 12) {
                airfoil(562f, 430f, 336f, 26f, span = 470f)
                box(Role.BODY, -1, 430f, 332f, 132f, 7f)
                fabricRibs(562f, 430f, 336f, 62f, 462f, 11)
                pitot(566f, 326f, 44f, 360f)
                navLight(500f, 334f, 462f)
            },
            part("eng_nose", "Nose engine", z = 50) {
                radialEngine(792f, 292f, 50f)
            },
            part("prop_nose", "Nose propeller", z = 58) {
                propeller(830f, 292f, 116f, blades = 3, spinner = 16f)
            },
            part("eng_left", "Left wing engine", z = 48) {
                slab(Role.BODY, 1, 560f, 336f, 660f, 332f, 668f, 384f, 560f, 388f)
                radialEngine(664f, 358f, 38f)
            },
            part("prop_left", "Left propeller", z = 56) {
                propeller(694f, 358f, 86f, blades = 3, spinner = 13f)
            },
            part("tail", "Tailplane", z = 30) {
                airfoil(254f, 146f, 260f, 18f, root = 26f, span = 200f)
                box(Role.BODY, -1, 96f, 254f, 134f, 7f)
            },
            part("fin", "Fin and rudder", z = 32, depth = 7f) {
                slab(Role.BODY, 1, 236f, 268f, 196f, 178f, 132f, 180f, 122f, 268f)
                box(Role.DARK, -2, 150f, 182f, 5f, 82f)
            },
            part("gear", "Spatted gear", z = 14) {
                slab(Role.BODY, 0, 604f, 340f, 668f, 344f, 660f, 424f, 606f, 420f)
                ngon(Role.DARK, -1, 634f, 424f, 32f, sides = 10)
            },
            part("tailwheel", "Tail wheel", z = 14) {
                quad(Role.METAL, -1, 170f, 286f, 184f, 286f, 172f, 340f, 160f, 338f, split = 0)
                ngon(Role.DARK, -1, 168f, 344f, 14f, sides = 7)
            },
            part("windows", "Cabin windows", z = 60, inner = 52f, outer = 55f, mirrored = true) {
                for (i in 0 until 6) box(Role.GLASS, 2, 342f + i * 58f, 262f, 34f, 24f)
            },
            part("cockpit", "Cockpit glazing", z = 60) {
                canopy(720f, 800f, 258f, 36f, framed = true)
            },
            part("door", "Cargo door", z = 40, inner = 54f, outer = 58f, mirrored = true) {
                slab(Role.TRIM, -1, 268f, 262f, 330f, 260f, 330f, 336f, 268f, 336f)
                box(Role.DARK, -3, 320f, 292f, 8f, 14f)
            },
        ),
        decoys = 2,
        lore = Ju52Lore.lore,
    )

    // -----------------------------------------------------------------------
    // WWII
    // -----------------------------------------------------------------------

    fun spitfire() = VehicleDef(
        id = "spitfire",
        name = "Supermarine Spitfire Mk V",
        era = Era.WWII,
        branch = Branch.AIR,
        country = "United Kingdom",
        year = 1941,
        fact = "The elliptical wing was a nightmare to build but let the Spitfire out-turn "
            + "the Bf 109 - and it stayed in production the entire war.",
        palette = RAF_GREEN,
        // 9.12 m nose to tail, drawn 700 units long, with the thrust line at y 294.
        model = ModelSource("models/spitfire.glb", scale = 76.75f, originX = 506f,
                            originY = -294f,
                            internals = "models/spitfire_int.glb"),
        parts = listOf(
            part("fuselage", "Monocoque fuselage", z = 20, pre = true) {
                fuselage(186f, 276f, 18f, 300f, 288f, 44f, 520f, 296f, 52f, 700f, 296f, 44f)
                // Flush-riveted stressed skin: panel joints, not the formers of a fabric
                // aeroplane. The Spitfire's finish was famously smooth for its day.
                fuselageRings(300f, 520f, 288f, 44f, 296f, 52f, count = 4)
                rivets(330f, 274f, 500f, 278f, 12, 40f, r = 2.4f)
                aerial(250f, 268f, 520f, 252f)
            },
            part("wing", "Elliptical wing", z = 12) {
                airfoil(622f, 440f, 330f, 24f, span = 408f)
                slab(Role.BODY, -1, 440f, 324f, 612f, 318f, 630f, 336f, 448f, 342f)
                rivets(612f, 326f, 452f, 326f, 8, 200f, r = 2.2f)
                pitot(626f, 330f, 44f, 320f)
                navLight(566f, 330f, 400f)
            },
            part("cowl", "Merlin cowling", z = 50) {
                cowling(700f, 812f, 294f, 46f, 26f, exhausts = 6)
                slab(Role.BODY, 2, 700f, 250f, 800f, 264f, 812f, 278f, 700f, 274f)
            },
            part("prop", "Rotol propeller", z = 58) {
                propeller(834f, 294f, 138f, blades = 3, spinner = 22f)
            },
            part("canopy", "Bubble canopy", z = 60) {
                canopy(524f, 664f, 252f, 44f, framed = true)
            },
            part("tail", "Tailplane", z = 30) {
                airfoil(258f, 172f, 268f, 16f, root = 22f, span = 168f)
            },
            part("fin", "Fin and rudder", z = 32, depth = 7f) {
                slab(Role.BODY, 1, 248f, 268f, 218f, 176f, 168f, 172f, 156f, 274f)
                box(Role.DARK, -2, 186f, 178f, 5f, 92f)
            },
            part("gear", "Main gear", z = 14) {
                gearLeg(648f, 330f, 418f, 30f, rake = 14f)
            },
            part("tailwheel", "Tail wheel", z = 14) {
                quad(Role.METAL, -1, 182f, 282f, 194f, 282f, 184f, 322f, 174f, 320f, split = 0)
                ngon(Role.DARK, -1, 180f, 326f, 13f, sides = 7)
            },
            part("guns", "Wing cannon", z = 44) {
                aircraftGun(700f, 338f, 118f, r = 6f)
            },
            part("radiator", "Radiator", z = 40, inner = 40f, outer = 96f, mirrored = true) {
                slab(Role.TRIM, -1, 540f, 330f, 640f, 328f, 636f, 366f, 546f, 368f)
                for (i in 0 until 5) box(Role.DARK, -3, 556f + i * 18f, 338f, 6f, 22f)
            },
            part("roundel", "RAF roundel", z = 62) { roundel(400f, 292f, 28f, skin = 47.6f) },
            part("stripe", "Sky band", z = 42, depth = 44f) {
                box(Role.ACCENT, 1, 252f, 268f, 24f, 54f)
            },
        ),
        decoys = 2,
        lore = SpitfireLore.lore,
    )

    fun b17() = VehicleDef(
        id = "b17",
        name = "B-17G Flying Fortress",
        era = Era.WWII,
        branch = Branch.AIR,
        country = "United States",
        year = 1943,
        fact = "Thirteen .50 calibre guns and a reputation for coming home in pieces - "
            + "12,731 built, and they dropped more bombs than any other US aircraft.",
        palette = USAAF_BARE,
        // 22.66 m nose to tail, drawn 752 units long, centreline at y 290.
        model = ModelSource("models/b17.glb", scale = 33.2f, originX = 508f,
                            originY = -290f,
                            internals = "models/b17_int.glb"),
        parts = listOf(
            part("fuselage", "Fuselage", z = 20, pre = true) {
                fuselage(150f, 268f, 20f, 260f, 288f, 48f, 620f, 296f, 56f, 850f, 294f, 34f)
                fuselageRings(270f, 620f, 288f, 48f, 296f, 56f, count = 7)
                rivets(300f, 268f, 600f, 272f, 14, 44f, r = 2.6f)
                aerial(268f, 262f, 600f, 244f)
            },
            part("wing", "Main wing", z = 12) {
                airfoil(604f, 444f, 334f, 28f, span = 512f)
                slab(Role.BODY, -1, 444f, 330f, 596f, 322f, 612f, 340f, 452f, 346f)
                rivets(596f, 330f, 460f, 330f, 9, 260f, r = 2.6f)
                pitot(608f, 334f, 46f, 420f)
                navLight(524f, 334f, 504f)
            },
            part("eng_in", "Inboard engine", z = 48) {
                slab(Role.BODY, 1, 560f, 306f, 654f, 302f, 664f, 356f, 562f, 360f)
                radialEngine(660f, 330f, 36f)
            },
            part("eng_out", "Outboard engine", z = 46) {
                slab(Role.BODY, 0, 424f, 312f, 510f, 308f, 518f, 356f, 426f, 360f)
                radialEngine(514f, 332f, 32f)
            },
            part("prop_in", "Inboard propeller", z = 56) {
                propeller(688f, 330f, 108f, blades = 3, spinner = 15f)
            },
            part("prop_out", "Outboard propeller", z = 54) {
                propeller(542f, 332f, 98f, blades = 3, spinner = 14f)
            },
            part("tail", "Tailplane", z = 30) {
                airfoil(244f, 140f, 264f, 20f, root = 26f, span = 212f)
            },
            part("fin", "Tall fin", z = 32, depth = 7f) {
                slab(Role.BODY, 1, 268f, 272f, 206f, 146f, 142f, 150f, 132f, 272f)
                box(Role.DARK, -2, 164f, 154f, 5f, 112f)
            },
            part("nose", "Bombardier nose", z = 52, depth = 34f) {
                slab(Role.GLASS, 2, 830f, 262f, 884f, 288f, 884f, 306f, 830f, 328f, contrast = 3)
                box(Role.DARK, -2, 852f, 262f, 5f, 64f)
            },
            part("turret_top", "Top turret", z = 58, depth = 30f) {
                ngon(Role.GLASS, 2, 736f, 250f, 30f, sides = 9)
                aircraftGun(742f, 240f, 62f)
            },
            part("turret_ball", "Ball turret", z = 16, depth = 34f) {
                ngon(Role.METAL, 0, 380f, 356f, 34f, sides = 10)
                ngon(Role.GLASS, 1, 380f, 356f, 17f, sides = 7)
            },
            part("gear", "Main gear", z = 14) {
                gearLeg(624f, 348f, 424f, 30f)
                gearLeg(654f, 348f, 424f, 30f)
            },
            part("tailgun", "Tail guns", z = 34, depth = 26f) {
                slab(Role.GLASS, 1, 150f, 278f, 176f, 272f, 176f, 300f, 150f, 296f)
                aircraftGun(140f, 282f, 62f)
                aircraftGun(140f, 296f, 62f)
            },
            part("star", "US star", z = 62, inner = 46f, outer = 48f, mirrored = true) {
                roundel(392f, 291f, 30f, skin = 50.9f)
            },
        ),
        decoys = 3,
        lore = B17Lore.lore,
    )

    // -----------------------------------------------------------------------
    // Cold War
    // -----------------------------------------------------------------------

    fun mig15() = VehicleDef(
        id = "mig15",
        name = "MiG-15",
        era = Era.COLD_WAR,
        branch = Branch.AIR,
        country = "Soviet Union",
        year = 1949,
        fact = "A swept wing on a copied Rolls-Royce Nene engine - it appeared over Korea and "
            + "made every straight-wing jet in the West obsolete overnight.",
        palette = MIG_SILVER,
        // 10.10 m long, drawn 726 units; centreline at y 297.
        model = ModelSource("models/mig15.glb", scale = 71.9f, originX = 525f,
                            originY = -297f,
                            internals = "models/mig15_int.glb"),
        parts = listOf(
            part("fuselage", "Fuselage", z = 20, pre = true) {
                fuselage(230f, 300f, 40f, 420f, 300f, 56f, 660f, 296f, 58f, 812f, 292f, 46f)
                fuselageRings(420f, 660f, 300f, 56f, 296f, 58f, count = 4)
                rivets(450f, 268f, 640f, 266f, 10, 52f, r = 2.2f)
            },
            part("intake", "Nose intake", z = 50) {
                intake(828f, 292f, 48f, depth = 40f)
                slab(Role.BODY, 1, 812f, 246f, 856f, 250f, 856f, 334f, 812f, 338f)
            },
            part("wing", "Swept wing", z = 12) {
                airfoil(566f, 414f, 326f, 24f, span = 320f)
                slab(Role.BODY, -1, 414f, 328f, 558f, 320f, 572f, 338f, 422f, 344f)
                rivets(558f, 330f, 424f, 330f, 8, 160f, r = 2.2f)
                pitot(570f, 330f, 52f, 300f)
                navLight(500f, 332f, 314f)
            },
            part("fence", "Wing fences", z = 40, inner = 120f, outer = 126f, mirrored = true) {
                for (i in 0 until 2) box(Role.TRIM, 1, 480f + i * 80f, 302f, 6f, 22f)
            },
            part("tail", "Tailplane", z = 30) {
                airfoil(268f, 190f, 202f, 16f, root = 22f, span = 150f)
            },
            part("fin", "Swept fin", z = 32, depth = 7f) {
                slab(Role.BODY, 1, 288f, 268f, 232f, 192f, 158f, 196f, 216f, 276f)
            },
            part("nozzle", "Jet nozzle", z = 22) {
                nozzle(238f, 300f, 40f, len = 58f)
            },
            part("canopy", "Canopy", z = 60) {
                canopy(668f, 780f, 250f, 42f)
            },
            part("gear_nose", "Nose gear", z = 14) {
                gearLeg(770f, 330f, 408f, 24f, rake = -8f)
            },
            part("gear_main", "Main gear", z = 14) {
                gearLeg(560f, 334f, 412f, 28f, rake = 10f)
            },
            part("guns", "37 mm cannon", z = 52) {
                aircraftGun(790f, 344f, 84f, r = 8f)
            },
            part("tank", "Drop tank", z = 10) {
                store(470f, 372f, 190f, 22f)
            },
            part("star", "Red star", z = 62, inner = 46f, outer = 48f, mirrored = true) {
                ngon(Role.ACCENT, 0, 360f, 282f, 28f, sides = 10, lit = false)
                ngon(Role.ACCENT, 3, 360f, 282f, 13f, sides = 5, lit = false)
            },
        ),
        decoys = 3,
        lore = Mig15Lore.lore,
    )

    // -----------------------------------------------------------------------
    // Modern
    // -----------------------------------------------------------------------

    fun f16() = VehicleDef(
        id = "f16",
        name = "F-16C Fighting Falcon",
        era = Era.MODERN,
        branch = Branch.AIR,
        country = "United States",
        year = 1984,
        fact = "The first production fighter built to be aerodynamically unstable - only the "
            + "fly-by-wire computer keeps it in the air, and that is what makes it turn.",
        palette = USAF_GREY,
        // 15.06 m long, drawn 783 units; centreline at y 297.
        model = ModelSource("models/f16.glb", scale = 52.0f, originX = 562f,
                            originY = -297f,
                            internals = "models/f16_int.glb"),
        parts = listOf(
            part("fuselage", "Fuselage", z = 20, pre = true) {
                fuselage(210f, 300f, 44f, 400f, 300f, 58f, 640f, 296f, 60f, 800f, 290f, 40f)
                slab(Role.BODY, 2, 800f, 268f, 894f, 288f, 894f, 300f, 800f, 314f)
                fuselageRings(400f, 640f, 300f, 58f, 296f, 60f, count = 3)
                panel(430f, 272f, 130f, 40f, 58f, bolts = 0)
            },
            part("intake", "Ventral intake", z = 18) {
                intake(768f, 352f, 40f, depth = 44f)
                slab(Role.BODY, -1, 400f, 336f, 776f, 326f, 776f, 386f, 420f, 380f)
            },
            part("wing", "Blended wing", z = 12) {
                airfoil(576f, 340f, 330f, 26f, span = 232f)
                slab(Role.BODY, -1, 340f, 330f, 570f, 318f, 592f, 340f, 356f, 350f)
                pitot(580f, 332f, 46f, 210f)
                navLight(470f, 334f, 228f)
            },
            part("lerx", "Strake", z = 24, depth = 26f) {
                slab(Role.BODY, 1, 640f, 300f, 790f, 282f, 794f, 296f, 648f, 316f)
            },
            part("tail", "Stabilator", z = 30) {
                airfoil(312f, 218f, 320f, 18f, root = 24f, span = 150f)
            },
            part("fin", "Vertical fin", z = 32, depth = 7f) {
                slab(Role.BODY, 1, 350f, 262f, 292f, 150f, 216f, 152f, 232f, 268f)
                box(Role.DARK, -2, 246f, 158f, 5f, 100f)
            },
            part("strake", "Ventral fins", z = 16, depth = 8f) {
                slab(Role.TRIM, -1, 300f, 336f, 350f, 334f, 328f, 382f, 296f, 382f)
            },
            part("nozzle", "F110 nozzle", z = 22) {
                nozzle(224f, 300f, 44f, len = 66f)
            },
            part("canopy", "Bubble canopy", z = 60) {
                canopy(676f, 820f, 248f, 50f)
            },
            part("gear_nose", "Nose gear", z = 14) {
                gearLeg(742f, 386f, 428f, 22f)
            },
            part("gear_main", "Main gear", z = 14) {
                gearLeg(552f, 350f, 428f, 26f, rake = 8f)
            },
            part("aim9", "AIM-9 Sidewinder", z = 10) {
                store(300f, 392f, 210f, 15f)
                slab(Role.DARK, 0, 500f, 384f, 524f, 388f, 524f, 396f, 500f, 400f)
            },
            part("tank", "Drop tank", z = 11) {
                store(430f, 366f, 178f, 24f)
            },
            part("radome", "Radome", z = 52, depth = 22f) {
                slab(Role.DARK, -1, 872f, 282f, 906f, 292f, 906f, 300f, 872f, 310f)
            },
            part("star", "USAF insignia", z = 62, inner = 46f, outer = 48f, mirrored = true) {
                roundel(452f, 299f, 24f, skin = 58.4f)
            },
        ),
        decoys = 4,
        lore = F16Lore.lore,
    )

    fun all(): List<VehicleDef> = listOf(
        sopwithCamel(), fokkerDr1(), ju52(), spitfire(), b17(), mig15(), f16(),
    )
}
