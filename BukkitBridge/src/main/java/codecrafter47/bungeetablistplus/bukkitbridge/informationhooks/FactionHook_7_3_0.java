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

import codecrafter47.bungeetablistplus.bukkitbridge.api.PlayerInformationProvider;
import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Florian Stober
 */
public class FactionHook_7_3_0 implements PlayerInformationProvider {

    @Override
    public Map<String, Object> getInformation(Player player) {
        Map<String, Object> map = new HashMap<>();
        MPlayer uplayer = MPlayer.get(player);
        map.put("factionName", uplayer.getFactionName());
        Faction faction = uplayer.getFaction();
        map.put("onlineFactionMembers", faction.getOnlinePlayers());
        faction = BoardColl.get().
                getFactionAt(PS.valueOf(player.getLocation()));
        if (faction != null) {
            map.put("factionsWhere", faction.getName());
        }
        return map;
    }

}
