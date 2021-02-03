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

package de.bixilon.minosoft.data;

public enum Directions {
    DOWN,
    UP,
    NORTH,
    SOUTH,
    WEST,
    EAST;

    public static final Directions[] DIRECTIONS = values();

    public static Directions byId(int id) {
        return DIRECTIONS[id];
    }

    public Directions inverse() {
        var ordinal = ordinal();
        if (ordinal % 2 == 0) {
            return byId(ordinal + 1);
        }
        return byId(ordinal - 1);

    }
}
