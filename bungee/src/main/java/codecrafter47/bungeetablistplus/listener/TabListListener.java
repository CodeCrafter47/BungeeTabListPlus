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
import codecrafter47.bungeetablistplus.managers.BungeePlayerProvider;
import codecrafter47.bungeetablistplus.managers.TabViewManager;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import de.codecrafter47.taboverlay.TabView;
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
            BungeePlayer player = plugin.getBungeePlayerProvider().onPlayerConnected(e.getPlayer());
            TabView tabView = plugin.getTabViewManager().onPlayerJoin(e.getPlayer());
            plugin.getListener().onTabViewAdded(tabView, player);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDisconnect(PlayerDisconnectEvent e) {
        try {
            TabView tabView = plugin.getTabViewManager().onPlayerDisconnect(e.getPlayer());
            plugin.getListener().onTabViewRemoved(tabView);
            plugin.getBungeePlayerProvider().onPlayerDisconnected(e.getPlayer());

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
    public void onReload(ProxyReloadEvent event) {
        plugin.reload();
    }
}
