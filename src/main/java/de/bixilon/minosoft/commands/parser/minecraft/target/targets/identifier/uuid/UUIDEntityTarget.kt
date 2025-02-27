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

package de.bixilon.minosoft.commands.parser.minecraft.target.targets.identifier.uuid

import de.bixilon.minosoft.commands.parser.minecraft.target.targets.CommandEntityTarget
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.world.WorldEntities
import java.util.*

class UUIDEntityTarget(
    val uuid: UUID,
) : CommandEntityTarget {

    override fun getEntities(executor: Entity?, entities: WorldEntities): List<Entity> {
        val entity = entities[uuid] ?: return emptyList()
        return listOf(entity)
    }

    override fun toString(): String {
        return "{$uuid}"
    }

    override fun hashCode(): Int {
        return uuid.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (other !is UUIDEntityTarget) {
            return false
        }
        return uuid == other.uuid
    }
}
