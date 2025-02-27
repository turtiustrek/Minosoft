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

package de.bixilon.minosoft.data.registries

import de.bixilon.minosoft.data.registries.identified.Namespaces
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.registries.identified.ResourceLocationUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertNotEquals

class ResourceLocationTest {

    @Test
    fun equals() {
        assertEquals(ResourceLocation("name", "path"), ResourceLocation("name", "path"))
    }

    @Test
    fun notEqualsNamespace() {
        assertNotEquals(ResourceLocation("name1", "path"), ResourceLocation("name", "path"))
    }

    @Test
    fun notEqualsPath() {
        assertNotEquals(ResourceLocation("name", "path1"), ResourceLocation("name", "path"))
    }

    @Test
    fun notEqualsBoth() {
        assertNotEquals(ResourceLocation("name1", "path1"), ResourceLocation("name", "path"))
    }

    @Test
    fun equalsHashCode() {
        assertEquals(ResourceLocation("name", "path").hashCode(), ResourceLocation("name", "path").hashCode())
    }

    @Test
    fun notEqualsHashCodeNamespace() {
        assertNotEquals(ResourceLocation("name1", "path").hashCode(), ResourceLocation("name", "path").hashCode())
    }

    @Test
    fun notEqualsHashCodePath() {
        assertNotEquals(ResourceLocation("name", "path1").hashCode(), ResourceLocation("name", "path").hashCode())
    }

    @Test
    fun notEqualsHashCodeBoth() {
        assertNotEquals(ResourceLocation("name1", "path1").hashCode(), ResourceLocation("name", "path").hashCode())
    }

    @Test
    fun invalidNamespace() {
        assertThrows<IllegalArgumentException> { ResourceLocation("in valid", "path") }
    }

    @Test
    fun integratedNamespaces() {
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace(Namespaces.MINECRAFT) }
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace(Namespaces.MINOSOFT) }
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace(Namespaces.DEFAULT) }
    }

    /**
     * @see [de.bixilon.minosoft.data.registries.identified.ResourceLocation]
     */
    @Test
    fun testAllowedNamespaces() {
        // Should Pass
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace("minecraft") }
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace("min1234567890craft") }
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace("mine-craft") }
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace("mine_craft") }
        assertDoesNotThrow { ResourceLocationUtil.validateNamespace("mine.craft") }

        // Should Fail
        assertThrows<IllegalArgumentException> { ResourceLocationUtil.validateNamespace("MineCraft") }
        assertThrows<IllegalArgumentException> { ResourceLocationUtil.validateNamespace("mine craft") }
        assertThrows<IllegalArgumentException> { ResourceLocationUtil.validateNamespace("minecraft!") }
        assertThrows<IllegalArgumentException> { ResourceLocationUtil.validateNamespace("^minecraft") }
        assertThrows<IllegalArgumentException> { ResourceLocationUtil.validateNamespace("mine/craft") }
    }

    /**
     * @see [de.bixilon.minosoft.data.registries.identified.ResourceLocation]
     */
    @Test
    fun testAllowedResourceLocationPaths() {
        // Should Pass
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("minecraft"), true)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("min1234567890craft"), true)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine-craft"), true)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine_craft"), true)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine.craft"), true)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine/craft"), true)
        // Should Fail
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("MineCraft"), false)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine craft"), false)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("minecraft!"), false)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("^minecraft"), false)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine//craft"), false)
        kotlin.test.assertEquals(ResourceLocation.ALLOWED_PATH_PATTERN.matches("mine///craft"), false)
    }

}
