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
import codecrafter47.bungeetablistplus.tablisthandler.CustomTabList18;
import codecrafter47.bungeetablistplus.tablisthandler.CustomTabListHandler;
import codecrafter47.bungeetablistplus.tablisthandler.MyTabList;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.tablisthandler.ScoreboardTabList;
import codecrafter47.bungeetablistplus.tablisthandler.TabList18;
import codecrafter47.bungeetablistplus.tablisthandler.TabListHandler;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ProxyReloadEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

public class TabListListener implements Listener {

    private final BungeeTabListPlus plugin;

    public TabListListener(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerJoin(PostLoginEvent e) {
        try {
            PlayerTablistHandler tab;
            TabListHandler handler;
            if (!BungeeTabListPlus.isVersion18()) {
                tab = new CustomTabListHandler(e.getPlayer());
                if (!plugin.getConfigManager().getMainConfig().useScoreboardToBypass16CharLimit) {
                    handler = new MyTabList(tab);
                } else {
                    handler = new ScoreboardTabList(tab);
                }
            } else {
                CustomTabList18 customTabList18 = new CustomTabList18(e.getPlayer());
                customTabList18.onConnect();
                tab = customTabList18;
                if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(e.getPlayer())) {
                    handler = new TabList18(tab);
                } else {
                    if (!plugin.getConfigManager().getMainConfig().useScoreboardToBypass16CharLimit) {
                        handler = new MyTabList(tab);
                    } else {
                        handler = new ScoreboardTabList(tab);
                    }
                }
            }
            tab.setTabListHandler(handler);
            BungeeTabListPlus.setTabList(e.getPlayer(), tab);

            if (plugin.getConfigManager().getMainConfig().updateOnPlayerJoinLeave) {
                plugin.resendTabLists();
            }
            plugin.updateTabListForPlayer(e.getPlayer());
        } catch (Throwable th){
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent e) {
        plugin.updateTabListForPlayer(e.getPlayer());
        if (plugin.getConfigManager().getMainConfig().updateOnServerChange) {
            plugin.resendTabLists();
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerDisconnectEvent e) {
        if (plugin.getConfigManager().getMainConfig().updateOnPlayerJoinLeave) {
            plugin.resendTabLists();
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
