package com.naymyo.warforge

import com.naymyo.warforge.data.Catalog
import org.junit.Test

/**
 * Prints what a Blender model has to match for each vehicle: the part ids it must name,
 * and where the pre-placed body sits on the blueprint. Not an assertion - a lookup, so
 * the scale and origin of a new model are read off rather than guessed.
 */
class BlueprintDump {
    @Test
    fun dump() {
        for (v in Catalog.campaign) {
            val base = v.parts.firstOrNull { it.preplaced } ?: continue
            val m = base.mesh
            var lo = Float.MAX_VALUE; var hi = -Float.MAX_VALUE
            for (p in v.parts) {
                if (p.mesh.isEmpty) continue
                lo = minOf(lo, p.mesh.minX); hi = maxOf(hi, p.mesh.maxX)
            }
            println(
                "%-16s %-5s %-6s base=%s x %.0f..%.0f  y %.0f..%.0f  all-x %.0f..%.0f  parts=%s"
                    .format(
                        v.id, v.era.name, v.branch.name, base.id,
                        m.minX, m.maxX, -m.maxY, -m.minY, lo, hi,
                        v.parts.joinToString(",") { it.id },
                    ),
            )
        }
    }
}
