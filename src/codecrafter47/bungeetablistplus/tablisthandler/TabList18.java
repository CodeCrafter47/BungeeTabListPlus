/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
import codecrafter47.bungeetablistplus.config.TabListProvider;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.packets.TabHeaderPacket;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Charsets;
import java.util.UUID;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.chat.ComponentSerializer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.PlayerListItem.Item;

public class TabList18 extends CustomTabList18 implements IMyTabListHandler {

    private static String getSlotID(int n) {
        String s = Integer.toString(n + 1000);
        return "0000tab#" + s;
    }

    private final int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int sendSlots = 0;

    private final String send[] = new String[ConfigManager.getTabSize()];

    public TabList18(ProxiedPlayer player) {
        super(player);
    }

    @Override
    public void recreate() {
        if (getPlayer().getServer() != null) {
            if (BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().excludeServers.contains(getPlayer().
                            getServer().getInfo().getName()) || isExcluded) {
                unload();
                return;
            }
        }

        TabListProvider tlp = BungeeTabListPlus.getInstance().
                getTabListManager().getTabListForPlayer(super.getPlayer());
        if (tlp == null) {
            exclude();
            unload();
            return;
        }
        TabList tabList = tlp.getTabList(super.getPlayer());

        resize(tabList.getCollums() * tabList.getRows());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getCollums() * tabList.getRows(); i++) {
            Slot line = tabList.getSlot((i % tabList.getRows()) * tabList.
                    getCollums() + (i / tabList.getRows()));
            if (line == null) {
                line = new Slot(" ");
            }
            String text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(line.text, super.getPlayer());
            text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(text);
            text = ChatColor.translateAlternateColorCodes('&', text);
            if (charLimit > 0) {
                text = ColorParser.substringIgnoreColors(text, charLimit);
            }

            String old = send[i];
            if (old == null || !old.equals(text) || line.ping != slots_ping[i]) {
                updateSlot(i, text, line.ping);
            }
        }

        // update header/footer
        if (tabList.getHeader() != null && tabList.getFooter() != null) {
            TabHeaderPacket packet = new TabHeaderPacket();
            if (tabList.getHeader() != null) {
                packet.setHeader(ComponentSerializer.toString(TextComponent.
                        fromLegacyText(tabList.getHeader())));
            }
            if (tabList.getFooter() != null) {
                packet.setFooter(ComponentSerializer.toString(TextComponent.
                        fromLegacyText(tabList.getFooter())));
            }
            player.unsafe().sendPacket(packet);
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
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        Item item = new Item();
        UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + getSlotID(i)).getBytes(Charsets.UTF_8));
        item.setUuid(offlineId);
        item.setDisplayName(" ");
        item.setGamemode(0);
        item.setPing(0);
        item.setUsername(getSlotID(i));
        item.setProperties(new String[0][0]);
        pli.setItems(new Item[]{item});
        getPlayer().unsafe().sendPacket(pli);
    }

    private void updateSlot(int row, String text, int ping) {
        if (ping != slots_ping[row]) {
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.UPDATE_LATENCY);
            Item item = new Item();
            UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setUsername(getSlotID(row));
            item.setProperties(new String[0][0]);
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
        }
        send[row] = text;
        slots_ping[row] = ping;
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        Item item = new Item();
        UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
        item.setUuid(offlineId);
        item.setDisplayName(ComponentSerializer.toString(TextComponent.
                fromLegacyText(text)));
        item.setUsername(getSlotID(row));
        item.setProperties(new String[0][0]);
        pli.setItems(new Item[]{item});
        getPlayer().unsafe().sendPacket(pli);
    }

    private void createSlot(int row) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        Item item = new Item();
        UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
        item.setUuid(offlineId);
        item.setDisplayName(" ");
        item.setGamemode(0);
        item.setPing(0);
        item.setUsername(getSlotID(row));
        item.setProperties(new String[0][0]);
        pli.setItems(new Item[]{item});
        getPlayer().unsafe().sendPacket(pli);
    }

    public void unload() {
        resize(0);
    }
}
