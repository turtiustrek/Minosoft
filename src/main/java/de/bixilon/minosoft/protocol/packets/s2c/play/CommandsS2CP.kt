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
package de.bixilon.minosoft.protocol.packets.s2c.play

import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.kutil.cast.CastUtil.nullCast
import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.kutil.exception.Broken
import de.bixilon.minosoft.commands.nodes.CommandNode
import de.bixilon.minosoft.commands.nodes.NamedNode
import de.bixilon.minosoft.commands.nodes.RootNode
import de.bixilon.minosoft.commands.nodes.builder.ArgumentNodes
import de.bixilon.minosoft.commands.nodes.builder.CommandNodeBuilder
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactories
import de.bixilon.minosoft.commands.parser.minosoft.dummy.DummyParser
import de.bixilon.minosoft.commands.suggestion.factory.SuggestionFactories
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.s2c.PlayS2CPacket
import de.bixilon.minosoft.protocol.protocol.ProtocolVersions
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType

@LoadPacket(threadSafe = true)
class CommandsS2CP(buffer: PlayInByteBuffer) : PlayS2CPacket {
    val nodes = buffer.readArray { buffer.readCommandNode() }.build()
    val rootNode = nodes[buffer.readVarInt()].nullCast<RootNode>()


    override fun handle(connection: PlayConnection) {
        connection.rootNode = rootNode
    }

    fun Array<CommandNodeBuilder>.build(): Array<CommandNode> {
        val nodes: Array<CommandNode?> = arrayOfNulls(this.size)
        for ((index, builder) in this.withIndex()) {
            val node = builder.build()
            nodes[index] = node
        }

        for ((index, builder) in this.withIndex()) {
            val node = nodes[index] ?: Broken()
            builder.redirectNode?.let { node.redirect = nodes[it] }

            builder.children?.let {
                for ((childIndex, child) in it.withIndex()) {
                    node.addChild(nodes.getOrNull(child) ?: throw IllegalArgumentException("Invalid child: $child for $childIndex"))
                }
            }
        }

        return nodes.unsafeCast()
    }


    fun PlayInByteBuffer.readCommandNode(): CommandNodeBuilder {
        val builder = CommandNodeBuilder()
        val flags = readUnsignedByte()
        val type = ArgumentNodes.VALUES[flags and 0x03]
        builder.type = type
        builder.children = readVarIntArray()
        builder.redirectNode = if (flags.isBitMask(0x08)) readVarInt() else null
        builder.executable = flags.isBitMask(0x04)
        if (NamedNode::class.java.isAssignableFrom(type.klass.java)) {
            builder.name = readString()
        }
        if (type == ArgumentNodes.ARGUMENT) {
            val parserName = if (versionId >= ProtocolVersions.V_22W12A) connection.registries.argumentType[readVarInt()] else readResourceLocation()
            val parser = ArgumentParserFactories[parserName]
            if (parser == null) {
                Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Can not find parser: $parserName" }
                builder.parser = DummyParser
            } else {
                builder.parser = parser.read(this)
            }

            if (flags.isBitMask(0x10)) {
                val suggestionName = readResourceLocation()
                val suggestionType = SuggestionFactories[suggestionName]
                if (suggestionType == null) {
                    Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Can not find suggestion type: $suggestionName" }
                } else {
                    builder.suggestionType = suggestionType.build(this.connection, this)
                }
            }
        }

        return builder
    }

    override fun log(reducedLog: Boolean) {
        Log.log(LogMessageType.NETWORK_PACKETS_IN, LogLevels.VERBOSE) { "Commands (nodes=$nodes, rootNode=$rootNode)" }
    }
}
