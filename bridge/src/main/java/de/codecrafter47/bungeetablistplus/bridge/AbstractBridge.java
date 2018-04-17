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

package de.codecrafter47.bungeetablistplus.bridge;

import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.DataStreamUtils;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import de.codecrafter47.data.api.*;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadLocalRandom;

public abstract class AbstractBridge<Player, Server> {

    @Nonnull
    private final DataKeyRegistry dataKeyRegistry;
    @Nonnull
    private final TypeAdapterRegistry typeAdapterRegistry;

    private final int serverIdentifier;
    @Nonnull
    private final String pluginVersion;

    @Nonnull
    private final Server server;

    private final Map<Player, PlayerConnectionInfo> playerPlayerConnectionInfoMap = new ConcurrentHashMap<>();

    private final Map<Integer, BridgeData> serverBridgeDataMap = new ConcurrentHashMap<>();

    private final Object updateDataLock = new Object();

    @Nonnull
    private DataAccess<Player> playerDataAccess = JoinedDataAccess.of(); // always returns null
    @Nonnull
    private DataAccess<Server> serverDataAccess = JoinedDataAccess.of();

    public AbstractBridge(@Nonnull DataKeyRegistry dataKeyRegistry, @Nonnull TypeAdapterRegistry typeAdapterRegistry, @Nonnull String pluginVersion, @Nonnull Server server) {
        this.dataKeyRegistry = dataKeyRegistry;
        this.typeAdapterRegistry = typeAdapterRegistry;
        this.pluginVersion = pluginVersion;
        this.server = server;
        this.serverIdentifier = ThreadLocalRandom.current().nextInt();
    }

    public void onPlayerConnect(@Nonnull Player player) {
        playerPlayerConnectionInfoMap.put(player, new PlayerConnectionInfo());
    }

    public void onPlayerDisconnect(@Nonnull Player player) {
        playerPlayerConnectionInfoMap.remove(player);
    }

    public void onMessage(@Nonnull Player player, @Nonnull DataInput input) throws IOException {

        PlayerConnectionInfo connectionInfo = playerPlayerConnectionInfoMap.get(player);

        if (connectionInfo == null) {
            return;
        }

        int messageId = input.readUnsignedByte();

        if (messageId == BridgeProtocolConstants.MESSAGE_ID_INTRODUCE) {

            int proxyIdentifier = input.readInt();
            int protocolVersion = input.readInt();
            int minimumCompatibleProtocolVersion = input.readInt();
            String proxyPluginVersion = input.readUTF();

            int connectionId = proxyIdentifier + serverIdentifier;

            if (connectionId == connectionInfo.connectionIdentifier) {
                return;
            }

            if (BridgeProtocolConstants.VERSION < minimumCompatibleProtocolVersion) {
                throw new IOException("Incompatible version of BTLP on Proxy detected: " + proxyPluginVersion);
            }

            // reset connection state
            connectionInfo.connectionIdentifier = connectionId;
            connectionInfo.isConnectionValid = true;
            connectionInfo.nextIntroducePacketDelay = 1;
            connectionInfo.introducePacketDelay = 1;
            connectionInfo.hasReceived = false;
            connectionInfo.protocolVersion = Integer.min(BridgeProtocolConstants.VERSION, protocolVersion);
            connectionInfo.proxyIdentifier = proxyIdentifier;
            connectionInfo.playerBridgeData = new BridgeData();
            connectionInfo.serverBridgeData = serverBridgeDataMap.computeIfAbsent(proxyIdentifier, key -> new BridgeData());

            // send ACK 0
            ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
            DataOutput output = new DataOutputStream(byteArrayOutput);

            output.writeByte(BridgeProtocolConstants.MESSAGE_ID_ACK);
            output.writeInt(connectionId);
            output.writeInt(0);

            byte[] message = byteArrayOutput.toByteArray();
            sendMessage(player, message);
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

            if (messageId == BridgeProtocolConstants.MESSAGE_ID_ACK) {

                int confirmed = sequenceNumber - bridgeData.lastConfirmed;

                if (confirmed > bridgeData.messagesPendingConfirmation.size()) {
                    return;
                }

                while (confirmed-- > 0) {
                    bridgeData.lastConfirmed++;
                    bridgeData.messagesPendingConfirmation.remove();
                }
            } else if (messageId == BridgeProtocolConstants.MESSAGE_ID_REQUEST_DATA) {

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
                sendMessage(player, message);

                if (sequenceNumber < bridgeData.nextIncomingMessageId) {
                    // ignore messages from the past after sending ACK
                    return;
                }

                bridgeData.nextIncomingMessageId++;

                int size = input.readInt();
                for (int i = 0; i < size; i++) {
                    DataKey<?> key = DataStreamUtils.readDataKey(input, dataKeyRegistry);
                    int keyNetId = input.readInt();

                    if (key != null) {
                        bridgeData.addRequest(key, keyNetId);
                    }
                }

                runAsync(() -> {
                    try {
                        updatePlayerData(player, connectionInfo);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                throw new IllegalArgumentException("Unexpected message id: " + messageId);
            }
        }
    }

    /**
     * Sends introduce packets to the proxy to try to establish a connection.
     * <p>
     * Should be called periodically, recommended interval is 100ms to 1s.
     */
    public void sendIntroducePackets() throws IOException {
        for (Map.Entry<Player, PlayerConnectionInfo> entry : playerPlayerConnectionInfoMap.entrySet()) {
            PlayerConnectionInfo connectionInfo = entry.getValue();
            if (!connectionInfo.hasReceived) {
                if (--connectionInfo.nextIntroducePacketDelay <= 0) {
                    connectionInfo.nextIntroducePacketDelay = connectionInfo.introducePacketDelay++;

                    ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
                    DataOutput output = new DataOutputStream(byteArrayOutput);

                    output.writeByte(BridgeProtocolConstants.MESSAGE_ID_INTRODUCE);
                    output.writeInt(serverIdentifier);
                    output.writeInt(BridgeProtocolConstants.VERSION);
                    output.writeInt(BridgeProtocolConstants.VERSION);
                    output.writeUTF(pluginVersion);

                    byte[] message = byteArrayOutput.toByteArray();
                    sendMessage(entry.getKey(), message);
                }
            }
        }

    }

    /**
     * Sends unconfirmed messages to the proxy yet another time, to ensure their arrival.
     * <p>
     * Should be called periodically, recommended interval is 1s to 10s.
     */
    public void resendUnconfirmedMessages() {
        long now = System.currentTimeMillis();
        Map<Integer, Player> proxyIds = new HashMap<>();

        for (Map.Entry<Player, PlayerConnectionInfo> e : playerPlayerConnectionInfoMap.entrySet()) {
            Player player = e.getKey();
            PlayerConnectionInfo connectionInfo = e.getValue();

            if (!connectionInfo.isConnectionValid) {
                continue;
            }

            proxyIds.putIfAbsent(connectionInfo.proxyIdentifier, player);
            BridgeData bridgeData = connectionInfo.playerBridgeData;

            if (bridgeData == null) {
                continue;
            }

            if (bridgeData.messagesPendingConfirmation.size() > 0 && (now > bridgeData.lastMessageSent + 1000 || bridgeData.messagesPendingConfirmation.size() > 5)) {
                for (byte[] message : bridgeData.messagesPendingConfirmation) {
                    sendMessage(player, message);
                }
            }
        }

        for (Map.Entry<Integer, Player> e : proxyIds.entrySet()) {
            Integer proxyIdentifier = e.getKey();
            Player player = e.getValue();
            BridgeData bridgeData = serverBridgeDataMap.get(proxyIdentifier);

            if (bridgeData != null && bridgeData.messagesPendingConfirmation.size() > 0 && (now > bridgeData.lastMessageSent + 1000 || bridgeData.messagesPendingConfirmation.size() > 5)) {
                for (byte[] message : bridgeData.messagesPendingConfirmation) {
                    sendMessage(player, message);
                }
            }
        }
    }

    public void updateData() throws IOException {
        synchronized (updateDataLock) {
            Map<Integer, Player> proxyIds = new HashMap<>();

            for (Map.Entry<Player, PlayerConnectionInfo> e : playerPlayerConnectionInfoMap.entrySet()) {
                Player player = e.getKey();
                PlayerConnectionInfo connectionInfo = e.getValue();

                if (!connectionInfo.isConnectionValid) {
                    continue;
                }

                proxyIds.putIfAbsent(connectionInfo.proxyIdentifier, player);

                updatePlayerData(player, connectionInfo);
            }

            for (Map.Entry<Integer, Player> e : proxyIds.entrySet()) {
                Integer proxyIdentifier = e.getKey();
                Player player = e.getValue();
                BridgeData bridgeData = serverBridgeDataMap.get(proxyIdentifier);

                if (bridgeData == null) {
                    continue;
                }

                int size = 0;

                for (CacheEntry entry : bridgeData.requestedData) {
                    Object value = serverDataAccess.get(entry.key, server);
                    entry.dirty = !Objects.equals(value, entry.value);
                    entry.value = value;

                    if (entry.dirty) {
                        size++;
                    }
                }

                ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
                DataOutput output = new DataOutputStream(byteArrayOutput);

                output.writeByte(BridgeProtocolConstants.MESSAGE_ID_UPDATE_DATA_SERVER);
                output.writeInt(proxyIdentifier + serverIdentifier);
                output.writeInt(bridgeData.nextOutgoingMessageId++);
                output.writeInt(size);

                for (CacheEntry entry : bridgeData.requestedData) {
                    if (entry.dirty) {
                        output.writeInt(entry.netId);
                        output.writeBoolean(entry.value == null);
                        if (entry.value != null) {
                            try {
                                typeAdapterRegistry.getTypeAdapter((TypeToken<Object>) entry.key.getType()).write(output, entry.value);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }

                byte[] message = byteArrayOutput.toByteArray();
                bridgeData.messagesPendingConfirmation.add(message);
                bridgeData.lastMessageSent = System.currentTimeMillis();
                sendMessage(player, message);
            }
        }
    }

    private void updatePlayerData(@Nonnull Player player, @Nonnull PlayerConnectionInfo connectionInfo) throws IOException {
        synchronized (updateDataLock) {
            BridgeData bridgeData = connectionInfo.playerBridgeData;

            if (bridgeData == null) {
                return;
            }

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
                ByteArrayOutputStream byteArrayOutput = new ByteArrayOutputStream();
                DataOutput output = new DataOutputStream(byteArrayOutput);
                output.writeByte(BridgeProtocolConstants.MESSAGE_ID_UPDATE_DATA);
                output.writeInt(connectionInfo.connectionIdentifier);
                output.writeInt(bridgeData.nextOutgoingMessageId++);
                output.writeInt(size);

                for (CacheEntry entry : bridgeData.requestedData) {
                    if (entry.dirty) {
                        output.writeInt(entry.netId);
                        output.writeBoolean(entry.value == null);
                        if (entry.value != null) {
                            try {
                                typeAdapterRegistry.getTypeAdapter((TypeToken<Object>) entry.key.getType()).write(output, entry.value);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                        }
                    }
                }

                byte[] message = byteArrayOutput.toByteArray();
                bridgeData.messagesPendingConfirmation.add(message);
                bridgeData.lastMessageSent = System.currentTimeMillis();
                sendMessage(player, message);
            }
        }
    }

    public void setPlayerDataAccess(@Nonnull DataAccess<Player> playerDataAccess) {
        this.playerDataAccess = playerDataAccess;
    }

    public void setServerDataAccess(@Nonnull DataAccess<Server> serverDataAccess) {
        this.serverDataAccess = serverDataAccess;
    }

    protected abstract void sendMessage(@Nonnull Player player, @Nonnull byte[] message);

    protected abstract void runAsync(@Nonnull Runnable task);

    private static class PlayerConnectionInfo {
        boolean isConnectionValid = false;
        boolean hasReceived = false;
        int connectionIdentifier = 0;
        int protocolVersion = 0;
        int proxyIdentifier = 0;
        int nextIntroducePacketDelay = 1;
        int introducePacketDelay = 1;
        @Nullable
        BridgeData playerBridgeData = null;
        @Nullable
        BridgeData serverBridgeData = null;
    }

    private static class CacheEntry {
        @Nonnull
        final DataKey<?> key;
        final int netId;
        @Nullable
        Object value = null;
        boolean dirty = false;

        CacheEntry(@Nonnull DataKey<?> key, int netId) {
            this.key = key;
            this.netId = netId;
        }
    }

    private static class BridgeData {
        final Queue<byte[]> messagesPendingConfirmation = new ConcurrentLinkedQueue<>();
        final List<CacheEntry> requestedData = new CopyOnWriteArrayList<>();
        int lastConfirmed = 0;
        int nextOutgoingMessageId = 1;
        int nextIncomingMessageId = 1;
        long lastMessageSent = 0;

        private void addRequest(@Nonnull DataKey<?> key, int netId) {
            for (CacheEntry registration : requestedData) {
                if (Objects.equals(registration.key, key)) {
                    return;
                }
            }

            requestedData.add(new CacheEntry(key, netId));
        }
    }
}
