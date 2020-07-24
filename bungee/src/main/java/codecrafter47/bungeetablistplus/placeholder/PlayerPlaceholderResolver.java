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

import codecrafter47.bungeetablistplus.cache.Cache;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.TypeToken;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.sponge.api.SpongeData;
import de.codecrafter47.taboverlay.config.context.Context;
import de.codecrafter47.taboverlay.config.expression.ExpressionUpdateListener;
import de.codecrafter47.taboverlay.config.expression.ToStringExpression;
import de.codecrafter47.taboverlay.config.expression.template.ExpressionTemplate;
import de.codecrafter47.taboverlay.config.placeholder.*;
import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.config.template.TemplateCreationContext;
import de.codecrafter47.taboverlay.config.view.AbstractActiveElement;
import lombok.val;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.function.Function;

public class PlayerPlaceholderResolver extends AbstractPlayerPlaceholderResolver {

    private final ServerPlaceholderResolver serverPlaceholderResolver;
    private final Cache cache;

    private final Set<String> placeholderAPIPluginPrefixes = Collections.synchronizedSet(new HashSet<>());
    private final Map<String, DataKey<String>> bridgeCustomPlaceholderDataKeys = Collections.synchronizedMap(new HashMap<>());
    private final Map<String, DataKey<String>> customPlaceholderDataKeys = Collections.synchronizedMap(new HashMap<>());

    private final Map<String, String> aliasMap = new HashMap<>();

    public PlayerPlaceholderResolver(ServerPlaceholderResolver serverPlaceholderResolver, Cache cache) {
        super();
        this.serverPlaceholderResolver = serverPlaceholderResolver;
        this.cache = cache;
        addPlaceholder("ping", create(BungeeData.BungeeCord_Ping));
        addPlaceholder("bungeecord_primary_group", create(BungeeData.BungeeCord_PrimaryGroup));
        addPlaceholder("bungeeperms_display", create(BungeeData.BungeePerms_DisplayPrefix));
        addPlaceholder("bungeeperms_prefix", create(BungeeData.BungeePerms_Prefix));
        addPlaceholder("bungeeperms_suffix", create(BungeeData.BungeePerms_Suffix));
        addPlaceholder("bungeeperms_primary_group", create(BungeeData.BungeePerms_PrimaryGroup));
        addPlaceholder("bungeeperms_primary_group_prefix", create(BungeeData.BungeePerms_PrimaryGroupPrefix));
        addPlaceholder("bungeeperms_user_prefix", create(BungeeData.BungeePerms_PlayerPrefix));
        addPlaceholder("bungeeperms_primary_group_rank", create(BungeeData.BungeePerms_Rank));
        addPlaceholder("bungeeperms_primary_group_weight", create(BungeeData.BungeePerms_Weight));
        addPlaceholder("luckpermsbungee_prefix", create(BungeeData.LuckPerms_Prefix));
        addPlaceholder("luckpermsbungee_suffix", create(BungeeData.LuckPerms_Suffix));
        addPlaceholder("luckpermsbungee_primary_group", create(BungeeData.LuckPerms_PrimaryGroup));
        addPlaceholder("luckpermsbungee_primary_group_weight", create(BungeeData.LuckPerms_Weight));
        addPlaceholder("client_version", create(BTLPBungeeDataKeys.DATA_KEY_CLIENT_VERSION));
        addPlaceholder("client_version_below_1_8", create(BTLPBungeeDataKeys.DATA_KEY_CLIENT_VERSION_BELOW_1_8));
        addPlaceholder("client_version_atleast_1_8", create(BTLPBungeeDataKeys.DATA_KEY_CLIENT_VERSION_BELOW_1_8, b -> !b, TypeToken.BOOLEAN));
        addPlaceholder("world", create(MinecraftData.World));
        addPlaceholder("team", create(MinecraftData.Team));
        addPlaceholder("vault_balance", create(MinecraftData.Economy_Balance));
        addPlaceholder("vault_balance2", create(MinecraftData.Economy_Balance, b -> {
            if (b == null) {
                return "";
            } else if (b >= 10_000_000) {
                return String.format("%1.0fM", b / 1_000_000);
            } else if (b >= 10_000) {
                return String.format("%1.0fK", b / 1_000);
            } else if (b >= 100) {
                return String.format("%1.0f", b);
            } else {
                return String.format("%1.2f", b);
            }
        }, TypeToken.STRING));
        addPlaceholder("multiverse_world_alias", create(BukkitData.Multiverse_WorldAlias));
        addPlaceholder("faction_name", create(BukkitData.Factions_FactionName));
        addPlaceholder("faction_member_count", create(BukkitData.Factions_FactionMembers));
        addPlaceholder("faction_online_member_count", create(BukkitData.Factions_OnlineFactionMembers));
        addPlaceholder("faction_at_current_location", create(BukkitData.Factions_FactionsWhere));
        addPlaceholder("faction_power", create(BukkitData.Factions_FactionPower));
        addPlaceholder("faction_player_power", create(BukkitData.Factions_PlayerPower));
        addPlaceholder("faction_rank", create(BukkitData.Factions_FactionsRank));
        addPlaceholder("SimpleClans_ClanName", create(BukkitData.SimpleClans_ClanName));
        addPlaceholder("SimpleClans_ClanMembers", create(BukkitData.SimpleClans_ClanMembers));
        addPlaceholder("SimpleClans_OnlineClanMembers", create(BukkitData.SimpleClans_OnlineClanMembers));
        addPlaceholder("SimpleClans_ClanTag", create(BukkitData.SimpleClans_ClanTag));
        addPlaceholder("SimpleClans_ClanTagLabel", create(BukkitData.SimpleClans_ClanTagLabel));
        addPlaceholder("SimpleClans_ClanColorTag", create(BukkitData.SimpleClans_ClanColorTag));
        addPlaceholder("vault_primary_group", create(MinecraftData.Permissions_PermissionGroup));
        addPlaceholder("vault_prefix", create(MinecraftData.Permissions_Prefix));
        addPlaceholder("vault_suffix", create(MinecraftData.Permissions_Suffix));
        addPlaceholder("vault_primary_group_prefix", create(MinecraftData.Permissions_PrimaryGroupPrefix));
        addPlaceholder("vault_player_prefix", create(MinecraftData.Permissions_PlayerPrefix));
        addPlaceholder("vault_primary_group_weight", create(MinecraftData.Permissions_PermissionGroupWeight));
        addPlaceholder("health", create(MinecraftData.Health));
        addPlaceholder("max_health", create(MinecraftData.MaxHealth));
        addPlaceholder("location_x", create(MinecraftData.PosX));
        addPlaceholder("location_y", create(MinecraftData.PosY));
        addPlaceholder("location_z", create(MinecraftData.PosZ));
        addPlaceholder("xp", create(MinecraftData.XP));
        addPlaceholder("total_xp", create(MinecraftData.TotalXP));
        addPlaceholder("level", create(MinecraftData.Level));
        addPlaceholder("player_points", create(BukkitData.PlayerPoints_Points));
        addPlaceholder("vault_currency", create(MinecraftData.Economy_CurrencyNameSingular));
        addPlaceholder("vault_currency_plural", create(MinecraftData.Economy_CurrencyNamePlural));
        addPlaceholder("tab_name", create(BukkitData.PlayerListName, (player, name) -> name == null ? player.getName() : name, TypeToken.STRING));
        addPlaceholder("display_name", create(MinecraftData.DisplayName, (player, name) -> name == null ? player.getName() : name, TypeToken.STRING));
        addPlaceholder("bungeecord_display_name", create(BungeeData.BungeeCord_DisplayName, (player, name) -> name == null ? player.getName() : name, TypeToken.STRING));
        addPlaceholder("session_duration_seconds", create(BungeeData.BungeeCord_SessionDuration, duration -> duration == null ? null : (int) (duration.getSeconds() % 60), TypeToken.INTEGER));
        addPlaceholder("session_duration_total_seconds", create(BungeeData.BungeeCord_SessionDuration, duration -> duration == null ? null : (int) duration.getSeconds(), TypeToken.INTEGER));
        addPlaceholder("session_duration_minutes", create(BungeeData.BungeeCord_SessionDuration, duration -> duration == null ? null : (int) ((duration.getSeconds() % 3600) / 60), TypeToken.INTEGER));
        addPlaceholder("session_duration_hours", create(BungeeData.BungeeCord_SessionDuration, duration -> duration == null ? null : (int) (duration.getSeconds() / 3600), TypeToken.INTEGER));
        addPlaceholder("essentials_afk", create(BukkitData.Essentials_IsAFK));
        addPlaceholder("is_hidden", create(BTLPBungeeDataKeys.DATA_KEY_IS_HIDDEN));
        addPlaceholder("gamemode", create(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE));
        addPlaceholder("bungeeonlinetime_seconds", create(BungeeData.BungeeOnlineTime_OnlineTime, duration -> duration == null ? null : (int) (duration.getSeconds() % 60), TypeToken.INTEGER));
        addPlaceholder("bungeeonlinetime_minutes", create(BungeeData.BungeeOnlineTime_OnlineTime, duration -> duration == null ? null : (int) ((duration.getSeconds() % 3600) / 60), TypeToken.INTEGER));
        addPlaceholder("bungeeonlinetime_hours", create(BungeeData.BungeeOnlineTime_OnlineTime, duration -> duration == null ? null : (int) (duration.getSeconds() / 3600), TypeToken.INTEGER));
        addPlaceholder("bungeeonlinetime_hours_of_24", create(BungeeData.BungeeOnlineTime_OnlineTime, duration -> duration == null ? null : (int) ((duration.getSeconds() / 3600) % 24), TypeToken.INTEGER));
        addPlaceholder("bungeeonlinetime_days", create(BungeeData.BungeeOnlineTime_OnlineTime, duration -> duration == null ? null : (int) (duration.getSeconds() / 86400), TypeToken.INTEGER));
        addPlaceholder("redisbungee_server_id", create(BTLPBungeeDataKeys.DATA_KEY_RedisBungee_ServerId));
        addPlaceholder("askyblock_island_level", create(BukkitData.ASkyBlock_IslandLevel));
        addPlaceholder("askyblock_island_name", create(BukkitData.ASkyBlock_IslandName));
        addPlaceholder("askyblock_team_leader", create(BukkitData.ASkyBlock_TeamLeader));
        addPlaceholder("paf_clans_clan_name", create(BungeeData.PAFClans_ClanName));
        addPlaceholder("paf_clans_clan_tag", create(BungeeData.PAFClans_ClanTag));
        addPlaceholder("paf_clans_clan_member_count", create(BungeeData.PAFClans_MemberCount));
        addPlaceholder("paf_clans_clan_online_member_count", create(BungeeData.PAFClans_OnlineMemberCount));
        addPlaceholder("paf_clans_is_leader", create(BungeeData.PAFClans_IsLeader));
        addPlaceholder("permission", this::resolvePermissionPlaceholder);
        addPlaceholder("nucleus_afk", create(SpongeData.Nucleus_IsAFK));
        addPlaceholder("nucleus_nick", create(SpongeData.Nucleus_Nick));

        // Server
        addPlaceholder("server", this::resolveServerPlaceholder);
    }

    @Nonnull
    private PlaceholderBuilder<?, ?> resolveServerPlaceholder(PlaceholderBuilder<Player, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws PlaceholderException {
        try {
            // The player's data holder should allow transparent access to the server data, so this is fine
            return serverPlaceholderResolver.resolve(builder.transformContext(c -> c), args, tcc);
        } catch (UnknownPlaceholderException e) {
            throw new PlaceholderException("Unknown placeholder");
        }
    }

    @Nonnull
    private PlaceholderBuilder<?, ?> resolvePermissionPlaceholder(PlaceholderBuilder<Player, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws PlaceholderException {
        if (args.isEmpty()) {
            throw new PlaceholderException("Use of permission placeholder lacks specification of specific permission");
        }
        if (args.get(0) instanceof PlaceholderArg.Text) {
            String permission = args.remove(0).getText();
            return builder.acquireData(new PlayerPlaceholderDataProviderSupplier<>(TypeToken.BOOLEAN, MinecraftData.permission(permission), (p, d) -> d), TypeToken.BOOLEAN);
        } else {
            ExpressionTemplate permission = args.remove(0).getExpression();
            Function<Context, Player> playerFunction = builder.getContextTransformation();
            return PlaceholderBuilder.create().acquireData(() -> new PermissionDataProvider(permission.instantiateWithStringResult(), playerFunction), TypeToken.BOOLEAN, builder.isRequiresViewerContext() || permission.requiresViewerContext());
        }
    }

    @Nonnull
    @Override
    public PlaceholderBuilder<?, ?> resolve(PlaceholderBuilder<Player, ?> builder, List<PlaceholderArg> args, TemplateCreationContext tcc) throws UnknownPlaceholderException, PlaceholderException {
        if (!args.isEmpty() && aliasMap.containsKey(args.get(0).getText())) {
            String replacement = aliasMap.get(args.get(0).getText());
            tcc.getErrorHandler().addWarning("Placeholder '" + args.get(0).getText() + "' has been deprecated. Use '" + replacement + "' instead.", null);
            args.set(0, new PlaceholderArg.Text(replacement));
        }
        try {
            return super.resolve(builder, args, tcc);
        } catch (UnknownPlaceholderException e) {
            if (!args.isEmpty()) {
                String id = args.get(0).getText().toLowerCase();
                PlaceholderBuilder<?, ?> result = null;
                if (customPlaceholderDataKeys.containsKey(id)) {
                    DataKey<String> dataKey = customPlaceholderDataKeys.get(id);
                    result = builder.acquireData(new PlayerPlaceholderDataProviderSupplier<>(TypeToken.STRING, dataKey, (player, replacement) -> replacement), TypeToken.STRING);
                } else if (bridgeCustomPlaceholderDataKeys.containsKey(id)) {
                    DataKey<String> dataKey = bridgeCustomPlaceholderDataKeys.get(id);
                    result = builder.acquireData(new PlayerPlaceholderDataProviderSupplier<>(TypeToken.STRING, dataKey, (player, replacement) -> replacement), TypeToken.STRING);
                } else {
                    for (String prefix : placeholderAPIPluginPrefixes) {
                        if (id.length() >= prefix.length() && id.substring(0, prefix.length()).equalsIgnoreCase(prefix)) {
                            id = args.remove(0).getText();
                            val resolver = create(BTLPDataKeys.createPlaceholderAPIDataKey("%" + id + "%"));
                            addPlaceholder(id, resolver);
                            return resolver.resolve(builder, args, tcc);
                        }
                    }
                }
                if (result == null) {
                    // prevent errors because bridge information has not been synced yet
                    if (cache.getCustomPlaceholdersBridge().contains(id)) {
                        result = builder.acquireData(new PlayerPlaceholderDataProviderSupplier<>(TypeToken.STRING, BTLPDataKeys.createThirdPartyVariableDataKey(id), (player, replacement) -> replacement), TypeToken.STRING);
                    } else {
                        for (String prefix : cache.getPAPIPrefixes()) {
                            if (id.length() >= prefix.length() && id.substring(0, prefix.length()).equalsIgnoreCase(prefix)) {
                                result = builder.acquireData(new PlayerPlaceholderDataProviderSupplier<>(TypeToken.STRING, BTLPDataKeys.createPlaceholderAPIDataKey("%" + id + "%"), (player, replacement) -> replacement), TypeToken.STRING);
                            }
                        }
                    }
                }
                if (result != null) {
                    args.remove(0);
                    return result;
                }
            }
            throw e;
        }
    }

    public void addPlaceholderAPIPluginPrefixes(Collection<String> prefixes) {
        placeholderAPIPluginPrefixes.addAll(prefixes);
    }

    public void addCustomPlaceholderDataKey(String id, DataKey<String> dataKey) {
        customPlaceholderDataKeys.put(id.toLowerCase(), dataKey);
    }

    public void addBridgeCustomPlaceholderDataKey(String id, DataKey<String> dataKey) {
        bridgeCustomPlaceholderDataKeys.put(id.toLowerCase(), dataKey);
    }

    private static class PermissionDataProvider extends AbstractActiveElement<Runnable> implements PlaceholderDataProvider<Context, Boolean>, ExpressionUpdateListener {
        private final ToStringExpression permission;
        private final Function<Context, Player> playerFunction;
        private DataKey<Boolean> permissionDataKey;

        private PermissionDataProvider(ToStringExpression permission, Function<Context, Player> playerFunction) {
            this.permission = permission;
            this.playerFunction = playerFunction;
        }

        @Override
        protected void onActivation() {
            permission.activate(getContext(), this);
            permissionDataKey = MinecraftData.permission(permission.evaluate());
            if (hasListener()) {
                playerFunction.apply(getContext()).addDataChangeListener(permissionDataKey, getListener());
            }
        }

        @Override
        protected void onDeactivation() {
            permission.deactivate();
            if (hasListener()) {
                playerFunction.apply(getContext()).removeDataChangeListener(permissionDataKey, getListener());
            }
        }

        @Override
        public Boolean getData() {
            return playerFunction.apply(getContext()).get(permissionDataKey);
        }

        @Override
        public void onExpressionUpdate() {
            if (hasListener()) {
                playerFunction.apply(getContext()).removeDataChangeListener(permissionDataKey, getListener());
            }
            permissionDataKey = MinecraftData.permission(permission.evaluate());
            if (hasListener()) {
                playerFunction.apply(getContext()).addDataChangeListener(permissionDataKey, getListener());
                getListener().run();
            }
        }
    }
}
