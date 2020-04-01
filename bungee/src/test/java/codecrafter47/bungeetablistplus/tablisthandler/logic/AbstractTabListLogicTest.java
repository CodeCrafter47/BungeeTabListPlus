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
import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;
import java.util.UUID;

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
        uuids[47] = clientUUID;
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

        Set<Integer> testSizes = Sets.newHashSet(1, 2, 19, 20, 21, 59, 60, 61, 79, 80);

        for (int size = 1; size <= 80; size++) {
            tabListHandler.setSize(size);
            assertEquals("Size check failed for size " + size, size, clientTabList.getSize());

            tabListHandler.setSlot(size - 1, Icon.DEFAULT, "Slot " + (size - 1), size - 1);

            if (testSizes.contains(size)) {
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
        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());

        tabListHandler.setSlot(1, new Icon(clientUUID, new String[0][]), "Test 1", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        item.setGamemode(0);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(0, clientTabList.entries.get(clientUUID).getGamemode());
        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        tabListHandler.setSlot(2, new Icon(clientUUID, new String[0][]), "Test 2", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        item.setGamemode(3);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());
        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabListHandler.setSlot(3, new Icon(clientUUID, new String[0][]), "Test 3", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        tabListHandler.setSlot(1, new Icon(clientUUID, new String[0][]), "Test 1", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());

        item.setGamemode(0);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(0, clientTabList.entries.get(clientUUID).getGamemode());
        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());

        tabListHandler.setSlot(2, new Icon(clientUUID, new String[0][]), "Test 2", -1);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(1).getUuid());
    }

    @Test
    public void testSpectatorMode2() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(80);

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

        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());

        item.setGamemode(0);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(0, clientTabList.entries.get(clientUUID).getGamemode());
    }

    @Test
    public void testSpectatorMode3() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(80);

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
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        assertEquals(0, clientTabList.entries.get(clientUUID).getGamemode());

        item.setGamemode(3);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());
    }

    @Test
    public void testSpectatorMode4() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(80);

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

        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());

        tabListHandler.setSize(20);

        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(19).getUuid());
        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());

        tabListHandler.setSize(80);

        assertEquals(3, clientTabList.entries.get(clientUUID).getGamemode());
    }


    @Test
    public void testSpectatorMode5() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(80);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[4]);
        item.setUuid(uuids[4]);
        item.setPing(4);
        item.setProperties(new String[0][]);
        item.setGamemode(3);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        assertEquals(3, clientTabList.entries.get(uuids[4]).getGamemode());

        item.setGamemode(0);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(0, clientTabList.entries.get(uuids[4]).getGamemode());
    }

    @Test
    public void testSpectatorMode6() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(80);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[4]);
        item.setUuid(uuids[4]);
        item.setPing(4);
        item.setProperties(new String[0][]);
        item.setGamemode(0);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        assertEquals(0, clientTabList.entries.get(uuids[4]).getGamemode());

        item.setGamemode(3);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(3, clientTabList.entries.get(uuids[4]).getGamemode());
    }

    @Test
    public void testSpectatorMode7() {
        assertEquals(0, clientTabList.getSize());
        tabListHandler.setPassThrough(false);
        assertEquals(0, clientTabList.getSize());

        tabListHandler.setSize(80);

        PlayerListItem packet = new PlayerListItem();
        packet.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setUsername(usernames[4]);
        item.setUuid(uuids[4]);
        item.setPing(4);
        item.setProperties(new String[0][]);
        item.setGamemode(3);
        packet.setItems(new PlayerListItem.Item[]{item});
        tabListHandler.onPlayerListPacket(packet);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);

        assertEquals(3, clientTabList.entries.get(uuids[4]).getGamemode());

        tabListHandler.setSize(20);

        assertEquals(uuids[4], clientTabList.getVisibleEntries().get(19).getUuid());
        assertEquals(0, clientTabList.entries.get(uuids[4]).getGamemode());

        tabListHandler.setSize(80);

        assertEquals(3, clientTabList.entries.get(uuids[4]).getGamemode());
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

        //assertEquals(20, clientTabList.getSize());

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

        tabListHandler.setSlot(0, Icon.DEFAULT, "abcd", 120);
        assertEquals("abcd", clientTabList.getText(0));
        assertEquals(120, clientTabList.getPing(0));
        // This no longer holds as BungeeTabListPlus now assigns players to a team if they don't already have one
        //for (int i = 25; i < 50; i++) {
        //    assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        //}

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
        // see above
        //for (int i = 25; i < 50; i++) {
        //    assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        //}

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
        // see above
        //for (int i = 25; i < 50; i++) {
        //    assertFalse(clientTabList.playerToTeamMap.containsKey(usernames[i]));
        //}

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
                team.setNameTagVisibility("never");
                tabListHandler.onTeamPacket(team);
            }
        }
        packet.setItems(items);
        tabListHandler.onPlayerListPacket(packet);
        tabListHandler.setSize(60);
        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 25; i++) {
            assertEquals("prefix " + i, clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
            assertEquals("never", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getNameTagVisibility());
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

        team = new net.md_5.bungee.protocol.packet.Team("Team " + 1);
        team.setMode((byte) 1);
        tabListHandler.onTeamPacket(team);

        assertEquals("always", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[1])).getNameTagVisibility());
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

        tabListHandler.onServerSwitch(false);

        tabListHandler.onPlayerListPacket(packet);
        tabListHandler.setSize(60);
        tabListHandler.setPassThrough(false);

        for (int i = 0; i < 50; i++) {
            assertEquals("", clientTabList.teams.get(clientTabList.playerToTeamMap.get(usernames[i])).getPrefix());
        }
    }

    @Test
    public void testSelfGamemode3Size0() {
        assertEquals(0, clientTabList.getSize());
        //tabListHandler.setResizePolicy(PlayerTablistHandler.ResizePolicy.DYNAMIC);
        tabListHandler.setPassThrough(false);
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
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(1, clientTabList.getSize());
        assertEquals(clientUUID, clientTabList.getVisibleEntries().get(0).getUuid());
    }

    @Test
    public void testSelfGamemode3Size1() {
        assertEquals(0, clientTabList.getSize());
        //tabListHandler.setResizePolicy(PlayerTablistHandler.ResizePolicy.DYNAMIC);
        tabListHandler.setPassThrough(false);
        tabListHandler.setSize(1);
        tabListHandler.setSlot(0, new Icon(clientUUID, new String[0][]), "name", 47);
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
        tabListHandler.onPlayerListPacket(packet);
        assertEquals(0, clientTabList.getVisibleEntries().get(0).getGamemode());

        item.setGamemode(3);
        packet.setAction(PlayerListItem.Action.UPDATE_GAMEMODE);
        tabListHandler.onPlayerListPacket(packet);

        assertEquals(3, clientTabList.getVisibleEntries().get(0).getGamemode());
    }
}