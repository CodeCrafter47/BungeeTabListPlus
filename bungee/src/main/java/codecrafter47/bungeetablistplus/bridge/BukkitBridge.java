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
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.DataStreamUtils;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.common.util.RateLimitedExecutor;
import codecrafter47.bungeetablistplus.data.TrackingDataCache;
import codecrafter47.bungeetablistplus.placeholder.Placeholder;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import it.unimi.dsi.fastutil.objects.ReferenceSet;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.DataOutput;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class BukkitBridge implements Listener {

    private static final TypeAdapterRegistry typeAdapterRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    private static final RateLimitedExecutor rlExecutor = new RateLimitedExecutor(5000);

    @Nonnull
    private final BungeeTabListPlus plugin;

    private final Map<ProxiedPlayer, PlayerConnectionInfo> playerPlayerConnectionInfoMap = new ConcurrentHashMap<>();
    private final Map<String, ServerBridgeDataCache> serverInformation = new ConcurrentHashMap<>();

    private final Set<String> registeredThirdPartyVariables = new HashSet<>();
    private final Set<String> registeredThirdPartyServerVariables = new HashSet<>();
    private final ReentrantLock thirdPartyVariablesLock = new ReentrantLock();

    private final int proxyIdentifier = ThreadLocalRandom.current().nextInt();

    private final NetDataKeyIdMap idMap = new NetDataKeyIdMap();

    public BukkitBridge(@Nonnull BungeeTabListPlus plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin.getPlugin(), this);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), () -> plugin.runInMainThread(this::checkForThirdPartyVariables), 2, 2, TimeUnit.SECONDS);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::sendIntroducePackets, 100, 100, TimeUnit.MILLISECONDS);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::resendUnconfirmedMessages, 2, 2, TimeUnit.SECONDS);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::removeObsoleteServerConnections, 5, 5, TimeUnit.SECONDS);
    }

    @EventHandler
    public void onPlayerConnect(PostLoginEvent event) {
        playerPlayerConnectionInfoMap.put(event.getPlayer(), new PlayerConnectionInfo());
    }

    @EventHandler
    public void onServerChange(ServerConnectedEvent event) {
        PlayerConnectionInfo previous = playerPlayerConnectionInfoMap.put(event.getPlayer(), new PlayerConnectionInfo());
        if (previous != null) {
            PlayerBridgeDataCache playerBridgeData = previous.playerBridgeData;
            if (playerBridgeData != null) {
                synchronized (playerBridgeData) {
                    playerBridgeData.connection = null;
                    playerBridgeData.reset();
                }
            }
        }
    }

    @EventHandler
    public void onPlayerDisconnect(PlayerDisconnectEvent event) {
        playerPlayerConnectionInfoMap.remove(event.getPlayer());
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(BridgeProtocolConstants.CHANNEL)) {
            if (event.getReceiver() instanceof ProxiedPlayer && event.getSender() instanceof Server) {

                ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                Server server = (Server) event.getSender();

                event.setCancelled(true);

                DataInput input = new DataInputStream(new ByteArrayInputStream(event.getData()));

                try {
                    handlePluginMessage(player, server, input);
                } catch (Throwable e) {
                    rlExecutor.execute(() -> {
                        plugin.getLogger().log(Level.SEVERE, "Unexpected error: ", e);
                    });
                }
            } else {
                // hacking attempt
                event.setCancelled(true);
            }
        }
    }

    private void handlePluginMessage(ProxiedPlayer player, Server server, DataInput input) throws IOException {
        PlayerConnectionInfo connectionInfo = playerPlayerConnectionInfoMap.get(player);

        if (connectionInfo == null) {
            return;
        }

        int messageId = input.readUnsignedByte();

        if (messageId == BridgeProtocolConstants.MESSAGE_ID_INTRODUCE) {

            int serverIdentifier = input.readInt();
            int protocolVersion = input.readInt();
            int minimumCompatibleProtocolVersion = input.readInt();
            String proxyPluginVersion = input.readUTF();

            int connectionId = serverIdentifier + proxyIdentifier;

            if (connectionId == connectionInfo.connectionIdentifier) {
                return;
            }

            if (BridgeProtocolConstants.VERSION < minimumCompatibleProtocolVersion) {
                rlExecutor.execute(() -> {
                    plugin.getLogger().log(Level.WARNING, "Incompatible version of BTLP on server " + server.getInfo().getName() + " detected: " + proxyPluginVersion);
                });
                return;
            }

            // reset connection state
            connectionInfo.connectionIdentifier = connectionId;
            connectionInfo.isConnectionValid = true;
            connectionInfo.nextIntroducePacketDelay = 1;
            connectionInfo.introducePacketDelay = 1;
            connectionInfo.hasReceived = false;
            connectionInfo.protocolVersion = Integer.min(BridgeProtocolConstants.VERSION, protocolVersion);

            ConnectedPlayer connectedPlayer = plugin.getConnectedPlayerManager().getPlayerIfPresent(player);
            if (connectedPlayer == null) {
                plugin.getLogger().severe("Internal error - Bridge functionality not available for " + player.getName());
            } else {
                connectionInfo.playerBridgeData = connectedPlayer.getBridgeDataCache();
                connectionInfo.playerBridgeData.setConnectionId(connectionId);
                connectionInfo.playerBridgeData.connection = server;
                connectionInfo.playerBridgeData.requestMissingData();
            }
            connectionInfo.serverBridgeData = getServerDataCache(server.getInfo().getName());
            connectionInfo.serverBridgeData.setConnectionId(connectionId);
            connectionInfo.serverBridgeData.addConnection(server);
            connectionInfo.serverBridgeData.requestMissingData();

            // send ACK 0
            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
            DataOutput output = new DataOutputStream(byteArrayOutput);

            output.writeByte(BridgeProtocolConstants.MESSAGE_ID_ACK);
            output.writeInt(connectionId);
            output.writeInt(0);

            byte[] message = byteArrayOutput.toByteArray();
            server.sendData(BridgeProtocolConstants.CHANNEL, message);
        } else {

            if (!connectionInfo.isConnectionValid) {
                return;
            }

            int connectionId = input.readInt();

            if (connectionId != connectionInfo.connectionIdentifier) {
                return;
            }

            connectionInfo.hasReceived = true;

            int sequenceNumber = input.readInt();

            BridgeData bridgeData;
            boolean isServerMessage;

            if ((messageId & 0x80) == 0) {
                bridgeData = connectionInfo.playerBridgeData;
                isServerMessage = false;
            } else {
                bridgeData = connectionInfo.serverBridgeData;
                isServerMessage = true;
            }

            if (bridgeData == null) {
                return;
            }

            messageId = messageId & 0x7f;

            synchronized (bridgeData) {

                if (messageId == BridgeProtocolConstants.MESSAGE_ID_ACK) {

                    int confirmed = sequenceNumber - bridgeData.lastConfirmed;

                    if (confirmed > bridgeData.messagesPendingConfirmation.size()) {
                        return;
                    }

                    while (confirmed-- > 0) {
                        bridgeData.lastConfirmed++;
                        bridgeData.messagesPendingConfirmation.remove();
                    }
                } else if (messageId == BridgeProtocolConstants.MESSAGE_ID_UPDATE_DATA) {

                    if (sequenceNumber > bridgeData.nextIncomingMessageId) {
                        // ignore messages from the future
                        return;
                    }

                    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
                    DataOutput output = new DataOutputStream(byteArrayOutput);

                    output.writeByte(BridgeProtocolConstants.MESSAGE_ID_ACK | (isServerMessage ? 0x80 : 0x00));
                    output.writeInt(connectionId);
                    output.writeInt(sequenceNumber);

                    byte[] message = byteArrayOutput.toByteArray();
                    server.sendData(BridgeProtocolConstants.CHANNEL, message);

                    if (sequenceNumber < bridgeData.nextIncomingMessageId) {
                        // ignore messages from the past after sending ACK
                        return;
                    }

                    bridgeData.nextIncomingMessageId++;

                    int size = input.readInt();
                    if (size > 0) {
                        onDataReceived(bridgeData, input, size);
                    }
                } else {
                    throw new IllegalArgumentException("Unexpected message id: " + messageId);
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

    /**
     * Sends introduce packets to the proxy to try to establish a connection.
     * <p>
     * Should be called periodically, recommended interval is 100ms to 1s.
     */
    private void sendIntroducePackets() {
        for (Map.Entry<ProxiedPlayer, PlayerConnectionInfo> entry : playerPlayerConnectionInfoMap.entrySet()) {
            Server server = entry.getKey().getServer();
            PlayerConnectionInfo connectionInfo = entry.getValue();
            if (server != null && !connectionInfo.hasReceived) {
                if (--connectionInfo.nextIntroducePacketDelay <= 0) {
                    connectionInfo.nextIntroducePacketDelay = connectionInfo.introducePacketDelay++;

                    try {

                        ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
                        DataOutput output = new DataOutputStream(byteArrayOutput);

                        output.writeByte(BridgeProtocolConstants.MESSAGE_ID_INTRODUCE);
                        output.writeInt(proxyIdentifier);
                        output.writeInt(BridgeProtocolConstants.VERSION);
                        output.writeInt(BridgeProtocolConstants.VERSION);
                        output.writeUTF(plugin.getPlugin().getDescription().getVersion());

                        byte[] message = byteArrayOutput.toByteArray();
                        server.sendData(BridgeProtocolConstants.CHANNEL, message);
                    } catch (Throwable th) {
                        rlExecutor.execute(() -> {
                            plugin.getLogger().log(Level.SEVERE, "Unexpected error", th);
                        });
                    }
                }
            }
        }
    }

    /**
     * Sends unconfirmed messages to the proxy yet another time, to ensure their arrival.
     * <p>
     * Should be called periodically, recommended interval is 1s to 10s.
     */
    private void resendUnconfirmedMessages() {
        long now = System.currentTimeMillis();

        for (Map.Entry<ProxiedPlayer, PlayerConnectionInfo> e : playerPlayerConnectionInfoMap.entrySet()) {
            ProxiedPlayer player = e.getKey();
            Server server = player.getServer();
            PlayerConnectionInfo connectionInfo = e.getValue();

            if (!connectionInfo.isConnectionValid) {
                continue;
            }

            BridgeData bridgeData = connectionInfo.playerBridgeData;

            if (bridgeData == null) {
                continue;
            }

            if (server != null && bridgeData.messagesPendingConfirmation.size() > 0 && (now > bridgeData.lastMessageSent + 1000 || bridgeData.messagesPendingConfirmation.size() > 5)) {
                for (byte[] message : bridgeData.messagesPendingConfirmation) {
                    server.sendData(BridgeProtocolConstants.CHANNEL, message);
                }
            }
        }

        for (ServerBridgeDataCache bridgeData : serverInformation.values()) {
            Server server = bridgeData.getConnection();

            if (server != null && bridgeData.messagesPendingConfirmation.size() > 0 && (now > bridgeData.lastMessageSent + 1000 || bridgeData.messagesPendingConfirmation.size() > 5)) {
                for (byte[] message : bridgeData.messagesPendingConfirmation) {
                    server.sendData(BridgeProtocolConstants.CHANNEL, message);
                }
            }
        }
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
                                plugin.reload();
                                registeredThirdPartyVariables.add(variable);
                            }
                        }
                    } catch (Throwable th) {
                        plugin.getLogger().log(Level.SEVERE, "Unexpected exception", th);
                    } finally {
                        thirdPartyVariablesLock.unlock();
                    }
                }
                List<String> serverVariables = getServerDataCache(serverInfo.getName()).get(BTLPDataKeys.REGISTERED_THIRD_PARTY_SERVER_VARIABLES);
                if (serverVariables != null) {
                    thirdPartyVariablesLock.lock();
                    try {
                        for (String variable : serverVariables) {
                            if (!registeredThirdPartyServerVariables.contains(variable)) {
                                Placeholder.remoteThirdPartyServerDataKeys.put(variable, BTLPDataKeys.createThirdPartyServerVariableDataKey(variable));
                                plugin.reload();
                                registeredThirdPartyServerVariables.add(variable);
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

    private void removeObsoleteServerConnections() {
        for (ServerBridgeDataCache cache : serverInformation.values()) {
            cache.removeObsoleteConnections();
        }
    }

    public PlayerBridgeDataCache createDataCacheForPlayer(@Nonnull ConnectedPlayer player) {
        return new PlayerBridgeDataCache();
    }

    public DataHolder getServerDataHolder(@Nonnull String server) {
        return getServerDataCache(server);
    }

    private ServerBridgeDataCache getServerDataCache(@Nonnull String serverName) {
        if (!serverInformation.containsKey(serverName)) {
            serverInformation.putIfAbsent(serverName, new ServerBridgeDataCache());
        }
        return serverInformation.get(serverName);
    }

    private static class PlayerConnectionInfo {
        boolean isConnectionValid = false;
        boolean hasReceived = false;
        int connectionIdentifier = 0;
        int protocolVersion = 0;
        int nextIntroducePacketDelay = 1;
        int introducePacketDelay = 1;
        @Nullable
        PlayerBridgeDataCache playerBridgeData = null;
        @Nullable
        ServerBridgeDataCache serverBridgeData = null;
    }

    private abstract class BridgeData extends TrackingDataCache {

        final Queue<byte[]> messagesPendingConfirmation = new ConcurrentLinkedQueue<>();
        int lastConfirmed = 0;
        int nextOutgoingMessageId = 1;
        int nextIncomingMessageId = 1;
        long lastMessageSent = 0;
        int connectionId;

        boolean requestAll = false;

        @Nullable
        abstract Server getConnection();

        @Override
        protected <V> void onMissingData(DataKey<V> key) {
            super.onMissingData(key);
            try {
                synchronized (this) {
                    Server connection = getConnection();
                    if (connection != null) {
                        ByteArrayDataOutput data = ByteStreams.newDataOutput();
                        data.writeByte(this instanceof PlayerBridgeDataCache ? BridgeProtocolConstants.MESSAGE_ID_REQUEST_DATA : BridgeProtocolConstants.MESSAGE_ID_REQUEST_DATA_SERVER);
                        data.writeInt(connectionId);
                        data.writeInt(nextOutgoingMessageId++);
                        data.writeInt(1);
                        DataStreamUtils.writeDataKey(data, key);
                        data.writeInt(idMap.getNetId(key));
                        byte[] message = data.toByteArray();
                        messagesPendingConfirmation.add(message);
                        lastMessageSent = System.currentTimeMillis();
                        connection.sendData(BridgeProtocolConstants.CHANNEL, message);
                    } else {
                        requestAll = true;
                    }
                }
            } catch (Throwable th) {
                rlExecutor.execute(() -> {
                    plugin.getLogger().log(Level.SEVERE, "Unexpected exception", th);
                });
                requestAll = true;
            }
        }

        void setConnectionId(int connectionId) {
            if (this.connectionId != connectionId) {
                reset();
            }
            this.connectionId = connectionId;
        }

        void reset() {
            synchronized (this) {
                requestAll = true;
                messagesPendingConfirmation.clear();
                lastConfirmed = 0;
                nextOutgoingMessageId = 1;
                nextIncomingMessageId = 1;
                lastMessageSent = 0;
                Collection<DataKey<?>> queriedKeys = new ArrayList<>(getQueriedKeys());
                plugin.runInMainThread(() -> {
                    for (DataKey<?> key : queriedKeys) {
                        updateValue(key, null);
                    }
                });
            }
        }

        void requestMissingData() throws IOException {
            synchronized (this) {
                if (requestAll) {
                    Server connection = getConnection();
                    if (connection != null) {
                        List<DataKey<?>> keys = new ArrayList<>(getQueriedKeys());

                        ByteArrayDataOutput data = ByteStreams.newDataOutput();
                        data.writeByte(this instanceof PlayerBridgeDataCache ? BridgeProtocolConstants.MESSAGE_ID_REQUEST_DATA : BridgeProtocolConstants.MESSAGE_ID_REQUEST_DATA_SERVER);
                        data.writeInt(connectionId);
                        data.writeInt(nextOutgoingMessageId++);
                        data.writeInt(keys.size());
                        for (DataKey<?> key : keys) {
                            DataStreamUtils.writeDataKey(data, key);
                            data.writeInt(idMap.getNetId(key));
                        }
                        byte[] message = data.toByteArray();
                        messagesPendingConfirmation.add(message);
                        lastMessageSent = System.currentTimeMillis();
                        connection.sendData(BridgeProtocolConstants.CHANNEL, message);
                    }
                    requestAll = false;
                }
            }
        }
    }

    public class PlayerBridgeDataCache extends BridgeData {
        @Nullable
        private volatile Server connection = null;

        @Override
        @Nullable
        Server getConnection() {
            return connection;
        }
    }

    private class ServerBridgeDataCache extends BridgeData {
        private final ReferenceSet<Server> connections = new ReferenceOpenHashSet<>();

        private void addConnection(@Nonnull Server server) {
            synchronized (this) {
                connections.add(server);
            }
        }

        @Override
        @Nullable
        Server getConnection() {
            synchronized (this) {
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
        }

        private void removeObsoleteConnections() {
            synchronized (this) {
                ObjectIterator<Server> iterator = connections.iterator();
                while (iterator.hasNext()) {
                    Server server = iterator.next();
                    if (!server.isConnected()) {
                        iterator.remove();
                    }
                }
            }
        }

        @Override
        void reset() {
            synchronized (this) {
                super.reset();
                connections.clear();
            }
        }
    }
}
