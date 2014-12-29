package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.skin.Skin;
import com.google.common.base.Charsets;
import com.google.common.base.Optional;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public class FakePlayer implements IPlayer {
    String name;
    ServerInfo server;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getUniqueID() {
        return UUID.nameUUIDFromBytes(("OfflinePlayer:" + name).getBytes(Charsets.UTF_8));
    }

    @Override
    public Optional<ServerInfo> getServer() {
        return Optional.of(server);
    }

    @Override
    public int getPing() {
        // yeah... faked players have always good ping
        return 0;
    }

    @Override
    public Skin getSkin() {
        return BungeeTabListPlus.getInstance().getSkinManager().getSkin(name);
    }
}
