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

package de.bixilon.minosoft.protocol.packets.s2c.play.combat

import de.bixilon.kutil.enums.EnumUtil
import de.bixilon.kutil.enums.ValuesEnum
import de.bixilon.minosoft.protocol.packets.factory.LoadPacket
import de.bixilon.minosoft.protocol.packets.factory.PacketDirection
import de.bixilon.minosoft.protocol.packets.factory.factories.PlayPacketFactory
import de.bixilon.minosoft.protocol.protocol.buffers.play.PlayInByteBuffer

@LoadPacket
object CombatEventS2CF : PlayPacketFactory {
    override val direction = PacketDirection.SERVER_TO_CLIENT

    override fun createPacket(buffer: PlayInByteBuffer): CombatEventS2CP {
        return when (CombatEvents[buffer.readVarInt()]) {
            CombatEvents.ENTER_COMBAT -> EnterCombatEventS2CP(buffer)
            CombatEvents.END_COMBAT -> EndCombatEventS2CP(buffer)
            CombatEvents.ENTITY_DEATH -> KillCombatEventS2CP(buffer)
        }
    }

    enum class CombatEvents {
        ENTER_COMBAT,
        END_COMBAT,
        ENTITY_DEATH,
        ;

        companion object : ValuesEnum<CombatEvents> {
            override val VALUES = values()
            override val NAME_MAP: Map<String, CombatEvents> = EnumUtil.getEnumValues(VALUES)
        }
    }
}
