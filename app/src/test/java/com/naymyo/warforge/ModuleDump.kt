package com.naymyo.warforge

import com.naymyo.warforge.data.Catalog
import org.junit.Test

/** Prints the internal modules each vehicle declares, and how big each one's mesh is. */
class ModuleDump {
    @Test
    fun dump() {
        for (v in Catalog.campaign) {
            val rows = v.modules.joinToString(" ") { "${it.id}:${it.kind.name.take(4)}:${it.mesh.triangleCount}" }
            println("%-16s %2d  %s".format(v.id, v.modules.size, rows))
        }
        println("TOTAL modules " + Catalog.campaign.sumOf { it.modules.size })
    }
}
