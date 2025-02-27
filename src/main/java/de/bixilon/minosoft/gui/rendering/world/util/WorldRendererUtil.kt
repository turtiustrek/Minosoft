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

package de.bixilon.minosoft.gui.rendering.world.util

import de.bixilon.minosoft.data.world.chunk.ChunkSection
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3dUtil.isEmpty
import de.bixilon.minosoft.gui.rendering.world.WorldRenderer
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

object WorldRendererUtil {
    const val STILL_LOADING_TIME = 50L
    const val MOVING_LOADING_TIME = 20L


    // If the player is still, then we can load more chunks (to not cause lags)
    val WorldRenderer.maxBusyTime: Long get() = if (connection.player.physics.velocity.isEmpty()) STILL_LOADING_TIME else MOVING_LOADING_TIME // TODO: get of camera


    val ChunkSection.smallMesh: Boolean get() = blocks.count < ProtocolDefinition.SECTION_MAX_X * ProtocolDefinition.SECTION_MAX_Z
}
