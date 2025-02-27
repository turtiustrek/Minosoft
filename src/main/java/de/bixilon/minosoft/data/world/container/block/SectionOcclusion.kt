/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.world.container.block

import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.registries.blocks.cube.CubeDirections
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.FullOpaqueBlock
import de.bixilon.minosoft.data.registries.blocks.types.properties.shape.special.PotentialFullOpaqueBlock
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import it.unimi.dsi.fastutil.ints.IntOpenHashSet

class SectionOcclusion(
    private val provider: BlockSectionDataProvider,
) {
    private var occlusion = EMPTY

    fun clear(notify: Boolean) {
        update(EMPTY, notify)
    }

    fun onSet(previous: BlockState?, value: BlockState?) {
        if (previous.isFullyOpaque() == value.isFullyOpaque()) {
            return
        }
        recalculate(true)
    }

    fun recalculate(notify: Boolean) {
        if (provider.isEmpty) {
            clear(notify)
            return
        }
        update(calculateOcclusion(floodFill()), notify)
    }


    private fun floodFill(): ShortArray {
        // mark regions and check direct neighbours
        val regions = ShortArray(ProtocolDefinition.BLOCKS_PER_SECTION)

        fun trace(x: Int, y: Int, z: Int, nextId: Short) {
            val index = y shl 8 or (z shl 4) or x
            if (regions[index] > 0) {
                return
            }
            val state = provider[index]
            if (state.isFullyOpaque()) {
                return
            }
            regions[index] = nextId
            if (x > 0) trace(x - 1, y, z, nextId)
            if (x < ProtocolDefinition.SECTION_MAX_X) trace(x + 1, y, z, nextId)
            if (y > 0) trace(x, y - 1, z, nextId)
            if (y < ProtocolDefinition.SECTION_MAX_Y) trace(x, y + 1, z, nextId)
            if (z > 0) trace(x, y, z - 1, nextId)
            if (z < ProtocolDefinition.SECTION_MAX_Z) trace(x, y, z + 1, nextId)
        }

        var next: Short = 1
        for (y in 0 until ProtocolDefinition.SECTION_HEIGHT_Y) {
            for (z in 0 until ProtocolDefinition.SECTION_WIDTH_Z) {
                for (x in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    trace(x, y, z, next++)
                }
            }
        }

        return regions
    }

    private fun calculateOcclusion(regions: ShortArray): BooleanArray {
        val sideRegions: Array<IntOpenHashSet> = Array(Directions.SIZE) { IntOpenHashSet() }

        for (axis in Axes.VALUES) {
            for (a in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                for (b in 0 until ProtocolDefinition.SECTION_WIDTH_X) {
                    val indexPrefix = when (axis) {
                        Axes.X -> a shl 8 or (b shl 4)
                        Axes.Y -> (a shl 4) or b
                        Axes.Z -> (a shl 8) or b
                    }
                    val nDirection = when (axis) {
                        Axes.X -> Directions.WEST
                        Axes.Y -> Directions.DOWN
                        Axes.Z -> Directions.NORTH
                    }
                    val nRegion = regions[indexPrefix].toInt()
                    if (nRegion > 0) {
                        sideRegions[nDirection.ordinal] += nRegion
                    }

                    val pDirection = when (axis) {
                        Axes.X -> Directions.EAST
                        Axes.Y -> Directions.UP
                        Axes.Z -> Directions.SOUTH
                    }
                    val index2 = indexPrefix or when (axis) {
                        Axes.X -> ProtocolDefinition.SECTION_MAX_X
                        Axes.Y -> ProtocolDefinition.SECTION_MAX_Y shl 8
                        Axes.Z -> ProtocolDefinition.SECTION_MAX_Z shl 4
                    }
                    val pRegion = regions[index2].toInt()
                    if (pRegion > 0) {
                        sideRegions[pDirection.ordinal] += pRegion
                    }
                }
            }
        }

        val occlusion = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)
        for ((index, pair) in CubeDirections.PAIRS.withIndex()) {
            occlusion[index] = sideRegions.canOcclude(pair.`in`, pair.out)
        }
        return occlusion
    }

    private fun update(occlusion: BooleanArray, notify: Boolean) {
        if (this.occlusion.contentEquals(occlusion)) {
            return
        }
        this.occlusion = occlusion
        if (notify) {
            provider.section.chunk.world.occlusion++
        }
    }

    private fun Array<IntOpenHashSet>.canOcclude(`in`: Directions, out: Directions): Boolean {
        val inSides = this[`in`.ordinal]
        val outSides = this[`out`.ordinal]
        if (inSides.isEmpty() || outSides.isEmpty()) {
            return true
        }

        val preferIn = inSides.size < outSides.size
        val first = if (preferIn) inSides else outSides
        val second = if (preferIn) outSides else inSides

        for (region in first.intIterator()) {
            if (region in second) {
                return false
            }
        }
        return true
    }

    /**
     * If we can **not** look from `in` to `out`
     */
    fun isOccluded(`in`: Directions, out: Directions): Boolean {
        if (`in` == out) {
            return false
        }
        return occlusion[CubeDirections.getIndex(`in`, out)]
    }

    fun isOccluded(index: Int): Boolean {
        return occlusion[index]
    }

    companion object {
        private val EMPTY = BooleanArray(CubeDirections.CUBE_DIRECTION_COMBINATIONS)


        fun BlockState?.isFullyOpaque(): Boolean {
            if (this == null) {
                return false
            }
            val block = this.block
            if (block is FullOpaqueBlock) return true
            if (block !is PotentialFullOpaqueBlock) return false

            return block.isFullOpaque(this)
        }
    }
}
