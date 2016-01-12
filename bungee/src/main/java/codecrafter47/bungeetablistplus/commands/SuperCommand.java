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
package codecrafter47.bungeetablistplus.commands;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ClickEvent.Action;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;

public class SuperCommand extends Command {

    private final BungeeTabListPlus plugin;

    public SuperCommand(BungeeTabListPlus plugin) {
        super("BungeeTabListPlus", null, "btlp", "bungeetablistplus", "BTLP");

        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] arg1) {
        plugin.runInMainThread(() -> {
            if (arg1.length == 1 && arg1[0].equalsIgnoreCase("reload")) {
                if (plugin.getPermissionManager().hasPermission(sender,
                        "bungeetablistplus.admin")) {
                    sendReloadComplete(sender, BungeeTabListPlus.getInstance().reload());
                } else {
                    sendNoPermission(sender);
                }
            } else if (arg1.length == 1 && arg1[0].equalsIgnoreCase("hide")) {
                if (plugin.getPermissionManager().hasPermission(sender,
                        "bungeetablistplus.hide")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer player = (ProxiedPlayer) sender;
                        if (BungeeTabListPlus.isHidden(plugin.getBungeePlayerProvider().wrapPlayer(player))) {
                            BungeeTabListPlus.unhidePlayer(player);
                            sendPlayerUnhide(player);
                        } else {
                            BungeeTabListPlus.hidePlayer(player);
                            sendPlayerHide(player);
                        }
                    } else {
                        sendNeedsPlayer(sender);
                    }
                } else {
                    sendNoPermission(sender);
                }
            } else if (arg1.length == 2 && arg1[0].equalsIgnoreCase("hide") && arg1[1].
                    equalsIgnoreCase("on")) {
                if (plugin.getPermissionManager().hasPermission(sender,
                        "bungeetablistplus.hide")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer player = (ProxiedPlayer) sender;
                        if (BungeeTabListPlus.isHidden(plugin.getBungeePlayerProvider().wrapPlayer(player))) {
                            sendAlreadyHidden(player);
                        } else {
                            BungeeTabListPlus.hidePlayer(player);
                            sendPlayerHide(player);
                        }
                    } else {
                        sendNeedsPlayer(sender);
                    }
                } else {
                    sendNoPermission(sender);
                }
            } else if (arg1.length == 2 && arg1[0].equalsIgnoreCase("hide") && arg1[1].
                    equalsIgnoreCase("off")) {
                if (plugin.getPermissionManager().hasPermission(sender,
                        "bungeetablistplus.hide")) {
                    if (sender instanceof ProxiedPlayer) {
                        ProxiedPlayer player = (ProxiedPlayer) sender;
                        if (BungeeTabListPlus.isHidden(plugin.getBungeePlayerProvider().wrapPlayer(player))) {
                            BungeeTabListPlus.unhidePlayer(player);
                            sendPlayerUnhide(player);
                        } else {
                            sendErrorNotHidden(player);
                        }
                    } else {
                        sendNeedsPlayer(sender);
                    }
                } else {
                    sendNoPermission(sender);
                }
            } else {
                if (plugin.getPermissionManager().hasPermission(sender,
                        "bungeetablistplus.help") || plugin.getPermissionManager().
                        hasPermission(sender, "bungeetablistplus.admin")) {
                    sendHelp(sender);
                } else {
                    sendNoPermission(sender);
                }
            }
        });
    }

    private void sendHelp(CommandSender target) {
        target.sendMessage(getPrefix().append(
                "==================================").
                color(ChatColor.DARK_BLUE).create());
        target.sendMessage(getPrefix().append("version " + plugin.getPlugin().
                getDescription().getVersion()).color(ChatColor.AQUA).create());
        if (plugin.isUpdateAvailable()) {
            target.sendMessage(getPrefix().append(
                    "A new version is available. Download ").color(
                    ChatColor.GOLD).append("here").color(
                    ChatColor.LIGHT_PURPLE).
                    underlined(true).event(new ClickEvent(Action.OPEN_URL,
                    "http://www.spigotmc.org/resources/bungeetablistplus.313/")).
                    create());
        }
        target.sendMessage(
                getPrefix().append("Commands:").color(ChatColor.AQUA).bold(true).
                        create());
        target.sendMessage(getPrefix().append("    ").append(
                "/BungeeTabListPlus help").color(ChatColor.AQUA).event(
                new ClickEvent(Action.SUGGEST_COMMAND,
                        "/BungeeTabListPlus help")).create());
        target.sendMessage(getPrefix().append("    ").append(
                "/BungeeTabListPlus hide [on|off]").color(ChatColor.AQUA).event(
                new ClickEvent(Action.SUGGEST_COMMAND,
                        "/BungeeTabListPlus hide ")).create());
        target.sendMessage(getPrefix().append("    ").append(
                "/BungeeTabListPlus reload").color(ChatColor.AQUA).event(
                new ClickEvent(Action.SUGGEST_COMMAND,
                        "/BungeeTabListPlus reload")).create());
        target.sendMessage(getPrefix().append(
                "==================================").
                color(ChatColor.DARK_BLUE).create());
    }

    private void sendNoPermission(CommandSender target) {
        if (plugin.getConfigManager().getMessages() != null) {
            String message = plugin.getConfigManager().getMessages().errorNoPermission;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(getPrefix().append(
                    "I'm pretty sorry, but the admin doesn't allow you to execute that command").
                    color(ChatColor.RED).create());
        }
    }

    private void sendNeedsPlayer(CommandSender target) {
        if (plugin.getConfigManager().getMessages() != null) {
            String message = plugin.getConfigManager().getMessages().errorNeedsPlayer;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(getPrefix().append(
                    "This command can only be executed by a player").color(
                    ChatColor.RED).create());
        }
    }

    private void sendReloadComplete(CommandSender target, boolean success) {
        if (success) {
            if (plugin.getConfigManager().getMessages() != null) {
                String message = plugin.getConfigManager().getMessages().successReloadComplete;
                if (message == null || message.isEmpty()) {
                    return;
                }
                message = ChatColor.translateAlternateColorCodes('&', message);
                target.sendMessage(message);
            } else {
                target.sendMessage(getPrefix().append("reload complete").color(
                        ChatColor.GREEN).create());
            }
        } else {
            target.sendMessage(getPrefix().append("reload failed").color(
                    ChatColor.RED).create());
        }
    }

    private void sendPlayerHide(CommandSender target) {
        if (plugin.getConfigManager().getMessages() != null) {
            String message = plugin.getConfigManager().getMessages().successPlayerHide;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(getPrefix().append(
                    "You have been hidden: Your name won't appear on the tablist").
                    color(ChatColor.GREEN).create());
        }
    }

    private void sendPlayerUnhide(CommandSender target) {
        if (plugin.getConfigManager().getMessages() != null) {
            String message = plugin.getConfigManager().getMessages().successPlayerUnhide;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(getPrefix().append(
                    "You're not hidden any longer").color(ChatColor.GREEN).
                    create());
        }
    }

    private void sendAlreadyHidden(CommandSender target) {
        if (plugin.getConfigManager().getMessages() != null) {
            String message = plugin.getConfigManager().getMessages().errorAlreadyHidden;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(getPrefix().append(
                    "Can't hide: You are already hidden").color(ChatColor.RED).
                    create());
        }
    }

    private void sendErrorNotHidden(CommandSender target) {
        if (plugin.getConfigManager().getMessages() != null) {
            String message = plugin.getConfigManager().getMessages().errorNotHidden;
            if (message == null || message.isEmpty()) {
                return;
            }
            message = ChatColor.translateAlternateColorCodes('&', message);
            target.sendMessage(message);
        } else {
            target.sendMessage(getPrefix().append(
                    "Can't unhide: You are not hidden").color(ChatColor.RED).
                    create());
        }
    }

    private ComponentBuilder getPrefix() {
        return new ComponentBuilder("[").color(ChatColor.BLUE).append(
                "BungeeTabListPlus").color(ChatColor.YELLOW).event(
                new ClickEvent(Action.OPEN_URL,
                        "http://www.spigotmc.org/resources/bungeetablistplus.313/")).
                append("] ").color(ChatColor.BLUE).event((ClickEvent) null);
    }
}
