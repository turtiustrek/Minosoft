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

package de.bixilon.minosoft.commands.parser.brigadier._long

import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.protocol.protocol.PlayInByteBuffer
import de.bixilon.minosoft.util.BitByte.isBitMask
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class LongParser(
    val min: Long = Long.MIN_VALUE,
    val max: Long = Long.MAX_VALUE,
) : BrigadierParser<Long> {
    override val examples: List<Long> = listOf(1L, -1L, 1000L)
    override val placeholder = ChatComponent.of("<long>")

    override fun parse(reader: CommandReader): Long {
        val result = reader.readResult { reader.readLong() }
        val long = result.result ?: throw LongParseError(reader, result)
        if (long !in min..max) {
            throw LongOutOfRangeError(reader, result, min, max)
        }

        return long
    }

    override fun getSuggestions(reader: CommandReader): List<Long> {
        if (reader.readString()?.isBlank() != false) {
            return examples
        }
        return emptyList()
    }

    companion object : ArgumentParserFactory<LongParser> {
        override val RESOURCE_LOCATION: ResourceLocation = "brigadier:long".toResourceLocation()

        override fun read(buffer: PlayInByteBuffer): LongParser {
            val flags = buffer.readUnsignedByte()
            val min = if (flags.isBitMask(0x01)) buffer.readLong() else Long.MIN_VALUE
            val max = if (flags.isBitMask(0x03)) buffer.readLong() else Long.MAX_VALUE
            return LongParser(min = min, max = max)
        }

        fun CommandReader.readLong(): Long? {
            return readNumeric(decimal = false)?.toLongOrNull()
        }

        fun CommandReader.readRequiredLong(): Long {
            readResult { readLong() }.let { return it.result ?: throw LongParseError(this, it) }
        }
    }
}
