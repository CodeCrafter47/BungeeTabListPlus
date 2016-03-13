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

package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.tab.TabList;

public class BungeePlugin extends Plugin {

    @Override
    public void onEnable() {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 52.0) {
            getLogger().severe("§cBungeeTabListPlus requires Java 8 or above. Please download and install it!");
            getLogger().severe("Disabling plugin!");
            return;
        }
        if (!getProxy().getPlayers().isEmpty() && getProxy().getPluginManager().getPlugin("BungeePluginManager") != null) {
            getLogger().severe("You cannot use BungeePluginManager to reload BungeeTabListPlus. Use /btlp reload instead.");
            getLogger().severe("Disabling plugin!");
            throw new RuntimeException("You cannot use BungeePluginManager to reload BungeeTabListPlus. Use /btlp reload instead.");
        }
        BungeeTabListPlus.getInstance(this).onEnable();
    }

    @Override
    public void onDisable() {
        if (BungeeCord.getInstance().isRunning && getProxy().getPluginManager().getPlugin("BungeePluginManager") != null) {
            getLogger().severe("You cannot use BungeePluginManager to reload BungeeTabListPlus. Use /btlp reload instead.");
            for (ProxiedPlayer proxiedPlayer : getProxy().getPlayers()) {
                try {
                    final TabList tablistHandler = ReflectionUtil.getTablistHandler(proxiedPlayer);
                    if (tablistHandler instanceof PlayerTablistHandler) {
                        ChannelWrapper channelWrapper = ReflectionUtil.getChannelWrapper(proxiedPlayer);
                        channelWrapper.getHandle().eventLoop().submit(new Runnable() {
                            @Override
                            public void run() {
                                ((PlayerTablistHandler) tablistHandler).exclude();
                            }
                        }).await();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            throw new RuntimeException("You cannot use BungeePluginManager to reload BungeeTabListPlus. Use /btlp reload instead.");
        }
    }
}
