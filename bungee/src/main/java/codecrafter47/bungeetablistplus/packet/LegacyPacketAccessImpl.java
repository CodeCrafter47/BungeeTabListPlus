/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.bungeetablistplus.packet;

import com.google.common.base.Preconditions;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

/**
 * @author Florian Stober
 */
public class LegacyPacketAccessImpl implements LegacyPacketAccess{

    @Override
    public void createTeam(Connection.Unsafe connection, String player) {
        Preconditions.checkArgument(player.length() <= 13);
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 0);
        t.setPrefix(" ");
        t.setDisplayName(" ");
        t.setSuffix(" ");
        t.setPlayers(new String[]{player});
        connection.sendPacket(t);
    }

    @Override
    public void updateTeam(Connection.Unsafe connection, String player,
                           String prefix, String displayname, String suffix) {
        Preconditions.checkArgument(player.length() <= 13);
        Preconditions.checkArgument(prefix.length() <= 16);
        Preconditions.checkArgument(displayname.length() <= 16);
        Preconditions.checkArgument(suffix.length() <= 16);
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
        Preconditions.checkArgument(player.length() <= 13);
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 1);
        connection.sendPacket(t);
    }

    @Override
    public void createOrUpdatePlayer(Connection.Unsafe connection, String player,
                                     int ping) {
        Preconditions.checkArgument(player.length() <= 16);
        PlayerListItem pli = new PlayerListItem();
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(player);
        item.setUsername(player);
        item.setPing(ping);
        pli.setItems(new PlayerListItem.Item[]{item});
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        connection.sendPacket(pli);
    }

    @Override
    public void removePlayer(Connection.Unsafe connection, String player) {
        Preconditions.checkArgument(player.length() <= 16);
        PlayerListItem pli = new PlayerListItem();
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(player);
        item.setUsername(player);
        item.setPing(9999);
        pli.setItems(new PlayerListItem.Item[]{item});
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        connection.sendPacket(pli);
    }

    @Override
    public boolean isScoreboardSupported() {
        return true;
    }

    @Override
    public boolean isTabModificationSupported() {
        return true;
    }
}
