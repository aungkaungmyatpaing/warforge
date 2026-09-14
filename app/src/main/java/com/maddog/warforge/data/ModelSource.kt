package com.maddog.warforge.data

import android.content.Context
import android.content.res.AssetManager
import com.maddog.warforge.data.parts.GROUND_Y
import com.maddog.warforge.solid.GlbLoader
import com.maddog.warforge.solid.Mesh

/**
 * A vehicle modelled in Blender rather than generated from its blueprint.
 *
 * The model is authored at full size in metres; the game works in blueprint units, with
 * the vehicle's nose at high x and the ground at [GROUND_Y]. These three numbers put one
 * into the other, so a Blender hull lands exactly where the procedural one did - which
 * matters, because the internal modules are still placed in blueprint coordinates and
 * have to end up inside it.
 *
 * @param asset path under `assets/`.
 * @param scale blueprint units per metre.
 * @param originX where the model's origin sits along the vehicle, in blueprint x.
 * @param originY the model's ground plane, in world y (which is blueprint y negated).
 * @param internals a second file holding the X-ray modules, in the same coordinates.
 *   Separate because the two are occluded separately - an engine baked against the hull
 *   around it comes out black, which is no use at all when the hull is ghosted away.
 */
class ModelSource(
    val asset: String,
    val scale: Float,
    val originX: Float,
    val originY: Float = -GROUND_Y,
    val internals: String? = null,
) {
    /** Maps a glTF vertex in metres onto the game's world, in place. */
    fun place(v: FloatArray) {
        v[0] = originX + v[0] * scale
        v[1] = originY + v[1] * scale
        v[2] = v[2] * scale
    }

    /** Normals only need rotating, and this transform does not rotate. */
    fun placeNormal(v: FloatArray) = Unit
}

/**
 * Loads and caches Blender models.
 *
 * Parsing a glb and re-transforming every vertex is not free, so a vehicle is read once
 * and kept. Call from a worker thread - [Scene.of] already runs on one.
 */
object ModelStore {

    private var assets: AssetManager? = null
    private val cache = HashMap<String, Map<String, Mesh>>()

    fun init(context: Context) {
        assets = context.applicationContext.assets
    }

    /** Part meshes by name, or an empty map when the model is missing or unreadable. */
    fun meshes(source: ModelSource): Map<String, Mesh> = read(source, source.asset)

    /** Module meshes by name, for the X-ray view. Empty when the vehicle has none yet. */
    fun internals(source: ModelSource): Map<String, Mesh> =
        source.internals?.let { read(source, it) }.orEmpty()

    private fun read(source: ModelSource, asset: String): Map<String, Mesh> =
        synchronized(cache) {
            cache.getOrPut(asset) {
                val manager = assets ?: return@getOrPut emptyMap()
                try {
                    manager.open(asset).use { GlbLoader.load(it, source::place) }
                } catch (e: Exception) {
                    // A missing or malformed model must never take the game down: the
                    // procedural geometry is still there to fall back on.
                    android.util.Log.w("Warforge", "could not load $asset", e)
                    emptyMap()
                }
            }
        }
}
