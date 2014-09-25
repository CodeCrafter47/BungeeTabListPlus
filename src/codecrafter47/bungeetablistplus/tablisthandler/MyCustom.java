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
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.HashSet;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class MyCustom extends CustomTabListHandler {

    private static final char[] FILLER = new char[]{'0', '1', '2', '2', '4',
        '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final int MAX_LEN = 16;

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
    /* ======================================================================== */
    private final Collection<String> sentStuff = new HashSet<>();
    /* ======================================================================== */
    private String[] sent = new String[ConfigManager.getTabSize()];
    private String[] slots = new String[ConfigManager.getTabSize()];
    private int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int rowLim;

    public MyCustom(ProxiedPlayer player) {
        this.init(player);
    }

    public synchronized String setSlot(int n, Slot line) {
        return setSlot(n, line, true);
    }

    public synchronized String setSlot(int n, Slot line, boolean update) {
        Preconditions.checkArgument(n >= 0 && n < ConfigManager.getTabSize(),
                "row out of range");

        String text = line.text;

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
        slots_ping[n] = line.ping;
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
                } catch (IllegalArgumentException ex) {

                }
            }
        }
        throw new IllegalArgumentException(
                "List already contains all variants of string '" + s + "'");
    }

    public synchronized void clear() {
        sentStuff.clear();
        for (int i = 0; i <= rowLim; i++) {
            slots[i] = null;
        }
    }

    public synchronized void unload() {
        for (int i = 0; i < ConfigManager.getTabSize(); i++) {
            if (sent[i] != null) {
                BungeeTabListPlus.getInstance().getPacketManager().removePlayer(
                        getPlayer().unsafe(), sent[i]);
            }
            sent[i] = null;
        }
        sentStuff.clear();
    }

    public synchronized void update() {
        boolean remove = false;
        for (int i = 0; i <= rowLim; i++) {
            if (sent[i] != null) {
                if (remove
                        || !sent[i]
                        .equals((slots[i] == null ? new StringBuilder()
                                .append(base(i)).toString() : slots[i]))) {
                    String line = sent[i];
                    sent[i] = null;
                    /*
                     getPlayer().unsafe().sendPacket(
                     new PlayerListItem(line, false, (short) 9999));*/
                    BungeeTabListPlus.getInstance().getPacketManager().
                            removePlayer(getPlayer().unsafe(), line);
                    remove = true;
                }
            }
        }
        sentStuff.clear();
        for (int i = 0; i <= rowLim; i++) {
            String line = (slots[i] != null) ? slots[i] : new StringBuilder()
                    .append(base(i)).toString();
            sent[i] = line;
            if (slots[i] != null) {
                sentStuff.add(line);
            }
            /*
             getPlayer().unsafe().sendPacket(
             new PlayerListItem(line, true, slots_ping[i]));*/
            BungeeTabListPlus.getInstance().getPacketManager().
                    createOrUpdatePlayer(getPlayer().unsafe(), line,
                            slots_ping[i]);
        }
    }
}
