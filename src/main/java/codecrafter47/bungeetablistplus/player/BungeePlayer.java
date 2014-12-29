package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.skin.PlayerSkin;
import codecrafter47.bungeetablistplus.skin.Skin;
import codecrafter47.bungeetablistplus.managers.SkinManager;
import com.google.common.base.Optional;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.connection.LoginResult;

import java.util.UUID;

public class BungeePlayer implements IPlayer {

    private final ProxiedPlayer player;
    private Skin skin = null;

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

    @Override
    public Skin getSkin() {
        if (skin == null) {
            LoginResult loginResult = ((UserConnection) player).
                    getPendingConnection().getLoginProfile();
            if (loginResult != null) {
                for (LoginResult.Property s : loginResult.getProperties()) {
                    if (s.getName().equals("textures")) {
                        skin = new PlayerSkin(player.getUniqueId(), new String[]{s.getName(), s.getValue(), s.getSignature()});
                    }
                }
            }
            if (skin == null) {
                skin = SkinManager.defaultSkin;
            }
        }
        return skin;
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }
}
