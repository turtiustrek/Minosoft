/*
 * Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.data.entities.entities;

import de.bixilon.minosoft.data.entities.EntityRotation;
import de.bixilon.minosoft.data.entities.Location;
import de.bixilon.minosoft.protocol.network.Connection;

import java.util.UUID;

public class ExperienceOrb extends Entity {
    private final int count;

    public ExperienceOrb(Connection connection, int entityId, UUID uuid, Location location, EntityRotation rotation) {
        super(connection, entityId, uuid, location, rotation);
        this.count = 0;
    }

    public ExperienceOrb(Connection connection, int entityId, Location location, int count) {
        super(connection, entityId, UUID.randomUUID(), location, new EntityRotation(0, 0, 0));
        this.count = count;
    }

    @EntityMetaDataFunction(identifier = "Count")
    public int getCount() {
        return this.count;
    }
}
