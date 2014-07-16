/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class WorldVariable extends PlayerVariable{

    public WorldVariable() {
        super("world");
    }

    @Override
    public String getReplacement(ProxiedPlayer player) {
        String world = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(player, "world");
        if(world == null)return "";
        return world;
    }
    
}
