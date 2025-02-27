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

package de.bixilon.minosoft.commands.parser.brigadier._float

import de.bixilon.kutil.bit.BitByte.isBitMask
import de.bixilon.minosoft.commands.parser.brigadier.BrigadierParser
import de.bixilon.minosoft.commands.parser.factory.ArgumentParserFactory
import de.bixilon.minosoft.commands.suggestion.Suggestion
import de.bixilon.minosoft.commands.util.CommandReader
import de.bixilon.minosoft.commands.util.StringReader
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class FloatParser(
    val min: Float = -Float.MAX_VALUE,
    val max: Float = Float.MAX_VALUE,
) : BrigadierParser<Float> {
    override val examples: List<Float> = listOf(1.0f, -1.0f, 1000.0f)

    override fun parse(reader: CommandReader): Float {
        val result = reader.readResult { reader.readFloat() }
        val float = result.result ?: throw FloatParseError(reader, result)
        if (float !in min..max) {
            throw FloatOutOfRangeError(reader, result, min, max)
        }

        return float
    }

    override fun getSuggestions(reader: CommandReader): List<Suggestion> {
        parse(reader)
        return emptyList()
    }


    companion object : ArgumentParserFactory<FloatParser> {
        override val identifier: ResourceLocation = "brigadier:float".toResourceLocation()
        val DEFAULT = FloatParser()

        override fun read(buffer: PlayInByteBuffer): FloatParser {
            val flags = buffer.readUnsignedByte()
            val min = if (flags.isBitMask(0x01)) buffer.readFloat() else -Float.MAX_VALUE
            val max = if (flags.isBitMask(0x03)) buffer.readFloat() else Float.MAX_VALUE
            return FloatParser(min = min, max = max)
        }

        fun StringReader.readFloat(): Float? {
            return readNumeric()?.toFloatOrNull()
        }

        fun StringReader.readRequiredFloat(): Float {
            readResult { readFloat() }.let { return it.result ?: throw FloatParseError(this, it) }
        }
    }
}
