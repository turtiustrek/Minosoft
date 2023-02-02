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

package de.bixilon.minosoft.data.text

import de.bixilon.kutil.url.URLUtil.toURL
import de.bixilon.minosoft.data.text.ChatComponent.Companion.chat
import de.bixilon.minosoft.data.text.events.click.OpenFileClickEvent
import de.bixilon.minosoft.data.text.events.click.OpenURLClickEvent
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor.Companion.asColor
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

internal class ChatComponentTest {


    @Test
    fun testEmpty() {
        assertSame(ChatComponent.EMPTY, "".chat())
    }

    @Test
    fun testSimpleText() {
        val expected = TextComponent("Test")
        val actual = "Test".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleColor() {
        val expected = TextComponent("Test").color(ChatColors.RED)
        val actual = "§cTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleColoredFormatting() {
        val expected = TextComponent("Test").color(ChatColors.RED).strikethrough()
        val actual = "§c§mTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSwappedSimpleColoredFormatting() {
        val expected = TextComponent("Test").color(ChatColors.RED).strikethrough()
        val actual = "§m§cTest".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun test2Texts() {
        val expected = BaseComponent(
            TextComponent("Test").color(ChatColors.RED),
            TextComponent("Next").color(ChatColors.GREEN),
        )
        val actual = "§cTest§aNext".chat()
        assertEquals(expected, actual)
    }


    @Test
    fun url1() {
        val expected = BaseComponent(
            TextComponent("Test").color(ChatColors.RED),
            TextComponent("https://bixilon.de").color(ChatColors.GREEN).clickEvent(OpenURLClickEvent("https://bixilon.de".toURL())),
        )
        val actual = "§cTest§ahttps://bixilon.de".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun url2() {
        val expected = BaseComponent(
            TextComponent("Test ").color(ChatColors.RED),
            TextComponent("file:/home/moritz").color(ChatColors.GREEN).clickEvent(OpenFileClickEvent("/home/moritz")),
        )
        val actual = ChatComponent.of("§cTest §afile:/home/moritz")
        assertEquals(expected, actual)
    }

    @Test
    fun url3() {
        val expected = BaseComponent(
            TextComponent("Hi, please take care of: "),
            TextComponent("https://bixilon.de/technoblade").clickEvent(OpenURLClickEvent("https://bixilon.de/technoblade".toURL())),
        )
        val actual = ChatComponent.of("Hi, please take care of: https://bixilon.de/technoblade")
        assertEquals(expected, actual)
    }

    @Test
    fun restrictedMode() {
        val expected = BaseComponent(
            TextComponent("Test ").color(ChatColors.RED),
            TextComponent("file:/home/moritz").color(ChatColors.GREEN),
        )
        val actual = ChatComponent.of("§cTest §afile:/home/moritz", restrictedMode = true)
        assertEquals(expected, actual)
    }

    @Test
    fun test2FormattedTexts() {
        val expected = BaseComponent(
            TextComponent("Test").color(ChatColors.RED).italic(),
            TextComponent("Next").color(ChatColors.GREEN).italic(),
        )
        val actual = "§c§oTest§a§oNext".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testSimpleJsonReading() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test"),
            )
        )
        val actual = """{"text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testColorJsonReading() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test").color(ChatColors.YELLOW),
            )
        )
        val actual = """{"text":"Test", "color": "yellow"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testHexColor() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test").color("#123456".asColor()),
            )
        )
        val actual = """{"text":"Test", "color": "#123456"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJsonArray() {
        val expected = BaseComponent(
            parts = arrayOf(
                BaseComponent(
                    parts = arrayOf(
                        TextComponent("Test"),
                    )
                )
            )
        )
        val actual = """[{"text":"Test"}]""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJsonWithWhitespaces() {
        val expected = BaseComponent(
            parts = arrayOf(
                TextComponent("Test"),
            )
        )
        val actual = """    {"text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testInvalidJson() {
        val expected = TextComponent("""{text":"Test"}""")
        val actual = """{text":"Test"}""".chat()
        assertEquals(expected, actual)
    }

    @Test
    fun testJson1() {
        val text = TextComponent("dummy")
        assertEquals(text.getJson(), mapOf("text" to "dummy"))
    }

    @Test
    fun testJson2() {
        val text = ChatComponent.of("dummy")
        assertEquals(text.getJson(), mapOf("text" to "dummy"))
    }

    @Test
    fun testJson3() {
        val text = ChatComponent.of("dummy§knext")
        assertEquals(text.getJson(), listOf(mapOf("text" to "dummy"), mapOf("text" to "next", "obfuscated" to true)))
    }

    @Test
    fun testJson4() {
        val text = ChatComponent.of("dummy§anext")
        assertEquals(text.getJson(), listOf(mapOf("text" to "dummy"), mapOf("text" to "next", "color" to "green")))
    }

    @Test
    fun hypixelMotd() {
        val string = "                §aHypixel Network §c[1.8-1.19]\n   §c§lLUNAR MAPS §7§l§ §6§lCOSMETICS §7| §d§lSKYBLOCK 0.17.3"
        val component = ChatComponent.of(string)

        val expected = BaseComponent(
            "                ",
            TextComponent("Hypixel Network ").color(ChatColors.GREEN),
            TextComponent("[1.8-1.19]\n   ").color(ChatColors.RED),
            TextComponent("LUNAR MAPS ").color(ChatColors.RED).bold(),
            TextComponent("COSMETICS ").color(ChatColors.GOLD).bold(),
            TextComponent("| ").color(ChatColors.GRAY),
            TextComponent("SKYBLOCK 0.17.3").color(ChatColors.LIGHT_PURPLE).bold(),
        )


        assertEquals("                Hypixel Network [1.8-1.19]\n   LUNAR MAPS COSMETICS | SKYBLOCK 0.17.3", component.message)
        assertEquals(expected, component)
    }
}
