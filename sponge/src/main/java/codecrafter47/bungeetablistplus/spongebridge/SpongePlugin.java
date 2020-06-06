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
import codecrafter47.bungeetablistplus.api.sponge.ServerVariable;
import codecrafter47.bungeetablistplus.api.sponge.Variable;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.common.util.RateLimitedExecutor;
import codecrafter47.bungeetablistplus.spongebridge.placeholderapi.PlaceholderAPIHook;
import codecrafter47.bungeetablistplus.spongebridge.util.ChannelBufInputStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.codecrafter47.bungeetablistplus.bridge.AbstractBridge;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyRegistry;
import de.codecrafter47.data.api.JoinedDataAccess;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.sponge.AbstractSpongeDataAccess;
import de.codecrafter47.data.sponge.PlayerDataAccess;
import de.codecrafter47.data.sponge.ServerDataAccess;
import de.codecrafter47.data.sponge.api.SpongeData;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.SpongeExecutorService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.DataInput;
import java.io.DataInputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Function;

@Plugin(id = "bungeetablistplus", name = "BungeeTabListPlus-SpongeBridge")
public class SpongePlugin extends BungeeTabListPlusSpongeAPI {

    private static final TypeAdapterRegistry typeRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    private static final DataKeyRegistry keyRegistry = DataKeyRegistry.of(
            MinecraftData.class,
            BTLPDataKeys.class,
            SpongeData.class);

    private final RateLimitedExecutor rlExecutor = new RateLimitedExecutor(5000);

    @Inject
    private Game game;

    @Inject
    private Logger logger;

    private Bridge bridge;

    private ChannelBinding.RawDataChannel channel;

    private PlaceholderAPIHook placeholderAPIHook = null;

    private final ReadWriteLock apiLock = new ReentrantReadWriteLock();
    private final Map<String, Variable> variablesByName = new HashMap<>();
    private final Multimap<Object, Variable> variablesByPlugin = HashMultimap.create();
    private final Map<String, ServerVariable> serverVariablesByName = new HashMap<>();
    private final Multimap<Object, ServerVariable> serverVariablesByPlugin = HashMultimap.create();
    private SpongeExecutorService asyncExecutor;

    @Listener
    public void onInitialization(GameInitializationEvent event) {
        try {
            Field field = BungeeTabListPlusSpongeAPI.class.getDeclaredField("instance");
            field.setAccessible(true);
            field.set(null, this);
        } catch (NoSuchFieldException | IllegalAccessException ex) {
            logger.error("Failed to initialize API", ex);
        }
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event) {

        // register plugin message channel
        channel = game.getChannelRegistrar().createRawChannel(this, BridgeProtocolConstants.CHANNEL);
        channel.addListener(Platform.Type.SERVER, (data, connection, side) -> {
            if (connection instanceof PlayerConnection) {
                Player player = ((PlayerConnection) connection).getPlayer();
                DataInput input = new DataInputStream(new ChannelBufInputStream(data));
                try {
                    bridge.onMessage(player, input);
                } catch (Throwable e) {
                    rlExecutor.execute(() -> {
                        logger.error("Unexpected error", e);
                    });
                }
            }
        });

        // init bridge
        initBridge();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        // start data update task
        asyncExecutor = game.getScheduler().createAsyncExecutor(this);

        game.getScheduler().createTaskBuilder().name("updateData").async().delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(() -> {
            try {
                bridge.updateData();
            } catch (Throwable e) {
                rlExecutor.execute(() -> {
                    logger.error("Unexpected error", e);
                });
            }
        }).submit(this);

        // start introduce task, it discovers the proxy plugins
        game.getScheduler().createTaskBuilder().name("discoverProxy").async().delay(100, TimeUnit.MILLISECONDS).interval(100, TimeUnit.MILLISECONDS).execute(() -> {
            try {
                bridge.sendIntroducePackets();
            } catch (Throwable e) {
                rlExecutor.execute(() -> {
                    logger.error("Unexpected error", e);
                });
            }
        }).submit(this);

        // start resendUnconfirmedMessages task, it ensures packets arrive in case of failing connections
        game.getScheduler().createTaskBuilder().name("housekeeping").async().delay(2500, TimeUnit.MILLISECONDS).interval(2500, TimeUnit.MILLISECONDS).execute(() -> {
            try {
                bridge.resendUnconfirmedMessages();
            } catch (Throwable e) {
                rlExecutor.execute(() -> {
                    logger.error("Unexpected error", e);
                });
            }
        }).submit(this);

        // start reset task, it resets the bridge state every 24h to clear up stale proxy handles
        game.getScheduler().createTaskBuilder().name("reset").delay(24, TimeUnit.HOURS).interval(24, TimeUnit.HOURS).execute(() -> {
            try {
                initBridge();
                for (Player player : game.getServer().getOnlinePlayers()) {
                    bridge.onPlayerConnect(player);
                }
            } catch (Throwable e) {
                rlExecutor.execute(() -> {
                    logger.error("Unexpected error", e);
                });
            }
        }).submit(this);
    }

    private void initBridge() {
        bridge = new Bridge();
        if (classExists("me.rojo8399.placeholderapi.PlaceholderService")) {
            placeholderAPIHook = new PlaceholderAPIHook(this);
            bridge.setPlayerDataAccess(JoinedDataAccess.of(new PlayerDataAccess(logger), new BTLPPlayerDataAccess(), placeholderAPIHook.getDataAccess()));
        } else {
            placeholderAPIHook = null;
            bridge.setPlayerDataAccess(JoinedDataAccess.of(new PlayerDataAccess(logger), new BTLPPlayerDataAccess()));
        }
        bridge.setServerDataAccess(JoinedDataAccess.of(new ServerDataAccess(logger), new BTLPServerDataAccess()));
    }

    @Listener
    public void onConnect(ClientConnectionEvent.Join event) {
        bridge.onPlayerConnect(event.getTargetEntity());
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        bridge.onPlayerDisconnect(event.getTargetEntity());
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
    protected void registerVariable0(Object plugin, ServerVariable variable) {
        Preconditions.checkNotNull(plugin, "plugin");
        Preconditions.checkNotNull(variable, "variable");
        apiLock.writeLock().lock();
        try {
            Preconditions.checkArgument(!variablesByName.containsKey(variable.getName()), "variable already registered");
            serverVariablesByName.put(variable.getName(), variable);
            serverVariablesByPlugin.put(plugin, variable);
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
    protected void unregisterVariable0(ServerVariable variable) {
        Preconditions.checkNotNull(variable, "variable");
        apiLock.writeLock().lock();
        try {
            Preconditions.checkArgument(serverVariablesByName.remove(variable.getName(), variable), "variable not registered");
            serverVariablesByPlugin.values().remove(variable);
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
            for (ServerVariable variable : serverVariablesByPlugin.removeAll(plugin)) {
                serverVariablesByName.remove(variable.getName());
            }
        } finally {
            apiLock.writeLock().unlock();
        }
    }

    public Logger getLogger() {
        return logger;
    }

    private class BTLPPlayerDataAccess extends AbstractSpongeDataAccess<Player> {
        BTLPPlayerDataAccess() {
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
        BTLPServerDataAccess() {
            super(logger);

            addProvider(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES, server -> {
                apiLock.readLock().lock();
                try {
                    return Lists.newArrayList(variablesByName.keySet());
                } finally {
                    apiLock.readLock().unlock();
                }
            });
            addProvider(BTLPDataKeys.REGISTERED_THIRD_PARTY_SERVER_VARIABLES, server -> {
                apiLock.readLock().lock();
                try {
                    return Lists.newArrayList(serverVariablesByName.keySet());
                } finally {
                    apiLock.readLock().unlock();
                }
            });
            addProvider(BTLPDataKeys.PLACEHOLDERAPI_PRESENT, server -> placeholderAPIHook != null);
            addProvider(BTLPDataKeys.PAPI_REGISTERED_PLACEHOLDER_PLUGINS, server -> placeholderAPIHook != null ? placeholderAPIHook.getRegisteredPlaceholderPlugins() : null);
            addProvider(BTLPDataKeys.ThirdPartyServerPlaceholder, this::resolveServerVariable);
        }

        private String resolveServerVariable(Server server, DataKey<String> key) {
            apiLock.readLock().lock();
            try {
                ServerVariable variable = serverVariablesByName.get(key.getParameter());
                if (variable != null) {
                    String replacement = null;
                    try {
                        replacement = variable.getReplacement();
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

    private class Bridge extends AbstractBridge<Player, Server> {

        Bridge() {
            super(SpongePlugin.keyRegistry, SpongePlugin.typeRegistry, Sponge.getPluginManager().getPlugin("bungeetablistplus").flatMap(PluginContainer::getVersion).orElse("unknown"), Sponge.getServer());
        }

        @Override
        protected void sendMessage(@Nonnull Player player, @Nonnull byte[] message) {
            channel.sendTo(player, buf -> buf.writeBytes(message));
        }

        @Override
        protected void runAsync(@Nonnull Runnable task) {
            asyncExecutor.execute(task);
        }
    }

    private static boolean classExists(String name) {
        try {
            Class.forName(name);
        } catch (ClassNotFoundException e) {
            return false;
        }
        return true;
    }
}
