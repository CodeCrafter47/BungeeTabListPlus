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
import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.api.bungee.Skin;
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.tablisthandler.logic.TabListLogic;
import codecrafter47.bungeetablistplus.tablistproviders.LegacyTablistProvider;
import codecrafter47.bungeetablistplus.tablistproviders.TablistProvider;
import codecrafter47.bungeetablistplus.util.ColorParser;
import codecrafter47.bungeetablistplus.util.FastChat;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import lombok.AllArgsConstructor;
import lombok.Getter;
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
    @Getter
    protected final ProxiedPlayer player;
    @Getter
    protected TablistProvider tablistProvider = LegacyTablistProvider.INSTANCE;

    protected PlayerTablistHandler(ProxiedPlayer player) {
        this.player = player;
        this.tablistProvider.onActivated(this);
    }

    public void setTablistProvider(TablistProvider provider) {
        if (provider != this.tablistProvider) {
            this.tablistProvider.onDeactivated(this);
            this.tablistProvider = provider;
            provider.onActivated(this);
        }
    }

    public void onDisconnect() {
        this.tablistProvider.onDeactivated(this);
    }

    public abstract void setPassThrough(boolean passThrough);

    public abstract List<IPlayer> getServerTabList();

    public abstract void sendTabList(TabList tabList);

    public abstract void setResizePolicy(ResizePolicy resizePolicy);

    public static PlayerTablistHandler create(ProxiedPlayer player, TabListLogic handle) {
        return new Default(player, handle);
    }

    public static PlayerTablistHandler create(ProxiedPlayer player, LegacyTabList handle) {
        return new Legacy(player, handle);
    }

    public void runInEventLoop(Runnable runnable) {
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

    public abstract void setSize(int size);

    public abstract void setHeaderFooter(String header, String footer);

    public abstract void setSlot(int row, int column, Icon icon, String text, int ping);

    @AllArgsConstructor
    @Getter
    public enum ResizePolicy {
        DEFAULT_NO_SHRINK(true, false), DEFAULT(true, true), DYNAMIC(true, true);
        boolean mod20;
        boolean reduceSize;
    }

    private static class Default extends PlayerTablistHandler {
        private final TabListLogic handle;

        private Default(ProxiedPlayer player, TabListLogic handle) {
            super(player);
            this.handle = handle;
        }

        @Override
        public void setPassThrough(boolean passThrough) {
            handle.setPassThrough(passThrough);
        }

        @Override
        public List<IPlayer> getServerTabList() {
            return handle.getServerTabList();
        }

        @Override
        public void sendTabList(TabList tabList0) {
            TabList tabList = tabList0.flip();

            if (tabList.shouldShrink() && tabList.getUsedSlots() > tabList.flip().getUsedSlots()) {
                tabList = tabList.flip();
            }

            handle.setResizePolicy(tabList.shouldShrink() ? ResizePolicy.DYNAMIC : ResizePolicy.DEFAULT);
            int size = min(80, tabList.shouldShrink() ? tabList.getUsedSlots() : tabList.getSize());
            handle.setSize(size);
            handle.setPassThrough(false);

            int charLimit = BungeeTabListPlus.getInstance().getConfig().charLimit;

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

                    Skin skin1 = slot.getSkin();
                    Icon skin = new Icon(skin1.getOwner(), skin1.toProperty());
                    handle.setSlot(i, skin, FastChat.legacyTextToJson(text, '&'), slot.getPing());
                } else {
                    Skin skin1 = tabList.getDefaultSkin();
                    Icon skin = new Icon(skin1.getOwner(), skin1.toProperty());
                    handle.setSlot(i, skin, FastChat.legacyTextToJson("", '&'), tabList.getDefaultPing());
                }
            }

            handle.setHeaderFooter(FastChat.legacyTextToJson(tabList.getHeader(), '&')
                    , FastChat.legacyTextToJson(tabList.getFooter(), '&'));
        }

        @Override
        public void setResizePolicy(ResizePolicy resizePolicy) {
            handle.setResizePolicy(resizePolicy);
        }

        @Override
        public void setSize(int size) {
            handle.setSize(min(size, 80));
        }

        @Override
        public void setHeaderFooter(String header, String footer) {
            handle.setHeaderFooter(header, footer);
        }

        @Override
        public void setSlot(int row, int column, Icon icon, String text, int ping) {
            int columns = (handle.getSize() + 19) / 20;
            int rows = columns == 0 ? 0 : handle.getSize() / columns;
            int index = column * rows + row;
            if (index < handle.getSize()) {
                handle.setSlot(index, icon, text, ping);
            }
        }
    }

    private static class Legacy extends PlayerTablistHandler {
        private final LegacyTabList handle;

        private Legacy(ProxiedPlayer player, LegacyTabList handle) {
            super(player);
            this.handle = handle;
        }

        @Override
        public void setPassThrough(boolean passThrough) {
            handle.setPassTrough(passThrough);
        }

        @Override
        public List<IPlayer> getServerTabList() {
            return handle.getServerTabList();
        }

        @Override
        public void sendTabList(TabList tabList) {
            int size = min(handle.getMaxSize(), tabList.getUsedSlots());
            handle.setSize(size);
            handle.setPassTrough(false);

            int charLimit = BungeeTabListPlus.getInstance().getConfig().charLimit;

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
        }

        @Override
        public void setResizePolicy(ResizePolicy resizePolicy) {
            // do nothing/ we cannot change the size either way
        }

        @Override
        public void setSize(int size) {
            handle.setSize(min(size, handle.getMaxSize()));
        }

        @Override
        public void setHeaderFooter(String header, String footer) {
            // doesn't have this
        }

        @Override
        public void setSlot(int row, int column, Icon icon, String text, int ping) {
            int columns = (handle.getMaxSize() + 19) / 20;
            int index = row * columns + column;
            if (index < handle.getMaxSize()) {
                handle.setSlot(index, text, ping);
            }
        }
    }
}
