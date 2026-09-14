package com.maddog.warforge.data.lore

import com.maddog.warforge.data.Lore
import com.maddog.warforge.data.ModuleKind
import com.maddog.warforge.data.Question
import com.maddog.warforge.data.Spec
import com.maddog.warforge.data.crewman
import com.maddog.warforge.data.fuelCell
import com.maddog.warforge.data.module
import com.maddog.warforge.poly.Role
import com.maddog.warforge.solid.box3
import com.maddog.warforge.solid.cylinderZ
import com.maddog.warforge.solid.revolveX

/**
 * Fokker Dr.I, opened up.
 *
 * Three short wings on a steel-tube fuselage, which is a very different structural idea
 * from the Camel's wooden box - and the reason the Dr.I needed almost no bracing wire.
 * Centreline near y = 302.
 */
object FokkerDr1Lore {

    private val modules = listOf(
        module(
            "engine", "Oberursel Ur.II rotary", ModuleKind.ENGINE,
            detail = "15.1 L nine-cylinder rotary · 110 hp",
            info = "A direct copy of the French Le Rhône, built under licence before the war "
                + "and then without it. Less powerful than the Clerget in a Camel, but the "
                + "Dr.I is lighter, so it climbs better and turns tighter - and loses badly "
                + "in a straight line.",
        ) {
            revolveX(Role.BODY, 0, 16, 700f, 302f, 44f, 744f, 302f, 48f)
            for (i in 0 until 7) {
                val a = (2.0 * Math.PI * i / 7).toFloat()
                cylinderZ(
                    Role.METAL, -1,
                    724f + 38f * kotlin.math.cos(a), 302f + 38f * kotlin.math.sin(a),
                    11f, -13f, 13f, 10,
                )
            }
        },
        module(
            "structure", "Welded steel tube fuselage", ModuleKind.ARMOUR,
            detail = "Chrome-molybdenum tube",
            info = "Fokker welded his fuselages from steel tube while the Allies were still "
                + "gluing spruce. A welded truss needs no bracing wires to stay true, does "
                + "not warp in the rain, and can be repaired at a squadron with a torch. It "
                + "is the single most modern thing about the aeroplane.",
        ) {
            box3(Role.BODY, 0, 230f, 284f, -18f, 700f, 290f, -14f)
            box3(Role.BODY, 0, 230f, 314f, 14f, 700f, 320f, 18f)
            box3(Role.BODY, 0, 230f, 284f, 14f, 700f, 290f, 18f)
            box3(Role.BODY, 0, 230f, 314f, -18f, 700f, 320f, -14f)
        },
        module(
            "wing_spar", "Cantilever wing box", ModuleKind.ARMOUR,
            detail = "Full-depth plywood box spar",
            info = "The wings carry their own loads through a deep plywood box, so they need "
                + "no flying wires at all - the struts between them are there to stop them "
                + "flexing, not to hold them up. Fokker's rushed production glued some of "
                + "these badly and several Dr.Is shed their upper wings before the fault was "
                + "found.",
        ) {
            box3(Role.BODY, 0, 560f, 162f, -300f, 600f, 176f, 300f)
            box3(Role.BODY, 0, 550f, 240f, -260f, 590f, 254f, 260f)
        },
        module(
            "fuel", "Fuel tank", ModuleKind.FUEL,
            detail = "72 L",
            info = "Between the engine and the pilot, as on every aeroplane of the period. "
                + "An hour and a half of flying, which is all a fighter with this much drag "
                + "was ever going to manage.",
        ) { fuelCell(624f, 278f, -30f, 690f, 326f, 30f) },
        module(
            "pilot", "Pilot", ModuleKind.CREW,
            detail = "One, high in a short fuselage",
            info = "Sits high with the middle wing at eye level - the Dr.I's worst feature, "
                + "since that wing sits exactly where a pilot wants to look for the enemy.",
        ) { crewman(548f, 308f, 0f) },
        module(
            "guns", "Twin Spandau LMG 08/15", ModuleKind.GUN,
            detail = "2 × 7.92 mm, synchronised",
            info = "German synchronising gear was better than the Allied equivalent for most "
                + "of the war, which is why German fighters mounted their guns on the nose "
                + "where the pilot could reach them and aim along them.",
        ) {
            box3(Role.BODY, 0, 626f, 254f, -18f, 782f, 268f, -5f)
            box3(Role.BODY, 0, 626f, 254f, 5f, 782f, 268f, 18f)
        },
        module(
            "ammo", "Ammunition boxes", ModuleKind.AMMO,
            detail = "2 × 500 rounds",
            info = "Under the breeches, feeding up. The pilot clears jams by hand, in the "
                + "air, with the slipstream in his face.",
        ) { box3(Role.BODY, 0, 610f, 270f, -24f, 676f, 298f, 24f) },
    )

    private val history = """
## An answer to the Sopwith Triplane

The Dr.I exists because of a British aeroplane. When Sopwith Triplanes appeared over
Flanders in 1917 and out-climbed everything Germany had, the response was immediate: get
Fokker to build a triplane. Three short wings give a lot of lift and a lot of roll rate in
a very small, light aeroplane.

They also give a great deal of drag. The Dr.I was slow - slower than the Camel it fought -
and slower than the Albatros it replaced. What it could do was climb, turn and roll better
than anything in the sky, which in 1917 was the argument that mattered.

## Richthofen

Manfred von Richthofen scored his last nineteen victories in a Dr.I and was killed in one
on 21 April 1918. The association is why everyone knows the aeroplane, although he flew
Albatros types for most of his career and only about 320 Dr.Is were ever built.

## Welded steel

Open the fuselage and the modern thing about the Dr.I is not the wings. Fokker welded his
fuselages from chrome-molybdenum steel tube while the British were still gluing spruce.
A welded truss holds its shape without rigging wires, does not warp in weather, and can be
repaired at a squadron with a gas torch. Aircraft were still being built this way in the
1950s.

The wings were cantilever too, carrying their loads through a deep plywood box spar, so
the struts between them brace rather than support. Unfortunately the rushed production
glued some of those spars badly, several Dr.Is shed their upper wings in 1917, and the
type was grounded until the fault was traced.

## Its shortcoming

The middle wing sits exactly at the pilot's eye level. In a fighter, whose entire business
is seeing the other aeroplane first, that is a serious thing to have designed in.
""".trimIndent()

    private val specs = listOf(
        Spec("Crew", "1"),
        Spec("Empty weight", "406 kg"),
        Spec("Loaded weight", "586 kg"),
        Spec("Wingspan (upper)", "7.19 m"),
        Spec("Engine", "Oberursel Ur.II, 110 hp"),
        Spec("Top speed", "185 km/h"),
        Spec("Rate of climb", "5.7 m/s"),
        Spec("Service ceiling", "6,100 m"),
        Spec("Armament", "2 × 7.92 mm Spandau"),
        Spec("Built", "320"),
    )

    private val quiz = listOf(
        Question(
            "Why did Germany build a triplane at all?",
            listOf(
                "Three wings are structurally stronger",
                "To answer the Sopwith Triplane, which out-climbed everything Germany had",
                "It needed less wing area for the same lift",
                "Triplanes were cheaper to build",
            ),
            1,
            "The Sopwith Triplane's climb rate over Flanders in 1917 prompted an immediate "
                + "instruction to Fokker. Three short wings give lift and roll rate in a very "
                + "small aeroplane - at the cost of a great deal of drag.",
        ),
        Question(
            "What was structurally advanced about the Dr.I?",
            listOf(
                "A stressed metal skin",
                "A welded steel-tube fuselage and cantilever wings needing no flying wires",
                "Retractable landing gear",
                "A monocoque plywood shell",
            ),
            1,
            "Welded chrome-molybdenum tube, when the Allies were gluing spruce. It holds its "
                + "shape with no rigging, does not warp, and can be repaired with a torch - a "
                + "method still in use in the 1950s.",
        ),
        Question(
            "What is the Dr.I's worst design fault as a fighter?",
            listOf(
                "It was too slow to catch anything",
                "The middle wing sits at the pilot's eye level",
                "The guns could not be reached to clear jams",
                "It had no fuel for a useful patrol",
            ),
            1,
            "A fighter's whole business is seeing the other aeroplane first, and the Dr.I has "
                + "a wing exactly where the pilot wants to look.",
        ),
    )

    val lore = Lore(modules, history, specs, quiz)
}
