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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.data.TrackingDataCache;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.Getter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class RedisPlayer implements Player {
    private String name;
    private final UUID uuid;
    private ServerInfo server;
    private long lastServerLookup = 0;

    @Getter
    private final DataCache data = new TrackingDataCache() {
        @Override
        protected <V> void onMissingData(DataKey<V> key) {
            super.onMissingData(key);
            BungeeTabListPlus.getInstance().getRedisPlayerManager().request(uuid, key);
        }
    };

    public RedisPlayer(UUID uuid) {
        this.uuid = uuid;
        ProxyServer.getInstance().getScheduler().runAsync(BungeeTabListPlus.getInstance().getPlugin(), () -> name = RedisBungee.getApi().getNameFromUuid(RedisPlayer.this.uuid));
    }

    @Override
    @SneakyThrows
    public String getName() {
        if (name == null) {
            return uuid.toString();
        }
        return name;
    }

    public boolean hasName() {
        return name != null;
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public Optional<ServerInfo> getServer() {
        try {
            if (server == null || System.currentTimeMillis() - lastServerLookup > 1000) {
                server = RedisBungee.getApi().getServerFor(uuid);
                lastServerLookup = System.currentTimeMillis();
            }
        } catch (RuntimeException ex) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Error while trying to fetch the server of a player from RedisBungee", ex);
        }
        return Optional.ofNullable(server);
    }

    @Override
    public int getPing() {
        // no way to know the real ping, so we just assume the best
        return 0;
    }

    @Override
    public int getGameMode() {
        return getOpt(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE).orElse(0);
    }

    private DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(BungeeData.SCOPE_BUNGEE_PLAYER)) {
            return data;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return data;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER)) {
            Optional<ServerInfo> server = getServer();
            if (server.isPresent()) {
                return BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(server.get().getName());
            }
            return NullDataHolder.INSTANCE;
        }

        BungeeTabListPlus.getInstance().getLogger().warning("Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }

    @Override
    public <V> V get(DataKey<V> key) {
        return getResponsibleDataHolder(key).get(key);
    }

    @Override
    public <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {
        getResponsibleDataHolder(key).addDataChangeListener(key, listener);
    }

    @Override
    public <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {
        getResponsibleDataHolder(key).removeDataChangeListener(key, listener);
    }
}
