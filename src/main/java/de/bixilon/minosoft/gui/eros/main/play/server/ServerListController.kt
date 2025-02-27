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

package de.bixilon.minosoft.gui.eros.main.play.server

import de.bixilon.kutil.collections.CollectionUtil.toSynchronizedSet
import de.bixilon.kutil.concurrent.pool.DefaultThreadPool.async
import de.bixilon.kutil.latch.CountUpAndDownLatch
import de.bixilon.kutil.primitive.BooleanUtil.decide
import de.bixilon.kutil.primitive.IntUtil.thousands
import de.bixilon.kutil.unit.UnitFormatter.formatNanos
import de.bixilon.minosoft.config.profile.ConnectionProfiles
import de.bixilon.minosoft.config.profile.profiles.eros.ErosProfileManager
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.AbstractServer
import de.bixilon.minosoft.config.profile.profiles.eros.server.entries.ErosServer
import de.bixilon.minosoft.data.registries.identified.ResourceLocation
import de.bixilon.minosoft.data.text.ChatComponent
import de.bixilon.minosoft.data.text.TranslatableComponents
import de.bixilon.minosoft.gui.eros.Eros
import de.bixilon.minosoft.gui.eros.controller.EmbeddedJavaFXController
import de.bixilon.minosoft.gui.eros.dialog.ServerModifyDialog
import de.bixilon.minosoft.gui.eros.dialog.SimpleErosConfirmationDialog
import de.bixilon.minosoft.gui.eros.dialog.connection.ConnectingDialog
import de.bixilon.minosoft.gui.eros.dialog.connection.KickDialog
import de.bixilon.minosoft.gui.eros.dialog.connection.LoadingDialog
import de.bixilon.minosoft.gui.eros.dialog.connection.VerifyAssetsDialog
import de.bixilon.minosoft.gui.eros.main.InfoPane
import de.bixilon.minosoft.gui.eros.main.play.server.card.FaviconManager.saveFavicon
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCard
import de.bixilon.minosoft.gui.eros.main.play.server.card.ServerCardController
import de.bixilon.minosoft.gui.eros.main.play.server.type.types.CustomServerType
import de.bixilon.minosoft.gui.eros.main.play.server.type.types.ServerType
import de.bixilon.minosoft.gui.eros.modding.invoker.JavaFXEventListener
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil
import de.bixilon.minosoft.gui.eros.util.JavaFXUtil.ctext
import de.bixilon.minosoft.modding.event.events.KickEvent
import de.bixilon.minosoft.modding.event.events.LoginKickEvent
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnection
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates
import de.bixilon.minosoft.protocol.network.connection.play.PlayConnectionStates.Companion.disconnected
import de.bixilon.minosoft.protocol.network.connection.status.StatusConnection
import de.bixilon.minosoft.util.DNSUtil
import de.bixilon.minosoft.util.KUtil.toResourceLocation
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeFX
import de.bixilon.minosoft.util.delegate.JavaFXDelegate.observeListFX
import javafx.fxml.FXML
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ListView
import javafx.scene.input.KeyCode
import javafx.scene.layout.Pane


class ServerListController : EmbeddedJavaFXController<Pane>(), Refreshable {
    @FXML private lateinit var hideOfflineFX: CheckBox
    @FXML private lateinit var hideFullFX: CheckBox
    @FXML private lateinit var hideEmptyFX: CheckBox

    @FXML private lateinit var addServerButtonFX: Button
    @FXML private lateinit var serverListViewFX: ListView<ServerCard>
    @FXML private lateinit var serverInfoFX: InfoPane<ServerCard>

    private val toRemove: MutableSet<ServerCard> = mutableSetOf() // workaround for crash when calling onPingUpdate in the event listener from onPingUpdate

    var serverType: ServerType? = null
        set(value) {
            check(value != null)
            field = value

            addServerButtonFX.isVisible = !value.readOnly
        }

    override fun init() {
        val erosProfile = ErosProfileManager.selected
        val serverConfig = erosProfile.server.list
        serverConfig::hideOffline.observeFX(this, true) { hideOfflineFX.isSelected = it;refreshList() }
        serverConfig::hideFull.observeFX(this, true) { hideFullFX.isSelected = it;refreshList() }
        serverConfig::hideEmpty.observeFX(this, true) { hideEmptyFX.isSelected = it;refreshList() }

        hideOfflineFX.setOnAction { ErosProfileManager.selected.server.list.hideOffline = hideOfflineFX.isSelected }
        hideOfflineFX.ctext = HIDE_OFFLINE
        hideFullFX.setOnAction { ErosProfileManager.selected.server.list.hideFull = hideFullFX.isSelected }
        hideFullFX.ctext = HIDE_FULL
        hideEmptyFX.setOnAction { ErosProfileManager.selected.server.list.hideEmpty = hideEmptyFX.isSelected }
        hideEmptyFX.ctext = HIDE_EMPTY


        addServerButtonFX.ctext = ADD_SERVER

        val accountProfile = erosProfile.general.accountProfile
        serverListViewFX.setCellFactory {
            val controller = ServerCardController.build()
            controller.serverList = this

            controller.root.setOnMouseClicked {
                if (it.clickCount != 2) {
                    return@setOnMouseClicked
                }
                val card = controller.item ?: return@setOnMouseClicked
                if (!card.canConnect(accountProfile.selected ?: return@setOnMouseClicked)) {
                    return@setOnMouseClicked
                }

                connect(card)
            }
            return@setCellFactory controller
        }

        serverListViewFX.selectionModel.selectedItemProperty().addListener { _, _, new ->
            setServerInfo(new)
        }
    }

    fun connect(serverCard: ServerCard) {
        val server = serverCard.server
        val ping = serverCard.ping
        val version = serverCard.server.forcedVersion ?: serverCard.ping.serverVersion ?: return
        Eros.mainErosController.verifyAccount { account ->
            val connection = PlayConnection(
                address = ping.realAddress ?: DNSUtil.getServerAddress(server.address),
                account = account,
                version = version,
                profiles = ConnectionProfiles(ErosProfileManager.selected.general.profileOverrides.toMutableMap().apply { putAll(server.profiles) })
            )
            account.connections[server] = connection
            serverCard.connections += connection

            connection::state.observeFX(serverCard) {
                if (it.disconnected) {
                    account.connections -= server
                    serverCard.connections -= connection
                }
                if (ErosProfileManager.selected.general.hideErosOnceConnected) {
                    if (connection.network.connected) {
                        if (connection.state == PlayConnectionStates.PLAYING) {
                            Eros.setVisibility(false)
                        }
                    } else {
                        var connected = false
                        for (entry in PlayConnection.ACTIVE_CONNECTIONS.toSynchronizedSet()) {
                            if (entry.network.connected) {
                                connected = true
                                break
                            }
                        }
                        if (!connected) {
                            Eros.setVisibility(true)
                        }
                    }
                }
                JavaFXUtil.runLater { updateServer(server, true) }
            }

            connection.events.register(JavaFXEventListener.of<KickEvent> { event ->
                KickDialog(
                    title = "minosoft:connection.kick.title".toResourceLocation(),
                    header = "minosoft:connection.kick.header".toResourceLocation(),
                    description = TranslatableComponents.CONNECTION_KICK_DESCRIPTION(server, account),
                    reason = event.reason,
                ).show()
            })
            connection.events.register(JavaFXEventListener.of<LoginKickEvent> { event ->
                KickDialog(
                    title = "minosoft:connection.login_kick.title".toResourceLocation(),
                    header = "minosoft:connection.login_kick.header".toResourceLocation(),
                    description = TranslatableComponents.CONNECTION_LOGIN_KICK_DESCRIPTION(server, account),
                    reason = event.reason,
                ).show()
            })
            val latch = CountUpAndDownLatch(1)
            val assetsDialog = VerifyAssetsDialog(latch = latch).apply { show() }
            connection::state.observeFX(serverCard) {
                if (it == PlayConnectionStates.LOADING || it.disconnected) {
                    assetsDialog.close()
                }
                if (it == PlayConnectionStates.LOADING) {
                    LoadingDialog(latch, connection).show()
                }
                if (it == PlayConnectionStates.ESTABLISHING) {
                    ConnectingDialog(connection).show()
                }
            }


            connection.connect(latch)
        }
    }

    override fun postInit() {
        root.setOnKeyPressed { if (it.code == KeyCode.ESCAPE) serverListViewFX.selectionModel.select(null) } // ToDo: Not working
    }

    fun initWatch() {
        ErosProfileManager.selected.general.accountProfile::selected.observeFX(this) { setServerInfo(serverListViewFX.selectionModel.selectedItem) }

        serverType!!::servers.observeListFX(this, instant = true) {
            for (remove in it.removes) {
                serverListViewFX.items -= ServerCard.CARDS.remove(remove)
            }
            for (add in it.adds) {
                updateServer(add)
            }
        }
    }

    @FXML
    fun refreshList() {
        if (serverType == null) {
            return
        }
        val selected = serverListViewFX.selectionModel.selectedItem
        serverListViewFX.items.clear()

        for (server in serverType!!.servers) {
            updateServer(server)
        }

        serverListViewFX.items.contains(selected).decide(selected, null).let {
            serverListViewFX.selectionModel.select(it)

            serverListViewFX.scrollTo(it)
        }
    }

    private fun updateServer(server: AbstractServer, refreshInfo: Boolean = false) {
        val serverType = serverType ?: return
        if (server !in serverType.servers) {
            return
        }
        val card = ServerCard.CARDS[server] ?: ServerCard(server).apply {
            ping::state.observeFX(this@apply) { updateServer(server) } // ToDo: Don't register twice
        }
        val wasSelected = serverListViewFX.selectionModel.selectedItem === card
        // Platform.runLater {serverListViewFX.items.remove(card)}

        if (card.ping.hide) {
            return
        }


        if (!serverListViewFX.items.contains(card)) {
            serverListViewFX.items.add(card)
        }
        // serverListViewFX.items.sortBy { it.server.id } // ToDo (Performance): Do not sort, add before/after other server


        if (wasSelected) {
            serverListViewFX.selectionModel.select(card)
            if (refreshInfo) {
                setServerInfo(card)
            }
        }
    }


    private fun setServerInfo(serverCard: ServerCard?) {
        val serverType = this.serverType!!

        if (serverCard == null) {
            serverInfoFX.reset()
            return
        }
        val account = ErosProfileManager.selected.general.accountProfile
        val server = serverCard.server

        val actions: Array<Node> = arrayOf(
            Button("Delete").apply {
                if (server !is ErosServer) {
                    isDisable = true
                }
                setOnAction {
                    SimpleErosConfirmationDialog(confirmButtonText = "minosoft:general.delete".toResourceLocation(), description = TranslatableComponents.EROS_DELETE_SERVER_CONFIRM_DESCRIPTION(serverCard.server.name, serverCard.server.address), onConfirm = {
                        val type = serverType
                        if (type !is CustomServerType) {
                            return@SimpleErosConfirmationDialog
                        }
                        type.servers.remove(server)
                    }).show()
                }
                ctext = TranslatableComponents.GENERAL_DELETE
                isDisable = serverType.readOnly
            },
            Button("Edit").apply {
                if (server !is ErosServer) {
                    isDisable = true
                }
                setOnAction {
                    if (server !is ErosServer) {
                        return@setOnAction
                    }
                    ServerModifyDialog(server = server, onUpdate = { name, address, forcedVersion, profiles, queryVersion ->
                        server.name = ChatComponent.of(name)
                        server.forcedVersion = forcedVersion
                        server.profiles = profiles.toMutableMap()
                        server.queryVersion = queryVersion
                        val ping = serverCard.ping
                        ping.forcedVersion = if (queryVersion) null else forcedVersion
                        if (server.address != address) {
                            server.faviconHash?.let { hash -> server.saveFavicon(null, hash) }

                            server.address = address

                            // disconnect all ping connections, re ping
                            // ToDo: server.connections.clear()

                            ping.disconnect()
                            ping.address = server.address
                            ping.ping()
                        }
                        JavaFXUtil.runLater { refreshList() }
                    }).show()
                }
                ctext = EDIT
            },
            Button("Refresh").apply {
                setOnAction {
                    async {
                        serverCard.ping.disconnect()
                        serverCard.ping.ping()
                    }
                }
                ctext = TranslatableComponents.GENERAL_REFRESH
            },
            Button("Connect").apply {
                setOnAction {
                    isDisable = true
                    connect(serverCard)
                }
                val selected = account.selected
                isDisable = selected != null && !serverCard.canConnect(selected)
                // ToDo: Also disable, if currently connecting
                ctext = CONNECT
                serverCard.ping::state.observeFX(serverCard) { isDisable = selected == null || !serverCard.canConnect(selected) }
            },
        )

        serverInfoFX.update(serverCard, SERVER_INFO_PROPERTIES, actions)
    }

    val StatusConnection.hide: Boolean
        get() {
            if (hideOfflineFX.isSelected && error != null) {
                return true
            }

            status?.let { status ->
                val usedSlots = status.usedSlots ?: 0
                val slots = status.slots ?: 0
                if (hideFullFX.isSelected && usedSlots >= slots && slots > 0) {
                    return true
                }

                if (hideEmptyFX.isSelected && usedSlots == 0 && slots > 0) {
                    return true
                }
            }
            return false
        }

    fun onPingUpdate(card: ServerCard) {
        val ping = card.ping
        val selected = serverListViewFX.selectionModel.selectedItem
        if (ping.hide) {
            toRemove += card
            if (toRemove.size != 1) { // isNotEmpty
                return
            }

            if (selected === card) {
                serverListViewFX.selectionModel.select(null)
            }
            serverListViewFX.items.remove(card)
            toRemove -= card
            if (toRemove.isNotEmpty()) {
                serverListViewFX.items.removeAll(toRemove)
                toRemove.clear()
            }
            return
        }
        if (card !== selected) {
            return
        }
        setServerInfo(card)
    }

    @FXML
    fun addServer() {
        ServerModifyDialog(onUpdate = { name, address, forcedVersion, profiles, queryVersion ->
            val type = serverType
            if (type !is CustomServerType) {
                return@ServerModifyDialog
            }
            type.servers += ErosServer(profile = ErosProfileManager.selected, name = ChatComponent.of(name), address = address, forcedVersion = forcedVersion, profiles = profiles.toMutableMap(), queryVersion = queryVersion)
        }).show()
    }

    override fun refresh() {
        serverType!!.refresh(serverListViewFX.items)
    }


    companion object {
        val LAYOUT = "minosoft:eros/main/play/server/server_list.fxml".toResourceLocation()

        private val HIDE_OFFLINE = "minosoft:server_list.hide_offline".toResourceLocation()
        private val HIDE_FULL = "minosoft:server_list.hide_full".toResourceLocation()
        private val HIDE_EMPTY = "minosoft:server_list.hide_empty".toResourceLocation()
        private val ADD_SERVER = "minosoft:server_list.add_server".toResourceLocation()

        private val CONNECT = "minosoft:server_list.button.connect".toResourceLocation()
        private val EDIT = "minosoft:server_list.button.edit".toResourceLocation()


        private val SERVER_INFO_PROPERTIES: List<Pair<ResourceLocation, (ServerCard) -> Any?>> = listOf(
            "minosoft:server_info.server_name".toResourceLocation() to { it.server.name },
            "minosoft:server_info.server_address".toResourceLocation() to { it.server.address },
            "minosoft:server_info.real_server_address".toResourceLocation() to { it.ping.realAddress },
            "minosoft:server_info.forced_version".toResourceLocation() to { it.server.forcedVersion },

            TranslatableComponents.GENERAL_EMPTY to { " " },

            "minosoft:server_info.remote_version".toResourceLocation() to { it.ping.serverVersion ?: "unknown" },
            "minosoft:server_info.remote_brand".toResourceLocation() to { it.ping.status?.serverBrand },
            "minosoft:server_info.players_online".toResourceLocation() to { it.ping.status?.let { status -> "${status.usedSlots?.thousands()} / ${status.slots?.thousands()}" } },
            "minosoft:server_info.ping".toResourceLocation() to { it.ping.pong?.latency?.formatNanos() },


            TranslatableComponents.GENERAL_EMPTY to { " " },

            "minosoft:server_info.active_connections".toResourceLocation() to { if (it.connections.isEmpty()) null else it.connections.size },
        )
    }
}
