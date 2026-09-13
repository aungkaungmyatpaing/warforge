package com.naymyo.warforge.data.parts

import com.naymyo.warforge.data.Branch
import com.naymyo.warforge.data.Era
import com.naymyo.warforge.data.ModelSource
import com.naymyo.warforge.data.VehicleDef
import com.naymyo.warforge.data.lore.AbramsLore
import com.naymyo.warforge.data.lore.CenturionLore
import com.naymyo.warforge.data.lore.MarkIVLore
import com.naymyo.warforge.data.lore.PanzerIVLore
import com.naymyo.warforge.data.lore.RenaultFTLore
import com.naymyo.warforge.data.lore.VickersMediumLore
import com.naymyo.warforge.data.lore.T34Lore
import com.naymyo.warforge.poly.Palette
import com.naymyo.warforge.poly.Role

/**
 * Ground units, WWI to the present. All drawn in side view with the nose to the right,
 * inside the 1000 x 620 blueprint, resting on [GROUND_Y].
 *
 * Draw order convention: 10 running gear, 20 hull, 30 fittings, 40 turret, 50 barrel,
 * 60 small details on top.
 */
object Ground {

    private val KHAKI = Palette.of(0x7A6A45, 0x2B261D, 0x8E897A, 0x38474E, 0xA8402F, 0x67593A)
    private val FRENCH = Palette.of(0x5F6B4B, 0x25281D, 0x8A8779, 0x39484A, 0xB0472F, 0x4E5A3E)
    private val BRITISH = Palette.of(0x5C6B52, 0x232821, 0x8C8A7E, 0x3A4A4C, 0xA83E2C, 0x4C5A44)
    private val DUNKELGELB = Palette.of(0x9A8756, 0x2A261C, 0x8F8B7D, 0x384549, 0x2F3A44, 0x6E5B3C)
    private val SOVIET = Palette.of(0x4C5D3A, 0x21261A, 0x87857A, 0x37474A, 0xB0332A, 0x3E4D30)
    private val NATO_GREEN = Palette.of(0x4E5B46, 0x1F241E, 0x8A887C, 0x3A4A4C, 0xB8A24A, 0x3F4A39)
    private val DESERT = Palette.of(0xA89268, 0x2C271E, 0x918C7E, 0x3C4A50, 0x3A4450, 0x8D7A55)

    // -----------------------------------------------------------------------
    // WWI
    // -----------------------------------------------------------------------

    /** The rhomboid outline the Mark IV's track runs around. Shared by hull and belt. */
    private val MK4 = floatArrayOf(
        880f, 348f, 838f, 300f, 560f, 276f, 262f, 316f,
        166f, 396f, 190f, 458f, 306f, 504f, 742f, 504f, 866f, 428f,
    )

    fun markIV() = VehicleDef(
        id = "mark_iv",
        name = "Mark IV",
        era = Era.WWI,
        branch = Branch.GROUND,
        country = "United Kingdom",
        year = 1917,
        fact = "The first tank built in real numbers - 1,220 of them - and the machine that "
            + "made trench lines crossable at Cambrai in 1917.",
        palette = KHAKI,
        // 8.05 m long over the hull, drawn 714 units; tracks on the ground at y 504.
        model = ModelSource("models/mark_iv.glb", scale = 88.7f, originX = 523f,
                            originY = -504f,
                            internals = "models/mark_iv_int.glb"),
        parts = listOf(
            part("hull", "Rhomboid hull", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, *MK4, contrast = 3)
                // Riveted, like every tank of its generation. Under fire the rivet heads
                // sheared off and flew round the inside of the hull.
                rivets(280f, 318f, 700f, 288f, 16, 108f, r = 5f)
                rivets(300f, 486f, 720f, 486f, 16, 108f, r = 5f)
                rivets(200f, 400f, 220f, 460f, 4, 108f, r = 5f)
                weld(262f, 316f, 560f, 276f, 108f)
                towHook(846f, 420f, 62f)
            },
            part("track", "Track belt", z = 30) { beltLoop(30f, *MK4) },
            part("cab", "Driver's cab", z = 40, depth = 78f) {
                slab(Role.BODY, 1, 596f, 274f, 760f, 286f, 760f, 214f, 616f, 210f)
                box(Role.GLASS, 1, 700f, 226f, 46f, 22f)
                box(Role.DARK, -2, 610f, 228f, 40f, 20f)
                rivets(606f, 220f, 752f, 226f, 8, 78f, r = 4f)
                rivets(606f, 268f, 752f, 278f, 8, 78f, r = 4f)
                visionPort(704f, 246f, 34f, 14f, 76f, mirrored = true)
            },
            part("sponson", "Gun sponson", z = 45, inner = 96f, outer = 156f, mirrored = true) {
                slab(Role.BODY, -1, 452f, 336f, 640f, 330f, 668f, 388f, 452f, 396f)
                box(Role.DARK, -3, 470f, 348f, 34f, 22f)
            },
            part("sponson_gun", "6-pdr gun", z = 50) {
                mainGun(660f, 360f, 176f, 10f, mantlet = true)
            },
            part("beam", "Unditching beam", z = 46, depth = 92f) {
                slab(Role.TRIM, 1, 320f, 268f, 660f, 250f, 660f, 272f, 320f, 290f)
                box(Role.DARK, -2, 380f, 262f, 12f, 24f)
                box(Role.DARK, -2, 580f, 254f, 12f, 24f)
            },
            part("exhaust", "Exhaust stack", z = 44) {
                exhaust(300f, 250f, 0f, 16f)
                box(Role.DARK, 0, 286f, 246f, 30f, 60f)
            },
            part("mg", "Hotchkiss MG", z = 52) { machineGun(300f, 330f, 66f) },
        ),
        decoys = 0,
        lore = MarkIVLore.lore,
    )

    fun renaultFT() = VehicleDef(
        id = "renault_ft",
        name = "Renault FT",
        era = Era.WWI,
        branch = Branch.GROUND,
        country = "France",
        year = 1918,
        fact = "The first tank with a full 360-degree turret over a rear engine - the layout "
            + "every tank since has copied.",
        palette = FRENCH,
        // Hull 4.10 m, drawn 404 units; tracks on the ground at y 476.
        model = ModelSource("models/renault_ft.glb", scale = 98.5f, originX = 474f,
                            originY = -476f,
                            internals = "models/renault_ft_int.glb"),
        parts = listOf(
            part("hull", "Hull tub", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, 300f, 336f, 620f, 330f, 676f, 392f, 668f, 470f, 300f, 476f, 272f, 404f)
                // Riveted throughout, like every tank of 1918.
                rivets(310f, 344f, 616f, 338f, 11, 108f, r = 4.5f)
                rivets(310f, 462f, 640f, 462f, 11, 108f, r = 4.5f)
                towHook(648f, 440f, 60f)
            },
            part("track", "Track frame", z = 30) {
                beltLoop(
                    26f,
                    300f, 330f, 620f, 314f, 690f, 286f, 744f, 330f, 762f, 400f,
                    722f, 470f, 640f, 496f, 330f, 496f, 262f, 468f, 250f, 392f,
                )
                trackLinks(
                    TRACK_INNER, TRACK_OUTER, linkLength = 30f,
                    313f, 344f, 616f, 328f, 676f, 302f, 722f, 340f, 738f, 400f,
                    702f, 458f, 634f, 480f, 336f, 480f, 280f, 456f, 268f, 396f,
                )
            },
            part("wheels", "Idler and sprocket", z = 25) {
                ngon(Role.METAL, 0, 698f, 398f, 60f, sides = 12)
                ngon(Role.DARK, 1, 698f, 398f, 22f, sides = 6)
                ngon(Role.METAL, -1, 308f, 448f, 32f, sides = 9)
                for (i in 0 until 5) ngon(Role.METAL, -1, 376f + i * 56f, 466f, 21f, sides = 8)
            },
            part("turret", "Omnibus turret", z = 40, depth = 86f) {
                slab(Role.BODY, 1, 396f, 330f, 528f, 330f, 546f, 268f, 508f, 238f, 424f, 238f, 384f, 270f)
            },
            part("dome", "Cupola dome", z = 46, depth = 36f) {
                ngon(Role.BODY, 2, 466f, 238f, 34f, sides = 8)
                box(Role.DARK, -2, 450f, 204f, 32f, 8f)
            },
            part("gun", "37 mm Puteaux", z = 50) {
                mainGun(546f, 292f, 130f, 9f, mantlet = true)
            },
            part("tail", "Trench tail", z = 18, depth = 26f) {
                slab(Role.TRIM, -1, 250f, 408f, 276f, 400f, 186f, 486f, 160f, 474f)
                box(Role.DARK, -2, 150f, 468f, 34f, 22f)
            },
            part("deck", "Engine deck", z = 42, depth = 110f) {
                slab(Role.TRIM, 1, 300f, 336f, 396f, 332f, 396f, 306f, 306f, 310f)
                box(Role.DARK, -2, 322f, 314f, 56f, 8f)
            },
        ),
        decoys = 1,
        lore = RenaultFTLore.lore,
    )

    // -----------------------------------------------------------------------
    // Interwar
    // -----------------------------------------------------------------------

    fun vickersMedium() = VehicleDef(
        id = "vickers_medium",
        name = "Vickers Medium Mk II",
        era = Era.INTERWAR,
        branch = Branch.GROUND,
        country = "United Kingdom",
        year = 1925,
        fact = "The first tank fast enough to manoeuvre rather than just crawl - 30 km/h, "
            + "and the testbed for Britain's whole interwar armoured doctrine.",
        palette = BRITISH,
        // 5.33 m long, drawn 648 units; ground at y 448.
        model = ModelSource("models/vickers_medium.glb", scale = 121.6f, originX = 520f,
                            originY = -448f,
                            internals = "models/vickers_medium_int.glb"),
        parts = listOf(
            part("hull", "Hull", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, 196f, 372f, 306f, 340f, 800f, 340f, 844f, 376f, 844f, 424f, 196f, 424f)
                hullDressing(200f, 840f, 344f, 420f, 108f, riveted = true, handles = 2)
            },
            part("track", "Track belt", z = 30) {
                trackBelt(178f, 856f, 392f, 486f, th = 18f)
                trackLinks(
                    TRACK_INNER, TRACK_OUTER, linkLength = 26f,
                    *ovalPath(178f, 856f, 392f, 486f, th = 18f),
                )
            },
            part("wheels", "Bogie wheels", z = 25) {
                roadWheels(178f, 856f, 439f, 22f, 8, driveR = 30f, returnRollers = true, rollerY = 402f)
            },
            part("skirt", "Armour skirt", z = 32, inner = 150f, outer = 160f, mirrored = true) {
                slab(Role.TRIM, -1, 240f, 400f, 800f, 400f, 800f, 452f, 240f, 452f)
                for (i in 0 until 5) box(Role.DARK, -3, 268f + i * 110f, 414f, 26f, 26f)
            },
            part("super", "Superstructure", z = 34, depth = 108f) {
                slab(Role.BODY, 1, 300f, 340f, 760f, 340f, 760f, 296f, 320f, 296f)
                box(Role.DARK, -2, 344f, 308f, 40f, 20f)
                rivets(320f, 302f, 752f, 302f, 14, 108f, r = 4.2f)
                rivets(320f, 334f, 752f, 334f, 14, 108f, r = 4.2f)
                visionPort(700f, 306f, 28f, 14f, 104f, mirrored = true)
                grabHandle(420f, 312f, 38f, 108f)
            },
            part("turret", "Cylindrical turret", z = 40, depth = 86f) {
                slab(Role.BODY, 1, 420f, 298f, 660f, 298f, 664f, 216f, 424f, 216f)
                box(Role.DARK, -3, 596f, 236f, 30f, 26f)
            },
            part("gun", "3-pdr gun", z = 50) { mainGun(664f, 252f, 172f, 9f) },
            part("cupola", "Cupola", z = 56) { cupola(470f, 216f, 74f, 34f) },
            part("mg", "Vickers MG", z = 52) { machineGun(424f, 262f, 62f) },
            part("stow", "Stowage bin", z = 36) { stowage(206f, 348f, 96f, 44f) },
        ),
        decoys = 1,
        lore = VickersMediumLore.lore,
    )

    // -----------------------------------------------------------------------
    // WWII
    // -----------------------------------------------------------------------

    fun panzerIV() = VehicleDef(
        id = "panzer_iv",
        name = "Panzer IV Ausf. H",
        era = Era.WWII,
        branch = Branch.GROUND,
        country = "Germany",
        year = 1943,
        fact = "The only German tank in production from the first day of the war to the last, "
            + "re-gunned from a short howitzer to the long 75 mm that could kill a T-34.",
        palette = DUNKELGELB,
        parts = listOf(
            part("hull", "Lower hull", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, 176f, 392f, 844f, 392f, 856f, 424f, 856f, 462f, 170f, 462f)
                weld(190f, 394f, 840f, 394f, 108f)
                rivets(210f, 452f, 820f, 452f, 14, 108f, r = 4f)
                towHook(828f, 430f, 66f)
                towHook(172f, 428f, 66f)
            },
            part("track", "Track belt", z = 30) {
                trackBelt(166f, 862f, 396f, 500f, th = 20f)
                trackLinks(
                    TRACK_INNER, TRACK_OUTER, linkLength = 34f,
                    *ovalPath(166f, 862f, 396f, 500f, th = 20f),
                )
            },
            part("wheels", "Bogie wheels", z = 25) {
                roadWheels(166f, 862f, 448f, 30f, 8, driveR = 36f, returnRollers = true, rollerY = 404f)
            },
            part("upper", "Upper hull", z = 34, depth = 112f) {
                slab(Role.BODY, 1, 196f, 392f, 760f, 392f, 796f, 336f, 232f, 336f)
                slab(Role.BODY, 2, 760f, 392f, 856f, 392f, 838f, 344f, 796f, 336f)
                box(Role.GLASS, 0, 808f, 352f, 26f, 14f)
                weld(232f, 338f, 792f, 338f, 112f)
                weld(760f, 392f, 796f, 338f, 112f)
                rivets(248f, 380f, 744f, 380f, 12, 112f)
                visionPort(804f, 348f, 34f, 16f, 108f, mirrored = true)
                panel(330f, 348f, 120f, 36f, 112f, bolts = 5)
                grabHandle(520f, 352f, 40f, 112f)
                deckHatch(300f, 60f, 336f, 36f)
                deckHatch(300f, -60f, 336f, 36f)
                pioneerTools(250f, 346f, 78f)
            },
            part("skirt", "Schürzen skirt", z = 32, inner = 150f, outer = 160f, mirrored = true) {
                for (i in 0 until 5) slab(Role.TRIM, -1, 224f + i * 122f, 382f, 340f + i * 122f, 382f, 340f + i * 122f, 448f, 224f + i * 122f, 448f)
            },
            part("turret", "Turret", z = 40, depth = 86f) {
                slab(Role.BODY, 1, 388f, 338f, 676f, 338f, 682f, 266f, 396f, 266f)
                slab(Role.TRIM, 0, 380f, 330f, 404f, 330f, 404f, 274f, 380f, 274f)
                weld(396f, 268f, 680f, 268f, 86f)
                rivets(410f, 330f, 664f, 330f, 9, 86f, r = 3.5f)
                visionPort(600f, 288f, 26f, 14f, 86f, mirrored = true)
                grabHandle(440f, 294f, 36f, 86f)
                ventilator(556f, 0f, 266f, 22f)
                deckHatch(628f, 44f, 266f, 30f)
            },
            part("gun", "7.5 cm KwK 40", z = 50) {
                mainGun(682f, 300f, 236f, 10f, brake = true)
            },
            part("cupola", "Commander's cupola", z = 56) { cupola(440f, 266f, 78f, 40f) },
            part("schurz_t", "Turret skirt", z = 42, inner = 92f, outer = 102f, mirrored = true) {
                slab(Role.TRIM, -1, 372f, 282f, 400f, 282f, 400f, 348f, 372f, 348f)
                slab(Role.TRIM, -1, 676f, 276f, 700f, 282f, 700f, 344f, 676f, 340f)
            },
            part("mg", "Bow MG 34", z = 52) { machineGun(820f, 366f, 54f) },
            part("stow", "Stowage bin", z = 44) { stowage(300f, 272f, 74f, 40f) },
            part("exhaust", "Exhaust muffler", z = 36) { exhaust(180f, 372f, 62f, 14f) },
        ),
        decoys = 2,
        lore = PanzerIVLore.lore,
        // Modelled in blender/panzer_iv.py at full size; 115.6 blueprint units per metre
        // puts its 5.88 m hull exactly where the extruded one sat.
        model = ModelSource("models/panzer_iv.glb", scale = 115.6f, originX = 515f,
                            internals = "models/panzer_iv_int.glb"),
    )

    fun t3485() = VehicleDef(
        id = "t34_85",
        name = "T-34-85",
        era = Era.WWII,
        branch = Branch.GROUND,
        country = "Soviet Union",
        year = 1944,
        fact = "Sloped armour, wide tracks and a diesel engine, built so simply that 84,000 "
            + "were made - more than every German tank type combined.",
        palette = SOVIET,
        // Hull 6.10 m long, and 692 blueprint units from 166 to 858.
        model = ModelSource("models/t34_85.glb", scale = 113.4f, originX = 512f,
                            internals = "models/t34_85_int.glb"),
        parts = listOf(
            part("hull", "Lower hull", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, 172f, 396f, 848f, 396f, 858f, 430f, 858f, 464f, 166f, 464f)
            },
            part("track", "Wide track", z = 30) {
                trackBelt(160f, 868f, 392f, 502f, th = 24f)
                trackLinks(
                    TRACK_INNER, TRACK_OUTER, linkLength = 38f,
                    *ovalPath(160f, 868f, 392f, 502f, th = 24f),
                )
            },
            part("wheels", "Christie wheels", z = 25) {
                roadWheels(160f, 868f, 448f, 46f, 5, driveR = 44f)
            },
            part("upper", "Glacis plate", z = 34, depth = 112f) {
                slab(Role.BODY, 2, 206f, 396f, 724f, 396f, 866f, 396f, 790f, 322f, 244f, 322f, contrast = 3)
                slab(Role.BODY, 1, 790f, 322f, 866f, 396f, 880f, 366f, 824f, 316f)
                box(Role.DARK, -3, 806f, 340f, 30f, 16f)
                // Welded and cast throughout - no rivets anywhere on a T-34.
                hullDressing(206f, 866f, 326f, 392f, 112f, riveted = false, handles = 3)
                weld(244f, 324f, 786f, 324f, 112f)
                visionPort(812f, 344f, 30f, 14f, 108f, mirrored = true)
                deckHatch(780f, 54f, 330f, 40f)
                panel(300f, 330f, 150f, 44f, 112f, bolts = 0)
            },
            part("turret", "Cast turret", z = 40, depth = 86f) {
                slab(Role.BODY, 1, 366f, 324f, 690f, 324f, 662f, 246f, 402f, 246f, contrast = 3)
                slab(Role.BODY, 2, 402f, 246f, 662f, 246f, 640f, 232f, 424f, 232f)
                weld(372f, 320f, 684f, 320f, 86f)
                grabHandle(420f, 284f, 40f, 86f)
                grabHandle(560f, 280f, 40f, 86f)
                visionPort(612f, 268f, 24f, 13f, 86f, mirrored = true)
                ventilator(470f, 0f, 232f, 20f)
                deckHatch(560f, 40f, 232f, 34f)
            },
            part("gun", "85 mm ZiS-S-53", z = 50) {
                mainGun(690f, 282f, 254f, 11f)
            },
            part("cupola", "Cupola", z = 56) { cupola(470f, 232f, 84f, 38f) },
            part("fender", "Fenders", z = 36) { fender(180f, 862f, 384f, th = 14f) },
            part("drums", "Fuel drums", z = 38, inner = 96f, outer = 130f, mirrored = true) {
                tube(Role.TRIM, 0, 186f, 372f, 30f, 262f, 372f, 30f)
                ngon(Role.TRIM, 1, 262f, 372f, 28f, sides = 9)
                box(Role.DARK, -2, 214f, 344f, 8f, 58f)
            },
            part("mg", "Bow DT MG", z = 52) { machineGun(828f, 356f, 52f) },
            part("rail", "Grab rails", z = 46, depth = 84f) {
                for (i in 0 until 4) box(Role.DARK, 0, 420f + i * 62f, 236f, 34f, 6f)
            },
        ),
        decoys = 2,
        lore = T34Lore.lore,
    )

    // -----------------------------------------------------------------------
    // Cold War
    // -----------------------------------------------------------------------

    fun centurion() = VehicleDef(
        id = "centurion",
        name = "Centurion Mk 5",
        era = Era.COLD_WAR,
        branch = Branch.GROUND,
        country = "United Kingdom",
        year = 1951,
        fact = "Designed in the last months of WWII and still fighting in the 1970s - the "
            + "tank that invented the idea of a single 'main battle tank'.",
        palette = NATO_GREEN,
        // Hull 7.82 m, drawn 722 units; ground at y 466.
        model = ModelSource("models/centurion.glb", scale = 92.3f, originX = 511f,
                            originY = -466f,
                            internals = "models/centurion_int.glb"),
        parts = listOf(
            part("hull", "Lower hull", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, 156f, 392f, 862f, 392f, 872f, 428f, 872f, 466f, 150f, 466f)
            },
            part("track", "Track belt", z = 30) {
                trackBelt(146f, 880f, 388f, 498f, th = 22f)
                trackLinks(
                    TRACK_INNER, TRACK_OUTER, linkLength = 36f,
                    *ovalPath(146f, 880f, 388f, 498f, th = 22f),
                )
            },
            part("wheels", "Road wheels", z = 25) {
                roadWheels(146f, 880f, 443f, 34f, 6, driveR = 38f, returnRollers = true, rollerY = 396f)
            },
            part("upper", "Upper hull", z = 34, depth = 112f) {
                slab(Role.BODY, 1, 178f, 392f, 800f, 392f, 862f, 340f, 232f, 330f, contrast = 3)
                box(Role.GLASS, 0, 818f, 352f, 26f, 14f)
                hullDressing(190f, 850f, 340f, 390f, 112f, riveted = false, handles = 3)
                visionPort(816f, 348f, 30f, 14f, 108f, mirrored = true)
                deckHatch(760f, 52f, 344f, 40f)
                panel(320f, 348f, 160f, 38f, 112f, bolts = 5)
            },
            part("skirt", "Side skirt", z = 32, inner = 150f, outer = 160f, mirrored = true) {
                slab(Role.TRIM, -1, 180f, 378f, 856f, 378f, 856f, 440f, 180f, 440f)
                for (i in 0 until 6) box(Role.DARK, -3, 210f + i * 110f, 392f, 24f, 34f)
            },
            part("turret", "Turret", z = 40, depth = 86f) {
                slab(Role.BODY, 1, 340f, 336f, 706f, 332f, 690f, 252f, 388f, 248f, contrast = 3)
                slab(Role.TRIM, 0, 318f, 330f, 344f, 336f, 344f, 262f, 318f, 266f)
            },
            part("gun", "20-pdr gun", z = 50) {
                mainGun(706f, 288f, 268f, 11f, extractor = true)
            },
            part("cupola", "Cupola", z = 56) { cupola(452f, 248f, 86f, 40f) },
            part("basket", "Stowage basket", z = 38, depth = 74f) {
                slab(Role.TRIM, -1, 232f, 262f, 328f, 258f, 328f, 322f, 232f, 326f)
                for (i in 0 until 4) box(Role.DARK, -3, 244f + i * 22f, 268f, 6f, 48f)
            },
            part("mg", "Coax MG", z = 52) { machineGun(690f, 268f, 64f) },
            part("antenna", "Radio antenna", z = 58) { antenna(372f, 248f, 128f) },
            part("lights", "Head lamps", z = 44, inner = 88f, outer = 98f, mirrored = true) {
                ngon(Role.GLASS, 2, 866f, 322f, 16f, sides = 8)
                box(Role.DARK, -2, 850f, 318f, 12f, 26f)
            },
        ),
        decoys = 3,
        lore = CenturionLore.lore,
    )

    // -----------------------------------------------------------------------
    // Modern
    // -----------------------------------------------------------------------

    fun abrams() = VehicleDef(
        id = "m1a2_abrams",
        name = "M1A2 Abrams",
        era = Era.MODERN,
        branch = Branch.GROUND,
        country = "United States",
        year = 1992,
        fact = "A 1,500 hp gas turbine, depleted-uranium composite armour, and a 120 mm "
            + "smoothbore stabilised well enough to hit while moving at 40 km/h.",
        palette = DESERT,
        // Hull 7.93 m, drawn 752 units; ground at y 462.
        model = ModelSource("models/m1a2_abrams.glb", scale = 94.8f, originX = 510f,
                            originY = -462f,
                            internals = "models/m1a2_abrams_int.glb"),
        parts = listOf(
            part("hull", "Lower hull", z = 20, pre = true, depth = 108f) {
                slab(Role.BODY, 0, 140f, 396f, 874f, 396f, 886f, 430f, 886f, 462f, 134f, 462f)
            },
            part("track", "Track belt", z = 30) {
                trackBelt(130f, 892f, 392f, 500f, th = 24f)
                trackLinks(
                    TRACK_INNER, TRACK_OUTER, linkLength = 40f,
                    *ovalPath(130f, 892f, 392f, 500f, th = 24f),
                )
            },
            part("wheels", "Road wheels", z = 25) {
                roadWheels(130f, 892f, 446f, 32f, 7, driveR = 38f, returnRollers = true, rollerY = 400f)
            },
            part("upper", "Hull deck", z = 34, depth = 112f) {
                slab(Role.BODY, 1, 166f, 396f, 780f, 396f, 886f, 396f, 800f, 350f, 210f, 344f, contrast = 3)
                box(Role.DARK, -3, 828f, 364f, 40f, 12f)
                // Bolted composite panels; nothing riveted on a tank of this generation.
                hullDressing(180f, 870f, 352f, 392f, 112f, riveted = false, handles = 3)
                panel(280f, 356f, 180f, 34f, 112f, bolts = 6)
                panel(480f, 356f, 180f, 34f, 112f, bolts = 6)
                deckHatch(820f, 0f, 352f, 42f)
                visionPort(828f, 356f, 34f, 12f, 108f, mirrored = false)
            },
            part("skirt", "Ballistic skirt", z = 32, inner = 150f, outer = 160f, mirrored = true) {
                slab(Role.TRIM, -1, 158f, 380f, 878f, 380f, 878f, 444f, 158f, 444f)
                for (i in 0 until 7) box(Role.DARK, -3, 186f + i * 100f, 392f, 20f, 40f)
            },
            part("turret", "Composite turret", z = 40, depth = 86f) {
                slab(Role.BODY, 1, 292f, 348f, 700f, 344f, 700f, 268f, 404f, 258f, 292f, 272f, contrast = 3)
                slab(Role.BODY, 2, 404f, 258f, 700f, 268f, 690f, 252f, 430f, 244f)
                panel(430f, 286f, 200f, 50f, 86f, bolts = 7)
                grabHandle(330f, 302f, 38f, 86f)
                deckHatch(360f, 46f, 262f, 38f)
                ventilator(400f, -40f, 256f, 18f)
            },
            part("gun", "120 mm M256", z = 50) {
                mainGun(700f, 300f, 286f, 12f, extractor = true)
            },
            part("bustle", "Turret bustle", z = 38, depth = 78f) {
                slab(Role.TRIM, -1, 214f, 282f, 296f, 276f, 296f, 344f, 214f, 344f)
                for (i in 0 until 3) box(Role.DARK, -3, 224f + i * 24f, 290f, 8f, 46f)
            },
            part("citv", "CITV sight", z = 56, depth = 28f) {
                slab(Role.BODY, 2, 470f, 244f, 544f, 244f, 544f, 206f, 470f, 206f)
                box(Role.GLASS, 2, 528f, 214f, 18f, 20f)
            },
            part("cwsmg", "Commander's .50 cal", z = 58) { machineGun(566f, 232f, 86f) },
            part("smoke", "Smoke launchers", z = 46, inner = 60f, outer = 84f, mirrored = true) {
                for (i in 0 until 4) box(Role.DARK, 0, 634f + i * 17f, 250f, 12f, 26f)
            },
            part("gps", "Gunner's sight", z = 54, depth = 30f) {
                slab(Role.BODY, 2, 620f, 268f, 686f, 268f, 686f, 240f, 620f, 240f)
                box(Role.GLASS, 2, 668f, 246f, 16f, 16f)
            },
            part("skirt_r", "Rear plate", z = 36, depth = 104f) {
                slab(Role.TRIM, 0, 134f, 396f, 172f, 396f, 172f, 344f, 140f, 350f)
                ngon(Role.DARK, -2, 152f, 368f, 16f, sides = 7)
            },
        ),
        decoys = 3,
        lore = AbramsLore.lore,
    )

    fun all(): List<VehicleDef> = listOf(
        markIV(), renaultFT(), vickersMedium(), panzerIV(), t3485(), centurion(), abrams(),
    )
}
