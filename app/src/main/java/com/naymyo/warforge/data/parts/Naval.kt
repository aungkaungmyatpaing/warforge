package com.naymyo.warforge.data.parts

import com.naymyo.warforge.data.Branch
import com.naymyo.warforge.data.Era
import com.naymyo.warforge.data.ModelSource
import com.naymyo.warforge.data.VehicleDef
import com.naymyo.warforge.data.lore.DreadnoughtLore
import com.naymyo.warforge.data.lore.FletcherLore
import com.naymyo.warforge.data.lore.GrafSpeeLore
import com.naymyo.warforge.data.lore.LosAngelesLore
import com.naymyo.warforge.data.lore.NimitzLore
import com.naymyo.warforge.data.lore.UBoatLore
import com.naymyo.warforge.data.lore.YamatoLore
import com.naymyo.warforge.poly.Palette
import com.naymyo.warforge.poly.Role

/**
 * Naval units, WWI to the present. Side view, bow to the right, floating on
 * [WATERLINE].
 *
 * Draw order: 10 hull, 20 deck fittings, 30 superstructure, 40 turrets, 50 masts,
 * 60 radar and small gear.
 */
object Naval {

    private val RN_GREY = Palette.of(0x6E7A80, 0x22282B, 0x9DA5A8, 0x44555E, 0xA8382A, 0x5C676C)
    private val UBOAT = Palette.of(0x585F63, 0x1C2022, 0x8E9497, 0x3E4C54, 0x9A3327, 0x474D50)
    private val KM_GREY = Palette.of(0x7B858A, 0x23282B, 0xA2A9AC, 0x46565F, 0x1E2A33, 0x697277)
    private val USN_BLUE = Palette.of(0x4E5A66, 0x1B2026, 0x929AA0, 0x435460, 0x8E2F26, 0x424D57)
    private val IJN_GREY = Palette.of(0x69737A, 0x1F2427, 0x99A0A4, 0x42525B, 0xA33228, 0x596268)
    private val SSN_BLACK = Palette.of(0x363B3F, 0x15181A, 0x7E8488, 0x39464D, 0x8E3228, 0x2C3033)
    private val CVN_GREY = Palette.of(0x5F6A72, 0x1E2326, 0x969DA1, 0x41515A, 0xB8952E, 0x515A61)

    // -----------------------------------------------------------------------
    // WWI
    // -----------------------------------------------------------------------

    fun dreadnought() = VehicleDef(
        id = "dreadnought",
        name = "HMS Dreadnought",
        era = Era.WWI,
        branch = Branch.NAVAL,
        country = "United Kingdom",
        year = 1906,
        fact = "Ten 12-inch guns and turbine power made every other battleship in the world "
            + "obsolete the day she launched - navies still counted 'dreadnoughts' for decades.",
        palette = RN_GREY,
        // 160.6 m long, drawn 834 units; waterline near y 398.
        model = ModelSource("models/dreadnought.glb", scale = 5.19f, originX = 505f,
                            originY = -398f,
                            internals = "models/dreadnought_int.glb"),
        parts = listOf(
            part("hull", "Hull", z = 10, pre = true, depth = 92f) {
                slab(
                    Role.BODY, 0,
                    96f, 392f, 118f, 356f, 800f, 352f, 900f, 340f, 930f, 384f,
                    900f, 446f, 150f, 450f,
                    contrast = 3,
                )
                waterline(98f, 928f)
                portholes(180f, 860f, 378f, 92f, 16)
                shipLadder(560f, 356f, 428f, 92f)
                weld(140f, 400f, 890f, 396f, 92f)
            },
            part("deck", "Main deck", z = 20, depth = 86f) {
                box(Role.TRIM, 2, 130f, 344f, 760f, 12f)
                for (i in 0 until 16) box(Role.DARK, -3, 150f + i * 48f, 344f, 3f, 12f)
                decking(140f, 890f, 344f, 84f, planks = 9)
                railing(150f, 880f, 344f, 84f, posts = 16)
                deckFittings(820f, 344f, 84f)
            },
            part("turret_a", "A turret", z = 40) {
                navalTurret(772f, 344f, 118f, 40f, barrels = 2, barrelLen = 122f)
            },
            part("turret_b", "B turret", z = 40) {
                navalTurret(236f, 344f, 118f, 40f, barrels = 2, barrelLen = 122f, facingRight = false)
            },
            part("turret_p", "Wing turret", z = 41) {
                navalTurret(470f, 336f, 108f, 38f, barrels = 2, barrelLen = 108f)
            },
            part("bridge", "Bridge", z = 30) {
                deckHouse(596f, 344f, 140f, 56f)
                deckHouse(620f, 288f, 92f, 34f)
            },
            part("funnel_f", "Forward funnel", z = 32) {
                funnel(552f, 344f, 214f, 54f, rake = 6f)
            },
            part("funnel_a", "Aft funnel", z = 32) {
                funnel(372f, 344f, 222f, 54f, rake = 6f)
            },
            part("mast_f", "Tripod mast", z = 50) {
                mast(600f, 288f, 150f, lattice = false, yards = 2)
                box(Role.METAL, 0, 586f, 176f, 40f, 16f)
            },
            part("mast_a", "Aft mast", z = 50) {
                mast(300f, 344f, 196f, lattice = false, yards = 1)
            },
            part("boats", "Ship's boats", z = 34) {
                lifeboat(452f, 316f, 96f)
                lifeboat(340f, 316f, 96f)
            },
            part("casemate", "Casemate guns", z = 22, inner = 88f, outer = 108f, mirrored = true) {
                for (i in 0 until 4) {
                    box(Role.DARK, -2, 640f + i * 56f, 372f, 20f, 18f)
                    tube(Role.METAL, 0, 660f + i * 56f, 380f, 4f, 706f + i * 56f, 376f, 3f)
                }
            },
            part("anchor", "Anchor gear", z = 24) { anchor(878f, 368f) },
        ),
        decoys = 1,
        lore = DreadnoughtLore.lore,
    )

    fun uBoat() = VehicleDef(
        id = "u9",
        name = "SM U-9",
        era = Era.WWI,
        branch = Branch.NAVAL,
        country = "Germany",
        year = 1910,
        fact = "On 22 September 1914 she sank three British cruisers in under an hour, and "
            + "every navy suddenly took submarines seriously.",
        palette = UBOAT,
        // 57.4 m long, drawn 790 units; waterline near y 384.
        model = ModelSource("models/u9.glb", scale = 13.76f, originX = 505f,
                            originY = -384f,
                            internals = "models/u9_int.glb"),
        parts = listOf(
            part("hull", "Pressure hull", z = 10, pre = true, depth = 92f) {
                fuselage(120f, 404f, 34f, 240f, 400f, 46f, 700f, 398f, 48f, 900f, 392f, 26f)
                waterline(110f, 900f, band = 16f)
            },
            part("casing", "Deck casing", z = 20, depth = 58f) {
                slab(Role.TRIM, 1, 180f, 352f, 860f, 348f, 880f, 372f, 172f, 376f)
                for (i in 0 until 12) box(Role.DARK, -3, 220f + i * 52f, 354f, 3f, 16f)
                railing(210f, 840f, 350f, 56f, posts = 11)
                rivets(220f, 366f, 840f, 362f, 20, 58f, r = 3f)
            },
            part("tower", "Conning tower", z = 30, depth = 44f) {
                slab(Role.BODY, 1, 430f, 352f, 446f, 296f, 560f, 296f, 574f, 352f, contrast = 3)
                box(Role.DARK, -2, 470f, 300f, 60f, 5f)
            },
            part("bridge", "Bridge rail", z = 34) {
                box(Role.METAL, 0, 434f, 292f, 138f, 4f)
                for (i in 0 until 5) box(Role.METAL, -1, 442f + i * 32f, 276f, 4f, 18f)
            },
            part("periscope", "Periscopes", z = 50, depth = 8f) {
                box(Role.METAL, 1, 486f, 208f, 7f, 90f)
                box(Role.METAL, 0, 508f, 226f, 7f, 72f)
                box(Role.DARK, -2, 482f, 204f, 15f, 8f)
            },
            part("gun", "8.8 cm deck gun", z = 40) {
                navalTurret(660f, 352f, 44f, 20f, barrels = 1, barrelLen = 110f, barrelR = 6f)
            },
            part("planes", "Dive planes", z = 12, depth = 8f) {
                airfoil(172f, 96f, 404f, 16f, role = Role.TRIM)
                airfoil(880f, 812f, 398f, 14f, role = Role.TRIM)
            },
            part("screw", "Screw and rudder", z = 14) {
                screw(126f, 404f, 44f, blades = 4)
                slab(Role.TRIM, 0, 150f, 374f, 168f, 372f, 168f, 436f, 150f, 436f)
            },
            part("tubes", "Torpedo tube doors", z = 22, inner = 0f, outer = 40f, mirrored = true) {
                ngon(Role.DARK, -2, 878f, 388f, 15f, sides = 7)
                ngon(Role.DARK, -2, 878f, 410f, 15f, sides = 7)
            },
            part("vents", "Ballast vents", z = 24, inner = 54f, outer = 60f, mirrored = true) {
                for (i in 0 until 6) box(Role.DARK, -3, 250f + i * 90f, 386f, 26f, 8f)
            },
        ),
        decoys = 1,
        lore = UBoatLore.lore,
    )

    // -----------------------------------------------------------------------
    // Interwar
    // -----------------------------------------------------------------------

    fun grafSpee() = VehicleDef(
        id = "graf_spee",
        name = "Admiral Graf Spee",
        era = Era.INTERWAR,
        branch = Branch.NAVAL,
        country = "Germany",
        year = 1936,
        fact = "A 'pocket battleship' built to treaty weight: cruiser tonnage carrying "
            + "battleship guns, and the first big warship to go to sea with search radar.",
        palette = KM_GREY,
        // 186 m long, drawn 854 units; waterline near y 393.
        model = ModelSource("models/graf_spee.glb", scale = 4.59f, originX = 510f,
                            originY = -393f,
                            internals = "models/graf_spee_int.glb"),
        parts = listOf(
            part("hull", "Hull", z = 10, pre = true, depth = 92f) {
                slab(
                    Role.BODY, 0,
                    86f, 386f, 108f, 350f, 820f, 344f, 918f, 326f, 940f, 372f,
                    906f, 442f, 140f, 446f,
                    contrast = 3,
                )
                waterline(88f, 938f)
                portholes(190f, 870f, 372f, 92f, 15)
                shipLadder(540f, 348f, 424f, 92f)
            },
            part("deck", "Main deck", z = 20, depth = 86f) {
                box(Role.TRIM, 2, 120f, 338f, 790f, 11f)
                decking(130f, 900f, 338f, 84f, planks = 9)
                railing(140f, 890f, 338f, 84f, posts = 16)
                deckFittings(830f, 338f, 84f)
            },
            part("turret_a", "Forward triple turret", z = 40) {
                navalTurret(790f, 338f, 134f, 44f, barrels = 3, barrelLen = 140f)
            },
            part("turret_b", "Aft triple turret", z = 40) {
                navalTurret(210f, 338f, 134f, 44f, barrels = 3, barrelLen = 140f, facingRight = false)
            },
            part("tower", "Tower superstructure", z = 30, depth = 44f) {
                deckHouse(540f, 338f, 170f, 62f)
                deckHouse(566f, 276f, 118f, 44f)
                deckHouse(588f, 232f, 74f, 34f)
            },
            part("funnel", "Funnel", z = 32) {
                funnel(430f, 338f, 238f, 62f, rake = 8f)
            },
            part("mast", "Tower mast", z = 50) {
                mast(612f, 232f, 122f, lattice = false, yards = 2)
            },
            part("radar", "Seetakt radar", z = 60) {
                radarArray(614f, 196f, 78f, 34f)
            },
            part("secondary", "15 cm casemates", z = 22) {
                for (i in 0 until 3) navalTurret(620f + i * 88f, 336f, 52f, 22f, 1, 68f, barrelR = 5f)
            },
            part("aa", "AA mounts", z = 42) {
                aaMount(486f, 304f, 17f)
                aaMount(370f, 312f, 17f)
            },
            part("catapult", "Floatplane catapult", z = 34, depth = 30f) {
                box(Role.METAL, 0, 290f, 312f, 120f, 9f)
                slab(Role.TRIM, 1, 300f, 312f, 334f, 296f, 372f, 298f, 384f, 312f)
                airfoil(376f, 296f, 300f, 12f, role = Role.TRIM)
            },
            part("boats", "Ship's boats", z = 36) {
                lifeboat(500f, 326f, 84f)
                lifeboat(268f, 328f, 84f)
            },
        ),
        decoys = 2,
        lore = GrafSpeeLore.lore,
    )

    // -----------------------------------------------------------------------
    // WWII
    // -----------------------------------------------------------------------

    fun fletcher() = VehicleDef(
        id = "fletcher",
        name = "Fletcher-class Destroyer",
        era = Era.WWII,
        branch = Branch.NAVAL,
        country = "United States",
        year = 1942,
        fact = "175 of them were built - fast, tough, and so well balanced that some served "
            + "into the 1970s under six different flags.",
        palette = USN_BLUE,
        // 114.7 m long, drawn 836 units, with the waterline at y 380.
        model = ModelSource("models/fletcher.glb", scale = 7.29f, originX = 516f,
                            originY = -380f,
                            internals = "models/fletcher_int.glb"),
        parts = listOf(
            part("hull", "Flush-deck hull", z = 10, pre = true, depth = 92f) {
                slab(
                    Role.BODY, 0,
                    98f, 380f, 116f, 352f, 800f, 346f, 912f, 322f, 934f, 366f,
                    898f, 430f, 146f, 434f,
                    contrast = 3,
                )
                waterline(100f, 932f, band = 20f)
                portholes(220f, 780f, 368f, 82f, 12, r = 5f)
                weld(150f, 392f, 880f, 386f, 82f)
            },
            part("deck", "Weather deck", z = 20, depth = 86f) {
                box(Role.TRIM, 2, 126f, 340f, 770f, 10f)
                // A flush-decked destroyer: steel deck, no planking.
                railing(140f, 900f, 340f, 78f, posts = 18)
                deckFittings(820f, 340f, 78f)
            },
            part("gun_a", "No.1 5-inch mount", z = 40) {
                navalTurret(790f, 340f, 74f, 32f, barrels = 1, barrelLen = 104f)
            },
            part("gun_b", "Superfiring mount", z = 41) {
                box(Role.BODY, -1, 660f, 320f, 110f, 22f)
                navalTurret(714f, 318f, 74f, 32f, barrels = 1, barrelLen = 104f)
            },
            part("gun_x", "Aft mount", z = 40) {
                navalTurret(214f, 340f, 74f, 32f, barrels = 1, barrelLen = 104f, facingRight = false)
            },
            part("bridge", "Bridge", z = 30) {
                deckHouse(552f, 340f, 116f, 52f)
                deckHouse(572f, 288f, 76f, 34f)
            },
            part("funnel_f", "Forward funnel", z = 32) {
                funnel(486f, 340f, 250f, 44f, rake = 7f)
            },
            part("funnel_a", "Aft funnel", z = 32) {
                funnel(358f, 340f, 256f, 44f, rake = 7f)
            },
            part("mast", "Tripod mast", z = 50) {
                mast(592f, 288f, 158f, lattice = false, yards = 2)
            },
            part("radar", "SC air-search radar", z = 60) {
                radarArray(594f, 146f, 68f, 30f)
                radarDish(560f, 264f, 22f)
            },
            part("torps", "Torpedo tubes", z = 34, depth = 26f) {
                for (i in 0 until 2) {
                    slab(Role.BODY, 0, 400f + i * 20f, 332f, 400f + i * 20f, 310f, 456f + i * 20f, 310f, 456f + i * 20f, 332f)
                    tube(Role.METAL, 0, 456f + i * 20f, 321f, 11f, 470f + i * 20f, 321f, 10f)
                }
            },
            part("aa", "40 mm Bofors", z = 42) {
                aaMount(300f, 330f, 16f, barrels = 2)
                aaMount(640f, 314f, 16f, barrels = 2)
            },
            part("depth", "Depth charge racks", z = 22, depth = 40f) {
                for (i in 0 until 5) ngon(Role.DARK, -1, 136f + i * 24f, 334f, 10f, sides = 7)
            },
        ),
        decoys = 3,
        lore = FletcherLore.lore,
    )

    fun yamato() = VehicleDef(
        id = "yamato",
        name = "Yamato",
        era = Era.WWII,
        branch = Branch.NAVAL,
        country = "Japan",
        year = 1941,
        fact = "The largest battleship ever built: 72,000 tonnes and nine 460 mm guns, each "
            + "shell weighing as much as a small car.",
        palette = IJN_GREY,
        // 263 m long, drawn 882 units, with the waterline near y 384.
        model = ModelSource("models/yamato.glb", scale = 3.354f, originX = 511f,
                            originY = -384f,
                            internals = "models/yamato_int.glb"),
        parts = listOf(
            part("hull", "Hull", z = 10, pre = true, depth = 92f) {
                slab(
                    Role.BODY, 0,
                    70f, 386f, 92f, 346f, 810f, 338f, 926f, 314f, 952f, 364f,
                    920f, 452f, 126f, 456f,
                    contrast = 3,
                )
                waterline(72f, 950f, band = 26f)
                portholes(200f, 840f, 366f, 92f, 16)
                shipLadder(520f, 342f, 430f, 92f)
            },
            part("deck", "Armoured deck", z = 20, depth = 86f) {
                box(Role.TRIM, 2, 104f, 332f, 800f, 12f)
                box(Role.DARK, -3, 140f, 332f, 760f, 3f)
                decking(116f, 910f, 332f, 84f, planks = 11)
                railing(130f, 900f, 332f, 84f, posts = 18)
                deckFittings(840f, 332f, 84f)
            },
            part("turret_a", "No.1 460 mm turret", z = 40) {
                navalTurret(790f, 332f, 150f, 48f, barrels = 3, barrelLen = 135f)
            },
            part("turret_b", "No.2 turret", z = 41) {
                box(Role.BODY, -1, 620f, 306f, 176f, 28f)
                navalTurret(706f, 304f, 150f, 48f, barrels = 3, barrelLen = 135f)
            },
            part("turret_c", "Aft turret", z = 40) {
                navalTurret(212f, 332f, 150f, 48f, barrels = 3, barrelLen = 135f, facingRight = false)
            },
            part("pagoda", "Pagoda mast", z = 30) {
                deckHouse(486f, 332f, 130f, 60f)
                deckHouse(506f, 272f, 92f, 46f)
                deckHouse(520f, 226f, 66f, 40f)
                deckHouse(530f, 186f, 46f, 32f)
            },
            part("funnel", "Raked funnel", z = 32) {
                funnel(400f, 332f, 214f, 66f, rake = 14f)
            },
            part("rangefinder", "Main rangefinder", z = 52, depth = 46f) {
                slab(Role.BODY, 2, 500f, 186f, 508f, 160f, 570f, 160f, 578f, 186f)
                box(Role.METAL, 0, 476f, 166f, 126f, 8f)
            },
            part("secondary", "15.5 cm turret", z = 42) {
                navalTurret(596f, 330f, 70f, 28f, barrels = 3, barrelLen = 74f, barrelR = 5f)
            },
            part("aa", "25 mm AA battery", z = 44) {
                aaMount(444f, 316f, 16f, barrels = 3)
                aaMount(344f, 320f, 16f, barrels = 3)
                aaMount(286f, 322f, 16f, barrels = 3)
            },
            part("catapult", "Aircraft catapult", z = 34, depth = 30f) {
                box(Role.METAL, 0, 132f, 320f, 140f, 9f)
                slab(Role.TRIM, 1, 146f, 320f, 182f, 302f, 226f, 304f, 240f, 320f)
                airfoil(232f, 148f, 304f, 12f, role = Role.TRIM)
            },
            part("chrys", "Chrysanthemum crest", z = 60, inner = 90f, outer = 94f, mirrored = true) {
                ngon(Role.ACCENT, 2, 936f, 336f, 18f, sides = 12, lit = false)
            },
        ),
        decoys = 3,
        lore = YamatoLore.lore,
    )

    // -----------------------------------------------------------------------
    // Cold War
    // -----------------------------------------------------------------------

    fun losAngeles() = VehicleDef(
        id = "ssn688",
        name = "Los Angeles-class SSN",
        era = Era.COLD_WAR,
        branch = Branch.NAVAL,
        country = "United States",
        year = 1976,
        fact = "A teardrop hull around a nuclear reactor: quiet enough to trail Soviet boats "
            + "for weeks and fast enough to keep up with a carrier group.",
        palette = SSN_BLACK,
        // 110.3 m long, drawn 826 units; centreline at y 386.
        model = ModelSource("models/ssn688.glb", scale = 7.49f, originX = 539f,
                            originY = -386f,
                            internals = "models/ssn688_int.glb"),
        parts = listOf(
            part("hull", "Teardrop hull", z = 10, pre = true, depth = 92f) {
                fuselage(126f, 392f, 30f, 230f, 386f, 60f, 700f, 386f, 62f, 900f, 382f, 40f)
                slab(Role.BODY, 1, 900f, 342f, 952f, 372f, 952f, 392f, 900f, 422f, contrast = 3)
                waterline(130f, 950f, band = 14f)
            },
            part("sail", "Sail", z = 30, depth = 30f) {
                slab(Role.BODY, 1, 500f, 330f, 524f, 252f, 640f, 250f, 652f, 330f, contrast = 3)
                box(Role.DARK, -3, 556f, 256f, 54f, 5f)
                railing(530f, 630f, 250f, 28f, posts = 6)
            },
            part("planes", "Sail planes", z = 32, depth = 8f) {
                airfoil(700f, 470f, 282f, 16f, role = Role.TRIM)
            },
            part("masts", "Periscopes and masts", z = 50, depth = 9f) {
                box(Role.METAL, 1, 556f, 178f, 8f, 76f)
                box(Role.METAL, 0, 582f, 192f, 8f, 62f)
                box(Role.METAL, -1, 606f, 204f, 7f, 50f)
                box(Role.DARK, -2, 550f, 172f, 20f, 8f)
            },
            part("rudder", "Cruciform stern", z = 14, depth = 9f) {
                slab(Role.TRIM, 1, 182f, 344f, 206f, 300f, 226f, 302f, 214f, 348f)
                slab(Role.TRIM, -1, 182f, 424f, 206f, 464f, 226f, 462f, 214f, 420f)
                airfoil(230f, 126f, 386f, 14f, role = Role.TRIM)
            },
            part("screw", "Seven-blade screw", z = 12) {
                screw(146f, 386f, 52f, blades = 7)
            },
            part("sonar", "Bow sonar dome", z = 16, depth = 34f) {
                ngon(Role.TRIM, 0, 926f, 382f, 34f, sides = 10)
            },
            part("tubes", "Torpedo tubes", z = 20, inner = 0f, outer = 40f, mirrored = true) {
                ngon(Role.DARK, -2, 838f, 402f, 13f, sides = 7)
                ngon(Role.DARK, -2, 838f, 428f, 13f, sides = 7)
            },
            part("vls", "Vertical launch tubes", z = 22, depth = 40f) {
                for (i in 0 until 6) ngon(Role.DARK, -2, 726f + i * 26f, 350f, 9f, sides = 6)
            },
            part("tiles", "Anechoic tiles", z = 24, inner = 60f, outer = 62f, mirrored = true) {
                for (i in 0 until 14) box(Role.DARK, -3, 250f + i * 46f, 402f, 34f, 4f)
            },
        ),
        decoys = 3,
        lore = LosAngelesLore.lore,
    )

    // -----------------------------------------------------------------------
    // Modern
    // -----------------------------------------------------------------------

    fun nimitz() = VehicleDef(
        id = "cvn68",
        name = "Nimitz-class Carrier",
        era = Era.MODERN,
        branch = Branch.NAVAL,
        country = "United States",
        year = 1992,
        fact = "333 metres of flight deck, two reactors good for twenty years between "
            + "refuellings, and an air wing bigger than most countries' entire air force.",
        palette = CVN_GREY,
        // 332.8 m long, drawn 862 units; waterline near y 403.
        model = ModelSource("models/cvn68.glb", scale = 2.59f, originX = 513f,
                            originY = -403f,
                            internals = "models/cvn68_int.glb"),
        parts = listOf(
            part("hull", "Hull", z = 10, pre = true, depth = 92f) {
                slab(
                    Role.BODY, 0,
                    92f, 388f, 104f, 350f, 840f, 344f, 934f, 322f, 954f, 366f,
                    920f, 456f, 140f, 460f,
                    contrast = 3,
                )
                waterline(94f, 952f, band = 26f)
                portholes(240f, 800f, 372f, 92f, 14, r = 5f)
                shipLadder(600f, 350f, 440f, 92f)
            },
            part("deck", "Flight deck", z = 20, depth = 86f) {
                slab(Role.TRIM, 2, 66f, 330f, 962f, 322f, 962f, 346f, 66f, 352f, contrast = 2)
                box(Role.DARK, -2, 66f, 330f, 896f, 4f)
                railing(120f, 920f, 326f, 88f, posts = 22)
            },
            part("markings", "Deck markings", z = 22, depth = 96f) {
                box(Role.ACCENT, 2, 180f, 324f, 520f, 5f)
                for (i in 0 until 9) box(Role.ACCENT, 0, 210f + i * 78f, 324f, 34f, 4f)
            },
            part("island", "Island superstructure", z = 30) {
                deckHouse(376f, 322f, 116f, 74f)
                deckHouse(392f, 248f, 84f, 40f)
                box(Role.GLASS, 2, 400f, 216f, 66f, 20f)
            },
            part("mast", "Island mast", z = 50) {
                mast(434f, 208f, 118f, lattice = true, yards = 2)
            },
            part("radar", "SPS-48 radar", z = 60) {
                radarArray(436f, 172f, 66f, 40f)
                radarDish(392f, 236f, 20f)
                radarDish(478f, 240f, 18f)
            },
            part("sponsons", "Deck sponsons", z = 24, inner = 92f, outer = 116f, mirrored = true) {
                slab(Role.BODY, -1, 700f, 346f, 800f, 344f, 790f, 372f, 706f, 374f)
                slab(Role.BODY, -1, 180f, 350f, 280f, 348f, 270f, 376f, 186f, 378f)
            },
            part("ciws", "CIWS mounts", z = 44) {
                for (x in listOf(740f, 236f)) {
                    ngon(Role.BODY, 1, x, 344f, 15f, sides = 8)
                    ngon(Role.METAL, 2, x, 326f, 11f, sides = 7)
                    tube(Role.METAL, 0, x, 318f, 5f, x + 6f, 300f, 4f)
                }
            },
            part("jet_deck", "F/A-18 on deck", z = 40) {
                fuselage(560f, 302f, 12f, 620f, 300f, 17f, 690f, 300f, 12f)
                airfoil(658f, 596f, 310f, 12f)
                slab(Role.BODY, 1, 588f, 296f, 570f, 268f, 552f, 270f, 566f, 298f)
                gearLeg(674f, 310f, 322f, 8f)
            },
            part("elevator", "Deck-edge elevator", z = 26, inner = 92f, outer = 100f, mirrored = true) {
                slab(Role.BODY, 1, 806f, 346f, 892f, 344f, 892f, 384f, 806f, 386f)
                box(Role.DARK, -3, 820f, 356f, 58f, 4f)
            },
            part("cats", "Catapult tracks", z = 28, depth = 90f) {
                box(Role.DARK, -3, 700f, 326f, 250f, 5f)
                box(Role.DARK, -3, 560f, 332f, 230f, 5f)
            },
            part("screws", "Screws and rudders", z = 12) {
                screw(120f, 420f, 40f, blades = 5)
                slab(Role.TRIM, 0, 150f, 410f, 172f, 408f, 172f, 470f, 150f, 470f)
            },
        ),
        decoys = 4,
        lore = NimitzLore.lore,
    )

    fun all(): List<VehicleDef> = listOf(
        dreadnought(), uBoat(), grafSpee(), fletcher(), yamato(), losAngeles(), nimitz(),
    )
}
