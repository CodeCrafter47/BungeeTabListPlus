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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class PlayerManager {

    private final BungeeTabListPlus plugin;

    public PlayerManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public boolean isServer(String s) {
        for (ServerInfo server : ProxyServer.getInstance().getServers().values()) {
            if (s.equalsIgnoreCase(server.getName())) {
                return true;
            }
            int i = s.indexOf('#');
            if (i > 1) {
                if (s.substring(0, i).equalsIgnoreCase(server.getName())) {
                    return true;
                }
            }
        }
        return false;
    }

    public List<ProxiedPlayer> getPlayers(Collection<String> filter,
            ProxiedPlayer who) {
        List<ProxiedPlayer> list = new ArrayList<>();
        Collection<ProxiedPlayer> players = new ArrayList<>(plugin.getProxy().getPlayers());
        players.addAll(BungeeTabListPlus.getInstance().getFakePlayerManager().getFakePlayers());
        for (ProxiedPlayer p : players) {
            boolean areGroupRules = false;
            boolean areServerRules = false;
            boolean fitGroupRules = false;
            boolean fitServerRules = false;
            String group = plugin.getPermissionManager().getMainGroup(p);
            for (String rule : filter) {
                if (rule.isEmpty()) {
                    // ignore
                } else if (rule.equalsIgnoreCase("currentserver")) {
                    areServerRules = true;
                    if (p.getServer() != null && who.getServer() != null) {
                        if (p.getServer().getInfo().getName().equalsIgnoreCase(
                                who.getServer().getInfo().getName())) {
                            fitServerRules = true;
                        }
                    }
                } else if (isServer(rule)) {
                    areServerRules = true;
                    Server server = p.getServer();
                    if (server != null) {
                        if (server.getInfo().getName().equalsIgnoreCase(rule)) {
                            fitServerRules = true;
                        }
                        String[] s = rule.split("#");
                        if (s.length == 2) {
                            if (server.getInfo().getName().
                                    equalsIgnoreCase(s[0])) {
                                String world = plugin.getBridge().
                                        getPlayerInformation(p, "world");
                                if (world != null) {
                                    if (world.equalsIgnoreCase(s[1])) {
                                        fitServerRules = true;
                                    }
                                }
                            }
                        }
                    }
                } else {
                    areGroupRules = true;
                    if (group != null) {
                        if (group.equalsIgnoreCase(rule)) {
                            fitGroupRules = true;
                        }
                    }
                }
            }
            if (((!areServerRules) || fitServerRules) && ((!areGroupRules) || fitGroupRules) && !BungeeTabListPlus.
                    isHidden(p)) {
                list.add(p);
            }
        }
        return list;
    }

    public int getServerPlayerCount(String server) {
        int num = 0;
        Collection<ProxiedPlayer> players = new ArrayList<>(plugin.getProxy().getPlayers());
        players.addAll(BungeeTabListPlus.getInstance().getFakePlayerManager().getFakePlayers());
        for (ProxiedPlayer p : players) {
            Server s = p.getServer();
            if (s != null) {
                if (s.getInfo().getName().equalsIgnoreCase(server) && !BungeeTabListPlus.
                        isHidden(p)) {
                    num++;
                }
            }
        }
        return num;
    }

    public int getGlobalPlayerCount() {
        int num = 0;
        Collection<ProxiedPlayer> players = new ArrayList<>(plugin.getProxy().getPlayers());
        players.addAll(BungeeTabListPlus.getInstance().getFakePlayerManager().getFakePlayers());
        for (ProxiedPlayer p : players) {
            if (!BungeeTabListPlus.isHidden(p)) {
                num++;
            }
        }
        return num;
    }

    public int getPlayerCount(String args, ProxiedPlayer player) {
        String tmp = args.replaceAll(",", "+");
        String[] all = tmp.split("\\+");
        return this.getPlayers(Arrays.asList(all), player).size();
    }
}
