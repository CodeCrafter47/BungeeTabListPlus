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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.handler.AbstractTabOverlayHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import com.google.common.base.MoreObjects;
import com.google.common.collect.ComparisonChain;
import de.codecrafter47.taboverlay.ProfileProperty;
import de.codecrafter47.taboverlay.handler.ContentOperationMode;
import de.codecrafter47.taboverlay.handler.SimpleTabOverlay;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.score.Team;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.DefinedPacket;
import net.md_5.bungee.protocol.packet.PlayerListHeaderFooter;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;

import java.util.*;
import java.util.logging.Logger;

import static org.junit.Assert.assertTrue;

public class AbstractTabListLogicTestBase {
    final UUID clientUUID = UUID.randomUUID();
    ClientTabList clientTabList;
    TabListHandler tabListHandler;

    @Before
    public void setUp() throws Exception {
        clientTabList = new ClientTabList();
        tabListHandler = new MockTabListLogic(clientTabList);
        tabListHandler.onConnected();
        tabListHandler.onServerSwitch(false);
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
            String displayName = getVisibleEntries().get(index).getDisplayName();
            String text = displayName == null ? null : BaseComponent.toLegacyText(ComponentSerializer.parse(displayName));
            while (text != null && text.startsWith("\u00A7f")) {
                text = text.substring(2);
            }
            return text;
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

    class MockTabListLogic extends TabListHandler {
        private final MockTabOverlayHandler tabOverlayHandler;

        private boolean passtrough = true;
        final Map<UUID, TabListEntry> serverTabList = new HashMap<>();
        private int size = 0;
        private String[] clientText = new String[80];
        private int[] clientPing = new int[80];
        private String[][][] clientSkin = new String[80][][];
        private UUID[] clientUuid = new UUID[80];
        private SimpleTabOverlay simpleTabOverlay;

        public MockTabListLogic(ClientTabList clientTabList) {
            super(null);
            this.tabOverlayHandler = new MockTabOverlayHandler(clientTabList);
        }

        private void validateConstraints() {
            boolean isCitizensDisordered = false;
            // validate client tab list
            if (passtrough) {
                Assert.assertEquals("server client tab size mismatch", serverTabList.size(), clientTabList.entries.size());
                for (TabListEntry item : serverTabList.values()) {
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
                Assert.assertEquals("server client tab size mismatch", serverTabList.size() + 97, clientTabList.entries.size());
                // fake players
                for (int i = 0; i < size; i++) {
                    Assert.assertEquals("text", clientText[i], clientTabList.getText(i));
                    Assert.assertEquals("ping", clientPing[i], clientTabList.getPing(i));
                    Assert.assertArrayEquals("skin", clientSkin[i], clientTabList.getProperties(i));
                }
                // real players
                for (TabListEntry item : serverTabList.values()) {
                    assertTrue("Missing player", clientTabList.entries.containsKey(item.getUuid()));
                    TabListEntry entry = clientTabList.entries.get(item.getUuid());
                    Assert.assertEquals("uuid passthrough", item.getUuid(), entry.getUuid());
                    Assert.assertEquals("username passthrough", item.getUsername(), entry.getUsername());
                    Assert.assertArrayEquals("skin passthrough", item.getProperties(), entry.getProperties());
                }
            } else {
                Assert.assertEquals("server client tab size mismatch", Math.min(80, Math.max(size, serverTabList.size())), clientTabList.getSize());
                isCitizensDisordered = serverTabList.keySet().stream().anyMatch(u -> u.version() == 2);
                if (!isCitizensDisordered) {
                    // validate client view
                    for (int i = 0; i < size; i++) {
                        Assert.assertEquals("text", clientText[i], clientTabList.getText(i));
                        Assert.assertEquals("ping", clientPing[i], clientTabList.getPing(i));
                        if (!serverTabList.containsKey(clientTabList.getVisibleEntries().get(i).getUuid())) {
                            Assert.assertArrayEquals("skin", clientSkin[i], clientTabList.getProperties(i));
                        }
                    }
                }
                // real players
                for (TabListEntry item : serverTabList.values()) {
                    assertTrue("Missing player", clientTabList.entries.containsKey(item.getUuid()));
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
        public void onConnected() {
            simpleTabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
            tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            for (int i = 0; i < 80; i++) {
                clientPing[i] = 0;
                clientSkin[i] = new String[][]{};
                clientText[i] = "";
            }
        }

        @Override
        public void onDisconnected() {
        }

        @Override
        public PacketListenerResult onPlayerListPacket(PlayerListItem packet) {
            validateConstraints();

            switch (packet.getAction()) {
                case ADD_PLAYER:
                    for (PlayerListItem.Item item : packet.getItems()) {
                        serverTabList.put(item.getUuid(), new TabListEntry(item));
                    }
                    break;
                case UPDATE_GAMEMODE:
                    for (PlayerListItem.Item item : packet.getItems()) {
                        TabListEntry playerListEntry = serverTabList.get(item.getUuid());
                        if (playerListEntry != null) {
                            playerListEntry.setGamemode(item.getGamemode());
                        }
                    }
                    break;
                case UPDATE_LATENCY:
                    for (PlayerListItem.Item item : packet.getItems()) {
                        TabListEntry playerListEntry = serverTabList.get(item.getUuid());
                        if (playerListEntry != null) {
                            playerListEntry.setPing(item.getPing());
                        }
                    }
                    break;
                case UPDATE_DISPLAY_NAME:
                    for (PlayerListItem.Item item : packet.getItems()) {
                        TabListEntry playerListEntry = serverTabList.get(item.getUuid());
                        if (playerListEntry != null) {
                            playerListEntry.setDisplayName(item.getDisplayName());
                        }
                    }
                    break;
                case REMOVE_PLAYER:
                    for (PlayerListItem.Item item : packet.getItems()) {
                        serverTabList.remove(item.getUuid());
                    }
                    break;
            }

            tabOverlayHandler.onPlayerListPacket(packet);
            validateConstraints();
            return PacketListenerResult.CANCEL;
        }

        @Override
        public PacketListenerResult onTeamPacket(net.md_5.bungee.protocol.packet.Team packet) {
            validateConstraints();
            tabOverlayHandler.onTeamPacket(packet);
            validateConstraints();
            return PacketListenerResult.CANCEL;
        }

        @Override
        public PacketListenerResult onPlayerListHeaderFooterPacket(PlayerListHeaderFooter packet) {
            validateConstraints();
            tabOverlayHandler.onPlayerListHeaderFooterPacket(packet);
            validateConstraints();
            return PacketListenerResult.CANCEL;
        }

        @Override
        public void onServerSwitch(boolean is13OrLater) {
            validateConstraints();
            tabOverlayHandler.onServerSwitch(is13OrLater);
            serverTabList.clear();
            validateConstraints();
        }

        @Override
        public void setPassThrough(boolean passTrough) {
            validateConstraints();
            passtrough = passTrough;
            if (passTrough) {
                tabOverlayHandler.enterContentOperationMode(ContentOperationMode.PASS_TROUGH);
            } else {
                simpleTabOverlay = tabOverlayHandler.enterContentOperationMode(ContentOperationMode.SIMPLE);
                simpleTabOverlay.setSize(size);
                for (int i = 0; i < size; i++) {
                    simpleTabOverlay.setSlot(i, clientUuid[i], makeIcon(clientSkin[i]), clientText[i], clientPing[i]);
                }
            }
            validateConstraints();
        }

        private de.codecrafter47.taboverlay.Icon makeIcon(String[][] strings) {
            if (strings.length == 0) {
                return de.codecrafter47.taboverlay.Icon.DEFAULT_STEVE;
            }
            return new de.codecrafter47.taboverlay.Icon(new ProfileProperty(strings[0][0], strings[0][1], strings[0][2]));
        }

        private String[][] normalizeProperties(String[][] properties) {
            return properties;
        }

        @Override
        public void setSize(int size) {
            validateConstraints();
            this.size = size;
            simpleTabOverlay.setSize(size);
            for (int i = 0; i < size; i++) {
                simpleTabOverlay.setSlot(i, clientUuid[i], makeIcon(clientSkin[i]), clientText[i], clientPing[i]);
            }
            validateConstraints();
        }

        @Override
        public void setSlot(int index, Icon skin, String text, int ping) {
            validateConstraints();
            clientText[index] = text;
            clientPing[index] = ping;
            clientSkin[index] = normalizeProperties(skin.getProperties());
            clientUuid[index] = skin.getPlayer();
            if (index < size) {
                simpleTabOverlay.setSlot(index, skin.getPlayer(), makeIcon(clientSkin[index]), text, ping);
            }
            validateConstraints();
        }

        @Override
        public void updateText(int index, String text) {
            validateConstraints();
            clientText[index] = text;
            simpleTabOverlay.setText(index, text);
            validateConstraints();
        }

        @Override
        public void updatePing(int index, int ping) {
            validateConstraints();
            clientPing[index] = ping;
            simpleTabOverlay.setPing(index, ping);
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
