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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.packet.v1_6_4.PlayerListPacketAccess16;
import codecrafter47.bungeetablistplus.packet.v1_6_4.TeamPacketAccess16;
import codecrafter47.bungeetablistplus.packet.v1_7_10.NewPlayerListPacketAccess;
import codecrafter47.bungeetablistplus.packet.v1_7_10.NewTeamPacketAccess;
import codecrafter47.bungeetablistplus.packet.v1_7_2.OldPlayerListPacketAccess;
import codecrafter47.bungeetablistplus.packet.v1_7_2.OldTeamPacketAccess;
import codecrafter47.bungeetablistplus.packet.v1_8.legacy.PlayerListPacketAccess18;
import codecrafter47.bungeetablistplus.packet.v1_8.legacy.TeamPacketAccess18;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.connection.Connection;

/**
 * @author Florian Stober
 */
public class LegacyPacketAccessImpl implements LegacyPacketAccess{

    private TeamPacketAccess teamPacket;
    private PlayerListPacketAccess playerListPacket;

    public LegacyPacketAccessImpl() {
        Class clazz;

        if (BungeeTabListPlus.isVersion18()) {
            teamPacket = new TeamPacketAccess18();
            playerListPacket = new PlayerListPacketAccess18();
        } else {
            try {
                try {
                    clazz = Class.forName("net.md_5.bungee.protocol.packet.Team");
                    if (clazz != null) {
                        clazz.getMethod("setName", String.class);
                        clazz.getMethod("setMode", byte.class);
                        clazz.getMethod("setPrefix", String.class);
                        clazz.getMethod("setSuffix", String.class);
                        clazz.getMethod("setDisplayName", String.class);
                        clazz.getMethod("setPlayers", String[].class);
                        try {
                            clazz.getMethod("setPlayerCount", short.class);
                            teamPacket = new OldTeamPacketAccess();
                        } catch (NoSuchMethodException ex) {
                            teamPacket = new NewTeamPacketAccess();
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    clazz = Class.forName(
                            "net.md_5.bungee.protocol.packet.PacketD1Team");
                    teamPacket = new TeamPacketAccess16(BungeeTabListPlus.getInstance().getLogger());
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
                            playerListPacket = new OldPlayerListPacketAccess();
                        } catch (NoSuchMethodException ex) {
                            playerListPacket = new NewPlayerListPacketAccess();
                        }
                    }
                } catch (ClassNotFoundException ex) {
                    clazz = Class.forName(
                            "net.md_5.bungee.protocol.packet.PacketC9PlayerListItem");
                    playerListPacket = new PlayerListPacketAccess16();
                }
            } catch (Throwable th) {
                playerListPacket = null;
            }
        }
    }

    @Override
    public void createTeam(Connection.Unsafe connection, String player) {
        Preconditions.checkArgument(player.length() <= 13);
        teamPacket.createTeam(connection, player);
    }

    @Override
    public void updateTeam(Connection.Unsafe connection, String player,
                           String prefix, String displayname, String suffix) {
        Preconditions.checkArgument(player.length() <= 13);
        Preconditions.checkArgument(prefix.length() <= 16);
        Preconditions.checkArgument(displayname.length() <= 16);
        Preconditions.checkArgument(suffix.length() <= 16);
        teamPacket.updateTeam(connection, player, prefix, displayname, suffix);
    }

    @Override
    public void removeTeam(Connection.Unsafe connection, String player) {
        Preconditions.checkArgument(player.length() <= 13);
        teamPacket.removeTeam(connection, player);
    }

    @Override
    public void createOrUpdatePlayer(Connection.Unsafe connection, String player,
                                     int ping) {
        Preconditions.checkArgument(player.length() <= 16);
        playerListPacket.createOrUpdatePlayer(connection, player, ping);
    }

    @Override
    public void removePlayer(Connection.Unsafe connection, String player) {
        Preconditions.checkArgument(player.length() <= 16);
        playerListPacket.removePlayer(connection, player);
    }

    @Override
    public boolean isScoreboardSupported() {
        return teamPacket != null;
    }

    @Override
    public boolean isTabModificationSupported() {
        return playerListPacket != null;
    }
}
