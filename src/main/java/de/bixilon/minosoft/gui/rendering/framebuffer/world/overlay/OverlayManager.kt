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

package de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay

import de.bixilon.minosoft.gui.rendering.RenderWindow
import de.bixilon.minosoft.gui.rendering.framebuffer.world.overlay.overlays.DefaultOverlays
import de.bixilon.minosoft.gui.rendering.renderer.Drawable

class OverlayManager(
    private val renderWindow: RenderWindow,
) : Drawable {
    private val overlays: MutableList<Overlay> = mutableListOf()

    fun init() {
        for ((index, factory) in DefaultOverlays.OVERLAYS.withIndex()) {
            overlays += factory.build(renderWindow, WORLD_FRAMEBUFFER_Z + (-0.01f * (index + 1)))
        }

        for (overlay in overlays) {
            overlay.init()
        }
    }

    fun postInit() {
        for (overlay in overlays) {
            overlay.postInit()
        }
    }

    override fun draw() {
        for (overlay in overlays) {
            overlay.update()
            if (!overlay.render) {
                continue
            }
            overlay.draw()
        }
    }

    companion object {
        private const val WORLD_FRAMEBUFFER_Z = -0.1f
    }
}
