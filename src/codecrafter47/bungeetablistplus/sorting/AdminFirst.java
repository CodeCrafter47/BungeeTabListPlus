/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package codecrafter47.bungeetablistplus.sorting;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class AdminFirst implements ISortingRule{

    @Override
    public int compare(ProxiedPlayer player1, ProxiedPlayer player2) {
        return BungeeTabListPlus.getInstance().getPermissionManager().comparePlayers(player1, player2);
    }
    
}
