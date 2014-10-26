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
package codecrafter47.bungeetablistplus.bukkitbridge.informationhooks;

import codecrafter47.bungeetablistplus.bukkitbridge.BukkitBridge;
import codecrafter47.bungeetablistplus.bukkitbridge.api.GeneralInformationProvider;
import codecrafter47.bungeetablistplus.bukkitbridge.api.PlayerInformationProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Florian Stober
 */
public class BukkitHook implements GeneralInformationProvider,
        PlayerInformationProvider {

    BukkitBridge plugin;

    public BukkitHook(BukkitBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public Map<String, Object> getInformation() {
        Map<String, Object> map = new HashMap<>();
        map.put("bridgeVersion", plugin.getDescription().getVersion());
        map.
                put("hasVault",
                        Bukkit.getPluginManager().getPlugin("Vault") != null);
        map.put("hasVanishNoPacket", Bukkit.getPluginManager().getPlugin(
                "VanishNoPacket") != null);
        return map;
    }

    @Override
    public Map<String, Object> getInformation(Player player) {
        Map<String, Object> map = new HashMap<>();
        map.put("tabName", player.getPlayerListName());
        map.put("displayName", player.getDisplayName());
        map.put("bungeetablistplus.admin", player.hasPermission(
                "bungeetablistplus.admin"));
        map.put("bungeetablistplus.help", player.hasPermission(
                "bungeetablistplus.help"));
        map.put("bungeetablistplus.vanish", player.hasPermission(
                "bungeetablistplus.vanish"));
        map.put("world", player.getWorld().getName());
        map.put("health", player.getHealth());
        map.put("level", player.getLevel());
        return map;
    }

}
