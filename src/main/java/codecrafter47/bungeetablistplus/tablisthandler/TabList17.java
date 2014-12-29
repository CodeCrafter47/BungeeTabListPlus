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
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.util.ColorParser;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.Connection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.protocol.packet.PlayerListItem;
import net.md_5.bungee.protocol.packet.Team;

/**
 * @author Florian Stober
 */
public class TabList17 extends CustomTabList18 implements
        IMyTabListHandler {

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
        return new StringBuilder().append(ChatColor.MAGIC).append(alloc).append(
                ChatColor.RESET).toString();
    }

    private final int[] slots_ping = new int[ConfigManager.getTabSize()];

    private int sendSlots = 0;

    private final String send[] = new String[ConfigManager.getTabSize()];

    public TabList17(ProxiedPlayer player) {
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

        ITabListProvider tlp = BungeeTabListPlus.getInstance().
                getTabListManager().getTabListForPlayer(super.getPlayer());
        if (tlp == null) {
            exclude();
            unload();
            return;
        }
        TabList tabList = tlp.getTabList(super.getPlayer());

        resize(tabList.getUsedSlots());

        int charLimit = BungeeTabListPlus.getInstance().getConfigManager().
                getMainConfig().charLimit;

        for (int i = 0; i < tabList.getUsedSlots(); i++) {
            Slot line = tabList.getSlot(i);
            if (line == null) {
                line = new Slot("", tabList.getDefaultPing());
            }
            String text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replacePlayerVariables(getPlayer(), line.text, BungeeTabListPlus.getInstance().getBungeePlayerProvider().wrapPlayer(super.getPlayer()));
            text = BungeeTabListPlus.getInstance().getVariablesManager().
                    replaceVariables(getPlayer(), text);
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
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.ADD_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(getSlotID(i));
        item.setPing(0);
        pli.setItems(new PlayerListItem.Item[]{item});
        getPlayer().unsafe().sendPacket(pli);

    }

    private void removeSlot(int i) {
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.REMOVE_PLAYER);
        PlayerListItem.Item item = new PlayerListItem.Item();
        item.setDisplayName(getSlotID(i));
        item.setPing(0);
        pli.setItems(new PlayerListItem.Item[]{item});
        getPlayer().unsafe().sendPacket(pli);
        Team t = new Team();
        t.setName("TAB" + getSlotID(i));
        t.setMode((byte) 1);
        getPlayer().unsafe().sendPacket(t);

    }

    private void updateSlot(int row, String text, int ping) {
        if (ping != slots_ping[row]) {
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.ADD_PLAYER);
            PlayerListItem.Item item = new PlayerListItem.Item();
            item.setDisplayName(getSlotID(row));
            item.setPing(ping);
            pli.setItems(new PlayerListItem.Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
        }
        send[row] = text;
        slots_ping[row] = ping;
        String split[] = splitText(text);
        updateTeam(getPlayer().unsafe(), getSlotID(row), split[0], /*split[1]*/ "", split[1]);

    }

    void updateTeam(Connection.Unsafe connection, String player,
                    String prefix, String displayname, String suffix) {
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 2);
        t.setPrefix(prefix);
        t.setDisplayName(displayname);
        t.setSuffix(suffix);
        connection.sendPacket(t);
    }

    private void createSlot(int row) {
        createTeam(getPlayer().unsafe(), getSlotID(row));
        send[row] = null;
        slots_ping[row] = 0;
    }

    void createTeam(Connection.Unsafe connection, String player) {
        Team t = new Team();
        t.setName("TAB" + player);
        t.setMode((byte) 0);
        t.setPrefix(" ");
        t.setDisplayName(" ");
        t.setSuffix(" ");
        t.setPlayers(new String[]{player});
        // TODO FIXME
        //t.setNameTagVisibility("never");
        connection.sendPacket(t);
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

    void unload() {
        resize(0);
    }
}