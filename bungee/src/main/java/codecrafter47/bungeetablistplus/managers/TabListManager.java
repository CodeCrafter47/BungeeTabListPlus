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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.config.ConfigParser;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import codecrafter47.bungeetablistplus.config.TabListProvider;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.ServerKickEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import static net.md_5.bungee.event.EventPriority.HIGHEST;

public class TabListManager implements Listener {

    private final BungeeTabListPlus plugin;
    private TabListProvider defaultTab;
    private final List<TabListProvider> tabLists = new ArrayList<>();

    private static final Map<ProxiedPlayer, ITabListProvider> customTabLists = new HashMap<>();

    public TabListManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin.getPlugin(), this);
    }

    // returns true on success
    public boolean loadTabLists() {
        try {
            if (!plugin.getConfigManager().defaultTabList.showTo.
                    equalsIgnoreCase("all")) {
                plugin.getLogger().warning(
                        "The default tabList is configured not to be shown by default");
                plugin.getLogger().warning(
                        "This is not recommended and you should not do this if you're not knowing exaclty what you are doing");
            }
            validateShowTo(plugin.getConfigManager().defaultTabList);
            defaultTab = new ConfigParser(
                    plugin.getConfigManager().defaultTabList, plugin).parse();
        } catch (Throwable ex) {
            plugin.getLogger().log(Level.SEVERE, "Could not load default tabList", ex);
            plugin.getLogger().log(Level.WARNING, "Disabling plugin");
            return false;
        }
        for (TabListConfig c : plugin.getConfigManager().tabLists) {
            try {
                validateShowTo(c);
                tabLists.add(new ConfigParser(c, plugin).parse());
            } catch (Throwable ex) {
                plugin.getLogger().log(Level.SEVERE, "Could not load " + c.getFileName(), ex);
            }
        }
        return true;
    }

    public ITabListProvider getTabListForPlayer(ProxiedPlayer player) {
        if (customTabLists.get(player) != null) return customTabLists.get(player);
        for (TabListProvider tabList : tabLists) {
            if (tabList.appliesTo(player)) {
                return tabList;
            }
        }
        if (defaultTab != null && defaultTab.appliesTo(player)) {
            return defaultTab;
        }
        return null;
    }

    private void validateShowTo(TabListConfig config) {
        String showTo = config.showTo;

        if (showTo.equalsIgnoreCase("ALL")) {
            return;
        }

        if (showTo.equalsIgnoreCase("1.7")) {
            return;
        }

        if (showTo.equalsIgnoreCase("1.8")) {
            return;
        }

        String s[] = showTo.split(":");

        if (s.length != 2) {
            invalidShowTo(config);
        }

        if (s[0].equalsIgnoreCase("player")) {
            if (s[1].contains(",")) {
                invalidShowTo(config);
            }
            return;
        }

        if (s[0].equalsIgnoreCase("players")) {
            return;
        }

        if (s[0].equalsIgnoreCase("server")) {
            if (s[1].contains(",")) {
                invalidShowTo(config);
            }
            if (plugin.isServer(s[1])) {
                return;
            } else {

                invalidShowTo(config);
            }
        }

        if (s[0].equalsIgnoreCase("servers")) {
            for (String sv : s[1].split(",")) {
                if (!plugin.isServer(sv)) {

                    invalidShowTo(config);
                }
            }
            return;
        }

        if (s[0].equalsIgnoreCase("group")) {
            if (s[1].contains(",")) {
                invalidShowTo(config);
            }
            return;
        }

        if (s[0].equals("groups")) {
            return;
        }

        invalidShowTo(config);
    }

    private void invalidShowTo(TabListConfig config) {
        plugin.getLogger().log(
                Level.WARNING, "{0}{1}: showTo is partly or completely invalid",
                new Object[]{ChatColor.RED,
                        config.getFileName()});
    }

    public static void setCustomTabList(ProxiedPlayer player, ITabListProvider tabList) {
        customTabLists.put(player, tabList);
    }

    public static void removeCustomTabList(ProxiedPlayer player) {
        customTabLists.remove(player);
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        if (customTabLists.containsKey(event.getPlayer())) customTabLists.remove(event.getPlayer());
    }

    @EventHandler(priority = HIGHEST)
    public void onDisconnect(ServerKickEvent event) {
        if (event.isCancelled()) return;
        if (customTabLists.containsKey(event.getPlayer())) customTabLists.remove(event.getPlayer());
    }
}
