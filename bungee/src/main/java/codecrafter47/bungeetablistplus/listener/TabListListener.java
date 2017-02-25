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
package codecrafter47.bungeetablistplus.listener;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.managers.ConnectedPlayerManager;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;
import net.md_5.bungee.netty.ChannelWrapper;

public class TabListListener implements Listener {

    private final BungeeTabListPlus plugin;

    public TabListListener(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PostLoginEvent e) {
        try {
            ConnectedPlayerManager manager = plugin.getConnectedPlayerManager();
            ConnectedPlayer oldConnectedPlayer = manager.getPlayerIfPresent(e.getPlayer().getUniqueId());
            if (oldConnectedPlayer != null) {
                ChannelWrapper channelWrapper = ReflectionUtil.getChannelWrapper(oldConnectedPlayer.getPlayer());
                channelWrapper.getHandle().eventLoop().execute(() -> manager.onPlayerDisconnected(oldConnectedPlayer));
            }
            ConnectedPlayer connectedPlayer = new ConnectedPlayer(e.getPlayer());
            manager.onPlayerConnected(connectedPlayer);

            plugin.updateTabListForPlayer(e.getPlayer());
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        try {
            ConnectedPlayerManager manager = plugin.getConnectedPlayerManager();
            ConnectedPlayer connectedPlayer = manager.getPlayerIfPresent(e.getPlayer().getUniqueId());
            if (connectedPlayer != null && connectedPlayer.getPlayer() == e.getPlayer()) {
                manager.onPlayerDisconnected(connectedPlayer);
            }

            // hack to revert changes from https://github.com/SpigotMC/BungeeCord/commit/830f18a35725f637d623594eaaad50b566376e59
            Server server = e.getPlayer().getServer();
            if (server != null) {
                server.disconnect("Quitting");
            }
            ((UserConnection) e.getPlayer()).setServer(null);
        } catch (Throwable th){
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @EventHandler
    public void onDevJoin(PostLoginEvent e) {
        if (plugin.getPlugin().getDescription().getAuthor().equalsIgnoreCase(e.getPlayer().
                getName())) {
            e.getPlayer().sendMessage(ChatColor.LIGHT_PURPLE + "Hello " + e.
                    getPlayer().getName() + ", this server uses " + plugin.getPlugin().
                    getDescription().getName() + ", one of you incredible good plugins");
        }
    }

    @EventHandler
    public void onReload(ProxyReloadEvent event) {
        plugin.reload();
    }
}
