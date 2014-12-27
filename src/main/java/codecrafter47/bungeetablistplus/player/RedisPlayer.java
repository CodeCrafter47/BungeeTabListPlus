package codecrafter47.bungeetablistplus.player;

import com.google.common.base.Optional;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

/**
 * Created by florian on 27.12.14.
 */
public class RedisPlayer implements IPlayer {
    String name;
    final UUID uuid;
    ServerInfo server;
    long lastServerLookup = 0;

    public RedisPlayer(UUID uuid) {
        this.uuid = uuid;
    }

    @Override
    public String getName() {
        if (name == null) {
            name = RedisBungee.getApi().getNameFromUuid(uuid);
        }
        return name;
    }

    @Override
    public UUID getUniqueID() {
        return uuid;
    }

    @Override
    public Optional<ServerInfo> getServer() {
        if (server == null || System.currentTimeMillis() - lastServerLookup > 1000) {
            server = RedisBungee.getApi().getServerFor(uuid);
            lastServerLookup = System.currentTimeMillis();
        }
        return Optional.fromNullable(server);
    }

    @Override
    public int getPing() {
        // no way to know the real ping, so we just assume the best
        return 0;
    }
}
