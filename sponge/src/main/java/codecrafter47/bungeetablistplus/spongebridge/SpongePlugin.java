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

package codecrafter47.bungeetablistplus.spongebridge;

import codecrafter47.bungeetablistplus.api.sponge.BungeeTabListPlusSpongeAPI;
import codecrafter47.bungeetablistplus.api.sponge.Variable;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.common.util.RateLimitedExecutor;
import codecrafter47.bungeetablistplus.spongebridge.messages.*;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.codecrafter47.data.api.DataAccess;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyRegistry;
import de.codecrafter47.data.api.JoinedDataAccess;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.sponge.AbstractSpongeDataAccess;
import de.codecrafter47.data.sponge.PlayerDataAccess;
import de.codecrafter47.data.sponge.ServerDataAccess;
import de.codecrafter47.data.sponge.api.SpongeData;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ChannelRegistrationEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.Plugin;

import javax.inject.Inject;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@Plugin(id = "bungeetablistplus", name = "BungeeTabListPlus-SpongeBridge", version = PomData.VERSION)
public class SpongePlugin extends BungeeTabListPlusSpongeAPI {

    public static final TypeAdapterRegistry typeRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    public static final DataKeyRegistry keyRegistry = DataKeyRegistry.of(
            MinecraftData.class,
            BungeeData.class,
            BTLPDataKeys.class,
            SpongeData.class);

    private final RateLimitedExecutor rlExecutor = new RateLimitedExecutor(5000);

    public static final UUID serverId = UUID.randomUUID();

    @Inject
    private Game game;

    @Inject
    private Logger logger;

    private ChannelBinding.IndexedMessageChannel channel;

    private final Map<Player, PlayerBridgeData> playerData = new ConcurrentHashMap<>();
    private final Map<UUID, ServerBridgeData> serverData = new ConcurrentHashMap<>();

    private DataAccess<Player> playerDataAccess;
    private DataAccess<Server> serverDataAccess;

    private final ReadWriteLock apiLock = new ReentrantReadWriteLock();
    private final Map<String, Variable> variablesByName = new HashMap<>();
    private final Multimap<Object, Variable> variablesByPlugin = HashMultimap.create();

    @Listener
    public void onInitialization(GameInitializationEvent event) {
        try {
            Field field = BungeeTabListPlusSpongeAPI.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, this);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            logger.error("Failed to initialize API", ex);
        }

        /*
        AbstractMessageProxyRequestData.missingDataKeyLogger = new Consumer<String>() {

            private final Set<String> missingKeys = Sets.newConcurrentHashSet();

            @Override
            public void accept(String id) {
                if (missingKeys.add(id)) {
                    logger.warn("Missing data key with id " + id + ". Is the plugin up-to-date?");
                }
            }
        };
        */
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event) {

        // register plugin message channel
        channel = game.getChannelRegistrar().createChannel(this, BridgeProtocolConstants.CHANNEL);
        channel.registerMessage(MessageProxyHandshake.class, BridgeProtocolConstants.MESSAGE_ID_PROXY_HANDSHAKE, this::onMessageHandshake);
        channel.registerMessage(MessageProxyRequestData.class, BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_DATA, this::onMessageRequestData);
        channel.registerMessage(MessageProxyRequestServerData.class, BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_SERVER_DATA, this::onMessageRequestServerData);
        channel.registerMessage(MessageProxyRequestServerDataReset.class, BridgeProtocolConstants.MESSAGE_ID_PROXY_REQUEST_RESET_SERVER_DATA, this::onMessageRequestServerDataReset);
        channel.registerMessage(MessageProxyPluginOutdated.class, BridgeProtocolConstants.MESSAGE_ID_PROXY_OUTDATED, this::onMessagePluginOutdated);
        channel.registerMessage(MessageServerHandshake.class, BridgeProtocolConstants.MESSAGE_ID_SERVER_HANDSHAKE);
        channel.registerMessage(MessageServerUpdateData.class, BridgeProtocolConstants.MESSAGE_ID_SERVER_UPDATE_DATA);
        channel.registerMessage(MessageServerUpdateServerData.class, BridgeProtocolConstants.MESSAGE_ID_SERVER_UPDATE_SERVER_DATA);
        channel.registerMessage(MessageServerEnableConnection.class, BridgeProtocolConstants.MESSAGE_ID_SERVER_ENABLE_CONNECTION);

        // init data hooks
        playerDataAccess = JoinedDataAccess.of(new PlayerDataAccess(logger), new BTLPPlayerDataAccess());
        serverDataAccess = JoinedDataAccess.of(new ServerDataAccess(logger), new BTLPServerDataAccess());
    }

    private void onMessageHandshake(MessageProxyHandshake message, RemoteConnection remoteConnection, Platform.Type platform) {
        if (!(remoteConnection instanceof PlayerConnection)) {
            throw new AssertionError("Expect plugin message to be sent from a player.");
        }
        Player player = ((PlayerConnection) remoteConnection).getPlayer();

        if (message.getProtocolVersion() > BridgeProtocolConstants.VERSION) {
            rlExecutor.execute(() -> logger.warn("BungeeTabListPlus_SpongeBridge is outdated."));
        } else if (message.getProtocolVersion() < BridgeProtocolConstants.VERSION) {
            rlExecutor.execute(() -> logger.warn("BungeeTabListPlus proxy plugin outdated."));
        } else {
            playerData.put(player, new PlayerBridgeData(message.getProxyId()));
            serverData.computeIfAbsent(message.getProxyId(), uuid -> new ServerBridgeData());
            channel.sendTo(player, new MessageServerHandshake());
        }
    }

    private void onMessageRequestData(MessageProxyRequestData message, RemoteConnection remoteConnection, Platform.Type platform) {
        if (!(remoteConnection instanceof PlayerConnection)) {
            throw new AssertionError("Expect plugin message to be sent from a player.");
        }
        Player player = ((PlayerConnection) remoteConnection).getPlayer();

        BridgeData bridgeData = playerData.get(player);
        if (bridgeData != null) {
            handleDataRequest(bridgeData, message);
        }
    }

    private void onMessageRequestServerData(MessageProxyRequestServerData message, RemoteConnection remoteConnection, Platform.Type platform) {
        if (!(remoteConnection instanceof PlayerConnection)) {
            throw new AssertionError("Expect plugin message to be sent from a player.");
        }
        Player player = ((PlayerConnection) remoteConnection).getPlayer();

        PlayerBridgeData playerBridgeData = playerData.get(player);
        if (playerBridgeData != null) {
            BridgeData bridgeData = serverData.get(playerBridgeData.proxyId);
            if (bridgeData != null) {
                handleDataRequest(bridgeData, message);
            }
        }
    }

    private void onMessageRequestServerDataReset(MessageProxyRequestServerDataReset message, RemoteConnection remoteConnection, Platform.Type platform) {
        if (!(remoteConnection instanceof PlayerConnection)) {
            throw new AssertionError("Expect plugin message to be sent from a player.");
        }
        Player player = ((PlayerConnection) remoteConnection).getPlayer();

        PlayerBridgeData playerBridgeData = playerData.get(player);
        if (playerBridgeData != null) {
            ServerBridgeData bridgeData = serverData.get(playerBridgeData.proxyId);
            if (bridgeData != null) {
                serverData.put(playerBridgeData.proxyId, new ServerBridgeData());
            }
        }
    }

    private void onMessagePluginOutdated(MessageProxyPluginOutdated message, RemoteConnection remoteConnection, Platform.Type platform) {
        rlExecutor.execute(() -> logger.warn("BungeeTabListPlus proxy plugin outdated."));
    }

    private void handleDataRequest(BridgeData bridgeData, AbstractMessageProxyRequestData message) {
        for (AbstractMessageProxyRequestData.Item item : message.getItems()) {
            bridgeData.addRequest(item.getKey(), item.getNetId());
        }
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        // start data update task
        game.getScheduler().createTaskBuilder().async().delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(() -> {
            long now = System.currentTimeMillis();
            Map<UUID, Player> proxyIds = new HashMap<>();
            List<AbstractMessageServerUpdateData.Item> update = new ArrayList<>();

            for (Map.Entry<Player, PlayerBridgeData> e : playerData.entrySet()) {
                Player player = e.getKey();
                PlayerBridgeData bridgeData = e.getValue();

                proxyIds.putIfAbsent(bridgeData.proxyId, player);

                for (CacheEntry entry : bridgeData.requestedData) {
                    Object value = playerDataAccess.get(entry.key, player);
                    if (!Objects.equals(value, entry.value)) {
                        update.add(new AbstractMessageServerUpdateData.Item(entry.netId, typeRegistry.getTypeAdapter(((DataKey<Object>) entry.key).getType()), value));
                    }
                    entry.value = value;
                }

                if (!update.isEmpty()) {
                    MessageServerUpdateData message = new MessageServerUpdateData();
                    message.setItems(ImmutableList.copyOf(update));
                    channel.sendTo(player, message);
                    update.clear();
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

                for (CacheEntry entry : bridgeData.requestedData) {
                    Object value = serverDataAccess.get(entry.key, game.getServer());
                    if (!Objects.equals(value, entry.value)) {
                        update.add(new AbstractMessageServerUpdateData.Item(entry.netId, typeRegistry.getTypeAdapter(((DataKey<Object>) entry.key).getType()), value));
                    }
                    entry.value = value;
                }

                if (!update.isEmpty()) {
                    bridgeData.revision++;
                }

                MessageServerUpdateServerData message = new MessageServerUpdateServerData();
                message.setRevision(bridgeData.revision);
                message.setItems(ImmutableList.copyOf(update));
                channel.sendTo(player, message);
                update.clear();
            }

            for (Iterator<ServerBridgeData> iterator = serverData.values().iterator(); iterator.hasNext(); ) {
                ServerBridgeData data = iterator.next();
                if (now - data.lastUpdate > 900000) {
                    iterator.remove();
                }
            }
        }).submit(this);
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        playerData.remove(event.getTargetEntity());
    }

    @Listener
    public void onChannelRegistration(ChannelRegistrationEvent.Register event) {
        if (BridgeProtocolConstants.CHANNEL.equals(event.getChannel())) {
            Optional<Player> player = event.getCause().get(NamedCause.SOURCE, Player.class);
            if (player.isPresent()) {
                channel.sendTo(player.get(), new MessageServerEnableConnection());
            } else {
                throw new AssertionError("Source is not present.");
            }
        }
    }

    @Override
    protected void registerVariable0(Object plugin, Variable variable) {
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
    protected void unregisterVariables0(Object plugin) {
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

    private class BTLPPlayerDataAccess extends AbstractSpongeDataAccess<Player> {
        public BTLPPlayerDataAccess() {
            super(logger);

            addProvider(BTLPDataKeys.ThirdPartyPlaceholder, this::resolveVariable);
        }

        private String resolveVariable(Player player, DataKey<?> key) {
            apiLock.readLock().lock();
            try {
                Variable variable = variablesByName.get(key.getParameter());
                if (variable != null) {
                    String replacement = null;
                    try {
                        replacement = variable.getReplacement(player);
                    } catch (Throwable th) {
                        logger.warn("An exception occurred while resolving a variable provided by a third party plugin", th);
                    }
                    return replacement;
                }
                return null;
            } finally {
                apiLock.readLock().unlock();
            }
        }
    }

    private class BTLPServerDataAccess extends AbstractSpongeDataAccess<Server> {
        public BTLPServerDataAccess() {
            super(logger);

            addProvider(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES, server -> {
                apiLock.readLock().lock();
                try {
                    return Lists.newArrayList(variablesByName.keySet());
                } finally {
                    apiLock.readLock().unlock();
                }
            });
            addProvider(BTLPDataKeys.PLACEHOLDERAPI_PRESENT, server -> false);
        }
    }

    @RequiredArgsConstructor
    private static class CacheEntry {
        private final DataKey<?> key;
        private final int netId;
        private Object value = null;
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
