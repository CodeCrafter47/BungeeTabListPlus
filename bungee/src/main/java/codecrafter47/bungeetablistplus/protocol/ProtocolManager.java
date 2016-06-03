/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.protocol;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;

import java.util.logging.Level;

public class ProtocolManager implements Listener {
    private final Plugin plugin;

    public ProtocolManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void enable() {
        plugin.getProxy().getPluginManager().registerListener(plugin, this);
    }

    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();

            ConnectedPlayer connectedPlayer = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(player);
            if (connectedPlayer != null) {
                ServerConnection server = (ServerConnection) event.getPlayer().getServer();

                ChannelWrapper wrapper = server.getCh();

                int version = player.getPendingConnection().getVersion();
                PacketHandler packetHandler = connectedPlayer.getPacketHandler();
                PacketListener packetListener = new PacketListener(server, packetHandler, version);

                wrapper.getHandle().pipeline().addBefore(PipelineUtils.BOSS_HANDLER, "btlp-packet-listener", packetListener);

                packetHandler.onServerSwitch();
            }
        } catch (Exception ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to inject packet listener", ex);
        }
    }
}
