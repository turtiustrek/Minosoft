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
package de.bixilon.minosoft.protocol.packets.s2c.status

import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnectionStates
import de.bixilon.minosoft.protocol.packets.c2s.status.PingC2SP
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.StatusS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolStates
import de.bixilon.minosoft.protocol.protocol.buffers.InByteBuffer
import de.bixilon.minosoft.protocol.status.ServerStatus
import de.bixilon.minosoft.protocol.status.StatusPing
import de.bixilon.minosoft.protocol.versions.Version
import de.bixilon.minosoft.protocol.versions.Versions
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType
import java.util.concurrent.ThreadLocalRandom

@LoadPacket(state = ProtocolStates.STATUS)
class StatusS2CP(buffer: InByteBuffer) : StatusS2CPacket {
    val status: ServerStatus = ServerStatus(buffer.readJson())

    override fun handle(connection: StatusConnection) {
        val version: Version? = Versions.getByProtocol(status.protocolId ?: -1)
        if (version == null) {
            Log.log(LogMessageType.NETWORK_STATUS, LogLevels.WARN) { "Server is running on unknown version (protocolId=${status.protocolId})" }
        } else {
            connection.serverVersion = version
        }

        connection.status = status
        val ping = StatusPing()
        connection.ping = ping
        connection.state = StatusConnectionStates.QUERYING_PING
        connection.sendPacket(PingC2SP(ThreadLocalRandom.current().nextLong()))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Server status response (status=$status)" }
    }
}
