package com.maddog.warforge

import com.maddog.warforge.data.Catalog
import com.maddog.warforge.data.VehicleDef
import com.maddog.warforge.game.LevelState
import com.maddog.warforge.game.Stage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class LevelStateTest {

    private val panzer = Catalog.byId("panzer_iv")!!

    /**
     * A vehicle with no cutaway data. Built here rather than picked from the catalogue,
     * because every catalogue entry is expected to grow internals eventually and this
     * case still has to work.
     */
    private val plain = Catalog.byId("sopwith_camel")!!.let { source ->
        VehicleDef(
            id = source.id, name = source.name, era = source.era, branch = source.branch,
            country = source.country, year = source.year, fact = source.fact,
            palette = source.palette, parts = source.parts, decoys = 0,
        )
    }

    /** Fits everything, stage by stage, dropping each piece on its own slot. */
    private fun buildAll(level: LevelState, assertEach: Boolean = true) {
        var guard = 0
        while (!level.isComplete && guard++ < 400) {
            val item = level.tray.firstOrNull { !it.decoy } ?: break
            val ok = level.tryPlace(item, item.slot.id)
            if (assertEach) assertTrue("${item.name} would not fit", ok)
        }
    }

    @Test
    fun `the tray only offers the current stage`() {
        val level = LevelState(panzer, random = Random(1))
        val stage = level.stage
        assertNotNull(stage)
        assertTrue(level.tray.isNotEmpty())
        assertTrue(level.tray.all { it.slot.stage == stage })
    }

    @Test
    fun `a tank with internals starts with its powerpack`() {
        val level = LevelState(panzer, random = Random(2))
        assertEquals(Stage.POWERPACK, level.stage)
        assertTrue(
            "the engine should be in the first tray",
            level.tray.any { it.slot.id == "mod_engine" },
        )
        assertTrue("internals are fitted before anything is bolted on",
            level.tray.filter { !it.decoy }.all { it.slot.internal })
    }

    @Test
    fun `a vehicle without internals starts on its structure`() {
        val level = LevelState(plain, random = Random(3))
        assertEquals(Stage.HULL, level.stage)
        assertFalse(level.stages.contains(Stage.POWERPACK))
    }

    @Test
    fun `preplaced parts start fitted and are not in the tray`() {
        val level = LevelState(panzer, random = Random(4))
        val foundation = panzer.parts.first { it.preplaced }
        assertTrue(level.isPlaced(foundation.id))
        assertTrue(level.tray.none { it.slot.id == foundation.id })
    }

    @Test
    fun `dropping on the right slot fits the piece`() {
        val level = LevelState(panzer, random = Random(5))
        val item = level.tray.first { !it.decoy }
        assertTrue(level.tryPlace(item, item.slot.id))
        assertTrue(level.isPlaced(item.slot.id))
        assertEquals(0, level.mistakes)
    }

    @Test
    fun `dropping on the wrong slot is a mistake and keeps the piece`() {
        val level = LevelState(panzer, random = Random(6))
        val real = level.tray.filter { !it.decoy }
        val item = real[0]
        val other = real[1]
        assertFalse(level.tryPlace(item, other.slot.id))
        assertEquals(1, level.mistakes)
        assertFalse(level.isPlaced(item.slot.id))
        assertTrue(level.tray.contains(item))
    }

    @Test
    fun `dropping on nothing is a mistake`() {
        val level = LevelState(panzer, random = Random(7))
        val item = level.tray.first { !it.decoy }
        assertFalse(level.tryPlace(item, null))
        assertEquals(1, level.mistakes)
    }

    @Test
    fun `a decoy fits nowhere, even on a real slot`() {
        val decoys = Catalog.decoysFor(panzer, 2, Random(8))
        val level = LevelState(panzer, decoys, Random(8))
        // Decoys are spread across stages; find one wherever it landed.
        var guard = 0
        while (level.tray.none { it.decoy } && guard++ < 40) {
            val item = level.tray.first { !it.decoy }
            level.tryPlace(item, item.slot.id)
        }
        val decoy = level.tray.firstOrNull { it.decoy }
        assertNotNull("no decoy ever reached the tray", decoy)
        val real = level.tray.first { !it.decoy }
        val before = level.mistakes
        assertFalse(level.tryPlace(decoy!!, real.slot.id))
        assertEquals(before + 1, level.mistakes)
        assertTrue(level.tray.contains(decoy))
    }

    @Test
    fun `stages run in order and end complete`() {
        val level = LevelState(panzer, Catalog.decoysFor(panzer, 2, Random(9)), Random(9))
        val seen = ArrayList<Stage>()
        var guard = 0
        while (!level.isComplete && guard++ < 400) {
            level.stage?.let { if (seen.lastOrNull() != it) seen += it }
            val item = level.tray.first { !it.decoy }
            assertTrue(level.tryPlace(item, item.slot.id))
        }
        assertTrue(level.isComplete)
        assertEquals(0, level.remaining)
        assertEquals(1f, level.progress, 0.001f)
        assertEquals(3, level.stars())
        // Stages only ever move forward.
        assertEquals(seen.sortedBy { it.ordinal }, seen)
        assertTrue("a Panzer IV build should cover several stages", seen.size >= 4)
    }

    @Test
    fun `internals are fitted before the armour that covers them`() {
        val level = LevelState(panzer, random = Random(10))
        val order = ArrayList<String>()
        var guard = 0
        while (!level.isComplete && guard++ < 400) {
            val item = level.tray.first { !it.decoy }
            order += item.slot.id
            level.tryPlace(item, item.slot.id)
        }
        val engine = order.indexOf("mod_engine")
        val armour = order.indexOf("mod_armour_front")
        val turret = order.indexOf("turret")
        assertTrue("engine goes in first", engine in 0 until armour)
        assertTrue("armour before the turret", armour in 0 until turret)
    }

    @Test
    fun `mistakes and hints cost stars`() {
        val level = LevelState(panzer, random = Random(11))
        val item = level.tray.first { !it.decoy }
        repeat(30) { level.tryPlace(item, null) }
        assertTrue(level.stars() < 3)
        assertTrue(level.coinReward() >= 10)
    }

    @Test
    fun `hint target is always a real piece in the current stage`() {
        val level = LevelState(panzer, Catalog.decoysFor(panzer, 2, Random(12)), Random(12))
        val target = level.hintTarget()
        assertNotNull(target)
        assertFalse(target!!.decoy)
        assertEquals(level.stage, target.slot.stage)
        assertEquals(1, level.hintsUsed)
    }

    @Test
    fun `every vehicle can be built end to end`() {
        for (v in Catalog.all) {
            val level = LevelState(v, Catalog.decoysFor(v, v.decoys, Random(13)), Random(13))
            buildAll(level)
            assertTrue("${v.id} did not complete", level.isComplete)
            assertEquals("${v.id} left mistakes behind", 0, level.mistakes)
        }
    }

    @Test
    fun `blueprint drops still work for vehicles without internals`() {
        val level = LevelState(plain, random = Random(14))
        val item = level.tray.first { !it.decoy }
        assertTrue(level.tryPlace(item, item.slot.bx, item.slot.by))
        assertTrue(level.isPlaced(item.slot.id))
    }
}
