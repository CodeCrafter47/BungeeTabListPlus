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

import codecrafter47.bungeetablistplus.packet.v1_8.InjectedTabHeaderPacketAccess;
import codecrafter47.bungeetablistplus.packet.v1_8.TabHeaderPacketAccess18;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PacketAccessImpl implements PacketAccess {
    private TabHeaderPacketAccess tabHeaderPacket;

    public PacketAccessImpl(Logger logger) {
        if (isClassPresent("net.md_5.bungee.protocol.packet.PlayerListHeaderFooter")) {
            tabHeaderPacket = new TabHeaderPacketAccess18();
        } else {
            try {
                tabHeaderPacket = new InjectedTabHeaderPacketAccess();
            } catch (Exception e) {
                logger.log(Level.SEVERE, "Failed to inject TabHeaderPacket", e);
            }
        }
    }

    private boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }

    @Override
    public boolean isTabHeaderFooterSupported(){
        return tabHeaderPacket != null;
    }

    @Override
    public void setTabHeaderAndFooter(Connection.Unsafe connection, String header, String footer) {
        tabHeaderPacket.setTabHeaderFooter(connection, header, footer);
    }

    @Override
    public Batch createBatch() {
        return new BatchImpl();
    }

    private final class BatchImpl implements Batch {
        List<Team> createdTeams;
        List<Team> playersAddedToTeams;
        List<Team> playersRemovedFromTeams;
        List<Team> removedTeams;
        List<PlayerListItem.Item> createdPlayers;
        List<PlayerListItem.Item> updatedNames;
        List<PlayerListItem.Item> updatedPings;
        List<PlayerListItem.Item> removedPlayers;

        @Override
        public void createTeam(String name, String player) {
            if (createdTeams == null) {
                createdTeams = new ArrayList<>();
            }
            Team team = new Team(name);
            team.setMode((byte) 0);
            team.setColor((byte) 0);
            team.setDisplayName("");
            team.setNameTagVisibility("always");
            team.setPrefix("");
            team.setSuffix("");
            team.setPlayers(new String[]{player});
            createdTeams.add(team);
        }

        @Override
        public void addPlayerToTeam(String team, String player) {
            if (playersAddedToTeams == null) {
                playersAddedToTeams = new ArrayList<>();
            }
            Team packet = new Team(team);
            packet.setMode((byte) 3);
            packet.setPlayers(new String[]{player});
            playersAddedToTeams.add(packet);
        }

        @Override
        public void removePlayerFromTeam(String team, String player) {
            if (playersRemovedFromTeams == null) {
                playersRemovedFromTeams = new ArrayList<>();
            }
            Team packet = new Team(team);
            packet.setMode((byte) 4);
            packet.setPlayers(new String[]{player});
            playersRemovedFromTeams.add(packet);
        }

        @Override
        public void removeTeam(String name) {
            if (removedTeams == null) {
                removedTeams = new ArrayList<>();
            }
            Team packet = new Team(name);
            packet.setMode((byte) 1);
            removedTeams.add(packet);
        }

        @Override
        public void createOrUpdatePlayer(UUID player, String username, int gamemode, int ping, String[][] properties) {
            if (createdPlayers == null) {
                createdPlayers = new ArrayList<>();
            }
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(player);
            item.setUsername(username);
            item.setGamemode(gamemode);
            item.setPing(ping);
            item.setProperties(properties);
            createdPlayers.add(item);
        }

        @Override
        public void updateDisplayName(UUID player, String displayName) {
            if (updatedNames == null) {
                updatedNames = new ArrayList<>();
            }
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(player);
            item.setDisplayName(displayName);
            updatedNames.add(item);
        }

        @Override
        public void updatePing(UUID player, int ping) {
            if (updatedPings == null) {
                updatedPings = new ArrayList<>();
            }
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(player);
            item.setPing(ping);
            updatedPings.add(item);
        }

        @Override
        public void removePlayer(UUID player) {
            if (removedPlayers == null) {
                removedPlayers = new ArrayList<>();
            }
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUuid(player);
            removedPlayers.add(item);
        }

        @Override
        public void send(Connection.Unsafe connection) {
            if (removedPlayers != null) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
                packet.setItems(removedPlayers.toArray(new PlayerListItem.Item[removedPlayers.size()]));
                connection.sendPacket(packet);
            }
            if (createdPlayers != null) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.ADD_PLAYER);
                packet.setItems(createdPlayers.toArray(new PlayerListItem.Item[createdPlayers.size()]));
                connection.sendPacket(packet);
            }
            if (updatedNames != null) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
                packet.setItems(updatedNames.toArray(new PlayerListItem.Item[updatedNames.size()]));
                connection.sendPacket(packet);
            }
            if (updatedPings != null) {
                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
                packet.setItems(updatedPings.toArray(new PlayerListItem.Item[updatedPings.size()]));
                connection.sendPacket(packet);
            }
            if (removedTeams != null) {
                removedTeams.forEach(connection::sendPacket);
            }
            if (createdTeams != null) {
                createdTeams.forEach(connection::sendPacket);
            }
            if (playersRemovedFromTeams != null) {
                playersRemovedFromTeams.forEach(connection::sendPacket);
            }
            if (playersAddedToTeams != null) {
                playersAddedToTeams.forEach(connection::sendPacket);
            }
        }
    }
}
