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

package codecrafter47.bungeetablistplus.data;

import java.time.Duration;

public class DataKeys {
    // Bukkit player data keys
    public final static DataKey<Double> Health = DataKey.builder().bukkit().player().id("minecraft:health").build();
    public final static DataKey<Double> MaxHealth = DataKey.builder().bukkit().player().id("minecraft:maxhealth").build();
    public final static DataKey<Integer> Level = DataKey.builder().bukkit().player().id("minecraft:xplevel").build();
    public final static DataKey<Float> XP = DataKey.builder().bukkit().player().id("minecraft:xp").build();
    public final static DataKey<Integer> TotalXP = DataKey.builder().bukkit().player().id("minecraft:totalxp").build();
    public final static DataKey<Double> PosX = DataKey.builder().bukkit().player().id("minecraft:posx").build();
    public final static DataKey<Double> PosY = DataKey.builder().bukkit().player().id("minecraft:posy").build();
    public final static DataKey<Double> PosZ = DataKey.builder().bukkit().player().id("minecraft:posz").build();
    public final static DataKey<String> Team = DataKey.builder().bukkit().player().id("minecraft:team").build();
    public final static DataKey<String> PlayerListName = DataKey.builder().bukkit().player().id("bukkit:playerlistname").build();
    public final static DataKey<String> DisplayName = DataKey.builder().bukkit().player().id("bukkit:displayname").build();
    public final static DataKey<String> World = DataKey.builder().bukkit().player().id("bukkit:world").build();
    public final static DataKey<String> Vault_Prefix = DataKey.builder().bukkit().player().id("vault:prefix").build();
    public final static DataKey<String> Vault_Suffix = DataKey.builder().bukkit().player().id("vault:suffix").build();
    public final static DataKey<String> Vault_PermissionGroup = DataKey.builder().bukkit().player().id("vault:permgroup").build();
    public final static DataKey<Integer> Vault_PermissionGroupRank = DataKey.builder().bukkit().player().id("vault:permgrouprank").build();
    public final static DataKey<Integer> Vault_PermissionGroupWeight = DataKey.builder().bukkit().player().id("vault:permgroupweight").build();
    public final static DataKey<Double> Vault_Balance = DataKey.builder().bukkit().player().id("vault:balance").build();
    public final static DataKey<String> Vault_PrimaryGroupPrefix = DataKey.builder().bukkit().player().id("vault:primarygroupprefix").build();
    public final static DataKey<String> Vault_PlayerPrefix = DataKey.builder().bukkit().player().id("vault:playerprefix").build();
    public final static DataKey<Boolean> VanishNoPacket_IsVanished = DataKey.builder().bukkit().player().id("vanishnopacket:isvanished").build();
    public final static DataKey<Integer> PlayerPoints_Points = DataKey.builder().bukkit().player().id("playerpoints:points").build();
    public final static DataKey<String> Factions_FactionName = DataKey.builder().bukkit().player().id("factions:factionname").build();
    public final static DataKey<Integer> Factions_FactionMembers = DataKey.builder().bukkit().player().id("factions:members").build();
    public final static DataKey<Integer> Factions_OnlineFactionMembers = DataKey.builder().bukkit().player().id("factions:onlinemembers").build();
    public final static DataKey<String> Factions_FactionsWhere = DataKey.builder().bukkit().player().id("factions:where").build();
    public final static DataKey<String> Factions_FactionsRank = DataKey.builder().bukkit().player().id("factions:rank").build();
    public final static DataKey<Integer> Factions_FactionPower = DataKey.builder().bukkit().player().id("factions:factionpower").build();
    public final static DataKey<Integer> Factions_PlayerPower = DataKey.builder().bukkit().player().id("factions:factionpower").build();
    public final static DataKey<Boolean> SuperVanish_IsVanished = DataKey.builder().bukkit().player().id("supervanish:isvanished").build();
    public final static DataKey<String> SimpleClans_ClanName = DataKey.builder().bukkit().player().id("simpleclans:clanname").build();
    public final static DataKey<Integer> SimpleClans_ClanMembers = DataKey.builder().bukkit().player().id("simpleclans:clanmembers").build();
    public final static DataKey<Integer> SimpleClans_OnlineClanMembers = DataKey.builder().bukkit().player().id("simpleclans:onlineclanmembers").build();
    public final static DataKey<String> SimpleClans_ClanTag = DataKey.builder().bukkit().player().id("simpleclans:clantag").build();
    public final static DataKey<String> SimpleClans_ClanTagLabel = DataKey.builder().bukkit().player().id("simpleclans:clantaglabel").build();
    public final static DataKey<String> SimpleClans_ClanColorTag = DataKey.builder().bukkit().player().id("simpleclans:clancolortag").build();
    public final static DataKey<Boolean> Essentials_IsVanished = DataKey.builder().bukkit().player().id("essentials:vanished").build();
    public final static DataKey<Boolean> Essentials_IsAFK = DataKey.builder().bukkit().player().id("essentials:isafk").build();
    public final static DataKey<String> Multiverse_WorldAlias = DataKey.builder().bukkit().player().id("multiverse:worldalias").build();

    // Bukkit server data keys
    public final static DataKey<String> Vault_CurrencyNameSingular = DataKey.builder().bukkit().server().id("vault:currencynamesingular").build();
    public final static DataKey<String> Vault_CurrencyNamePlural = DataKey.builder().bukkit().server().id("vault:currencynameplural").build();
    public final static DataKey<Double> TPS = DataKey.builder().bukkit().server().id("minecraft:tps").build();
    public final static DataKey<String> MinecraftVersion = DataKey.builder().bukkit().server().id("minecraft:version").build();
    public final static DataKey<String> ServerModName = DataKey.builder().bukkit().server().id("bukkit:name").build();
    public final static DataKey<String> ServerModVersion = DataKey.builder().bukkit().server().id("bukkit:version").build();

    // BungeeCord player data keys
    public final static DataKey<String> BungeeCord_PrimaryGroup = DataKey.builder().bungee().player().id("bungeecord:group").build();
    public final static DataKey<Integer> BungeeCord_Rank = DataKey.builder().bungee().player().id("bungeecord:rank").build();
    public final static DataKey<String> BungeePerms_PrimaryGroup = DataKey.builder().bungee().player().id("bungeeperms:group").build();
    public final static DataKey<String> BungeePerms_Prefix = DataKey.builder().bungee().player().id("bungeeperms:prefix").build();
    public final static DataKey<String> BungeePerms_DisplayPrefix = DataKey.builder().bungee().player().id("bungeeperms:displayprefix").build();
    public final static DataKey<String> BungeePerms_Suffix = DataKey.builder().bungee().player().id("bungeeperms:suffix").build();
    public final static DataKey<Integer> BungeePerms_Rank = DataKey.builder().bungee().player().id("bungeeperms:rank").build();
    public final static DataKey<String> BungeePerms_PrimaryGroupPrefix = DataKey.builder().bungee().player().id("bungeeperms:primarygroupprefix").build();
    public final static DataKey<String> BungeePerms_PlayerPrefix = DataKey.builder().bungee().player().id("bungeeperms:playerprefix").build();
    public final static DataKey<String> ClientVersion = DataKey.builder().bungee().player().id("minecraft:clientversion").build();
    public final static DataKey<Duration> BungeeCord_SessionDuration = DataKey.builder().bungee().player().id("minecraft:sessionduration").build();

    public static PermissionDataKey permission(String permission) {
        return new PermissionDataKey(permission);
    }
}
