/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package codecrafter47.bungeetablistplus.sorting;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class YouFirst implements ISortingRule{
    ProxiedPlayer player;
    
    public YouFirst(ProxiedPlayer p){
        player = p;
    }

    @Override
    public int compare(ProxiedPlayer player1, ProxiedPlayer player2) {
        if(player1 == player)return 1;
        if(player2 == player)return -1;
        return 0;
    }
    
}
