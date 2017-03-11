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
package codecrafter47.bungeetablistplus.updater;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * @author florian
 */
public class UpdateNotifier implements Runnable {

    private final BungeeTabListPlus plugin;

    public UpdateNotifier(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (!plugin.getConfig().notifyAdminsIfUpdateAvailable) {
            return;
        }
        if (!plugin.isUpdateAvailable() && !plugin.isNewDevBuildAvailable()) {
            return;
        }
        for (ProxiedPlayer player : plugin.getProxy().getPlayers()) {
            if (player.hasPermission("bungeetablistplus.admin")) {
                if (plugin.isUpdateAvailable()) {
                    player.sendMessage(getPrefix().append(
                            "A new version is available. Download ").color(
                            ChatColor.GOLD).append("here").color(
                            ChatColor.LIGHT_PURPLE).
                            underlined(true).event(
                            new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    "http://www.spigotmc.org/resources/bungeetablistplus.313/")).
                            create());
                } else {
                    player.sendMessage(getPrefix().append(
                            "A new dev-build is available. Download ").color(
                            ChatColor.GOLD).append("here").color(
                            ChatColor.LIGHT_PURPLE).
                            underlined(true).event(
                            new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    "http://ci.codecrafter47.dyndns.eu/job/BungeeTabListPlus/")).
                            create());
                }
            }
        }
        if (plugin.isUpdateAvailable()) {
            plugin.getLogger().info("A new version of BungeeTabListPlus is available. Download from http://www.spigotmc.org/resources/bungeetablistplus.313/");
        } else {
            plugin.getLogger().info("A new dev-build is available at http://ci.codecrafter47.dyndns.eu/job/BungeeTabListPlus/");
        }
    }

    private ComponentBuilder getPrefix() {
        return new ComponentBuilder("[").color(ChatColor.BLUE).append(
                "BungeeTabListPlus").color(ChatColor.YELLOW).event(
                new ClickEvent(ClickEvent.Action.OPEN_URL,
                        "http://www.spigotmc.org/resources/bungeetablistplus.313/")).
                append("] ").color(ChatColor.BLUE).event((ClickEvent) null);
    }

}
