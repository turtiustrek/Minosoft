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

package de.bixilon.minosoft.data.accounts

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import de.bixilon.kutil.collections.CollectionUtil.synchronizedMapOf
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.observer.DataObserver.Companion.observed
import de.bixilon.minosoft.config.profile.profiles.account.AccountProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.data.accounts.types.microsoft.MicrosoftAccount
import de.bixilon.minosoft.data.accounts.types.mojang.MojangAccount
import de.bixilon.minosoft.data.accounts.types.offline.OfflineAccount
import de.bixilon.minosoft.data.entities.entities.player.properties.PlayerProperties
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.util.account.minecraft.MinecraftPrivateKey
import java.util.*

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes(
    JsonSubTypes.Type(value = MojangAccount::class, name = "minosoft:mojang_account"),
    JsonSubTypes.Type(value = OfflineAccount::class, name = "minosoft:offline_account"),
    JsonSubTypes.Type(value = MicrosoftAccount::class, name = "minosoft:microsoft_account"),
)
abstract class Account(
    val username: String,
) {
    abstract val id: String
    abstract val type: ResourceLocation
    abstract val properties: PlayerProperties?
    @get:JsonIgnore @set:JsonIgnore open var state: AccountStates by observed(AccountStates.UNCHECKED)
    @get:JsonIgnore open var error: Throwable? by observed(null)
    abstract val uuid: UUID


    @get:JsonIgnore open val supportsEncryption: Boolean get() = true
    @get:JsonIgnore open val supportsSkins: Boolean get() = true

    @Transient
    @JsonIgnore
    val connections: MutableMap<AbstractServer, PlayConnection> = synchronizedMapOf()

    abstract fun join(serverId: String)

    abstract fun logout(clientToken: String)
    abstract fun check(latch: CountUpAndDownLatch?, clientToken: String)

    @Synchronized
    open fun tryCheck(latch: CountUpAndDownLatch?, clientToken: String) {
        if (state == AccountStates.CHECKING || state == AccountStates.REFRESHING) {
            // already checking
            return
        }
        if (state == AccountStates.WORKING) {
            // Nothing to do
            return
        }
        check(latch, clientToken)
    }

    fun save() {
        // ToDo: Optimize
        profiles@ for (profile in AccountProfileManager.profiles.values) {
            for ((_, account) in profile.entries) {
                if (account === this) {
                    AccountProfileManager.saveAsync(profile)
                    break@profiles
                }
            }
        }
    }

    open fun fetchKey(latch: CountUpAndDownLatch?): MinecraftPrivateKey? = null
}
