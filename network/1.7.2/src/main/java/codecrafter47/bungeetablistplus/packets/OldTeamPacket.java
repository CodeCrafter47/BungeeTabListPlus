/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
package codecrafter47.bungeetablistplus.packets;

import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.Team;

/**
 * @author Florian Stober
 */
public class OldTeamPacket implements ITeamPacket {

    @Override
    public void createTeam(Connection.Unsafe connection, String player) {
        Team t = new Team();
        t.setName(player);
        t.setMode((byte) 0);
        t.setPrefix(" ");
        t.setDisplayName(" ");
        t.setSuffix(" ");
        t.setPlayers(new String[]{player});
        t.setPlayerCount((short) 1);
        connection.sendPacket(t);
    }

    @Override
    public void updateTeam(Connection.Unsafe connection, String player, String prefix, String displayname, String suffix) {
        Team t = new Team();
        t.setName(player);
        t.setMode((byte) 2);
        t.setPrefix(prefix);
        t.setDisplayName(displayname);
        t.setSuffix(suffix);
        connection.sendPacket(t);
    }

    @Override
    public void removeTeam(Connection.Unsafe connection, String player) {
        Team t = new Team();
        t.setName(player);
        t.setMode((byte) 1);
        connection.sendPacket(t);
    }

}
