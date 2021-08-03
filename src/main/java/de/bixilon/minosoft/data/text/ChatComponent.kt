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
package de.bixilon.minosoft.data.text

import com.squareup.moshi.JsonEncodingException
import de.bixilon.minosoft.data.language.Translator
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.text
import de.bixilon.minosoft.util.KUtil.unsafeCast
import de.bixilon.minosoft.util.json.JSONSerializer
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.scene.Node
import javafx.scene.text.TextFlow

interface ChatComponent {
    /**
     * @return Returns the message formatted with ANSI Formatting codes
     */
    val ansiColoredMessage: String

    /**
     * @return Returns the message formatted with minecraft formatting codes (§)
     */
    val legacyText: String

    /**
     * @return Returns the unformatted message
     */
    val message: String

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    fun getJavaFXText(nodes: ObservableList<Node>): ObservableList<Node>

    /**
     * @return Returns the a list of Nodes, drawable in JavaFX (TextFlow)
     */
    val javaFXText: ObservableList<Node>
        get() = getJavaFXText(FXCollections.observableArrayList())

    val textFlow: TextFlow
        get() {
            val textFlow = TextFlow()
            textFlow.text = this
            return textFlow
        }

    /**
     * Sets a default color for all elements that don't have a color yet
     */
    fun applyDefaultColor(color: RGBColor)


    companion object {
        val EMPTY = ChatComponent.of("")

        @JvmOverloads
        fun of(raw: Any? = null, translator: Translator? = null, parent: TextComponent? = null, ignoreJson: Boolean = false, restrictedMode: Boolean = false): ChatComponent {
            if (raw == null) {
                return BaseComponent()
            }
            if (raw is ChatComponent) {
                return raw
            }
            if (raw is Map<*, *>) {
                return BaseComponent(translator, parent, raw.unsafeCast(), restrictedMode)
            }
            val string = when (raw) {
                is List<*> -> {
                    val component = BaseComponent()
                    for (part in raw) {
                        component += of(part, translator, parent, restrictedMode = restrictedMode)
                    }
                    return component
                }
                else -> raw.toString()
            }
            if (!ignoreJson && string.startsWith('{')) {
                try {
                    return BaseComponent(translator, parent, JSONSerializer.MAP_ADAPTER.fromJson(string)!!, restrictedMode)
                } catch (ignored: JsonEncodingException) {
                }
            }

            return BaseComponent(parent, string, restrictedMode)
        }

        fun String.chat(): ChatComponent {
            return of(this)
        }
    }
}
