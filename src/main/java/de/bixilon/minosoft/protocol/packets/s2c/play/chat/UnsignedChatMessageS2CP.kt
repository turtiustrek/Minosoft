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
package de.bixilon.minosoft.protocol.packets.s2c.play.chat

import de.bixilon.minosoft.data.chat.message.FormattedChatMessage
import de.bixilon.minosoft.data.registries.chat.ChatParameter
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.modding.event.events.chat.ChatMessageEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = false)
class UnsignedChatMessageS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val text: ChatComponent = buffer.readChatComponent()
    val type = buffer.readRegistryItem(buffer.connection.registries.messageType)
    val parameters: Map<ChatParameter, ChatComponent> = mutableMapOf<ChatParameter, ChatComponent>().apply {
        buffer.readChatMessageParameters(this)
        this[ChatParameter.CONTENT] = text
    }

    override fun handle(connection: PlayConnection) {
        val message = FormattedChatMessage(connection, type, parameters)
        connection.events.fire(ChatMessageEvent(connection, message))
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, level = LogLevels.VERBOSE) { "Chat message (text=$text, parameters=$parameters)" }
    }
}
