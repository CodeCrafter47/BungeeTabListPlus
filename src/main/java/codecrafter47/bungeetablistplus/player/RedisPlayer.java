package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.skin.Skin;
import com.google.common.base.Optional;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class RedisPlayer implements IPlayer {
    private String name;
    private final UUID uuid;
    private ServerInfo server;
    private long lastServerLookup = 0;

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

    @Override
    public Skin getSkin() {
        return BungeeTabListPlus.getInstance().getSkinManager().getSkin(uuid.toString());
    }
}
