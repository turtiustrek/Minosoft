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

package de.bixilon.minosoft.gui.rendering.camera.shaking

import de.bixilon.kotlinglm.mat4x4.Mat4
import de.bixilon.kotlinglm.vec3.Vec3
import de.bixilon.kotlinglm.vec3.swizzle.xz
import de.bixilon.kutil.avg.FloatAverage
import de.bixilon.kutil.time.TimeUtil.millis
import de.bixilon.minosoft.config.profile.profiles.rendering.camera.shaking.ShakingC
import de.bixilon.minosoft.gui.rendering.camera.Camera
import de.bixilon.minosoft.protocol.protocol.ProtocolDefinition
import kotlin.math.PI
import kotlin.math.sin

class CameraShaking(
    private val camera: Camera,
    private val profile: ShakingC,
) {
    private var rotation = 0.0f
    private var previous_velocity = 0.0f
    private var strength = 0.02f
    private val speed = FloatAverage(10 * ProtocolDefinition.TICK_TIME * 1_000_000L, 0.0f)
    private val physics = camera.context.connection.camera.entity.physics

    val isEmpty: Boolean get() = rotation == 0.0f

    fun update(): Boolean {
        if(!this.physics.onGround){
            this.speed += 0.0f
            this.rotation = 0.0f
            return false
        }
        val time = millis()
        this.rotation = bobbing(time,10f,this.strength)
        this.strength = 0.02f
        return true
    }

    private fun bobbing(time: Long,frequency: Float, intensity: Float): Float {
        var velocity = this.physics.velocity.xz.length2().toFloat() // velocity affects how quick it goes
        if(this.previous_velocity != 0.0f){
            velocity = this.previous_velocity + (velocity - this.previous_velocity) * 0.25f // interpolate
            this.speed += velocity
        }else{
            this.speed += 0.0f
        }
        this.previous_velocity = velocity
        var seconds = (time/1000.0).toDouble()
        val minimum_speed = 0.14f
        return (sin(seconds * frequency).toFloat() * minOf(this.speed.avg, 0.1f) * intensity) / minimum_speed
    }
    fun onDamage() {
        //TODO: verify this properly, frequency may need to be changed as well
        this.strength += 1f
    }

    fun transform(): Mat4? {
        if (this.rotation == 0.0f) return null
        return Mat4()
            .rotateAssign(this.rotation, Vec3(0, 0, 1))
    }
}
