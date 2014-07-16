/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package codecrafter47.bungeetablistplus.variables;

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author florian
 */
public class UUIDVariable extends PlayerVariable{

    public UUIDVariable(String name) {
        super(name);
    }

    @Override
    public String getReplacement(ProxiedPlayer player) {
        return player.getUniqueId().toString();
    }
    
}
