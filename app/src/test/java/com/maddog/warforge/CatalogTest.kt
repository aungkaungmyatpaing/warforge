package com.maddog.warforge

import com.maddog.warforge.data.BP_H
import com.maddog.warforge.data.BP_W
import com.maddog.warforge.data.Branch
import com.maddog.warforge.data.Catalog
import com.maddog.warforge.data.Era
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Guards on the catalog data itself. The geometry is hand-authored, so these catch the
 * mistakes that are invisible until a part turns out to be unreachable on a phone.
 */
class CatalogTest {

    /** A few units of slack for stroke widths and rounding. */
    private val MARGIN = 8f

    @Test
    fun `every vehicle id is unique`() {
        val ids = Catalog.all.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test
    fun `part ids are unique inside a vehicle`() {
        for (v in Catalog.all) {
            val ids = v.parts.map { it.id }
            assertEquals("duplicate part id in ${v.id}", ids.size, ids.toSet().size)
        }
    }

    @Test
    fun `every vehicle has one preplaced foundation and enough loose parts`() {
        for (v in Catalog.all) {
            assertEquals("${v.id} needs exactly one preplaced part", 1, v.parts.count { it.preplaced })
            assertTrue("${v.id} has too few parts", v.partCount >= 5)
        }
    }

    /**
     * The board letterboxes exactly the blueprint rect, so anything outside it is drawn
     * off-screen. Collects every offender rather than stopping at the first, because
     * fixing hand-authored geometry one assertion at a time is miserable.
     */
    @Test
    fun `no part escapes the blueprint`() {
        val bad = buildList {
            for (v in Catalog.all) for (p in v.parts) {
                if (p.minX < -MARGIN || p.maxX > BP_W + MARGIN ||
                    p.minY < -MARGIN || p.maxY > BP_H + MARGIN
                ) {
                    add(
                        "${v.id}/${p.id} x=[${p.minX.toInt()},${p.maxX.toInt()}] " +
                            "y=[${p.minY.toInt()},${p.maxY.toInt()}]"
                    )
                }
            }
        }
        assertTrue("parts outside the blueprint:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test
    fun `no part is degenerate`() {
        for (v in Catalog.all) {
            for (p in v.parts) {
                assertTrue("${v.id}/${p.id} has no facets", p.polys.isNotEmpty())
                assertTrue("${v.id}/${p.id} is a point", p.width > 1f || p.height > 1f)
                for (poly in p.polys) {
                    assertTrue("${v.id}/${p.id} facet needs 3+ points", poly.pts.size >= 6)
                    assertEquals("${v.id}/${p.id} facet has a stray coordinate", 0, poly.pts.size % 2)
                }
            }
        }
    }

    /**
     * A part snaps to its own home, so overlapping homes are fine - a tank's wheels and
     * its track really do occupy the same place. What matters is that every home sits
     * where a thumb can actually reach it.
     */
    @Test
    fun `every home position is reachable on the board`() {
        val bad = buildList {
            for (v in Catalog.all) for (p in v.loose) {
                if (p.homeX !in 0f..BP_W || p.homeY !in 0f..BP_H) {
                    add("${v.id}/${p.id} home=(${p.homeX.toInt()},${p.homeY.toInt()})")
                }
            }
        }
        assertTrue("unreachable slots:\n" + bad.joinToString("\n"), bad.isEmpty())
    }

    @Test
    fun `campaign covers every vehicle and starts in WWI`() {
        assertEquals(Catalog.all.size, Catalog.campaign.size)
        assertEquals(Catalog.all.toSet(), Catalog.campaign.toSet())
        assertEquals(Era.WWI, Catalog.campaign.first().era)
        // Eras never go backwards through the campaign.
        var last = -1
        for (v in Catalog.campaign) {
            val ord = v.era.ordinal
            assertTrue("era order broken at ${v.id}", ord >= last)
            last = ord
        }
    }

    @Test
    fun `every era and branch is represented`() {
        for (era in Era.entries) {
            assertTrue("no vehicles in $era", Catalog.inEra(era).isNotEmpty())
        }
        for (branch in Branch.entries) {
            assertTrue("no $branch vehicles", Catalog.all.any { it.branch == branch })
        }
    }

    @Test
    fun `decoys never duplicate a real part name`() {
        for (v in Catalog.all) {
            val decoys = Catalog.decoysFor(v, v.decoys)
            assertEquals(v.decoys, decoys.size)
            val real = v.parts.map { it.name }.toSet()
            for (d in decoys) assertTrue("${v.id} got decoy ${d.name}", d.name !in real)
        }
    }

    @Test
    fun `lookup finds every vehicle`() {
        for (v in Catalog.all) {
            assertNotNull(Catalog.byId(v.id))
            assertTrue(Catalog.indexOf(v.id) >= 0)
        }
    }
}
