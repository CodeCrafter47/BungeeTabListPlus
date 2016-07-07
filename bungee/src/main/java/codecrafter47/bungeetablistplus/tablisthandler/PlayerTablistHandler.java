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
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.tablisthandler.logic.AbstractTabListLogic;
import codecrafter47.bungeetablistplus.tablisthandler.logic.TabListLogic;
import codecrafter47.bungeetablistplus.util.ColorParser;
import codecrafter47.bungeetablistplus.util.FastChat;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.netty.ChannelWrapper;

import java.util.List;
import java.util.logging.Level;

import static java.lang.Integer.min;

/**
 * @author Florian Stober
 */
public abstract class PlayerTablistHandler {

    public abstract void setPassThrough(boolean passThrough);

    public abstract List<IPlayer> getServerTabList();

    public abstract void sendTabList(TabList tabList);

    public static PlayerTablistHandler create(ProxiedPlayer player, TabListLogic handle) {
        return new Default(player, handle);
    }

    public static PlayerTablistHandler create(ProxiedPlayer player, LegacyTabList handle) {
        return new Legacy(player, handle);
    }

    static void runInEventLoop(ProxiedPlayer player, Runnable runnable) {
        ChannelWrapper ch = null;
        try {
            ch = ReflectionUtil.getChannelWrapper(player);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "failed to get ChannelWrapper for player", e);
        }
        if (ch != null) {
            ch.getHandle().eventLoop().submit(runnable);
        }
    }

    private static class Default extends PlayerTablistHandler {
        private final ProxiedPlayer player;
        private final TabListLogic handle;

        private Default(ProxiedPlayer player, TabListLogic handle) {
            this.player = player;
            this.handle = handle;
        }

        @Override
        public void setPassThrough(boolean passThrough) {
            runInEventLoop(player, () -> {
                handle.setPassTrough(passThrough);
            });
        }

        @Override
        public List<IPlayer> getServerTabList() {
            return handle.getServerTabList();
        }

        @Override
        public void sendTabList(TabList tabList0) {
            runInEventLoop(player, () -> {
                TabList tabList = tabList0.flip();

                if (tabList.shouldShrink() && tabList.getUsedSlots() > tabList.flip().getUsedSlots()) {
                    tabList = tabList.flip();
                }

                handle.setResizePolicy(tabList.shouldShrink() ? AbstractTabListLogic.ResizePolicy.DYNAMIC : AbstractTabListLogic.ResizePolicy.DEFAULT);
                int size = min(80, tabList.shouldShrink() ? tabList.getUsedSlots() : tabList.getSize());
                handle.setSize(size);
                handle.setPassTrough(false);

                int charLimit = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().charLimit;

                for (int i = 0; i < size; i++) {
                    Slot slot = tabList.getSlot(i);
                    if (slot != null) {
                        String text = slot.getText();

                        if (charLimit > 0) {
                            text = ChatColor.translateAlternateColorCodes('&', text);
                            text = ColorParser.substringIgnoreColors(text, charLimit);
                            for (int j = charLimit - ChatColor.stripColor(text).length(); j > 0; j--) {
                                text += ' ';
                            }
                        }

                        handle.setSlot(i, slot.getSkin(), FastChat.legacyTextToJson(text, '&'), slot.getPing());
                    } else {
                        handle.setSlot(i, tabList.getDefaultSkin(), FastChat.legacyTextToJson("", '&'), tabList.getDefaultPing());
                    }
                }

                handle.setHeaderFooter(FastChat.legacyTextToJson(tabList.getHeader(), '&')
                        , FastChat.legacyTextToJson(tabList.getFooter(), '&'));
            });
        }
    }

    private static class Legacy extends PlayerTablistHandler {
        private final ProxiedPlayer player;
        private final LegacyTabList handle;

        private Legacy(ProxiedPlayer player, LegacyTabList handle) {
            this.player = player;
            this.handle = handle;
        }

        @Override
        public void setPassThrough(boolean passThrough) {
            runInEventLoop(player, () -> {
                handle.setPassTrough(passThrough);
            });
        }

        @Override
        public List<IPlayer> getServerTabList() {
            return handle.getServerTabList();
        }

        @Override
        public void sendTabList(TabList tabList) {
            runInEventLoop(player, () -> {
                int size = min(handle.getMaxSize(), tabList.getUsedSlots());
                handle.setSize(size);
                handle.setPassTrough(false);

                int charLimit = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().charLimit;

                for (int i = 0; i < size; i++) {
                    Slot slot = tabList.getSlot(i);
                    if (slot != null) {
                        String text = slot.getText();

                        text = ChatColor.translateAlternateColorCodes('&', text);

                        if (charLimit > 0) {
                            text = ColorParser.substringIgnoreColors(text, charLimit);
                            for (int j = charLimit - ChatColor.stripColor(text).length(); j > 0; j--) {
                                text += ' ';
                            }
                        }

                        handle.setSlot(i, text, slot.getPing());
                    } else {
                        handle.setSlot(i, " ", tabList.getDefaultPing());
                    }
                }
            });
        }
    }
}
