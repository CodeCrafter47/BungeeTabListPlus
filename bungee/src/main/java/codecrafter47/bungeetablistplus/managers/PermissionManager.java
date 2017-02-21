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

import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;

import java.util.Optional;

public class PermissionManager {

    private final BungeeTabListPlus plugin;

    public PermissionManager(BungeeTabListPlus plugin) {
        this.plugin = plugin;
    }

    public String getMainGroup(IPlayer player) {
        switch (plugin.getConfig().permissionSourceValue()) {
            case BUKKIT:
                return ((Player) player).getOpt(MinecraftData.Permissions_PermissionGroup).orElse("");
            case BUNGEE:
                return ((Player) player).getOpt(BungeeData.BungeeCord_PrimaryGroup).orElse("default");
            case BUNGEEPERMS:
                return ((Player) player).getOpt(BungeeData.BungeePerms_PrimaryGroup).orElse("");
            case LUCKPERMS:
                return ((Player) player).getOpt(BungeeData.LuckPerms_PrimaryGroup).orElse("");
            case AUTO:
            default:
                Optional<String> ret = ((Player) player).getOpt(BungeeData.BungeePerms_PrimaryGroup);
                if (ret.isPresent()) {
                    return ret.get();
                }

                ret = ((Player) player).getOpt(BungeeData.LuckPerms_PrimaryGroup);
                if (ret.isPresent()) {
                    return ret.get();
                }

                ret = ((Player) player).getOpt(MinecraftData.Permissions_PermissionGroup);
                if (ret.isPresent()) {
                    return ret.get();
                }

                return ((Player) player).getOpt(BungeeData.BungeeCord_PrimaryGroup).orElse("default");
        }
    }

    public int comparePlayers(IPlayer p1, IPlayer p2) {
        switch (plugin.getConfig().permissionSourceValue()) {
            case BUKKIT: {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(MinecraftData.Permissions_PermissionGroupWeight);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(MinecraftData.Permissions_PermissionGroupWeight);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
                p1Rank = ((Player) p1).getOpt(MinecraftData.Permissions_PermissionGroupRank);
                p2Rank = ((Player) p2).getOpt(MinecraftData.Permissions_PermissionGroupRank);
                return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
            }
            case BUNGEE: {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(BungeeData.BungeeCord_Rank);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(BungeeData.BungeeCord_Rank);
                return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
            }
            case BUNGEEPERMS: {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(BungeeData.BungeePerms_Rank);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(BungeeData.BungeePerms_Rank);
                return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
            }
            case LUCKPERMS: {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(BungeeData.LuckPerms_Weight);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(BungeeData.LuckPerms_Weight);
                return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
            }
            case AUTO:
            default: {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(BungeeData.BungeePerms_Rank);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(BungeeData.BungeePerms_Rank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(BungeeData.LuckPerms_Weight);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(BungeeData.LuckPerms_Weight);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(MinecraftData.Permissions_PermissionGroupWeight);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(MinecraftData.Permissions_PermissionGroupWeight);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(MinecraftData.Permissions_PermissionGroupRank);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(MinecraftData.Permissions_PermissionGroupRank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }

            {
                Optional<Integer> p1Rank = ((Player) p1).getOpt(BungeeData.BungeeCord_Rank);
                Optional<Integer> p2Rank = ((Player) p2).getOpt(BungeeData.BungeeCord_Rank);
                if (p1Rank.isPresent() || p2Rank.isPresent()) {
                    return p1Rank.orElse(Integer.MAX_VALUE) - p2Rank.orElse(Integer.MAX_VALUE);
                }
            }
            return 0;
        }
    }

    public String getPrefix(TabListContext context) {
        IPlayer player = context.getPlayer();
        switch (plugin.getConfig().permissionSourceValue()) {
            case BUKKIT:
                return ((Player) player).getOpt(MinecraftData.Permissions_Prefix).orElse("");
            case BUNGEE:
                return getConfigPrefix(context, player);
            case BUNGEEPERMS:
                return ((Player) player).getOpt(BungeeData.BungeePerms_Prefix).orElse("");
            case LUCKPERMS:
                return ((Player) player).getOpt(BungeeData.LuckPerms_Prefix).orElse("");
            case AUTO:
            default:
                String prefix = getConfigPrefix(context, player);
                if (!prefix.isEmpty()) {
                    return prefix;
                }

                Optional<String> ret = ((Player) player).getOpt(BungeeData.BungeePerms_Prefix);
                if (ret.isPresent()) {
                    return ret.get();
                }

                ret = ((Player) player).getOpt(BungeeData.LuckPerms_Prefix);
                if (ret.isPresent()) {
                    return ret.get();
                }

                return  ((Player) player).getOpt(MinecraftData.Permissions_Prefix).orElse("");
        }
    }

    public String getConfigPrefix(TabListContext context, IPlayer player) {
        String prefix = plugin.getConfig().prefixes.get(getMainGroup(player));
        if (prefix != null) {
            prefix = BungeeTabListPlus.getInstance().getPlaceholderManager0().parseSlot(prefix).buildSlot(context).getText();
        }
        return prefix != null ? prefix : "";
    }

    public String getSuffix(IPlayer player) {
        switch (plugin.getConfig().permissionSourceValue()) {
            case BUKKIT:
                return ((Player) player).getOpt(MinecraftData.Permissions_Suffix).orElse("");
            case BUNGEE:
            case BUNGEEPERMS:
                return ((Player) player).getOpt(BungeeData.BungeePerms_Suffix).orElse("");
            case LUCKPERMS:
                return ((Player) player).getOpt(BungeeData.LuckPerms_Suffix).orElse("");
            case AUTO:
            default:
                Optional<String> ret = ((Player) player).getOpt(BungeeData.BungeePerms_Suffix);
                if (ret.isPresent()) {
                    return ret.get();
                }

                ret = ((Player) player).getOpt(BungeeData.LuckPerms_Suffix);
                if (ret.isPresent()) {
                    return ret.get();
                }

                return ((Player) player).getOpt(MinecraftData.Permissions_Suffix).orElse("");
        }
    }

    public boolean hasPermission(CommandSender sender, String permission) {
        if (sender.hasPermission(permission)) {
            return true;
        }

        try {
            DataKey<Boolean> dataKey = MinecraftData.permission(permission);
            ConnectedPlayer player = plugin.getConnectedPlayerManager().getPlayerIfPresent((ProxiedPlayer) sender);
            if (player != null) {
                Optional<Boolean> has = player.getOpt(dataKey);
                if (has.isPresent()) return has.get();
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }

        return false;
    }
}
