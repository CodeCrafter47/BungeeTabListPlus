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

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import com.google.common.base.Charsets;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.ImmutableSet;
import de.codecrafter47.taboverlay.Icon;
import de.codecrafter47.taboverlay.ProfileProperty;
import de.codecrafter47.taboverlay.handler.ContentOperationMode;
import de.codecrafter47.taboverlay.handler.RectangularTabOverlay;
import de.codecrafter47.taboverlay.handler.SimpleTabOverlay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.val;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.*;

public class AbstractTabOverlayHandlerTest {
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

    UUID clientUUID;
    ClientTabList clientTabList;
    MockTabOverlayHandler tabOverlayHandler;

    @Before
    public void setUp() throws Exception {
        clientTabList = new ClientTabList();
        tabOverlayHandler = new MockTabOverlayHandler(clientTabList);
    }

    @After
    public void tearDown() throws Exception {
        tabOverlayHandler = null;
        clientTabList = null;
    }

    @Before
    public void setClientUUID() {
        clientUUID = uuids[47];
    }

    @Test
    public void testPassThrough() {
        assertEquals(0, clientTabList.getSize());
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        assertEquals(0, clientTabList.getSize());
        assertEquals(80, tabOverlay.getMaxSize());
        tabOverlay.setSize(1);
        assertEquals(1, clientTabList.getSize());
        tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
        assertFalse(tabOverlay.isValid());
        assertEquals(0, clientTabList.getSize());
        tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(2);
        assertEquals(2, clientTabList.getSize());
        tabOverlay.setSlot(1, Icon.DEFAULT_ALEX, "Slot", 1);
        assertEquals(2, clientTabList.getSize());
        assertArrayEquals("Skin check failed", new String[0][], clientTabList.getProperties(1));
        assertEquals("Text check failed", "Slot", clientTabList.getText(1));
        assertEquals("Ping check failed", 1, clientTabList.getPing(1));
    }

    @Test
    public void testSimpleTabList() {

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        for (int size = 1; size <= 80; size++) {
            tabOverlay.setSize(size);
            assertEquals("Size check failed for size " + size, size, clientTabList.getSize());

            tabOverlay.setSlot(size - 1, Icon.DEFAULT_STEVE, "Slot " + (size - 1), size - 1);

            for (int i = 0; i < size; i++) {
                assertArrayEquals("Skin check failed for size " + size + " slot " + i, codecrafter47.bungeetablistplus.api.bungee.Icon.DEFAULT.getProperties(), clientTabList.getProperties(i));
                assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
            }
        }
    }

    @Test
    public void testRectangularTabList() {

        RectangularTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.RECTANGULAR);

        for (val dimension : tabOverlay.getSupportedSizes()) {
            tabOverlay.setSize(dimension);
            assertEquals("Size check failed for size " + dimension, dimension.getSize(), clientTabList.getSize());
        }
    }

    @Test
    public void testPlayersOnServer() {
        assertEquals(0, clientTabList.getSize());
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        assertEquals(0, clientTabList.getSize());

        for (int size : new int[]{0, 1, 2, 3, 45, 46, 47, 48, 49, 50, 51, 78, 79, 80}) {

            for (int p = 0; p < 160; p++) {

                tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
                assertEquals("passthrough test failed size " + size + " players " + p, p, clientTabList.entries.size());

                tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

                tabOverlay.setSize(size);
                if (p <= size && size != 80) {
                    assertEquals("Size check failed for size " + size, size, clientTabList.getSize());
                }

                for (int i = 0; i < size; i++) {
                    tabOverlay.setSlot(i, Icon.DEFAULT_STEVE, "Slot " + i, i);
                }

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
                tabOverlayHandler.onPlayerListPacket(packet);

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i + " players " + p, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }
            }

            for (int p = 0; p < 160; p++) {

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }

                PlayerListItem packet = new PlayerListItem();
                packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
                PlayerListItem.Item item = new PlayerListItem.Item();
                item.setUuid(uuids[p]);
                packet.setItems(new PlayerListItem.Item[]{item});
                tabOverlayHandler.onPlayerListPacket(packet);

                for (int i = 0; i < size; i++) {
                    assertEquals("Text check failed for size " + size + " slot " + i, "Slot " + i, clientTabList.getText(i));
                    assertEquals("Ping check failed for size " + size + " slot " + i, i, clientTabList.getPing(i));
                }
            }
        }
    }

    @Test
    public void testSpectatorMode() {
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        tabOverlay.setSize(20);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(3);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabOverlay.setSlot(1, clientUUID, Icon.DEFAULT_ALEX, "Test 1", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        item.setGamemode(0);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        tabOverlay.setSlot(2, clientUUID, Icon.DEFAULT_STEVE, "Test 2", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        item.setGamemode(3);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabOverlay.setSlot(3, clientUUID, Icon.DEFAULT_STEVE, "Test 3", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabOverlay.setSlot(1, clientUUID, Icon.DEFAULT_STEVE, "Test 1", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        item.setGamemode(0);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        tabOverlay.setSlot(2, clientUUID, Icon.DEFAULT_STEVE, "Test 2", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());
    }

    @Test
    public void testSetIconA() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        tabOverlay.setSize(20);

        tabOverlay.setSlot(3, clientUUID, new Icon(new ProfileProperty("texture", "", "")), "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());
    }

    @Test
    public void testSetIconB() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        tabOverlay.setSize(80);

        tabOverlay.setSlot(3, clientUUID, new Icon(new ProfileProperty("texture", "abc", "")), "Hi", 1);
        assertEquals("abc", clientTabList.getVisibleEntries().get(3).getProperties()[0][1]);
    }

    @Test
    public void testMoveUuidA() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        Icon icon1 = new Icon(new ProfileProperty("texture", "abc", ""));
        Icon icon2 = new Icon(new ProfileProperty("texture", "def", ""));

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        tabOverlay.setSize(20);

        tabOverlay.setSlot(3, clientUUID, icon1, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());

        tabOverlay.setSlot(3, null, icon2, "Hi", 1);
        tabOverlay.setSlot(5, clientUUID, icon1, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(5).getUuid());
    }

    @Test
    public void testMoveUuidB() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        Icon icon1 = new Icon(new ProfileProperty("texture", "abc", ""));
        Icon icon2 = new Icon(new ProfileProperty("texture", "def", ""));

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(20);

        tabOverlay.setSlot(3, clientUUID, icon1, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());

        tabOverlay.setSlot(5, clientUUID, icon1, "Hi", 1);
        tabOverlay.setSlot(3, null, icon2, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(5).getUuid());
    }

    @Test
    public void testSwapUuidA() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabOverlayHandler.onPlayerListPacket(packet);

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(20);

        tabOverlay.setSlot(3, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(5, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(5).getUuid());

        tabOverlay.setSlot(5, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(3, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(5).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(3).getUuid());
    }

    @Test
    public void testSwapUuidB() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(1, clientTabList.getSize());

        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        assertEquals(2, clientTabList.getSize());

        tabOverlay.setSize(2);

        tabOverlay.setSlot(0, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(1, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(1).getUuid());

        tabOverlay.setSlot(1, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(0, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(0).getUuid());
    }

    @Test
    public void testSwapUuidC() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[49]);
        item.setUuid(uuids[49]);
        tabOverlayHandler.onPlayerListPacket(packet);

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(20);

        tabOverlay.setSlot(3, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(5, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(6, uuids[49], Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(5).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(6).getUuid());

        tabOverlay.setSlot(3, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(5, uuids[49], Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(6, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(6).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(5).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(3).getUuid());
    }

    @Test
    public void testSwapUuidD() {
        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(uuids[47]);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[48]);
        item.setUuid(uuids[48]);
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUsername(usernames[49]);
        item.setUuid(uuids[49]);
        tabOverlayHandler.onPlayerListPacket(packet);

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(3);

        tabOverlay.setSlot(0, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(1, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(2, uuids[49], Icon.DEFAULT_STEVE, "Hi", 1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(1).getUuid());
        assertEquals(uuids[49], clientTabList.getVisibleEntries().get(2).getUuid());

        tabOverlay.setSlot(0, uuids[48], Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(1, uuids[49], Icon.DEFAULT_STEVE, "Hi", 1);
        tabOverlay.setSlot(2, clientUUID, Icon.DEFAULT_STEVE, "Hi", 1);

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
                tabOverlayHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabOverlayHandler.onPlayerListPacket(packet);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(60);

        assertEquals(60, clientTabList.getSize());

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(60);

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        for (int i = 0; i < 25; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }
        for (int i = 25; i < 50; i++) {
            assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        }

        tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);

        tabOverlay.setSize(60);

        for (int i = 0; i < 25; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        for (int i = 25; i < 50; i++) {
            net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + i);
            team.setPlayers(new String[]{usernames[i]});
            team.setMode((byte) 0);
            team.setCollisionRule("always");
            team.setNameTagVisibility("always");
            tabOverlayHandler.onTeamPacket(team);
        }

        for (int i = 0; i < 50; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        for (int i = 0; i < 50; i++) {
            assertEquals("Team " + i, clientTabList.playerToTeamMap.get(usernames[i]));
        }

        tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(60);

        for (int i = 0; i < 50; i++) {
            assertEquals(0, clientTabList.teams.get("Team " + i).getPlayers().size());
        }

        tabOverlay.setSize(80);

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
                tabOverlayHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabOverlayHandler.onPlayerListPacket(packet);

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(60);

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
        tabOverlayHandler.onTeamPacket(team);

        assertEquals("Test", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[0])).getPrefix());

        team = new net.md_5.bungee.protocol.packet.Team("Team " + 0);
        team.setMode((byte) 1);
        tabOverlayHandler.onTeamPacket(team);

        assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[0])).getPrefix());
    }

    @Test
    public void testTeamPropertyPassthrough2() {

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(80);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[0]);
        item.setUuid(uuids[0]);
        item.setPing(15);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        PlayerListItem.Item[] items = new PlayerListItem.Item[]{item};
        packet.setItems(items);
        tabOverlayHandler.onPlayerListPacket(packet);

        net.md_5.bungee.protocol.packet.Team team = new net.md_5.bungee.protocol.packet.Team("Team " + 0);
        team.setPlayers(new String[]{usernames[0]});
        team.setMode((byte) 0);
        team.setPrefix("prefix " + 0);
        team.setCollisionRule("always");
        team.setNameTagVisibility("never");
        tabOverlayHandler.onTeamPacket(team);

        assertEquals("never", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[0])).getNameTagVisibility());
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
                tabOverlayHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabOverlayHandler.onPlayerListPacket(packet);

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(60);

        for (int i = 0; i < 25; i++) {
            assertEquals("prefix " + i, clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }
        for (int i = 25; i < 50; i++) {
            assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }

        tabOverlayHandler.onServerSwitch(false);

        tabOverlayHandler.onPlayerListPacket(packet);

        for (int i = 0; i < 50; i++) {
            assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }
    }

    @Test
    public void testSelfGamemode3Size0() {
        assertEquals(0, clientTabList.getSize());
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(0);
        assertEquals(0, clientTabList.getSize());

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(3);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(1, clientTabList.getSize());
        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(0).getUuid());
    }

    @Test
    public void testSelfGamemode3Size1() {
        assertEquals(0, clientTabList.getSize());
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(1);
        tabOverlay.setSlot(0, clientUUID, Icon.DEFAULT_STEVE, "name", 47);
        assertEquals(1, clientTabList.getSize());

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        assertEquals(0, clientTabList.getVisibleEntries().get(0).getGamemode());

        item.setGamemode(3);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(3, clientTabList.getVisibleEntries().get(0).getGamemode());
    }

    @Test
    public void testCitizensCompatibility() {
        assertEquals(0, clientTabList.getSize());
        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(10);
        tabOverlay.setSlot(3, clientUUID, Icon.DEFAULT_STEVE, "name", 47);
        assertEquals(10, clientTabList.getSize());

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());

        item.setUuid(UUID.fromString("fc28363a-06b3-24fb-b8d0-dd8e827ea413"));
        tabOverlayHandler.onPlayerListPacket(packet);

        packet.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(3).getUuid());

        assertEquals(10, clientTabList.getSize());
    }

    @Test
    public void testPassThrough2() {
        assertEquals(0, clientTabList.getSize());

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(10);

        assertEquals(10, clientTabList.getSize());

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(2);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);

        assertEquals(1, clientTabList.getSize());

        assertNull(clientTabList.entries.get(clientUUID).getDisplayName());
        assertEquals(2, clientTabList.entries.get(clientUUID).getGamemode());
        assertEquals(47, clientTabList.entries.get(clientUUID).getPing());
    }

    @Test
    public void testSpectatorMode2() {
        assertEquals(0, clientTabList.getSize());

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(2);

        assertEquals(2, clientTabList.getSize());

        tabOverlay.setSlot(0, clientUUID, Icon.DEFAULT_STEVE, "A", 1);
        tabOverlay.setSlot(1, uuids[48], Icon.DEFAULT_STEVE, "B", 2);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUuid(uuids[48]);
        item.setUsername(usernames[48]);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());
        assertEquals("A", clientTabList.getText(0));
        assertEquals("B", clientTabList.getText(1));
        assertEquals(1, clientTabList.getPing(0));
        assertEquals(2, clientTabList.getPing(1));
        assertEquals(uuids[47], clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(1).getUuid());

        item.setGamemode(3);
        item.setUuid(clientUUID);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());
        assertEquals("A", clientTabList.getText(0));
        assertEquals("B", clientTabList.getText(1));
        assertEquals(1, clientTabList.getPing(0));
        assertEquals(2, clientTabList.getPing(1));
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[47], clientTabList.getVisibleEntries().get(1).getUuid());
    }

    @Test
    public void testSpectatorModeServerSwitch() {
        assertEquals(0, clientTabList.getSize());

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(2);

        assertEquals(2, clientTabList.getSize());

        tabOverlay.setSlot(0, clientUUID, Icon.DEFAULT_STEVE, "A", 1);
        tabOverlay.setSlot(1, uuids[48], Icon.DEFAULT_STEVE, "B", 2);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);
        item.setUuid(uuids[48]);
        item.setUsername(usernames[48]);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());
        assertEquals("A", clientTabList.getText(0));
        assertEquals("B", clientTabList.getText(1));
        assertEquals(1, clientTabList.getPing(0));
        assertEquals(2, clientTabList.getPing(1));
        assertEquals(uuids[47], clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(1).getUuid());

        item.setGamemode(3);
        item.setUuid(clientUUID);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());
        assertEquals("A", clientTabList.getText(0));
        assertEquals("B", clientTabList.getText(1));
        assertEquals(1, clientTabList.getPing(0));
        assertEquals(2, clientTabList.getPing(1));
        assertEquals(uuids[48], clientTabList.getVisibleEntries().get(0).getUuid());
        assertEquals(uuids[47], clientTabList.getVisibleEntries().get(1).getUuid());

        tabOverlayHandler.onServerSwitch(false);

        assertEquals(2, clientTabList.getSize());
        assertEquals("A", clientTabList.getText(0));
        assertEquals("B", clientTabList.getText(1));
        assertEquals(1, clientTabList.getPing(0));
        assertEquals(2, clientTabList.getPing(1));

        item.setGamemode(3);
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertEquals(2, clientTabList.getSize());
        assertEquals("A", clientTabList.getText(0));
        assertEquals("B", clientTabList.getText(1));
        assertEquals(1, clientTabList.getPing(0));
        assertEquals(2, clientTabList.getPing(1));
        assertEquals(uuids[47], clientTabList.getVisibleEntries().get(1).getUuid());

    }

    @Test
    public void testNameChange() {
        assertEquals(0, clientTabList.getSize());

        SimpleTabOverlay tabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
        tabOverlay.setSize(1);

        assertEquals(1, clientTabList.getSize());

        tabOverlay.setSlot(0, clientUUID, Icon.DEFAULT_STEVE, "A", 1);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[47]);
        item.setUuid(clientUUID);
        item.setPing(47);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabOverlayHandler.onPlayerListPacket(packet);

        assertTrue(clientTabList.playerToTeamMap.containsKey(usernames[47]));

        item.setUsername(usernames[48]);
        tabOverlayHandler.onPlayerListPacket(packet);

        assertTrue(clientTabList.playerToTeamMap.containsKey(usernames[48]));
    }

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    static class TabListEntry {
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

    static class ClientTabList {
        final Map<UUID, TabListEntry> entries = new HashMap<>();
        final Map<String, Team> teams = new HashMap<>();
        final Map<String, String> playerToTeamMap = new HashMap<>();
        private List<TabListEntry> visibleEntries = Collections.emptyList();

        private void updateVisibleEntries() {
            this.visibleEntries = null;
        }

        List<TabListEntry> getVisibleEntries() {
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

        int getSize() {
            return Math.min(entries.size(), 80);
        }

        String[][] getProperties(int index) {
            return getVisibleEntries().get(index).getProperties();
        }

        String getText(int index) {
            String displayName = getVisibleEntries().get(index).getDisplayName();
            return displayName == null ? null : BaseComponent.toPlainText(ComponentSerializer.parse(displayName));
        }

        int getPing(int index) {
            return getVisibleEntries().get(index).getPing();
        }
    }

    class MockTabOverlayHandler extends AbstractTabOverlayHandler {
        private final ClientTabList clientTabList;

        public MockTabOverlayHandler(ClientTabList clientTabList) {
            super(Logger.getGlobal(), Runnable::run, clientUUID, false, false);
            this.clientTabList = clientTabList;
            this.active = true;
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
                    Team t;
                    if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 0) {
                        t = new Team(((net.md_5.bungee.protocol.packet.Team) packet).getName());
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
                                    clientTabList.playerToTeamMap.remove(s, t.getName());
                                    //assertTrue("Tried to remove player not in team.", clientTabList.playerToTeamMap.remove(s, t.getName()));
                                }
                            }
                        }
                    }
                }
            }
            clientTabList.updateVisibleEntries();
        }

        @Override
        public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
            PacketListenerResult packetListenerResult = super.onPlayerListPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            return packetListenerResult;
        }

        @Override
        public PacketListenerResult onTeamPacket(net.md_5.bungee.protocol.packet.Team packet) {
            PacketListenerResult packetListenerResult = super.onTeamPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            return packetListenerResult;
        }

        @Override
        public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
            PacketListenerResult packetListenerResult = super.onPlayerListHeaderFooterPacket(packet);
            if (packetListenerResult != PacketListenerResult.CANCEL) {
                sendPacket(packet);
            }
            return packetListenerResult;
        }

        @Override
        protected boolean isExperimentalTabCompleteSmileys() {
            return false;
        }

        @Override
        protected boolean isExperimentalTabCompleteFixForTabSize80() {
            return true;
        }
    }


}