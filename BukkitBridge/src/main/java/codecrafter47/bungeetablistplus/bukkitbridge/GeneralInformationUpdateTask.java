/*
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
package codecrafter47.bungeetablistplus.bukkitbridge;

import codecrafter47.bungeetablistplus.bukkitbridge.api.GeneralInformationProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.Map.Entry;

/**
 *
 * @author Florian Stober
 */
public class GeneralInformationUpdateTask extends BukkitRunnable {

    BukkitBridge plugin;

    Map<String, Object> buffer = new HashMap<>();

    boolean initialized = false;

    public GeneralInformationUpdateTask(BukkitBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Collection<? extends Player> players = Bukkit.getOnlinePlayers();

        // a player is required to be online
        if (players == null) {
            return;
        }
        if (players.size() == 0) {
            return;
        }

        Player p = players.iterator().next();

        if (!initialized) {
            init(p);
        } else {
            update(p);
        }
    }

    public void setInitialized(boolean b) {
        this.initialized = b;
    }

    private void init(Player player) {
        this.buffer.clear();
        List<GeneralInformationProvider> remove = new ArrayList<>();
        for (GeneralInformationProvider ip : plugin.
                getGeneralInformationProviders()) {
            try {
                buffer.putAll(ip.getInformation());
            } catch (Throwable th) {
                plugin.reportError(th);
                remove.add(ip);
            }
        }
        plugin.sendInformation(Constants.subchannel_init, buffer, player);
        initialized = true;
        for(GeneralInformationProvider ip: remove){
            plugin.generalInformationProviders.remove(ip);
        }
    }

    private void update(Player player) {
        Map<String, Object> updates = new HashMap<>();
        List<GeneralInformationProvider> remove = new ArrayList<>();
        for (GeneralInformationProvider ip : plugin.
                getGeneralInformationProviders()) {
            try {
                for (Entry<String, Object> entry : ip.getInformation().
                        entrySet()) {
                    if (!buffer.containsKey(entry.getKey()) || !buffer.get(
                            entry.
                            getKey()).equals(entry.getValue())) {
                        updates.put(entry.getKey(), entry.getValue());
                        buffer.put(entry.getKey(), entry.getValue());
                    }
                }
            } catch (Throwable th) {
                plugin.reportError(th);
                remove.add(ip);
            }
        }
        if (!updates.isEmpty()) {
            plugin.sendInformation(Constants.subchannel_update, updates, player);
        }
        for(GeneralInformationProvider ip: remove){
            plugin.generalInformationProviders.remove(ip);
        }
    }

}
