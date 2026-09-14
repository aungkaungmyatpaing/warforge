package com.maddog.warforge

import com.maddog.warforge.data.Catalog
import com.maddog.warforge.solid.GlbLoader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Reads the exported Blender models straight off disk.
 *
 * The loader itself has no Android dependencies, so this runs on the JVM and catches the
 * things that actually go wrong between Blender and the game: a part renamed on one side
 * only, a model that lands in the wrong place, a mesh that came through empty.
 */
class GlbLoaderTest {

    private val models = File("src/main/assets/models")

    private fun model(name: String): File = File(models, name)

    /**
     * Every vehicle that has been migrated to Blender, so a new one is covered the
     * moment it is wired into the catalogue rather than when somebody remembers to add
     * it here. [Catalog] is the single list of what exists.
     */
    private fun modelled(): List<Pair<com.maddog.warforge.data.VehicleDef, File>> =
        Catalog.campaign.mapNotNull { v ->
            v.model?.let { File("src/main/assets/${it.asset}") }
                ?.takeIf { it.exists() }
                ?.let { v to it }
        }

    @Test
    fun `the panzer model parses into named parts`() {
        val file = model("panzer_iv.glb")
        assumeTrue("no exported model yet", file.exists())

        val meshes = file.inputStream().use { GlbLoader.load(it) }
        assertTrue("no parts in the model", meshes.isNotEmpty())
        for (id in listOf("hull", "upper", "turret", "gun", "wheels", "track", "skirt")) {
            assertTrue("model is missing '$id' (got ${meshes.keys})", id in meshes)
        }
        for ((name, mesh) in meshes) {
            assertTrue("$name came through empty", mesh.triangleCount > 0)
        }
    }

    /**
     * Every part named in a model must exist in the catalogue, or the scene silently
     * drops it and the vehicle comes out missing pieces.
     */
    @Test
    fun `model part names match the catalogue`() {
        val checked = modelled()
        assumeTrue("no exported models yet", checked.isNotEmpty())
        for ((vehicle, file) in checked) {
            val known = vehicle.parts.map { it.id }.toSet()
            val meshes = file.inputStream().use { GlbLoader.load(it) }
            val stray = meshes.keys - known
            assertTrue("${vehicle.id}: parts the catalogue does not know: $stray", stray.isEmpty())
            for ((name, mesh) in meshes) {
                assertTrue("${vehicle.id}: $name came through empty", mesh.triangleCount > 0)
            }
        }
    }

    /**
     * A modelled vehicle has to land where the procedural one did, because the internal
     * modules are still placed in blueprint coordinates and have to end up inside it.
     *
     * The pre-placed part is the one to check - a tank's lower hull, an aeroplane's
     * fuselage, a ship's hull. It is the body everything else is measured from, and a
     * wrong scale or an origin in the wrong place shows up there first.
     */
    @Test
    fun `every model lands on its blueprint`() {
        val checked = modelled()
        assumeTrue("no exported models yet", checked.isNotEmpty())
        for ((vehicle, file) in checked) {
            val base = vehicle.parts.firstOrNull { it.preplaced } ?: continue
            val blueprint = base.mesh
            val meshes = file.inputStream().use { GlbLoader.load(it, vehicle.model!!::place) }
            val body = meshes[base.id] ?: continue
            val tolerance = (blueprint.maxX - blueprint.minX) * 0.12f
            assertEquals("${vehicle.id} ${base.id} front", blueprint.maxX, body.maxX, tolerance)
            assertEquals("${vehicle.id} ${base.id} back", blueprint.minX, body.minX, tolerance)
            assertEquals(
                "${vehicle.id} ${base.id} height",
                (blueprint.minY + blueprint.maxY) * 0.5f,
                (body.minY + body.maxY) * 0.5f,
                tolerance,
            )
            assertEquals("${vehicle.id} is off centre", 0f, (body.minZ + body.maxZ) * 0.5f, 12f)
        }
    }

    /**
     * The X-ray models name their objects for *module* ids. A typo there is silent - the
     * app just falls back to the procedural box and the modelled version never appears -
     * so both directions are checked: nothing unknown in the file, and nothing in the
     * catalogue left behind.
     */
    @Test
    fun `internal model names match the modules`() {
        val checked = Catalog.campaign.mapNotNull { v ->
            v.model?.internals?.let { File("src/main/assets/$it") }?.takeIf { it.exists() }
                ?.let { v to it }
        }
        assumeTrue("no exported internals yet", checked.isNotEmpty())
        for ((vehicle, file) in checked) {
            val known = vehicle.modules.map { it.id }.toSet()
            val meshes = file.inputStream().use { GlbLoader.load(it) }
            val stray = meshes.keys - known
            assertTrue("${vehicle.id}: modelled internals the catalogue does not know: $stray",
                stray.isEmpty())
            val missing = known - meshes.keys
            assertTrue("${vehicle.id}: modules still falling back to boxes: $missing",
                missing.isEmpty())
            for ((name, mesh) in meshes) {
                assertTrue("${vehicle.id}: $name came through empty", mesh.triangleCount > 0)
            }
        }
    }

    @Test
    fun `every model is a sane size for a phone`() {
        val checked = modelled()
        assumeTrue("no exported models yet", checked.isNotEmpty())
        for ((vehicle, file) in checked) {
            val meshes = file.inputStream().use { GlbLoader.load(it) }
            val triangles = meshes.values.sumOf { it.triangleCount }
            assertTrue("${vehicle.id}: $triangles triangles is too many", triangles < 80_000)
            assertTrue("${vehicle.id}: only $triangles triangles", triangles > 3_000)
        }
    }

    /**
     * The model is authored in metres and the game works in blueprint units. If the
     * transform is wrong the hull ends up the wrong size or somewhere else entirely, and
     * the internal modules - still placed in blueprint coordinates - miss it.
     */
    @Test
    fun `the transform lands the model on the blueprint`() {
        val file = model("panzer_iv.glb")
        assumeTrue("no exported model yet", file.exists())

        val vehicle = com.maddog.warforge.data.Catalog.byId("panzer_iv")!!
        val source = vehicle.model!!
        val meshes = file.inputStream().use { GlbLoader.load(it, source::place) }
        val hull = meshes["hull"]!!

        // The catalogue's lower hull runs from x 176 to 856, with its floor at y 462 -
        // world y 462 negated. Allow a little slack for the bevels.
        assertEquals("hull nose", 856f, hull.maxX, 45f)
        assertEquals("hull tail", 176f, hull.minX, 45f)
        assertEquals("hull floor", -462f, hull.minY, 45f)
        assertEquals("hull roof", -392f, hull.maxY, 45f)

        // Symmetric about the centreline, and wide enough to hold the internals.
        assertEquals("hull is off centre", 0f, (hull.minZ + hull.maxZ) * 0.5f, 8f)
        assertTrue("hull is too narrow for its modules", hull.maxZ > 100f)
    }

    /**
     * The weathering pass is the difference between a model and a toy, and it lives
     * entirely in a vertex colour that nothing else would miss if it stopped arriving -
     * the vehicle would just quietly go back to being one flat colour.
     */
    @Test
    fun `painted parts carry baked surface variation`() {
        val file = model("panzer_iv.glb")
        assumeTrue("no exported model yet", file.exists())

        val meshes = file.inputStream().use { GlbLoader.load(it) }
        for (id in listOf("hull", "upper", "turret", "skirt", "wheels", "track")) {
            assertTrue("$id lost its vertex colours", meshes[id]!!.hasTints)
        }

        val hull = meshes["hull"]!!
        val tint = FloatArray(3)
        var varied = 0
        for (v in 0 until hull.triangleCount * 3) {
            hull.tintAt(v, tint)
            if (tint[0] < 0.92f || tint[1] < 0.92f || tint[2] < 0.92f) varied++
        }
        val fraction = varied.toFloat() / (hull.triangleCount * 3)
        assertTrue("only ${(fraction * 100).toInt()}% of the hull varies", fraction > 0.25f)
        assertTrue("the whole hull is tinted - the base colour has gone", fraction < 0.98f)

        // Markings are a multiplier above one, which is how white survives a dark palette.
        val skirt = meshes["skirt"]!!
        var white = 0
        for (v in 0 until skirt.triangleCount * 3) {
            skirt.tintAt(v, tint)
            if (tint[2] > 1.5f) white++
        }
        assertTrue("no Balkenkreuz on the skirts", white > 24)
    }

}
