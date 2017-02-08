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
package codecrafter47.bungeetablistplus.bukkitbridge;

import codecrafter47.bungeetablistplus.api.bukkit.BungeeTabListPlusBukkitAPI;
import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import codecrafter47.bungeetablistplus.bukkitbridge.placeholderapi.PlaceholderAPIHook;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.DataStreamUtils;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.common.util.RateLimitedExecutor;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.codecrafter47.data.api.*;
import de.codecrafter47.data.bukkit.AbstractBukkitDataAccess;
import de.codecrafter47.data.bukkit.PlayerDataAccess;
import de.codecrafter47.data.bukkit.ServerDataAccess;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRegisterChannelEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.logging.Level;

public class BukkitBridge extends BungeeTabListPlusBukkitAPI implements Listener {

    private static final TypeAdapterRegistry typeRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    private static final DataKeyRegistry keyRegistry = DataKeyRegistry.of(
            MinecraftData.class,
            BukkitData.class,
            BungeeData.class,
            BTLPDataKeys.class);

    private static final RateLimitedExecutor rlExecutor = new RateLimitedExecutor(5000);

    private final UUID serverId = UUID.randomUUID();

    private final Plugin plugin;

    private final Map<Player, PlayerBridgeData> playerData = new ConcurrentHashMap<>();
    private final Map<UUID, ServerBridgeData> serverData = new ConcurrentHashMap<>();

    private DataAccess<Player> playerDataAccess;
    private DataAccess<Server> serverDataAccess;

    private PlaceholderAPIHook placeholderAPIHook = null;

    private final ReadWriteLock apiLock = new ReentrantReadWriteLock();
    private final Map<String, Variable> variablesByName = new HashMap<>();
    private final Multimap<Plugin, Variable> variablesByPlugin = HashMultimap.create();

    private final Consumer<String> missingDataKeyLogger = null;
    /*
    private final Consumer<String> missingDataKeyLogger = new Consumer<String>() {

        private final Set<String> missingKeys = Sets.newConcurrentHashSet();

        @Override
        public void accept(String id) {
            if (missingKeys.add(id)) {
                plugin.getLogger().warning("Missing data key with id " + id + ". Is the plugin up-to-date?");
            }
        }
    };
    */

    public BukkitBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        try {
            Field field = BungeeTabListPlusBukkitAPI.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, this);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to initialize API", ex);
        }

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin,
                BridgeProtocolConstants.CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,
                BridgeProtocolConstants.CHANNEL, (string, player, bytes) -> {

                    DataInput input = new DataInputStream(new ByteArrayInputStream(bytes));

                    try {
                        int messageId = input.readUnsignedByte();

                        switch (messageId) {
                            case BridgeProtocolConstants.MESSAGE_ID_PROXY_HANDSHAKE:
                                UUID proxyId = DataStreamUtils.readUUID(input);
                                int protocolVersion = input.readInt();

                                if (protocolVersion > BridgeProtocolConstants.VERSION) {
                                    rlExecutor.execute(() -> plugin.getLogger().warning("BungeeTabListPlus_BukkitBridge is outdated."));
                                } else if (protocolVersion < BridgeProtocolConstants.VERSION) {
                                    rlExecutor.execute(() -> plugin.getLogger().warning("BungeeTabListPlus proxy plugin outdated."));
                                } else {
                                    playerData.put(player, new PlayerBridgeData(proxyId));
                                    serverData.computeIfAbsent(proxyId, uuid -> new ServerBridgeData());
                                    ByteArrayDataOutput data = ByteStreams.newDataOutput();
                                    data.writeByte(BridgeProtocolConstants.MESSAGE_ID_SERVER_HANDSHAKE);
                                    player.sendPluginMessage(plugin, BridgeProtocolConstants.CHANNEL, data.toByteArray());
                                }

                                break;

                            case BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_DATA:
                                BridgeData bridgeData = playerData.get(player);
                                if (bridgeData != null) {
                                    handleDataRequest(bridgeData, input);
                                }
                                break;

                            case BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_SERVER_DATA:
                                PlayerBridgeData playerBridgeData = playerData.get(player);
                                if (playerBridgeData != null) {
                                    bridgeData = serverData.get(playerBridgeData.proxyId);
                                    if (bridgeData != null) {
                                        handleDataRequest(bridgeData, input);
                                    }
                                }
                                break;

                            case BridgeProtocolConstants.MESSAGE_ID_PROXY_OUTDATED:
                                rlExecutor.execute(() -> plugin.getLogger().warning("BungeeTabListPlus proxy plugin outdated."));
                                break;

                            case BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_RESET_SERVER_DATA:
                                playerBridgeData = playerData.get(player);
                                if (playerBridgeData != null) {
                                    bridgeData = serverData.get(playerBridgeData.proxyId);
                                    if (bridgeData != null) {
                                        serverData.put(playerBridgeData.proxyId, new ServerBridgeData());
                                    }
                                }
                                break;

                            default:
                                plugin.getLogger().warning("Received unknown message id " + messageId);
                                break;
                        }
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred while processing a plugin message.", ex);
                    }
                });

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        updateDataHooks();

        // initialize bridge for players already on the server
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            ByteArrayDataOutput data = ByteStreams.newDataOutput();
            data.writeByte(BridgeProtocolConstants.MESSAGE_ID_SERVER_ENABLE_CONNECTION);
            try {
                DataStreamUtils.writeUUID(data, serverId);
            } catch (IOException e) {
                throw new AssertionError(e);
            }
            player.sendPluginMessage(plugin, BridgeProtocolConstants.CHANNEL, data.toByteArray());
        }

        // start update task
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            Map<UUID, Player> proxyIds = new HashMap<>();

            for (Map.Entry<Player, PlayerBridgeData> e : playerData.entrySet()) {
                Player player = e.getKey();
                PlayerBridgeData bridgeData = e.getValue();

                proxyIds.putIfAbsent(bridgeData.proxyId, player);

                int size = 0;

                for (CacheEntry entry : bridgeData.requestedData) {
                    Object value = playerDataAccess.get(entry.key, player);
                    entry.dirty = !Objects.equals(value, entry.value);
                    entry.value = value;

                    if (entry.dirty) {
                        size++;
                    }
                }

                if (size != 0) {
                    ByteArrayDataOutput data = ByteStreams.newDataOutput();
                    data.writeByte(BridgeProtocolConstants.MESSAGE_ID_SERVER_UPDATE_DATA);
                    data.writeInt(size);

                    for (CacheEntry entry : bridgeData.requestedData) {
                        if (entry.dirty) {
                            data.writeInt(entry.netId);
                            data.writeBoolean(entry.value == null);
                            if (entry.value != null) {
                                try {
                                    typeRegistry.getTypeAdapter((TypeToken<Object>) entry.key.getType()).write(data, entry.value);
                                } catch (java.io.IOException e1) {
                                    e1.printStackTrace();
                                }
                            }
                        }
                    }

                    player.sendPluginMessage(plugin, BridgeProtocolConstants.CHANNEL, data.toByteArray());
                }
            }

            for (Map.Entry<UUID, Player> e : proxyIds.entrySet()) {
                UUID proxyId = e.getKey();
                Player player = e.getValue();
                ServerBridgeData bridgeData = serverData.get(proxyId);

                if (bridgeData == null) {
                    continue;
                }

                bridgeData.lastUpdate = now;

                int size = 0;

                for (CacheEntry entry : bridgeData.requestedData) {
                    Object value = serverDataAccess.get(entry.key, plugin.getServer());
                    entry.dirty = !Objects.equals(value, entry.value);
                    entry.value = value;

                    if (entry.dirty) {
                        size++;
                    }
                }

                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeByte(BridgeProtocolConstants.MESSAGE_ID_SERVER_UPDATE_SERVER_DATA);

                if (size > 0) {
                    bridgeData.revision++;
                }

                data.writeInt(bridgeData.revision);
                data.writeInt(size);

                for (CacheEntry entry : bridgeData.requestedData) {
                    if (entry.dirty) {
                        data.writeInt(entry.netId);
                        data.writeBoolean(entry.value == null);
                        if (entry.value != null) {
                            try {
                                typeRegistry.getTypeAdapter((TypeToken<Object>) entry.key.getType()).write(data, entry.value);
                            } catch (java.io.IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }

                player.sendPluginMessage(plugin, BridgeProtocolConstants.CHANNEL, data.toByteArray());
            }

            for (Iterator<ServerBridgeData> iterator = serverData.values().iterator(); iterator.hasNext(); ) {
                ServerBridgeData data = iterator.next();
                if (now - data.lastUpdate > 900000) {
                    iterator.remove();
                }
            }

        }, 20, 20);
    }

    private void handleDataRequest(BridgeData bridgeData, DataInput input) throws IOException {
        int size = input.readInt();
        for (int i = 0; i < size; i++) {
            DataKey<?> key = DataStreamUtils.readDataKey(input, keyRegistry, missingDataKeyLogger);
            int keyNetId = input.readInt();

            if (key != null) {
                bridgeData.addRequest(key, keyNetId);
            }
        }
    }

    private void updateDataHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIHook = new PlaceholderAPIHook(plugin);
        } else {
            placeholderAPIHook = null;
        }

        if (placeholderAPIHook != null) {
            playerDataAccess = JoinedDataAccess.of(new PlayerDataAccess(plugin), new ThirdPartyVariablesAccess(), placeholderAPIHook.getDataAccess());
        } else {
            playerDataAccess = JoinedDataAccess.of(new PlayerDataAccess(plugin), new ThirdPartyVariablesAccess());
        }
        serverDataAccess = JoinedDataAccess.of(new ServerDataAccess(plugin), new BTLPServerDataKeyAccess());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        playerData.remove(event.getPlayer());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        updateDataHooks();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        updateDataHooks();
    }

    @EventHandler
    @SneakyThrows
    public void onChannelRegistration(PlayerRegisterChannelEvent event) {
        if (BridgeProtocolConstants.CHANNEL.equals(event.getChannel())) {
            ByteArrayDataOutput data = ByteStreams.newDataOutput();
            data.writeByte(BridgeProtocolConstants.MESSAGE_ID_SERVER_ENABLE_CONNECTION);
            DataStreamUtils.writeUUID(data, serverId);
            event.getPlayer().sendPluginMessage(plugin, BridgeProtocolConstants.CHANNEL, data.toByteArray());
        }
    }

    @Override
    protected void registerVariable0(Plugin plugin, Variable variable) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(variable, "variable");
        apiLock.writeLock().lock();
        try {
            Preconditions.checkArgument(!variablesByName.containsKey(variable.getName()), "variable already registered");
            variablesByName.put(variable.getName(), variable);
            variablesByPlugin.put(plugin, variable);
        } finally {
            apiLock.writeLock().unlock();
        }
    }

    @Override
    protected void unregisterVariable0(Variable variable) {
        Preconditions.checkNotNull(variable, "variable");
        apiLock.writeLock().lock();
        try {
            Preconditions.checkArgument(variablesByName.remove(variable.getName(), variable), "variable not registered");
            variablesByPlugin.values().remove(variable);
        } finally {
            apiLock.writeLock().unlock();
        }
    }

    @Override
    protected void unregisterVariables0(Plugin plugin) {
        Preconditions.checkNotNull(plugin, "plugin");
        apiLock.writeLock().lock();
        try {
            for (Variable variable : variablesByPlugin.removeAll(plugin)) {
                variablesByName.remove(variable.getName());
            }

        } finally {
            apiLock.writeLock().unlock();
        }
    }

    private class ThirdPartyVariablesAccess extends AbstractBukkitDataAccess<Player> {
        ThirdPartyVariablesAccess() {
            super(BukkitBridge.this.plugin.getLogger(), BukkitBridge.this.plugin);
            addProvider(BTLPDataKeys.ThirdPartyPlaceholder, this::resolveVariable);
        }

        private String resolveVariable(Player player, DataKey<String> key) {
            apiLock.readLock().lock();
            try {
                Variable variable = variablesByName.get(key.getParameter());
                if (variable != null) {
                    String replacement = null;
                    try {
                        replacement = variable.getReplacement(player);
                    } catch (Throwable th) {
                        plugin.getLogger().log(Level.WARNING, "An exception occurred while resolving a variable provided by a third party plugin", th);
                    }
                    return replacement;
                }
                return null;
            } finally {
                apiLock.readLock().unlock();
            }
        }
    }

    private class BTLPServerDataKeyAccess extends AbstractBukkitDataAccess<Server> {
        BTLPServerDataKeyAccess() {
            super(BukkitBridge.this.plugin.getLogger(), BukkitBridge.this.plugin);
            addProvider(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES, server -> {
                apiLock.readLock().lock();
                try {
                    return Lists.newArrayList(variablesByName.keySet());
                } finally {
                    apiLock.readLock().unlock();
                }
            });
            addProvider(BTLPDataKeys.PLACEHOLDERAPI_PRESENT, server -> placeholderAPIHook != null);
            addProvider(BTLPDataKeys.PAPI_REGISTERED_PLACEHOLDER_PLUGINS, server -> placeholderAPIHook != null ? placeholderAPIHook.getRegisteredPlaceholderPlugins() : null);
        }
    }

    @RequiredArgsConstructor
    private static class CacheEntry {
        private final DataKey<?> key;
        private final int netId;
        private Object value = null;
        private boolean dirty = false;
    }

    private static class BridgeData {
        protected final List<CacheEntry> requestedData = new CopyOnWriteArrayList<>();

        private void addRequest(DataKey<?> key, int netId) {
            for (CacheEntry registration : requestedData) {
                if (Objects.equals(registration.key, key)) {
                    return;
                }
            }

            requestedData.add(new CacheEntry(key, netId));
        }
    }

    private static class PlayerBridgeData extends BridgeData {
        private UUID proxyId;

        public PlayerBridgeData(UUID proxyId) {
            this.proxyId = proxyId;
        }
    }

    private static class ServerBridgeData extends BridgeData {
        private int revision = 0;
        private long lastUpdate;
    }
}
