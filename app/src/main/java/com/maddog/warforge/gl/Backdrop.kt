package com.maddog.warforge.gl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

/**
 * The studio the vehicle sits in: a graded backdrop and a soft contact shadow on the
 * floor.
 *
 * Neither is strictly about the model, and both do more for how solid it looks than any
 * amount of extra geometry. An object on a flat background floats; the same object over
 * its own shadow, against a wall that is lighter behind it than at the edges of frame,
 * sits on the ground.
 */
class Backdrop {

    private var program = 0
    private var aPos = 0
    private var aColor = 0
    private var uMvp = 0

    private var gradient: FloatBuffer? = null
    private var gradientVerts = 0
    private var shadow: FloatBuffer? = null
    private var shadowVerts = 0

    private val identity = floatArrayOf(
        1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f,
    )

    fun create() {
        program = Shaders.flatProgram()
        aPos = GLES20.glGetAttribLocation(program, Shaders.A_POS)
        aColor = GLES20.glGetAttribLocation(program, Shaders.A_COLOR)
        uMvp = GLES20.glGetUniformLocation(program, Shaders.U_MVP)
        buildGradient()
    }

    /**
     * A vertical wash drawn straight in clip space, darkened at the top and bottom so
     * the eye settles on the middle of the frame where the vehicle is.
     */
    private fun buildGradient() {
        val bands = arrayOf(
            floatArrayOf(-1f, 0.045f, 0.062f, 0.082f),
            floatArrayOf(-0.25f, 0.075f, 0.094f, 0.116f),
            floatArrayOf(0.35f, 0.086f, 0.104f, 0.126f),
            floatArrayOf(1f, 0.038f, 0.048f, 0.062f),
        )
        val data = ArrayList<Float>()
        for (i in 0 until bands.size - 1) {
            val lo = bands[i]
            val hi = bands[i + 1]
            fun vertex(x: Float, b: FloatArray) {
                data.add(x); data.add(b[0]); data.add(0.999f)
                data.add(b[1]); data.add(b[2]); data.add(b[3]); data.add(1f)
            }
            vertex(-1f, lo); vertex(1f, lo); vertex(1f, hi)
            vertex(-1f, lo); vertex(1f, hi); vertex(-1f, hi)
        }
        gradientVerts = data.size / 7
        gradient = data.toFloatArray().toBuffer()
    }

    /**
     * Builds an elliptical shadow for a model of these bounds: opaque in the middle,
     * fading to nothing at the rim.
     */
    fun setModelBounds(
        minX: Float, minY: Float, minZ: Float, maxX: Float, maxY: Float, maxZ: Float,
    ) {
        val cx = (minX + maxX) * 0.5f
        val cz = (minZ + maxZ) * 0.5f
        val rx = (maxX - minX) * 0.58f
        val rz = (maxZ - minZ) * 0.72f
        val y = minY - (maxY - minY) * 0.012f
        val segments = 40
        val data = ArrayList<Float>((segments + 1) * 21)
        for (i in 0 until segments) {
            val a0 = (2.0 * Math.PI * i / segments).toFloat()
            val a1 = (2.0 * Math.PI * (i + 1) / segments).toFloat()
            data.add(cx); data.add(y); data.add(cz)
            data.add(0f); data.add(0f); data.add(0f); data.add(0.5f)
            for (a in listOf(a0, a1)) {
                data.add(cx + rx * kotlin.math.cos(a))
                data.add(y)
                data.add(cz + rz * kotlin.math.sin(a))
                data.add(0f); data.add(0f); data.add(0f); data.add(0f)
            }
        }
        shadowVerts = data.size / 7
        shadow = data.toFloatArray().toBuffer()
    }

    /** Full-screen wash. Drawn first, with depth off. */
    fun drawGradient() {
        val buffer = gradient ?: return
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glDepthMask(false)
        GLES20.glDisable(GLES20.GL_BLEND)
        draw(identity, buffer, gradientVerts)
        GLES20.glDepthMask(true)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }

    /** Contact shadow, in model space. Drawn before the vehicle, blended. */
    fun drawShadow(mvp: FloatArray) {
        val buffer = shadow ?: return
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glDepthMask(false)
        draw(mvp, buffer, shadowVerts)
        GLES20.glDepthMask(true)
        GLES20.glDisable(GLES20.GL_BLEND)
    }

    private fun draw(mvp: FloatArray, buffer: FloatBuffer, vertices: Int) {
        if (program == 0) return
        GLES20.glUseProgram(program)
        GLES20.glUniformMatrix4fv(uMvp, 1, false, mvp, 0)
        buffer.position(0)
        GLES20.glEnableVertexAttribArray(aPos)
        GLES20.glVertexAttribPointer(aPos, 3, GLES20.GL_FLOAT, false, 7 * 4, buffer)
        buffer.position(3)
        GLES20.glEnableVertexAttribArray(aColor)
        GLES20.glVertexAttribPointer(aColor, 4, GLES20.GL_FLOAT, false, 7 * 4, buffer)
        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertices)
        GLES20.glDisableVertexAttribArray(aPos)
        GLES20.glDisableVertexAttribArray(aColor)
    }

    private fun FloatArray.toBuffer(): FloatBuffer =
        ByteBuffer.allocateDirect(size * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
            .also { it.put(this).position(0) }
}
