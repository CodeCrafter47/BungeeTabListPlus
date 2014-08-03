/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().
                getPlugin("BungeePerms");
        if (bp != null) {
            try {
                bpgroup = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(player.getName())).
                        getName();
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
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
        if (bgroup == null) {
            bgroup = "default";
        }

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return bpgroup != null ? bpgroup : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vgroup != null ? vgroup : "";
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return bgroup;
        }

        if (bpgroup != null) {
            return bpgroup;
        }
        if (vgroup != null) {
            return vgroup;
        }
        return bgroup;
    }

    public int comparePlayers(ProxiedPlayer p1, ProxiedPlayer p2) {
        // TODO Vault/Bukkit support

        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().
                getPlugin("BungeePerms");
        if (bp != null) {
            try {
                Group g1 = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(p1.getName()));
                Group g2 = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(p2.getName()));
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
                BungeeTabListPlus.getInstance().reportError(th);
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
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().
                getPlugin("BungeePerms");
        String bpprefix = null;
        if (bp != null) {
            try {
                bpprefix = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(player.getName())).
                        getPrefix();
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        String bprefix = plugin.getConfigManager().getMainConfig().prefixes.get(
                getMainGroup(player));

        String vprefix = plugin.getBridge().getPlayerInformation(player,
                "prefix");

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return bpprefix != null ? bpprefix : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vprefix != null ? vprefix : "";
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return bprefix;
        }

        if (bpprefix != null) {
            return bpprefix;
        }
        if (vprefix != null) {
            return vprefix;
        }
        if (bprefix != null) {
            return bprefix;
        }
        return "";
    }

    public String getDisplayPrefix(ProxiedPlayer player) {
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().
                getPlugin("BungeePerms");
        String display = null;
        if (bp != null) {
            try {
                display = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(player.getName())).
                        getDisplay();
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        if (display == null) {
            display = "";
        }

        return display;
    }

    public String getSuffix(ProxiedPlayer player) {
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().
                getPlugin("BungeePerms");
        String suffix = null;
        if (bp != null) {
            try {
                suffix = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(player.getName())).
                        getSuffix();
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        String vsuffix = plugin.getBridge().getPlayerInformation(player,
                "suffix");

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return suffix;
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vsuffix != null ? vsuffix : "";
        }

        if (suffix != null) {
            return suffix;
        }

        if (vsuffix != null) {
            return vsuffix;
        }

        return "";
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        try {
            Boolean b = Boolean.valueOf(plugin.getBridge().getPlayerInformation(
                    (ProxiedPlayer) sender, permission));
            if (b != null) {
                return b;
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }

        return false;
    }
}
