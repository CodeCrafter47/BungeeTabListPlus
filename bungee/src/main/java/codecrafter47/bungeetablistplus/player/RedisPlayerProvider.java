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
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Collections2;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;


public class RedisPlayerProvider implements IPlayerProvider {

    private final Cache<UUID, RedisPlayer> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();
    private Collection<? extends IPlayer> players = Collections.emptySet();

    public RedisPlayerProvider() {
        ProxyServer.getInstance().getScheduler().schedule(BungeeTabListPlus.getInstance().getPlugin(), new Runnable() {
            @Override
            public void run() {
                players = getPlayers0();
            }
        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public Collection<? extends IPlayer> getPlayers() {
        return players;
    }

    private Collection<? extends IPlayer> getPlayers0() {
        return Collections2.filter(Collections2.transform(RedisBungee.getApi().getPlayersOnline(), new Function<UUID, RedisPlayer>() {
            @Override
            public RedisPlayer apply(UUID player) {
                return wrapPlayer(player);
            }
        }), new Predicate<RedisPlayer>() {
            @Override
            public boolean apply(RedisPlayer input) {
                return input.hasName();
            }
        });
    }

    @SneakyThrows
    RedisPlayer wrapPlayer(final UUID player) {
        return cache.get(player, new Callable<RedisPlayer>() {
            @Override
            public RedisPlayer call() {
                return new RedisPlayer(player);
            }
        });
    }
}
