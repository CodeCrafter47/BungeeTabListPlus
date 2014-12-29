/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package codecrafter47.bungeetablistplus.bukkitbridge.informationhooks;

import codecrafter47.bungeetablistplus.bukkitbridge.BukkitBridge;
import codecrafter47.bungeetablistplus.bukkitbridge.api.PlayerInformationProvider;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

public class PlayerPointsHook implements PlayerInformationProvider {

    BukkitBridge plugin;

    public PlayerPointsHook(BukkitBridge plugin) {
        this.plugin = plugin;
    }

    @Override
    public Map<String, Object> getInformation(Player player) {
        PlayerPointsAPI playerPoints = ((PlayerPoints) plugin.getServer().getPluginManager().getPlugin("PlayerPoints")).getAPI();
        Map<String, Object> map = new HashMap<>();
        map.put("playerPoints", playerPoints.look(player.getUniqueId()));
        return map;
    }
}
