/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.PlayerVariable;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class GroupVariable implements PlayerVariable {

    @Override
    public String getReplacement(String args, ProxiedPlayer player) {
        return BungeeTabListPlus.getInstance().getPermissionManager().
                getMainGroup(player);
    }

}
