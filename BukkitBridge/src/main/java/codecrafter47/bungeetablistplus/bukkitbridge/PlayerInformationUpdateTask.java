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

import codecrafter47.bungeetablistplus.bukkitbridge.api.PlayerInformationProvider;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Florian Stober
 */
public class PlayerInformationUpdateTask extends BukkitRunnable {

    Player player;

    BukkitBridge plugin;

    Map<String, Object> buffer = new HashMap<>();

    boolean initialized = false;

    public PlayerInformationUpdateTask(BukkitBridge plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
    }

    @Override
    public void run() {
        if (!player.isOnline()) {
            this.cancel();
            return;
        }

        if (!initialized) {
            init();
        } else {
            update();
        }
    }

    public void setInitialized(boolean b) {
        this.initialized = b;
    }

    private void init() {
        this.buffer.clear();
        List<PlayerInformationProvider> remove = new ArrayList<>();
        for (PlayerInformationProvider ip : plugin.
                getPlayerInformationProviders()) {
            try {
                buffer.putAll(ip.getInformation(player));
            } catch (Throwable th) {
                plugin.reportError(th);
                remove.add(ip);
            }
        }
        plugin.sendInformation(Constants.subchannel_initplayer, buffer, player);
        initialized = true;
        for(PlayerInformationProvider ip: remove){
            plugin.playerInformationProviders.remove(ip);
        }
    }

    private void update() {
        Map<String, Object> updates = new HashMap<>();
        List<PlayerInformationProvider> remove = new ArrayList<>();
        for (PlayerInformationProvider ip : plugin.
                getPlayerInformationProviders()) {
            try {
                for (Map.Entry<String, Object> entry
                        : ip.getInformation(player).
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
            plugin.sendInformation(Constants.subchannel_updateplayer, updates,
                    player);
        }
        for(PlayerInformationProvider ip: remove){
            plugin.playerInformationProviders.remove(ip);
        }
    }
}
