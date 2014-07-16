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
public class BalanceVariable extends PlayerVariable{

    public BalanceVariable() {
        super("balance");
    }

    @Override
    public String getReplacement(ProxiedPlayer player) {
        String balance = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(player, "balance");
        if(balance == null)return "";
        return balance;
    }
    
}
