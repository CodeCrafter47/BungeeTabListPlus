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
public interface ISortingRule {

    /**
     * return 1: player1 before player2 return 0: invoke next sorting rule
     * return -1: player2 before player1
     *
     */
    public int compare(ProxiedPlayer player1, ProxiedPlayer player2);
}
