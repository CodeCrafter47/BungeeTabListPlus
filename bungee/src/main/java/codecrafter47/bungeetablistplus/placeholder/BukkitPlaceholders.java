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
import codecrafter47.bungeetablistplus.api.PlaceholderProvider;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import codecrafter47.data.Value;
import codecrafter47.data.Values;
import net.md_5.bungee.api.ProxyServer;

import java.util.Optional;
import java.util.function.Function;

public class BukkitPlaceholders extends PlaceholderProvider {
    @Override
    public void setup() {
        bind("world").to(context -> {
            Optional<String> world = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(context.getPlayer(), Values.Player.Bukkit.World);
            if (!world.isPresent()) {
                return "";
            }
            if (!context.getPlayer().getServer().isPresent()) return "";
            String key = context.getPlayer().getServer().get().getName() + ":" + world.get();
            String alias = BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().worldAlias.get(key);
            if (alias != null) return alias;
            return world.get();
        });
        addBukkitBridgePlaceholder("balance", Values.Player.Vault.Balance, balance -> balance.map(b -> String.format("%1.2f", b)).orElse("-"));
        addBukkitBridgePlaceholder("factionName", Values.Player.Factions.FactionName);
        addBukkitBridgePlaceholder("onlineFactionMembers", Values.Player.Factions.OnlineFactionMembers, num -> num.orElse(-1).toString());
        addBukkitBridgePlaceholder("factionsWhere", Values.Player.Factions.FactionsWhere);
        addBukkitBridgePlaceholder("factionPower", Values.Player.Factions.FactionPower);
        addBukkitBridgePlaceholder("factionsPlayerPower", Values.Player.Factions.PlayerPower);
        addBukkitBridgePlaceholder("factionMembers", Values.Player.Factions.FactionMembers);
        addBukkitBridgePlaceholder("factionRank", Values.Player.Factions.FactionsRank);
        addBukkitBridgePlaceholder("SimpleClans_ClanName", Values.Player.SimpleClans.ClanName);
        addBukkitBridgePlaceholder("SimpleClans_ClanMembers", Values.Player.SimpleClans.ClanMembers);
        addBukkitBridgePlaceholder("SimpleClans_OnlineClanMembers", Values.Player.SimpleClans.OnlineClanMembers);
        addBukkitBridgePlaceholder("SimpleClans_ClanTag", Values.Player.SimpleClans.ClanTag);
        addBukkitBridgePlaceholder("SimpleClans_ClanTagLabel", Values.Player.SimpleClans.ClanTagLabel);
        addBukkitBridgePlaceholder("SimpleClans_ClanColorTag", Values.Player.SimpleClans.ClanColorTag);
        addBukkitBridgePlaceholder("health", Values.Player.Minecraft.Health, health -> health.map(h -> String.format("%1.1f", h)).orElse("-"));
        addBukkitBridgePlaceholder("maxHealth", Values.Player.Minecraft.MaxHealth, health -> health.map(h -> String.format("%1.1f", h)).orElse("-"));
        addBukkitBridgePlaceholder("posX", Values.Player.Minecraft.PosX, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("posY", Values.Player.Minecraft.PosY, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("posZ", Values.Player.Minecraft.PosZ, pos -> pos.map(d -> String.format("%1.0f", d)).orElse(""));
        addBukkitBridgePlaceholder("XP", Values.Player.Minecraft.XP, xp -> xp.map(f -> String.format("%1.2f", f)).orElse(""));
        addBukkitBridgePlaceholder("totalXP", Values.Player.Minecraft.TotalXP);
        addBukkitBridgePlaceholder("level", Values.Player.Minecraft.Level, level -> level.orElse(-1).toString());
        addBukkitBridgePlaceholder("playerPoints", Values.Player.PlayerPoints.Points);
        addBukkitBridgeServerPlaceholder("currency", Values.Server.Vault.CurrencyNameSingular);
        addBukkitBridgeServerPlaceholder("currencyPl", Values.Server.Vault.CurrencyNamePlural);
        addBukkitBridgeServerPlaceholder("tps", Values.Server.TPS, tps -> tps.map(d -> String.format("%1.1f", d)).orElse(""));
        bind("tabName").to(context -> {
            Optional<String> tabName = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(context.getPlayer(), Values.Player.Bukkit.PlayerListName);
            if (tabName.isPresent()) {
                return tabName.get();
            }
            if (context.getPlayer() instanceof BungeePlayer)
                return ((BungeePlayer) context.getPlayer()).getPlayer().getDisplayName();
            return context.getPlayer().getName();
        });
    }

    public <T> void addBukkitBridgePlaceholder(String name, Value<T> value) {
        addBukkitBridgePlaceholder(name, value, t -> t.map(Object::toString).orElse(""));
    }

    public <T> void addBukkitBridgePlaceholder(String name, Value<T> value, Function<Optional<T>, String> toString) {
        bind(name).to(context -> toString.apply(BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(context.getPlayer(), value)));
    }

    public <T> void addBukkitBridgeServerPlaceholder(String name, Value<T> value) {
        addBukkitBridgeServerPlaceholder(name, value, t -> t.map(Object::toString).orElse(""));
    }

    public <T> void addBukkitBridgeServerPlaceholder(String name, Value<T> value, Function<Optional<T>, String> toString) {
        bind(name).to(context -> toString.apply(BungeeTabListPlus.getInstance().getBridge().getServerInformation(ProxyServer.getInstance().getServerInfo(context.getServer().get(0)), value)));
    }
}
