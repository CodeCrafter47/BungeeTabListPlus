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
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.data.PermissionDataKey;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.util.Functions;
import codecrafter47.bungeetablistplus.util.PingTask;
import lombok.AllArgsConstructor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public abstract class Placeholder {

    private static final Placeholder NULL_PLACEHOLDER = new NullPlaceholder();
    private static final Placeholder OTHER_COUNT_PLACEHOLDER = new OtherCountPlaceholder();
    private static final Placeholder SERVER_PLAYER_COUNT_PLACEHOLDER = new ServerPlayerCountPlaceholder();
    private static final Map<String, Function<Player, String>> playerPlaceholders = new HashMap<>();
    private static final Map<String, Function<ServerInfo, String>> serverPlaceholders = new HashMap<>();

    public static final Map<String, DataKey<String>> placeholderAPIDataKeys = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, DataKey<String>> remoteThirdPartyDataKeys = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, DataKey<String>> thirdPartyDataKeys = Collections.synchronizedMap(new HashMap<>());

    static {
        playerPlaceholders.put("name", IPlayer::getName);
        playerPlaceholders.put("ping", player -> Integer.toString(player.getPing()));
        playerPlaceholders.put("skin", IPlayer::getName); // todo change this
        playerPlaceholders.put("bungeeperms_display", player -> player.get(DataKeys.BungeePerms_DisplayPrefix).orElse(""));
        playerPlaceholders.put("bungeeperms_prefix", player -> player.get(DataKeys.BungeePerms_Prefix).orElse(""));
        playerPlaceholders.put("bungeeperms_suffix", player -> player.get(DataKeys.BungeePerms_Suffix).orElse(""));
        playerPlaceholders.put("bungeeperms_primary_group", player -> player.get(DataKeys.BungeePerms_PrimaryGroup).orElse(""));
        playerPlaceholders.put("bungeeperms_primary_group_prefix", player -> player.get(DataKeys.BungeePerms_PrimaryGroupPrefix).orElse(""));
        playerPlaceholders.put("bungeeperms_user_prefix", player -> player.get(DataKeys.BungeePerms_PlayerPrefix).orElse(""));
        playerPlaceholders.put("client_version", player -> player.get(DataKeys.ClientVersion).orElse(""));
        playerPlaceholders.put("uuid", player -> player.getUniqueID().toString());
        playerPlaceholders.put("world", player -> player.get(DataKeys.World).orElse(""));
        playerPlaceholders.put("team", player -> player.get(DataKeys.Team).orElse(""));
        playerPlaceholders.put("vault_balance", player -> player.get(DataKeys.Vault_Balance).map(d -> String.format("%1.2f", d)).orElse(""));
        playerPlaceholders.put("multiverse_world_alias", player -> player.get(DataKeys.Multiverse_WorldAlias).orElse(""));
        playerPlaceholders.put("faction_name", player -> player.get(DataKeys.Factions_FactionName).orElse(""));
        playerPlaceholders.put("faction_member_count", player -> player.get(DataKeys.Factions_FactionMembers).map(n -> Integer.toString(n)).orElse(""));
        playerPlaceholders.put("faction_online_member_count", player -> player.get(DataKeys.Factions_OnlineFactionMembers).map(n -> Integer.toString(n)).orElse(""));
        playerPlaceholders.put("faction_at_current_location", player -> player.get(DataKeys.Factions_FactionsWhere).orElse(""));
        playerPlaceholders.put("faction_power", player -> player.get(DataKeys.Factions_FactionPower).map(n -> Integer.toString(n)).orElse(""));
        playerPlaceholders.put("faction_player_power", player -> player.get(DataKeys.Factions_PlayerPower).map(n -> Integer.toString(n)).orElse(""));
        playerPlaceholders.put("faction_rank", player -> player.get(DataKeys.Factions_FactionsRank).orElse(""));
        playerPlaceholders.put("SimpleClans_ClanName", player -> player.get(DataKeys.SimpleClans_ClanName).orElse(""));
        playerPlaceholders.put("SimpleClans_ClanMembers", player -> player.get(DataKeys.SimpleClans_ClanMembers).map(n -> Integer.toString(n)).orElse(""));
        playerPlaceholders.put("SimpleClans_OnlineClanMembers", player -> player.get(DataKeys.SimpleClans_OnlineClanMembers).map(n -> Integer.toString(n)).orElse(""));
        playerPlaceholders.put("SimpleClans_ClanTag", player -> player.get(DataKeys.SimpleClans_ClanTag).orElse(""));
        playerPlaceholders.put("SimpleClans_ClanTagLabel", player -> player.get(DataKeys.SimpleClans_ClanTagLabel).orElse(""));
        playerPlaceholders.put("SimpleClans_ClanColorTag", player -> player.get(DataKeys.SimpleClans_ClanColorTag).orElse(""));
        playerPlaceholders.put("vault_primary_group", player -> player.get(DataKeys.Vault_PermissionGroup).orElse(""));
        playerPlaceholders.put("vault_prefix", player -> player.get(DataKeys.Vault_Prefix).orElse(""));
        playerPlaceholders.put("vault_suffix", player -> player.get(DataKeys.Vault_Suffix).orElse(""));
        playerPlaceholders.put("vault_primary_group_prefix", player -> player.get(DataKeys.Vault_PrimaryGroupPrefix).orElse(""));
        playerPlaceholders.put("vault_player_prefix", player -> player.get(DataKeys.Vault_PlayerPrefix).orElse(""));
        playerPlaceholders.put("health", player -> player.get(DataKeys.Health).map(h -> String.format("%1.1f", h)).orElse(""));
        playerPlaceholders.put("max_health", player -> player.get(DataKeys.MaxHealth).map(h -> String.format("%1.1f", h)).orElse(""));
        playerPlaceholders.put("location_x", player -> player.get(DataKeys.PosX).map(d -> String.format("%1.0f", d)).orElse(""));
        playerPlaceholders.put("location_y", player -> player.get(DataKeys.PosY).map(d -> String.format("%1.0f", d)).orElse(""));
        playerPlaceholders.put("location_z", player -> player.get(DataKeys.PosZ).map(d -> String.format("%1.0f", d)).orElse(""));
        playerPlaceholders.put("xp", player -> player.get(DataKeys.XP).map(d -> String.format("%1.2f", d)).orElse(""));
        playerPlaceholders.put("total_xp", player -> player.get(DataKeys.TotalXP).map(d -> Integer.toString(d)).orElse(""));
        playerPlaceholders.put("level", player -> player.get(DataKeys.Level).map(d -> Integer.toString(d)).orElse(""));
        playerPlaceholders.put("player_points", player -> player.get(DataKeys.PlayerPoints_Points).map(Object::toString).orElse(""));
        playerPlaceholders.put("vault_currency", player -> player.get(DataKeys.Vault_CurrencyNameSingular).orElse(""));
        playerPlaceholders.put("vault_currency_plural", player -> player.get(DataKeys.Vault_CurrencyNamePlural).orElse(""));
        playerPlaceholders.put("tab_name", player -> player.get(DataKeys.PlayerListName).orElse(player.getName()));
        playerPlaceholders.put("display_name", player -> player.get(DataKeys.DisplayName).orElse(player.getName()));

        // Server
        serverPlaceholders.put("tps", serverInfo -> BungeeTabListPlus.getInstance().getBridge().get(serverInfo, DataKeys.TPS).map(d -> String.format("%1.1f", d)).orElse(""));
        serverPlaceholders.put("online", serverInfo -> {
            PingTask serverState = BungeeTabListPlus.getInstance().getServerState(serverInfo.getName());
            return serverState != null ? Boolean.toString(serverState.isOnline()) : "false";
        });
    }

    public abstract String evaluate(Context context);

    public static Placeholder of(String s) {
        String[] tokens = s.split(" ");

        if (tokens.length == 0) {
            return NULL_PLACEHOLDER;
        } else if ("player".equals(tokens[0])) {
            return parsePlayerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), Context::getPlayer);
        } else if ("viewer".equals(tokens[0])) {
            return parsePlayerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), Context::getViewer);
        } else if ("server".equals(tokens[0])) {
            return parseServerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), Context::getServer);
        } else if (tokens[0].startsWith("playerset:")) {
            String playerSet = tokens[0].split(":")[1];
            return new Placeholder() {
                @Override
                public String evaluate(Context context) {
                    return Integer.toString(context.getPlayers(playerSet).size());
                }
            };
        } else if (tokens[0].startsWith("server:")) {
            String server = tokens[0].split(":")[1];
            return parseServerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), context -> ProxyServer.getInstance().getServerInfo(server));
        } else if ("time".equals(tokens[0])) {
            return new TimePlaceholder(TimePlaceholders.getFormat(tokens[1]));
        } else if ("server_player_count".equals(tokens[0])) {
            return SERVER_PLAYER_COUNT_PLACEHOLDER;
        } else if ("other_count".equals(tokens[0])) {
            return OTHER_COUNT_PLACEHOLDER;
        } else {
            return NULL_PLACEHOLDER;
        }
    }

    private static Placeholder parsePlayerPlaceholder(String[] tokens, Function<Context, Player> playerFunction) {
        if (tokens.length == 0) {
            return new PlayerBoundPlaceholder(playerFunction, Player::getName);
        } else if ("server".equals(tokens[0])) {
            return parseServerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), Functions.composeNullable(p -> p.getServer().orElse(null), playerFunction));
        } else if ("permission".equals(tokens[0])) {
            return new PermissionPlaceholder(playerFunction, tokens[1]);
        } else if (playerPlaceholders.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, playerPlaceholders.get(tokens[0]));
        } else if (thirdPartyDataKeys.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, player -> player.get(thirdPartyDataKeys.get(tokens[0])).orElse(""));
        } else if (remoteThirdPartyDataKeys.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, player -> player.get(remoteThirdPartyDataKeys.get(tokens[0])).orElse(""));
        } else if (placeholderAPIDataKeys.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, player -> player.get(placeholderAPIDataKeys.get(tokens[0])).orElse(""));
        } else {
            BungeeTabListPlus.getInstance().getPlaceholderAPIHook().addMaybePlaceholder(tokens[0]);
            return NULL_PLACEHOLDER;
        }
    }

    private static Placeholder parseServerPlaceholder(String[] tokens, Function<Context, ServerInfo> serverFunction) {
        if (tokens.length == 0) {
            return new ServerBoundPlaceholder(serverFunction, ServerInfo::getName);
        } else if (serverPlaceholders.containsKey(tokens[0])) {
            return new ServerBoundPlaceholder(serverFunction, serverPlaceholders.get(tokens[0]));
        } else {
            return NULL_PLACEHOLDER;
        }
    }

    private static class NullPlaceholder extends Placeholder {

        @Override
        public String evaluate(Context context) {
            return "";
        }
    }

    @AllArgsConstructor
    private static class PlayerBoundPlaceholder extends Placeholder {
        private final Function<Context, Player> playerFunction;
        private final Function<Player, String> function;

        @Override
        public String evaluate(Context context) {
            Player player = playerFunction.apply(context);
            return player != null ? function.apply(player) : "";
        }
    }

    private static class PermissionPlaceholder extends Placeholder {
        private final Function<Context, Player> playerFunction;
        private final PermissionDataKey permissionDataKey;

        private PermissionPlaceholder(Function<Context, Player> playerFunction, String permission) {
            this.playerFunction = playerFunction;
            permissionDataKey = DataKeys.permission(permission);
        }

        @Override
        public String evaluate(Context context) {
            Player player = playerFunction.apply(context);
            return player != null ? player.get(permissionDataKey).orElse(false).toString() : "";
        }
    }

    @AllArgsConstructor
    private static class ServerBoundPlaceholder extends Placeholder {
        private final Function<Context, ServerInfo> serverFunction;
        private final Function<ServerInfo, String> function;

        @Override
        public String evaluate(Context context) {
            ServerInfo serverInfo = serverFunction.apply(context);
            return serverInfo != null ? function.apply(serverInfo) : "";
        }
    }

    private static class OtherCountPlaceholder extends Placeholder {

        @Override
        public String evaluate(Context context) {
            int n;
            return -1 != (n = context.getOtherPlayersCount()) ? Integer.toString(n) : "";
        }
    }

    private static class ServerPlayerCountPlaceholder extends Placeholder {

        @Override
        public String evaluate(Context context) {
            int n;
            return -1 != (n = context.getServerPlayerCount()) ? Integer.toString(n) : "";
        }
    }

    private static class TimePlaceholder extends Placeholder {
        private final SimpleDateFormat format;

        public TimePlaceholder(SimpleDateFormat format) {
            this.format = format;
        }

        @Override
        public String evaluate(Context context) {
            return format.format(System.currentTimeMillis());
        }
    }
}
