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

package de.bixilon.minosoft.data.registries.blocks.types.pixlyzer

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.registries.blocks.factory.PixLyzerBlockFactory
import de.bixilon.minosoft.data.registries.blocks.properties.BlockProperties.Companion.isLit
import de.bixilon.minosoft.data.registries.blocks.state.BlockState
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.particle.data.DustParticleData
import de.bixilon.minosoft.data.registries.registries.Registries
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.gui.rendering.particle.types.render.texture.simple.dust.DustParticle
import de.bixilon.minosoft.gui.rendering.util.VecUtil.of
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.EMPTY
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import java.util.*

open class RedstoneTorchBlock(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>) : TorchBlock(resourceLocation, registries, data) {
    private val redstoneDustParticle = registries.particleType[DustParticle]

    override fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {
        if (!blockState.isLit()) {
            return
        }

        (flameParticle ?: redstoneDustParticle)?.let { connection.world += it.factory?.build(connection, Vec3d(blockPosition) + Vec3d(0.5, 0.7, 0.5) + (Vec3d.of { random.nextDouble() - 0.5 } * 0.2), Vec3d.EMPTY, DustParticleData(Colors.TRUE_RED, 1.0f, it)) }
    }

    companion object : PixLyzerBlockFactory<RedstoneTorchBlock> {

        override fun build(resourceLocation: ResourceLocation, registries: Registries, data: Map<String, Any>): RedstoneTorchBlock {
            return RedstoneTorchBlock(resourceLocation, registries, data)
        }
    }
}
