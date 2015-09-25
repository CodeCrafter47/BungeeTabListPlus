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

package codecrafter47.bungeetablistplus.data.bukkit;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.plugin.Plugin;

import java.util.function.Function;

class ServerTPSProvider implements Function<Server, Double>, Runnable {
    private static ServerTPSProvider instance = null;

    public static synchronized ServerTPSProvider getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new ServerTPSProvider(plugin);
        }
        return instance;
    }

    private ServerTPSProvider(Plugin plugin) {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, monitorInterval, monitorInterval);
    }

    @Override
    public Double apply(Server server) {
        return tps;
    }

    private final int monitorInterval = 40;
    private long prevtime;
    private double tps = 20;

    @Override
    public void run() {
        long time = System.currentTimeMillis();
        long elapsedtime = time - prevtime;
        double elapsedtimesec = (double) elapsedtime / 1000;
        tps = monitorInterval / elapsedtimesec;
        prevtime = time;
    }
}
