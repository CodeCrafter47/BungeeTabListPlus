/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package codecrafter47.bungeetablistplus.player;

import com.google.common.base.Function;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import lombok.SneakyThrows;

import java.util.Collection;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


public class RedisPlayerProvider implements IPlayerProvider {

    private final Cache<UUID, IPlayer> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
    private Collection<IPlayer> players;
    private long last = 0;

    @Override
    public Collection<IPlayer> getPlayers() {
        long now = System.currentTimeMillis();
        if (now - last > 2000) {
            players = getPlayers0();
            last = now;
        }
        return players;
    }

    private Collection<IPlayer> getPlayers0() {
        return Collections2.transform(RedisBungee.getApi().getPlayersOnline(), new Function<UUID, IPlayer>() {
            @Override
            public IPlayer apply(UUID player) {
                return wrapPlayer(player);
            }
        });
    }

    @SneakyThrows
    IPlayer wrapPlayer(final UUID player) {
        return cache.get(player, new Callable<IPlayer>() {
            @Override
            public IPlayer call() {
                return new RedisPlayer(player);
            }
        });
    }
}
