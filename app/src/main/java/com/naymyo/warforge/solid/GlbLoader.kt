package com.naymyo.warforge.solid

import com.naymyo.warforge.poly.Role
import org.json.JSONObject
import java.io.InputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * Reads a binary glTF and hands back one [Mesh] per object, keyed by the object's name.
 *
 * Deliberately a small subset: positions, normals, indices and a material name. That is
 * everything the models exported from `blender/` carry, and everything the renderer can
 * use. Anything else in the file - animations, skins, textures, cameras - is ignored.
 *
 * Colour does not come across. A Blender material is named for one of the game's roles
 * (`BODY`, `METAL`, `DARK`, `GLASS`, `ACCENT`, `TRIM`) and the loader maps it back, so
 * the vehicle is still painted from its own palette at upload time. That is what lets a
 * single model be drawn in any livery, and keeps the X-ray colour coding consistent.
 */
object GlbLoader {

    private const val MAGIC = 0x46546C67          // "glTF"
    private const val CHUNK_JSON = 0x4E4F534A
    private const val CHUNK_BIN = 0x004E4942

    private const val UNSIGNED_BYTE = 5121
    private const val UNSIGNED_SHORT = 5123
    private const val UNSIGNED_INT = 5125
    private const val FLOAT = 5126

    /**
     * What a stored `COLOR_0` of 1.0 means as a colour multiplier.
     *
     * glTF normalises integer colour channels to 0..1, which leaves no way to say
     * "brighter than the palette" - and a marking has to: white paint over a dark hull
     * is a multiplier well above one. `blender/wf.py` divides every tint by this before
     * writing it, and it is undone here. The two constants must match.
     */
    private const val TINT_RANGE = 5f

    /**
     * @param transform maps a glTF vertex (x forward, y up, z to starboard) into the
     *   game's world. Blender authors in metres; the game works in blueprint units.
     */
    fun load(stream: InputStream, transform: (FloatArray) -> Unit = {}): Map<String, Mesh> {
        val bytes = stream.readBytes()
        val buffer = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
        require(buffer.int == MAGIC) { "not a glb file" }
        buffer.int                                      // version
        buffer.int                                      // total length

        var json: JSONObject? = null
        var bin: ByteBuffer? = null
        while (buffer.remaining() >= 8) {
            val length = buffer.int
            val type = buffer.int
            val start = buffer.position()
            when (type) {
                CHUNK_JSON -> json = JSONObject(String(bytes, start, length, Charsets.UTF_8))
                CHUNK_BIN -> bin = ByteBuffer.wrap(bytes, start, length)
                    .slice().order(ByteOrder.LITTLE_ENDIAN)
            }
            buffer.position(start + length + (4 - length % 4) % 4)
        }
        val doc = json ?: error("glb has no json chunk")
        val blob = bin ?: error("glb has no binary chunk")
        return Reader(doc, blob, transform).read()
    }

    // ----------------------------------------------------------------------

    private class Reader(
        val doc: JSONObject,
        val bin: ByteBuffer,
        val transform: (FloatArray) -> Unit,
    ) {
        val accessors = doc.optJSONArray("accessors")
        val views = doc.optJSONArray("bufferViews")
        val meshes = doc.optJSONArray("meshes")
        val materials = doc.optJSONArray("materials")

        fun read(): Map<String, Mesh> {
            val out = LinkedHashMap<String, Mesh>()
            val nodes = doc.optJSONArray("nodes") ?: return out
            for (i in 0 until nodes.length()) {
                val node = nodes.getJSONObject(i)
                if (!node.has("mesh")) continue
                val name = node.optString("name", "node$i")
                val mesh = Mesh()
                val matrix = nodeMatrix(node)
                readMesh(meshes!!.getJSONObject(node.getInt("mesh")), mesh, matrix)
                if (!mesh.isEmpty) out[name] = mesh
            }
            return out
        }

        /** Node transform as a 4x4, from either an explicit matrix or TRS. */
        fun nodeMatrix(node: JSONObject): FloatArray? {
            node.optJSONArray("matrix")?.let { m ->
                return FloatArray(16) { m.getDouble(it).toFloat() }
            }
            val t = node.optJSONArray("translation")
            val r = node.optJSONArray("rotation")
            val s = node.optJSONArray("scale")
            if (t == null && r == null && s == null) return null

            val out = floatArrayOf(
                1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 0f, 1f,
            )
            if (r != null) {
                val x = r.getDouble(0).toFloat(); val y = r.getDouble(1).toFloat()
                val z = r.getDouble(2).toFloat(); val w = r.getDouble(3).toFloat()
                // Column-major, matching glTF.
                out[0] = 1 - 2 * (y * y + z * z); out[1] = 2 * (x * y + z * w); out[2] = 2 * (x * z - y * w)
                out[4] = 2 * (x * y - z * w); out[5] = 1 - 2 * (x * x + z * z); out[6] = 2 * (y * z + x * w)
                out[8] = 2 * (x * z + y * w); out[9] = 2 * (y * z - x * w); out[10] = 1 - 2 * (x * x + y * y)
            }
            if (s != null) {
                for (c in 0 until 3) {
                    val k = s.getDouble(c).toFloat()
                    out[c * 4] *= k; out[c * 4 + 1] *= k; out[c * 4 + 2] *= k
                }
            }
            if (t != null) {
                out[12] = t.getDouble(0).toFloat()
                out[13] = t.getDouble(1).toFloat()
                out[14] = t.getDouble(2).toFloat()
            }
            return out
        }

        fun readMesh(meshJson: JSONObject, out: Mesh, matrix: FloatArray?) {
            val primitives = meshJson.optJSONArray("primitives") ?: return
            for (p in 0 until primitives.length()) {
                val prim = primitives.getJSONObject(p)
                // Only triangles; the exporter is configured to produce nothing else.
                if (prim.optInt("mode", 4) != 4) continue
                val attrs = prim.getJSONObject("attributes")
                val pos = floats(attrs.getInt("POSITION"), 3) ?: continue
                val nrm = floats(attrs.optInt("NORMAL", -1), 3)
                val col = colors(attrs.optInt("COLOR_0", -1))
                val idx = indices(prim.optInt("indices", -1), pos.size / 3)
                val role = roleOf(prim.optInt("material", -1))

                var i = 0
                while (i + 2 < idx.size) {
                    val a = idx[i]; val b = idx[i + 1]; val c = idx[i + 2]
                    val va = vertex(pos, a, matrix, 0)
                    val vb = vertex(pos, b, matrix, 1)
                    val vc = vertex(pos, c, matrix, 2)
                    if (nrm != null) {
                        val na = normal(nrm, a, matrix, 0)
                        val nb = normal(nrm, b, matrix, 1)
                        val nc = normal(nrm, c, matrix, 2)
                        if (col != null) {
                            out.triNC(
                                va[0], va[1], va[2], na[0], na[1], na[2], tint(col, a, 0),
                                vb[0], vb[1], vb[2], nb[0], nb[1], nb[2], tint(col, b, 1),
                                vc[0], vc[1], vc[2], nc[0], nc[1], nc[2], tint(col, c, 2),
                                role, 0,
                            )
                            i += 3
                            continue
                        }
                        out.triN(
                            va[0], va[1], va[2], na[0], na[1], na[2],
                            vb[0], vb[1], vb[2], nb[0], nb[1], nb[2],
                            vc[0], vc[1], vc[2], nc[0], nc[1], nc[2],
                            role, 0,
                        )
                    } else {
                        out.tri(
                            va[0], va[1], va[2], vb[0], vb[1], vb[2],
                            vc[0], vc[1], vc[2], role, 0,
                        )
                    }
                    i += 3
                }
            }
        }

        /**
         * Scratch vectors, one set per corner of a triangle.
         *
         * A tank is sixty thousand triangles, and a fresh three-float array for every
         * position, normal and tint of every corner is over half a million throwaway
         * objects - enough garbage to be most of the time spent opening a vehicle.
         */
        private val scratch = Array(9) { FloatArray(3) }

        fun tint(src: FloatArray, index: Int, corner: Int): FloatArray {
            val out = scratch[6 + corner]
            out[0] = src[index * 3]
            out[1] = src[index * 3 + 1]
            out[2] = src[index * 3 + 2]
            return out
        }

        fun vertex(src: FloatArray, index: Int, matrix: FloatArray?, corner: Int): FloatArray {
            val v = scratch[corner]
            v[0] = src[index * 3]; v[1] = src[index * 3 + 1]; v[2] = src[index * 3 + 2]
            matrix?.let { apply(it, v, 1f) }
            transform(v)
            return v
        }

        fun normal(src: FloatArray, index: Int, matrix: FloatArray?, corner: Int): FloatArray {
            val v = scratch[3 + corner]
            v[0] = src[index * 3]; v[1] = src[index * 3 + 1]; v[2] = src[index * 3 + 2]
            matrix?.let { apply(it, v, 0f) }
            // The game's transform is a uniform scale plus a translation, so a normal
            // only needs re-normalising, not a separate inverse-transpose.
            val len = kotlin.math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2])
            if (len > 1e-6f) { v[0] /= len; v[1] /= len; v[2] /= len }
            return v
        }

        fun apply(m: FloatArray, v: FloatArray, w: Float) {
            val x = m[0] * v[0] + m[4] * v[1] + m[8] * v[2] + m[12] * w
            val y = m[1] * v[0] + m[5] * v[1] + m[9] * v[2] + m[13] * w
            val z = m[2] * v[0] + m[6] * v[1] + m[10] * v[2] + m[14] * w
            v[0] = x; v[1] = y; v[2] = z
        }

        fun roleOf(material: Int): Int {
            if (material < 0 || materials == null) return Role.BODY
            return when (materials.getJSONObject(material).optString("name").substringBefore('.')) {
                "DARK" -> Role.DARK
                "METAL" -> Role.METAL
                "GLASS" -> Role.GLASS
                "ACCENT" -> Role.ACCENT
                "TRIM" -> Role.TRIM
                else -> Role.BODY
            }
        }

        /**
         * Reads `COLOR_0` as three floats a vertex, whatever it was stored as.
         *
         * glTF allows vec3 or vec4, float or normalised byte or short, and Blender picks
         * short - so all of them have to be understood here. Alpha is dropped: the
         * models use the channel for nothing, and transparency comes from the role.
         */
        fun colors(accessor: Int): FloatArray? {
            if (accessor < 0 || accessors == null) return null
            val acc = accessors.getJSONObject(accessor)
            val components = if (acc.optString("type") == "VEC4") 4 else 3
            val type = acc.optInt("componentType")
            val count = acc.getInt("count")
            val view = views!!.getJSONObject(acc.getInt("bufferView"))
            val base = view.optInt("byteOffset", 0) + acc.optInt("byteOffset", 0)
            val size = when (type) {
                FLOAT -> 4
                UNSIGNED_SHORT -> 2
                UNSIGNED_BYTE -> 1
                else -> return null
            }
            val stride = view.optInt("byteStride", components * size)
            val scale = when (type) {
                UNSIGNED_SHORT -> TINT_RANGE / 65535f
                UNSIGNED_BYTE -> TINT_RANGE / 255f
                else -> TINT_RANGE
            }
            val out = FloatArray(count * 3)
            for (i in 0 until count) {
                for (c in 0 until 3) {
                    val at = base + i * stride + c * size
                    out[i * 3 + c] = when (type) {
                        FLOAT -> bin.getFloat(at) * scale
                        UNSIGNED_SHORT -> (bin.getShort(at).toInt() and 0xFFFF) * scale
                        else -> (bin.get(at).toInt() and 0xFF) * scale
                    }
                }
            }
            return out
        }

        /** Reads a float accessor, honouring the byte stride if the view is interleaved. */
        fun floats(accessor: Int, components: Int): FloatArray? {
            if (accessor < 0 || accessors == null) return null
            val acc = accessors.getJSONObject(accessor)
            if (acc.optInt("componentType") != FLOAT) return null
            val count = acc.getInt("count")
            val view = views!!.getJSONObject(acc.getInt("bufferView"))
            val base = view.optInt("byteOffset", 0) + acc.optInt("byteOffset", 0)
            val stride = view.optInt("byteStride", components * 4)
            val out = FloatArray(count * components)
            for (i in 0 until count) {
                for (c in 0 until components) {
                    out[i * components + c] = bin.getFloat(base + i * stride + c * 4)
                }
            }
            return out
        }

        /** Index accessor, or a sequential run when the primitive is not indexed. */
        fun indices(accessor: Int, vertexCount: Int): IntArray {
            if (accessor < 0 || accessors == null) return IntArray(vertexCount) { it }
            val acc = accessors.getJSONObject(accessor)
            val count = acc.getInt("count")
            val view = views!!.getJSONObject(acc.getInt("bufferView"))
            val base = view.optInt("byteOffset", 0) + acc.optInt("byteOffset", 0)
            val type = acc.getInt("componentType")
            val size = when (type) {
                UNSIGNED_BYTE -> 1
                UNSIGNED_SHORT -> 2
                UNSIGNED_INT -> 4
                else -> error("unsupported index type $type")
            }
            val stride = view.optInt("byteStride", size)
            return IntArray(count) { i ->
                val at = base + i * stride
                when (type) {
                    UNSIGNED_BYTE -> bin.get(at).toInt() and 0xFF
                    UNSIGNED_SHORT -> bin.getShort(at).toInt() and 0xFFFF
                    else -> bin.getInt(at)
                }
            }
        }
    }
}
