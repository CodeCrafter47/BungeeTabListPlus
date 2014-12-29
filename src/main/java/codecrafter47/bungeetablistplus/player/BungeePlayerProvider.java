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

/**
 * Created by florian on 27.12.14.
 */
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
