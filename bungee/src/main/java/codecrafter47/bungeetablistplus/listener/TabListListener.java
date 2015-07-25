/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.listener;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablisthandler.MyTabList;
import codecrafter47.bungeetablistplus.tablisthandler.ScoreboardTabList;
import codecrafter47.bungeetablistplus.tablisthandler.TabList18;
import codecrafter47.bungeetablistplus.tablisthandler.TabList18v3;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

public class TabListListener implements Listener {

    private final BungeeTabListPlus plugin;

    public TabListListener(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PostLoginEvent e) throws NoSuchFieldException,
            IllegalArgumentException, IllegalAccessException {
        Object tab;
        if (!BungeeTabListPlus.isVersion18() || e.getPlayer().getPendingConnection().getVersion() < 47) {
            if (!plugin.getConfigManager().getMainConfig().useScoreboardToBypass16CharLimit) {
                tab = new MyTabList(e.getPlayer());
                if (plugin.getConfigManager().getMainConfig().updateOnPlayerJoinLeave) {
                    plugin.resendTabLists();
                }
                plugin.sendImmediate(e.getPlayer());
            } else {
                tab = new ScoreboardTabList(e.getPlayer());
            }
        } else {
            if (ConfigManager.getTabSize() >= 80 && !plugin.getConfigManager().getMainConfig().autoShrinkTabList) {
                tab = new TabList18(e.getPlayer());
            } else {
                tab = new TabList18v3(e.getPlayer());
            }
        }
        ProxiedPlayer player = e.getPlayer();
        BungeeTabListPlus.setTabList(player, tab);
    }

    @EventHandler
    public void onPlayerJoin(ServerConnectedEvent e) {
        plugin.sendImmediate(e.getPlayer());
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
}
