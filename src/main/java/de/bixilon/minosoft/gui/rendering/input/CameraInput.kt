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

package de.bixilon.minosoft.gui.rendering.input

import de.bixilon.kotlinglm.GLM
import de.bixilon.kotlinglm.vec2.Vec2d
import de.bixilon.minosoft.config.key.KeyActions
import de.bixilon.minosoft.config.key.KeyBinding
import de.bixilon.minosoft.config.key.KeyCodes
import de.bixilon.minosoft.data.entities.EntityRotation
import de.bixilon.minosoft.gui.rendering.RenderContext
import de.bixilon.minosoft.gui.rendering.camera.MatrixHandler
import de.bixilon.minosoft.input.camera.MovementInputActions
import de.bixilon.minosoft.input.camera.PlayerMovementInput
import de.bixilon.minosoft.util.KUtil.toResourceLocation

class CameraInput(
    private val context: RenderContext,
    val matrixHandler: MatrixHandler,
) {
    private val connection = context.connection
    private val controlsProfile = connection.profiles.controls

    private var changeFly = false

    private fun registerKeyBindings() {
        context.inputHandler.registerCheckCallback(
            MOVE_SPRINT_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_LEFT_CONTROL),
            ),
            MOVE_FORWARDS_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_W),
            ),
            MOVE_BACKWARDS_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_S),
            ),
            MOVE_LEFT_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_A),
            ),
            MOVE_RIGHT_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_D),
            ),
            FLY_UP_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_SPACE),
            ),
            FLY_DOWN_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_LEFT_SHIFT),
            ),
            JUMP_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_SPACE),
            ),
            SNEAK_KEYBINDING to KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_LEFT_SHIFT),
            ),
            CHANGE_FLY_KEYBINDING to KeyBinding(
                KeyActions.DOUBLE_PRESS to setOf(KeyCodes.KEY_SPACE),
            ),
            START_ELYTRA_FLY_KEYBINDING to KeyBinding(
                KeyActions.PRESS to setOf(KeyCodes.KEY_SPACE),
            ),
        )


        context.inputHandler.registerKeyCallback(
            ZOOM_KEYBINDING, KeyBinding(
                KeyActions.CHANGE to setOf(KeyCodes.KEY_C),
            )
        ) { matrixHandler.zoom = if (it) 2.0f else 0.0f }
    }

    fun init() {
        registerKeyBindings()
    }

    fun updateInput(delta: Double) {
        val input = PlayerMovementInput(
            forward = context.inputHandler.isKeyBindingDown(MOVE_FORWARDS_KEYBINDING),
            backward = context.inputHandler.isKeyBindingDown(MOVE_BACKWARDS_KEYBINDING),
            left = context.inputHandler.isKeyBindingDown(MOVE_LEFT_KEYBINDING),
            right = context.inputHandler.isKeyBindingDown(MOVE_RIGHT_KEYBINDING),
            jump = context.inputHandler.isKeyBindingDown(JUMP_KEYBINDING),
            sneak = context.inputHandler.isKeyBindingDown(SNEAK_KEYBINDING),
            sprint = context.inputHandler.isKeyBindingDown(MOVE_SPRINT_KEYBINDING),
            flyDown = context.inputHandler.isKeyBindingDown(FLY_DOWN_KEYBINDING),
            flyUp = context.inputHandler.isKeyBindingDown(FLY_UP_KEYBINDING),
        )

        val changeFly = context.inputHandler.isKeyBindingDown(CHANGE_FLY_KEYBINDING)
        val startElytraFly = context.inputHandler.isKeyBindingDown(START_ELYTRA_FLY_KEYBINDING)
        val inputActions = MovementInputActions(
            toggleFly = changeFly != this.changeFly,
            startElytraFly = startElytraFly,
        )
        this.changeFly = changeFly

        context.camera.view.view.onInput(input, inputActions, delta)
    }

    fun updateMouse(movement: Vec2d) {
        context.camera.view.view.onMouse(movement)
    }

    fun calculateRotation(delta: Vec2d, rotation: EntityRotation): EntityRotation {
        val delta = delta * 0.1f * controlsProfile.mouse.sensitivity
        var yaw = delta.x + rotation.yaw
        if (yaw > 180) {
            yaw -= 360
        } else if (yaw < -180) {
            yaw += 360
        }
        yaw %= 180
        val pitch = GLM.clamp(delta.y + rotation.pitch, -89.9, 89.9)
        return EntityRotation(yaw.toFloat(), pitch.toFloat())
    }

    private companion object {
        private val MOVE_SPRINT_KEYBINDING = "minosoft:move_sprint".toResourceLocation()
        private val MOVE_FORWARDS_KEYBINDING = "minosoft:move_forward".toResourceLocation()
        private val MOVE_BACKWARDS_KEYBINDING = "minosoft:move_backwards".toResourceLocation()
        private val MOVE_LEFT_KEYBINDING = "minosoft:move_left".toResourceLocation()
        private val MOVE_RIGHT_KEYBINDING = "minosoft:move_right".toResourceLocation()

        private val SNEAK_KEYBINDING = "minosoft:move_sneak".toResourceLocation()
        private val JUMP_KEYBINDING = "minosoft:move_jump".toResourceLocation()

        private val START_ELYTRA_FLY_KEYBINDING = "minosoft:move_start_elytra_fly".toResourceLocation()

        private val CHANGE_FLY_KEYBINDING = "minosoft:move_change_fly".toResourceLocation()
        private val FLY_UP_KEYBINDING = "minosoft:move_fly_up".toResourceLocation()
        private val FLY_DOWN_KEYBINDING = "minosoft:move_fly_down".toResourceLocation()

        private val ZOOM_KEYBINDING = "minosoft:zoom".toResourceLocation()
    }
}
