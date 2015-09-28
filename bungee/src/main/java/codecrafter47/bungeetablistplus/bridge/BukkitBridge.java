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
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.placeholder.PlaceholderProvider;
import codecrafter47.bungeetablistplus.common.BTLPDataKeys;
import codecrafter47.bungeetablistplus.common.Constants;
import codecrafter47.bungeetablistplus.data.DataCache;
import codecrafter47.bungeetablistplus.data.DataKey;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.SneakyThrows;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PlayerDisconnectEvent;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

public class BukkitBridge implements Listener {
    private final BungeeTabListPlus plugin;

    private final Map<String, DataCache> serverInformation = new ConcurrentHashMap<>();
    private final Cache<UUID, DataCache> playerInformation = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).build();

    private final Set<String> registeredThirdPartyVariables = new HashSet<>();
    private final ReentrantLock thirdPartyVariablesLock = new ReentrantLock();

    public BukkitBridge(BungeeTabListPlus plugin) {
        this.plugin = plugin;
        plugin.getProxy().getPluginManager().registerListener(plugin.getPlugin(), this);
        plugin.getProxy().getScheduler().schedule(plugin.getPlugin(), this::checkForThirdPartyVariables, 2, 2, TimeUnit.SECONDS);
    }

    private void checkForThirdPartyVariables() {
        for (ServerInfo serverInfo : plugin.getProxy().getServers().values()) {
            get(serverInfo, BTLPDataKeys.REGISTERED_THIRD_PARTY_VARIABLES).ifPresent(variables -> {
                thirdPartyVariablesLock.lock();
                try {
                    for (String variable : variables) {
                        if (!registeredThirdPartyVariables.contains(variable)) {
                            plugin.registerPlaceholderProvider0(new PlaceholderProvider() {
                                @Override
                                public void setup() {
                                    bind(variable).to(context -> plugin.getBridge().get(context.getPlayer(), BTLPDataKeys.createThirdPartyVariableDataKey(variable)).orElse(""));
                                }
                            });
                            registeredThirdPartyVariables.add(variable);
                        }
                    }

                } finally {
                    thirdPartyVariablesLock.unlock();
                }
            });
        }
    }

    private DataCache getServerDataCache(String serverName) {
        if (!serverInformation.containsKey(serverName)) {
            serverInformation.putIfAbsent(serverName, new DataCache());
        }
        return serverInformation.get(serverName);
    }

    @SneakyThrows
    private DataCache getPlayerDataCache(UUID uuid) {
        return playerInformation.get(uuid, DataCache::new);
    }

    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (event.getTag().equals(Constants.channel)) {
            event.setCancelled(true);
            if (event.getReceiver() instanceof ProxiedPlayer && event.getSender() instanceof Server) {
                try {
                    ProxiedPlayer player = (ProxiedPlayer) event.getReceiver();
                    Server server = (Server) event.getSender();

                    ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(event.getData()));

                    String subchannel = in.readUTF();

                    switch (subchannel) {
                        case Constants.subchannelUpdateServer:
                            updateData(in, getServerDataCache(server.getInfo().getName()));
                            break;
                        case Constants.subchannelUpdatePlayer:
                            updateData(in, getPlayerDataCache(player.getUniqueId()));
                            break;
                        case Constants.subchannelPlayerHash:
                            if (getPlayerDataCache(player.getUniqueId()).getMap().hashCode() != in.readInt()) {
                                requestReset(player);
                            }
                            break;
                        case Constants.subchannelServerHash:
                            if (getServerDataCache(server.getInfo().getName()).getMap().hashCode() != in.readInt()) {
                                requestReset(server);
                            }
                            break;
                        case Constants.subchannelPlaceholder:
                            plugin.getPlaceholderAPIHook().onPlaceholderConfirmed(in.readUTF());
                            break;
                        default:
                            plugin.getLogger().log(Level.SEVERE,
                                    "BukkitBridge on server " + server.getInfo().
                                            getName() + " send an unknown packet! Is everything up-to-date?");
                            break;
                    }
                } catch (StreamCorruptedException ex) {
                    plugin.getLogger().log(Level.WARNING, "BungeeTabListPlus_BukkitBridge.jar on server {0} needs to be updated", ((Server) event.getSender()).getInfo());
                } catch (IOException | ClassNotFoundException ex) {
                    plugin.getLogger().log(Level.SEVERE, "Exception while parsing data from Bukkit", ex);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void updateData(ObjectInputStream in, DataCache dataCache) throws IOException, ClassNotFoundException {
        for (Entry<DataKey, Object> entry : ((Map<DataKey, Object>) in.readObject()).entrySet()) {
            dataCache.updateValue(entry.getKey(), entry.getValue());
        }
    }

    @EventHandler
    public void onServerChange(ServerConnectedEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        playerInformation.invalidate(player.getUniqueId());
    }

    @EventHandler
    public void onDisconnect(PlayerDisconnectEvent event) {
        final ProxiedPlayer player = event.getPlayer();

        playerInformation.invalidate(player.getUniqueId());
    }

    public <T> Optional<T> get(ServerInfo server, DataKey<T> key) {
        Optional<T> value = getServerDataCache(server.getName()).getValue(key);
        if (!value.isPresent()) {
            try {
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(os);
                out.writeUTF(Constants.subchannelRequestServerVariable);
                out.writeObject(key);
                out.close();
                server.sendData(Constants.channel, os.toByteArray(), false);
            } catch (IOException ex) {
                plugin.getLogger().log(Level.SEVERE, "Error while requesting data from bukkit", ex);
            }
        }
        return value;
    }

    public <T> Optional<T> get(IPlayer player, DataKey<T> key) {
        if (key.getScope() == DataKey.Scope.SERVER) {
            return player.getServer().flatMap(server -> get(server, key));
        }
        UUID uniqueID = player.getUniqueID();
        if (uniqueID != null) {
            Optional<T> value = getPlayerDataCache(uniqueID).getValue(key);
            if (!value.isPresent()) {
                ProxiedPlayer proxiedPlayer = plugin.getProxy().getPlayer(uniqueID);
                if (proxiedPlayer != null) {
                    try {
                        ByteArrayOutputStream os = new ByteArrayOutputStream();
                        ObjectOutputStream out = new ObjectOutputStream(os);
                        out.writeUTF(Constants.subchannelRequestPlayerVariable);
                        out.writeObject(key);
                        out.close();
                        Optional.ofNullable(proxiedPlayer.getServer()).ifPresent(server -> server.sendData(Constants.channel, os.toByteArray()));
                    } catch (IOException ex) {
                        plugin.getLogger().log(Level.SEVERE, "Error while requesting data from bukkit", ex);
                    }
                }
            }
            return value;
        }
        return Optional.empty();
    }

    private void requestReset(ProxiedPlayer player) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeUTF(Constants.subchannelRequestResetPlayerVariables);
            out.close();
            player.sendData(Constants.channel, os.toByteArray());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error while requesting data from bukkit", ex);
        }
    }

    private void requestReset(Server server) {
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(os);
            out.writeUTF(Constants.subchannelRequestResetServerVariables);
            out.close();
            server.sendData(Constants.channel, os.toByteArray());
        } catch (IOException ex) {
            plugin.getLogger().log(Level.SEVERE, "Error while requesting data from bukkit", ex);
        }
    }
}
