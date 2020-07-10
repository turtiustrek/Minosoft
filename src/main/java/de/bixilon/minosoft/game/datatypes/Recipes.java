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

package de.bixilon.minosoft.game.datatypes;

public enum Recipes {
    TODO(0, new Identifier("todo"));

    final int id;
    final Identifier identifier;

    Recipes(int id, Identifier identifier) {
        this.id = id;
        this.identifier = identifier;
    }

    public static Recipes byId(int id) {
        return TODO;
    }

    public static Recipes byName(String name) {
        return TODO;
    }

    public int getId() {
        return id;
    }

    public Identifier getIdentifier() {
        return identifier;
    }
}
