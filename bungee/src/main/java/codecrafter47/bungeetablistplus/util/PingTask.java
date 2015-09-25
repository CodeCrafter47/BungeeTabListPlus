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

package codecrafter47.bungeetablistplus.util;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.config.ServerInfo;

public class PingTask implements Runnable {

    private final ServerInfo server;
    private boolean online = true;
    private int maxPlayers = Integer.MAX_VALUE;
    private int onlinePlayers = 0;

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
            maxPlayers = v.getPlayers().getMax();
            onlinePlayers = v.getPlayers().getOnline();
        });
    }
}
