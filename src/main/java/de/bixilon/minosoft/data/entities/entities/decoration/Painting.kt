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
package de.bixilon.minosoft.data.entities.entities.decoration

import de.bixilon.kotlinglm.vec3.Vec3d
import de.bixilon.kotlinglm.vec3.Vec3i
import de.bixilon.minosoft.data.direction.Directions
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.data.entities.entities.Entity
import de.bixilon.minosoft.data.entities.entities.SynchronizedEntityData
import de.bixilon.minosoft.data.registries.Motive
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.entities.EntityFactory
import de.bixilon.minosoft.data.registries.entities.EntityType
import de.bixilon.minosoft.gui.rendering.util.VecUtil.entityPosition
import de.bixilon.minosoft.gui.rendering.util.vec.vec3.Vec3iUtil.toVec3i
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection

class Painting(
    connection: PlayConnection,
    entityType: EntityType,
    position: Vec3i,
    @get:SynchronizedEntityData(name = "Direction") val direction: Directions,
    @get:SynchronizedEntityData(name = "Motive") val motive: Motive?,
) : Entity(connection, entityType, position.entityPosition, EntityRotation(0.0f, 0.0f)) {

    companion object : EntityFactory<Painting> {
        override val RESOURCE_LOCATION: ResourceLocation = ResourceLocation("painting")

        override fun build(connection: PlayConnection, entityType: EntityType, position: Vec3d, rotation: EntityRotation): Painting {
            return Painting(connection, entityType, position.toVec3i(), Directions.NORTH, null) // ToDo: Get data from entity data (22w16a+)
        }
    }
}
