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
import lombok.SneakyThrows;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

public class BungeePlayerProvider implements IPlayerProvider {

    private final Cache<ProxiedPlayer, IPlayer> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

    @Override
    public Collection<IPlayer> getPlayers() {
        return Collections2.transform(ProxyServer.getInstance().getPlayers(), new Function<ProxiedPlayer, IPlayer>() {
            @Override
            public IPlayer apply(ProxiedPlayer player) {
                return wrapPlayer(player);
            }
        });
    }

    @SneakyThrows
    public IPlayer wrapPlayer(final ProxiedPlayer player) {
        return cache.get(player, new Callable<IPlayer>() {
            @Override
            public IPlayer call() {
                return new BungeePlayer(player);
            }
        });
    }
}
