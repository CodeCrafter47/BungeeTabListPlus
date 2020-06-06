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
import codecrafter47.bungeetablistplus.api.bukkit.ServerVariable;
import codecrafter47.bungeetablistplus.api.bukkit.Variable;
import codecrafter47.bungeetablistplus.bukkitbridge.placeholderapi.PlaceholderAPIHook;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.network.BridgeProtocolConstants;
import codecrafter47.bungeetablistplus.common.network.TypeAdapterRegistry;
import codecrafter47.bungeetablistplus.common.util.RateLimitedExecutor;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import de.codecrafter47.bungeetablistplus.bridge.AbstractBridge;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.DataKeyRegistry;
import de.codecrafter47.data.api.JoinedDataAccess;
import de.codecrafter47.data.bukkit.AbstractBukkitDataAccess;
import de.codecrafter47.data.bukkit.PlayerDataAccess;
import de.codecrafter47.data.bukkit.ServerDataAccess;
import de.codecrafter47.data.bukkit.api.BukkitData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class BukkitBridge extends BungeeTabListPlusBukkitAPI implements Listener {

    private static final TypeAdapterRegistry typeRegistry = TypeAdapterRegistry.DEFAULT_TYPE_ADAPTERS;

    private static final DataKeyRegistry keyRegistry = DataKeyRegistry.of(
            MinecraftData.class,
            BukkitData.class,
            BTLPDataKeys.class);

    private static final RateLimitedExecutor rlExecutor = new RateLimitedExecutor(5000);

    private final Plugin plugin;

    private PlaceholderAPIHook placeholderAPIHook = null;

    private final ReadWriteLock apiLock = new ReentrantReadWriteLock();
    private final Map<String, Variable> variablesByName = new HashMap<>();
    private final Multimap<Plugin, Variable> variablesByPlugin = HashMultimap.create();
    private final Map<String, ServerVariable> serverVariablesByName = new HashMap<>();
    private final Multimap<Plugin, ServerVariable> serverVariablesByPlugin = HashMultimap.create();

    private Bridge bridge;

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

        initBridge();

        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin,
                BridgeProtocolConstants.CHANNEL);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,
                BridgeProtocolConstants.CHANNEL, (string, player, bytes) -> {

                    DataInput input = new DataInputStream(new ByteArrayInputStream(bytes));

                    try {
                        bridge.onMessage(player, input);
                    } catch (IOException ex) {
                        rlExecutor.execute(() -> plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred while processing a plugin message.", ex));
                    }
                });

        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        // initialize bridge for players already on the server
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            bridge.onPlayerConnect(player);
        }

        // start update task
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                bridge.updateData();
            } catch (IOException e) {
                rlExecutor.execute(() -> plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred", e));
            }
        }, 20, 20);

        // start introduce task, it discovers the proxy plugins
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try {
                bridge.sendIntroducePackets();
            } catch (IOException e) {
                rlExecutor.execute(() -> plugin.getLogger().log(Level.SEVERE, "An unexpected error occurred", e));
            }
        }, 2, 2);

        // start resendUnconfirmedMessages task, it ensures packets arrive in case of failing connections
        plugin.getServer().getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            bridge.resendUnconfirmedMessages();
        }, 50, 50);

        // start reset task, it resets the bridge state every 24h to clear up stale proxy handles
        plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            initBridge();
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                bridge.onPlayerConnect(player);
            }
        }, 1728000, 1728000);
    }

    private void initBridge() {
        bridge = new Bridge();
        updateDataHooks();
    }

    private void updateDataHooks() {
        if (plugin.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderAPIHook = new PlaceholderAPIHook(plugin);
        } else {
            placeholderAPIHook = null;
        }

        if (placeholderAPIHook != null) {
            bridge.setPlayerDataAccess(JoinedDataAccess.of(new PlayerDataAccess(plugin), new ThirdPartyVariablesAccess(), placeholderAPIHook.getDataAccess()));
        } else {
            bridge.setPlayerDataAccess(JoinedDataAccess.of(new PlayerDataAccess(plugin), new ThirdPartyVariablesAccess()));
        }
        bridge.setServerDataAccess(JoinedDataAccess.of(new ServerDataAccess(plugin), new BTLPServerDataKeyAccess()));
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        bridge.onPlayerConnect(event.getPlayer());
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        bridge.onPlayerDisconnect(event.getPlayer());
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        updateDataHooks();
    }

    @EventHandler
    public void onPluginEnable(PluginEnableEvent event) {
        updateDataHooks();
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
    protected void registerVariable0(Plugin plugin, ServerVariable variable) {
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
    protected void unregisterVariables0(Plugin plugin) {
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

    private class Bridge extends AbstractBridge<Player, Server> {

        Bridge() {
            super(BukkitBridge.keyRegistry, BukkitBridge.typeRegistry, BukkitBridge.this.plugin.getDescription().getVersion(), BukkitBridge.this.plugin.getServer());
        }

        @Override
        protected void sendMessage(@Nonnull Player player, @Nonnull byte[] message) {
            try {
                player.sendPluginMessage(plugin, BridgeProtocolConstants.CHANNEL, message);
            } catch (IllegalArgumentException e) {
                if (plugin.isEnabled()) {
                    throw e;
                }
            }
        }

        @Override
        protected void runAsync(@Nonnull Runnable task) {
            plugin.getServer().getScheduler().runTaskAsynchronously(plugin, task);
        }
    }
}
