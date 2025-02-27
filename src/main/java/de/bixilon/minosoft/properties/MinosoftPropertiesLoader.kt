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

package de.bixilon.minosoft.properties

import de.bixilon.minosoft.Minosoft
import de.bixilon.minosoft.assets.util.InputStreamUtil.readJson
import de.bixilon.minosoft.data.registries.identified.Namespaces.minosoft
import de.bixilon.minosoft.terminal.RunConfiguration
import de.bixilon.minosoft.util.logging.Log
import de.bixilon.minosoft.util.logging.LogLevels
import de.bixilon.minosoft.util.logging.LogMessageType


object MinosoftPropertiesLoader {

    fun load() {
        val json = Minosoft.MINOSOFT_ASSETS_MANAGER[minosoft("version.json")].readJson<MinosoftP>()
        MinosoftProperties = json

        RunConfiguration.APPLICATION_NAME = "Minosoft ${MinosoftProperties.general.name}"
        Log.log(LogMessageType.OTHER, LogLevels.INFO) { "This is minosoft version ${MinosoftProperties.general.name}${MinosoftProperties.git?.let { ", built on ${it.commitShort}/${it.branch}" }}!" }
    }
}
