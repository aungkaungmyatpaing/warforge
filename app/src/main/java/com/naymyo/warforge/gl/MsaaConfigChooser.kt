package com.naymyo.warforge.gl

import android.opengl.GLSurfaceView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLDisplay

/**
 * Asks for multisampling, and settles for less rather than failing.
 *
 * Nothing else in this renderer buys as much perceived quality per line of code: the
 * models are all straight edges meeting at angles, and without MSAA every one of them
 * crawls with stair-steps. Some devices - and the software renderer in an emulator -
 * offer no multisampled config at all, hence the ladder down to plain RGB565.
 */
class MsaaConfigChooser : GLSurfaceView.EGLConfigChooser {

    override fun chooseConfig(egl: EGL10, display: EGLDisplay): EGLConfig {
        for (samples in intArrayOf(4, 2)) {
            pick(egl, display, samples)?.let { return it }
        }
        return pick(egl, display, 0) ?: error("no usable EGL config")
    }

    private fun pick(egl: EGL10, display: EGLDisplay, samples: Int): EGLConfig? {
        val spec = mutableListOf(
            EGL10.EGL_RENDERABLE_TYPE, 4,        // EGL_OPENGL_ES2_BIT
            EGL10.EGL_RED_SIZE, 8,
            EGL10.EGL_GREEN_SIZE, 8,
            EGL10.EGL_BLUE_SIZE, 8,
            EGL10.EGL_ALPHA_SIZE, 0,
            EGL10.EGL_DEPTH_SIZE, 16,
        )
        if (samples > 0) {
            spec += listOf(EGL10.EGL_SAMPLE_BUFFERS, 1, EGL10.EGL_SAMPLES, samples)
        }
        spec += EGL10.EGL_NONE

        val count = IntArray(1)
        val attrs = spec.toIntArray()
        if (!egl.eglChooseConfig(display, attrs, null, 0, count) || count[0] <= 0) return null
        val configs = arrayOfNulls<EGLConfig>(count[0])
        if (!egl.eglChooseConfig(display, attrs, configs, count[0], count)) return null
        return configs.firstOrNull()
    }
}
