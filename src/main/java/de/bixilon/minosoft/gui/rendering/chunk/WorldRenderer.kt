/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger, Lukas Eisenhauer
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.config.StaticConfiguration
import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.BlockState
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.world.*
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import de.bixilon.minosoft.util.logging.Log
import org.lwjgl.opengl.GL11.GL_CULL_FACE
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL13.glDisable
import java.util.concurrent.ConcurrentHashMap

class WorldRenderer(private val connection: Connection, private val world: World, val renderWindow: RenderWindow) : Renderer {
    private lateinit var minecraftTextures: TextureArray
    lateinit var chunkShader: Shader
    private val chunkSectionsToDraw = ConcurrentHashMap<ChunkPosition, ConcurrentHashMap<Int, ChunkMesh>>()
    private val visibleChunks: MutableSet<ChunkPosition> = mutableSetOf()
    private lateinit var frustum: Frustum
    private var currentTick = 0 // for animation usage
    private var lastTickIncrementTime = 0L

    private fun prepareChunk(chunkPosition: ChunkPosition, sectionHeight: Int, section: ChunkSection, chunk: Chunk): ChunkMesh {
        if (frustum.containsChunk(chunkPosition, connection)) {
            visibleChunks.add(chunkPosition)
        }
        val mesh = ChunkMesh()

        val below = world.chunks[chunkPosition]?.sections?.get(sectionHeight - 1)
        val above = world.chunks[chunkPosition]?.sections?.get(sectionHeight + 1)
        //val north = (world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        //val south = (world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?: throw ChunkNotLoadedException("South not loaded")).sections?.get(sectionHeight)
        //val west = (world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?: throw ChunkNotLoadedException("West not loaded")).sections?.get(sectionHeight)
        //val east = (world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        val north = world.chunks[chunkPosition.getLocationByDirection(Directions.NORTH)]?.sections?.get(sectionHeight)
        val south = world.chunks[chunkPosition.getLocationByDirection(Directions.SOUTH)]?.sections?.get(sectionHeight)
        val west = world.chunks[chunkPosition.getLocationByDirection(Directions.WEST)]?.sections?.get(sectionHeight)
        val east = world.chunks[chunkPosition.getLocationByDirection(Directions.EAST)]?.sections?.get(sectionHeight)

        for ((position, blockInfo) in section.blocks) {
            val blockBelow: BlockInfo? = if (position.y == 0 && below != null) {
                below.getBlockInfo(position.x, ProtocolDefinition.SECTION_HEIGHT_Y - 1, position.z)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.DOWN))
            }
            val blockAbove: BlockInfo? = if (position.y == ProtocolDefinition.SECTION_HEIGHT_Y - 1 && above != null) {
                above.getBlockInfo(position.x, 0, position.z)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.UP))
            }
            val blockNorth: BlockInfo? = if (position.z == 0 && north != null) {
                north.getBlockInfo(position.x, position.y, ProtocolDefinition.SECTION_WIDTH_Z - 1)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.NORTH))
            }
            val blockSouth: BlockInfo? = if (position.z == ProtocolDefinition.SECTION_WIDTH_Z - 1 && south != null) {
                south.getBlockInfo(position.x, position.y, 0)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.SOUTH))
            }
            val blockWest: BlockInfo? = if (position.x == 0 && west != null) {
                west.getBlockInfo(ProtocolDefinition.SECTION_WIDTH_X - 1, position.y, position.x)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.WEST))
            }
            val blockEast: BlockInfo? = if (position.x == ProtocolDefinition.SECTION_WIDTH_X - 1 && east != null) {
                east.getBlockInfo(0, position.y, position.z)
            } else {
                section.getBlockInfo(position.getLocationByDirection(Directions.EAST))
            }
            val blockPosition = BlockPosition(chunkPosition, sectionHeight, position)
            if (blockPosition == BlockPosition(-103, 3, 288)) {
                Log.debug("")
            }
            val biome = chunk.biomeAccessor!!.getBiome(blockPosition)

            var tintColor: RGBColor? = null
            if (StaticConfiguration.BIOME_DEBUG_MODE) {
                tintColor = RGBColor(biome.hashCode())
            } else {
                biome?.let {
                    biome.foliageColor?.let { tintColor = it }

                    blockInfo.block.owner.tint?.let { tint ->
                        tintColor = renderWindow.tintColorCalculator.calculateTint(tint, biome, blockPosition)
                    }
                }

                blockInfo.block.tintColor?.let { tintColor = it }
            }

            blockInfo.block.getBlockRenderer(blockPosition).render(blockInfo, chunk.lightAccessor!!, tintColor, blockPosition, mesh, arrayOf(blockBelow, blockAbove, blockNorth, blockSouth, blockWest, blockEast))
        }
        return mesh
    }

    override fun init() {
        minecraftTextures = TextureArray.createTextureArray(connection.version.assetsManager, resolveBlockTextureIds(connection.version.mapping.blockStateIdMap.values))
        minecraftTextures.load()


        chunkShader = Shader("chunk_vertex.glsl", "chunk_fragment.glsl")
        chunkShader.load()

    }

    override fun postInit() {
        minecraftTextures.use(chunkShader, "blockTextureArray")
    }

    override fun draw() {
        glEnable(GL_CULL_FACE)

        chunkShader.use()
        if (Minosoft.getConfig().config.game.animations.textures) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastTickIncrementTime >= ProtocolDefinition.TICK_TIME) {
                chunkShader.setInt("animationTick", currentTick++)
                lastTickIncrementTime = currentTime
            }
        }

        for ((chunkLocation, map) in chunkSectionsToDraw) {
            if (!visibleChunks.contains(chunkLocation)) {
                continue
            }
            for ((_, mesh) in map) {
                mesh.draw()
            }
        }
        glDisable(GL_CULL_FACE)
    }

    private fun resolveBlockTextureIds(blocks: Set<BlockState>): List<Texture> {
        val textures: MutableList<Texture> = mutableListOf()
        textures.add(TextureArray.DEBUG_TEXTURE)
        val textureMap: MutableMap<String, Texture> = mutableMapOf()
        textureMap[TextureArray.DEBUG_TEXTURE.name] = TextureArray.DEBUG_TEXTURE

        for (block in blocks) {
            for (model in block.renders) {
                model.resolveTextures(textures, textureMap)
            }
        }
        return textures
    }

    fun prepareChunk(chunkPosition: ChunkPosition, chunk: Chunk) {
        chunkSectionsToDraw[chunkPosition] = ConcurrentHashMap()
        if (!chunk.isFullyLoaded) {
            return
        }
        for ((sectionHeight, section) in chunk.sections!!) {
            prepareChunkSection(chunkPosition, sectionHeight, section, chunk)
        }
    }

    fun prepareChunkSection(chunkPosition: ChunkPosition, sectionHeight: Int, section: ChunkSection, chunk: Chunk) {
        renderWindow.rendering.executor.execute {
            val mesh = prepareChunk(chunkPosition, sectionHeight, section, chunk)

            var sectionMap = chunkSectionsToDraw[chunkPosition]
            if (sectionMap == null) {
                sectionMap = ConcurrentHashMap()
                chunkSectionsToDraw[chunkPosition] = sectionMap
            }
            renderWindow.renderQueue.add {
                mesh.load()
                sectionMap[sectionHeight]?.unload()
                sectionMap[sectionHeight] = mesh
            }
        }
    }

    fun clearChunkCache() {
        renderWindow.renderQueue.add {
            for ((location, map) in chunkSectionsToDraw) {
                for ((sectionHeight, mesh) in map) {
                    mesh.unload()
                    map.remove(sectionHeight)
                }
                chunkSectionsToDraw.remove(location)
            }
        }
    }

    fun unloadChunk(chunkPosition: ChunkPosition) {
        renderWindow.renderQueue.add {
            chunkSectionsToDraw[chunkPosition]?.let {
                for ((_, mesh) in it) {
                    mesh.unload()
                }
                chunkSectionsToDraw.remove(chunkPosition)
            }
        }
    }

    private fun prepareWorld(world: World) {
        for ((chunkLocation, chunk) in world.chunks) {
            prepareChunk(chunkLocation, chunk)
        }
    }

    fun refreshChunkCache() {
        clearChunkCache()
        prepareWorld(connection.player.world)
    }

    fun recalculateFrustum(frustum: Frustum) {
        visibleChunks.clear()
        this.frustum = frustum
        for ((chunkLocation, _) in chunkSectionsToDraw.entries) {
            if (frustum.containsChunk(chunkLocation, connection)) {
                visibleChunks.add(chunkLocation)
            }
        }
    }

    fun getChunkSize(): Int {
        return chunkSectionsToDraw.size
    }
}
