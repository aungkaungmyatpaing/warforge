package com.maddog.warforge.data.lore

import com.maddog.warforge.data.Lore
import com.maddog.warforge.data.ModuleKind
import com.maddog.warforge.data.Question
import com.maddog.warforge.data.Spec
import com.maddog.warforge.data.crewman
import com.maddog.warforge.data.fuelCell
import com.maddog.warforge.data.module
import com.maddog.warforge.data.shellRack
import com.maddog.warforge.poly.Role
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.cylinderZ

/**
 * Centurion Mk 5, opened up.
 *
 * The first tank designed with no separate cruiser and infantry roles in mind - and the
 * first with a gun stabiliser worth the name. Hull floor near y = 466, turret roof near
 * y = 248.
 */
object CenturionLore {

    private val modules = listOf(
        module(
            "engine", "Rolls-Royce Meteor", ModuleKind.ENGINE,
            detail = "27 L V12 petrol · 650 hp",
            info = "A Merlin with the supercharger taken off and the reduction gear removed - "
                + "the same engine that powered the Spitfire, detuned for a tank. Britain had "
                + "a mature 27-litre V12 in production and no good tank engine, so it used "
                + "the one it had. The result was the best-powered tank of the late war.",
        ) {
            box3(Role.BODY, 0, 170f, 350f, -84f, 380f, 452f, 84f)
            box3(Role.TRIM, 2, 196f, 338f, -50f, 280f, 350f, 50f)
        },
        module(
            "cooling", "Radiators", ModuleKind.RADIATOR,
            detail = "Twin radiators, rear",
            info = "A 650 hp petrol V12 rejects a great deal of heat, and the rear deck is "
                + "mostly grille because of it - the Centurion's most obvious weak spot from "
                + "the air.",
        ) {
            box3(Role.BODY, 0, 180f, 348f, 86f, 350f, 420f, 106f)
            box3(Role.BODY, 0, 180f, 348f, -106f, 350f, 420f, -86f)
        },
        module(
            "transmission", "Merritt-Brown gearbox", ModuleKind.TRANSMISSION,
            detail = "Five speeds, regenerative steering",
            info = "Regenerative steering feeds the power taken from the inner track back "
                + "into the outer one, so the tank keeps its speed through a turn and can "
                + "pivot on the spot. A genuine British advantage, and a nightmare to build.",
        ) { box3(Role.BODY, 0, 170f, 400f, -74f, 260f, 462f, 74f) },
        module(
            "suspension", "Horstmann suspension", ModuleKind.DRIVE,
            detail = "Three bogies per side, external springs",
            info = "The springs are bolted to the outside of the hull, so a damaged unit can "
                + "be changed in the field in under an hour without opening the tank. "
                + "Torsion bars ride better; Horstmann is repairable, and over a thirty-year "
                + "service life that mattered more.",
        ) {
            for (i in 0 until 3) {
                box3(Role.BODY, 0, 300f + i * 190f, 420f, 92f, 450f + i * 190f, 470f, 118f)
                box3(Role.BODY, 0, 300f + i * 190f, 420f, -118f, 450f + i * 190f, 470f, -92f)
            }
        },
        module(
            "driver", "Driver", ModuleKind.CREW,
            detail = "Front right",
            info = "Alone at the front - the Centurion dropped the hull machine gunner that "
                + "every wartime tank carried, using the space for ammunition instead.",
        ) { crewman(790f, 414f, 46f) },
        module(
            "commander", "Commander", ModuleKind.CREW,
            detail = "Turret right, under the cupola",
            info = "A vision cupola that turns independently of the turret, so he can search "
                + "in one direction while the gun points in another. Sounds obvious; took "
                + "most of the war to become standard.",
        ) { crewman(430f, 330f, 48f) },
        module(
            "gunner", "Gunner", ModuleKind.CREW,
            detail = "Turret right, ahead of the commander",
            info = "Works the first tank gun stabiliser that actually helped. The Centurion "
                + "could fire accurately on the move, which no wartime tank could do.",
        ) { crewman(510f, 330f, 46f) },
        module(
            "loader", "Loader / radio operator", ModuleKind.CREW,
            detail = "Turret left",
            info = "Loads the 20-pounder and works the wireless. A 20-pdr round weighs about "
                + "20 kg, and he has a turret floor that turns with him to stand on.",
        ) { crewman(500f, 330f, -50f, facing = -1f) },
        module(
            "breech", "20-pounder breech", ModuleKind.GUN,
            detail = "84 mm, stabilised",
            info = "The gun that made the Centurion. 84 mm with a high muzzle velocity and, "
                + "from the Mk 3, a two-plane stabiliser - the reason Centurions on the Golan "
                + "Heights could hit while moving and the T-55s facing them could not.",
        ) {
            box3(Role.BODY, 0, 610f, 264f, -32f, 702f, 312f, 32f)
            box3(Role.TRIM, -1, 560f, 270f, -28f, 610f, 306f, 28f)
            cylinderZ(Role.DARK, 1, 702f, 288f, 17f, -20f, 20f, 12)
        },
        module(
            "ammo_hull", "Hull ammunition", ModuleKind.AMMO,
            detail = "65 rounds",
            info = "Most of it low in the hull beside the driver, in the space a bow gunner "
                + "would have occupied. Low ammunition is ammunition a side hit has to get "
                + "through the running gear to reach.",
        ) {
            shellRack(420f, 460f, 62f, 8, r = 10f, len = 62f, spacing = 24f, halfZ = 16f)
            shellRack(420f, 460f, -62f, 8, r = 10f, len = 62f, spacing = 24f, halfZ = 16f)
        },
        module(
            "fuel", "Fuel tanks", ModuleKind.FUEL,
            detail = "458 L internal",
            info = "A Meteor drinks. Even with this much aboard the Centurion's road range "
                + "was around 100 km, and British crews routinely towed an extra fuel trailer "
                + "behind them - a habit that lasted until the diesel conversions.",
        ) {
            fuelCell(260f, 400f, -80f, 400f, 460f, -24f)
            fuelCell(260f, 400f, 24f, 400f, 460f, 80f)
        },
        module(
            "optics", "Gunner's sight and stabiliser", ModuleKind.OPTICS,
            detail = "Two-plane stabilisation",
            info = "Gyros hold the gun on target while the hull pitches under it. Crude by "
                + "modern standards and revolutionary in 1950.",
        ) { box3(Role.BODY, 0, 620f, 268f, -52f, 700f, 286f, -36f) },
        module(
            "armour_front", "Glacis", ModuleKind.ARMOUR,
            detail = "76 mm at 57 degrees",
            info = "Sloped, in the lesson the T-34 taught everybody. Later marks went to "
                + "152 mm on the nose, because by then the guns facing it had doubled in "
                + "power.",
        ) { box3(Role.BODY, 0, 840f, 336f, -108f, 876f, 400f, 108f) },
        module(
            "turret_drive", "Electric turret traverse", ModuleKind.POWER,
            detail = "Metadyne electric · 360 degrees in 20 s",
            info = "Electric rather than hydraulic, so there is no high-pressure fluid to "
                + "catch fire in the turret when it is hit - a lesson the British took "
                + "directly from burnt-out Shermans.",
        ) {
            box3(Role.BODY, 0, 440f, 352f, -44f, 512f, 398f, 44f)
            cylinderZ(Role.TRIM, 1, 476f, 350f, 28f, -34f, 34f, 12)
        },
        module(
            "radio", "Larkspur C42 set", ModuleKind.RADIO,
            detail = "Turret bustle",
            info = "In the bustle behind the loader, where the crew can reach it and a "
                + "frontal hit cannot. Radio stowage moved to the back of the turret on "
                + "almost every tank after 1945 for exactly that reason.",
        ) {
            box3(Role.BODY, 0, 300f, 322f, -58f, 372f, 372f, 58f)
            box3(Role.TRIM, 2, 372f, 332f, -40f, 380f, 362f, 40f)
        },
        module(
            "fire_suppression", "Fire bottles", ModuleKind.LIFE,
            detail = "Methyl bromide · engine bay",
            info = "Two bottles plumbed into the engine bay, fired by a handle in the "
                + "fighting compartment. A tank fire is usually fuel or hydraulic fluid "
                + "rather than ammunition, and both can be put out if somebody is quick.",
        ) {
            cylinderZ(Role.BODY, 0, 248f, 400f, 20f, -60f, 60f, 12)
            cylinderZ(Role.BODY, 0, 284f, 400f, 20f, -60f, 60f, 12)
        },
    )

    private val history = """
## One tank instead of two

Britain spent the Second World War building two families of tanks: slow, thick infantry
tanks and fast, thin cruisers, neither of which could do the other's job. The Centurion,
designed in 1943 and delivered six weeks after the war in Europe ended, was the admission
that this had been a mistake. It was fast enough to manoeuvre and armoured enough to
fight, and it is where the phrase "main battle tank" begins.

## The Merlin in the hull

Britain had no good tank engine and an excellent aero engine in mass production. Rolls-
Royce stripped the supercharger and reduction gear off a Merlin, called it the Meteor, and
dropped 650 hp into a 51-tonne tank. Nothing else in 1945 came close, and the same engine
was still pushing Centurions around in the 1970s.

## Firing on the move

The Centurion's real advantage was less visible. From the Mk 3 it carried a two-plane
stabiliser that held the 20-pounder on target while the hull pitched underneath it. On the
Golan Heights in 1967 and 1973, Israeli Centurions engaged Syrian T-55s while moving and
hit; the T-55s had to stop to shoot. That difference decided several actions outright.

## Reading the layout

Open the hull and two things stand out. The suspension springs are on the outside, bolted
to the hull side, so a damaged bogie can be swapped in the field without opening the tank -
less comfortable than torsion bars, far easier to keep running.

And there is no hull machine gunner. The Centurion put ammunition in the space every
wartime tank wasted on a fifth crewman with a rifle-calibre gun, and reduced the crew to
four. Every tank since has done the same.

## A very long life

Centurions fought in Korea, at Suez, in Vietnam with the Australians, in every Arab-
Israeli war from 1967 onward, and in Iraq in 1991 as engineering vehicles. Some are still
in service, rebuilt beyond recognition, seventy years after the first one was delivered.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "4"),
        Spec("Combat weight", "51 t"),
        Spec("Length (hull)", "7.60 m"),
        Spec("Height", "3.01 m"),
        Spec("Engine", "Rolls-Royce Meteor, 650 hp"),
        Spec("Power to weight", "12.7 hp/t"),
        Spec("Road speed", "34 km/h"),
        Spec("Range (road)", "100 km"),
        Spec("Main gun", "20-pdr (84 mm), stabilised"),
        Spec("Ammunition", "65 rounds"),
        Spec("Armour, glacis", "76 mm at 57°"),
        Spec("Built", "4,423"),
    )

    private val quiz = listOf(
        Question(
            "Where did the Centurion's engine come from?",
            listOf(
                "A purpose-built tank diesel",
                "A Spitfire's Merlin, stripped of its supercharger",
                "An American radial aero engine",
                "Two bus engines coupled together",
            ),
            1,
            "The Rolls-Royce Meteor is a Merlin with the supercharger and reduction gear "
                + "removed. Britain had no good tank engine and a superb aero engine already "
                + "in mass production, so it used the one it had.",
        ),
        Question(
            "What let Israeli Centurions beat T-55s that outnumbered them?",
            listOf(
                "Thicker armour",
                "A two-plane gun stabiliser, so they could hit while moving",
                "A faster reverse gear",
                "Night vision equipment",
            ),
            1,
            "The T-55 had to stop to shoot accurately. A stabilised Centurion did not, and on "
                + "the Golan Heights that difference decided several actions outright.",
        ),
        Question(
            "Why is the Centurion's suspension bolted to the outside of the hull?",
            listOf(
                "To save internal space for ammunition",
                "So a damaged bogie can be changed in the field without opening the tank",
                "Because torsion bars had not been invented",
                "To improve the ride over rough ground",
            ),
            1,
            "Horstmann bogies ride worse than torsion bars but can be swapped in under an "
                + "hour by a crew with spanners. Over a thirty-year service life that was "
                + "worth more than comfort.",
        ),
        Question(
            "Why does the Centurion have four crew rather than five?",
            listOf(
                "The turret was too small for three",
                "It dropped the hull machine gunner and used the space for ammunition",
                "The commander also drove",
                "Automatic loading",
            ),
            1,
            "A fifth man firing a rifle-calibre gun through the glacis earned his space on no "
                + "wartime tank. The Centurion put rounds there instead, and every tank since "
                + "has followed.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
