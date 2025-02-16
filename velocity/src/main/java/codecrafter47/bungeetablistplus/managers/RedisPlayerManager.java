/*
 *     Copyright (C) 2025 proferabg
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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.DataStreamUtils;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.data.BTLPDataTypes;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import codecrafter47.bungeetablistplus.player.RedisPlayer;
import codecrafter47.bungeetablistplus.util.ProxyServer;
import com.google.common.collect.Sets;
import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.imaginarycode.minecraft.redisbungee.RedisBungeeAPI;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import com.velocitypowered.api.event.Subscribe;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyRegistry;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.sponge.api.SpongeData;
import de.codecrafter47.data.velocity.api.VelocityData;
import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import io.netty.util.concurrent.EventExecutor;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RedisPlayerManager implements PlayerProvider {

    private static final TypeAdapterRegistry typeRegistry = TypeAdapterRegistry.of(
            TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS,
            BTLPDataTypes.REGISTRY);

    private static final DataKeyRegistry keyRegistry = DataKeyRegistry.of(
            MinecraftData.class,
            BukkitData.class,
            SpongeData.class,
            VelocityData.class,
            BTLPDataKeys.class,
            BTLPVelocityDataKeys.class);

    private static String CHANNEL_REQUEST_DATA_OLD = "btlp-data-request";
    private static String CHANNEL_DATA_OLD = "btlp-data";
    private static String CHANNEL_DATA_REQUEST = "btlp-data-req";
    private static String CHANNEL_DATA_UPDATE = "btlp-data-upd";

    private final Map<UUID, RedisPlayer> byUUID = new ConcurrentHashMap<>();
    private final VelocityPlayerProvider velocityPlayerProvider;
    private final BungeeTabListPlus plugin;
    private final EventExecutor mainThread;
    private final Logger logger;
    private final Set<Listener> listeners = new ReferenceOpenHashSet<>();

    private boolean redisBungeeAPIError = false;

    private final Consumer<String> missingDataKeyLogger = new Consumer<String>() {

        private final Set<String> missingKeys = Sets.newConcurrentHashSet();

        @Override
        public void accept(String id) {
            if (missingKeys.add(id)) {
                logger.warning("Missing data key with id " + id + ". Is the plugin up-to-date?");
            }
        }
    };

    private boolean redisConnectionSuccessful = false;

    public RedisPlayerManager(VelocityPlayerProvider velocityPlayerProvider, BungeeTabListPlus plugin, Logger logger) {
        this.velocityPlayerProvider = velocityPlayerProvider;
        this.plugin = plugin;
        this.logger = logger;
        this.mainThread = plugin.getMainThreadExecutor();

        RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(CHANNEL_REQUEST_DATA_OLD, CHANNEL_DATA_OLD);
        RedisBungeeAPI.getRedisBungeeApi().registerPubSubChannels(CHANNEL_DATA_REQUEST, CHANNEL_DATA_UPDATE);

        plugin.getProxy().getScheduler().buildTask(plugin, this::updatePlayers).delay(5, TimeUnit.SECONDS).repeat(5, TimeUnit.SECONDS).schedule();

        plugin.getProxy().getEventManager().register(plugin, this);
    }

    @Override
    public Collection<RedisPlayer> getPlayers() {
        return byUUID.values();
    }

    @Subscribe
    @SuppressWarnings("unchecked")
    public void onRedisMessage(PubSubMessageEvent event) {
        String channel = event.getChannel();
        if (channel.equals(CHANNEL_DATA_REQUEST)) {
            ByteArrayDataInput input = ByteStreams.newDataInput(Base64.getDecoder().decode(event.getMessage()));
            try {
                UUID uuid = DataStreamUtils.readUUID(input);

                com.velocitypowered.api.proxy.Player Player = ProxyServer.getInstance().getPlayer(uuid).orElse(null);
                if (Player != null) {
                    VelocityPlayer player = velocityPlayerProvider.getPlayerIfPresent(Player);
                    if (player != null) {
                        DataKey<?> key = DataStreamUtils.readDataKey(input, keyRegistry, missingDataKeyLogger);

                        if (key != null) {
                            player.addDataChangeListener((DataKey<Object>) key, new DataChangeListener(player, (DataKey<Object>) key));
                            updateData(uuid, (DataKey<Object>) key, player.get(key));
                        }

                    }
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Unexpected error reading redis message", ex);
            }
        } else if (channel.equals(CHANNEL_DATA_UPDATE)) {
            ByteArrayDataInput input = ByteStreams.newDataInput(Base64.getDecoder().decode(event.getMessage()));
            try {
                UUID uuid = DataStreamUtils.readUUID(input);

                RedisPlayer player = byUUID.get(uuid);
                if (player != null) {
                    DataCache cache = player.getData();
                    DataKey<?> key = DataStreamUtils.readDataKey(input, keyRegistry, missingDataKeyLogger);

                    if (key != null) {
                        boolean removed = input.readBoolean();

                        if (removed) {

                            mainThread.execute(() -> cache.updateValue(key, null));
                        } else {

                            Object value = typeRegistry.getTypeAdapter(key.getType()).read(input);
                            mainThread.execute(() -> cache.updateValue((DataKey<Object>) key, value));
                        }
                    }
                }
            } catch (IOException ex) {
                logger.log(Level.SEVERE, "Unexpected error reading redis message", ex);
            }
        } else if (channel.equals(CHANNEL_DATA_OLD) || channel.equals(CHANNEL_REQUEST_DATA_OLD)) {
            logger.warning("BungeeTabListPlus on at least one proxy in your network is outdated.");
        }
    }

    private void updatePlayers() {
        Set<UUID> playersOnline;
        try {
            playersOnline = RedisBungeeAPI.getRedisBungeeApi().getPlayersOnline();
        } catch (Throwable th) {
            if (!redisBungeeAPIError) {
                logger.log(Level.WARNING, "Error using RedisBungee API", th);
                redisBungeeAPIError = true;
            }
            return;
        }
        redisBungeeAPIError = false;

        // fetch names for new players
        Map<UUID, String> uuidToNameMap = new Object2ObjectOpenHashMap<>();
        for (UUID uuid : playersOnline) {
            if (!byUUID.containsKey(uuid) && ProxyServer.getInstance().getPlayer(uuid) == null) {
                try {
                    uuidToNameMap.put(uuid, RedisBungeeAPI.getRedisBungeeApi().getNameFromUuid(uuid));
                } catch (Throwable ex) {
                    logger.log(Level.WARNING, "Error while using RedisBungee API", ex);
                }
            }
        }

        redisConnectionSuccessful = true;

        try {
            mainThread.submit(() -> {
                // remove players which have gone offline
                for (Iterator<UUID> iterator = byUUID.keySet().iterator(); iterator.hasNext(); ) {
                    UUID uuid = iterator.next();
                    if (!playersOnline.contains(uuid) || ProxyServer.getInstance().getPlayer(uuid) != null) {
                        RedisPlayer redisPlayer = byUUID.get(uuid);
                        iterator.remove();
                        listeners.forEach(listener -> listener.onPlayerRemoved(redisPlayer));
                    }
                }

                // add new players
                for (UUID uuid : uuidToNameMap.keySet()) {
                    if (!byUUID.containsKey(uuid) && ProxyServer.getInstance().getPlayer(uuid) == null) {
                        RedisPlayer redisPlayer = new RedisPlayer(uuid, uuidToNameMap.get(uuid));
                        byUUID.put(uuid, redisPlayer);
                        listeners.forEach(listener -> listener.onPlayerAdded(redisPlayer));
                    }
                }
            }).sync();
        } catch (InterruptedException ignored) {
        }
    }

    @Override
    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }

    public <T> void request(UUID uuid, DataKey<T> key) {
        try {
            ByteArrayDataOutput data = ByteStreams.newDataOutput();
            DataStreamUtils.writeUUID(data, uuid);
            DataStreamUtils.writeDataKey(data, key);
            RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_DATA_REQUEST, Base64.getEncoder().encodeToString(data.toByteArray()));
            redisBungeeAPIError = false;
        } catch (RuntimeException ex) {
            if (!redisBungeeAPIError) {
                logger.log(Level.WARNING, "Error using RedisBungee API", ex);
                redisBungeeAPIError = true;
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to request data", th);
        }
    }

    private <T> void updateData(UUID uuid, DataKey<T> key, T value) {
        try {
            ByteArrayDataOutput data = ByteStreams.newDataOutput();
            DataStreamUtils.writeUUID(data, uuid);
            DataStreamUtils.writeDataKey(data, key);
            data.writeBoolean(value == null);
            if (value != null) {
                typeRegistry.getTypeAdapter(key.getType()).write(data, value);
            }
            RedisBungeeAPI.getRedisBungeeApi().sendChannelMessage(CHANNEL_DATA_UPDATE, Base64.getEncoder().encodeToString(data.toByteArray()));
        } catch (RuntimeException ex) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "RedisBungee Error", ex);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to send data", th);
        }
    }

    private class DataChangeListener implements Runnable {
        private final Player player;
        private final DataKey<Object> dataKey;

        private DataChangeListener(Player player, DataKey<Object> dataKey) {
            this.player = player;
            this.dataKey = dataKey;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DataChangeListener that = (DataChangeListener) o;

            return player.equals(that.player) && dataKey.equals(that.dataKey);

        }

        @Override
        public int hashCode() {
            int result = player.hashCode();
            result = 31 * result + dataKey.hashCode();
            return result;
        }

        @Override
        public void run() {
            RedisPlayerManager.this.updateData(player.getUniqueID(), dataKey, player.get(dataKey));
        }
    }
}
