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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.DataKey;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.IPlayerProvider;
import codecrafter47.bungeetablistplus.player.RedisPlayer;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import com.imaginarycode.minecraft.redisbungee.events.PubSubMessageEvent;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.stream.Collectors;


public class RedisPlayerManager implements IPlayerProvider, Listener {

    private static String CHANNEL_REQUEST_DATA = "btlp-data-request";
    private static String CHANNEL_DATA = "btlp-data";

    private List<RedisPlayer> playerList = Collections.emptyList();
    private Map<UUID, RedisPlayer> byUUID = new ConcurrentHashMap<>();
    private final ConnectedPlayerManager connectedPlayerManager;

    public RedisPlayerManager(ConnectedPlayerManager connectedPlayerManager) {
        this.connectedPlayerManager = connectedPlayerManager;
        RedisBungee.getApi().registerPubSubChannels(CHANNEL_REQUEST_DATA, CHANNEL_DATA);
        ProxyServer.getInstance().getScheduler().schedule(BungeeTabListPlus.getInstance().getPlugin(), this::updatePlayers, 1, 1, TimeUnit.SECONDS);
        ProxyServer.getInstance().getPluginManager().registerListener(BungeeTabListPlus.getInstance().getPlugin(), this);
    }

    @Override
    public Collection<RedisPlayer> getPlayers() {
        return playerList;
    }

    @EventHandler
    @SuppressWarnings("unchecked")
    public void onRedisMessage(PubSubMessageEvent event) {
        if (event.getChannel().equals(CHANNEL_REQUEST_DATA)) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(event.getMessage()));
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                UUID uuid = (UUID) objectInputStream.readObject();
                DataKey<Object> dataKey = (DataKey<Object>) objectInputStream.readObject();
                objectInputStream.close();

                ConnectedPlayer player = connectedPlayerManager.getPlayerIfPresent(uuid);
                if (player != null) {
                    player.registerDataChangeListener(dataKey, new DataChangeListener(uuid, dataKey));
                    updateData(uuid, dataKey, player.get(dataKey).orElse(null));
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to process data from BungeeTabListPlus running on another BungeeCord instance", th);
            }
        } else if (event.getChannel().equals(CHANNEL_DATA)) {
            try {
                ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64.getDecoder().decode(event.getMessage()));
                ObjectInputStream objectInputStream = new ObjectInputStream(inputStream);
                UUID uuid = (UUID) objectInputStream.readObject();
                DataKey<Object> dataKey = (DataKey<Object>) objectInputStream.readObject();
                Object value = objectInputStream.readObject();
                objectInputStream.close();

                RedisPlayer player = byUUID.get(uuid);
                if (player != null) {
                    BungeeTabListPlus.getInstance().runInMainThread(() -> player.getData().updateValue(dataKey, value));
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to process data from BungeeTabListPlus running on another BungeeCord instance", th);
            }
        }
    }

    private void updatePlayers() {
        Set<UUID> playersOnline = RedisBungee.getApi().getPlayersOnline();

        // remove players which have gone offline
        for (Iterator<UUID> iterator = byUUID.keySet().iterator(); iterator.hasNext(); ) {
            UUID uuid = iterator.next();
            if (!playersOnline.contains(uuid) || connectedPlayerManager.getPlayerIfPresent(uuid) != null) {
                iterator.remove();
            }
        }

        // add new players
        for (UUID uuid : playersOnline) {
            if (!byUUID.containsKey(uuid) && connectedPlayerManager.getPlayerIfPresent(uuid) == null) {
                byUUID.put(uuid, new RedisPlayer(uuid));
            }
        }

        // update list
        playerList = byUUID.values().stream().filter(RedisPlayer::hasName).collect(Collectors.toList());
    }

    public <T> void request(UUID uuid, DataKey<T> key) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(uuid);
            objectOutputStream.writeObject(key);
            objectOutputStream.close();
            byte[] bytes = outputStream.toByteArray();
            RedisBungee.getApi().sendChannelMessage(CHANNEL_REQUEST_DATA, Base64.getEncoder().encodeToString(bytes));
        } catch (RuntimeException ex) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "RedisBungee Error", ex);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to request data", th);
        }
    }

    public <T> void updateData(UUID uuid, DataKey<T> key, T value) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(outputStream);
            objectOutputStream.writeObject(uuid);
            objectOutputStream.writeObject(key);
            objectOutputStream.writeObject(value);
            objectOutputStream.close();
            byte[] bytes = outputStream.toByteArray();
            RedisBungee.getApi().sendChannelMessage(CHANNEL_DATA, Base64.getEncoder().encodeToString(bytes));
        } catch (RuntimeException ex) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "RedisBungee Error", ex);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to send data", th);
        }
    }

    private class DataChangeListener implements Consumer<Object> {
        private final UUID uuid;
        private final DataKey<Object> dataKey;

        public DataChangeListener(UUID uuid, DataKey<Object> dataKey) {
            this.uuid = uuid;
            this.dataKey = dataKey;
        }

        @Override
        public void accept(Object value) {
            RedisPlayerManager.this.updateData(uuid, dataKey, value);
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            DataChangeListener that = (DataChangeListener) o;

            if (!uuid.equals(that.uuid)) return false;
            return dataKey.equals(that.dataKey);

        }

        @Override
        public int hashCode() {
            int result = uuid.hashCode();
            result = 31 * result + dataKey.hashCode();
            return result;
        }
    }
}
