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
import codecrafter47.bungeetablistplus.common.PermissionValues;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.bungeetablistplus.player.IPlayer;
import codecrafter47.data.Value;
import codecrafter47.data.Values;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Collection;
import java.util.Optional;
import java.util.logging.Level;

public class PermissionManager {

    private final BungeeTabListPlus plugin;

    public PermissionManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public String getMainGroup(IPlayer player) {
        String bpgroup = null;
        // BungeePerms
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
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
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occured while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        // Vault
        Optional<String> vgroup = plugin.getBridge().getPlayerInformation(player, Values.Player.Vault.PermissionGroup);

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
            return vgroup.isPresent() ? vgroup.get() : "";
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return bgroup;
        }

        if (bpgroup != null) {
            return bpgroup;
        }
        if (vgroup.isPresent()) {
            return vgroup.get();
        }
        return bgroup;
    }

    public int comparePlayers(IPlayer p1, IPlayer p2) {
        // TODO Vault/Bukkit support

        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                Group g1 = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(p1.getName()));
                Group g2 = bp.getPermissionsManager().getMainGroup(bp.
                        getPermissionsManager().getUser(p2.getName()));
                int r1 = g1.getRank();
                int r2 = g2.getRank();
                if (r1 > r2) {
                    return -1;
                }
                if (r2 > r1) {
                    return 1;
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
                return -1;
            }
            if (j > i) {
                return 1;
            }
        }
        return 0;
    }

    public String getPrefix(IPlayer player) {
        // BungeePerms
        String bpprefix = null;
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                User user = bp.getPermissionsManager().getUser(player.getName());
                if (user != null) {
                    bpprefix = bp.getPermissionsManager().getMainGroup(user).getPrefix();
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occured while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        String bprefix = plugin.getConfigManager().getMainConfig().prefixes.get(
                getMainGroup(player));

        Optional<String> vprefix = plugin.getBridge().getPlayerInformation(player, Values.Player.Vault.Prefix);

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return bpprefix != null ? bpprefix : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vprefix.isPresent() ? vprefix.get() : "";
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return bprefix != null ? bprefix : "";
        }

        if (bprefix != null) {
            return bprefix;
        }
        if (bpprefix != null) {
            return bpprefix;
        }
        if (vprefix.isPresent()) {
            return vprefix.get();
        }
        return "";
    }

    public String getDisplayPrefix(IPlayer player) {
        // BungeePerms
        String display = null;
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                User user = bp.getPermissionsManager().getUser(player.getName());
                if (user != null) {
                    display = bp.getPermissionsManager().getMainGroup(user).getDisplay();
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occured while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
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
        String suffix = null;
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                User user = bp.getPermissionsManager().getUser(player.getName());
                if (user != null) {
                    suffix = bp.getPermissionsManager().getMainGroup(user).getSuffix();
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occured while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        Optional<String> vsuffix = plugin.getBridge().getPlayerInformation(player, Values.Player.Vault.Suffix);

        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return suffix;
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return vsuffix.orElse("");
        }

        if (suffix != null) {
            return suffix;
        }

        if (vsuffix.isPresent()) {
            return vsuffix.get();
        }

        return "";
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        try {
            Value<Boolean> value = PermissionValues.getValueForPermission(permission);
            if (value != null) {
                Optional<Boolean> has = plugin.getBridge().getPlayerInformation(plugin.getBungeePlayerProvider().wrapPlayer((ProxiedPlayer) sender), value);
                if (has.isPresent()) return has.get();
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }

        return false;
    }
}
