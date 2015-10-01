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
import codecrafter47.bungeetablistplus.tablist.GenericTabList;
import codecrafter47.bungeetablistplus.tablistproviders.ErrorTabListProvider;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Charsets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;

import java.util.Objects;
import java.util.UUID;

public class TabList18 implements TabListHandler {

    private static final String[] fakePlayerUsernames = new String[80];
    private static final UUID[] fakePlayerUUIDs = new UUID[80];

    {
        for (int i = 0; i < 80; i++) {
            fakePlayerUsernames[i] = " §" + (char) (970 + i) + " ?tab";
            fakePlayerUUIDs[i] = UUID.nameUUIDFromBytes(("OfflinePlayer:" + fakePlayerUsernames[i]).getBytes(Charsets.UTF_8));
        }
    }

    private final int[] slots_ping = new int[80];

    private int sendSlots = 0;

    private final String[] send = new String[80];

    private final String[] sendTextures = new String[80];

    private final PacketAccess packetAccess = BungeeTabListPlus.getInstance().getPacketAccess();

    private final PlayerTablistHandler playerTablistHandler;

    private String sentHeader = null;
    private String sendFooter = null;

    public TabList18(PlayerTablistHandler playerTablistHandler) {
        this.playerTablistHandler = playerTablistHandler;
    }

    @Override
    public void sendTabList(TabList tabList) {
        if (tabList.getColumns() * tabList.getRows() > 80) {
            // tab_size not supported
            tabList = new GenericTabList(4, 20);
            ErrorTabListProvider.constructErrorTabList(playerTablistHandler.getPlayer(), tabList, "Maximum tab_size for 1.8 is 80.", null);
        }
        resize(tabList.getColumns() * tabList.getRows());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getColumns() * tabList.getRows(); i++) {
            Slot slot = tabList.getSlot((i % tabList.getRows()) * tabList.getColumns() + (i / tabList.getRows()));
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
            updateSlot(i, text, ping, skin);
        }

        // update header/footer
        if (packetAccess.isTabHeaderFooterSupported()) {
            String header = tabList.getHeader();
            String footer = tabList.getFooter();
            if (!Objects.equals(header, sentHeader) && !Objects.equals(footer, sendFooter)) {
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
                    packetAccess.setTabHeaderAndFooter(playerTablistHandler.getPlayer().unsafe(), headerJson, footerJson);
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
        UUID uuid = fakePlayerUUIDs[i];
        packetAccess.removePlayer(playerTablistHandler.getPlayer().unsafe(), uuid);
    }

    private void updateSlot(int row, String text, int ping, Skin skin) {
        boolean textureUpdate = false;
        String[] textures = skin.toProperty();
        if (textures != null) {
            textures = new String[]{textures[1], textures[2]};
        }
        if ((sendTextures[row] == null && textures != null) || (sendTextures[row] != null && textures == null) || (textures != null && sendTextures[row] != null && !textures[0].equals(sendTextures[row]))) {
            // update texture
            UUID offlineId = fakePlayerUUIDs[row];
            String[][] properties;
            if (textures != null) {
                properties = new String[][]{{"textures", textures[0], textures[1]}};
                sendTextures[row] = textures[0];
            } else {
                properties = new String[0][0];
                sendTextures[row] = null;
            }
            packetAccess.createOrUpdatePlayer(playerTablistHandler.getPlayer().unsafe(), offlineId, fakePlayerUsernames[row], 0, ping, properties);
            textureUpdate = true;
            slots_ping[row] = ping;
        }

        // update ping
        if (ping != slots_ping[row]) {
            slots_ping[row] = ping;
            UUID offlineId = fakePlayerUUIDs[row];
            packetAccess.updatePing(playerTablistHandler.getPlayer().unsafe(), offlineId, ping);
        }

        // update name
        String old = send[row];
        if (old == null || !old.equals(text) || textureUpdate) {
            send[row] = text;
            UUID offlineId = fakePlayerUUIDs[row];
            packetAccess.updateDisplayName(playerTablistHandler.getPlayer().unsafe(), offlineId, ComponentSerializer.toString(TextComponent.fromLegacyText(text)));
        }
    }

    private void createSlot(int row) {
        UUID offlineId = fakePlayerUUIDs[row];
        packetAccess.createOrUpdatePlayer(playerTablistHandler.getPlayer().unsafe(), offlineId, fakePlayerUsernames[row], 0, 0, new String[0][0]);
        send[row] = null;
        slots_ping[row] = 0;
        sendTextures[row] = null;
    }

    @Override
    public void unload() {
        resize(0);
    }
}