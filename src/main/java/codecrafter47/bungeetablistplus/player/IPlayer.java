package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.skin.Skin;
import com.google.common.base.Optional;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

public interface IPlayer {
    String getName();

    UUID getUniqueID();

    Optional<ServerInfo> getServer();

    int getPing();

    Skin getSkin();
}
