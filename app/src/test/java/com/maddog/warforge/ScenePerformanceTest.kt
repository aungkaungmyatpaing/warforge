package com.maddog.warforge

import com.maddog.warforge.data.Catalog
import com.maddog.warforge.solid.AmbientOcclusion
import com.maddog.warforge.solid.GlbLoader
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File

/**
 * Guards how long a modelled vehicle takes to become drawable.
 *
 * This is the one wait the player actually sits through - the museum shows "Building
 * model..." until it finishes - and it is easy to make it much worse without noticing,
 * because on a desktop JVM even the slow version finishes in under a second. The two
 * ways it has gone wrong so far are a loader that allocated three floats per corner of
 * every triangle, and occluding a Blender mesh at run time when Blender already baked
 * it. Both are caught by the budget below rather than by anyone timing it by hand.
 */
class ScenePerformanceTest {

    private fun millis(block: () -> Unit): Long {
        val started = System.nanoTime()
        block()
        return (System.nanoTime() - started) / 1_000_000
    }

    @Test
    fun `building the panzer stays within budget`() {
        val file = File("src/main/assets/models/panzer_iv.glb")
        assumeTrue("no exported model yet", file.exists())

        val vehicle = Catalog.byId("panzer_iv")!!
        val source = vehicle.model!!

        // Once through first, so the measurement is not paying for class loading.
        file.inputStream().use { GlbLoader.load(it, source::place) }

        var model = emptyMap<String, com.maddog.warforge.solid.Mesh>()
        val load = millis { model = file.inputStream().use { GlbLoader.load(it, source::place) } }

        val parts = vehicle.parts.map { model[it.id] ?: it.mesh }.filter { !it.isEmpty }
        val modules = vehicle.modules.filter { !it.mesh.isEmpty }.map { it.mesh }
        val meshes = parts + modules

        var occlusion = emptyList<FloatArray>()
        val ao = millis { occlusion = AmbientOcclusion.bake(meshes) }
        val upload = millis {
            for ((i, m) in meshes.withIndex()) m.toVertexArray(vehicle.palette, ao = occlusion[i])
        }

        println("load ${load}ms  ao ${ao}ms  upload ${upload}ms")

        // Generous: a phone is several times slower than this, and the point is to catch
        // a change that costs an order of magnitude, not to police milliseconds.
        assertTrue("loading took ${load}ms", load < 1500)
        assertTrue("occlusion took ${ao}ms", ao < 1500)
        assertTrue("vertex arrays took ${upload}ms", upload < 1500)
    }

    /**
     * The parts that came from Blender must not be occluded again at run time. Sampling
     * them is most of the cost of opening a vehicle, and buys nothing - the model
     * already carries better occlusion than the voxel grid can produce.
     */
    @Test
    fun `blender parts are not occluded twice`() {
        val modelled = Catalog.campaign.mapNotNull { v ->
            v.model?.let { File("src/main/assets/${it.asset}") }?.takeIf { it.exists() }
                ?.let { v.id to it }
        }
        assumeTrue("no exported models yet", modelled.isNotEmpty())
        for ((id, file) in modelled) {
            val meshes = file.inputStream().use { GlbLoader.load(it) }.values.toList()
            val occlusion = AmbientOcclusion.bake(meshes)
            for ((i, mesh) in meshes.withIndex()) {
                assertTrue("$id: a model part arrived without baked tints", mesh.hasTints)
                assertTrue(
                    "$id: part $i was occluded again at run time",
                    occlusion[i].all { it == 1f },
                )
            }
        }
    }
}
