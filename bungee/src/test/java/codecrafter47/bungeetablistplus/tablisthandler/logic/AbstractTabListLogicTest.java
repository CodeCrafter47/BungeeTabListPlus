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

package codecrafter47.bungeetablistplus.tablisthandler.logic;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class AbstractTabListLogicTest extends AbstractTabListLogicTestBase {
    private static final String[] usernames = new String[160];
    private static final UUID[] uuids = new UUID[160];

    static {
        for (int i = 0; i < 160; i++) {
            usernames[i] = String.format("Player %3d", i);
            uuids[i] = UUID.nameUUIDFromBytes(("OfflinePlayer:" + usernames[i]).getBytes(Charsets.UTF_8));
        }
        assertEquals(ImmutableSet.copyOf(uuids).size(), uuids.length);
        assertEquals(ImmutableSet.copyOf(usernames).size(), usernames.length);
    }

    @Before
    public void setClientUUID() {
        clientUUID = uuids[47];
    }

    @Test
    public void testPassthrough() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setSize(1);
        assertEquals(1, clientTabList.getSize());
        tabListHandler.setPassThrough(true);
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setSize(2);
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setSlot(1, Icon.DEFAULT, "Slot", 1);
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(2, clientTabList.getSize());
        assertArrayEquals("Skin check failed", Icon.DEFAULT.getProperties(), clientTabList.getProperties(1));
        assertEquals("Text check failed", "Slot", clientTabList.getText(1));
        assertEquals("Ping check failed", 1, clientTabList.getPing(1));
    }

    @Test
    public void testSimpleTabList() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        for (int size = 1; size <= 80; size++) {
            tabListHandler.setSize(size);
            assertEquals("Size check failed for size " + size, size, clientTabList.getSize());

            tabListHandler.setSlot(size - 1, Icon.DEFAULT, "Slot " + (size - 1), size - 1);

            for (int i = 0; i < size; i++) {
                assertArrayEquals("Skin check failed for size " + size + " slot " + i, Icon.DEFAULT.getProperties(), clientTabList.getProperties(i));
                assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
            }

            tabListHandler.setPassThrough(true);
            assertEquals("passthrough test failed size " + size, 0, clientTabList.getSize());
            tabListHandler.setPassThrough(false);

            for (int i = 0; i < size; i++) {
                assertArrayEquals("Skin check failed for size " + size + " slot " + i, Icon.DEFAULT.getProperties(), clientTabList.getProperties(i));
                assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
            }
        }
    }

    @Test
    public void testPlayersOnServer() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        for (int size = 1; size <= 80; size++) {
            tabListHandler.setSize(size);
            assertEquals("Size check failed for size " + size, size, clientTabList.getSize());

            tabListHandler.setSlot(size - 1, Icon.DEFAULT, "Slot " + (size - 1), size - 1);

            for (int i = 0; i < size; i++) {
                assertArrayEquals("Skin check failed for size " + size + " slot " + i, Icon.DEFAULT.getProperties(), clientTabList.getProperties(i));
                assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
            }

            for (int p = 0; p < 160; p++) {

                tabListHandler.setPassThrough(true);
                assertEquals("passthrough test failed size " + size + " players " + p, p, clientTabList.entries.size());
                tabListHandler.setPassThrough(false);

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }

                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.ADD_PLAYER);
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setUsername(usernames[p]);
                item.setUuid(uuids[p]);
                item.setPing(p + 13);
                item.setProperties(new String[0][]);
                item.setGamemode(p % 4);
                packet.setItems(new PlayerListItem.Item[]{item});
                tabListHandler.onPlayerListPacket(packet);

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i + " players " + p, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }
            }

            for (int p = 0; p < 160; p++) {

                tabListHandler.setPassThrough(true);
                assertEquals("passthrough test failed size " + size + " players " + p, 160 - p, clientTabList.entries.size());
                tabListHandler.setPassThrough(false);

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }

                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setUuid(uuids[p]);
                packet.setItems(new PlayerListItem.Item[]{item});
                tabListHandler.onPlayerListPacket(packet);

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }
            }
        }
    }

    @Test
    public void testSpectatorMode() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(20);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(3);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabListHandler.setSlot(1, new Icon(clientUUID, new String[0][]), "Test 1", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        item.setGamemode(0);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        tabListHandler.setSlot(2, new Icon(clientUUID, new String[0][]), "Test 2", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        item.setGamemode(3);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Test 3", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabListHandler.setSlot(1, new Icon(clientUUID, new String[0][]), "Test 1", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        item.setGamemode(0);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        tabListHandler.setSlot(2, new Icon(clientUUID, new String[0][]), "Test 2", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());
    }

    @Test
    public void testSetSkinA() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);

        tabListHandler.setPassThrough(false);

        tabListHandler.setSize(20);

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());
    }

    @Test
    public void testSetSkinB() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);

        tabListHandler.setPassThrough(false);

        tabListHandler.setSize(80);

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Hi", 1);
    }

    @Test
    public void testMoveSkin() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);

        tabListHandler.setPassThrough(false);

        tabListHandler.setSize(20);

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());

        tabListHandler.setSlot(3, new Icon(null, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(5, new Icon(clientUUID, new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(5).getUuid());
    }

    @Test
    public void testSwapSkinA() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabListHandler.onPlayerListPacket(packet);

        tabListHandler.setPassThrough(false);

        tabListHandler.setSize(20);

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(5, new Icon(uuids[48], new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(5).getUuid());

        tabListHandler.setSlot(5, new Icon(clientUUID, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(3, new Icon(uuids[48], new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(5).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(3).getUuid());
    }

    @Test
    public void testSwapSkinB() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(1, clientTabList.getSize());

        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());

        tabListHandler.setPassThrough(false);

        assertEquals(20, clientTabList.getSize());

        tabListHandler.setSize(2);

        tabListHandler.setSlot(0, new Icon(clientUUID, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(1, new Icon(uuids[48], new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(1).getUuid());

        tabListHandler.setSlot(1, new Icon(clientUUID, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(0, new Icon(uuids[48], new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(0).getUuid());
    }

    @Test
    public void testSwapSkinC() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabListHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[49]);
        item.setUuid(uuids[49]);
        tabListHandler.onPlayerListPacket(packet);

        tabListHandler.setPassThrough(false);

        tabListHandler.setSize(20);

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(5, new Icon(uuids[48], new String[0][]), "Hi", 1);
        tabListHandler.setSlot(6, new Icon(uuids[49], new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(5).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(6).getUuid());

        tabListHandler.setSlot(3, new Icon(uuids[48], new String[0][]), "Hi", 1);
        tabListHandler.setSlot(5, new Icon(uuids[49], new String[0][]), "Hi", 1);
        tabListHandler.setSlot(6, new Icon(clientUUID, new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(6).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(5).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(3).getUuid());
    }

    @Test
    public void testSwapSkinD() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabListHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[49]);
        item.setUuid(uuids[49]);
        tabListHandler.onPlayerListPacket(packet);

        tabListHandler.setPassThrough(false);

        tabListHandler.setSize(3);

        tabListHandler.setSlot(0, new Icon(clientUUID, new String[0][]), "Hi", 1);
        tabListHandler.setSlot(1, new Icon(uuids[48], new String[0][]), "Hi", 1);
        tabListHandler.setSlot(2, new Icon(uuids[49], new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(1).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(2).getUuid());

        tabListHandler.setSlot(0, new Icon(uuids[48], new String[0][]), "Hi", 1);
        tabListHandler.setSlot(1, new Icon(uuids[49], new String[0][]), "Hi", 1);
        tabListHandler.setSlot(2, new Icon(clientUUID, new String[0][]), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(2).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(1).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(0).getUuid());
    }

    @Test
    public void testTeamRestore() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item[] items = new PlayerListItem.Item[50];
        for (int i = 0; i < 50; i++) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUsername(usernames[i]);
            item.setUuid(uuids[i]);
            item.setPing(15);
            item.setProperties(new String[0][]);
            item.setGamemode(0);
            items[i] = item;

            if (i < 25) {
                net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + i);
                team.setPlayers(new String[]{usernames[i]});
                team.setMode((byte) 0);
                team.setCollisionRule("always");
                team.setNameTagVisibility("always");
                tabListHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabListHandler.onPlayerListPacket(packet);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setSize(60);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setPassThrough(false);

        assertEquals(60, clientTabList.getSize());

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabListHandler.setSize(80);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setSize(60);

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabListHandler.setPassThrough(true);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabListHandler.setSize(80);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setPassThrough(true);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabListHandler.setSize(60);

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        for (int i = 25; i < 50; i++) {
            net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + i);
            team.setPlayers(new String[]{usernames[i]});
            team.setMode((byte) 0);
            team.setCollisionRule("always");
            team.setNameTagVisibility("always");
            tabListHandler.onTeamPacket(team);
        }

        for (int i = 0; i < 50; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabListHandler.setPassThrough(true);

        for (int i = 0; i < 50; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }

        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 50; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabListHandler.setSize(80);

        for (int i = 0; i < 50; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
    }

    @Test
    public void testTeamPropertyPassthrough() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item[] items = new PlayerListItem.Item[50];
        for (int i = 0; i < 50; i++) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUsername(usernames[i]);
            item.setUuid(uuids[i]);
            item.setPing(15);
            item.setProperties(new String[0][]);
            item.setGamemode(0);
            items[i] = item;

            if (i < 25) {
                net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + i);
                team.setPlayers(new String[]{usernames[i]});
                team.setMode((byte) 0);
                team.setPrefix("prefix " + i);
                team.setCollisionRule("always");
                team.setNameTagVisibility("always");
                tabListHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabListHandler.onPlayerListPacket(packet);
        tabListHandler.setSize(60);
        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 25; i++) {
            assertEquals("prefix " + i, clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }
        for (int i = 25; i < 50; i++) {
            assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }

        net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + 0);
        team.setMode((byte) 2);
        team.setPrefix("Test");
        team.setCollisionRule("always");
        team.setNameTagVisibility("always");
        tabListHandler.onTeamPacket(team);

        assertEquals("Test", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[0])).getPrefix());

        team = new net.md_5.bungee.protocol.packet.Team("Team " + 0);
        team.setMode((byte) 1);
        tabListHandler.onTeamPacket(team);

        assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[0])).getPrefix());
    }

    @Test
    public void testTeamPropertyPassthroughServerSwitch() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item[] items = new PlayerListItem.Item[50];
        for (int i = 0; i < 50; i++) {
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setUsername(usernames[i]);
            item.setUuid(uuids[i]);
            item.setPing(15);
            item.setProperties(new String[0][]);
            item.setGamemode(0);
            items[i] = item;

            if (i < 25) {
                net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + i);
                team.setPlayers(new String[]{usernames[i]});
                team.setMode((byte) 0);
                team.setPrefix("prefix " + i);
                team.setCollisionRule("always");
                team.setNameTagVisibility("always");
                tabListHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabListHandler.onPlayerListPacket(packet);
        tabListHandler.setSize(60);
        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 25; i++) {
            assertEquals("prefix " + i, clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }
        for (int i = 25; i < 50; i++) {
            assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }

        tabListHandler.onServerSwitch();

        tabListHandler.onPlayerListPacket(packet);
        tabListHandler.setSize(60);
        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 50; i++) {
            assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TabListEntry {
        private UUID uuid;
        private String[][] properties;
        private String username;
        private String displayName;
        private int ping;
        private int gamemode;

        private TabListEntry(PlayerListItem.Item item) {
            this(item.getUuid(), item.getProperties(), item.getUsername(), item.getDisplayName(), item.getPing(), item.getGamemode());
        }
    }

    private class ClientTabList {
        private final Map<UUID, TabListEntry> entries = new HashMap<>();
        private final Map<String, Team> teams = new HashMap<>();
        private final Map<String, String> playerToTeamMap = new HashMap<>();
        private List<TabListEntry> visibleEntries = Collections.emptyList();

        private void updateVisibleEntries() {
            this.visibleEntries = null;
        }

        private List<TabListEntry> getVisibleEntries() {
            if (visibleEntries == null) {
                List<TabListEntry> entries = new ArrayList<>(this.entries.values());
                entries.sort(new Comparator<TabListEntry>() {
                    @Override
                    public int compare(TabListEntry o1, TabListEntry o2) {
                        return ComparisonChain.start().compareTrueFirst(o1.getGamemode() != 3, o2.getGamemode() != 3)
                                .compare(MoreObjects.firstNonNull(playerToTeamMap.get(o1.getUsername()), ""), MoreObjects.firstNonNull(playerToTeamMap.get(o2.getUsername()), ""))
                                .compare(o1.getUsername(), o2.getUsername()).result();
                    }
                });
                if (entries.size() > 80) {
                    entries = entries.subList(0, 80);
                }
                visibleEntries = Collections.unmodifiableList(entries);
            }
            return visibleEntries;
        }

        private int getSize() {
            return Math.min(entries.size(), 80);
        }

        private String[][] getProperties(int index) {
            return getVisibleEntries().get(index).getProperties();
        }

        private String getText(int index) {
            return getVisibleEntries().get(index).getDisplayName();
        }

        private int getPing(int index) {
            return getVisibleEntries().get(index).getPing();
        }
    }

    private class MockTabListLogic extends AbstractTabListLogic {
        private final ClientTabList clientTabList;

        public MockTabListLogic(ClientTabList clientTabList) {
            super(null);
            this.clientTabList = clientTabList;
        }

        private void validateConstraints() {
            if (passtrough) {
                assertEquals("server client tab size mismatch", serverTabList.size(), clientTabList.entries.size());
                for (AbstractTabListLogic.TabListItem item : serverTabList.values()) {
                    assertTrue("Missing player", clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    assertEquals("display name passthrough", item.getDisplayName(), entry.getDisplayName());
                    assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    assertEquals("ping passthrough", item.getPing(), entry.getPing());
                    assertEquals("gamemode passthrough", item.getGamemode(), entry.getGamemode());
                    assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    assertArrayEquals("display name passthrough", item.getProperties(), entry.getProperties());
                }
            } else if (size == 80) {
                assertEquals("server client tab size mismatch", 80, clientTabList.getSize());
                assertEquals("server client tab size mismatch", serverTabList.size() + 80, clientTabList.entries.size());
                // fake players
                for (int i = 0; i < size; i++) {
                    assertEquals("uuid", fakePlayerUUIDs[i], clientUuid[i]);
                    assertEquals("username", fakePlayerUsernames[i], clientUsername[i]);
                    assertEquals("uuid", clientUuid[i], clientTabList.getVisibleEntries().get(i).getUuid());
                    assertEquals("username", clientUsername[i], clientTabList.getVisibleEntries().get(i).getUsername());
                    assertEquals("text", clientText[i], clientTabList.getText(i));
                    assertEquals("ping", clientPing[i], clientTabList.getPing(i));
                    assertArrayEquals("skin", clientSkin[i].getProperties(), clientTabList.getProperties(i));
                }
                // real players
                for (AbstractTabListLogic.TabListItem item : serverTabList.values()) {
                    assertTrue("Missing player", clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    //assertEquals("display name passthrough", item.getDisplayName(), entry.getDisplayName());
                    //assertEquals("ping passthrough", item.getPing(), entry.getPing());
                    assertEquals("gamemode passthrough", item.getGamemode(), entry.getGamemode());
                    assertArrayEquals("skin passthrough", item.getProperties(), entry.getProperties());
                }
            } else {
                assertEquals("server client tab size mismatch", size, clientTabList.getSize());
                for (int i = 0; i < size; i++) {
                    assertEquals("uuid", clientUuid[i], clientTabList.getVisibleEntries().get(i).getUuid());
                    assertEquals("username", clientUsername[i], clientTabList.getVisibleEntries().get(i).getUsername());
                    assertEquals("text", clientText[i], clientTabList.getText(i));
                    assertEquals("ping", clientPing[i], clientTabList.getPing(i));
                    if (clientUuid[i] == fakePlayerUUIDs[i]) {
                        assertArrayEquals("skin", clientSkin[i].getProperties(), clientTabList.getProperties(i));
                    } else {
                        assertArrayEquals("skin", serverTabList.get(clientUuid[i]).getProperties(), clientTabList.getProperties(i));
                    }
                }
                // real players
                for (AbstractTabListLogic.TabListItem item : serverTabList.values()) {
                    assertTrue("Missing player " + item, clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    //assertEquals("display name passthrough", item.getDisplayName(), entry.getDisplayName());
                    //assertEquals("ping passthrough", item.getPing(), entry.getPing());
                    //assertEquals("gamemode passthrough", item.getGamemode(), entry.getGamemode());
                    assertArrayEquals("skin passthrough", item.getProperties(), entry.getProperties());
                    IntCollection collection = skinUuidToSlotMap.get(item.getUuid());
                    if (!collection.isEmpty()) {
                        int match = 0;
                        for (Integer i : collection) {
                            if (clientTabList.getVisibleEntries().get(i) == entry) {
                                match++;
                            }
                        }
                        assertTrue(match == 1);
                    }
                }
            }
        }

        @Override
        protected UUID getUniqueId() {
            return clientUUID;
        }

        @Override
        protected void sendPacket(DefinedPacket packet) {
            if (packet instanceof PlayerListItem) {
                for (PlayerListItem.Item item : ((PlayerListItem) packet).getItems()) {
                    switch (((PlayerListItem) packet).getAction()) {
                        case ADD_PLAYER:
                            clientTabList.entries.put(item.getUuid(), new TabListEntry(item));
                            break;
                        case UPDATE_GAMEMODE:
                            TabListEntry tabListEntry = clientTabList.entries.get(item.getUuid());
                            if (tabListEntry != null) {
                                tabListEntry.setGamemode(item.getGamemode());
                            }
                            break;
                        case UPDATE_LATENCY:
                            tabListEntry = clientTabList.entries.get(item.getUuid());
                            if (tabListEntry != null) {
                                tabListEntry.setPing(item.getPing());
                            }
                            break;
                        case UPDATE_DISPLAY_NAME:
                            tabListEntry = clientTabList.entries.get(item.getUuid());
                            if (tabListEntry != null) {
                                tabListEntry.setDisplayName(item.getDisplayName());
                            }
                            break;
                        case REMOVE_PLAYER:
                            clientTabList.entries.remove(item.getUuid());
                            break;
                    }
                }
            } else if (packet instanceof net.md_5.bungee.protocol.packet.Team) {
                if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 1) {
                    Team team = clientTabList.teams.remove(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                    for (String player : team.getPlayers()) {
                        clientTabList.playerToTeamMap.remove(player, team.getName());
                    }

                } else {

                    // Create or get old team
                    net.md_5.bungee.api.score.Team t;
                    if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 0) {
                        t = new net.md_5.bungee.api.score.Team(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                        clientTabList.teams.put(((net.md_5.bungee.protocol.packet.Team) packet).getName(), t);
                    } else {
                        t = clientTabList.teams.get(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                    }

                    if (t != null) {
                        if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 0 || ((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 2) {
                            t.setDisplayName(((net.md_5.bungee.protocol.packet.Team) packet).getDisplayName());
                            t.setPrefix(((net.md_5.bungee.protocol.packet.Team) packet).getPrefix());
                            t.setSuffix(((net.md_5.bungee.protocol.packet.Team) packet).getSuffix());
                            t.setFriendlyFire(((net.md_5.bungee.protocol.packet.Team) packet).getFriendlyFire());
                            t.setNameTagVisibility(((net.md_5.bungee.protocol.packet.Team) packet).getNameTagVisibility());
                            t.setCollisionRule(((net.md_5.bungee.protocol.packet.Team) packet).getCollisionRule());
                            t.setColor(((net.md_5.bungee.protocol.packet.Team) packet).getColor());
                        }
                        if (((net.md_5.bungee.protocol.packet.Team) packet).getPlayers() != null) {
                            for (String s : ((net.md_5.bungee.protocol.packet.Team) packet).getPlayers()) {
                                if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 0 || ((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 3) {
                                    if (clientTabList.playerToTeamMap.containsKey(s)) {
                                        clientTabList.teams.get(clientTabList.playerToTeamMap.get(s)).removePlayer(s);
                                    }
                                    t.addPlayer(s);
                                    clientTabList.playerToTeamMap.put(s, t.getName());
                                } else {
                                    t.removePlayer(s);
                                    assertTrue("Tried to remove player not in team.", clientTabList.playerToTeamMap.remove(s, t.getName()));
                                }
                            }
                        }
                    }
                }
            }
            clientTabList.updateVisibleEntries();
        }

        @Override
        public void onConnected() {
            validateConstraints();
            super.onConnected();
            validateConstraints();
        }

        @Override
        public void onDisconnected() {
            validateConstraints();
            super.onDisconnected();
            validateConstraints();
        }

        @Override
        public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
            validateConstraints();
            PacketListenerResult packetListenerResult = super.onPlayerListPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            validateConstraints();
            return packetListenerResult;
        }

        @Override
        public PacketListenerResult onTeamPacket(net.md_5.bungee.protocol.packet.Team packet) {
            validateConstraints();
            PacketListenerResult packetListenerResult = super.onTeamPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            validateConstraints();
            return packetListenerResult;
        }

        @Override
        public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
            validateConstraints();
            PacketListenerResult packetListenerResult = super.onPlayerListHeaderFooterPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            validateConstraints();
            return packetListenerResult;
        }

        @Override
        public void onServerSwitch() {
            validateConstraints();
            super.onServerSwitch();
            validateConstraints();
        }

        @Override
        public void setPassThrough(boolean passTrough) {
            validateConstraints();
            super.setPassThrough(passTrough);
            validateConstraints();
        }

        @Override
        public void setSize(int size) {
            validateConstraints();
            super.setSize(size);
            validateConstraints();
        }

        @Override
        public void setSlot(int index, Icon icon, String text, int ping) {
            validateConstraints();
            super.setSlot(index, icon, text, ping);
            validateConstraints();
        }

        @Override
        public void updateText(int index, String text) {
            validateConstraints();
            super.updateText(index, text);
            validateConstraints();
        }

        @Override
        public void updatePing(int index, int ping) {
            validateConstraints();
            super.updatePing(index, ping);
            validateConstraints();
        }

        @Override
        public void setHeaderFooter(String header, String footer) {
            validateConstraints();
            super.setHeaderFooter(header, footer);
            validateConstraints();
        }
    }
}