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
    public void createTeam(Connection.Unsafe connection, String name, String player) {
        Team team = new Team(name);
        team.setMode((byte) 0);
        team.setColor((byte) 0);
        team.setDisplayName("");
        team.setNameTagVisibility("always");
        team.setPrefix("");
        team.setSuffix("");
        team.setPlayers(new String[]{player});
        connection.sendPacket(team);
    }

    @Override
    public void addPlayerToTeam(Connection.Unsafe connection, String team, String player){
        Team packet = new Team(team);
        packet.setMode((byte) 3);
        packet.setPlayers(new String[]{player});
        connection.sendPacket(packet);
    }

    @Override
    public void removePlayerFromTeam(Connection.Unsafe connection, String team, String player){
        Team packet = new Team(team);
        packet.setMode((byte) 4);
        packet.setPlayers(new String[]{player});
        connection.sendPacket(packet);
    }

    @Override
    public void removeTeam(Connection.Unsafe connection, String name) {
        Team packet = new Team(name);
        packet.setMode((byte) 1);
        connection.sendPacket(packet);
    }

    @Override
    public void createOrUpdatePlayer(Connection.Unsafe connection, UUID player, String username, int gamemode, int ping, String[][] properties) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player);
        item.setUsername(username);
        item.setGamemode(gamemode);
        item.setPing(ping);
        item.setProperties(properties);
        packet.setItems(new PlayerListItem.Item[]{item});
        connection.sendPacket(packet);
    }

    @Override
    public void updateDisplayName(Connection.Unsafe connection, UUID player, String displayName) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player);
        item.setDisplayName(displayName);
        packet.setItems(new PlayerListItem.Item[]{item});
        connection.sendPacket(packet);
    }

    @Override
    public void updatePing(Connection.Unsafe connection, UUID player, int ping) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.UPDATE_LATENCY);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player);
        item.setPing(ping);
        packet.setItems(new PlayerListItem.Item[]{item});
        connection.sendPacket(packet);
    }

    @Override
    public void removePlayer(Connection.Unsafe connection, UUID player) {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUuid(player);
        packet.setItems(new PlayerListItem.Item[]{item});
        connection.sendPacket(packet);
    }
}
