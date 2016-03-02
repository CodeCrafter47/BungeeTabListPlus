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
package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;
import net.alpenblock.bungeeperms.BungeePerms;
import net.alpenblock.bungeeperms.Group;
import net.alpenblock.bungeeperms.PermissionsManager;
import net.alpenblock.bungeeperms.User;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
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
        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return ((Player) player).get(DataKeys.BungeePerms_PrimaryGroup).orElse("");
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return ((Player) player).get(DataKeys.Vault_PermissionGroup).orElse("");
        } else if (mode.equalsIgnoreCase("BukkitPermissionsEx")) {
            return ((Player) player).get(DataKeys.PermissionsEx_PermissionGroup).orElse("");
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return ((Player) player).get(DataKeys.BungeeCord_PrimaryGroup).orElse("default");
        } else {
            Optional<String> group = ((Player) player).get(DataKeys.BungeePerms_PrimaryGroup);
            if (group.isPresent()) {
                return group.get();
            }
            Optional<String> optional = ((Player) player).get(DataKeys.PermissionsEx_PermissionGroup);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                optional = ((Player) player).get(DataKeys.Vault_PermissionGroup);
                if (optional.isPresent()) {
                    return optional.get();
                } else {
                    return ((Player) player).get(DataKeys.BungeeCord_PrimaryGroup).orElse("default");
                }
            }
        }
    }

    String getMainGroupFromBungeeCord(ProxiedPlayer proxiedPlayer) {
        if (proxiedPlayer != null) {
            Collection<String> groups = proxiedPlayer.getGroups();
            if (groups.size() == 1) {
                return groups.iterator().next();
            }
            for (String group : groups) {
                if (!group.equals("default")) {
                    return group;
                }
            }
        }
        return "default";
    }

    String getMainGroupFromBungeePerms(ProxiedPlayer player) {
        Plugin p = ProxyServer.getInstance().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                PermissionsManager pm = bp.getPermissionsManager();
                if (pm != null) {
                    User user = pm.getUser(player.getName());
                    Group mainGroup = null;
                    if (user != null) {
                        mainGroup = pm.getMainGroup(user);
                    }
                    if (mainGroup == null) {
                        if (!pm.getDefaultGroups().isEmpty()) {
                            mainGroup = pm.getDefaultGroups().get(0);
                            for (int i = 1; i < pm.getDefaultGroups().size(); ++i) {
                                if (pm.getDefaultGroups().get(i).getWeight() < mainGroup.getWeight()) {
                                    mainGroup = pm.getDefaultGroups().get(i);
                                }
                            }
                        }
                    }

                    if (mainGroup != null) {
                        return mainGroup.getName();
                    }
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occurred while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
        return null;
    }

    Integer getBungeePermsRank(ProxiedPlayer player) {
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                PermissionsManager pm = bp.getPermissionsManager();
                if (pm != null) {
                    User user = pm.getUser(player.getName());
                    if (user != null) {
                        Group mainGroup = pm.getMainGroup(user);
                        if (mainGroup != null) {
                            return mainGroup.getRank();
                        }
                    }
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
        return null;
    }

    public int comparePlayers(IPlayer p1, IPlayer p2) {
        String permissionSource = plugin.getConfigManager().getMainConfig().permissionSource;
        if (permissionSource.equalsIgnoreCase("BungeePerms")) {
            Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.BungeePerms_Rank);
            Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.BungeePerms_Rank);
            return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
        } else if (permissionSource.equalsIgnoreCase("Bukkit")) {
            Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.Vault_PermissionGroupRank);
            Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.Vault_PermissionGroupRank);
            return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
        } else if (permissionSource.equalsIgnoreCase("BukkitPermissionsEx")) {
            Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.PermissionsEx_GroupRank);
            Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.PermissionsEx_GroupRank);
            return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
        } else if (permissionSource.equalsIgnoreCase("Bungee")) {
            Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.BungeeCord_Rank);
            Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.BungeeCord_Rank);
            return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
        } else {
            {
                Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.BungeePerms_Rank);
                Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.BungeePerms_Rank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            {
                Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.PermissionsEx_GroupRank);
                Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.PermissionsEx_GroupRank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            {
                Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.Vault_PermissionGroupRank);
                Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.Vault_PermissionGroupRank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            // BungeeCord
            {
                Optional<Integer> p1Rank = ((Player) p1).get(DataKeys.BungeeCord_Rank);
                Optional<Integer> p2Rank = ((Player) p2).get(DataKeys.BungeeCord_Rank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }
            return 0;
        }
    }

    int getBungeeCordRank(ProxiedPlayer player) {
        int rank = 0;
        for (String group : player.getGroups()) {
            if (!group.equals("default")) {
                rank += 1;
            }
            if (group.equals("admin")) {
                rank += 2;
            }
        }
        return Integer.MAX_VALUE - rank;
    }

    public String getPrefix(TabListContext context) {
        IPlayer player = context.getPlayer();
        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return ((Player) player).get(DataKeys.BungeePerms_Prefix).orElse("");
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return ((Player) player).get(DataKeys.Vault_Prefix).orElse("");
        } else if (mode.equalsIgnoreCase("BukkitPermissionsEx")) {
            return ((Player) player).get(DataKeys.PermissionsEx_Prefix).orElse("");
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return getConfigPrefix(context, player);
        }

        String prefix = getConfigPrefix(context, player);
        if (!prefix.isEmpty()) {
            return prefix;
        }
        return ((Player) player).get(DataKeys.BungeePerms_Prefix).orElseGet(() -> ((Player) player).get(DataKeys.PermissionsEx_Prefix).orElseGet(() -> ((Player) player).get(DataKeys.Vault_Prefix).orElse("")));
    }

    public String getConfigPrefix(TabListContext context, IPlayer player) {
        String prefix = plugin.getConfigManager().getMainConfig().prefixes.get(getMainGroup(player));
        if (prefix != null) {
            prefix = BungeeTabListPlus.getInstance().getPlaceholderManager0().parseSlot(prefix).buildSlot(context).getText();
        }
        return prefix != null ? prefix : "";
    }

    String getPrefixFromBungeePerms(ProxiedPlayer player) {
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                PermissionsManager pm = bp.getPermissionsManager();
                if (pm != null) {
                    User user = pm.getUser(player.getName());
                    if (user != null) {
                        if (isBungeePerms3()) {
                            return user.buildPrefix();
                        } else {
                            Group mainGroup = pm.getMainGroup(user);
                            if (mainGroup != null) {
                                return mainGroup.getPrefix();
                            }
                        }
                    }
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occurred while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
        return null;
    }

    String getDisplayPrefix(ProxiedPlayer player) {
        // BungeePerms
        String display = null;
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                PermissionsManager pm = bp.getPermissionsManager();
                if (pm != null) {
                    User user = pm.getUser(player.getName());
                    if (user != null) {
                        if (isBungeePerms3()) {
                            display = user.getDisplay();
                            if (display == null || display.isEmpty()) {
                                Group group = pm.getMainGroup(user);
                                if (group != null) {
                                    display = group.getDisplay();
                                }
                            }
                        } else {
                            Group group = pm.getMainGroup(user);
                            if (group != null) {
                                display = group.getDisplay();
                            }
                        }
                    }
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occurred while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
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
        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            return ((Player) player).get(DataKeys.BungeePerms_Suffix).orElse("");
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return ((Player) player).get(DataKeys.Vault_Suffix).orElse("");
        } else if (mode.equalsIgnoreCase("BukkitPermissionsEx")) {
            return ((Player) player).get(DataKeys.PermissionsEx_Suffix).orElse("");
        }
        return ((Player) player).get(DataKeys.BungeePerms_Suffix).orElseGet(() -> ((Player) player).get(DataKeys.PermissionsEx_Suffix).orElseGet(() -> ((Player) player).get(DataKeys.Vault_Suffix).orElse("")));
    }

    String getSuffixFromBungeePerms(ProxiedPlayer player) {
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                PermissionsManager pm = bp.getPermissionsManager();
                if (pm != null) {
                    User user = pm.getUser(player.getName());
                    if (user != null) {
                        if (isBungeePerms3()) {
                            return user.buildSuffix();
                        } else {
                            Group group = pm.getMainGroup(user);
                            if (group != null) {
                                return group.getSuffix();
                            }
                        }
                    }
                }
            } catch (NullPointerException ex) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "An error occurred while querying data from BungeePerms. Make sure you have configured BungeePerms to use it's uuidPlayerDB.", ex);
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
        return null;
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        try {
            DataKey<Boolean> dataKey = DataKeys.permission(permission);
            ConnectedPlayer player = plugin.getConnectedPlayerManager().getPlayerIfPresent((ProxiedPlayer) sender);
            if (player != null) {
                Optional<Boolean> has = player.get(dataKey);
                if (has.isPresent()) return has.get();
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }

        return false;
    }

    private boolean isBungeePerms3() {
        return isClassPresent("net.alpenblock.bungeeperms.platform.bungee.BungeePlugin");
    }

    private boolean isClassPresent(String name) {
        try {
            Class.forName(name);
            return true;
        } catch (ClassNotFoundException ignored) {
            return false;
        }
    }
}
