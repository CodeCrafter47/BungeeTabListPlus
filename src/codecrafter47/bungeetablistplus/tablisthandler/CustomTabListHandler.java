/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.md_5.bungee.api.ChatColor;

/**
 *
 * @author Florian Stober
 */
public class CustomTabListHandler {

    boolean isExcluded = false;

    private final Collection<String> usernames = new HashSet<>();
    public final List<String> bukkitplayers = new ArrayList<>(100);

    public void onServerChange() {/*
         // remove all those names from the clients tab, he's on another server now
         synchronized (usernames) {
         for (String username : usernames) {
         BungeeTabListPlus.getInstance().getPacketManager().removePlayer(
         getPlayer().unsafe(), username);
         }
         usernames.clear();
         }
         synchronized (bukkitplayers) {
         bukkitplayers.clear();
         }
         isExcluded = false;*/

    }

    public boolean onListUpdate(String name, boolean online, int ping) {
        if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().autoExcludeServers && !ChatColor.
                stripColor(name).equals(name)) {
            exclude();
        }

        synchronized (bukkitplayers) {
            if (online) {
                if (!bukkitplayers.contains(name)) {
                    bukkitplayers.add(name);
                }
            } else {
                bukkitplayers.remove(name);
            }
        }
        /*
         if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().excludeServers.
         contains(getPlayer().getServer().getInfo().getName()) || isExcluded) {
         // save which packets are send to the client
         synchronized (usernames) {
         if (online) {
         if (!usernames.contains(name)) {
         usernames.add(name);
         }
         } else {
         usernames.remove(name);
         }
         }
         // Pass the Packet to the client
         return true;
         } else {
         // Don't pass the packet to the client, he will see the tabList provided by this plugin
         return false;
         }*/
        return true;
    }

    public void exclude() {
        /*
         isExcluded = true;
         synchronized (bukkitplayers) {
         synchronized (usernames) {
         for (String s : bukkitplayers) {
         if (!usernames.contains(s)) {
         BungeeTabListPlus.getInstance().getPacketManager().
         createOrUpdatePlayer(getPlayer().unsafe(), s, 0);
         usernames.add(s);
         }
         }
         }
         }
         */
    }
}
