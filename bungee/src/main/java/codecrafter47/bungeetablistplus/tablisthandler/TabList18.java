/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.packet.PacketAccess;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Charsets;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

import java.util.UUID;

public class TabList18 implements TabListHandler {

    private static String getSlotID(int n) {
        String s = Integer.toString(n + 1000);
        return " ?tab" + s;
    }

    private final int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int sendSlots = 0;

    private final String send[] = new String[ConfigManager.getTabSize()];

    private final String[] sendTextures = new String[ConfigManager.getTabSize()];

    private final PacketAccess packetAccess = BungeeTabListPlus.getInstance().getPacketAccess();

    private final PlayerTablistHandler playerTablistHandler;

    public TabList18(PlayerTablistHandler playerTablistHandler) {
        this.playerTablistHandler = playerTablistHandler;
    }

    @Override
    public void sendTabList(ITabList tabList) {
        resize(tabList.getColumns() * tabList.getRows());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getColumns() * tabList.getRows(); i++) {
            Slot line = tabList.getSlot((i % tabList.getRows()) * tabList.
                    getColumns() + (i / tabList.getRows()));
            if (line == null) {
                line = new Slot(" ", tabList.getDefaultPing());
            }
            String text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(playerTablistHandler.getPlayer(), line.text, BungeeTabListPlus.getInstance().getBungeePlayerProvider().wrapPlayer(playerTablistHandler.getPlayer()));
            text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(playerTablistHandler.getPlayer(), text);
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

            if (line.getSkin() == SkinManager.defaultSkin) {
                line.setSkin(tabList.getDefaultSkin());
            }
            updateSlot(i, text, line.ping, line.getSkin());
        }

        // update header/footer
        if (packetAccess.isTabHeaderFooterSupported()) {
            String header = tabList.getHeader();
            if (header != null && header.endsWith("" + ChatColor.COLOR_CHAR)) {
                header = header.substring(0, header.length() - 1);
            }
            String footer = tabList.getFooter();
            if (footer != null && footer.endsWith("" + ChatColor.COLOR_CHAR)) {
                footer = footer.substring(0, footer.length() - 1);
            }
            if (header != null || footer != null) {
                String headerJson = ComponentSerializer.toString(TextComponent.fromLegacyText(header != null ? header : ""));
                String footerJson = ComponentSerializer.toString(TextComponent.fromLegacyText(footer != null ? footer : ""));
                packetAccess.setTabHeaderAndFooter(playerTablistHandler.getPlayer().unsafe(), headerJson, footerJson);
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
        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getSlotID(i)).getBytes(Charsets.UTF_8));
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
            UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
            String[][] properties;
            if (textures != null) {
                properties = new String[][]{{"textures", textures[0], textures[1]}};
                sendTextures[row] = textures[0];
            } else {
                properties = new String[0][0];
                sendTextures[row] = null;
            }
            packetAccess.createOrUpdatePlayer(playerTablistHandler.getPlayer().unsafe(), offlineId, getSlotID(row), 0, ping, properties);
            textureUpdate = true;
            slots_ping[row] = ping;
        }

        // update ping
        if (ping != slots_ping[row]) {
            slots_ping[row] = ping;
            UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
            packetAccess.updatePing(playerTablistHandler.getPlayer().unsafe(), offlineId, ping);
        }

        // update name
        String old = send[row];
        if (old == null || !old.equals(text) || textureUpdate) {
            send[row] = text;
            UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
            packetAccess.updateDisplayName(playerTablistHandler.getPlayer().unsafe(), offlineId, ComponentSerializer.toString(TextComponent.fromLegacyText(text)));
        }
    }

    private void createSlot(int row) {
        UUID offlineId = UUID.nameUUIDFromBytes(("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
        packetAccess.createOrUpdatePlayer(playerTablistHandler.getPlayer().unsafe(), offlineId, getSlotID(row), 0, 0, new String[0][0]);
        send[row] = null;
        slots_ping[row] = 0;
        sendTextures[row] = null;
    }

    @Override
    public void unload() {
        resize(0);
    }
}