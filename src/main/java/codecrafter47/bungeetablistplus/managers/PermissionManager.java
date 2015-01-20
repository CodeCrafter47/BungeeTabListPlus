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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.bungeetablistplus.player.IPlayer;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;

public class PermissionManager {

    private final BungeeTabListPlus plugin;

    public PermissionManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public String getMainGroup(IPlayer player) {
        String bpgroup = null;
        // BungeePerms
        BungeePerms bp = (BungeePerms) plugin.getProxy().getPluginManager().
                getPlugin("BungeePerms");
        if (bp != null) {
            try {
                User user = bp.getPermissionsManager().getUser(player.getName());
                Group mainGroup = null;
                if (user != null) {
                    mainGroup = bp.getPermissionsManager().getMainGroup(user);
                }
                if (mainGroup == null) {
                    if (!bp.getPermissionsManager().getDefaultGroups().isEmpty()) {
                        mainGroup = bp.getPermissionsManager().getDefaultGroups().get(0);
                        for (int i = 1; i < bp.getPermissionsManager().getDefaultGroups().size(); ++i) {
                            if (bp.getPermissionsManager().getDefaultGroups().get(i).getWeight() < mainGroup.getWeight()) {
                                mainGroup = bp.getPermissionsManager().getDefaultGroups().get(i);
                            }
                        }
                    }
                }

                if (mainGroup != null) {
                    bpgroup = mainGroup.getName();
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        // Vault
        String vgroup = plugin.getBridge().getPlayerInformation(player, "group");

        // BungeeCord
        String bgroup = null;
        Collection<String> groups = plugin.getProxy().getConfigurationAdapter().getGroups(player.getName());
        if (groups.size() == 1) {
            bgroup = groups.iterator().next();
        }
        for (String group : groups) {
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

    public int comparePlayers(IPlayer p1, IPlayer p2) {
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
        if (p1 instanceof BungeePlayer && p2 instanceof BungeePlayer) {
            int i = 0;
            for (String group : ((BungeePlayer) p1).getPlayer().getGroups()) {
                if (!group.equals("default")) {
                    i = 1;
                }
            }
            int j = 0;
            for (String group : ((BungeePlayer) p2).getPlayer().getGroups()) {
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
        }
        return 0;
    }

    public String getPrefix(IPlayer player) {
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
            return bprefix != null ? bprefix : "";
        }

        if (bprefix != null) {
            return bprefix;
        }
        if (bpprefix != null) {
            return bpprefix;
        }
        if (vprefix != null) {
            return vprefix;
        }
        return "";
    }

    public String getDisplayPrefix(IPlayer player) {
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

    public String getSuffix(IPlayer player) {
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
                    plugin.getBungeePlayerProvider().wrapPlayer((ProxiedPlayer) sender), permission));
            if (b != null) {
                return b;
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }

        return false;
    }
}
