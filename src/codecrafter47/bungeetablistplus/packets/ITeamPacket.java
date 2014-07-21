/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.packets;

import net.md_5.bungee.api.connection.Connection.Unsafe;

/**
 *
 * @author florian
 */
public interface ITeamPacket {

    void createTeam(Unsafe connection, String player);

    void updateTeam(Unsafe connection, String player, String prefix,
            String displayname, String suffix);

    void removeTeam(Unsafe connection, String player);
}
