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
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.function.Function;

public class BukkitPlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("world").to(context -> {
            Optional<String> world = ((Player) context.getPlayer()).get(DataKeys.World);
            Optional<ServerInfo> server = context.getPlayer().getServer();
            if (world.isPresent() && server.isPresent()) {
                String key = server.get().getName() + ":" + world.get();
                String alias = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().worldAlias.get(key);
                if (alias != null) return alias;
                return world.get();
            }
            return "";
        });
        addBukkitBridgePlaceholder("team", DataKeys.Team);
        addBukkitBridgePlaceholder("balance", DataKeys.Vault_Balance, balance -> balance.map(b -> String.format("%1.2f", b)).orElse("-"));
        addBukkitBridgePlaceholder("balance2", DataKeys.Vault_Balance, balance -> balance.map(b -> {
            if (b > 2000000) {
                return String.format("%1.0fM", b / 1000000);
            } else if (b > 2000) {
                return String.format("%1.0fK", b / 1000);
            } else {
                return String.format("%1.0f", b);
            }
        }).orElse("-"));
        addBukkitBridgePlaceholder("factionName", DataKeys.Factions_FactionName);
        addBukkitBridgePlaceholder("MVWorldAlias", DataKeys.Multiverse_WorldAlias);
        addBukkitBridgePlaceholder("onlineFactionMembers", DataKeys.Factions_OnlineFactionMembers, num -> num.orElse(-1).toString());
        addBukkitBridgePlaceholder("factionsWhere", DataKeys.Factions_FactionsWhere);
        addBukkitBridgePlaceholder("factionPower", DataKeys.Factions_FactionPower);
        addBukkitBridgePlaceholder("factionsPlayerPower", DataKeys.Factions_PlayerPower);
        addBukkitBridgePlaceholder("factionMembers", DataKeys.Factions_FactionMembers);
        addBukkitBridgePlaceholder("factionRank", DataKeys.Factions_FactionsRank);
        addBukkitBridgePlaceholder("SimpleClans_ClanName", DataKeys.SimpleClans_ClanName);
        addBukkitBridgePlaceholder("SimpleClans_ClanMembers", DataKeys.SimpleClans_ClanMembers);
        addBukkitBridgePlaceholder("SimpleClans_OnlineClanMembers", DataKeys.SimpleClans_OnlineClanMembers);
        addBukkitBridgePlaceholder("SimpleClans_ClanTag", DataKeys.SimpleClans_ClanTag);
        addBukkitBridgePlaceholder("SimpleClans_ClanTagLabel", DataKeys.SimpleClans_ClanTagLabel);
        addBukkitBridgePlaceholder("SimpleClans_ClanColorTag", DataKeys.SimpleClans_ClanColorTag);
        addBukkitBridgePlaceholder("permissionsex_group", DataKeys.Vault_PermissionGroup);
        addBukkitBridgePlaceholder("permissionsex_prefix", DataKeys.Vault_Prefix);
        addBukkitBridgePlaceholder("permissionsex_suffix", DataKeys.Vault_Suffix);
        addBukkitBridgePlaceholder("vault_group", DataKeys.Vault_PermissionGroup);
        addBukkitBridgePlaceholder("vault_prefix", DataKeys.Vault_Prefix);
        addBukkitBridgePlaceholder("vault_suffix", DataKeys.Vault_Suffix);
        addBukkitBridgePlaceholder("vault_primary_group_prefix", DataKeys.Vault_PrimaryGroupPrefix);
        addBukkitBridgePlaceholder("vault_player_prefix", DataKeys.Vault_PlayerPrefix);
        addBukkitBridgePlaceholder("health", DataKeys.Health, health -> health.map(h -> String.format("%1.1f", h)).orElse("-"));
        addBukkitBridgePlaceholder("maxHealth", DataKeys.MaxHealth, health -> health.map(h -> String.format("%1.1f", h)).orElse("-"));
        addBukkitBridgePlaceholder("posX", DataKeys.PosX, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("posY", DataKeys.PosY, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("posZ", DataKeys.PosZ, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("XP", DataKeys.XP, xp -> xp.map(f -> String.format("%1.2f", f)).orElse(""));
        addBukkitBridgePlaceholder("totalXP", DataKeys.TotalXP);
        addBukkitBridgePlaceholder("level", DataKeys.Level, level -> level.orElse(-1).toString());
        addBukkitBridgePlaceholder("playerPoints", DataKeys.PlayerPoints_Points);
        addBukkitBridgeServerPlaceholder("currency", DataKeys.Vault_CurrencyNameSingular);
        addBukkitBridgeServerPlaceholder("currencyPl", DataKeys.Vault_CurrencyNamePlural);
        addBukkitBridgeServerPlaceholder("tps", DataKeys.TPS, tps -> tps.map(d -> String.format("%1.1f", d)).orElse(""));
        bind("tabName").to(context -> {
            Optional<String> tabName = ((Player) context.getPlayer()).get(DataKeys.PlayerListName);
            if (tabName.isPresent()) {
                return tabName.get();
            }
            if (context.getPlayer() instanceof ConnectedPlayer)
                return ((ConnectedPlayer) context.getPlayer()).getPlayer().getDisplayName();
            return context.getPlayer().getName();
        });
    }

    public <T> void addBukkitBridgePlaceholder(String name, DataKey<T> dataKey) {
        addBukkitBridgePlaceholder(name, dataKey, t -> t.map(Object::toString).orElse(""));
    }

    public <T> void addBukkitBridgePlaceholder(String name, DataKey<T> dataKey, Function<Optional<T>, String> toString) {
        bind(name).to(context -> toString.apply(((Player) context.getPlayer()).get(dataKey)));
    }

    public <T> void addBukkitBridgeServerPlaceholder(String name, DataKey<T> dataKey) {
        addBukkitBridgeServerPlaceholder(name, dataKey, t -> t.map(Object::toString).orElse(""));
    }

    public <T> void addBukkitBridgeServerPlaceholder(String name, DataKey<T> dataKey, Function<Optional<T>, String> toString) {
        bind(name).to(context -> context.getServer().map(s -> toString.apply(BungeeTabListPlus.getInstance().getBridge().get(s, dataKey))).orElse(""));
    }
}
