package com.maddog.warforge.gl

import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

/** How much of the vehicle is showing. */
enum class ViewMode {
    /** Just the vehicle, solid. */
    SOLID,

    /** Outer shell faded so the guts show through it. */
    XRAY,

    /** Near half sliced away, like a museum sectioned exhibit. */
    CUTAWAY,
}

/**
 * Draws one vehicle.
 *
 * Everything is flat-shaded triangles in one program, so the whole renderer is a mode
 * switch over three draw passes: internals opaque, shell either opaque or blended, and
 * a highlight tint on whatever the player last tapped.
 */
class VehicleRenderer : GLSurfaceView.Renderer {

    val camera = OrbitCamera()

    /** Set from the UI thread; picked up on the next frame. */
    @Volatile var pendingScene: Scene? = null
    @Volatile var mode: ViewMode = ViewMode.SOLID
    @Volatile var selectedId: String? = null

    /** 0 = cut at the centreline, 1 = nothing cut. Fraction of the half-width. */
    @Volatile var cutFraction: Float = 1f

    /**
     * Camera input waiting to be applied, in pixels and as a zoom factor.
     *
     * Touch events arrive far faster than a slow device draws frames. Posting one
     * `queueEvent` per move let a backlog build up behind a struggling GL thread and
     * then run all at once, which is why the model used to keep spinning after the
     * finger stopped. Accumulating instead means a frame consumes exactly the motion
     * that happened since the last one, however far behind it is.
     */
    private val pendingOrbit = FloatArray(2)
    private var pendingZoom = 1f
    private val inputLock = Any()

    fun queueOrbit(dxPx: Float, dyPx: Float) = synchronized(inputLock) {
        pendingOrbit[0] += dxPx
        pendingOrbit[1] += dyPx
    }

    fun queueZoom(factor: Float) = synchronized(inputLock) {
        pendingZoom *= factor
    }

    private fun consumeInput() {
        var dx = 0f
        var dy = 0f
        var zoom = 1f
        synchronized(inputLock) {
            dx = pendingOrbit[0]; dy = pendingOrbit[1]; zoom = pendingZoom
            pendingOrbit[0] = 0f; pendingOrbit[1] = 0f; pendingZoom = 1f
        }
        if (dx != 0f || dy != 0f) camera.orbit(dx * ORBIT_PER_PX, -dy * ORBIT_PER_PX)
        if (zoom != 1f) camera.zoom(zoom)
    }

    var scene: Scene? = null
        private set

    private val backdrop = Backdrop()

    private var program = 0
    private var aPos = 0
    private var aNormal = 0
    private var aColor = 0
    private var aMaterial = 0
    private var uMvp = 0
    private var uModel = 0
    private var uEye = 0
    private var uAlpha = 0
    private var uCut = 0
    private var uTint = 0
    private var uTintMix = 0

    private var viewW = 1
    private var viewH = 1

    /** Set when the surface is recreated: every VBO is gone and must be re-uploaded. */
    private var contextLost = true

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.043f, 0.055f, 0.070f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)
        program = Shaders.surfaceProgram()
        aPos = GLES20.glGetAttribLocation(program, Shaders.A_POS)
        aNormal = GLES20.glGetAttribLocation(program, Shaders.A_NORMAL)
        aColor = GLES20.glGetAttribLocation(program, Shaders.A_COLOR)
        aMaterial = GLES20.glGetAttribLocation(program, Shaders.A_MATERIAL)
        uMvp = GLES20.glGetUniformLocation(program, Shaders.U_MVP)
        uModel = GLES20.glGetUniformLocation(program, Shaders.U_MODEL)
        uEye = GLES20.glGetUniformLocation(program, Shaders.U_EYE)
        backdrop.create()
        uAlpha = GLES20.glGetUniformLocation(program, Shaders.U_ALPHA)
        uCut = GLES20.glGetUniformLocation(program, Shaders.U_CUT)
        uTint = GLES20.glGetUniformLocation(program, Shaders.U_TINT)
        uTintMix = GLES20.glGetUniformLocation(program, Shaders.U_TINT_MIX)
        contextLost = true
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        viewW = width
        viewH = height
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        pendingScene?.let { next ->
            pendingScene = null
            scene = next
            camera.frame(next.minX, next.minY, next.minZ, next.maxX, next.maxY, next.maxZ)
            backdrop.setModelBounds(
                next.minX, next.minY, next.minZ, next.maxX, next.maxY, next.maxZ,
            )
            contextLost = true
        }
        val scene = scene ?: run {
            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
            backdrop.drawGradient()
            return
        }
        if (contextLost) {
            for (n in scene.nodes) upload(n)
            contextLost = false
        }

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        backdrop.drawGradient()
        consumeInput()
        camera.update(viewW, viewH)
        backdrop.drawShadow(camera.mvp)
        // The overlay projects slot positions on the UI thread; hand it a snapshot
        // rather than the live matrix the GL thread is about to overwrite.
        mvpForUi = camera.mvp.copyOf()
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(uMvp, 1, false, camera.mvp, 0)
        GLES20.glUniformMatrix4fv(uModel, 1, false, camera.modelMatrix, 0)
        GLES20.glUniform3f(uEye, camera.eye[0], camera.eye[1], camera.eye[2])

        val mode = mode
        val cut = if (mode == ViewMode.CUTAWAY) {
            scene.minZ + (scene.maxZ - scene.minZ) * cutFraction
        } else {
            Float.MAX_VALUE
        }
        GLES20.glUniform1f(uCut, cut)

        // Internals first, always solid: they are the subject in both special modes.
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
        if (mode != ViewMode.SOLID) {
            for (n in scene.nodes) {
                if (n.layer == Layer.INTERNAL && n.xrayAlpha >= 1f) draw(n, 1f)
            }
            // Armour last and see-through, so it frames the crew instead of boxing them in.
            // A section view is different: there the plate thickness is the point.
            val armourAlpha = if (mode == ViewMode.CUTAWAY) 1f else -1f
            val translucent = scene.nodes.filter { it.layer == Layer.INTERNAL && it.xrayAlpha < 1f }
            if (translucent.isNotEmpty()) {
                if (armourAlpha < 0f) {
                    GLES20.glEnable(GLES20.GL_BLEND)
                    GLES20.glDepthMask(false)
                }
                for (n in translucent) draw(n, if (armourAlpha > 0f) armourAlpha else n.xrayAlpha)
                GLES20.glDepthMask(true)
                GLES20.glDisable(GLES20.GL_BLEND)
            }
        }

        when (mode) {
            ViewMode.SOLID -> {
                for (n in scene.nodes) if (n.layer == Layer.SHELL) draw(n, 1f)
            }
            ViewMode.CUTAWAY -> {
                // The slice exposes interior faces, so both sides have to be drawn.
                GLES20.glDisable(GLES20.GL_CULL_FACE)
                for (n in scene.nodes) if (n.layer == Layer.SHELL) draw(n, 1f)
            }
            ViewMode.XRAY -> {
                GLES20.glEnable(GLES20.GL_BLEND)
                GLES20.glDepthMask(false)
                GLES20.glDisable(GLES20.GL_CULL_FACE)
                for (n in scene.nodes) if (n.layer == Layer.SHELL) draw(n, SHELL_ALPHA)
                GLES20.glDepthMask(true)
                GLES20.glDisable(GLES20.GL_BLEND)
            }
        }
        drawGhosts(scene)
        GLES20.glEnable(GLES20.GL_CULL_FACE)
    }

    /**
     * Empty slots, drawn as faint blue shapes. Showing the space a part will occupy -
     * rather than just naming it - is what makes fitting an engine into a hull you
     * cannot see inside possible at all.
     */
    private fun drawGhosts(scene: Scene) {
        val ghosts = scene.nodes.filter { it.state == NodeState.GHOST }
        if (ghosts.isEmpty()) return
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glDepthMask(false)
        GLES20.glDisable(GLES20.GL_CULL_FACE)
        for (node in ghosts) {
            if (node.vertexCount == 0) continue
            val lit = node.id == selectedId
            GLES20.glUniform1f(uAlpha, if (lit) 0.55f else GHOST_ALPHA)
            if (lit) GLES20.glUniform3f(uTint, 1f, 0.78f, 0.30f)
            else GLES20.glUniform3f(uTint, 0.35f, 0.62f, 0.86f)
            GLES20.glUniform1f(uTintMix, 0.88f)
            bindAndDraw(node)
        }
        GLES20.glUniform1f(uTintMix, 0f)
        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun draw(node: SceneNode, alpha: Float) {
        if (node.state != NodeState.PLACED || node.vertexCount == 0) return
        val selected = node.id == selectedId
        GLES20.glUniform1f(uAlpha, if (selected) minOf(1f, alpha + 0.45f) else alpha)
        GLES20.glUniform3f(uTint, 1f, 0.84f, 0.42f)
        GLES20.glUniform1f(uTintMix, if (selected) 0.45f else 0f)
        bindAndDraw(node)
    }

    private fun bindAndDraw(node: SceneNode) {
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, node.vbo)
        val stride = STRIDE_FLOATS * 4
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, stride, 0)
        GLES20.glEnableVertexAttribArray(aNormal)
        GLES20.glVertexAttribPointer(aNormal, 3, GLES20.GL_FLOAT, false, stride, 12)
        GLES20.glEnableVertexAttribArray(aColor)
        GLES20.glVertexAttribPointer(aColor, 4, GLES20.GL_FLOAT, false, stride, 24)
        GLES20.glEnableVertexAttribArray(aMaterial)
        GLES20.glVertexAttribPointer(aMaterial, 2, GLES20.GL_FLOAT, false, stride, 40)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, node.vertexCount)
        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aNormal)
        GLES20.glDisableVertexAttribArray(aColor)
        GLES20.glDisableVertexAttribArray(aMaterial)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    private fun upload(node: SceneNode) {
        val ids = IntArray(1)
        GLES20.glGenBuffers(1, ids, 0)
        node.vbo = ids[0]
        node.vertexCount = node.verts.size / STRIDE_FLOATS
        val bb = ByteBuffer.allocateDirect(node.verts.size * 4).order(ByteOrder.nativeOrder())
        val fb: FloatBuffer = bb.asFloatBuffer()
        fb.put(node.verts).position(0)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, node.vbo)
        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, node.verts.size * 4, fb, GLES20.GL_STATIC_DRAW)
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0)
    }

    // ----------------------------------------------------------------------
    // Picking
    // ----------------------------------------------------------------------

    @Volatile private var mvpForUi: FloatArray? = null

    /**
     * Projects a model-space point to view pixels. Returns false when the point is
     * behind the camera, where a projection would be meaningless.
     */
    fun projectToScreen(x: Float, y: Float, z: Float, out: FloatArray): Boolean {
        val m = mvpForUi ?: return false
        val cx = m[0] * x + m[4] * y + m[8] * z + m[12]
        val cy = m[1] * x + m[5] * y + m[9] * z + m[13]
        val cw = m[3] * x + m[7] * y + m[11] * z + m[15]
        if (cw <= 1e-4f) return false
        out[0] = (cx / cw * 0.5f + 0.5f) * viewW
        out[1] = (0.5f - cy / cw * 0.5f) * viewH
        return true
    }

    private val invMvp = FloatArray(16)
    private val near = FloatArray(4)
    private val far = FloatArray(4)

    /**
     * Returns the node under ([sx], [sy]) in view pixels, or null.
     *
     * Bounding boxes rather than triangles: parts here are compact blocks, a finger is
     * far bigger than the error, and the alternative is a per-triangle test over tens of
     * thousands of triangles on the UI thread.
     */
    fun pick(sx: Float, sy: Float): SceneNode? {
        val scene = scene ?: return null
        if (!Matrix.invertM(invMvp, 0, camera.mvp, 0)) return null
        val ndcX = 2f * sx / viewW - 1f
        val ndcY = 1f - 2f * sy / viewH
        unproject(ndcX, ndcY, -1f, near)
        unproject(ndcX, ndcY, 1f, far)

        val ox = near[0]; val oy = near[1]; val oz = near[2]
        val dx = far[0] - ox; val dy = far[1] - oy; val dz = far[2] - oz

        val cut = if (mode == ViewMode.CUTAWAY) {
            scene.minZ + (scene.maxZ - scene.minZ) * cutFraction
        } else {
            Float.MAX_VALUE
        }

        // Internals win ties: in X-ray and cutaway they are what the player is aiming at.
        val order = if (mode == ViewMode.SOLID) listOf(Layer.SHELL, Layer.INTERNAL)
        else listOf(Layer.INTERNAL, Layer.SHELL)
        for (layer in order) {
            var best: SceneNode? = null
            var bestT = Float.MAX_VALUE
            for (n in scene.nodes) {
                if (n.state != NodeState.PLACED || n.layer != layer) continue
                if (n.minZ > cut) continue
                val t = intersect(ox, oy, oz, dx, dy, dz, n, minOf(n.maxZ, cut))
                if (t in 0f..bestT) { bestT = t; best = n }
            }
            if (best != null) return best
        }
        return null
    }

    private fun unproject(x: Float, y: Float, z: Float, out: FloatArray) {
        val v = floatArrayOf(x, y, z, 1f)
        Matrix.multiplyMV(out, 0, invMvp, 0, v, 0)
        if (out[3] != 0f) {
            out[0] /= out[3]; out[1] /= out[3]; out[2] /= out[3]
        }
    }

    /** Slab test. Returns the entry distance, or -1 when the ray misses. */
    private fun intersect(
        ox: Float, oy: Float, oz: Float, dx: Float, dy: Float, dz: Float,
        n: SceneNode, maxZ: Float,
    ): Float {
        var tMin = 0f
        var tMax = 1f
        val lo = floatArrayOf(n.minX, n.minY, n.minZ)
        val hi = floatArrayOf(n.maxX, n.maxY, maxZ)
        val o = floatArrayOf(ox, oy, oz)
        val d = floatArrayOf(dx, dy, dz)
        for (i in 0 until 3) {
            if (kotlin.math.abs(d[i]) < 1e-7f) {
                if (o[i] < lo[i] || o[i] > hi[i]) return -1f
                continue
            }
            var t1 = (lo[i] - o[i]) / d[i]
            var t2 = (hi[i] - o[i]) / d[i]
            if (t1 > t2) { val t = t1; t1 = t2; t2 = t }
            if (t1 > tMin) tMin = t1
            if (t2 < tMax) tMax = t2
            if (tMin > tMax) return -1f
        }
        return tMin
    }

    private companion object {
        /** Radians of orbit per pixel dragged. */
        const val ORBIT_PER_PX = 0.008f
        const val STRIDE_FLOATS = 12
        const val SHELL_ALPHA = 0.19f
        const val GHOST_ALPHA = 0.17f
    }
}
