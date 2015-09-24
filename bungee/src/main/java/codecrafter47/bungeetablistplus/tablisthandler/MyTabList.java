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
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.util.ColorParser;
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.ChatColor;

import java.util.Collection;
import java.util.HashSet;

public class MyTabList implements TabListHandler {

    private static final char[] FILLER = new char[]{'0', '1', '2', '2', '4',
            '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int MAX_LEN = 16;
    /* ======================================================================== */
    private final Collection<String> sentStuff = new HashSet<>();
    /* ======================================================================== */
    private final String[] sent = new String[ConfigManager.getTabSize()];
    private final String[] slots = new String[ConfigManager.getTabSize()];
    private final int[] slots_ping = new int[ConfigManager.getTabSize()];
    private int rowLim;
    private final PlayerTablistHandler playerTablistHandler;

    public MyTabList(PlayerTablistHandler playerTablistHandler) {
        this.playerTablistHandler = playerTablistHandler;
    }

    private static char[] base(int n) {
        String hex = Integer.toHexString(n + 1);
        char[] alloc = new char[hex.length() * 2];
        for (int i = 0; i < alloc.length; i++) {
            if (i % 2 == 0) {
                alloc[i] = ChatColor.COLOR_CHAR;
            } else {
                alloc[i] = hex.charAt(i / 2);
            }
        }
        return alloc;
    }

    @Override
    public void sendTabList(ITabList tabList) {
        clear();

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().charLimit;

        for (int i = 0; i < tabList.getUsedSlots(); i++) {
            Slot slot = tabList.getSlot(i);
            String text;
            int ping;
            if (slot != null) {
                text = slot.getText();
                text = ChatColor.translateAlternateColorCodes('&', text);
                if (charLimit > 0) {
                    text = ColorParser.substringIgnoreColors(text, charLimit);
                }
                ping = slot.getPing();
            } else {
                text = "";
                ping = tabList.getDefaultPing();
            }

            setSlot(i, text, ping, false);
        }
        update();
    }

    private String setSlot(int n, String text, int ping, boolean update) {
        Preconditions.checkArgument(n >= 0 && n < ConfigManager.getTabSize(), "row out of range");

        if (slots[n] != null) {
            sentStuff.remove(slots[n]);
        }

        if (text != null) {
            if (text.length() > 16) {
                text = text.substring(0, 16);
            }
            try {
                text = attempt(text);
            } catch (Exception ex) {
                try {
                    text = text.substring(0, text.length() - 2);
                    text = attempt(text);
                } catch (Exception e) {
                    text = text.substring(0, text.length() - 2);
                    text = attempt(text);
                }
            }
            sentStuff.add(text);

            if (rowLim < n) {
                rowLim = n;
            }
        }

        slots[n] = text;
        slots_ping[n] = ping;
        if (update) {
            update();
        }
        return text;
    }

    private String attempt(String s) {
        if (!sentStuff.contains(s)) {
            return s;
        }
        if (s.length() <= MAX_LEN - 2) {
            for (char c : FILLER) {
                String attempt = s + Character.toString(ChatColor.COLOR_CHAR)
                        + c;
                try {
                    return attempt(attempt);
                } catch (IllegalArgumentException ignored) {

                }
            }
        }
        throw new IllegalArgumentException(
                "List already contains all variants of string '" + s + "'");
    }

    private void clear() {
        sentStuff.clear();
        for (int i = 0; i <= rowLim; i++) {
            slots[i] = null;
        }
    }

    @Override
    public void unload() {
        for (int i = 0; i < ConfigManager.getTabSize(); i++) {
            if (sent[i] != null) {
                BungeeTabListPlus.getInstance().getLegacyPacketAccess().removePlayer(
                        playerTablistHandler.getPlayer().unsafe(), sent[i]);
            }
            sent[i] = null;
        }
        sentStuff.clear();
    }

    private void update() {
        boolean remove = false;
        for (int i = 0; i <= rowLim; i++) {
            if (sent[i] != null) {
                if (remove
                        || !sent[i]
                        .equals((slots[i] == null ? String.valueOf(MyTabList.base(i)) : slots[i]))) {
                    String line = sent[i];
                    sent[i] = null;
                    BungeeTabListPlus.getInstance().getLegacyPacketAccess().
                            removePlayer(playerTablistHandler.getPlayer().unsafe(), line);
                    remove = true;
                }
            }
        }
        sentStuff.clear();
        for (int i = 0; i <= rowLim; i++) {
            String line = (slots[i] != null) ? slots[i] : String.valueOf(MyTabList.base(i));
            sent[i] = line;
            if (slots[i] != null) {
                sentStuff.add(line);
            }
            BungeeTabListPlus.getInstance().getLegacyPacketAccess().
                    createOrUpdatePlayer(playerTablistHandler.getPlayer().unsafe(), line,
                            slots_ping[i]);
        }
    }
}
