/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.config.MainConfig;
import lombok.NonNull;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerStateManager {

    private MainConfig config;
    private final Plugin plugin;

    private final Map<String, PingTask> serverState = new HashMap<>();

    public ServerStateManager(MainConfig config, Plugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    public boolean isOnline(@Nonnull @NonNull String name) {
        PingTask state = getServerState(name);
        return (state != null && state.isOnline()) || hasPlayers(name);
    }

    private boolean hasPlayers(String name) {
        ServerInfo serverInfo = plugin.getProxy().getServerInfo(name);
        if (serverInfo == null)
            return false;
        Collection<ProxiedPlayer> players = serverInfo.getPlayers();
        if (players == null)
            return false;
        return !players.isEmpty();
    }

    public synchronized void updateConfig(MainConfig config) {
        this.serverState.values().forEach(pingTask -> pingTask.task.cancel());
        this.serverState.clear();
        this.config = config;
    }

    private synchronized PingTask getServerState(String serverName) {
        PingTask task = serverState.get(serverName);
        if (task != null) {
            return task;
        }
        ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(serverName);
        if (serverInfo != null) {
            // start server ping tasks
            int delay = config.pingDelay;
            if (delay <= 0 || delay > 10) {
                delay = 10;
            }
            task = new PingTask(serverInfo);
            serverState.put(serverName, task);
            task.task = plugin.getProxy().getScheduler().schedule(plugin, task, delay, delay, TimeUnit.SECONDS);
        }
        return task;
    }

    public static class PingTask implements Runnable {

        private final ServerInfo server;
        private boolean online = true;
        private int maxPlayers = Integer.MAX_VALUE;
        private int onlinePlayers = 0;
        private ScheduledTask task;

        public PingTask(ServerInfo server) {
            this.server = server;
        }

        public boolean isOnline() {
            return online;
        }

        public int getMaxPlayers() {
            return maxPlayers;
        }

        public int getOnlinePlayers() {
            return onlinePlayers;
        }

        @Override
        public void run() {
            if (!BungeeCord.getInstance().isRunning) return;
            server.ping((v, thrwbl) -> {
                if (thrwbl != null) {
                    online = false;
                    return;
                }
                if (v == null) {
                    PingTask.this.online = false;
                    return;
                }
                online = true;
                ServerPing.Players players = v.getPlayers();
                if (players != null) {
                    maxPlayers = players.getMax();
                    onlinePlayers = players.getOnline();
                } else {
                    maxPlayers = 0;
                    onlinePlayers = 0;
                }
            });
        }
    }
}
