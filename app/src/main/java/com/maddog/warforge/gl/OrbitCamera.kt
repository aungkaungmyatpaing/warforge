package com.maddog.warforge.gl

import android.opengl.Matrix
import kotlin.math.atan
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Turntable camera: the model sits still at the origin and the eye swings around it.
 *
 * Distance is computed from the model's bounding sphere and the *narrower* of the two
 * field-of-view angles, so a 33-metre carrier and a 5-metre Renault both frame
 * themselves - and so does either one on a tall phone, where the horizontal field is the
 * tight one and a long hull would otherwise run off both edges.
 */
class OrbitCamera {

    /** Radians. Opens on the vehicle's front quarter. */
    var azimuth = 0.72f
    var elevation = 0.28f

    /** Player zoom, multiplying the automatic fit. Below 1 is closer in. */
    var zoom = 1f
        private set

    private var cx = 0f
    private var cy = 0f
    private var cz = 0f

    /** Radius of the sphere enclosing the model, in model units. */
    private var radius = 1f

    private val model = FloatArray(16)
    private val view = FloatArray(16)
    private val proj = FloatArray(16)
    private val temp = FloatArray(16)
    val mvp = FloatArray(16)
    val modelMatrix: FloatArray get() = model

    /** Eye position in world space, for specular highlights and the rim term. */
    val eye = FloatArray(3)

    fun frame(minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float) {
        cx = (minX + maxX) * 0.5f
        cy = (minY + maxY) * 0.5f
        cz = (minZ + maxZ) * 0.5f
        val hx = (maxX - minX) * 0.5f
        val hy = (maxY - minY) * 0.5f
        val hz = (maxZ - minZ) * 0.5f
        // Corner-to-centre distance: the model can be turned to any angle, so the
        // enclosing sphere is what has to fit, not the silhouette from one side.
        radius = sqrt(hx * hx + hy * hy + hz * hz).coerceAtLeast(1f)
    }

    fun update(viewportW: Int, viewportH: Int) {
        val aspect = if (viewportH == 0) 1f else viewportW.toFloat() / viewportH
        Matrix.perspectiveM(proj, 0, FOV_Y, aspect, 0.02f, 40f)

        // Model: centre on the origin, then shrink to unit radius.
        Matrix.setIdentityM(model, 0)
        val s = 1f / radius
        Matrix.scaleM(model, 0, s, s, s)
        Matrix.translateM(model, 0, -cx, -cy, -cz)

        val distance = fitDistance(aspect) * zoom
        val ex = distance * cos(elevation) * cos(azimuth)
        val ey = distance * sin(elevation)
        val ez = distance * cos(elevation) * sin(azimuth)
        eye[0] = ex; eye[1] = ey; eye[2] = ez
        Matrix.setLookAtM(view, 0, ex, ey, ez, 0f, 0f, 0f, 0f, 1f, 0f)

        Matrix.multiplyMM(temp, 0, view, 0, model, 0)
        Matrix.multiplyMM(mvp, 0, proj, 0, temp, 0)
    }

    /** How far back a unit sphere has to sit to fill the tighter of the two axes. */
    private fun fitDistance(aspect: Float): Float {
        val halfY = (FOV_Y * 0.5f) * DEG_TO_RAD
        val halfX = atan(tan(halfY) * aspect)
        return MARGIN / sin(min(halfX, halfY))
    }

    /** Clamps elevation so the camera never tips past the poles and gimbal-flips. */
    fun orbit(dAzimuth: Float, dElevation: Float) {
        azimuth += dAzimuth
        elevation = (elevation + dElevation).coerceIn(-1.45f, 1.45f)
    }

    fun zoom(factor: Float) {
        zoom = (zoom / factor).coerceIn(0.45f, 2.2f)
    }

    fun resetZoom() { zoom = 1f }

    private companion object {
        const val FOV_Y = 42f
        const val DEG_TO_RAD = (Math.PI / 180.0).toFloat()

        /** A little air around the model so it never touches the edges. */
        const val MARGIN = 1.12f
    }
}
