/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.tab.TabListAdapter;

/**
 *
 * @author florian
 */
public class CustomTabListHandler extends TabListAdapter {

    boolean isExcluded = false;

    private final Collection<String> usernames = new HashSet<>();
    public final List<String> bukkitplayers = new ArrayList<>(100);

    @Override
    public void onServerChange() {
        // remove all those names from the clients tab, he's on another server now
        synchronized (usernames) {
            for (String username : usernames) {
                BungeeTabListPlus.getInstance().getPacketManager().removePlayer(getPlayer().unsafe(), username);
            }
            usernames.clear();
        }
        synchronized (bukkitplayers) {
            bukkitplayers.clear();
        }
        isExcluded = false;
    }

    @Override
    public boolean onListUpdate(String name, boolean online, int ping) {
        if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().autoExcludeServers && !ChatColor.stripColor(name).equals(name)) {
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

        if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().excludeServers.contains(getPlayer().getServer().getInfo().getName()) || isExcluded) {
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
        }
    }

    public void exclude() {
        isExcluded = true;
        synchronized (bukkitplayers) {
            synchronized (usernames) {
                for (String s : bukkitplayers) {
                    if (!usernames.contains(s)) {
                        BungeeTabListPlus.getInstance().getPacketManager().createOrUpdatePlayer(getPlayer().unsafe(), s, 0);
                        usernames.add(s);
                    }
                }
            }
        }
    }
}
