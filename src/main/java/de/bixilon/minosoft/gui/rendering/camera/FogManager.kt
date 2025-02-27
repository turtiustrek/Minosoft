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

package de.bixilon.minosoft.gui.rendering.camera

import de.bixilon.kutil.math.interpolation.FloatInterpolation.interpolateLinear
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.data.registries.effects.vision.VisionEffect
import de.bixilon.minosoft.data.registries.fluid.fluids.LavaFluid
import de.bixilon.minosoft.data.registries.fluid.fluids.WaterFluid
import de.bixilon.minosoft.data.text.formatting.color.ChatColors
import de.bixilon.minosoft.data.text.formatting.color.ColorInterpolation.interpolateSine
import de.bixilon.minosoft.data.text.formatting.color.Colors
import de.bixilon.minosoft.data.text.formatting.color.RGBColor
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.shader.types.FogShader
import de.bixilon.minosoft.gui.rendering.system.base.shader.NativeShader
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition

class FogManager(
    private val context: RenderContext,
) {
    private val player = context.connection.player

    private var fogChange = -1L

    private var _fogStart = 0.0f
    private var _fogEnd = 0.0f
    private var _fogColor: RGBColor? = null

    private var fogStart = 0.0f
    private var fogEnd = 0.0f
    private var fogColor: RGBColor? = null


    var interpolatedFogStart = 0.0f
        private set
    var interpolatedFogEnd = 0.0f
        private set
    var interpolatedFogColor: RGBColor? = null
        private set
    var interpolatedRevision = 0L
        private set


    private var shaderRevision = -1L

    fun draw() {
        if (!calculateFog()) {
            updateValues()
        }
        updateShaders()
    }

    private fun calculateFog(): Boolean {
        val sky = context.connection.world.dimension.effects
        var fogStart = if (!context.connection.profiles.rendering.fog.enabled || sky == null || !sky.fog) {
            Float.MAX_VALUE
        } else {
            (context.connection.world.view.viewDistance - 2.0f) * ProtocolDefinition.SECTION_WIDTH_X  // could be improved? basically view distance in blocks and then the center of that chunk
        }
        var fogEnd = fogStart + 15.0f
        var color: RGBColor? = null

        val submergedFluid = player.physics.submersion.eye

        if (submergedFluid is LavaFluid) {
            color = LAVA_FOG_COLOR
            fogStart = 0.2f
            fogEnd = 1.0f
        } else if (submergedFluid is WaterFluid) {
            color = player.physics.positionInfo.biome?.waterFogColor
            fogStart = 5.0f
            fogEnd = 10.0f
        } else if (player.effects[VisionEffect.Blindness] != null) {
            color = ChatColors.BLACK
            fogStart = 3.0f
            fogEnd = 5.0f
        }

        if (fogStart == this.fogStart && fogEnd == this.fogEnd && color == this.fogColor) {
            return false
        }

        saveFog()
        this.fogStart = fogStart
        this.fogEnd = fogEnd
        this.fogColor = color
        return true
    }

    private fun saveFog() {
        val time = millis()
        updateValues(time)
        fogChange = time
        _fogStart = interpolatedFogStart
        _fogEnd = interpolatedFogEnd
        _fogColor = interpolatedFogColor
    }

    private fun updateValues(time: Long = millis()) {
        val delta = time - fogChange
        if (delta > FOG_INTERPOLATION_TIME) {
            // already up to date
            return
        }
        val progress = delta / FOG_INTERPOLATION_TIME.toFloat()
        this.interpolatedFogStart = interpolateLinear(progress, _fogStart, fogStart)
        this.interpolatedFogEnd = interpolateLinear(progress, _fogEnd, fogEnd)
        var color: RGBColor? = interpolateSine(progress, _fogColor ?: Colors.TRANSPARENT, fogColor ?: Colors.TRANSPARENT)
        if (color == Colors.TRANSPARENT) {
            color = null
        }
        this.interpolatedFogColor = color

        this.interpolatedRevision++
    }


    private fun updateShaders() {
        val revision = interpolatedRevision

        if (revision == this.shaderRevision) {
            return
        }

        val start = interpolatedFogStart * interpolatedFogStart
        val end = interpolatedFogEnd * interpolatedFogEnd
        val color = interpolatedFogColor
        val distance = end - start

        for (shader in context.renderSystem.shaders) {
            if (shader !is FogShader || shader.fog != this) {
                continue
            }
            use(shader.native, start, end, color, distance)
        }
        this.shaderRevision = revision
    }

    fun use(shader: NativeShader) {
        use(shader, interpolatedFogStart * interpolatedFogStart, interpolatedFogEnd * interpolatedFogEnd, interpolatedFogColor)
    }

    fun use(shader: NativeShader, start: Float, end: Float, color: RGBColor?, distance: Float = end - start) {
        shader.use()

        shader["uFogStart"] = start
        shader["uFogEnd"] = end
        shader["uFogDistance"] = distance
        if (color == null) {
            shader[USE_FOG_COLOR] = false
        } else {
            shader[FOG_COLOR] = color
            shader[USE_FOG_COLOR] = true
        }
    }

    companion object {
        private val LAVA_FOG_COLOR = RGBColor(0.6f, 0.1f, 0.0f)
        private const val FOG_INTERPOLATION_TIME = 300

        private const val FOG_COLOR = "uFogColor"
        private const val USE_FOG_COLOR = "uUseFogColor"
    }
}
