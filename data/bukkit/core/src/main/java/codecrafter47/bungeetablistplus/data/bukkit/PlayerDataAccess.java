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

package codecrafter47.bungeetablistplus.data.bukkit;

import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.data.PermissionDataKey;
import codecrafter47.bungeetablistplus.data.essentials.EssentialsIsVanishedProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionMembersProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionNameProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionOnlineMembersProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionPowerProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionRankProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionWhereProvider;
import codecrafter47.bungeetablistplus.data.factions.FactionsPlayerPowerProvider;
import codecrafter47.bungeetablistplus.data.factionsuuid.FactionPlayerPowerProvider;
import codecrafter47.bungeetablistplus.data.multiverse.MultiverseWorldAliasProvider;
import codecrafter47.bungeetablistplus.data.permissionsex.PermissionsExGroupProvider;
import codecrafter47.bungeetablistplus.data.permissionsex.PermissionsExGroupRankProvider;
import codecrafter47.bungeetablistplus.data.permissionsex.PermissionsExPrefixProvider;
import codecrafter47.bungeetablistplus.data.permissionsex.PermissionsExSuffixProvider;
import codecrafter47.bungeetablistplus.data.playerpoints.PlayerPointsProvider;
import codecrafter47.bungeetablistplus.data.simpleclans.SimpleClansClanColorTagProvider;
import codecrafter47.bungeetablistplus.data.simpleclans.SimpleClansClanNameProvider;
import codecrafter47.bungeetablistplus.data.simpleclans.SimpleClansClanTagLabelProvider;
import codecrafter47.bungeetablistplus.data.simpleclans.SimpleClansClanTagProvider;
import codecrafter47.bungeetablistplus.data.simpleclans.SimpleClansMembersProvider;
import codecrafter47.bungeetablistplus.data.simpleclans.SimpleClansOnlineClanMembersProvider;
import codecrafter47.bungeetablistplus.data.supervanish.SuperVanishIsVanishedProvider;
import codecrafter47.bungeetablistplus.data.vanishnopacket.VanishNoPacketIsVanishedProvider;
import codecrafter47.bungeetablistplus.data.vault.VaultBalanceProvider;
import codecrafter47.bungeetablistplus.data.vault.VaultGroupProvider;
import codecrafter47.bungeetablistplus.data.vault.VaultPrefixProvider;
import codecrafter47.bungeetablistplus.data.vault.VaultSuffixProvider;
import org.bukkit.Bukkit;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;

public class PlayerDataAccess extends AbstractDataAccess<Player> {
    private final Plugin plugin;

    public PlayerDataAccess(Plugin plugin) {
        super(plugin.getLogger(), plugin);
        this.plugin = plugin;
        init();
    }

    protected void init() {
        bind(DataKeys.UserName, Player::getName);
        bind(DataKeys.UUID, Player::getUniqueId);
        bind(DataKeys.Health, Damageable::getHealth);
        bind(DataKeys.Level, Player::getLevel);
        bind(DataKeys.MaxHealth, Player::getMaxHealth);
        bind(DataKeys.XP, Player::getExp);
        bind(DataKeys.TotalXP, Player::getTotalExperience);
        bind(DataKeys.PosX, player -> player.getLocation().getX());
        bind(DataKeys.PosY, player -> player.getLocation().getY());
        bind(DataKeys.PosZ, player -> player.getLocation().getZ());
        bind(DataKeys.Team, player -> {
            Team team = player.getScoreboard().getPlayerTeam(player);
            return team != null ? team.getName() : null;
        });
        bind(PermissionDataKey.class, (player, key) -> player.hasPermission(key.getPermission()));

        bind(DataKeys.DisplayName, Player::getDisplayName);
        bind(DataKeys.PlayerListName, Player::getPlayerListName);
        bind(DataKeys.World, player -> player.getWorld().getName());

        if (Bukkit.getPluginManager().getPlugin("Vault") != null) {
            bind(DataKeys.Vault_Balance, new VaultBalanceProvider(plugin));
            bind(DataKeys.Vault_PermissionGroup, new VaultGroupProvider());
            bind(DataKeys.Vault_Prefix, new VaultPrefixProvider());
            bind(DataKeys.Vault_Suffix, new VaultSuffixProvider());
        }
        
        if (Bukkit.getPluginManager().getPlugin("PermissionsEx") != null) {
			bind(DataKeys.PermissionsEx_PermissionGroup, new PermissionsExGroupProvider());
			bind(DataKeys.PermissionsEx_Prefix, new PermissionsExPrefixProvider());
			bind(DataKeys.PermissionsEx_Suffix, new PermissionsExSuffixProvider());
            bind(DataKeys.PermissionsEx_GroupRank, new PermissionsExGroupRankProvider());
        }

        if (Bukkit.getPluginManager().getPlugin("VanishNoPacket") != null) {
            bind(DataKeys.VanishNoPacket_IsVanished, new VanishNoPacketIsVanishedProvider());
        }

        if (Bukkit.getPluginManager().getPlugin("PlayerPoints") != null) {
            bind(DataKeys.PlayerPoints_Points, new PlayerPointsProvider(logger));
        }

        if (Bukkit.getPluginManager().getPlugin("Factions") != null) {
            if (classExists("com.massivecraft.factions.FPlayer")) {
                bind(DataKeys.Factions_FactionName, new codecrafter47.bungeetablistplus.data.factionsuuid.FactionNameProvider());
                bind(DataKeys.Factions_FactionMembers, new codecrafter47.bungeetablistplus.data.factionsuuid.FactionMembersProvider());
                bind(DataKeys.Factions_FactionPower, new codecrafter47.bungeetablistplus.data.factionsuuid.FactionPowerProvider());
                bind(DataKeys.Factions_FactionsRank, new codecrafter47.bungeetablistplus.data.factionsuuid.FactionRankProvider());
                bind(DataKeys.Factions_FactionsWhere, new codecrafter47.bungeetablistplus.data.factionsuuid.FactionWhereProvider());
                bind(DataKeys.Factions_OnlineFactionMembers, new codecrafter47.bungeetablistplus.data.factionsuuid.FactionOnlineMembersProvider());
                bind(DataKeys.Factions_PlayerPower, new FactionPlayerPowerProvider());
            } else if (classExists("com.massivecraft.factions.entity.MPlayer")) {
                bind(DataKeys.Factions_FactionName, new FactionNameProvider());
                bind(DataKeys.Factions_FactionMembers, new FactionMembersProvider());
                bind(DataKeys.Factions_FactionPower, new FactionPowerProvider());
                bind(DataKeys.Factions_FactionsRank, new FactionRankProvider());
                bind(DataKeys.Factions_FactionsWhere, new FactionWhereProvider());
                bind(DataKeys.Factions_OnlineFactionMembers, new FactionOnlineMembersProvider());
                bind(DataKeys.Factions_PlayerPower, new FactionsPlayerPowerProvider());
            } else {
                logger.warning("Unable to recognize your Factions version. Factions support is disabled. Please contact " +
                        "the plugin developer to request support for your Factions version (" +
                        Bukkit.getPluginManager().getPlugin("Factions").getDescription().getVersion() + ").");
            }
        }

        if (Bukkit.getPluginManager().getPlugin("SuperVanish") != null) {
            bind(DataKeys.SuperVanish_IsVanished, new SuperVanishIsVanishedProvider());
        }

        if (Bukkit.getPluginManager().getPlugin("SimpleClans") != null) {
            bind(DataKeys.SimpleClans_ClanName, new SimpleClansClanNameProvider());
            bind(DataKeys.SimpleClans_ClanMembers, new SimpleClansMembersProvider());
            bind(DataKeys.SimpleClans_OnlineClanMembers, new SimpleClansOnlineClanMembersProvider());
            bind(DataKeys.SimpleClans_ClanTag, new SimpleClansClanTagProvider());
            bind(DataKeys.SimpleClans_ClanTagLabel, new SimpleClansClanTagLabelProvider());
            bind(DataKeys.SimpleClans_ClanColorTag, new SimpleClansClanColorTagProvider());
        }

        if (Bukkit.getPluginManager().getPlugin("Essentials") != null) {
            bind(DataKeys.Essentials_IsVanished, new EssentialsIsVanishedProvider());
        }

        if (Bukkit.getPluginManager().getPlugin("Multiverse-Core") != null) {
            bind(DataKeys.Multiverse_WorldAlias, new MultiverseWorldAliasProvider());
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
