/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.packets;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.PlayerListItem;

/**
 *
 * @author florian
 */
public class NewPlayerListPacket implements IPlayerListPacket {

    @Override
    public void createOrUpdatePlayer(Connection.Unsafe connection, String player, int ping) {
        connection.sendPacket(new PlayerListItem(player, true, ping));
    }

    @Override
    public void removePlayer(Connection.Unsafe connection, String player) {
        connection.sendPacket(new PlayerListItem(player, false, 9999));
    }
}
