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
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.util.Functions;
import codecrafter47.bungeetablistplus.util.PingTask;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class Placeholder {

    private static final Placeholder NULL_PLACEHOLDER = new NullPlaceholder();
    private static final Placeholder OTHER_COUNT_PLACEHOLDER = new OtherCountPlaceholder();
    private static final Placeholder SERVER_PLAYER_COUNT_PLACEHOLDER = new ServerPlayerCountPlaceholder();
    private static final Map<String, BiFunction<String[], Function<Context, Player>, Placeholder>> playerPlaceholders = new HashMap<>();
    private static final Map<String, BiFunction<String[], Function<Context, String>, Placeholder>> serverPlaceholders = new HashMap<>();

    public static final Map<String, DataKey<String>> placeholderAPIDataKeys = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, DataKey<String>> remoteThirdPartyDataKeys = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, DataKey<String>> remoteThirdPartyServerDataKeys = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, DataKey<String>> thirdPartyDataKeys = Collections.synchronizedMap(new HashMap<>());
    public static final Map<String, DataKey<String>> thirdPartyServerDataKeys = Collections.synchronizedMap(new HashMap<>());

    static {
        playerPlaceholders.put("name", ofFunction(IPlayer::getName));
        playerPlaceholders.put("ping", ofFunction(player -> Integer.toString(player.getPing())));
        playerPlaceholders.put("bungeecord_primary_group", ofStringData(BungeeData.BungeeCord_PrimaryGroup));
        playerPlaceholders.put("bungeeperms_display", ofStringData(BungeeData.BungeePerms_DisplayPrefix));
        playerPlaceholders.put("bungeeperms_prefix", ofStringData(BungeeData.BungeePerms_Prefix));
        playerPlaceholders.put("bungeeperms_suffix", ofStringData(BungeeData.BungeePerms_Suffix));
        playerPlaceholders.put("bungeeperms_primary_group", ofStringData(BungeeData.BungeePerms_PrimaryGroup));
        playerPlaceholders.put("bungeeperms_primary_group_prefix", ofStringData(BungeeData.BungeePerms_PrimaryGroupPrefix));
        playerPlaceholders.put("bungeeperms_user_prefix", ofStringData(BungeeData.BungeePerms_PlayerPrefix));
        playerPlaceholders.put("luckperms_prefix", ofStringData(BungeeData.LuckPerms_Prefix));
        playerPlaceholders.put("luckperms_suffix", ofStringData(BungeeData.LuckPerms_Suffix));
        playerPlaceholders.put("luckperms_primary_group", ofStringData(BungeeData.LuckPerms_PrimaryGroup));
        playerPlaceholders.put("client_version", ofStringData(BungeeData.ClientVersion));
        playerPlaceholders.put("uuid", ofFunction(player -> player.getUniqueID().toString()));
        playerPlaceholders.put("world", ofStringData(MinecraftData.World));
        playerPlaceholders.put("team", ofStringData(MinecraftData.Team));
        playerPlaceholders.put("vault_balance", ofDoubleData(MinecraftData.Economy_Balance));
        playerPlaceholders.put("vault_balance2", ofFunction(player -> player.getOpt(MinecraftData.Economy_Balance).map(b -> {
            if (b >= 10_000_000) {
                return String.format("%1.0fM", b / 1_000_000);
            } else if (b >= 10_000) {
                return String.format("%1.0fK", b / 1_000);
            } else if (b >= 100) {
                return String.format("%1.0f", b);
            } else {
                return String.format("%1.2f", b);
            }
        }).orElse("")));
        playerPlaceholders.put("multiverse_world_alias", ofStringData(BukkitData.Multiverse_WorldAlias));
        playerPlaceholders.put("faction_name", ofStringData(BukkitData.Factions_FactionName));
        playerPlaceholders.put("faction_member_count", ofIntData(BukkitData.Factions_FactionMembers));
        playerPlaceholders.put("faction_online_member_count", ofIntData(BukkitData.Factions_OnlineFactionMembers));
        playerPlaceholders.put("faction_at_current_location", ofStringData(BukkitData.Factions_FactionsWhere));
        playerPlaceholders.put("faction_power", ofIntData(BukkitData.Factions_FactionPower));
        playerPlaceholders.put("faction_player_power", ofIntData(BukkitData.Factions_PlayerPower));
        playerPlaceholders.put("faction_rank", ofStringData(BukkitData.Factions_FactionsRank));
        playerPlaceholders.put("SimpleClans_ClanName", ofStringData(BukkitData.SimpleClans_ClanName));
        playerPlaceholders.put("SimpleClans_ClanMembers", ofIntData(BukkitData.SimpleClans_ClanMembers));
        playerPlaceholders.put("SimpleClans_OnlineClanMembers", ofIntData(BukkitData.SimpleClans_OnlineClanMembers));
        playerPlaceholders.put("SimpleClans_ClanTag", ofStringData(BukkitData.SimpleClans_ClanTag));
        playerPlaceholders.put("SimpleClans_ClanTagLabel", ofStringData(BukkitData.SimpleClans_ClanTagLabel));
        playerPlaceholders.put("SimpleClans_ClanColorTag", ofStringData(BukkitData.SimpleClans_ClanColorTag));
        playerPlaceholders.put("vault_primary_group", ofStringData(MinecraftData.Permissions_PermissionGroup));
        playerPlaceholders.put("vault_prefix", ofStringData(MinecraftData.Permissions_Prefix));
        playerPlaceholders.put("vault_suffix", ofStringData(MinecraftData.Permissions_Suffix));
        playerPlaceholders.put("vault_primary_group_prefix", ofStringData(MinecraftData.Permissions_PrimaryGroupPrefix));
        playerPlaceholders.put("vault_player_prefix", ofStringData(MinecraftData.Permissions_PlayerPrefix));
        playerPlaceholders.put("health", ofDoubleData(MinecraftData.Health));
        playerPlaceholders.put("max_health", ofDoubleData(MinecraftData.MaxHealth));
        playerPlaceholders.put("location_x", ofDoubleData(MinecraftData.PosX));
        playerPlaceholders.put("location_y", ofDoubleData(MinecraftData.PosY));
        playerPlaceholders.put("location_z", ofDoubleData(MinecraftData.PosZ));
        playerPlaceholders.put("xp", ofFloatData(MinecraftData.XP));
        playerPlaceholders.put("total_xp", ofIntData(MinecraftData.TotalXP));
        playerPlaceholders.put("level", ofIntData(MinecraftData.Level));
        playerPlaceholders.put("player_points", ofIntData(BukkitData.PlayerPoints_Points));
        playerPlaceholders.put("vault_currency", ofStringData(MinecraftData.Economy_CurrencyNameSingular));
        playerPlaceholders.put("vault_currency_plural", ofStringData(MinecraftData.Economy_CurrencyNamePlural));
        playerPlaceholders.put("tab_name", ofFunction(player -> player.getOpt(BukkitData.PlayerListName).orElse(player.getName())));
        playerPlaceholders.put("display_name", ofFunction(player -> player.getOpt(MinecraftData.DisplayName).orElse(player.getName())));
        playerPlaceholders.put("bungeecord_display_name", ofFunction(player -> player.getOpt(BungeeData.BungeeCord_DisplayName).orElse(player.getName())));
        playerPlaceholders.put("session_duration_seconds", ofIntFunction(player -> player.getOpt(BungeeData.BungeeCord_SessionDuration).map(duration -> (int) (duration.getSeconds() % 60)).orElse(0)));
        playerPlaceholders.put("session_duration_minutes", ofIntFunction(player -> player.getOpt(BungeeData.BungeeCord_SessionDuration).map(duration -> (int) ((duration.getSeconds() % 3600) / 60)).orElse(0)));
        playerPlaceholders.put("session_duration_hours", ofIntFunction(player -> player.getOpt(BungeeData.BungeeCord_SessionDuration).map(duration -> (int) (duration.getSeconds() / 3600)).orElse(0)));
        playerPlaceholders.put("essentials_afk", ofFunction(p -> p.getOpt(BukkitData.Essentials_IsAFK).orElse(false).toString()));
        playerPlaceholders.put("is_hidden", ofFunction(p -> Boolean.toString(BungeeTabListPlus.isHidden(p))));
        playerPlaceholders.put("gamemode", ofIntData(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE));
        playerPlaceholders.put("bungeeonlinetime_seconds", ofIntFunction(player -> player.getOpt(BungeeData.BungeeOnlineTime_OnlineTime).map(duration -> (int) (duration.getSeconds() % 60)).orElse(0)));
        playerPlaceholders.put("bungeeonlinetime_minutes", ofIntFunction(player -> player.getOpt(BungeeData.BungeeOnlineTime_OnlineTime).map(duration -> (int) ((duration.getSeconds() % 3600) / 60)).orElse(0)));
        playerPlaceholders.put("bungeeonlinetime_hours", ofIntFunction(player -> player.getOpt(BungeeData.BungeeOnlineTime_OnlineTime).map(duration -> (int) (duration.getSeconds() / 3600)).orElse(0)));
        playerPlaceholders.put("redisbungee_server_id", ofStringData(BTLPBungeeDataKeys.DATA_KEY_RedisBungee_ServerId));
        playerPlaceholders.put("askyblock_island_level", ofIntData(BukkitData.ASkyBlock_IslandLevel));
        playerPlaceholders.put("askyblock_island_name", ofStringData(BukkitData.ASkyBlock_IslandName));
        playerPlaceholders.put("askyblock_team_leader", ofStringData(BukkitData.ASkyBlock_TeamLeader));
        playerPlaceholders.put("paf_clans_clan_name", ofStringData(BungeeData.PAFClans_ClanName));
        playerPlaceholders.put("paf_clans_clan_tag", ofStringData(BungeeData.PAFClans_ClanTag));
        playerPlaceholders.put("paf_clans_clan_member_count", ofIntData(BungeeData.PAFClans_MemberCount));
        playerPlaceholders.put("paf_clans_clan_online_member_count", ofIntData(BungeeData.PAFClans_OnlineMemberCount));
        playerPlaceholders.put("paf_clans_is_leader", ofFunction(p -> p.getOpt(BungeeData.PAFClans_IsLeader).orElse(false).toString()));

        // Server
        serverPlaceholders.put("tps", (tokens, serverFunction) -> {
            if (tokens.length == 0) {
                return new ServerBoundPlaceholder(serverFunction, server -> {
                    Double tps = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(server).get(MinecraftData.TPS);
                    return tps != null ? String.format("%1.1f", tps) : "";
                });
            } else {
                String format = "%0" + tokens[0] + "f";
                return new ServerBoundPlaceholder(serverFunction, server -> {
                    Double tps = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(server).get(MinecraftData.TPS);
                    return tps != null ? String.format(format, tps) : "";
                });
            }
        });
        serverPlaceholders.put("name", (tokens, serverFunction) -> {
            if (tokens.length == 0) {
                return new ServerBoundPlaceholder(serverFunction, server -> server);
            } else {
                int length = Integer.valueOf(tokens[0]);
                return new ServerBoundPlaceholder(serverFunction, server -> server.substring(0, length));
            }
        });
        serverPlaceholders.put("online", (tokens, serverFunction) -> {
            return new ServerBoundPlaceholder(serverFunction, serverName -> {
                PingTask serverState = BungeeTabListPlus.getInstance().getServerState(serverName);
                return serverState != null ? Boolean.toString(serverState.isOnline()) : "false";
            });
        });
    }

    public abstract String evaluate(Context context);

    public static Placeholder of(String s) {
        String[] tokens = s.split(" ");

        if (tokens.length == 0) {
            return NULL_PLACEHOLDER;
        } else if ("player".equals(tokens[0])) {
            return parsePlayerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), (context) -> context.get(Context.KEY_PLAYER));
        } else if ("viewer".equals(tokens[0])) {
            return parsePlayerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), (context) -> context.get(Context.KEY_VIEWER));
        } else if ("server".equals(tokens[0])) {
            return parseServerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), (context) -> context.get(Context.KEY_SERVER));
        } else if (tokens[0].startsWith("playerset:")) {
            String playerSet = tokens[0].split(":")[1];
            if (tokens.length == 3 && "size".equals(tokens[1])) {
                String format = "%0" + tokens[2] + "d";
                return new Placeholder() {
                    @Override
                    public String evaluate(Context context) {
                        List<Player> players = context.get(Context.KEY_PLAYER_SETS).get(playerSet);
                        if (players == null) {
                            players = Collections.emptyList();
                            BungeeTabListPlus.getInstance().getLogger().info("Missing player set " + playerSet);
                        }
                        return String.format(format, players.size());
                    }
                };
            } else {
                return new Placeholder() {
                    @Override
                    public String evaluate(Context context) {
                        List<Player> players = context.get(Context.KEY_PLAYER_SETS).get(playerSet);
                        if (players == null) {
                            players = Collections.emptyList();
                            BungeeTabListPlus.getInstance().getLogger().info("Missing player set " + playerSet);
                        }
                        return Integer.toString(players.size());
                    }
                };
            }
        } else if (tokens[0].startsWith("server:")) {
            String server = tokens[0].split(":")[1];
            return parseServerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), context -> server);
        } else if ("time".equals(tokens[0])) {
            return new TimePlaceholder(getFormat(tokens[1]));
        } else if ("server_player_count".equals(tokens[0])) {
            return SERVER_PLAYER_COUNT_PLACEHOLDER;
        } else if ("other_count".equals(tokens[0])) {
            return OTHER_COUNT_PLACEHOLDER;
        } else {
            return new CustomPlaceholder(tokens[0], Arrays.copyOfRange(tokens, 1, tokens.length));
        }
    }

    private static Placeholder parsePlayerPlaceholder(String[] tokens, Function<Context, Player> playerFunction) {
        if (tokens.length == 0) {
            return new PlayerBoundPlaceholder(playerFunction, Player::getName);
        } else if ("server".equals(tokens[0])) {
            return parseServerPlaceholder(Arrays.copyOfRange(tokens, 1, tokens.length), Functions.composeNullable(p -> p.getOpt(BungeeData.BungeeCord_Server).orElse(null), playerFunction));
        } else if ("permission".equals(tokens[0])) {
            return new PermissionPlaceholder(playerFunction, tokens[1]);
        } else if (playerPlaceholders.containsKey(tokens[0])) {
            return playerPlaceholders.get(tokens[0]).apply(Arrays.copyOfRange(tokens, 1, tokens.length), playerFunction);
        } else if (thirdPartyDataKeys.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(thirdPartyDataKeys.get(tokens[0])).orElse(""));
        } else if (remoteThirdPartyDataKeys.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(remoteThirdPartyDataKeys.get(tokens[0])).orElse(""));
        } else if (placeholderAPIDataKeys.containsKey(tokens[0])) {
            return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(placeholderAPIDataKeys.get(tokens[0])).orElse(""));
        } else {
            BungeeTabListPlus.getInstance().getPlaceholderAPIHook().addMaybePlaceholder(tokens[0]);
            return NULL_PLACEHOLDER;
        }
    }

    private static Placeholder parseServerPlaceholder(String[] tokens, Function<Context, String> serverFunction) {
        if (tokens.length == 0) {
            return new ServerBoundPlaceholder(serverFunction, o -> o);
        } else if (serverPlaceholders.containsKey(tokens[0])) {
            return serverPlaceholders.get(tokens[0]).apply(Arrays.copyOfRange(tokens, 1, tokens.length), serverFunction);
        } else if (thirdPartyServerDataKeys.containsKey(tokens[0])) {
            DataKey<String> key = thirdPartyServerDataKeys.get(tokens[0]);
            return new ServerBoundPlaceholder(serverFunction, serverName -> {
                String value = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(serverName).get(key);
                return value != null ? value : "";
            });
        } else if (remoteThirdPartyServerDataKeys.containsKey(tokens[0])) {
            DataKey<String> key = remoteThirdPartyServerDataKeys.get(tokens[0]);
            return new ServerBoundPlaceholder(serverFunction, serverName -> {
                String value = BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(serverName).get(key);
                return value != null ? value : "";
            });
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
        private final DataKey<Boolean> permissionDataKey;

        private PermissionPlaceholder(Function<Context, Player> playerFunction, String permission) {
            this.playerFunction = playerFunction;
            permissionDataKey = MinecraftData.permission(permission);
        }

        @Override
        public String evaluate(Context context) {
            Player player = playerFunction.apply(context);
            return player != null ? player.getOpt(permissionDataKey).orElse(false).toString() : "";
        }
    }

    @AllArgsConstructor
    private static class ServerBoundPlaceholder extends Placeholder {
        private final Function<Context, String> serverFunction;
        private final Function<String, String> function;

        @Override
        public String evaluate(Context context) {
            String serverName = serverFunction.apply(context);
            return serverName != null ? function.apply(serverName) : "";
        }
    }

    private static class OtherCountPlaceholder extends Placeholder {

        @Override
        public String evaluate(Context context) {
            Integer n;
            return null != (n = context.get(Context.KEY_OTHER_PLAYERS_COUNT)) ? n.toString() : "";
        }
    }

    private static class ServerPlayerCountPlaceholder extends Placeholder {

        @Override
        public String evaluate(Context context) {
            Integer n;
            return null != (n = context.get(Context.KEY_SERVER_PLAYER_COUNT)) ? n.toString() : "";
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

    @RequiredArgsConstructor
    private static class CustomPlaceholder extends Placeholder {
        private final String id;
        private final String[] parameters;
        private Placeholder instance = null;

        @Override
        public String evaluate(Context context) {
            if (instance == null) {
                if (context.get(Context.KEY_CUSTOM_PLACEHOLDERS).containsKey(id)) {
                    instance = context.get(Context.KEY_CUSTOM_PLACEHOLDERS).get(id).instantiate(parameters);
                } else {
                    return "";
                }
            }
            return instance.evaluate(context);
        }

    }

    private static BiFunction<String[], Function<Context, Player>, Placeholder> ofFunction(Function<Player, String> function) {
        return (tokens, playerFunction) -> new PlayerBoundPlaceholder(playerFunction, function);
    }

    private static BiFunction<String[], Function<Context, Player>, Placeholder> ofStringData(DataKey<String> dataKey) {
        return (tokens, playerFunction) -> {
            if (tokens.length == 0) {
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).orElse(""));
            } else {
                int length = Integer.valueOf(tokens[0]);
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(s -> (s.length() > length ? s.substring(0, length) : s)).orElse(""));
            }
        };
    }

    private static BiFunction<String[], Function<Context, Player>, Placeholder> ofIntData(DataKey<Integer> dataKey) {
        return (tokens, playerFunction) -> {
            if (tokens.length == 0) {
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(Object::toString).orElse(""));
            } else {
                String format = "%0" + tokens[0] + "d";
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(i -> String.format(format, i)).orElse(""));
            }
        };
    }

    private static BiFunction<String[], Function<Context, Player>, Placeholder> ofIntFunction(Function<Player, Integer> function) {
        return (tokens, playerFunction) -> {
            if (tokens.length == 0) {
                return new PlayerBoundPlaceholder(playerFunction, player -> function.apply(player).toString());
            } else {
                String format = "%0" + tokens[0] + "d";
                return new PlayerBoundPlaceholder(playerFunction, player -> String.format(format, function.apply(player)));
            }
        };
    }

    private static BiFunction<String[], Function<Context, Player>, Placeholder> ofDoubleData(DataKey<Double> dataKey) {
        return (tokens, playerFunction) -> {
            if (tokens.length == 0) {
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(Object::toString).orElse(""));
            } else {
                String format = "%0" + tokens[0] + "f";
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(i -> String.format(format, i)).orElse(""));
            }
        };
    }

    private static BiFunction<String[], Function<Context, Player>, Placeholder> ofFloatData(DataKey<Float> dataKey) {
        return (tokens, playerFunction) -> {
            if (tokens.length == 0) {
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(Object::toString).orElse(""));
            } else {
                String format = "%0" + tokens[0] + "f";
                return new PlayerBoundPlaceholder(playerFunction, player -> player.getOpt(dataKey).map(i -> String.format(format, i)).orElse(""));
            }
        };
    }

    public static SimpleDateFormat getFormat(String pattern) {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setTimeZone(BungeeTabListPlus.getInstance().getConfig().getTimeZone());
        return format;
    }
}
