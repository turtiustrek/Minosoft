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

package de.bixilon.minosoft.gui.rendering.gui.hud.elements.hotbar

import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.data.container.types.PlayerInventory
import de.bixilon.minosoft.gui.rendering.gui.GUIRenderer
import de.bixilon.minosoft.gui.rendering.gui.elements.Element
import de.bixilon.minosoft.gui.rendering.gui.elements.Pollable
import de.bixilon.minosoft.gui.rendering.gui.elements.items.ContainerItemsElement
import de.bixilon.minosoft.gui.rendering.gui.elements.primitive.AtlasImageElement
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexConsumer
import de.bixilon.minosoft.gui.rendering.gui.mesh.GUIVertexOptions
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class HotbarBaseElement(guiRenderer: GUIRenderer) : Element(guiRenderer), Pollable {
    private val baseAtlasElement = guiRenderer.atlasManager[BASE]!!
    private val base = AtlasImageElement(guiRenderer, baseAtlasElement)
    private val frame = AtlasImageElement(guiRenderer, guiRenderer.atlasManager[FRAME]!!, size = Vec2i(FRAME_SIZE))

    private val containerElement = ContainerItemsElement(guiRenderer, guiRenderer.context.connection.player.items.inventory, baseAtlasElement.slots)

    private var selectedSlot = 0

    init {
        size = HOTBAR_BASE_SIZE + Vec2i(HORIZONTAL_MARGIN * 2, 1) // offset left and right; offset for the frame is just on top, not on the bottom
        cacheUpToDate = false // ToDo: Check changes

        base.parent = this
        frame.parent = this
        containerElement.parent = this
    }

    override fun forceRender(offset: Vec2i, consumer: GUIVertexConsumer, options: GUIVertexOptions?) {
        base.render(offset + HORIZONTAL_MARGIN, consumer, options)

        baseAtlasElement.slots[selectedSlot + PlayerInventory.HOTBAR_OFFSET]?.let {
            frame.render(offset + it.start - HORIZONTAL_MARGIN + FRAME_OFFSET, consumer, options)
        }

        containerElement.render(offset + HORIZONTAL_MARGIN, consumer, options)
    }

    override fun poll(): Boolean {
        val selectedSlot = guiRenderer.context.connection.player.items.hotbar

        if (this.selectedSlot != selectedSlot || containerElement.silentApply()) {
            this.selectedSlot = selectedSlot
            return true
        }

        return false
    }

    override fun forceSilentApply() {
        containerElement.silentApply()
        cacheUpToDate = false
    }

    companion object {
        private val BASE = "minecraft:hotbar_base".toResourceLocation()
        private val FRAME = "minecraft:hotbar_frame".toResourceLocation()

        private val HOTBAR_BASE_SIZE = Vec2i(182, 22)
        private const val FRAME_SIZE = 24
        const val HORIZONTAL_MARGIN = 1
        private const val FRAME_OFFSET = -2 // FRAME_SIZE - HOTBAR_BASE_SIZE.y
    }
}
