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
package de.bixilon.minosoft.data.registries.fluid

import com.google.gson.JsonObject
import de.bixilon.minosoft.data.registries.ResourceLocation
import de.bixilon.minosoft.data.registries.blocks.BlockState
import de.bixilon.minosoft.data.registries.blocks.types.FluidBlock
import de.bixilon.minosoft.data.registries.fluid.lava.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.water.WaterFluid
import de.bixilon.minosoft.data.registries.items.Item
import de.bixilon.minosoft.data.registries.particle.ParticleType
import de.bixilon.minosoft.data.registries.registry.RegistryItem
import de.bixilon.minosoft.data.registries.registry.ResourceLocationDeserializer
import de.bixilon.minosoft.data.registries.versions.Registries
import de.bixilon.minosoft.protocol.network.connection.PlayConnection
import glm_.vec3.Vec3i
import kotlin.random.Random

open class Fluid(
    override val resourceLocation: ResourceLocation,
    registries: Registries,
    data: JsonObject,
) : RegistryItem {
    private val bucketItemId = data["bucket"]?.asInt
    val dripParticle: ParticleType? = data["drip_particle_type"]?.asInt?.let { registries.particleTypeRegistry[it] }
    open val stillTexture: ResourceLocation? = null
    var bucketItem: Item? = null
        private set

    override fun toString(): String {
        return resourceLocation.full
    }

    override fun postInit(registries: Registries) {
        bucketItem = bucketItemId?.let { registries.itemRegistry[it] }
    }

    open fun matches(other: Fluid): Boolean {
        return other == this
    }

    open fun matches(other: BlockState?): Boolean {
        other ?: return false
        if (other.block !is FluidBlock) {
            return false
        }

        return matches(other.block.fluid)
    }


    open fun randomTick(connection: PlayConnection, blockState: BlockState, blockPosition: Vec3i, random: Random) {}

    companion object : ResourceLocationDeserializer<Fluid> {
        private val CONSTRUCTORS: Map<String, (resourceLocation: ResourceLocation, registries: Registries, data: JsonObject) -> Fluid> = mapOf(
            "EmptyFluid" to { resourceLocation, registries, data -> EmptyFluid(resourceLocation, registries, data) },
            "WaterFluid\$Flowing" to { resourceLocation, registries, data -> WaterFluid(resourceLocation, registries, data) },
            "WaterFluid\$Still" to { resourceLocation, registries, data -> WaterFluid(resourceLocation, registries, data) },
            "LavaFluid\$Flowing" to { resourceLocation, registries, data -> LavaFluid(resourceLocation, registries, data) },
            "LavaFluid\$Still" to { resourceLocation, registries, data -> LavaFluid(resourceLocation, registries, data) },
        )

        override fun deserialize(registries: Registries?, resourceLocation: ResourceLocation, data: JsonObject): Fluid {
            check(registries != null) { "Registries is null!" }
            CONSTRUCTORS[data["class"]?.asString]?.let {
                return it.invoke(resourceLocation, registries, data)
            }

            return Fluid(resourceLocation, registries, data)
        }
    }
}
