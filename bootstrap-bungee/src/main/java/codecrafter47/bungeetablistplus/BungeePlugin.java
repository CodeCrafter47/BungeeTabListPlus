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

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

public class BungeePlugin extends Plugin {

    @Override
    public void onLoad() {
        if (Float.parseFloat(System.getProperty("java.class.version")) < 52.0) {
            getLogger().severe("§cBungeeTabListPlus requires Java 8 or above. Please download and install it!");
            getLogger().severe("Disabling plugin!");
            return;
        }
        if (!getProxy().getPlayers().isEmpty()) {
            for (ProxiedPlayer proxiedPlayer : getProxy().getPlayers()) {
                proxiedPlayer.disconnect("Cannot reload BungeeTabListPlus while players are online.");
            }
        }
        BungeeTabListPlus.getInstance(this).onLoad();
        if (!getProxy().getPlayers().isEmpty()) {
            for (ProxiedPlayer proxiedPlayer : getProxy().getPlayers()) {
                proxiedPlayer.disconnect("Cannot reload BungeeTabListPlus while players are online.");
            }
        }
    }

    @Override
    public void onEnable() {
        BungeeTabListPlus.getInstance().onEnable();
    }

    @Override
    public void onDisable() {
        BungeeTabListPlus.getInstance().onDisable();
        if (BungeeCord.getInstance().isRunning) {
            getLogger().severe("You cannot use BungeePluginManager to reload BungeeTabListPlus. Use /btlp reload instead.");
            if (!getProxy().getPlayers().isEmpty()) {
                for (ProxiedPlayer proxiedPlayer : getProxy().getPlayers()) {
                    proxiedPlayer.disconnect("Cannot reload BungeeTabListPlus while players are online.");
                }
            }
        }
    }
}
