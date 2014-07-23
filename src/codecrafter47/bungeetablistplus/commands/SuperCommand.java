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
        if (arg1.length == 1 && arg1[0].equalsIgnoreCase("reload")) {
            if (plugin.getPermissionManager().hasPermission(sender,
                    "bungeetablistplus.admin")) {
                BungeeTabListPlus.getInstance().reload();
                sendReloadComplete(sender);
            } else {
                sendNoPermission(sender);
            }
        } else if (arg1.length == 1 && arg1[0].equalsIgnoreCase("hide")) {
            if (plugin.getPermissionManager().hasPermission(sender,
                    "bungeetablistplus.hide")) {
                if (sender instanceof ProxiedPlayer) {
                    ProxiedPlayer player = (ProxiedPlayer) sender;
                    if (BungeeTabListPlus.isHidden(player)) {
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
                    if (BungeeTabListPlus.isHidden(player)) {
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
                    if (BungeeTabListPlus.isHidden(player)) {
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
    }

    private void sendHelp(CommandSender target) {
        target.sendMessage(getPrefix().append(
                "==================================").
                color(ChatColor.DARK_BLUE).create());
        target.sendMessage(getPrefix().append("version " + plugin.
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
        for (String s : plugin.getProxy().getServers().keySet()) {
            if (!plugin.getBridge().isUpToDate(s)) {
                target.sendMessage(getPrefix().append(
                        "BukkitBridge on server '" + s + "' is outdated. Please update!").
                        color(ChatColor.RED).create());
            }
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

    private void sendReloadComplete(CommandSender target) {
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
