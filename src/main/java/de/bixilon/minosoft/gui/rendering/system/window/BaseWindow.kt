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

package de.bixilon.minosoft.gui.rendering.system.window

import de.bixilon.kotlinglm.vec2.Vec2
import de.bixilon.kotlinglm.vec2.Vec2i
import de.bixilon.minosoft.assets.AssetsManager
import de.bixilon.minosoft.config.profile.profiles.rendering.RenderingProfile
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.DesktopUtil
import de.bixilon.minosoft.util.delegate.RenderingDelegate.observeRendering
import de.matthiasmann.twl.utils.PNGDecoder
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer

interface BaseWindow {
    val systemScale: Vec2

    var size: Vec2i
    val sizef: Vec2
        get() = Vec2(size)
    var minSize: Vec2i
    var maxSize: Vec2i

    var visible: Boolean
    var resizable: Boolean
    var fullscreen: Boolean

    var swapInterval: Int

    var cursorMode: CursorModes
    var cursorShape: CursorShapes


    var clipboardText: String
    var title: String
    val version: String

    val time: Double

    val iconified: Boolean
    val focused: Boolean

    fun init(profile: RenderingProfile) {
        resizable = true
        profile.advanced::swapInterval.observeRendering(this, true) { swapInterval = it }

        if (!RunConfiguration.DISABLE_CURSOR_CATCH) {
            cursorMode = CursorModes.DISABLED
        }
        size = DEFAULT_WINDOW_SIZE
        minSize = DEFAULT_MINIMUM_WINDOW_SIZE
        maxSize = DEFAULT_MAXIMUM_WINDOW_SIZE
    }

    fun destroy()

    fun close()

    fun forceClose()

    fun swapBuffers()

    fun pollEvents()

    fun setOpenGLVersion(major: Int, minor: Int, coreProfile: Boolean)


    fun setIcon(size: Vec2i, buffer: ByteBuffer)


    fun setDefaultIcon(assetsManager: AssetsManager) {
        val decoder = PNGDecoder(assetsManager[DesktopUtil.ICON])
        val data = BufferUtils.createByteBuffer(decoder.width * decoder.height * PNGDecoder.Format.RGBA.numComponents)
        decoder.decode(data, decoder.width * PNGDecoder.Format.RGBA.numComponents, PNGDecoder.Format.RGBA)
        data.flip()
        setIcon(Vec2i(decoder.width, decoder.height), data)
    }

    fun resetCursor() {
        cursorShape = CursorShapes.ARROW
    }

    companion object {
        val DEFAULT_WINDOW_SIZE: Vec2i
            get() = Vec2i(900, 500)
        val DEFAULT_MINIMUM_WINDOW_SIZE: Vec2i
            get() = Vec2i(300, 100)
        val DEFAULT_MAXIMUM_WINDOW_SIZE: Vec2i
            get() = Vec2i(-1, -1)
    }
}
