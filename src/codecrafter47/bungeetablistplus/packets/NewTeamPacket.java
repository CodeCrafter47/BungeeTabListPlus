/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.packets;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.Team;

/**
 *
 * @author florian
 */
public class NewTeamPacket implements ITeamPacket {

    @Override
    public void createTeam(Connection.Unsafe connection, String player) {
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 0);
        t.setPrefix(" ");
        t.setDisplayName(" ");
        t.setSuffix(" ");
        t.setPlayers(new String[]{player});
        // TODO FIXME
        //t.setNameTagVisibility("never");
        connection.sendPacket(t);
    }

    @Override
    public void updateTeam(Connection.Unsafe connection, String player,
            String prefix, String displayname, String suffix) {
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 2);
        t.setPrefix(prefix);
        t.setDisplayName(displayname);
        t.setSuffix(suffix);
        connection.sendPacket(t);
    }

    @Override
    public void removeTeam(Connection.Unsafe connection, String player) {
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 1);
        connection.sendPacket(t);
    }
}
