package com.maddog.warforge.ui

import android.annotation.SuppressLint
import android.content.Context
import android.opengl.GLSurfaceView
import android.os.SystemClock
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ViewConfiguration
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.gl.Scene
import com.maddog.warforge.gl.MsaaConfigChooser
import com.maddog.warforge.gl.NodeState
import com.maddog.warforge.gl.SceneNode
import com.maddog.warforge.gl.VehicleRenderer
import com.maddog.warforge.gl.ViewMode
import kotlin.math.abs
import kotlin.math.hypot

/**
 * The 3D viewport: drag to orbit, pinch to zoom, tap to identify.
 *
 * Left alone for a few seconds it starts turning on its own, which is what makes the
 * screen read as an exhibit rather than a form.
 */
class InspectSurfaceView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null,
) : GLSurfaceView(context, attrs) {

    private val renderer = VehicleRenderer()

    /** Fired when the player taps a part or module - null when they tap empty space. */
    var onPicked: ((SceneNode?) -> Unit)? = null

    /**
     * False when something else owns the gestures - the assembly board puts an overlay
     * on top and forwards only the camera drags down here.
     */
    var interactive: Boolean = true

    /**
     * Idle spin is a museum flourish; it would fight the player during a build. It also
     * decides how the surface renders: a spinning exhibit needs every frame, a board that
     * only changes when the player does something does not, and redrawing it anyway
     * costs battery for nothing.
     */
    var autoSpin: Boolean = true
        set(value) {
            field = value
            renderMode = if (value) RENDERMODE_CONTINUOUSLY else RENDERMODE_WHEN_DIRTY
        }

    private var downX = 0f
    private var downY = 0f
    private var lastX = 0f
    private var lastY = 0f
    private var lastPinch = 0f
    private var dragging = false
    private var pinching = false
    private var lastTouchAt = 0L
    private val slop = ViewConfiguration.get(context).scaledTouchSlop

    init {
        setEGLContextClientVersion(2)
        setEGLConfigChooser(MsaaConfigChooser())
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
        preserveEGLContextOnPause = true
    }

    /**
     * Loads [vehicle] and calls [onReady] on the main thread once it can be drawn.
     *
     * The build - tessellation plus baked occlusion - runs on a worker thread. Doing it
     * inline was enough to hang the app on a slow device, and there is nothing to show
     * until it finishes anyway.
     */
    fun show(vehicle: VehicleDef, onReady: () -> Unit = {}) {
        renderer.pendingScene = null
        renderer.selectedId = null
        val token = ++loadToken
        Thread({
            val scene = Scene.of(vehicle)
            post {
                // A second vehicle may have been asked for while this one was building.
                if (token != loadToken) return@post
                renderer.pendingScene = scene
                requestRender()
                onReady()
            }
        }, "warforge-scene").apply { isDaemon = true }.start()
    }

    /** Guards against a stale load finishing after the player has moved on. */
    private var loadToken = 0

    /**
     * Sets what is fitted and what is still an empty slot. Everything not named in
     * either set belongs to a later stage and stays hidden.
     */
    fun setBuildState(placed: Set<String>, ghosts: Set<String>) {
        val scene = renderer.scene ?: return
        for (node in scene.nodes) {
            node.state = when (node.id) {
                in placed -> NodeState.PLACED
                in ghosts -> NodeState.GHOST
                else -> NodeState.HIDDEN
            }
        }
        requestRender()
    }

    /** Everything visible - the museum's default. */
    fun showEverything() {
        val scene = renderer.scene ?: return
        for (node in scene.nodes) node.state = NodeState.PLACED
        requestRender()
    }

    /** Camera control for an owner that has taken over the gestures. */
    fun orbitBy(dxPx: Float, dyPx: Float) {
        renderer.queueOrbit(dxPx, dyPx)
        requestRender()
    }

    fun zoomBy(factor: Float) {
        renderer.queueZoom(factor)
        requestRender()
    }

    /** Where a slot's centre lands on screen, for aiming a dropped part at it. */
    fun project(x: Float, y: Float, z: Float, out: FloatArray): Boolean =
        renderer.projectToScreen(x, y, z, out)

    var mode: ViewMode
        get() = renderer.mode
        set(value) { renderer.mode = value; requestRender() }

    var cutFraction: Float
        get() = renderer.cutFraction
        set(value) { renderer.cutFraction = value; requestRender() }

    var selectedId: String?
        get() = renderer.selectedId
        set(value) { renderer.selectedId = value; requestRender() }

    fun legend() = renderer.scene?.nodes.orEmpty()

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        lastTouchAt = SystemClock.uptimeMillis()
        if (!interactive) return false
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                downX = event.x; downY = event.y
                lastX = event.x; lastY = event.y
                dragging = false
                pinching = false
                parent?.requestDisallowInterceptTouchEvent(true)
            }

            MotionEvent.ACTION_POINTER_DOWN -> {
                pinching = true
                lastPinch = spread(event)
            }

            // Lifting one finger of a pinch leaves the remaining one somewhere else
            // entirely. Re-anchoring here stops the model jumping when the drag resumes.
            MotionEvent.ACTION_POINTER_UP -> {
                val remaining = if (event.actionIndex == 0) 1 else 0
                lastX = event.getX(remaining)
                lastY = event.getY(remaining)
                downX = lastX
                downY = lastY
                pinching = false
                dragging = false
            }

            MotionEvent.ACTION_MOVE -> {
                if (pinching && event.pointerCount >= 2) {
                    val now = spread(event)
                    if (lastPinch > 1f && now > 1f) renderer.queueZoom(now / lastPinch)
                    lastPinch = now
                } else {
                    val dx = event.x - lastX
                    val dy = event.y - lastY
                    if (!dragging && hypot(event.x - downX, event.y - downY) > slop) dragging = true
                    if (dragging) renderer.queueOrbit(dx, dy)
                    lastX = event.x; lastY = event.y
                }
                requestRender()
            }

            MotionEvent.ACTION_UP -> {
                if (!dragging && !pinching &&
                    abs(event.x - downX) < slop && abs(event.y - downY) < slop
                ) {
                    val x = event.x
                    val y = event.y
                    // Picking needs the current matrices, which only the GL thread owns.
                    queueEvent {
                        val hit = renderer.pick(x, y)
                        renderer.selectedId = hit?.id
                        post { onPicked?.invoke(hit) }
                    }
                }
                dragging = false
                pinching = false
            }
        }
        return true
    }

    private fun spread(event: MotionEvent): Float =
        hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))

    /** Idle spin, so a vehicle left alone keeps showing itself off. */
    private val idleSpin = object : Runnable {
        override fun run() {
            if (autoSpin && SystemClock.uptimeMillis() - lastTouchAt > IDLE_MS) {
                queueEvent { renderer.camera.orbit(0.0035f, 0f) }
            }
            postDelayed(this, 16L)
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        postDelayed(idleSpin, IDLE_MS)
    }

    override fun onDetachedFromWindow() {
        removeCallbacks(idleSpin)
        super.onDetachedFromWindow()
    }

    /** Records a touch so the idle spin holds off while the player is working. */
    fun noteTouch() { lastTouchAt = SystemClock.uptimeMillis() }

    private companion object {
        const val IDLE_MS = 3200L
    }
}
