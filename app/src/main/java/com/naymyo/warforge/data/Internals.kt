package com.naymyo.warforge.data

import com.naymyo.warforge.poly.Palette
import com.naymyo.warforge.poly.Role
import com.naymyo.warforge.solid.Mesh
import com.naymyo.warforge.solid.box3
import com.naymyo.warforge.solid.cylinderZ
import com.naymyo.warforge.solid.sphere

/**
 * What is inside the vehicle.
 *
 * Modules are the X-ray layer: colour-coded blocks standing for the engine, the
 * transmission, each crew position, the ammunition and the fuel. They are deliberately
 * simple shapes - the point is to show *where* things sit and how close together, which
 * is the thing that decides how a vehicle behaves and how it dies.
 */

/** Colour coding, consistent across every vehicle so the legend only has to be learnt once. */
enum class ModuleKind(val label: String, val palette: Palette) {
    ENGINE("Engine", kindPalette(0xD4562E)),
    TRANSMISSION("Transmission", kindPalette(0x8E6BC4)),
    DRIVE("Final drive", kindPalette(0x7E8F55)),
    CREW("Crew", kindPalette(0x5FB85F)),
    AMMO("Ammunition", kindPalette(0xE0B62E)),
    FUEL("Fuel", kindPalette(0xC97A3A)),
    GUN("Gun and breech", kindPalette(0x9AA3AA)),
    OPTICS("Optics", kindPalette(0xC45FA8)),
    RADIO("Radio", kindPalette(0x3E9BC4)),
    RADIATOR("Cooling", kindPalette(0x4FC3B0)),
    ARMOUR("Armour", kindPalette(0x6E7A84)),
    AVIONICS("Avionics", kindPalette(0x4E7FD4)),
    MAGAZINE("Magazine", kindPalette(0xD8912E)),
    REACTOR("Reactor", kindPalette(0x7FD44E)),
    ESCAPE("Escape system", kindPalette(0xE05FA0)),

    /**
     * Powered systems: turret traverse, undercarriage retraction, steering gear.
     *
     * Worth a colour of its own because whether a thing is moved by a motor or by a man
     * on a handwheel decides how fast it can be aimed, and that decides fights.
     */
    POWER("Powered drive", kindPalette(0x3EC4A6)),

    /**
     * What keeps the crew alive: ventilation, oxygen, scrubbers, fire suppression.
     *
     * Usually left out of cutaways, and usually the thing that actually killed people -
     * a Mark IV's crew were poisoned by their own engine long before anybody shot at them.
     */
    LIFE("Life support", kindPalette(0xB8D44E)),
    ;
}

/**
 * Builds a module's colour ramp from one hue. Module colours are fixed rather than
 * palette-driven: a yellow block means ammunition whether it is in a Renault FT or a
 * Nimitz, which is the whole value of a legend.
 */
private fun kindPalette(base: Long): Palette = Palette.of(
    base, scaleRgb(base, 0.45f), lightenRgb(base, 0.35f), 0x9FD8E8,
    lightenRgb(base, 0.6f), scaleRgb(base, 0.2f),
)

private fun lightenRgb(c: Long, f: Float): Long {
    fun up(v: Long) = (v + (255 - v) * f).toLong().coerceIn(0, 255)
    return (up((c shr 16) and 0xFF) shl 16) or (up((c shr 8) and 0xFF) shl 8) or up(c and 0xFF)
}

private fun scaleRgb(c: Long, f: Float): Long {
    fun down(v: Long) = (v * f).toLong().coerceIn(0, 255)
    return (down((c shr 16) and 0xFF) shl 16) or (down((c shr 8) and 0xFF) shl 8) or down(c and 0xFF)
}

/**
 * One thing inside the vehicle.
 *
 * @param detail the number the player actually wants - horsepower, calibre, how many
 *   rounds, how thick.
 * @param info why it is here and what it means for the vehicle.
 */
class ModuleDef(
    val id: String,
    val name: String,
    val kind: ModuleKind,
    private val meshProvider: () -> Mesh,
    val detail: String = "",
    val info: String = "",
) {
    /** Built on first use, like a part's - the hangar never needs it. */
    val mesh: Mesh by lazy(LazyThreadSafetyMode.PUBLICATION) { meshProvider() }
}

/** Declares one internal module. Blueprint x/y, explicit z; y still points down. */
fun module(
    id: String, name: String, kind: ModuleKind, detail: String = "", info: String = "",
    block: Mesh.() -> Unit,
): ModuleDef = ModuleDef(id, name, kind, { Mesh().apply(block) }, detail, info)

/**
 * A seated crew figure, facing [facing] (+1 toward the nose).
 *
 * Crew are drawn as people rather than boxes on purpose: seeing how tightly five of them
 * are packed around a breech, and how little steel is between them and the outside, is
 * most of what the X-ray view is there to teach.
 */
fun Mesh.crewman(x: Float, y: Float, z: Float, facing: Float = 1f, half: Float = 20f) {
    box3(Role.BODY, 0, x - 15f, y - 46f, z - half, x + 15f, y, z + half)               // torso
    sphere(Role.BODY, 2, x + 2f * facing, y - 60f, z, 14f, 8, 5)                        // head
    box3(Role.BODY, -2, x + 8f * facing, y - 12f, z - half * 0.85f,                     // thighs
        x + 48f * facing, y + 8f, z + half * 0.85f)
    box3(Role.BODY, -3, x + 40f * facing, y + 8f, z - half * 0.8f,                      // shins
        x + 58f * facing, y + 46f, z + half * 0.8f)
    box3(Role.TRIM, -1, x + 4f * facing, y - 40f, z - half * 1.15f,                     // arms
        x + 34f * facing, y - 26f, z + half * 1.15f)
}

/** A stack of shells stood on end - the readiest ammunition in the vehicle. */
fun Mesh.shellRack(
    x0: Float, y: Float, z: Float, count: Int, r: Float = 9f, len: Float = 62f,
    spacing: Float = 22f, halfZ: Float = 10f,
) {
    for (i in 0 until count) {
        cylinderZ(Role.BODY, 0, x0 + i * spacing, y - len * 0.5f, r, z - halfZ, z + halfZ, 8)
        box3(Role.TRIM, 1, x0 + i * spacing - r, y - len, z - halfZ, x0 + i * spacing + r, y - len * 0.5f, z + halfZ)
    }
}

/** A drum or block of fuel. */
fun Mesh.fuelCell(x0: Float, y0: Float, z0: Float, x1: Float, y1: Float, z1: Float) {
    box3(Role.BODY, 0, x0, y0, z0, x1, y1, z1)
    box3(Role.TRIM, 2, x0 + (x1 - x0) * 0.35f, y0 - 8f, z0 + 4f, x0 + (x1 - x0) * 0.55f, y0, z1 - 4f)
}
