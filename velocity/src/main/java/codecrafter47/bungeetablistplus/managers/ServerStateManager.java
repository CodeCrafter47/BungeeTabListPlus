/*
 *     Copyright (C) 2025 proferabg
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
import codecrafter47.bungeetablistplus.util.VelocityPlugin;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.server.RegisteredServer;
import com.velocitypowered.api.proxy.server.ServerInfo;
import com.velocitypowered.api.proxy.server.ServerPing;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.NonNull;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ServerStateManager {

    private MainConfig config;
    private final VelocityPlugin plugin;

    private final Map<String, PingTask> serverState = new HashMap<>();

    public ServerStateManager(MainConfig config, VelocityPlugin plugin) {
        this.config = config;
        this.plugin = plugin;
    }

    public boolean isOnline(@Nonnull @NonNull String name) {
        PingTask state = getServerState(name);
        return (state != null && state.isOnline()) || hasPlayers(name);
    }

    private boolean hasPlayers(String name) {
        RegisteredServer server = plugin.getProxy().getServer(name).orElse(null);
        if (server == null)
            return false;
        Collection<Player> players = server.getPlayersConnected();
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
        RegisteredServer server = plugin.getProxy().getServer(serverName).orElse(null);
        if (server != null) {
            // start server ping tasks
            int delay = config.pingDelay;
            if (delay <= 0 || delay > 10) {
                delay = 10;
            }
            task = new PingTask(plugin, server);
            serverState.put(serverName, task);
            task.task = plugin.getProxy().getScheduler().buildTask(plugin, task).delay(delay, TimeUnit.SECONDS).repeat(delay, TimeUnit.SECONDS).schedule();
        }
        return task;
    }

    public static class PingTask implements Runnable {

        private final VelocityPlugin plugin;
        private final RegisteredServer server;
        private boolean online = true;
        private int maxPlayers = Integer.MAX_VALUE;
        private int onlinePlayers = 0;
        private ScheduledTask task;

        public PingTask(VelocityPlugin plugin, RegisteredServer server) {
            this.plugin = plugin;
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
            if (plugin.getProxy().isShuttingDown()) return;
            server.ping().whenComplete((serverPing, throwable) -> {
                if (throwable != null) {
                    online = false;
                    return;
                }
                if (serverPing == null) {
                    PingTask.this.online = false;
                    return;
                }
                online = true;
                ServerPing.Players players = serverPing.getPlayers().orElse(null);
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
