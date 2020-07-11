/*
 * Codename Minosoft
 * Copyright (C) 2020 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 *  This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.game.datatypes.inventory;

import de.bixilon.minosoft.game.datatypes.TextComponent;
import de.bixilon.minosoft.game.datatypes.entities.Items;
import de.bixilon.minosoft.nbt.tag.CompoundTag;
import de.bixilon.minosoft.protocol.protocol.ProtocolVersion;

public class Slot {
    String identifier;
    int itemCount;
    short itemMetadata;
    CompoundTag nbt;

    public Slot(String identifier, int itemCount, CompoundTag nbt) {
        this.identifier = identifier;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public Slot(String identifier, byte itemCount, short itemMetadata, CompoundTag nbt) {
        this.identifier = identifier;
        this.itemMetadata = itemMetadata;
        this.itemCount = itemCount;
        this.nbt = nbt;
    }

    public String getIdentifier() {
        return this.identifier;
    }

    public int getItemId(ProtocolVersion version) {
        return Items.getItemId(identifier, version);
    }

    public int getItemCount() {
        return itemCount;
    }

    public short getItemMetadata() {
        return itemMetadata;
    }

    public CompoundTag getNbt() {
        return nbt;
    }

    public String getDisplayName() {
        if (nbt != null && nbt.containsKey("display") && nbt.getCompoundTag("display").containsKey("Name")) { // check if object has nbt data, and a custom display name
            return String.format("%s (%s)", new TextComponent(nbt.getCompoundTag("display").getStringTag("Name").getValue()).getColoredMessage(), identifier);
        }
        return identifier; // ToDo display name per Item (from language file)
    }
}
