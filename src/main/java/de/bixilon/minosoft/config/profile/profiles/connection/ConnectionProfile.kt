/*
 * Minosoft
 * Copyright (C) 2020-2022 Moritz Zwerger
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.
 *
 * This software is not affiliated with Mojang AB, the original developer of Minecraft.
 */

package de.bixilon.minosoft.config.profile.profiles.connection

import de.bixilon.kutil.cast.CastUtil.unsafeCast
import de.bixilon.minosoft.config.profile.ProfileManager
import de.bixilon.minosoft.config.profile.delegate.primitive.BooleanDelegate
import de.bixilon.minosoft.config.profile.delegate.types.EnumDelegate
import de.bixilon.minosoft.config.profile.delegate.types.LanguageDelegate
import de.bixilon.minosoft.config.profile.delegate.types.StringDelegate
import de.bixilon.minosoft.config.profile.profiles.Profile
import de.bixilon.minosoft.config.profile.profiles.connection.ConnectionProfileManager.latestVersion
import de.bixilon.minosoft.config.profile.profiles.connection.signature.SignatureC
import de.bixilon.minosoft.config.profile.profiles.connection.skin.SkinC
import de.bixilon.minosoft.data.entities.entities.player.Arms
import java.util.concurrent.atomic.AtomicInteger

/**
 * Profile for connection
 */
class ConnectionProfile(
    description: String? = null,
) : Profile {
    override val manager: ProfileManager<Profile> = ConnectionProfileManager.unsafeCast()
    override var initializing: Boolean = true
        private set
    override var reloading: Boolean = false
    override var saved: Boolean = true
    override var ignoreReloads = AtomicInteger()
    override val version: Int = latestVersion
    override var description by StringDelegate(this, description ?: "")

    /**
     * Language for language files.
     * Will be sent to the server
     * If unset (null), uses eros language
     */
    var language: String? by LanguageDelegate(this, null)

    /**
     * If false, the server should not list us the ping player list
     * Will be sent to the server
     */
    var playerListing by BooleanDelegate(this, true)

    /**
     * Main arm to use
     */
    var mainArm by EnumDelegate(this, Arms.RIGHT, Arms)

    val skin = SkinC(this)
    val signature = SignatureC(this)

    var autoRespawn by BooleanDelegate(this, false)

    /**
     * If set, the client will respond with "vanilla" as brand and not "minosoft"
     */
    var fakeBrand by BooleanDelegate(this, false)

    override fun toString(): String {
        return ConnectionProfileManager.getName(this)
    }

    init {
        initializing = false
    }
}
