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
import codecrafter47.bungeetablistplus.common.BugReportingService;
import codecrafter47.bungeetablistplus.common.Constants;
import codecrafter47.bungeetablistplus.data.AbstractDataAccessor;
import codecrafter47.bungeetablistplus.data.CompoundDataAccessor;
import codecrafter47.bungeetablistplus.data.DataAccessor;
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.data.bukkit.PlayerDataAccessor;
import codecrafter47.bungeetablistplus.data.bukkit.ServerDataAccessor;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class BukkitBridge extends BungeeTabListPlusBukkitAPI implements Listener {
    private final Plugin plugin;

    private ServerDataUpdateTask serverDataUpdateTask = null;

    private final Map<UUID, PlayerDataUpdateTask> playerInformationUpdaters = new ConcurrentHashMap<>();

    private DataAccessor<Player> playerDataAccessor;
    private DataAccessor<Server> serverDataAccessor;

    private MainConfig config = new MainConfig();

    private PlaceholderAPIHook placeholderAPIHook = null;

    private final ReadWriteLock apiLock = new ReentrantReadWriteLock();
    private final Map<String, Variable> variablesByName = new HashMap<>();
    private final Multimap<Plugin, Variable> variablesByPlugin = HashMultimap.create();

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

        try {
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdir();
            }
            File file = new File(plugin.getDataFolder(), "config.yml");
            if (file.exists()) {
                config.read(file);
            }
            config.setHeader(plugin.getDescription().getName() + " " + plugin.getDescription().getVersion());
            config.write(file);
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load config.yml", e);
            config.automaticallySendBugReports = false;
        }
        if (config.automaticallySendBugReports) {
            BugReportingService bugReportingService = new BugReportingService(Level.SEVERE, plugin.getDescription().getName(), plugin.getDescription().getVersion(), command -> Bukkit.getScheduler().runTaskAsynchronously(plugin, command));
            bugReportingService.registerLogger(plugin.getLogger());
        }
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin,
                Constants.channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,
                Constants.channel, (string, player, bytes) -> {
                    try {
                        ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes));

                        String subchannel = in.readUTF();
                        if (subchannel.equals(Constants.subchannelRequestPlayerVariable)) {
                            DataKey<Object> dataKey = (DataKey<Object>) in.readObject();
                            getPlayerDataUpdateTask(player).requestValue(dataKey);
                        } else if (subchannel.equals(Constants.subchannelRequestServerVariable)) {
                            DataKey<Object> dataKey = (DataKey<Object>) in.readObject();
                            this.serverDataUpdateTask.requestValue(dataKey);
                        } else if (subchannel.equals(Constants.subchannelRequestResetPlayerVariables)) {
                            getPlayerDataUpdateTask(player).reset();
                        } else if (subchannel.equals(Constants.subchannelRequestResetServerVariables)) {
                            serverDataUpdateTask.reset();
                        } else if (subchannel.equals(Constants.subchannelPlaceholder)) {
                            String placeholder = in.readUTF();
                            PlaceholderAPIHook hook = this.placeholderAPIHook;
                            if (hook != null) {
                                plugin.getServer().getScheduler().runTaskAsynchronously(plugin, () -> {
                                    try {
                                        if (hook.isPlaceholder(player, placeholder)) {
                                            ByteArrayOutputStream os = new ByteArrayOutputStream();
                                            ObjectOutputStream out = new ObjectOutputStream(os);
                                            out.writeUTF(subchannel);
                                            out.writeUTF(placeholder);
                                            out.close();
                                            player.sendPluginMessage(plugin, Constants.channel, os.toByteArray());
                                        }
                                    } catch (Throwable ex) {
                                        plugin.getLogger().log(Level.SEVERE, "something funny happened", ex);
                                    }
                                });
                            }
                        } else {
                            plugin.getLogger().warning("Received plugin message of unknown format. Proxy/Bukkit plugin version mismatch?");
                        }
                    } catch (IOException | ClassNotFoundException ex) {
                        plugin.getLogger().log(Level.SEVERE, "An error occurred while handling an incoming plugin message", ex);
                    }
                });
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        updateDataHooks();

        // start generalInformation update task
        this.serverDataUpdateTask = new ServerDataUpdateTask();
        this.serverDataUpdateTask.runTaskTimerAsynchronously(plugin, 0, 20);

        // start update tasks for players already on the server
        plugin.getServer().getOnlinePlayers().forEach(this::getPlayerDataUpdateTask);
    }

    private void updateDataHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIHook = new PlaceholderAPIHook(plugin);
        } else {
            placeholderAPIHook = null;
        }

        if (placeholderAPIHook != null) {
            playerDataAccessor = CompoundDataAccessor.of(new PlayerDataAccessor(plugin), new ThirdPartyVariablesAccessor(), placeholderAPIHook.getDataAccessor());
        } else {
            playerDataAccessor = CompoundDataAccessor.of(new PlayerDataAccessor(plugin), new ThirdPartyVariablesAccessor());
        }
        serverDataAccessor = CompoundDataAccessor.of(new ServerDataAccessor(plugin), new BTLPServerDataKeyAccessor());
    }

    private PlayerDataUpdateTask getPlayerDataUpdateTask(Player player) {
        if (playerInformationUpdaters.get(player.getUniqueId()) == null) {
            PlayerDataUpdateTask playerDataUpdateTask = new PlayerDataUpdateTask(player);
            playerDataUpdateTask.runTaskTimerAsynchronously(plugin, 0, 20);
            playerInformationUpdaters.put(player.getUniqueId(), playerDataUpdateTask);
        }
        return playerInformationUpdaters.get(player.getUniqueId());
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        getPlayerDataUpdateTask(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (playerInformationUpdaters.containsKey(player.getUniqueId())) {
            try {
                playerInformationUpdaters.remove(player.getUniqueId()).cancel();
            } catch (Exception ex) {
                plugin.getLogger().log(Level.SEVERE, "An exception occurred while removing a player", ex);
            }
        }
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        updateDataHooks();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        updateDataHooks();
    }

    protected void sendInformation(String subchannel, Map<DataKey<?>, Object> delta, Player player) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeUTF(subchannel);
            out.writeObject(delta);
            out.close();
            player.sendPluginMessage(plugin, Constants.channel, os.toByteArray());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
        }
    }

    protected void sendHash(String subchannel, int hash, Player player) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeUTF(subchannel);
            out.writeInt(hash);
            out.close();
            player.sendPluginMessage(plugin, Constants.channel, os.toByteArray());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, null, ex);
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
            for (Iterator<Variable> iterator = variablesByPlugin.values().iterator(); iterator.hasNext(); ) {
                if (iterator.next().equals(variable)) {
                    iterator.remove();
                }
            }
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

    private class ThirdPartyVariablesAccessor extends AbstractDataAccessor<Player> {
        public ThirdPartyVariablesAccessor() {
            super(plugin.getLogger());
            bind(BTLPDataKeys.ThirdPartyVariableDataKey.class, this::resolveVariable);
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

    private class BTLPServerDataKeyAccessor extends AbstractDataAccessor<Server> {
        public BTLPServerDataKeyAccessor() {
            super(plugin.getLogger());
            bind(BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES, server -> {
                apiLock.readLock().lock();
                try {
                    return ImmutableList.copyOf(variablesByName.keySet());
                } finally {
                    apiLock.readLock().unlock();
                }
            });
            bind(BTLPDataKeys.PLACEHOLDERAPI_PRESENT, server -> placeholderAPIHook != null);
        }
    }

    public abstract class DataUpdateTask<B> extends BukkitRunnable {
        Map<DataKey<?>, Object> sentData = new ConcurrentHashMap<>();
        ImmutableSet<DataKey<?>> requestedData = ImmutableSet.of();
        boolean requestedReset = true;

        protected final void update(Player player, DataAccessor<B> dataAccessor, B boundType, String subchannel) {
            Map<DataKey<?>, Object> newData = new ConcurrentHashMap<>();
            requestedData.parallelStream().forEach(value -> {
                dataAccessor.getValue(value, boundType).ifPresent(data -> newData.put(value, data));
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
        public void run() {
            plugin.getServer().getOnlinePlayers().stream().findAny().ifPresent(player -> {
                update(player, serverDataAccessor, plugin.getServer(), Constants.subchannelUpdateServer);
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
        public void run() {
            update(player, playerDataAccessor, player, Constants.subchannelUpdatePlayer);
            sendHash(Constants.subchannelPlayerHash, sentData.hashCode(), player);
        }
    }
}
