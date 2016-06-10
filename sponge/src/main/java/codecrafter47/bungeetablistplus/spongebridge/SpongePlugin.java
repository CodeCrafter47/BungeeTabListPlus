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
import codecrafter47.bungeetablistplus.common.Constants;
import codecrafter47.bungeetablistplus.data.AbstractDataAccess;
import codecrafter47.bungeetablistplus.data.DataAccess;
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.data.PermissionDataKey;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import lombok.SneakyThrows;
import org.slf4j.Logger;
import org.spongepowered.api.Game;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.state.GameAboutToStartServerEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.event.network.ClientConnectionEvent;
import org.spongepowered.api.network.ChannelBinding;
import org.spongepowered.api.network.ChannelBuf;
import org.spongepowered.api.network.PlayerConnection;
import org.spongepowered.api.network.RawDataListener;
import org.spongepowered.api.network.RemoteConnection;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.scoreboard.Team;
import org.spongepowered.api.service.economy.EconomyService;

import javax.annotation.Nonnull;
import javax.inject.Inject;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;

@Plugin(id = "bungeetablistplus", name = "BungeeTabListPlus-SpongeBridge", version = PomData.VERSION)
public class SpongePlugin extends BungeeTabListPlusSpongeAPI implements RawDataListener {

    @Inject
    private Game game;

    @Inject
    private Logger logger;


    private ChannelBinding.RawDataChannel channel;

    private ServerDataUpdateTask serverDataUpdateTask;

    private final Map<UUID, PlayerDataUpdateTask> playerInformationUpdaters = new ConcurrentHashMap<>();

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
    }

    @Listener
    public void onServerAboutToStart(GameAboutToStartServerEvent event) {

        // register plugin message channel
        channel = game.getChannelRegistrar().createRawChannel(this, Constants.channel);
        channel.addListener(Platform.Type.SERVER, this);

        // init data hooks
        playerDataAccess = new PlayerDataAccess();
        serverDataAccess = new ServerDataAccess();
    }

    @Listener
    public void onServerStart(GameStartedServerEvent event) {

        // start server data update task
        serverDataUpdateTask = new ServerDataUpdateTask();
        game.getScheduler().createTaskBuilder().async().delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(serverDataUpdateTask).submit(this);
    }

    @Listener
    public void onJoin(ClientConnectionEvent.Join event) {
        PlayerDataUpdateTask task = new PlayerDataUpdateTask(event.getTargetEntity());
        playerInformationUpdaters.put(event.getTargetEntity().getUniqueId(), task);
        game.getScheduler().createTaskBuilder().async().delay(1, TimeUnit.SECONDS).interval(1, TimeUnit.SECONDS).execute(task).submit(this);
    }

    @Listener
    public void onDisconnect(ClientConnectionEvent.Disconnect event) {
        playerInformationUpdaters.remove(event.getTargetEntity().getUniqueId());
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

    @Override
    public void handlePayload(@Nonnull ChannelBuf data, @Nonnull RemoteConnection connection, @Nonnull Platform.Type side) {
        try {
            ObjectInputStream in = new ObjectInputStream(new ChannelBufInputStream(data));

            String subchannel = in.readUTF();
            UUID uniqueId = ((PlayerConnection) connection).getPlayer().getUniqueId();
            if (subchannel.equals(Constants.subchannelRequestPlayerVariable)) {
                if (playerInformationUpdaters.containsKey(uniqueId)) {
                    DataKey<Object> dataKey = (DataKey<Object>) in.readObject();
                    playerInformationUpdaters.get(uniqueId).requestValue(dataKey);
                }
            } else if (subchannel.equals(Constants.subchannelRequestServerVariable)) {
                DataKey<Object> dataKey = (DataKey<Object>) in.readObject();
                this.serverDataUpdateTask.requestValue(dataKey);
            } else if (subchannel.equals(Constants.subchannelRequestResetPlayerVariables)) {
                if (playerInformationUpdaters.containsKey(uniqueId)) {
                    playerInformationUpdaters.get(uniqueId).reset();
                }
            } else if (subchannel.equals(Constants.subchannelRequestResetServerVariables)) {
                serverDataUpdateTask.reset();
            } else if (subchannel.equals(Constants.subchannelPlaceholder)) {
                // we don't have this on sponge
            } else {
                logger.warn("Received plugin message of unknown format. Proxy/Sponge plugin version mismatch?");
            }
        } catch (IOException | ClassNotFoundException ex) {
            logger.error("An error occurred while handling an incoming plugin message", ex);
        }
    }

    protected void sendInformation(String subchannel, Map<DataKey<?>, Object> delta, Player player) {
        channel.sendTo(player, new Consumer<ChannelBuf>() {
            @Override
            @SneakyThrows
            public void accept(ChannelBuf channelBuf) {
                ObjectOutputStream out = new ObjectOutputStream(new ChannelBufOutputStream(channelBuf));
                out.writeUTF(subchannel);
                out.writeObject(delta);
                out.close();
            }
        });
    }

    protected void sendHash(String subchannel, int hash, Player player) {
        channel.sendTo(player, new Consumer<ChannelBuf>() {
            @Override
            @SneakyThrows
            public void accept(ChannelBuf channelBuf) {
                ObjectOutputStream out = new ObjectOutputStream(new ChannelBufOutputStream(channelBuf));
                out.writeUTF(subchannel);
                out.writeInt(hash);
                out.close();
            }
        });
    }

    private abstract class AbstractSpongeDataAccess<T> extends AbstractDataAccess<T> {

        @Override
        @SuppressWarnings("unchecked")
        public <V> V getRawValue(DataKey<V> key, T context) {
            try {
                return super.getRawValue(key, context);
            } catch (Throwable th) {

                logger.error("Unexpected exception", th);
            }
            return null;
        }
    }

    private class PlayerDataAccess extends AbstractSpongeDataAccess<Player> {
        public PlayerDataAccess() {
            bind(BTLPDataKeys.ThirdPartyVariableDataKey.class, this::resolveVariable);
            // todo register more of these
            bind(DataKeys.Health, player -> player.getHealthData().health().get());
            bind(DataKeys.MaxHealth, player -> player.getHealthData().maxHealth().get());
            bind(DataKeys.Level, player -> player.get(Keys.EXPERIENCE_LEVEL).orElse(null));
            // todo bind(DataKeys.XP, ...);
            bind(DataKeys.TotalXP, player -> player.get(Keys.TOTAL_EXPERIENCE).orElse(null));
            bind(DataKeys.PosX, player -> player.getLocation().getX());
            bind(DataKeys.PosY, player -> player.getLocation().getY());
            bind(DataKeys.PosZ, player -> player.getLocation().getZ());
            bind(DataKeys.Team, player -> player.getScoreboard().getMemberTeam(player.getTeamRepresentation()).map(Team::getName).orElse(null));
            bind(PermissionDataKey.class, (player, key) -> player.hasPermission(key.getPermission()));

            bind(DataKeys.DisplayName, player -> player.getDisplayNameData().displayName().get().toPlain());
            bind(DataKeys.World, player -> player.getWorld().getName());

            bind(DataKeys.Vault_Balance, player -> game.getServiceManager().provide(EconomyService.class).flatMap(e -> e.getOrCreateAccount(player.getUniqueId()).map(a -> a.getBalance(e.getDefaultCurrency(), player.getActiveContexts()).doubleValue())).orElse(null));

            // todo bind(DataKeys.Vault_PermissionGroup,...);
            // todo bind(DataKeys.Vault_PermissionGroupWeight,...);
            // todo bind(DataKeys.Vault_Prefix,...);
            // todo bind(DataKeys.Vault_Suffix,...);
        }

        private String resolveVariable(Player player, BTLPDataKeys.ThirdPartyVariableDataKey key) {
            apiLock.readLock().lock();
            try {
                Variable variable = variablesByName.get(key.getName());
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

    private class ServerDataAccess extends AbstractSpongeDataAccess<Server> {
        public ServerDataAccess() {
            bind(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES, server -> {
                apiLock.readLock().lock();
                try {
                    return Lists.newArrayList(variablesByName.keySet());
                } finally {
                    apiLock.readLock().unlock();
                }
            });
            bind(BTLPDataKeys.PLACEHOLDERAPI_PRESENT, server -> false);

            bind(DataKeys.MinecraftVersion, server -> game.getPlatform().getMinecraftVersion().getName());
            bind(DataKeys.ServerModName, server -> game.getPlatform().getImplementation().getName());
            bind(DataKeys.ServerModVersion, server -> game.getPlatform().getImplementation().getVersion().orElse(null));
            bind(DataKeys.TPS, Server::getTicksPerSecond);

            bind(DataKeys.Vault_CurrencyNamePlural, server -> game.getServiceManager().provide(EconomyService.class).map(e -> e.getDefaultCurrency().getPluralDisplayName().toPlain()).orElse(null));
            bind(DataKeys.Vault_CurrencyNameSingular, server -> game.getServiceManager().provide(EconomyService.class).map(e -> e.getDefaultCurrency().getDisplayName().toPlain()).orElse(null));
        }
    }

    public abstract class DataUpdateTask<B> implements Consumer<Task> {
        Map<DataKey<?>, Object> sentData = new ConcurrentHashMap<>();
        ImmutableSet<DataKey<?>> requestedData = ImmutableSet.of();
        boolean requestedReset = true;

        protected final void update(Player player, DataAccess<B> dataAccess, B boundType, String subchannel) {
            Map<DataKey<?>, Object> newData = new ConcurrentHashMap<>();
            requestedData.parallelStream().forEach(value -> {
                dataAccess.getValue(value, boundType).ifPresent(data -> newData.put(value, data));
            });
            Map<DataKey<?>, Object> delta = new HashMap<>();
            for (Map.Entry<DataKey<?>, Object> entry : sentData.entrySet()) {
                if (!newData.containsKey(entry.getKey())) {
                    delta.put(entry.getKey(), null);
                } else if (!Objects.equals(newData.get(entry.getKey()), entry.getValue())) {
                    delta.put(entry.getKey(), newData.get(entry.getKey()));
                }
            }
            for (Map.Entry<DataKey<?>, Object> entry : newData.entrySet()) {
                if (requestedReset || !sentData.containsKey(entry.getKey())) {
                    delta.put(entry.getKey(), entry.getValue());
                }
            }

            if (!delta.isEmpty()) {
                sendInformation(subchannel, delta, player);
            }
            sentData = newData;

            if (requestedReset) {
                requestedReset = false;
            }
        }

        public void requestValue(DataKey<?> dataKey) {
            if (!requestedData.contains(dataKey)) {
                requestedData = ImmutableSet.<DataKey<?>>builder().addAll(requestedData).add(dataKey).build();
            }
        }

        public void reset() {
            requestedReset = true;
        }
    }

    public class ServerDataUpdateTask extends DataUpdateTask<Server> {

        @Override
        public void accept(Task task) {
            game.getServer().getOnlinePlayers().stream().findAny().ifPresent(player -> {
                update(player, serverDataAccess, game.getServer(), Constants.subchannelUpdateServer);
                sendHash(Constants.subchannelServerHash, sentData.hashCode(), player);
            });
        }

    }

    public class PlayerDataUpdateTask extends DataUpdateTask<Player> {
        private final Player player;

        public PlayerDataUpdateTask(Player player) {
            this.player = player;
        }

        @Override
        public void accept(Task task) {
            if (player.isOnline() && playerInformationUpdaters.containsKey(player.getUniqueId())) {
                update(player, playerDataAccess, player, Constants.subchannelUpdatePlayer);
                sendHash(Constants.subchannelPlayerHash, sentData.hashCode(), player);
            } else {
                task.cancel();
            }
        }
    }
}
