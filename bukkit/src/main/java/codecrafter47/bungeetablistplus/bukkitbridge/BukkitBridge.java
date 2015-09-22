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

import codecrafter47.bungeetablistplus.common.BugReportingService;
import codecrafter47.bungeetablistplus.common.Constants;
import codecrafter47.bungeetablistplus.common.PermissionValues;
import codecrafter47.data.DataAggregator;
import codecrafter47.data.Value;
import codecrafter47.data.Values;
import codecrafter47.data.bukkit.PlayerDataAggregator;
import codecrafter47.data.bukkit.ServerDataAggregator;
import net.cubespace.Yamler.Config.InvalidConfigurationException;
import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.server.PluginEnableEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

/**
 * @author Florian Stober
 */
public class BukkitBridge implements Listener {

    private final Plugin plugin;

    private ServerDataUpdateTask serverDataUpdateTask = null;

    private final Map<UUID, PlayerDataUpdateTask> playerInformationUpdaters = new ConcurrentHashMap<>();

    private PlayerDataAggregator playerDataAggregator;
    private ServerDataAggregator serverDataAggregator;

    private MainConfig config = new MainConfig();

    public BukkitBridge(Plugin plugin) {
        this.plugin = plugin;
    }

    public void onEnable() {
        try {
            if(!plugin.getDataFolder().exists()){
                plugin.getDataFolder().mkdir();
            }
            File file = new File(plugin.getDataFolder(), "config.yml");
            config.init(file);
            config.save(file);
        } catch (InvalidConfigurationException e) {
            plugin.getLogger().log(Level.WARNING, "Failed to load config.yml", e);
            config.automaticallySendBugReports = false;
        }
        if(config.automaticallySendBugReports){
            BugReportingService bugReportingService = new BugReportingService(Level.SEVERE, plugin.getDescription().getName(), plugin.getDescription().getVersion(), command -> Bukkit.getScheduler().runTaskAsynchronously(plugin, command));
            bugReportingService.registerLogger(plugin.getLogger());
        }
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin,
                Constants.channel);
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin,
                Constants.channel, (string, player, bytes) -> {
                    try {
                        DataInputStream in = new DataInputStream(
                                new ByteArrayInputStream(bytes));

                        String subchannel = in.readUTF();
                        if (subchannel.equals(Constants.subchannelRequestPlayerVariable)) {
                            String id = in.readUTF();
                            Value<Object> value = Values.getValue(id);
                            if (value != null) {
                                getPlayerDataUpdateTask(player).requestValue(value);
                            } else {
                                plugin.getLogger().warning("Proxy requested unknown value \"" + id + "\". Proxy/Bukkit plugin version mismatch?");
                            }
                        } else if (subchannel.equals(Constants.subchannelRequestServerVariable)) {
                            String id = in.readUTF();
                            Value<Object> value = Values.getValue(id);
                            if (value != null) {
                                this.serverDataUpdateTask.requestValue(value);
                            } else {
                                plugin.getLogger().warning("Proxy requested unknown value \"" + id + "\". Proxy/Bukkit plugin version mismatch?");
                            }
                        } else {
                            plugin.getLogger().warning("Received plugin message of unknown format. Proxy/Bukkit plugin version mismatch?");
                        }
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "An error occurred while handling an incoming plugin message", ex);
                    }
                });
        plugin.getServer().getPluginManager().registerEvents(this, plugin);

        updateDataHooks();

        // start generalInformation update task
        this.serverDataUpdateTask = new ServerDataUpdateTask();
        this.serverDataUpdateTask.runTaskTimerAsynchronously(plugin, 0, 20);
    }

    private void updateDataHooks() {
        playerDataAggregator = new PlayerDataAggregator(plugin) {
            @Override
            protected void init() {
                super.init();
                for (String permission : PermissionValues.getRegisteredPermissions()) {
                    bind(PermissionValues.getValueForPermission(permission), player -> player.hasPermission(permission));
                }
            }
        };
        serverDataAggregator = new ServerDataAggregator(plugin);
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

    protected void sendInformation(String subchannel, Map<String, Object> delta, Player player) {
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

    public abstract class DataUpdateTask<B, V extends DataAggregator<B>> extends BukkitRunnable {
        Map<Value<?>, Object> sentData = new ConcurrentHashMap<>();
        List<Value<?>> requestedData = new CopyOnWriteArrayList<>();

        protected final void update(Player player, V dataAggregator, B boundType, String subchannel) {
            Map<Value<?>, Object> newData = new ConcurrentHashMap<>();
            requestedData.parallelStream().forEach(value -> {
                dataAggregator.getValue(value, boundType).ifPresent(data -> newData.put(value, data));
            });
            Map<String, Object> delta = new HashMap<>();
            for (Map.Entry<Value<?>, Object> entry : sentData.entrySet()) {
                if (!newData.containsKey(entry.getKey())) {
                    delta.put(entry.getKey().getId(), null);
                } else if (!Objects.equals(newData.get(entry.getKey()), entry.getValue())) {
                    delta.put(entry.getKey().getId(), newData.get(entry.getKey()));
                }
            }
            for (Map.Entry<Value<?>, Object> entry : newData.entrySet()) {
                if (!sentData.containsKey(entry.getKey())) {
                    delta.put(entry.getKey().getId(), entry.getValue());
                }
            }

            if (!delta.isEmpty()) {
                sendInformation(subchannel, delta, player);
            }
            sentData = newData;
        }

        public void requestValue(Value<?> value) {
            if (!requestedData.contains(value)) {
                requestedData.add(value);
            }
        }
    }

    public class ServerDataUpdateTask extends DataUpdateTask<Server, ServerDataAggregator> {

        @Override
        public void run() {
            plugin.getServer().getOnlinePlayers().stream().findAny().ifPresent(player -> {
                update(player, serverDataAggregator, plugin.getServer(), Constants.subchannelUpdateServer);
            });
        }

    }

    public class PlayerDataUpdateTask extends DataUpdateTask<Player, PlayerDataAggregator> {
        private final Player player;

        public PlayerDataUpdateTask(Player player) {
            this.player = player;
        }

        @Override
        public void run() {
            update(player, playerDataAggregator, player, Constants.subchannelUpdatePlayer);
        }
    }
}
