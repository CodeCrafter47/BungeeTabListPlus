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
import codecrafter47.bungeetablistplus.player.BungeePlayer;
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
            String group = getMainGroupFromBungeePerms(player);
            return group != null ? group : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return plugin.getBridge().get(player, DataKeys.Vault_PermissionGroup).orElse("");
		} else if (mode.equalsIgnoreCase("BukkitPermissionsEx")) {
			return plugin.getBridge().get(player, DataKeys.PermissionsEx_PermissionGroup).orElse("");
        } else if (mode.equalsIgnoreCase("Bungee")) {
            return getMainGroupFromBungeeCord(player);
        } else {
            String group = getMainGroupFromBungeePerms(player);
            if (group != null) {
                return group;
            }
            Optional<String> optional = plugin.getBridge().get(player, DataKeys.PermissionsEx_PermissionGroup);
            if (optional.isPresent()) {
                return optional.get();
            } else {
                optional = plugin.getBridge().get(player, DataKeys.Vault_PermissionGroup);
                if (optional.isPresent()) {
                    return optional.get();
                } else {
                    return getMainGroupFromBungeeCord(player);
                }
            }
        }
    }

    private String getMainGroupFromBungeeCord(IPlayer player) {
        ProxiedPlayer proxiedPlayer = ProxyServer.getInstance().getPlayer(player.getName());
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

    private String getMainGroupFromBungeePerms(IPlayer player) {
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

    public int comparePlayers(IPlayer p1, IPlayer p2) {
        Plugin p = plugin.getProxy().getPluginManager().getPlugin("BungeePerms");
        if (p != null) {
            BungeePerms bp = BungeePerms.getInstance();
            try {
                PermissionsManager pm = bp.getPermissionsManager();
                if (pm != null) {
                    User u1 = pm.getUser(p1.getName());
                    User u2 = pm.getUser(p2.getName());
                    if (u1 != null && u2 != null) {
                        Group g1 = pm.getMainGroup(u1);
                        Group g2 = pm.getMainGroup(u2);
                        if (g1 != null && g2 != null) {
                            int r1 = g1.getRank();
                            int r2 = g2.getRank();
                            return r1 - r2;
                        }
                    }
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }

        {
            Optional<Integer> p1Rank = plugin.getBridge().get(p1, DataKeys.PermissionsEx_GroupRank);
            Optional<Integer> p2Rank = plugin.getBridge().get(p2, DataKeys.PermissionsEx_GroupRank);
            if (p1Rank.isPresent() || p2Rank.isPresent()) {
                return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
            }
        }

        {
            Optional<Integer> p1Rank = plugin.getBridge().get(p1, DataKeys.Vault_PermissionGroupRank);
            Optional<Integer> p2Rank = plugin.getBridge().get(p2, DataKeys.Vault_PermissionGroupRank);
            if (p1Rank.isPresent() || p2Rank.isPresent()) {
                return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
            }
        }

        // BungeeCord
        if (p1 instanceof BungeePlayer && p2 instanceof BungeePlayer) {
            int r1 = 0;
            for (String group : ((BungeePlayer) p1).getPlayer().getGroups()) {
                if (!group.equals("default")) {
                    r1 += 1;
                }
                if (group.equals("admin")) {
                    r1 += 2;
                }
            }
            int r2 = 0;
            for (String group : ((BungeePlayer) p2).getPlayer().getGroups()) {
                if (!group.equals("default")) {
                    r2 += 1;
                }
                if (group.equals("admin")) {
                    r2 += 2;
                }
            }
            return r1 - r2;
        }
        return 0;
    }

    public String getPrefix(TabListContext context) {
        IPlayer player = context.getPlayer();
        String mode = plugin.getConfigManager().getMainConfig().permissionSource;
        if (mode.equalsIgnoreCase("BungeePerms")) {
            String prefix = getPrefixFromBungeePerms(player);
            return prefix != null ? prefix : "";
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return plugin.getBridge().get(player, DataKeys.Vault_Prefix).orElse("");
		} else if (mode.equalsIgnoreCase("BukkitPermissionsEx")) {
			return plugin.getBridge().get(player, DataKeys.PermissionsEx_Prefix).orElse("");
        } else if (mode.equalsIgnoreCase("Bungee")) {
            String prefix = plugin.getConfigManager().getMainConfig().prefixes.get(getMainGroup(player));
            if (prefix != null) {
                prefix = BungeeTabListPlus.getInstance().getPlaceholderManager0().parseSlot(prefix).buildSlot(context).getText();
            }
            return prefix != null ? prefix : "";
        }

        String prefix = plugin.getConfigManager().getMainConfig().prefixes.get(getMainGroup(player));
        if (prefix != null) {
            prefix = BungeeTabListPlus.getInstance().getPlaceholderManager0().parseSlot(prefix).buildSlot(context).getText();
            return prefix;
        }
        prefix = getPrefixFromBungeePerms(player);
        if (prefix != null) {
            return prefix;
        }
        return plugin.getBridge().get(player, DataKeys.PermissionsEx_Prefix).orElseGet(() -> plugin.getBridge().get(player, DataKeys.Vault_Prefix).orElse(""));
    }

    private String getPrefixFromBungeePerms(IPlayer player) {
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

    public String getDisplayPrefix(IPlayer player) {
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
            String suffix = getSuffixFromBungeePerms(player);
            return suffix == null ? "" : suffix;
        } else if (mode.equalsIgnoreCase("Bukkit")) {
            return plugin.getBridge().get(player, DataKeys.Vault_Suffix).orElse("");
        } else if (mode.equalsIgnoreCase("BukkitPermissionsEx")) {
            return plugin.getBridge().get(player, DataKeys.PermissionsEx_Suffix).orElse("");
        }
        String suffix = getSuffixFromBungeePerms(player);
        if (suffix != null) {
            return suffix;
        }
        return plugin.getBridge().get(player, DataKeys.PermissionsEx_Suffix).orElseGet(() -> plugin.getBridge().get(player, DataKeys.Vault_Suffix).orElse(""));
    }

    private String getSuffixFromBungeePerms(IPlayer player) {
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
            Optional<Boolean> has = plugin.getBridge().get(plugin.getBungeePlayerProvider().wrapPlayer((ProxiedPlayer) sender), dataKey);
            if (has.isPresent()) return has.get();
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
