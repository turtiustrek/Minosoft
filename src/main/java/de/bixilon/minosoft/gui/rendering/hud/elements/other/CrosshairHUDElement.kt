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

package de.bixilon.minosoft.gui.rendering.hud.elements.other

import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.gui.rendering.hud.HUDElementProperties
import de.bixilon.minosoft.gui.rendering.hud.HUDMesh
import de.bixilon.minosoft.gui.rendering.hud.HUDRenderer
import de.bixilon.minosoft.gui.rendering.hud.atlas.HUDAtlasElement
import de.bixilon.minosoft.gui.rendering.hud.elements.HUDElement
import glm_.vec2.Vec2

class CrosshairHUDElement(
    hudRender: HUDRenderer,
) : HUDElement(hudRender) {
    override val elementProperties = HUDElementProperties(
        position = Vec2(0f, 0f),
        xBinding = HUDElementProperties.PositionBindings.CENTER,
        yBinding = HUDElementProperties.PositionBindings.CENTER,
        scale = 1f,
        enabled = true,
    )

    private lateinit var crosshairAtlasElement: HUDAtlasElement

    private lateinit var crosshairRealSize: Vec2


    override fun init() {
        crosshairAtlasElement = hudRenderer.hudAtlasElements[ResourceLocation("minecraft:crosshair")]!!
        crosshairRealSize = crosshairAtlasElement.binding.size * hudRenderer.hudScale.scale * elementProperties.scale
    }


    override fun prepare(hudMesh: HUDMesh) {
        drawImage(getRealPosition(crosshairRealSize, elementProperties, RealTypes.START), getRealPosition(crosshairRealSize, elementProperties, RealTypes.END), hudMesh, crosshairAtlasElement, 1)
    }
}
