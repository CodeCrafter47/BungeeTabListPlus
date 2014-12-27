package codecrafter47.bungeetablistplus.player;

import com.google.common.base.Optional;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.UUID;

/**
 * Created by florian on 27.12.14.
 */
public interface IPlayer {
    String getName();

    UUID getUniqueID();

    Optional<ServerInfo> getServer();

    int getPing();
}
