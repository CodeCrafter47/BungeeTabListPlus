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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.Skin;
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.packet.PacketAccess;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Charsets;
import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.*;
import java.util.logging.Level;

public class TabList18v3 implements TabListHandler {

    private static final String[] fakePlayerUsernames = new String[80];
    private static final UUID[] fakePlayerUUIDs = new UUID[80];

    {
        for (int i = 0; i < 80; i++) {
            fakePlayerUsernames[i] = " §" + (char) (970 + i) + " ?tab";
            fakePlayerUUIDs[i] = UUID.nameUUIDFromBytes(("OfflinePlayer:" + fakePlayerUsernames[i]).getBytes(Charsets.UTF_8));
        }
    }

    private final Map<UUID, Integer> sendPing = new HashMap<>();

    private int sendSlots = 0;

    private final Map<UUID, String> send = new HashMap<>();

    private final Map<UUID, String> sendTextures = new HashMap<>();

    private final Map<UUID, String> sendUsernames = new HashMap<>();

    private final TIntObjectMap<String> sendTeam = new TIntObjectHashMap<>();

    private final PacketAccess packetAccess = BungeeTabListPlus.getInstance().getPacketAccess();

    private final CustomTabList18 playerTabListHandler;

    private String sentHeader = null;
    private String sendFooter = null;

    public TabList18v3(CustomTabList18 playerTabListHandler) {
        this.playerTabListHandler = playerTabListHandler;
    }

    @Override
    public void sendTabList(TabList tabList) {
        synchronized (playerTabListHandler.usernames) {
            tabList = tabList.flip();

            int numFakePlayers = 80;

            int tab_size = tabList.getRows() * tabList.getColumns();

            if (tabList.shouldShrink()) {
                if (tabList.flip().getUsedSlots() < tabList.getUsedSlots()) {
                    tabList = tabList.flip();
                }
                tab_size = tabList.getUsedSlots();
                if (tab_size < playerTabListHandler.uuids.size()) {
                    tab_size = playerTabListHandler.uuids.size();
                    tab_size = tab_size > 80 ? 80 : tab_size;
                }
            }

            if (tab_size < 80) {
                numFakePlayers = tab_size - playerTabListHandler.uuids.size();
                if (numFakePlayers < 0) {
                    BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Could not update tablist for {0}. Please increase tab_size", playerTabListHandler.getPlayer().getName());
                    return;
                }
            }

            resize(numFakePlayers);

            int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().charLimit;

            // create uuidList
            List<UUID> list = new ArrayList<>(playerTabListHandler.uuids.keySet());

            sendUsernames.clear();
            for (UUID uuid : list) {
                sendUsernames.put(uuid, playerTabListHandler.uuids.get(uuid));
            }

            List<UUID> fakeUUIDs = new ArrayList<>();
            for (int i = 0; i < numFakePlayers; i++) {
                UUID uuid = fakePlayerUUIDs[i];
                fakeUUIDs.add(uuid);
                sendUsernames.put(uuid, fakePlayerUsernames[i]);
            }

            for (int i = 0; i < tab_size; i++) {
                Slot slot = tabList.getSlot(i);
                String text;
                int ping;
                Skin skin;
                if (slot != null) {
                    text = slot.getText();
                    text = ChatColor.translateAlternateColorCodes('&', text);
                    if (charLimit > 0) {
                        text = ColorParser.substringIgnoreColors(text, charLimit);
                        for (int j = charLimit - ChatColor.stripColor(text).length(); j > 0; j--) {
                            text += ' ';
                        }
                    }
                    if (text.endsWith("" + ChatColor.COLOR_CHAR)) {
                        text = text.substring(0, text.length() - 1);
                    }
                    ping = slot.getPing();
                    skin = slot.getSkin();
                    if (skin == SkinManager.defaultSkin) {
                        skin = tabList.getDefaultSkin();
                    }
                } else {
                    text = "";
                    ping = tabList.getDefaultPing();
                    skin = tabList.getDefaultSkin();
                }

                UUID uuid = null;
                boolean reorder = true;
                if (skin.getOwner() != null && list.contains(skin.getOwner()) && (((UserConnection) playerTabListHandler.getPlayer()).getGamemode() != 3 || !Objects.equals(skin.getOwner(), playerTabListHandler.getPlayer().getUniqueId()))) {
                    uuid = skin.getOwner();
                    list.remove(uuid);
                }
                if (uuid == null && !fakeUUIDs.isEmpty()) {
                    uuid = fakeUUIDs.get(0);
                    fakeUUIDs.remove(uuid);
                }
                if (uuid == null) {
                    for (int j = 0; j < list.size(); j++) {
                        uuid = list.get(0);
                        if (!(Objects.equals(uuid, playerTabListHandler.getPlayer().getUniqueId()) && ((UserConnection) playerTabListHandler.getPlayer()).getGamemode() == 3)) {
                            list.remove(uuid);
                            reorder = false;
                            break;
                        }
                    }
                }
                if (uuid == null) {
                    uuid = list.get(0);
                    list.remove(uuid);
                    reorder = false;
                }

                String oldPlayer = sendTeam.get(i);
                if (oldPlayer != null) {
                    if (!oldPlayer.equals(sendUsernames.get(uuid))) {
                        packetAccess.removePlayerFromTeam(playerTabListHandler.getPlayer().unsafe(), fakePlayerUsernames[i], oldPlayer);
                        packetAccess.addPlayerToTeam(playerTabListHandler.getPlayer().unsafe(), fakePlayerUsernames[i], sendUsernames.get(uuid));
                        sendTeam.put(i, sendUsernames.get(uuid));
                    }
                } else {
                    packetAccess.createTeam(playerTabListHandler.getPlayer().unsafe(), fakePlayerUsernames[i], sendUsernames.get(uuid));
                    sendTeam.put(i, sendUsernames.get(uuid));
                }

                updateSlot(uuid, text, ping, skin);
            }

            // update header/footer
            if (packetAccess.isTabHeaderFooterSupported()) {
                String header = tabList.getHeader();
                String footer = tabList.getFooter();
                if (!Objects.equals(header, sentHeader) || !Objects.equals(footer, sendFooter)) {
                    sentHeader = header;
                    sendFooter = footer;
                    if (header != null || footer != null) {
                        if (header != null && header.endsWith("" + ChatColor.COLOR_CHAR)) {
                            header = header.substring(0, header.length() - 1);
                        }
                        if (header != null) {
                            header = ChatColor.translateAlternateColorCodes('&', header);
                        }
                        if (footer != null && footer.endsWith("" + ChatColor.COLOR_CHAR)) {
                            footer = footer.substring(0, footer.length() - 1);
                        }
                        if (footer != null) {
                            footer = ChatColor.translateAlternateColorCodes('&', footer);
                        }
                        String headerJson = ComponentSerializer.toString(TextComponent.fromLegacyText(header != null ? header : ""));
                        String footerJson = ComponentSerializer.toString(TextComponent.fromLegacyText(footer != null ? footer : ""));
                        packetAccess.setTabHeaderAndFooter(playerTabListHandler.getPlayer().unsafe(), headerJson, footerJson);
                    }
                }
            }
        }
    }

    private void resize(int size) {
        if (size == sendSlots) {
            return;
        }
        if (size > sendSlots) {
            for (int i = sendSlots; i < size; i++) {
                createSlot(i);
            }
            sendSlots = size;
        } else if (size < sendSlots) {
            for (int i = size; i < sendSlots; i++) {
                removeSlot(i);
            }
        }
        sendSlots = size;
    }

    private void removeSlot(int i) {
        UUID offlineId = fakePlayerUUIDs[i];
        packetAccess.removePlayer(playerTabListHandler.getPlayer().unsafe(), offlineId);
        send.remove(offlineId);
        sendTextures.remove(offlineId);
        sendPing.remove(offlineId);
    }

    private void updateSlot(UUID offlineId, String text, int ping, Skin skin) {
        boolean textureUpdate = false;
        String[] textures = skin.toProperty();
        if (textures != null) {
            textures = new String[]{textures[1], textures[2]};
        }
        // textures
        if (!playerTabListHandler.uuids.containsKey(offlineId) && ((sendTextures.get(offlineId) == null && textures != null) || (sendTextures.get(offlineId) != null && textures == null) || (textures != null && sendTextures.get(offlineId) != null && !textures[0].equals(sendTextures.get(offlineId))))) {
            // update texture
            String[][] properties;
            if (textures != null) {
                properties = new String[][]{{"textures", textures[0], textures[1]}};
                sendTextures.put(offlineId, textures[0]);
            } else {
                properties = new String[0][0];
                sendTextures.remove(offlineId);
            }
            packetAccess.createOrUpdatePlayer(playerTabListHandler.getPlayer().unsafe(), offlineId, sendUsernames.get(offlineId), 0, ping, properties);
            textureUpdate = true;
            sendPing.put(offlineId, 0);
        }

        // update ping
        if (sendPing.get(offlineId) == null) {
            sendPing.put(offlineId, 0);
        }
        if (ping != sendPing.get(offlineId)) {
            sendPing.put(offlineId, ping);
            packetAccess.updatePing(playerTabListHandler.getPlayer().unsafe(), offlineId, ping);
        }

        // update name
        String old = send.get(offlineId);
        if (textureUpdate || old == null || !old.equals(text) || playerTabListHandler.requiresUpdate.contains(offlineId)) {
            send.put(offlineId, text);
            packetAccess.updateDisplayName(playerTabListHandler.getPlayer().unsafe(), offlineId, ComponentSerializer.toString(TextComponent.fromLegacyText(text)));
        }
    }

    private void createSlot(int row) {
        UUID offlineId = fakePlayerUUIDs[row];
        packetAccess.createOrUpdatePlayer(playerTabListHandler.getPlayer().unsafe(), offlineId, fakePlayerUsernames[row], 0, 0, new String[0][0]);
        sendPing.put(offlineId, 0);
        send.put(offlineId, null);
    }

    @Override
    public void unload() {
        resize(0);
        sendTeam.forEachKey(value -> {
            packetAccess.removeTeam(playerTabListHandler.getPlayer().unsafe(), fakePlayerUsernames[value]);
            return true;
        });
        sendTeam.clear();
    }
}