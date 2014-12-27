package codecrafter47.bungeetablistplus.player;

import com.google.common.base.Optional;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.UUID;

/**
 * Created by florian on 27.12.14.
 */
public class BungeePlayer implements IPlayer {

    private ProxiedPlayer player;

    public BungeePlayer(ProxiedPlayer player) {
        this.player = player;
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueID() {
        return player.getUniqueId();
    }

    @Override
    public Optional<ServerInfo> getServer() {
        Server server = player.getServer();
        if (server == null) return Optional.absent();
        return Optional.of(server.getInfo());
    }

    @Override
    public int getPing() {
        return player.getPing();
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
