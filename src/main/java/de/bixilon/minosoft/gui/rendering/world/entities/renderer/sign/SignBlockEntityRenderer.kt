/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.world.entities.renderer.sign

import de.bixilon.kotlinglm.func.rad
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.minosoft.data.Axes
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.block.SignBlockEntity
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties
import de.bixilon.minosoft.data.registries.blocks.types.entity.sign.StandingSignBlock
import de.bixilon.minosoft.data.registries.blocks.types.entity.sign.WallSignBlock
import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.font.renderer.ChatComponentRenderer
import de.bixilon.minosoft.gui.rendering.models.unbaked.element.UnbakedElement
import de.bixilon.minosoft.gui.rendering.util.VecUtil.toVec3
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3Util.rotateAssign
import de.bixilon.minosoft.gui.rendering.world.entities.MeshedBlockEntityRenderer
import de.bixilon.minosoft.gui.rendering.world.mesh.WorldMesh
import java.util.*

class SignBlockEntityRenderer(
    val sign: SignBlockEntity,
    val renderWindow: RenderWindow,
    override val blockState: BlockState,
) : MeshedBlockEntityRenderer<SignBlockEntity> {

    override fun singleRender(position: Vec3i, mesh: WorldMesh, random: Random, blockState: BlockState, neighbours: Array<BlockState?>, light: ByteArray, ambientLight: FloatArray, tints: IntArray?): Boolean {
        val block = this.blockState.block
        if (block is StandingSignBlock) {
            // println("Rendering standing sign at $position (${block.resourceLocation})")
        } else if (block is WallSignBlock) {
            renderWallText(position, mesh, blockState)
        }

        return true
    }

    private fun renderWallText(position: Vec3i, mesh: WorldMesh, blockState: BlockState) {
        val yRotation = when (val rotation = this.blockState.properties[BlockProperties.FACING].nullCast<Directions>() ?: Directions.NORTH) {
            Directions.SOUTH -> 0.0f
            Directions.EAST -> 90.0f
            Directions.NORTH -> 180.0f
            Directions.WEST -> 270.0f
            else -> TODO("Sign can not have this facing: $rotation")
        }

        val rotationVector = Vec3(PIXEL_SCALE * 5, 0.75f, 2.0f / UnbakedElement.BLOCK_RESOLUTION + 0.001f)
        rotationVector.signRotate(-yRotation.rad)
        val textPosition = position.toVec3 + rotationVector

        for (line in sign.lines) {
            ChatComponentRenderer.render3dFlat(renderWindow, textPosition, TEXT_SCALE, Vec3(0.0f, yRotation, 0.0f), mesh, line)
            textPosition.y -= 0.11f
        }
    }

    private fun Vec3.signRotate(yRotation: Float) {
        this -= 0.5f

        rotateAssign(yRotation, Axes.Y)

        this += 0.5f
    }

    companion object {
        private const val PIXEL_SCALE = 1.0f / ChatComponentRenderer.TEXT_BLOCK_RESOLUTION
        private const val TEXT_SCALE = 1.2f
    }
}
