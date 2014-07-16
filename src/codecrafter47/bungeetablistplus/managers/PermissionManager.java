package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PermissionManager {

    private final BungeeTabListPlus plugin;

    public PermissionManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public String getMainGroup(ProxiedPlayer player) {
        String bpgroup = null;
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (bp != null) {
            try {
                bpgroup = bp.getPermissionsManager().getMainGroup(bp.getPermissionsManager().getUser(player.getName())).getName();
            } catch (Throwable th) {
                // TODO do something here
            }
        }

        // Vault
        String vgroup = plugin.getBridge().getPlayerInformation(player, "group");

        // BungeeCord
        String bgroup = null;
        if (player.getGroups().size() == 1) {
            bgroup = player.getGroups().iterator().next();
        }
        for (String group : player.getGroups()) {
            if (!group.equals("default")) {
                bgroup = group;
                break;
            }
        }

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return bpgroup != null ? bpgroup : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vgroup != null ? vgroup : "";
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return bgroup;
        }

        if (vgroup != null) {
            return vgroup;
        }
        if (bpgroup != null) {
            return bpgroup;
        }
        return bgroup;
    }

    public int comparePlayers(ProxiedPlayer p1, ProxiedPlayer p2) {
        // TODO Vault/Bukkit support

        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (bp != null) {
            try {
                Group g1 = bp.getPermissionsManager().getMainGroup(bp.getPermissionsManager().getUser(p1.getName()));
                Group g2 = bp.getPermissionsManager().getMainGroup(bp.getPermissionsManager().getUser(p2.getName()));
                int r1 = g1.getRank();
                int r2 = g2.getRank();
                if (r1 > r2) {
                    return 1;
                }
                if (r2 > r1) {
                    return -1;
                }
                return 0;
            } catch (Throwable th) {
                // TODO do something here
            }
        }

        // BungeeCord
        int i = 0;
        for (String group : p1.getGroups()) {
            if (!group.equals("default")) {
                i = 1;
            }
        }
        int j = 0;
        for (String group : p2.getGroups()) {
            if (!group.equals("default")) {
                j = 1;
            }
        }
        if (i > j) {
            return 1;
        }
        if (j > i) {
            return -1;
        }
        return 0;
    }

    public String getPrefix(ProxiedPlayer player) {
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        String bpprefix = null;
        if (bp != null) {
            try {
                bpprefix = bp.getPermissionsManager().getMainGroup(bp.getPermissionsManager().getUser(player.getName())).getPrefix();
            } catch (Throwable th) {
                // TODO do something here
            }
        }

        String bprefix = plugin.getConfigManager().getMainConfig().prefixes.get(getMainGroup(player));

        String vprefix = plugin.getBridge().getPlayerInformation(player, "prefix");

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return bpprefix != null ? bpprefix : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vprefix != null ? vprefix : "";
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return bprefix;
        }

        if (vprefix != null) {
            return vprefix;
        }
        if (bpprefix != null) {
            return bpprefix;
        }
        return bprefix;
    }

    public String getDisplayPrefix(ProxiedPlayer player) {
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        String display = null;
        if (bp != null) {
            try {
                display = bp.getPermissionsManager().getMainGroup(bp.getPermissionsManager().getUser(player.getName())).getDisplay();
            } catch (Throwable th) {
                // TODO do something here
            }
        }

        if (display == null) {
            display = "";
        }

        return display;
    }

    public String getSuffix(ProxiedPlayer player) {
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        String suffix = null;
        if (bp != null) {
            try {
                suffix = bp.getPermissionsManager().getMainGroup(bp.getPermissionsManager().getUser(player.getName())).getSuffix();
            } catch (Throwable th) {
                // TODO do something here
            }
        }

        if (suffix == null) {
            suffix = "";
        }

        String vsuffix = plugin.getBridge().getPlayerInformation(player, "suffix");

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return suffix;
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vsuffix != null ? vsuffix : "";
        }

        if (vsuffix != null) {
            return vsuffix;
        }

        return suffix;
    }
    
    public boolean hasPermission(CommandSender sender, String permission){
        if(sender.hasPermission(permission))return true;
        
        try{
            Boolean b = Boolean.valueOf(plugin.getBridge().getPlayerInformation((ProxiedPlayer)sender, permission));
            if(b != null)return b;
        }catch(Throwable th){
            // TODO do something! This should never happen! But you cant know :-(
        }
        
        return false;
    }
}
