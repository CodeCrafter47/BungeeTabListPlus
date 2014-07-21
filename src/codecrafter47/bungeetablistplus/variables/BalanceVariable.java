/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.PlayerVariable;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class BalanceVariable implements PlayerVariable {

    @Override
    public String getReplacement(String args, ProxiedPlayer player) {
        if (args == null) {
            String balance = BungeeTabListPlus.getInstance().getBridge().
                    getPlayerInformation(player, "balance");
            if (balance == null) {
                return "-";
            }
            return balance;
        } else {
            ProxiedPlayer player2 = ProxyServer.getInstance().getPlayer(args);
            if (player2 != null) {
                String balance = BungeeTabListPlus.getInstance().getBridge().
                        getPlayerInformation(player2, "balance");
                if (balance == null) {
                    return "-";
                }
                return balance;
            }
        }
        return "-";
    }

}
