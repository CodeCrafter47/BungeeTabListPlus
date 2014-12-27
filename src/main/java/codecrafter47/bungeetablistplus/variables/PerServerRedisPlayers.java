package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.ServerVariable;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Created by florian on 30.09.14.
 */
public class PerServerRedisPlayers implements ServerVariable {

    @Override
    public String getReplacement(ProxiedPlayer viewer, ServerInfo server, String args) {
        return Integer.toString(RedisBungee.getApi().getPlayersOnServer(server.getName()).size());
    }
}
