/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.data.AbstractCompositeDataProvider;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.data.ServerDataHolder;
import codecrafter47.bungeetablistplus.data.TrackingDataCache;
import codecrafter47.bungeetablistplus.handler.GetGamemodeLogic;
import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import codecrafter47.bungeetablistplus.util.IconUtil;
import codecrafter47.bungeetablistplus.util.MatchingStringsCollection;
import codecrafter47.bungeetablistplus.util.VelocityPlugin;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import de.codecrafter47.data.api.*;
import de.codecrafter47.data.velocity.AbstractVelocityDataAccess;
import de.codecrafter47.data.velocity.PlayerDataAccess;
import de.codecrafter47.taboverlay.config.misc.Unchecked;
import io.netty.util.concurrent.EventExecutor;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DataManager {

    private final API api;
    private final EventExecutor mainThreadExecutor;
    private final VelocityPlayerProvider velocityPlayerProvider;
    private final ServerStateManager serverStateManager;
    private final BukkitBridge bukkitBridge;

    private final DataAccess<Player> playerDataAccess;
    private final DataAccess<String> serverDataAccess;
    private final DataAccess<ProxyServer> proxyDataAccess;
    private final Map<String, AbstractCompositeDataProvider<?>> compositeDataProviders = new HashMap<>();

    private final Map<String, TrackingDataCache> serverData = new ConcurrentHashMap<>();
    private final Map<String, DataHolder> combinedServerData = new ConcurrentHashMap<>();

    @Getter
    private final TrackingDataCache proxyData = new TrackingDataCache();

    @Setter
    MatchingStringsCollection hiddenServers = new MatchingStringsCollection(Collections.emptyList());
    @Setter
    List<String> permanentlyHiddenPlayers = Collections.emptyList();

    public DataManager(API api, VelocityPlugin plugin, Logger logger, VelocityPlayerProvider velocityPlayerProvider, EventExecutor mainThreadExecutor, ServerStateManager serverStateManager, BukkitBridge bukkitBridge) {
        this.api = api;
        this.velocityPlayerProvider = velocityPlayerProvider;
        this.mainThreadExecutor = mainThreadExecutor;
        this.serverStateManager = serverStateManager;
        this.bukkitBridge = bukkitBridge;
        this.playerDataAccess = JoinedDataAccess.of(new PlayerDataAccess(plugin.getProxy(), plugin, logger), new LocalPlayerDataAccess(plugin, logger));
        this.serverDataAccess = new LocalServerDataAccess(plugin, logger);
        this.proxyDataAccess = new ProxyDataAccess(plugin, logger);

        plugin.getProxy().getScheduler().buildTask(plugin, () -> updateData(plugin.getProxy())).delay(1, TimeUnit.SECONDS).repeat(1, TimeUnit.SECONDS).schedule();
    }

    public LocalDataCache createDataCacheForPlayer(VelocityPlayer player) {
        return new LocalDataCache(player);
    }

    public DataHolder getServerDataHolder(@Nonnull String serverName) {
        if (!combinedServerData.containsKey(serverName)) {
            combinedServerData.put(serverName, new ServerDataHolder(getLocalServerDataHolder(serverName), bukkitBridge.getServerDataHolder(serverName)));
        }
        return combinedServerData.get(serverName);
    }

    public <T> void addCompositeDataProvider(@Nonnull AbstractCompositeDataProvider<T> provider) {
        compositeDataProviders.put(provider.getCompositeDataKey().getId(), provider);
    }

    private DataHolder getLocalServerDataHolder(@Nonnull String serverName) {
        if (!serverData.containsKey(serverName)) {
            serverData.putIfAbsent(serverName, new TrackingDataCache());
        }
        return serverData.get(serverName);
    }

    private void updateData(ProxyServer server) {
        for (VelocityPlayer player : velocityPlayerProvider.getPlayers()) {
            for (DataKey<?> dataKey : player.getLocalDataCache().getActiveKeys()) {
                if (playerDataAccess.provides(dataKey)) {
                    DataKey<Object> key = Unchecked.cast(dataKey);
                    updateIfNecessary(player.getLocalDataCache(), key, playerDataAccess.get(key, player.getPlayer()));
                }
            }
        }
        for (Map.Entry<String, TrackingDataCache> entry : serverData.entrySet()) {
            String serverName = entry.getKey();
            TrackingDataCache dataCache = entry.getValue();
            for (DataKey<?> dataKey : dataCache.getActiveKeys()) {
                DataKey<Object> key = Unchecked.cast(dataKey);
                updateIfNecessary(dataCache, key, serverDataAccess.get(key, serverName));
            }
        }
        for (DataKey<?> dataKey : proxyData.getActiveKeys()) {
            DataKey<Object> key = Unchecked.cast(dataKey);
            updateIfNecessary(proxyData, key, proxyDataAccess.get(key, server));
        }

    }

    private <T> void updateIfNecessary(DataCache data, DataKey<T> key, T value) {
        if (!Objects.equals(data.get(key), value)) {
            mainThreadExecutor.execute(() -> data.updateValue(key, value));
        }
    }

    private class LocalPlayerDataAccess extends AbstractVelocityDataAccess<Player> {

        LocalPlayerDataAccess(VelocityPlugin plugin, Logger logger) {
            super(plugin.getProxy(), plugin, logger);

            addProvider(BTLPVelocityDataKeys.DATA_KEY_GAMEMODE, p -> GetGamemodeLogic.getGameMode(p.getUniqueId()));
            addProvider(BTLPVelocityDataKeys.DATA_KEY_ICON, IconUtil::getIconFromPlayer);
            addProvider(BTLPVelocityDataKeys.ThirdPartyPlaceholderBungee, (player, dataKey) -> api.resolveCustomPlaceholder(dataKey.getParameter(), player));
            addProvider(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_PLAYER_CONFIG, (player, dataKey) -> permanentlyHiddenPlayers.contains(player.getUsername()) || permanentlyHiddenPlayers.contains(player.getUniqueId().toString()));

            if (plugin.getProxy().getPluginManager().getPlugin("redisbungee").isPresent()) {
                addProvider(BTLPVelocityDataKeys.DATA_KEY_RedisBungee_ServerId, player -> RedisBungeeAPI.getRedisBungeeApi().getServerId());
            }
        }
    }

    private class LocalServerDataAccess extends AbstractVelocityDataAccess<String> {

        LocalServerDataAccess(VelocityPlugin plugin, Logger logger) {
            super(plugin.getProxy(), plugin, logger);
            addProvider(BTLPVelocityDataKeys.DATA_KEY_SERVER_ONLINE, (serverName, dataKey) -> serverStateManager.isOnline(serverName));
            addProvider(BTLPVelocityDataKeys.DATA_KEY_IS_HIDDEN_SERVER_CONFIG, (serverName, dataKey) -> hiddenServers.contains(serverName));
            addProvider(BTLPVelocityDataKeys.ThirdPartyServerPlaceholderBungee, (serverName, dataKey) -> api.resolveCustomPlaceholderServer(dataKey.getParameter(), serverName));
            addProvider(BTLPVelocityDataKeys.DATA_KEY_ServerName, (serverName, dataKey) -> serverName);
        }
    }

    private class ProxyDataAccess extends AbstractVelocityDataAccess<ProxyServer> {

        ProxyDataAccess(VelocityPlugin plugin, Logger logger) {
            super(plugin.getProxy(), plugin, logger);
            addProvider(BTLPVelocityDataKeys.DATA_KEY_Server_Count, (proxy, dataKey) -> proxy.getAllServers().size());
            addProvider(BTLPVelocityDataKeys.DATA_KEY_Server_Count_Online, (proxy, dataKey) -> (int) proxy.getAllServers().stream().filter((key) -> serverStateManager.isOnline(key.getServerInfo().getName())).count());
        }
    }

    public class LocalDataCache extends TrackingDataCache {

        private final de.codecrafter47.taboverlay.config.player.Player player;

        private LocalDataCache(de.codecrafter47.taboverlay.config.player.Player player) {
            this.player = player;
        }

        @Override
        protected <T> void addActiveKey(DataKey<T> key) {
            AbstractCompositeDataProvider<?> compositeDataProvider = compositeDataProviders.get(key.getId());
            if (compositeDataProvider != null) {
                compositeDataProvider.onPlayerAdded(player, Unchecked.cast(key));
            } else {
                super.addActiveKey(key);
            }
        }

        @Override
        protected <T> void removeActiveKey(DataKey<T> key) {
            AbstractCompositeDataProvider<?> compositeDataProvider = compositeDataProviders.get(key.getId());
            if (compositeDataProvider != null) {
                compositeDataProvider.onPlayerRemoved(player, Unchecked.cast(key));
            } else {
                super.removeActiveKey(key);
            }
        }
    }
}
