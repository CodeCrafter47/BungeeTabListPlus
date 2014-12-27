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

    Cache<UUID, IPlayer> cache = CacheBuilder.newBuilder().expireAfterAccess(10, TimeUnit.SECONDS).build();

    @Override
    public Collection<IPlayer> getPlayers() {
        return Collections2.transform(RedisBungee.getApi().getPlayersOnline(), new Function<UUID, IPlayer>() {
            @Override
            public IPlayer apply(UUID player) {
                return wrapPlayer(player);
            }
        });
    }

    @SneakyThrows
    public IPlayer wrapPlayer(final UUID player) {
        return cache.get(player, new Callable<IPlayer>() {
            @Override
            public IPlayer call() {
                return new RedisPlayer(player);
            }
        });
    }
}
