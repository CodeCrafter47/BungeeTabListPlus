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
import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractTabListLogicTestBase {
    UUID clientUUID;
    ClientTabList clientTabList;
    AbstractTabListLogic tabListHandler;

    @Before
    public void setUp() throws Exception {
        clientTabList = new ClientTabList();
        tabListHandler = new MockTabListLogic(clientTabList);
        tabListHandler.onConnected();
    }

    @After
    public void tearDown() throws Exception {
        tabListHandler = null;
        clientTabList = null;
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
            return getVisibleEntries().get(index).getDisplayName();
        }

        int getPing(int index) {
            return getVisibleEntries().get(index).getPing();
        }
    }

    class MockTabListLogic extends AbstractTabListLogic {
        private final ClientTabList clientTabList;

        public MockTabListLogic(ClientTabList clientTabList) {
            super(null);
            this.clientTabList = clientTabList;
        }

        private void validateConstraints() {
            // validate client tab list
            if (passtrough) {
                Assert.assertEquals("server client tab size mismatch", serverTabList.size(), clientTabList.entries.size());
                for (TabListItem item : serverTabList.values()) {
                    assertTrue("Missing player", clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    Assert.assertEquals("display name passthrough", item.getDisplayName(), entry.getDisplayName());
                    Assert.assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    Assert.assertEquals("ping passthrough", item.getPing(), entry.getPing());
                    Assert.assertEquals("gamemode passthrough", item.getGamemode(), entry.getGamemode());
                    Assert.assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    Assert.assertArrayEquals("display name passthrough", item.getProperties(), entry.getProperties());
                }
            } else if (size == 80) {
                Assert.assertEquals("server client tab size mismatch", 80, clientTabList.getSize());
                Assert.assertEquals("server client tab size mismatch", serverTabList.size() + 80, clientTabList.entries.size());
                // fake players
                for (int i = 0; i < size; i++) {
                    assertEquals("uuid", fakePlayerUUIDs[i], clientUuid[i]);
                    assertEquals("username", fakePlayerUsernames[i], clientUsername[i]);
                    Assert.assertEquals("uuid", clientUuid[i], clientTabList.getVisibleEntries().get(i).getUuid());
                    Assert.assertEquals("username", clientUsername[i], clientTabList.getVisibleEntries().get(i).getUsername());
                    Assert.assertEquals("text", clientText[i], clientTabList.getText(i));
                    Assert.assertEquals("ping", clientPing[i], clientTabList.getPing(i));
                    Assert.assertArrayEquals("skin", clientSkin[i].getProperties(), clientTabList.getProperties(i));
                }
                // real players
                for (TabListItem item : serverTabList.values()) {
                    assertTrue("Missing player", clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    Assert.assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    Assert.assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    //assertEquals("display name passthrough", item.getDisplayName(), entry.getDisplayName());
                    //assertEquals("ping passthrough", item.getPing(), entry.getPing());
                    Assert.assertEquals("gamemode passthrough", item.getGamemode(), entry.getGamemode());
                    Assert.assertArrayEquals("skin passthrough", item.getProperties(), entry.getProperties());
                }
            } else {
                // validate inner constraints
                for (Object2IntMap.Entry<UUID> entry : uuidToSlotMap.object2IntEntrySet()) {
                    assertEquals("uuidToSlotMap constraint violation", clientUuid[entry.getIntValue()], entry.getKey());
                }

                for (TabListItem item : serverTabList.values()) {
                    assertTrue("uuidToSlotMap doesn't contain " + item.getUuid(), uuidToSlotMap.containsKey(item.getUuid()));
                }

                Assert.assertEquals("server client tab size mismatch", size, clientTabList.getSize());
                boolean isCitizensDisordered = serverTabList.keySet().stream().anyMatch(u -> u.version() == 2);
                if (!isCitizensDisordered) {
                    // validate inner constraints
                    for (Map.Entry<String, Integer> entry : nameToSlotMap.entrySet()) {
                        assertEquals("nameToSlotMap constraint violation", clientUsername[entry.getValue()], entry.getKey());
                        assertTrue("team doesn't exist: " + fakePlayerUsernames[entry.getValue()], clientTabList.teams.containsKey(fakePlayerUsernames[entry.getValue()]));
                        assertTrue("player not in team: " + entry.getKey() + " in " + fakePlayerUsernames[entry.getValue()], clientTabList.teams.get(fakePlayerUsernames[entry.getValue()]).getPlayers().contains(entry.getKey()));
                    }

                    for (TabListItem item : serverTabList.values()) {
                        assertTrue("nameToSlotMap doesn't contain " + item.getUsername(), nameToSlotMap.containsKey(item.getUsername()));
                    }

                    // validate client view
                    for (int i = 0; i < size; i++) {
                        Assert.assertEquals("uuid", clientUuid[i], clientTabList.getVisibleEntries().get(i).getUuid());
                        Assert.assertEquals("username", clientUsername[i], clientTabList.getVisibleEntries().get(i).getUsername());
                        Assert.assertEquals("text", clientText[i], clientTabList.getText(i));
                        Assert.assertEquals("ping", clientPing[i], clientTabList.getPing(i));
                        if (clientUuid[i] == fakePlayerUUIDs[i]) {
                            Assert.assertArrayEquals("skin", clientSkin[i].getProperties(), clientTabList.getProperties(i));
                        } else {
                            Assert.assertArrayEquals("skin", serverTabList.get(clientUuid[i]).getProperties(), clientTabList.getProperties(i));
                        }
                        assertEquals("nameToSlotMap:" + i, i, nameToSlotMap.getInt(clientUsername[i]));
                    }
                }
                // real players
                for (TabListItem item : serverTabList.values()) {
                    assertTrue("Missing player " + item, clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    Assert.assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    Assert.assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    //assertEquals("display name passthrough", item.getDisplayName(), entry.getDisplayName());
                    //assertEquals("ping passthrough", item.getPing(), entry.getPing());
                    //assertEquals("gamemode passthrough", item.getGamemode(), entry.getGamemode());
                    Assert.assertArrayEquals("skin passthrough", item.getProperties(), entry.getProperties());
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
        public void setSlot(int index, Icon skin, String text, int ping) {
            validateConstraints();
            super.setSlot(index, skin, text, ping);
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
