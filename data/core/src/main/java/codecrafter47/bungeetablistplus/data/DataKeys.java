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

import java.util.UUID;

public class DataKeys {
    public final static DataKey<String> UserName = DataKey.builder().player().id("minecraft:username").build();
    public final static DataKey<UUID> UUID = DataKey.builder().player().id("minecraft:uuid").build();
    public final static DataKey<Double> Health = DataKey.builder().player().id("minecraft:health").build();
    public final static DataKey<Double> MaxHealth = DataKey.builder().player().id("minecraft:maxhealth").build();
    public final static DataKey<Integer> Level = DataKey.builder().player().id("minecraft:xplevel").build();
    public final static DataKey<Float> XP = DataKey.builder().player().id("minecraft:xp").build();
    public final static DataKey<Integer> TotalXP = DataKey.builder().player().id("minecraft:totalxp").build();
    public final static DataKey<Double> PosX = DataKey.builder().player().id("minecraft:posx").build();
    public final static DataKey<Double> PosY = DataKey.builder().player().id("minecraft:posy").build();
    public final static DataKey<Double> PosZ = DataKey.builder().player().id("minecraft:posz").build();
    public final static DataKey<String> Team = DataKey.builder().player().id("minecraft:team").build();
    public final static DataKey<String> PlayerListName = DataKey.builder().player().id("bukkit:playerlistname").build();
    public final static DataKey<String> DisplayName = DataKey.builder().player().id("bukkit:displayname").build();
    public final static DataKey<String> World = DataKey.builder().player().id("bukkit:world").build();
    public final static DataKey<String> Vault_Prefix = DataKey.builder().player().id("vault:prefix").build();
    public final static DataKey<String> Vault_Suffix = DataKey.builder().player().id("vault:suffix").build();
    public final static DataKey<String> Vault_PermissionGroup = DataKey.builder().player().id("vault:permgroup").build();
    public final static DataKey<Double> Vault_Balance = DataKey.builder().player().id("vault:balance").build();
    public final static DataKey<String> PermissionsEx_Prefix = DataKey.builder().player().id("permissionsex:prefix").build();
    public final static DataKey<String> PermissionsEx_Suffix = DataKey.builder().player().id("permissionsex:suffix").build();
    public final static DataKey<String> PermissionsEx_PermissionGroup = DataKey.builder().player().id("permissionsex:permgroup").build();
    public final static DataKey<Integer> PermissionsEx_GroupRank = DataKey.builder().player().id("permissionsex:permgroup.rank").build();
    public final static DataKey<Boolean> VanishNoPacket_IsVanished = DataKey.builder().player().id("vanishnopacket:isvanished").build();
    public final static DataKey<Integer> PlayerPoints_Points = DataKey.builder().player().id("playerpoints:points").build();
    public final static DataKey<String> Factions_FactionName = DataKey.builder().player().id("factions:factionname").build();
    public final static DataKey<Integer> Factions_FactionMembers = DataKey.builder().player().id("factions:members").build();
    public final static DataKey<Integer> Factions_OnlineFactionMembers = DataKey.builder().player().id("factions:onlinemembers").build();
    public final static DataKey<String> Factions_FactionsWhere = DataKey.builder().player().id("factions:where").build();
    public final static DataKey<String> Factions_FactionsRank = DataKey.builder().player().id("factions:rank").build();
    public final static DataKey<Integer> Factions_FactionPower = DataKey.builder().player().id("factions:factionpower").build();
    public final static DataKey<Integer> Factions_PlayerPower = DataKey.builder().player().id("factions:factionpower").build();
    public final static DataKey<Boolean> SuperVanish_IsVanished = DataKey.builder().player().id("supervanish:isvanished").build();
    public final static DataKey<String> SimpleClans_ClanName = DataKey.builder().player().id("simpleclans:clanname").build();
    public final static DataKey<Integer> SimpleClans_ClanMembers = DataKey.builder().player().id("simpleclans:clanmembers").build();
    public final static DataKey<Integer> SimpleClans_OnlineClanMembers = DataKey.builder().player().id("simpleclans:onlineclanmembers").build();
    public final static DataKey<String> SimpleClans_ClanTag = DataKey.builder().player().id("simpleclans:clantag").build();
    public final static DataKey<String> SimpleClans_ClanTagLabel = DataKey.builder().player().id("simpleclans:clantaglabel").build();
    public final static DataKey<String> SimpleClans_ClanColorTag = DataKey.builder().player().id("simpleclans:clancolortag").build();
    public final static DataKey<Boolean> Essentials_IsVanished = DataKey.builder().player().id("essentials:vanished").build();
    public final static DataKey<String> Vault_CurrencyNameSingular = DataKey.builder().server().id("vault:currencynamesingular").build();
    public final static DataKey<String> Vault_CurrencyNamePlural = DataKey.builder().server().id("vault:currencynameplural").build();
    public final static DataKey<Double> TPS = DataKey.builder().server().id("minecraft:tps").build();
    public final static DataKey<String> MinecraftVersion = DataKey.builder().server().id("minecraft:version").build();
    public final static DataKey<String> ServerModName = DataKey.builder().server().id("bukkit:name").build();
    public final static DataKey<String> ServerModVersion = DataKey.builder().server().id("bukkit:version").build();

    public static PermissionDataKey permission(String permission) {
        return new PermissionDataKey(permission);
    }
}
