package com.naymyo.warforge

import com.naymyo.warforge.data.BP_H
import com.naymyo.warforge.data.BP_W
import com.naymyo.warforge.data.Catalog
import com.naymyo.warforge.data.ModuleKind
import com.naymyo.warforge.solid.Mesh
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards on the generated 3D geometry. The solids are derived from the 2D art rather
 * than authored, so what can go wrong is structural: a part that extrudes to nothing, a
 * depth that was never set, a mesh so dense it stalls the upload.
 */
class SolidTest {

    @Test
    fun `every part produces a solid`() {
        val empty = buildList {
            for (v in Catalog.all) for (p in v.parts) {
                if (p.mesh.isEmpty) add("${v.id}/${p.id}")
            }
        }
        assertTrue("parts with no 3D geometry:\n" + empty.joinToString("\n"), empty.isEmpty())
    }

    @Test
    fun `vertex arrays are well formed`() {
        for (v in Catalog.all) {
            for (p in v.parts) {
                val verts = p.mesh.toVertexArray(v.palette)
                assertEquals(
                    "${v.id}/${p.id} vertex array is not a whole number of vertices",
                    0, verts.size % Mesh.STRIDE,
                )
                assertEquals(
                    "${v.id}/${p.id} vertex count does not match its triangles",
                    p.mesh.triangleCount * 3, verts.size / Mesh.STRIDE,
                )
                for (f in verts) {
                    assertTrue("${v.id}/${p.id} has a NaN in its vertex data", !f.isNaN())
                }
            }
        }
    }

    /**
     * Solids are extruded from the blueprint, so x and y must stay inside it; depth is
     * free but a part wider than the blueprint is long means a depth argument went in
     * with the wrong sign or scale.
     */
    @Test
    fun `solids keep to a sane volume`() {
        val bad = buildList {
            for (v in Catalog.all) for (p in v.parts) {
                val m = p.mesh
                if (m.isEmpty) continue
                if (m.minX < -60f || m.maxX > BP_W + 60f) add("${v.id}/${p.id} x=[${m.minX},${m.maxX}]")
                // World y is the blueprint's y negated, so the model sits at negative y.
                if (m.maxY > 60f || m.minY < -(BP_H + 60f)) add("${v.id}/${p.id} y=[${m.minY},${m.maxY}]")
                if (m.minZ < -520f || m.maxZ > 520f) add("${v.id}/${p.id} z=[${m.minZ},${m.maxZ}]")
            }
        }
        assertTrue("solids out of bounds:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test
    fun `no vehicle is too heavy to upload`() {
        val heavy = buildList {
            for (v in Catalog.all) {
                val tris = v.parts.sumOf { it.mesh.triangleCount } +
                    v.modules.sumOf { it.mesh.triangleCount }
                if (tris > 180_000) add("${v.id}: $tris triangles")
            }
        }
        assertTrue("vehicles over the triangle budget:\n" + heavy.joinToString("\n"), heavy.isEmpty())
    }

    @Test
    fun `mirrored parts reach both sides of the vehicle`() {
        // A tank's track is the clearest case: it must exist left and right of centre.
        val panzer = Catalog.byId("panzer_iv")!!
        val track = panzer.parts.first { it.id == "track" }
        assertTrue("track does not reach the left side", track.mesh.minZ < -50f)
        assertTrue("track does not reach the right side", track.mesh.maxZ > 50f)
    }

    @Test
    fun `internal modules are inside the vehicle they belong to`() {
        for (v in Catalog.all) {
            if (v.modules.isEmpty()) continue
            var hullMinX = Float.MAX_VALUE
            var hullMaxX = -Float.MAX_VALUE
            for (p in v.parts) {
                if (p.mesh.isEmpty) continue
                hullMinX = minOf(hullMinX, p.mesh.minX)
                hullMaxX = maxOf(hullMaxX, p.mesh.maxX)
            }
            for (m in v.modules) {
                assertTrue("${v.id}/${m.id} has no geometry", !m.mesh.isEmpty)
                assertTrue(
                    "${v.id}/${m.id} sits outside the vehicle",
                    m.mesh.minX >= hullMinX - 40f && m.mesh.maxX <= hullMaxX + 40f,
                )
            }
        }
    }

    /**
     * Every vehicle carries the full educational payload. This is the guard that keeps
     * a new vehicle from being added as geometry alone - the point of the catalogue is
     * as much what it explains as what it draws.
     */
    @Test
    fun `every vehicle has internals, history, specs and a quiz`() {
        val gaps = buildList {
            for (v in Catalog.all) {
                if (!v.hasCutaway) add("${v.id}: no internals")
                if (v.modules.size < 5) add("${v.id}: only ${v.modules.size} modules")
                if (v.history.length < 1200) add("${v.id}: history is ${v.history.length} chars")
                if (v.specs.size < 8) add("${v.id}: only ${v.specs.size} spec rows")
                if (v.quiz.size < 3) add("${v.id}: only ${v.quiz.size} questions")
            }
        }
        assertTrue("incomplete vehicles:\n" + gaps.joinToString("\n"), gaps.isEmpty())
    }

    @Test
    fun `every module explains itself`() {
        val gaps = buildList {
            for (v in Catalog.all) for (m in v.modules) {
                if (m.info.length < 60) add("${v.id}/${m.id}: info too short")
                if (m.detail.isBlank()) add("${v.id}/${m.id}: no detail line")
            }
        }
        assertTrue("modules missing explanation:\n" + gaps.joinToString("\n"), gaps.isEmpty())
    }

    @Test
    fun `every quiz question is answerable`() {
        val bad = buildList {
            for (v in Catalog.all) for ((i, q) in v.quiz.withIndex()) {
                if (q.answers.size < 3) add("${v.id} Q${i + 1}: too few answers")
                if (q.correct !in q.answers.indices) add("${v.id} Q${i + 1}: correct index out of range")
                if (q.because.length < 60) add("${v.id} Q${i + 1}: explanation too short")
                if (q.answers.toSet().size != q.answers.size) add("${v.id} Q${i + 1}: duplicate answers")
            }
        }
        assertTrue("malformed questions:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test
    fun `crew counts match the specification table`() {
        // The crew figures in the cutaway and the "Crew" spec row are written in
        // different files; a mismatch means one of them is wrong.
        val mismatched = buildList {
            for (v in Catalog.all) {
                val stated = v.specs.firstOrNull { it.label == "Crew" }?.value ?: continue
                val exact = stated.trim().toIntOrNull() ?: continue
                val modelled = v.modules.count { it.kind == ModuleKind.CREW }
                // Modules may group several people, so only flag the impossible case of
                // modelling more crew than the vehicle is stated to carry.
                if (modelled > exact) add("${v.id}: $modelled crew modules, spec says $exact")
            }
        }
        assertTrue("crew mismatches:\n" + mismatched.joinToString("\n"), mismatched.isEmpty())
    }
}
