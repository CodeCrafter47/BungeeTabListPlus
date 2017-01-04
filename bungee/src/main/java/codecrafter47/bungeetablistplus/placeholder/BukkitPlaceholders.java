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

package codecrafter47.bungeetablistplus.placeholder;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.function.Function;

public class BukkitPlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("world").to(context -> {
            Optional<String> world = ((Player) context.getPlayer()).getOpt(MinecraftData.World);
            Optional<ServerInfo> server = context.getPlayer().getServer();
            if (world.isPresent() && server.isPresent()) {
                String key = server.get().getName() + ":" + world.get();
                String alias = BungeeTabListPlus.getInstance().getConfig().worldAlias.get(key);
                if (alias != null) return alias;
                return world.get();
            }
            return "";
        });
        addBukkitBridgePlaceholder("team", MinecraftData.Team);
        addBukkitBridgePlaceholder("balance", MinecraftData.Economy_Balance, balance -> balance.map(b -> String.format("%1.2f", b)).orElse("-"));
        addBukkitBridgePlaceholder("balance2", MinecraftData.Economy_Balance, balance -> balance.map(b -> {
            if (b > 2000000) {
                return String.format("%1.0fM", b / 1000000);
            } else if (b > 2000) {
                return String.format("%1.0fK", b / 1000);
            } else {
                return String.format("%1.0f", b);
            }
        }).orElse("-"));
        addBukkitBridgePlaceholder("factionName", BukkitData.Factions_FactionName);
        addBukkitBridgePlaceholder("MVWorldAlias", BukkitData.Multiverse_WorldAlias);
        addBukkitBridgePlaceholder("onlineFactionMembers", BukkitData.Factions_OnlineFactionMembers, num -> num.orElse(-1).toString());
        addBukkitBridgePlaceholder("factionsWhere", BukkitData.Factions_FactionsWhere);
        addBukkitBridgePlaceholder("factionPower", BukkitData.Factions_FactionPower);
        addBukkitBridgePlaceholder("factionsPlayerPower", BukkitData.Factions_PlayerPower);
        addBukkitBridgePlaceholder("factionMembers", BukkitData.Factions_FactionMembers);
        addBukkitBridgePlaceholder("factionRank", BukkitData.Factions_FactionsRank);
        addBukkitBridgePlaceholder("SimpleClans_ClanName", BukkitData.SimpleClans_ClanName);
        addBukkitBridgePlaceholder("SimpleClans_ClanMembers", BukkitData.SimpleClans_ClanMembers);
        addBukkitBridgePlaceholder("SimpleClans_OnlineClanMembers", BukkitData.SimpleClans_OnlineClanMembers);
        addBukkitBridgePlaceholder("SimpleClans_ClanTag", BukkitData.SimpleClans_ClanTag);
        addBukkitBridgePlaceholder("SimpleClans_ClanTagLabel", BukkitData.SimpleClans_ClanTagLabel);
        addBukkitBridgePlaceholder("SimpleClans_ClanColorTag", BukkitData.SimpleClans_ClanColorTag);
        addBukkitBridgePlaceholder("permissionsex_group", MinecraftData.Permissions_PermissionGroup);
        addBukkitBridgePlaceholder("permissionsex_prefix", MinecraftData.Permissions_Prefix);
        addBukkitBridgePlaceholder("permissionsex_suffix", MinecraftData.Permissions_Suffix);
        addBukkitBridgePlaceholder("vault_group", MinecraftData.Permissions_PermissionGroup);
        addBukkitBridgePlaceholder("vault_prefix", MinecraftData.Permissions_Prefix);
        addBukkitBridgePlaceholder("vault_suffix", MinecraftData.Permissions_Suffix);
        addBukkitBridgePlaceholder("vault_primary_group_prefix", MinecraftData.Permissions_PrimaryGroupPrefix);
        addBukkitBridgePlaceholder("vault_player_prefix", MinecraftData.Permissions_PlayerPrefix);
        addBukkitBridgePlaceholder("health", MinecraftData.Health, health -> health.map(h -> String.format("%1.1f", h)).orElse("-"));
        addBukkitBridgePlaceholder("maxHealth", MinecraftData.MaxHealth, health -> health.map(h -> String.format("%1.1f", h)).orElse("-"));
        addBukkitBridgePlaceholder("posX", MinecraftData.PosX, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("posY", MinecraftData.PosY, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("posZ", MinecraftData.PosZ, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("XP", MinecraftData.XP, xp -> xp.map(f -> String.format("%1.2f", f)).orElse(""));
        addBukkitBridgePlaceholder("totalXP", MinecraftData.TotalXP);
        addBukkitBridgePlaceholder("level", MinecraftData.Level, level -> level.orElse(-1).toString());
        addBukkitBridgePlaceholder("playerPoints", BukkitData.PlayerPoints_Points);
        addBukkitBridgeServerPlaceholder("currency", MinecraftData.Economy_CurrencyNameSingular);
        addBukkitBridgeServerPlaceholder("currencyPl", MinecraftData.Economy_CurrencyNamePlural);
        bind("tps").withArgs().to((context, arg) -> {
            if (arg != null) {
                ServerInfo serverInfo = ProxyServer.getInstance().getServerInfo(arg);
                if (serverInfo != null) {
                    DataHolder dataHolder = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(serverInfo.getName());
                    if (dataHolder != null) {
                        Double tps = dataHolder.get(MinecraftData.TPS);
                        return tps != null ? String.format("%1.1f", tps) : "";
                    }
                }
                return "";
            }
            return context.getServer().map(serverInfo -> {
                DataHolder dataHolder = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(serverInfo.getName());
                if (dataHolder != null) {
                    Double tps = dataHolder.get(MinecraftData.TPS);
                    return tps != null ? String.format("%1.1f", tps) : "";
                }
                return "";
            }).orElse("");
        });
        bind("tabName").to(context -> {
            Optional<String> tabName = ((Player) context.getPlayer()).getOpt(BukkitData.PlayerListName);
            if (tabName.isPresent()) {
                return tabName.get();
            }
            if (context.getPlayer() instanceof ConnectedPlayer)
                return ((ConnectedPlayer) context.getPlayer()).getPlayer().getDisplayName();
            return context.getPlayer().getName();
        });
    }

    private <T> void addBukkitBridgePlaceholder(String name, DataKey<T> dataKey) {
        addBukkitBridgePlaceholder(name, dataKey, t -> t.map(Object::toString).orElse(""));
    }

    private <T> void addBukkitBridgePlaceholder(String name, DataKey<T> dataKey, Function<Optional<T>, String> toString) {
        bind(name).to(context -> toString.apply(((Player) context.getPlayer()).getOpt(dataKey)));
    }

    private <T> void addBukkitBridgeServerPlaceholder(String name, DataKey<T> dataKey) {
        addBukkitBridgeServerPlaceholder(name, dataKey, t -> t.map(Object::toString).orElse(""));
    }

    private <T> void addBukkitBridgeServerPlaceholder(String name, DataKey<T> dataKey, Function<Optional<T>, String> toString) {
        bind(name).to(context -> context.getServer().map(serverInfo -> {
            DataHolder dataHolder = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(serverInfo.getName());
            return dataHolder != null ? toString.apply(Optional.ofNullable(dataHolder.get(dataKey))) : "";
        }).orElse(""));
    }
}
