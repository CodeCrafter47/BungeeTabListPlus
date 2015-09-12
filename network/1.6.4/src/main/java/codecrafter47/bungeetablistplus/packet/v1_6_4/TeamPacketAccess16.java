/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.packet.v1_6_4;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;

import codecrafter47.bungeetablistplus.packet.LegacyPacketAccess;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.PacketD1Team;

/**
 * @author Florian Stober
 */
public class TeamPacketAccess16 implements LegacyPacketAccess.TeamPacketAccess {
    private final Logger logger;

    public TeamPacketAccess16(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void createTeam(Connection.Unsafe connection, String player) {
        try {
            PacketD1Team t = new PacketD1Team("TAB" + player);
            Field mode = t.getClass().getDeclaredField("mode");
            mode.setAccessible(true);
            mode.set(t, (byte) 0);

            Field players = t.getClass().getDeclaredField("players");
            players.setAccessible(true);
            players.set(t, new String[]{player});

            Field prefix = t.getClass().getDeclaredField("prefix");
            prefix.setAccessible(true);
            prefix.set(t, " ");

            Field suffix = t.getClass().getDeclaredField("suffix");
            suffix.setAccessible(true);
            suffix.set(t, " ");

            Field displayName = t.getClass().getDeclaredField("displayName");
            displayName.setAccessible(true);
            displayName.set(t, " ");

            Field playerCount = t.getClass().getDeclaredField("playerCount");
            playerCount.setAccessible(true);
            playerCount.set(t, (short) 1);

            connection.sendPacket(t);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Failed to send create team packet", ex);
        }
    }

    @Override
    public void updateTeam(Connection.Unsafe connection, String player, String gprefix, String displayname, String gsuffix) {
        try {
            PacketD1Team t = new PacketD1Team("TAB" + player);
            Field mode = t.getClass().getDeclaredField("mode");
            mode.setAccessible(true);
            mode.set(t, (byte) 2);

            Field prefix = t.getClass().getDeclaredField("prefix");
            prefix.setAccessible(true);
            prefix.set(t, gprefix);

            Field suffix = t.getClass().getDeclaredField("suffix");
            suffix.setAccessible(true);
            suffix.set(t, gsuffix);

            Field displayName = t.getClass().getDeclaredField("displayName");
            displayName.setAccessible(true);
            displayName.set(t, displayname);
            connection.sendPacket(t);
        } catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException ex) {
            logger.log(Level.SEVERE, "Failed to send update team packet", ex);
        }
    }

    @Override
    public void removeTeam(Connection.Unsafe connection, String player) {
        PacketD1Team t = new PacketD1Team("TAB" + player);
        connection.sendPacket(t);
    }

}
