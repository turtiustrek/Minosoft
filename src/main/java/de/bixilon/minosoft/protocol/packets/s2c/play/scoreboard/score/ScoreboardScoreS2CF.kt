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

package de.bixilon.minosoft.protocol.packets.s2c.play.scoreboard.score

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.factory.PacketDirection
import de.bixilon.minosoft.protocol.packets.factory.factories.PlayPacketFactory
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

@LoadPacket(threadSafe = false)
object ScoreboardScoreS2CF : PlayPacketFactory {
    override val direction = PacketDirection.SERVER_TO_CLIENT

    override fun createPacket(buffer: PlayInByteBuffer): ScoreboardScoreS2CP {
        val entity = buffer.readString()
        val action = ScoreboardScoreActions[buffer.readVarInt()]
        val objective = buffer.readNullString()
        return when (action) {
            ScoreboardScoreActions.PUT -> PutScoreboardScoreS2CP(entity, objective, buffer)
            ScoreboardScoreActions.REMOVE -> RemoveScoreboardScoreS2CP(entity, objective, buffer)
        }
    }

    enum class ScoreboardScoreActions {
        PUT,
        REMOVE,
        ;

        companion object : ValuesEnum<ScoreboardScoreActions> {
            override val VALUES: Array<ScoreboardScoreActions> = values()
            override val NAME_MAP: Map<String, ScoreboardScoreActions> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
