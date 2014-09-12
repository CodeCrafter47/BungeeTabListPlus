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

    private final String[] sendTextures = new String[ConfigManager.getTabSize()];

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
                updateSlot(i, text, line.ping, line.textures);
            }
        }

        // update header/footer
        if (tabList.getHeader() != null || tabList.getFooter() != null) {
            if (BungeeTabListPlus.isAbove995()) {
                player.setTabHeader(TextComponent.fromLegacyText(tabList.
                        getHeader()), TextComponent.fromLegacyText(tabList.
                                getFooter()));
            } else {
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

    private void updateSlot(int row, String text, int ping, String[] textures) {
        if (sendTextures[row] == null || (textures != null && !textures[0].
                equals(
                        sendTextures[row]))) {
            // update texture
            PlayerListItem pli = new PlayerListItem();
            pli.setAction(PlayerListItem.Action.ADD_PLAYER);
            Item item = new Item();
            UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                    ("OfflinePlayer:" + getSlotID(row)).getBytes(
                            Charsets.UTF_8));
            item.setUuid(offlineId);
            item.setPing(ping);
            item.setDisplayName(ComponentSerializer.toString(
                    TextComponent.
                    fromLegacyText(text)));

            item.setUsername(getSlotID(row));
            item.setGamemode(0);
            if (textures != null) {
                item.setProperties(new String[][]{{"textures", textures[0],
                    //  "o14FenB0emn6eLFBGY2rHUwT9J2BPfdeSBCCie7JM/pE9RSQgJyOHAuNnwnolYVd59N+5BbLQGS8FY5mTnSY2jPqYByP71gHMOqKY6MGR95Hcf+xkTyCs5cOITI1S2dd1asncMtqyRd71VPyCelYlnoBPYYCppltJRfO3lledtFnvXB7qa9dIxbgcxesfttU0YT5HyodLoGrr5NpC+oYVpxrYAPspXTm+kmjkxEJABQ72eDw2cQSt+SVh5zV3kLcOsLI/Hljzi/MUvyldGNM94joK7JvCrqt5hcjwaWudnIE/iUkeXtZOBwfVGh4JKBlmxmwJDOIhPeoibH7awnmvhj7JEVXJs905SLRFDdDtKJu7M3TDgvW4tWXzryV5WJKu8XsisuOMrNfcGUV6rsb9jAZDTQb1PE9oS5kuk5APLlp9s2LHHrAlGBFFKHJzGMBhzf+mmgYb0wP1v4ovan1tlMfC4kUS7jC3KgqpIHoB2bJ1WiiUMg3fucpfAjpx/DiPO/5IUZx56F8YbNLhYl8SJrs36SnqijgHFRG75HWzs7WTLR8I6dct7SMWyE4A2cIut1yX9vwX9Mfyx6qfoHA+8NeZvwpkq2UJqavCCwgNOhBxroY371GmeZ7gQdOsGw2Gqo412NlU94uF1UE5BhxMdbTPjasHZTE41zhrCypo10="/*,
                    textures[1]

                }});
            } else {
                item.setProperties(new String[][]{{"textures",
                    "eyJ0aW1lc3RhbXAiOjE0MTA0NzA2NjMxMDIsInByb2ZpbGVJZCI6ImY4NGM2YTc5MGE0ZTQ1ZTA4NzliY2Q0OWViZDRjNGUyIiwicHJvZmlsZU5hbWUiOiJIZXJvYnJpbmUiLCJpc1B1YmxpYyI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzk4YjdjYTNjN2QzMTRhNjFhYmVkOGZjMThkNzk3ZmMzMGI2ZWZjODQ0NTQyNWM0ZTI1MDk5N2U1MmU2Y2IifX19",
                    "fttLQKGbHG8n6mFUr3LaCG03GNzGk5dEmirzJ9l7DFFbaKm4vWW8ETeLEUBZynwejRUVFuC7Cr7WRvQvvni2fluH9zUw4sY/rceoFYXqJn3zyTSseOGkSgsdIiNIw21l4qpgAkvW8NvV+wWIC4gBv9qfQoNbDbHttOa+QVDpaPOWLcYUZG9yNdkjxmVbviPOytzwBk+hzF6U9Lx+gOjFYnNUgIO9efQ27itdONKlEbZBTM0PWuHJXTIwYACkSGNdRmyrTzgnJI1fH4v7DLi0kwd/dC1Kc8P+/GFdzzpGDhrUqWDPdBl2HguwaC1z+ks2U827+dsiq5fgTUvVpALLCCEIjcncpPH6QrkgHHfn0BHNqdWQ6PL+ugegT3561iKAX9CDRQbdCmu0rZ6K6dnb0u1cS4dcfGGMCYrOolhHzXSHxHPaFxpvNTO8uzTL0r9qu3FDsw7aBGTR1lrMOrdI53PItSNao/41CSOK5KRoFCDjgRrscppNf3sQtGNPh4L2vFkvfnPeOdGXeWjBYQpC/W2o6upuDgJEyV2vPEsqMA6LKZBOKadKcpXkCt0V9+cdkeZViCMtjFh8uI+qg34q3zd1/H1TmaSILZoVX6pSm+vOUk/C+gs+sTVdQGyeXObf558EYwGubT23Ri9aJyKOahib/OX/meGbsivh46xbNRU="
                }});

            }
            pli.setItems(new Item[]{item});
            getPlayer().unsafe().sendPacket(pli);
            sendTextures[row] = item.getProperties()[0][1];
        }

        // update ping
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

        // update name
        send[row] = text;
        slots_ping[row] = ping;
        PlayerListItem pli = new PlayerListItem();
        pli.setAction(PlayerListItem.Action.UPDATE_DISPLAY_NAME);
        Item item = new Item();
        UUID offlineId = java.util.UUID.nameUUIDFromBytes(
                ("OfflinePlayer:" + getSlotID(row)).getBytes(Charsets.UTF_8));
        item.setUuid(offlineId);
        item.setPing(ping);
        item.setDisplayName(ComponentSerializer.toString(TextComponent.
                fromLegacyText(text)));

        item.setUsername(getSlotID(row));
        item.setGamemode(0);
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
