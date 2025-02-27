/*
 * Minosoft
 * Copyright (C) 2020-2023 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.util.json

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.json.JsonReadFeature
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.databind.type.MapType
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import de.bixilon.minosoft.util.json.vec.Vec2Serializer
import de.bixilon.minosoft.util.json.vec.Vec3Serializer
import de.bixilon.minosoft.util.json.vec.Vec4Serializer

object Jackson {
    val MAPPER = JsonMapper.builder()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        .disable(JsonParser.Feature.AUTO_CLOSE_SOURCE)
        .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS)
        .enable(JsonReadFeature.ALLOW_UNESCAPED_CONTROL_CHARS)
        .build()
        .registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        .registerModule(JavaTimeModule())
        .registerModule(ResourceLocationSerializer)
        .registerModule(RGBColorSerializer)
        .registerModule(ChatComponentColorSerializer)
        .registerModule(Vec2Serializer)
        .registerModule(Vec3Serializer)
        .registerModule(Vec4Serializer)
        .setDefaultMergeable(true)
        .apply {
            propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
        }


    val JSON_MAP_TYPE: MapType = MAPPER.typeFactory.constructMapType(HashMap::class.java, Any::class.java, Any::class.java)
}
