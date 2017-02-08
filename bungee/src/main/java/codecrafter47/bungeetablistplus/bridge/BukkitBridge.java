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
package codecrafter47.bungeetablistplus.bridge;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.DataStreamUtils;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.common.util.RateLimitedExecutor;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.data.TrackingDataCache;
import codecrafter47.bungeetablistplus.placeholder.Placeholder;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;
import com.google.common.collect.Lists;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import lombok.SneakyThrows;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class BukkitBridge implements Listener {

    private static final TypeAdapterRegistry typeAdapterRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    private static final RateLimitedExecutor rlExecutor = new RateLimitedExecutor(5000);

    private final BungeeTabListPlus plugin;

    private final Map<String, ServerBridgeDataCache> serverInformation = new HashMap<>();

    private final Set<String> registeredThirdPartyVariables = new HashSet<>();
    private final ReentrantLock thirdPartyVariablesLock = new ReentrantLock();

    private final UUID proxyId = UUID.randomUUID();

    private final NetDataKeyIdMap idMap = new NetDataKeyIdMap();

    public BukkitBridge(BungeeTabListPlus plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin.getPlugin(), this);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), () -> plugin.runInMainThread(this::checkForThirdPartyVariables), 2, 2, TimeUnit.SECONDS);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::requestMissingServerData, 1, 1, TimeUnit.SECONDS);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::removeObsoleteServerConnections, 5, 5, TimeUnit.SECONDS);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::tryInitConnections, 5, 5, TimeUnit.SECONDS);
    }

    private void checkForThirdPartyVariables() {
        try {
            for (ServerInfo serverInfo : plugin.getProxy().getServers().values()) {
                List<String> variables = getServerDataCache(serverInfo.getName()).get(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES);
                if (variables != null) {
                    thirdPartyVariablesLock.lock();
                    try {
                        for (String variable : variables) {
                            if (!registeredThirdPartyVariables.contains(variable)) {
                                Placeholder.remoteThirdPartyDataKeys.put(variable, BTLPDataKeys.createThirdPartyVariableDataKey(variable));
                                // this implicitly causes a reload
                                plugin.registerPlaceholderProvider0(new PlaceholderProvider() {
                                    @Override
                                    public void setup() {
                                        bind(variable).to(context -> ((Player) context.getPlayer()).getOpt(BTLPDataKeys.createThirdPartyVariableDataKey(variable)).orElse(""));
                                    }
                                });
                                registeredThirdPartyVariables.add(variable);
                            }
                        }
                    } catch (Throwable th) {
                        plugin.getLogger().log(Level.SEVERE, "Unexpected exception", th);
                    } finally {
                        thirdPartyVariablesLock.unlock();
                    }
                }
            }
        } catch (ConcurrentModificationException ignored) {
            // The map returned from ProxyServer#getServers is mutable
            // If it is mutated while we are iterating over its values
            // an exception is thrown
        }
    }

    private void requestMissingServerData() {
        for (ServerBridgeDataCache cache : serverInformation.values()) {
            cache.requestMissingData();
        }
    }

    private void removeObsoleteServerConnections() {
        for (ServerBridgeDataCache cache : serverInformation.values()) {
            cache.removeObsoleteConnections();
        }
    }

    private void tryInitConnections() {
        // Usually the handshake is initiated by the server, but in case this fails, try again.
        for (ConnectedPlayer player : BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayers()) {
            if (player.getBridgeDataCache().connection == null) {
                Server server = player.getPlayer().getServer();
                if (server != null && server.isConnected()) {
                    initializeHandshake(server);
                }
            }
        }
    }

    public PlayerBridgeDataCache createDataCacheForPlayer(ConnectedPlayer player) {
        return new PlayerBridgeDataCache();
    }

    public DataHolder getServerDataHolder(String server) {
        DataHolder data = serverInformation.get(server);
        return data != null ? data : NullDataHolder.INSTANCE;
    }

    private ServerBridgeDataCache getServerDataCache(String serverName) {
        if (!serverInformation.containsKey(serverName)) {
            serverInformation.putIfAbsent(serverName, new ServerBridgeDataCache());
        }
        return serverInformation.get(serverName);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getReceiver() instanceof ProxiedPlayer && event.getSender() instanceof Server) {

            ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
            Server server = (Server) event.getSender();

            if (event.getTag().equals(BridgeProtocolConstants.CHANNEL)) {
                event.setCancelled(true);

                ConnectedPlayer connectedPlayer = plugin.getConnectedPlayerManager().getPlayerIfPresent(player);
                if (connectedPlayer != null) {

                    PlayerBridgeDataCache cache = connectedPlayer.getBridgeDataCache();

                    DataInput input = new DataInputStream(new ByteArrayInputStream(event.getData()));

                    try {
                        int messageId = input.readUnsignedByte();

                        switch (messageId) {
                            case BridgeProtocolConstants.MESSAGE_ID_SERVER_HANDSHAKE:

                                if (cache.connection != null) {
                                    disableConnection(cache);
                                }
                                cache.connection = server;
                                getServerDataCache(server.getInfo().getName()).addConnection(server);
                                ArrayList<DataKey<?>> keys = Lists.newArrayList(cache.getQueriedKeys());

                                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                                data.writeByte(BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_DATA);
                                data.writeInt(keys.size());
                                for (DataKey<?> key : keys) {
                                    DataStreamUtils.writeDataKey(data, key);
                                    data.writeInt(idMap.getNetId(key));
                                }
                                server.sendData(BridgeProtocolConstants.CHANNEL, data.toByteArray());
                                break;
                            case BridgeProtocolConstants.MESSAGE_ID_SERVER_UPDATE_DATA:

                                if (cache.connection == server) {
                                    onDataReceived(cache, input, input.readInt());
                                }

                                break;
                            case BridgeProtocolConstants.MESSAGE_ID_SERVER_UPDATE_SERVER_DATA:

                                if (cache.connection == server) {
                                    ServerBridgeDataCache serverDataCache = getServerDataCache(server.getInfo().getName());
                                    int revision = input.readInt();
                                    int size = input.readInt();
                                    if (size > 0 && revision == serverDataCache.lastRevision + 1) {
                                        onDataReceived(serverDataCache, input, size);
                                        serverDataCache.lastRevision = revision;
                                    } else if (size > 0 || revision > serverDataCache.lastRevision) {
                                        Server connection = serverDataCache.getConnection();
                                        if (connection != null) {
                                            ByteArrayDataOutput output = ByteStreams.newDataOutput();
                                            output.writeByte(BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_RESET_SERVER_DATA);
                                            connection.sendData(BridgeProtocolConstants.CHANNEL, output.toByteArray());
                                            serverDataCache.lastRevision = 0;
                                        }
                                    }
                                }

                                break;
                            case BridgeProtocolConstants.MESSAGE_ID_SERVER_DISABLE_CONNECTION:

                                disableConnection(cache);
                                serverInformation.get(server.getInfo().getName()).reset();
                                break;
                            case BridgeProtocolConstants.MESSAGE_ID_SERVER_ENABLE_CONNECTION:

                                UUID serverId = DataStreamUtils.readUUID(input);
                                ServerBridgeDataCache serverData = getServerDataCache(server.getInfo().getName());
                                if (!serverData.serverId.equals(serverId)) {
                                    serverData.serverId = serverId;
                                    serverData.reset();
                                }

                                initializeHandshake(server);
                                break;
                            case BridgeProtocolConstants.MESSAGE_ID_SERVER_OUTDATED:

                                rlExecutor.execute(() -> plugin.getLogger().warning("Bridge plugin on server " + server.getInfo().getName() + " is outdated."));
                                break;
                        }
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred while processing bridge data.", ex);
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void onDataReceived(DataCache cache, DataInput input, int size) throws IOException {
        if (size == 1) {
            int netId = input.readInt();
            DataKey<?> key = idMap.getKey(netId);

            if (key == null) {
                throw new AssertionError("Received unexpected data key net id " + netId);
            }

            boolean removed = input.readBoolean();

            if (removed) {

                plugin.runInMainThread(() -> cache.updateValue(key, null));
            } else {

                Object value = typeAdapterRegistry.getTypeAdapter(key.getType()).read(input);
                plugin.runInMainThread(() -> cache.updateValue((DataKey<Object>) key, value));
            }
        } else {
            Object[] update = new Object[size * 2];

            for (int i = 0; i < update.length; i += 2) {
                int netId = input.readInt();
                DataKey<?> key = idMap.getKey(netId);

                if (key == null) {
                    throw new AssertionError("Received unexpected data key net id " + netId);
                }

                boolean removed = input.readBoolean();
                Object value = null;

                if (!removed) {
                    value = typeAdapterRegistry.getTypeAdapter(key.getType()).read(input);
                }

                update[i] = key;
                update[i + 1] = value;
            }

            plugin.runInMainThread(() -> {
                for (int i = 0; i < update.length; i += 2) {
                    cache.updateValue((DataKey<Object>) update[i], update[i + 1]);
                }
            });
        }
    }

    @EventHandler
    public void onServerChange(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        ConnectedPlayer connectedPlayer = plugin.getConnectedPlayerManager().getPlayerIfPresent(player);
        if (connectedPlayer != null) {
            PlayerBridgeDataCache cache = connectedPlayer.getBridgeDataCache();
            disableConnection(cache);
        }
    }

    private void disableConnection(PlayerBridgeDataCache cache) {
        cache.connection = null;
        for (DataKey<?> key : cache.getQueriedKeys()) {
            cache.updateValue(key, null);
        }
    }

    @SneakyThrows
    private void initializeHandshake(Server server) {
        ByteArrayDataOutput data = ByteStreams.newDataOutput();
        data.writeByte(BridgeProtocolConstants.MESSAGE_ID_PROXY_HANDSHAKE);
        DataStreamUtils.writeUUID(data, proxyId);
        data.writeInt(BridgeProtocolConstants.VERSION);
        server.sendData(BridgeProtocolConstants.CHANNEL, data.toByteArray());
    }

    public class PlayerBridgeDataCache extends TrackingDataCache {
        private volatile Server connection = null;

        @Override
        @SneakyThrows
        protected <V> void onMissingData(DataKey<V> key) {
            super.onMissingData(key);
            if (connection != null) {
                ByteArrayDataOutput data = ByteStreams.newDataOutput();
                data.writeByte(BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_DATA);
                data.writeInt(1);
                DataStreamUtils.writeDataKey(data, key);
                data.writeInt(idMap.getNetId(key));
                connection.sendData(BridgeProtocolConstants.CHANNEL, data.toByteArray());
            }
        }
    }

    public class ServerBridgeDataCache extends TrackingDataCache {
        private final Set<DataKey<?>> receivedData = new CopyOnWriteArraySet<>();
        private ReferenceSet<Server> connections = new ReferenceOpenHashSet<>();

        private UUID serverId = UUID.randomUUID();

        private int lastRevision = 0;

        private synchronized void addConnection(Server server) {
            connections.add(server);
        }

        @Nullable
        private synchronized Server getConnection() {
            ObjectIterator<Server> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Server server = iterator.next();
                if (server.isConnected()) {
                    return server;
                } else {
                    iterator.remove();
                }
            }
            return null;
        }

        private synchronized void removeObsoleteConnections() {
            ObjectIterator<Server> iterator = connections.iterator();
            while (iterator.hasNext()) {
                Server server = iterator.next();
                if (!server.isConnected()) {
                    iterator.remove();
                }
            }
        }

        @Override
        public <T> void updateValue(DataKey<T> dataKey, T object) {
            receivedData.add(dataKey);
            super.updateValue(dataKey, object);
        }

        @SneakyThrows
        private void requestMissingData() {
            if (getQueriedKeys().size() > receivedData.size()) {
                Server connection = getConnection();
                if (connection != null) {
                    List<DataKey<?>> keys = new LinkedList<>();
                    for (DataKey<?> key : getQueriedKeys()) {
                        if (!receivedData.contains(key)) {
                            keys.add(key);
                        }
                    }

                    ByteArrayDataOutput data = ByteStreams.newDataOutput();
                    data.writeByte(BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_SERVER_DATA);
                    data.writeInt(keys.size());
                    for (DataKey<?> key : keys) {
                        DataStreamUtils.writeDataKey(data, key);
                        data.writeInt(idMap.getNetId(key));
                    }
                    connection.sendData(BridgeProtocolConstants.CHANNEL, data.toByteArray());
                }
            }
        }

        private synchronized void reset() {
            connections.clear();
            for (DataKey<?> key : getQueriedKeys()) {
                super.updateValue(key, null);
            }
            receivedData.clear();
        }
    }
}
