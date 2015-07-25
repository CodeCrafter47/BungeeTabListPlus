/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.commands;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class OldSuperCommand extends Command {

    private final BungeeTabListPlus plugin;

    public OldSuperCommand(BungeeTabListPlus plugin) {
        super("BungeeTabListPlus", null, "btlp", "bungeetablistplus", "BTLP");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] arg1) {
        if (arg1.length == 1 && arg1[0].equalsIgnoreCase("reload")) {
            if (sender.hasPermission("bungeetablistplus.admin")) {
                sendReloadComplete(sender, BungeeTabListPlus.getInstance().reload());
            } else {
                sendNoPermission(sender);
            }
        } else {
            if (sender.hasPermission("bungeetablistplus.help") || sender.
                    hasPermission("bungeetablistplus.admin")) {
                sender.sendMessage(
                        ChatColor.DARK_PURPLE + "BungeeTabListPlus " + BungeeTabListPlus.
                                getInstance().getPlugin().getDescription().getVersion());
                sender.sendMessage(
                        ChatColor.DARK_PURPLE + "/BungeeTabListPlus reload");
            } else {
                sendNoPermission(sender);
            }
        }
    }

    private void sendNoPermission(CommandSender target) {
        String message = plugin.getConfigManager().getMessages().errorNoPermission;
        if (message == null || message.isEmpty()) {
            return;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }

    private void sendNeedsPlayer(CommandSender target) {
        String message = plugin.getConfigManager().getMessages().errorNeedsPlayer;
        if (message == null || message.isEmpty()) {
            return;
        }
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }

    private void sendReloadComplete(CommandSender target, boolean success) {
        if (success) {
            String message = plugin.getConfigManager().getMessages().successReloadComplete;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(ChatColor.RED + "Reloading BungeeTabListPlus failed");
        }
    }
}
