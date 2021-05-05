/*
 * Minosoft
 * Copyright (C) 2021 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */
package de.bixilon.minosoft.data.mappings.effects

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.mappings.ResourceLocation
import de.bixilon.minosoft.data.mappings.registry.RegistryItem
import de.bixilon.minosoft.data.mappings.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.mappings.registry.Translatable
import de.bixilon.minosoft.data.mappings.versions.VersionMapping
import de.bixilon.minosoft.data.text.RGBColor
import de.bixilon.minosoft.data.text.RGBColor.Companion.asRGBColor
import de.bixilon.minosoft.datafixer.EntityAttributeFixer.fix

data class StatusEffect(
    override val resourceLocation: ResourceLocation,
    val category: StatusEffectCategories,
    override val translationKey: String?,
    val color: RGBColor,
    val attributes: Map<ResourceLocation, StatusEffectAttribute>,
) : RegistryItem, Translatable {

    override fun toString(): String {
        return resourceLocation.full
    }

    companion object : ResourceLocationDeserializer<StatusEffect> {
        override fun deserialize(mappings: VersionMapping?, resourceLocation: ResourceLocation, data: JsonObject): StatusEffect {
            val attributes: MutableMap<ResourceLocation, StatusEffectAttribute> = mutableMapOf()

            data["attributes"]?.asJsonObject?.let {
                for ((key, value) in it.entrySet()) {
                    attributes[ResourceLocation.getResourceLocation(key).fix()] = StatusEffectAttribute.deserialize(value.asJsonObject)
                }
            }

            return StatusEffect(
                resourceLocation = resourceLocation,
                category = StatusEffectCategories.NAME_MAP[data["category"].asString]!!,
                translationKey = data["translation_key"]?.asString,
                color = data["color"].asInt.asRGBColor(),
                attributes.toMap(),
            )
        }
    }
}
