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

package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.FakePlayer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.tab.TabListAdapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

/**
 * @author Florian Stober
 */
public class CustomTabListHandler extends TabListAdapter implements PlayerTablistHandler {

    boolean isExcluded = false;
    private ConnectedPlayer connectedPlayer = null;

    private final Collection<String> usernames = new HashSet<>();
    public final List<String> bukkitplayers = new ArrayList<>(100);

    private TabListHandler tabListHandler;

    public CustomTabListHandler(ProxiedPlayer player) {
        init(player);
    }

    public void setTabListHandler(TabListHandler tabListHandler) {
        if (this.tabListHandler != null) {
            this.tabListHandler.unload();
        }
        this.tabListHandler = tabListHandler;
    }

    @Override
    public void onConnect() {
        if (connectedPlayer == null) {
            System.out.println("connect: " + getPlayer());
            connectedPlayer = new ConnectedPlayer(getPlayer());
            BungeeTabListPlus.getInstance().getConnectedPlayerManager().onPlayerConnected(connectedPlayer);
        }
    }

    @Override
    public void onDisconnect() {
        if (connectedPlayer != null) {
            BungeeTabListPlus.getInstance().getConnectedPlayerManager().onPlayerDisconnected(connectedPlayer);
        }
    }

    @Override
    public void onServerChange() {
        try {
            // remove all those names from the clients tab, he's on another server now
            synchronized (usernames) {
                for (String username : usernames) {
                    BungeeTabListPlus.getInstance().getLegacyPacketAccess().removePlayer(
                            getPlayer().unsafe(), username);
                }
                usernames.clear();
            }
            synchronized (bukkitplayers) {
                bukkitplayers.clear();
            }
            isExcluded = false;
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @Override
    public boolean onListUpdate(String name, boolean online, int ping) {
        try {
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
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
            return false;
        }
    }

    public void exclude() {
        if (!isExcluded) {
            synchronized (bukkitplayers) {
                synchronized (usernames) {
                    for (String s : bukkitplayers) {
                        if (!usernames.contains(s)) {
                            BungeeTabListPlus.getInstance().getLegacyPacketAccess().
                                    createOrUpdatePlayer(getPlayer().unsafe(), s, 0);
                            usernames.add(s);
                        }
                    }
                }
            }
            tabListHandler.unload();
        }
        isExcluded = true;
    }

    @Override
    public void sendTablist(TabList tabList) {
        if (tabListHandler instanceof ScoreboardTabList) {
            if (!BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().useScoreboardToBypass16CharLimit) {
                setTabListHandler(new MyTabList(this));
            }
        } else if (tabListHandler instanceof MyTabList) {
            if (BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().useScoreboardToBypass16CharLimit) {
                setTabListHandler(new ScoreboardTabList(this));
            }
        }
        tabListHandler.sendTabList(tabList);
    }

    @Override
    public boolean isExcluded() {
        return isExcluded;
    }

    @Override
    public List<IPlayer> getPlayers() {
        List<IPlayer> bukkitPlayers = new ArrayList<>();
        for (String s : bukkitplayers) {
            bukkitPlayers.add(new FakePlayer(s, getPlayer().getServer() != null ? getPlayer().getServer().getInfo() : null, false));
        }
        return bukkitPlayers;
    }
}