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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.packets.*;
import net.md_5.bungee.api.connection.Connection;

/**
 * @author Florian Stober
 */
public class PacketManager {

    ITeamPacket teamPacket;
    IPlayerListPacket playerListPacket;

    public PacketManager() {
        Class clazz;

        try {
            try {
                clazz = Class.forName("net.md_5.bungee.protocol.packet.Team");
                if (clazz != null) {
                    clazz.getMethod("setName", String.class);
                    clazz.getMethod("setMode", byte.class);
                    clazz.getMethod("setPrefix", String.class);
                    clazz.getMethod("setSuffix", String.class);
                    clazz.getMethod("setDisplayName", String.class);
                    clazz.getMethod("setDisplayName", String.class);
                    clazz.getMethod("setPlayers", String[].class);
                    try {
                        clazz.getMethod("setPlayerCount", short.class);
                        teamPacket = new OldTeamPacket();
                    } catch (NoSuchMethodException ex) {
                        teamPacket = new NewTeamPacket();
                    }
                }
            } catch (ClassNotFoundException ex) {
                clazz = Class.forName(
                        "net.md_5.bungee.protocol.packet.PacketD1Team");
                teamPacket = new TeamPacket16();
            }
        } catch (Throwable th) {
            teamPacket = null;
        }

        try {
            try {
                clazz = Class.forName(
                        "net.md_5.bungee.protocol.packet.PlayerListItem");
                if (clazz != null) {
                    clazz.getMethod("setUsername", String.class);
                    clazz.getMethod("setOnline", boolean.class);
                    try {
                        clazz.getMethod("setPing", short.class);
                        playerListPacket = new OldPlayerListPacket();
                    } catch (NoSuchMethodException ex) {
                        playerListPacket = new NewPlayerListPacket();
                    }
                }
            } catch (ClassNotFoundException ex) {
                clazz = Class.forName(
                        "net.md_5.bungee.protocol.packet.PacketC9PlayerListItem");
                playerListPacket = new PlayerListPacket16();
            }
        } catch (Throwable th) {
            playerListPacket = null;
        }
    }

    public void createTeam(Connection.Unsafe connection, String player) {
        teamPacket.createTeam(connection, player);
    }

    public void updateTeam(Connection.Unsafe connection, String player,
                           String prefix, String displayname, String suffix) {
        teamPacket.updateTeam(connection, player, prefix, displayname, suffix);
    }

    public void removeTeam(Connection.Unsafe connection, String player) {
        teamPacket.removeTeam(connection, player);
    }

    public void createOrUpdatePlayer(Connection.Unsafe connection, String player,
                                     int ping) {
        playerListPacket.createOrUpdatePlayer(connection, player, ping);
    }

    public void removePlayer(Connection.Unsafe connection, String player) {
        playerListPacket.removePlayer(connection, player);
    }

    public boolean isScoreboardSupported() {
        return teamPacket != null;
    }

    public boolean isTabModificationSupported() {
        return playerListPacket != null;
    }
}
