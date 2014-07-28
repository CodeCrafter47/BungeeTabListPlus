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
                BungeeTabListPlus.getInstance().reload();
                sendReloadComplete(sender);
            } else {
                sendNoPermission(sender);
            }
        } else if (arg1.length == 1 && arg1[0].equalsIgnoreCase("hide")) {
            if (sender.hasPermission("bungeetablistplus.hide")) {
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
        } else if (arg1.length == 2 && arg1[0].equalsIgnoreCase("hide") && arg1[1].equalsIgnoreCase("on")) {
            if (sender.hasPermission("bungeetablistplus.hide")) {
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
        } else if (arg1.length == 2 && arg1[0].equalsIgnoreCase("hide") && arg1[1].equalsIgnoreCase("off")) {
            if (sender.hasPermission("bungeetablistplus.hide")) {
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
            if (sender.hasPermission("bungeetablistplus.help") || sender.hasPermission("bungeetablistplus.admin")) {
                sender.sendMessage(ChatColor.DARK_PURPLE + "BungeeTabListPlus " + BungeeTabListPlus.getInstance().getDescription().getVersion());
                sender.sendMessage(ChatColor.DARK_PURPLE + "/BungeeTabListPlus reload");
                sender.sendMessage(ChatColor.DARK_PURPLE + "/BungeeTabListPlus hide [on/off]");
            }else{
                sendNoPermission(sender);
            }
        }
    }
    
    private void sendNoPermission(CommandSender target){
        String message = plugin.getConfigManager().getMessages().errorNoPermission;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
    
    private void sendNeedsPlayer(CommandSender target){
        String message = plugin.getConfigManager().getMessages().errorNeedsPlayer;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
    
    private void sendReloadComplete(CommandSender target){
        String message = plugin.getConfigManager().getMessages().successReloadComplete;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
    
    private void sendPlayerHide(CommandSender target){
        String message = plugin.getConfigManager().getMessages().successPlayerHide;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
    
    private void sendPlayerUnhide(CommandSender target){
        String message = plugin.getConfigManager().getMessages().successPlayerUnhide;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
    
    private void sendAlreadyHidden(CommandSender target){
        String message = plugin.getConfigManager().getMessages().errorAlreadyHidden;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
    
    private void sendErrorNotHidden(CommandSender target){
        String message = plugin.getConfigManager().getMessages().errorNotHidden;
        if(message == null || message.isEmpty())return;
        message = ChatColor.translateAlternateColorCodes('&', message);
        target.sendMessage(message);
    }
}
