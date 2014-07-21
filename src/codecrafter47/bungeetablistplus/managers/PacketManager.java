/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.packets.IPlayerListPacket;
import codecrafter47.bungeetablistplus.packets.ITeamPacket;
import codecrafter47.bungeetablistplus.packets.NewPlayerListPacket;
import codecrafter47.bungeetablistplus.packets.NewTeamPacket;
import codecrafter47.bungeetablistplus.packets.OldPlayerListPacket;
import codecrafter47.bungeetablistplus.packets.OldTeamPacket;
import codecrafter47.bungeetablistplus.packets.PlayerListPacket16;
import codecrafter47.bungeetablistplus.packets.TeamPacket16;
import net.md_5.bungee.api.connection.Connection;

/**
 *
 * @author florian
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
