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

package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class AbstractLegacyTabListTest {
    private ClientTabList clientTabList;
    private MockLegacyTabList tabListHandler;

    @Before
    public void setUp() throws Exception {
        clientTabList = new ClientTabList();
        tabListHandler = new MockLegacyTabList(60, clientTabList);
    }

    @After
    public void tearDown() throws Exception {
        tabListHandler = null;
        clientTabList = null;
    }

    @Test
    public void testTabList() {
        tabListHandler.setSize(60);
        tabListHandler.setPassTrough(false);
        for (int i = 0; i < 60; i++) {
            tabListHandler.setSlot(i, String.format("Slot %02d", i), i);
        }
    }

    private static class ClientTabList {
        private Map<String, Integer> ping = new HashMap<>();
        private List<String> players = new ArrayList<>();
        private final Map<String, Team> teams = new HashMap<>();
        private final Map<String, String> playerToTeamMap = new HashMap<>();
    }

    private static class MockLegacyTabList extends AbstractLegacyTabList {
        private final ClientTabList clientTabList;

        public MockLegacyTabList(int maxSize, ClientTabList clientTabList) {
            super(maxSize);
            this.clientTabList = clientTabList;
        }

        private void validateConstraints() {
            if (passThrough) {
                assertEquals(serverTabListEntryNames.size(), clientTabList.players.size());
            } else {
                assertEquals(clientSize, usedSlots);
                assertEquals(usedSlots, clientTabList.players.size());
                for (int i = 0; i < usedSlots; i++) {
                    assertEquals((long) clientPing[i], (long) clientTabList.ping.get(clientTabList.players.get(i)));
                    String team = clientTabList.playerToTeamMap.get(clientTabList.players.get(i));
                    assertNotNull(team);
                    assertTrue(clientText[i].startsWith(clientTabList.teams.get(team).getPrefix()));
                }
            }
        }

        @Override
        protected void sendPacket(DefinedPacket packet) {

            if (packet instanceof PlayerListItem) {
                for (PlayerListItem.Item item : ((PlayerListItem) packet).getItems()) {
                    switch (((PlayerListItem) packet).getAction()) {
                        case ADD_PLAYER:
                            if (!clientTabList.ping.containsKey(item.getDisplayName())) {
                                clientTabList.players.add(item.getDisplayName());
                            }
                            clientTabList.ping.put(item.getDisplayName(), item.getPing());
                            break;
                        case REMOVE_PLAYER:
                            assertTrue(clientTabList.ping.containsKey(item.getDisplayName()));
                            clientTabList.players.remove(item.getDisplayName());
                            clientTabList.ping.remove(item.getDisplayName());
                            break;
                        default:
                            fail();
                    }
                }
            } else if (packet instanceof net.md_5.bungee.protocol.packet.Team) {
                if (((net.md_5.bungee.protocol.packet.Team) packet).getMode() == 1) {
                    Team team = clientTabList.teams.remove(((net.md_5.bungee.protocol.packet.Team) packet).getName());
                    assertNotNull(team);
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
                    assertNotNull(t);

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
        public void setSize(int size) {
            validateConstraints();
            super.setSize(size);
            validateConstraints();
        }

        @Override
        public void setPassTrough(boolean passThrough) {
            validateConstraints();
            super.setPassTrough(passThrough);
            validateConstraints();
        }

        @Override
        public void setSlot(int i, String text, int ping) {
            validateConstraints();
            super.setSlot(i, text, ping);
            validateConstraints();
        }
    }

}