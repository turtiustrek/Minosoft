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
package de.bixilon.minosoft.protocol.packets.s2c.play.entity.spawn

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.entities.LightningBolt
import de.bixilon.minosoft.modding.event.events.EntitySpawnEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.util.KUtil.startInit
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class GlobalEntitySpawnS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val entityId: Int = buffer.readVarInt()
    val type = buffer.readByte()
    val entity: LightningBolt

    init {
        val position: Vec3d = buffer.readVec3d()

        entity = LightningBolt(buffer.connection, buffer.connection.registries.entityType[LightningBolt.identifier]!!, EntityData(buffer.connection), position)
        entity.startInit()
    }

    override fun handle(connection: PlayConnection) {
        connection.world.entities.add(entityId, null, entity)

        connection.events.fire(EntitySpawnEvent(connection, this))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Global entity spawn (entityId=$entity, type=$type, entity=$entity)" }
    }
}
