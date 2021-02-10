package de.bixilon.minosoft.gui.rendering.chunk

import de.bixilon.minosoft.data.Directions
import de.bixilon.minosoft.data.mappings.blocks.Block
import de.bixilon.minosoft.data.world.Chunk
import de.bixilon.minosoft.data.world.ChunkLocation
import de.bixilon.minosoft.data.world.ChunkSection
import de.bixilon.minosoft.data.world.World
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.Renderer
import de.bixilon.minosoft.gui.rendering.shader.Shader
import de.bixilon.minosoft.gui.rendering.textures.Texture
import de.bixilon.minosoft.gui.rendering.textures.TextureArray
import de.bixilon.minosoft.protocol.network.Connection
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import glm_.vec3.Vec3
import org.lwjgl.opengl.GL11.GL_DEPTH_TEST
import org.lwjgl.opengl.GL11.glEnable
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import java.util.concurrent.ConcurrentHashMap

class ChunkRenderer(private val world: World, val renderWindow: RenderWindow) : Renderer {
    private lateinit var minecraftTextures: TextureArray
    lateinit var chunkShader: Shader
    private val chunkSectionsToDraw = ConcurrentHashMap<ChunkLocation, ConcurrentHashMap<Int, WorldMesh>>()

    private fun prepareChunk(chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection): FloatArray {
        val data: MutableList<Float> = mutableListOf()

        // ToDo: Greedy meshing!

        val below = world.allChunks[chunkLocation]?.sections?.get(sectionHeight - 1)
        val above = world.allChunks[chunkLocation]?.sections?.get(sectionHeight + 1)
        //val north = (world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        //val south = (world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?: throw ChunkNotLoadedException("South not loaded")).sections?.get(sectionHeight)
        //val west = (world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?: throw ChunkNotLoadedException("West not loaded")).sections?.get(sectionHeight)
        //val east = (world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?: throw ChunkNotLoadedException("North not loaded")).sections?.get(sectionHeight)
        val north = world.allChunks[chunkLocation.getLocationByDirection(Directions.NORTH)]?.sections?.get(sectionHeight)
        val south = world.allChunks[chunkLocation.getLocationByDirection(Directions.SOUTH)]?.sections?.get(sectionHeight)
        val west = world.allChunks[chunkLocation.getLocationByDirection(Directions.WEST)]?.sections?.get(sectionHeight)
        val east = world.allChunks[chunkLocation.getLocationByDirection(Directions.EAST)]?.sections?.get(sectionHeight)

        for ((position, block) in section.blocks) {
            val blockBelow: Block? = if (position.y == 0 && below != null) {
                below.getBlock(position.x, ProtocolDefinition.SECTION_HEIGHT_Y - 1, position.z)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.DOWN))
            }
            val blockAbove: Block? = if (position.y == ProtocolDefinition.SECTION_HEIGHT_Y - 1 && above != null) {
                above.getBlock(position.x, 0, position.z)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.UP))
            }
            val blockNorth: Block? = if (position.z == 0 && north != null) {
                north.getBlock(position.x, position.y, ProtocolDefinition.SECTION_WIDTH_Z - 1)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.NORTH))
            }
            val blockSouth: Block? = if (position.z == ProtocolDefinition.SECTION_WIDTH_Z - 1 && south != null) {
                south.getBlock(position.x, position.y, 0)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.SOUTH))
            }
            val blockWest: Block? = if (position.x == 0 && west != null) {
                west.getBlock(ProtocolDefinition.SECTION_WIDTH_X - 1, position.y, position.x)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.WEST))
            }
            val blockEast: Block? = if (position.x == ProtocolDefinition.SECTION_WIDTH_X - 1 && east != null) {
                east.getBlock(0, position.y, position.x)
            } else {
                section.getBlock(position.getLocationByDirection(Directions.EAST))
            }

            //  if(block.fullIdentifier != "minecraft:dropper"){
            //      continue
            //  }

            block.blockModel.render(position, data, arrayOf(blockBelow, blockAbove, blockNorth, blockSouth, blockWest, blockEast))
        }
        return data.toFloatArray()
    }

    override fun init(connection: Connection) {
        minecraftTextures = TextureArray.createTextureArray(connection.version.assetsManager, resolveBlockTextureIds(connection.version.mapping.blockMap.values), 16, 16) // ToDo :Remove fixed size
        minecraftTextures.load()

        chunkShader = Shader("chunk_vertex.glsl", "chunk_fragment.glsl")
        chunkShader.load()
        chunkShader.use()
    }

    override fun draw() {
        glEnable(GL_DEPTH_TEST)
        minecraftTextures.use(GL_TEXTURE0)

        chunkShader.use()

        for ((_, map) in chunkSectionsToDraw) {
            for ((_, mesh) in map) {
                mesh.draw(chunkShader)
            }
        }
    }

    private fun resolveBlockTextureIds(blocks: Set<Block>): List<Texture> {
        val textures: MutableList<Texture> = mutableListOf()
        textures.add(TextureArray.DEBUG_TEXTURE)
        val textureMap: MutableMap<String, Texture> = mutableMapOf()
        textureMap[TextureArray.DEBUG_TEXTURE.name] = TextureArray.DEBUG_TEXTURE

        for (block in blocks) {
            block.blockModel?.resolveTextures(textures, textureMap)
        }
        return textures
    }

    fun prepareChunk(chunkLocation: ChunkLocation, chunk: Chunk) {
        chunkSectionsToDraw[chunkLocation] = ConcurrentHashMap()
        for ((sectionHeight, section) in chunk.sections) {
            prepareChunkSection(chunkLocation, sectionHeight, section)
        }
    }

    fun prepareChunkSection(chunkLocation: ChunkLocation, sectionHeight: Int, section: ChunkSection) {
        renderWindow.rendering.executor.execute {
            renderWindow.rendering.latch.waitUntilZero() // Wait until rendering is started
            val data = prepareChunk(chunkLocation, sectionHeight, section)
            val sectionMap = chunkSectionsToDraw[chunkLocation]!!
            renderWindow.renderQueue.add {
                val newMesh = WorldMesh(data, Vec3(chunkLocation.x, sectionHeight, chunkLocation.z))
                sectionMap[sectionHeight]?.unload()
                sectionMap[sectionHeight] = newMesh
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
}
