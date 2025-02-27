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
package de.bixilon.minosoft.data.entities.entities.display

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.data.EntityData
import de.bixilon.minosoft.data.entities.data.EntityDataField
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.data.registries.identified.Namespaces.minecraft
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class TextDisplayEntity(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation) : DisplayEntity(connection, entityType, data, position, rotation) {

    companion object : EntityFactory<TextDisplayEntity> {
        override val identifier: ResourceLocation = minecraft("text_display")
        private val TEXT = EntityDataField("TEXT")
        private val LINE_WIDTH = EntityDataField("LINE_WIDTH")
        private val BACKGROUND = EntityDataField("BACKGROUND")
        private val TEXT_OPACITY = EntityDataField("TEXT_OPACITY")
        private val TEXT_DISPLAY_FLAGS = EntityDataField("TEXT_DISPLAY_FLAGS")

        override fun build(connection: PlayConnection, entityType: EntityType, data: EntityData, position: Vec3d, rotation: EntityRotation): TextDisplayEntity {
            return TextDisplayEntity(connection, entityType, data, position, rotation)
        }
    }
}
