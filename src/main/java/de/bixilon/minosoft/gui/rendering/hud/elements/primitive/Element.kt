/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program.If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.gui.rendering.hud.elements.primitive

import de.bixilon.minosoft.gui.rendering.hud.HUDCacheMesh
import glm_.mat4x4.Mat4
import glm_.vec2.Vec2i

abstract class Element(
    private var _start: Vec2i,
    initialCacheSize: Int = 1000,
) {
    var start: Vec2i
        get() {
            return _start
        }
        set(value) {
            _start = value
            parent?.recalculateSize()
            clearCache()
        }

    val cache = HUDCacheMesh(initialCacheSize)
    open var parent: Element? = null
    var size: Vec2i = Vec2i()

    abstract fun recalculateSize()

    fun needsCacheUpdate(): Boolean {
        return cache.isEmpty()
    }

    fun checkCache(start: Vec2i, scaleFactor: Float, matrix: Mat4, z: Int = 1) {
        if (!needsCacheUpdate()) {
            return
        }
        prepareCache(start, scaleFactor, matrix, z)
    }

    open fun clearCache() {
        cache.clear()
    }

    abstract fun prepareCache(start: Vec2i, scaleFactor: Float, matrix: Mat4, z: Int = 1)
}
