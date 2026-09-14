package com.maddog.warforge.gl

import android.opengl.GLES20
import android.util.Log

/**
 * The two programs the game draws with.
 *
 * [SURFACE] does the vehicles: a three-light studio rig with a real specular highlight
 * whose strength and tightness come from the material, plus a rim term that separates a
 * dark hull from a dark background. [FLAT] does the backdrop and the ground shadow, which
 * need vertex colour and nothing else.
 */
object Shaders {

    const val A_POS = "aPos"
    const val A_NORMAL = "aNormal"
    const val A_COLOR = "aColor"
    const val A_MATERIAL = "aMaterial"
    const val U_MVP = "uMvp"
    const val U_MODEL = "uModel"
    const val U_EYE = "uEye"
    const val U_ALPHA = "uAlpha"
    const val U_CUT = "uCut"
    const val U_TINT = "uTint"
    const val U_TINT_MIX = "uTintMix"

    private const val SURFACE_VERT = """
        uniform mat4 uMvp;
        uniform mat4 uModel;
        attribute vec3 aPos;
        attribute vec3 aNormal;
        attribute vec4 aColor;
        attribute vec2 aMaterial;
        varying vec3 vNormal;
        varying vec4 vColor;
        varying vec2 vMaterial;
        varying vec3 vLocal;
        varying vec3 vWorld;
        void main() {
            vNormal = (uModel * vec4(aNormal, 0.0)).xyz;
            vColor = aColor;
            vMaterial = aMaterial;
            vLocal = aPos;
            vWorld = (uModel * vec4(aPos, 1.0)).xyz;
            gl_Position = uMvp * vec4(aPos, 1.0);
        }
    """

    private const val SURFACE_FRAG = """
        precision mediump float;
        varying vec3 vNormal;
        varying vec4 vColor;
        varying vec2 vMaterial;
        varying vec3 vLocal;
        varying vec3 vWorld;
        uniform vec3 uEye;
        uniform float uAlpha;
        uniform float uCut;
        uniform vec3 uTint;
        uniform float uTintMix;

        void main() {
            // The cutaway plane lives in model space, so the slice stays on the
            // vehicle's centreline however the camera is orbited.
            if (vLocal.z > uCut) discard;

            vec3 n = normalize(vNormal);
            vec3 v = normalize(uEye - vWorld);
            // Two-sided: a sectioned hull shows its inside faces, and an inward-facing
            // normal would render them black.
            if (dot(n, v) < 0.0) n = -n;

            vec3 keyDir = normalize(vec3(-0.42, 0.78, 0.46));
            vec3 fillDir = normalize(vec3(0.72, 0.12, -0.62));

            // Wrapped diffuse: softens the terminator so curved panels roll into shadow
            // instead of ending at a hard line.
            float key = max((dot(n, keyDir) + 0.28) / 1.28, 0.0);
            float fill = max(dot(n, fillDir), 0.0) * 0.30;
            // Hemispheric ambient - cool from the sky, warm off the ground.
            float up = n.y * 0.5 + 0.5;
            vec3 ambient = mix(vec3(0.105, 0.115, 0.105), vec3(0.185, 0.205, 0.245), up);

            vec3 diffuse = vColor.rgb * (ambient + key * 1.08 + fill);

            // Blinn-Phong highlight, tightness and strength straight off the material.
            vec3 h = normalize(keyDir + v);
            float spec = pow(max(dot(n, h), 0.0), vMaterial.y) * vMaterial.x;

            // Rim light: the only thing that reliably separates a dark hull from a dark
            // background when the camera is side-on.
            float rim = pow(1.0 - max(dot(n, v), 0.0), 3.0) * 0.24;

            vec3 c = diffuse + vec3(spec) + vec3(0.40, 0.48, 0.58) * rim;
            c = mix(c, uTint, uTintMix);
            // Gentle filmic shoulder so highlights roll off instead of clipping flat.
            c = c / (c + vec3(0.94)) * 1.94;
            gl_FragColor = vec4(c, vColor.a * uAlpha);
        }
    """

    private const val FLAT_VERT = """
        uniform mat4 uMvp;
        attribute vec3 aPos;
        attribute vec4 aColor;
        varying vec4 vColor;
        void main() {
            vColor = aColor;
            gl_Position = uMvp * vec4(aPos, 1.0);
        }
    """

    private const val FLAT_FRAG = """
        precision mediump float;
        varying vec4 vColor;
        void main() { gl_FragColor = vColor; }
    """

    fun surfaceProgram(): Int = link(SURFACE_VERT, SURFACE_FRAG)

    fun flatProgram(): Int = link(FLAT_VERT, FLAT_FRAG)

    private fun link(vertex: String, fragment: String): Int {
        val vs = compile(GLES20.GL_VERTEX_SHADER, vertex)
        val fs = compile(GLES20.GL_FRAGMENT_SHADER, fragment)
        val program = GLES20.glCreateProgram()
        GLES20.glAttachShader(program, vs)
        GLES20.glAttachShader(program, fs)
        GLES20.glLinkProgram(program)
        val status = IntArray(1)
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e("Warforge", "link failed: " + GLES20.glGetProgramInfoLog(program))
            GLES20.glDeleteProgram(program)
            return 0
        }
        GLES20.glDeleteShader(vs)
        GLES20.glDeleteShader(fs)
        return program
    }

    private fun compile(type: Int, source: String): Int {
        val shader = GLES20.glCreateShader(type)
        GLES20.glShaderSource(shader, source)
        GLES20.glCompileShader(shader)
        val status = IntArray(1)
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, status, 0)
        if (status[0] == 0) {
            Log.e("Warforge", "compile failed: " + GLES20.glGetShaderInfoLog(shader))
        }
        return shader
    }
}
