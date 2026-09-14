package com.maddog.warforge.gl

import com.maddog.warforge.data.ModelStore
import com.maddog.warforge.data.ModuleKind
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.solid.AmbientOcclusion
import com.maddog.warforge.solid.Mesh

/** Which layer a node belongs to: the outside of the vehicle, or its guts. */
enum class Layer { SHELL, INTERNAL }

/** How a node takes part in the current frame. */
enum class NodeState {
    /** Fitted: drawn for real. */
    PLACED,

    /** An empty slot waiting for its part: drawn as a translucent marker. */
    GHOST,

    /** Belongs to a later stage, or to nothing at all: not drawn. */
    HIDDEN,
}

/**
 * One drawable lump in the 3D view - a bolted-on part or an internal module.
 *
 * Vertex data is built on the main thread and uploaded to a buffer the first time the
 * GL thread sees it, which is why [vbo] and [vertexCount] start at zero.
 */
class SceneNode(
    val id: String,
    val name: String,
    val layer: Layer,
    val category: String,
    val detail: String,
    val info: String,
    val verts: FloatArray,
    val minX: Float, val minY: Float, val minZ: Float,
    val maxX: Float, val maxY: Float, val maxZ: Float,
    /**
     * How solid this node is in X-ray. Armour is the one thing that has to stay
     * see-through even in the internals pass: drawn opaque it is a box round the crew,
     * and hides the very thing it is supposed to put in context.
     */
    val xrayAlpha: Float = 1f,
) {
    var vbo = 0
    var vertexCount = 0

    /** Set by the board each time the build advances. */
    var state: NodeState = NodeState.PLACED
}

/** Everything to draw for one vehicle, plus the bounds the camera frames. */
class Scene(val nodes: List<SceneNode>) {

    /** Vertex data held in memory, for the cache to budget against. */
    val vertexBytes: Long = nodes.sumOf { it.verts.size.toLong() } * 4

    var minX = Float.MAX_VALUE; private set
    var minY = Float.MAX_VALUE; private set
    var minZ = Float.MAX_VALUE; private set
    var maxX = -Float.MAX_VALUE; private set
    var maxY = -Float.MAX_VALUE; private set
    var maxZ = -Float.MAX_VALUE; private set

    init {
        for (n in nodes) {
            if (n.minX < minX) minX = n.minX
            if (n.minY < minY) minY = n.minY
            if (n.minZ < minZ) minZ = n.minZ
            if (n.maxX > maxX) maxX = n.maxX
            if (n.maxY > maxY) maxY = n.maxY
            if (n.maxZ > maxZ) maxZ = n.maxZ
        }
        if (nodes.isEmpty()) {
            minX = -1f; minY = -1f; minZ = -1f; maxX = 1f; maxY = 1f; maxZ = 1f
        }
    }

    fun node(id: String): SceneNode? = nodes.firstOrNull { it.id == id }

    companion object {

        /**
         * The last few scenes built. Tessellating a vehicle and baking its occlusion
         * takes long enough to be worth never doing twice - reopening the museum on a
         * vehicle just looked at should be instant.
         */
        /**
         * How much vertex data the cache may hold, in bytes.
         *
         * A count of scenes is the wrong bound now that vehicles are modelled rather
         * than generated: a Blender tank is tens of thousands of triangles and its
         * vertices have to be kept, not just uploaded, because a lost GL context means
         * uploading them again. Four of those is a great deal of memory on a cheap
         * phone; four procedural ships is nothing.
         */
        private const val CACHE_BYTES = 24 * 1024 * 1024

        private val cache = object : LinkedHashMap<String, Scene>(8, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, Scene>): Boolean {
                if (size > 4) return true
                // Never evict down to nothing: the scene just asked for is in here.
                return size > 1 && values.sumOf { it.vertexBytes } > CACHE_BYTES
            }
        }

        /**
         * Builds the scene for [vehicle], or returns the cached one.
         *
         * **Call this off the main thread.** Building a vehicle tessellates every part,
         * every module, and bakes ambient occlusion over the lot; on a mid-range phone
         * that is a few hundred milliseconds and on a slow one it is enough to trip the
         * ANR watchdog.
         */
        fun of(vehicle: VehicleDef): Scene = synchronized(cache) {
            cache[vehicle.id]?.let { return it }
            val started = android.os.SystemClock.elapsedRealtime()
            val scene = build(vehicle)
            cache[vehicle.id] = scene
            // Worth logging: this is the one thing the player actually waits for, and
            // how long it takes depends on the model, not on anything visible in code.
            android.util.Log.d(
                "Warforge",
                "built ${vehicle.id} in ${android.os.SystemClock.elapsedRealtime() - started}ms" +
                    " - ${scene.nodes.size} nodes, ${scene.vertexBytes / 1024}KB",
            )
            scene
        }

        private fun build(vehicle: VehicleDef): Scene {
            // A Blender model supersedes the extruded blueprint part by part, so a
            // vehicle can be migrated a piece at a time rather than all at once.
            val model = vehicle.model?.let { ModelStore.meshes(it) }.orEmpty()
            val inside = vehicle.model?.let { ModelStore.internals(it) }.orEmpty()
            val parts = vehicle.parts.map { it to (model[it.id] ?: it.mesh) }
                .filter { !it.second.isEmpty }
            // Modules migrate to Blender the same way parts did: one at a time, by name.
            val modules = vehicle.modules.map { it to (inside[it.id] ?: it.mesh) }
                .filter { !it.second.isEmpty }

            // Occlusion is baked over the whole vehicle at once, so a turret darkens the
            // deck it sits on and a road wheel darkens inside its own track. Baking each
            // part alone would miss every contact that matters.
            val meshes = parts.map { it.second } + modules.map { it.second }
            val occlusion = AmbientOcclusion.bake(meshes)

            val nodes = ArrayList<SceneNode>(meshes.size)
            for ((i, entry) in parts.withIndex()) {
                val (p, mesh) = entry
                nodes += node(
                    p.id, p.name, Layer.SHELL, vehicle.branch.label, "", p.info,
                    mesh, mesh.toVertexArray(vehicle.palette, ao = occlusion[i]),
                )
            }
            for ((i, entry) in modules.withIndex()) {
                val (m, mesh) = entry
                nodes += node(
                    "mod_" + m.id, m.name, Layer.INTERNAL, m.kind.label, m.detail, m.info,
                    mesh,
                    mesh.toVertexArray(m.kind.palette, ao = occlusion[parts.size + i]),
                    xrayAlpha = if (m.kind == ModuleKind.ARMOUR) ARMOUR_ALPHA else 1f,
                )
            }
            return Scene(nodes)
        }

        private const val ARMOUR_ALPHA = 0.32f

        private fun node(
            id: String, name: String, layer: Layer, category: String,
            detail: String, info: String, mesh: Mesh, verts: FloatArray,
            xrayAlpha: Float = 1f,
        ) = SceneNode(
            id, name, layer, category, detail, info, verts,
            mesh.minX, mesh.minY, mesh.minZ, mesh.maxX, mesh.maxY, mesh.maxZ, xrayAlpha,
        )

        /** Kinds present in a vehicle, for the legend. */
        fun legend(vehicle: VehicleDef): List<ModuleKind> =
            vehicle.modules.map { it.kind }.distinct()
    }
}
