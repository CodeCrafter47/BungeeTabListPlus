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
import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.layout.TabListContext;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.util.ColorParser;
import net.md_5.bungee.api.ChatColor;

/**
 * @author Florian Stober
 */
public class ScoreboardTabList implements TabListHandler {
    private final PlayerTablistHandler playerTablistHandler;

    private static String getSlotID(int n) {
        String hex = Integer.toHexString(n + 1);
        char[] alloc = new char[hex.length() * 2];
        for (int i = 0; i < alloc.length; i++) {
            if (i % 2 == 0) {
                alloc[i] = ChatColor.COLOR_CHAR;
            } else {
                alloc[i] = hex.charAt(i / 2);
            }
        }
        return String.valueOf(ChatColor.MAGIC) + String.valueOf(alloc) + ChatColor.RESET;
    }

    private final int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int sendSlots = 0;

    private final String send[] = new String[ConfigManager.getTabSize()];

    public ScoreboardTabList(PlayerTablistHandler playerTablistHandler) {
        this.playerTablistHandler = playerTablistHandler;
    }

    @Override
    public void sendTabList(ITabList tabList, TabListContext context) {
        resize(tabList.getUsedSlots());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getUsedSlots(); i++) {
            Slot line = tabList.getSlot(i);
            if (line == null) {
                line = new Slot("", tabList.getDefaultPing());
            }
            String text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(playerTablistHandler.getPlayer(), line.text, BungeeTabListPlus.getInstance().getBungeePlayerProvider().wrapPlayer(playerTablistHandler.getPlayer()), context);
            text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(playerTablistHandler.getPlayer(), text, context);
            text = ChatColor.translateAlternateColorCodes('&', text);
            if (charLimit > 0) {
                text = ColorParser.substringIgnoreColors(text, charLimit);
            }

            String old = send[i];
            if (old == null || !old.equals(text) || line.ping != slots_ping[i]) {
                updateSlot(i, text, line.ping);
            }
        }
    }

    private void resize(int size) {
        if (size == sendSlots) {
            return;
        }
        if (size > sendSlots) {
            for (int i = sendSlots; i < size; i++) {
                sendSlot(i);
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

    private void sendSlot(int i) {

        if (i >= ConfigManager.getTabSize()) {
            return;
        }
        BungeeTabListPlus.getInstance().getLegacyPacketAccess().createOrUpdatePlayer(
                playerTablistHandler.getPlayer().unsafe(), getSlotID(i), slots_ping[i]);
        send[i] = "";
    }

    private void removeSlot(int i) {
        BungeeTabListPlus.getInstance().getLegacyPacketAccess().removePlayer(
                playerTablistHandler.getPlayer().unsafe(), getSlotID(i));
        BungeeTabListPlus.getInstance().getLegacyPacketAccess().removeTeam(
                playerTablistHandler.getPlayer().unsafe(), getSlotID(i));
    }

    private void updateSlot(int row, String text, int ping) {
        if (ping != slots_ping[row]) {
            BungeeTabListPlus.getInstance().getLegacyPacketAccess().
                    createOrUpdatePlayer(playerTablistHandler.getPlayer().unsafe(), getSlotID(row),
                            ping);
        }
        send[row] = text;
        slots_ping[row] = ping;
        String split[] = splitText(text);
        BungeeTabListPlus.getInstance().getLegacyPacketAccess().updateTeam(
                playerTablistHandler.getPlayer().unsafe(), getSlotID(row), split[0], /*split[1]*/ "", split[1]);
    }

    private void createSlot(int row) {
        BungeeTabListPlus.getInstance().getLegacyPacketAccess().createTeam(
                playerTablistHandler.getPlayer().unsafe(), getSlotID(row));
    }

    private String[] splitText(String s) {
        String ret[] = new String[3];
        int left = s.length();
        if (left <= 16) {
            ret[0] = s;
            ret[1] = "";
            ret[2] = "";
        } else {
            int end = s.charAt(15) == ChatColor.COLOR_CHAR ? 15 : 16;
            ret[0] = s.substring(0, end);
            int start = ColorParser.endofColor(s, end);
            String colors = ColorParser.extractColorCodes(s.substring(0, start));
            end = start + 16 - colors.length();
            if (end >= s.length()) {
                end = s.length();
            }
            ret[1] = colors + s.substring(start, end);
            start = end;
            end += 16;
            if (end >= s.length()) {
                end = s.length();
            }
            ret[2] = s.substring(start, end);
        }
        return ret;
    }

    @Override
    public void unload() {
        resize(0);
    }
}
