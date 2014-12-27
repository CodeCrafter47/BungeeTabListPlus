package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.Variable;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * Created by florian on 30.09.14.
 */
public class RedisPlayers implements Variable {

    @Override
    public String getReplacement(ProxiedPlayer viewer, String args) {
        if (args == null) {
            return Integer.toString(RedisBungee.getApi().getPlayerCount());
        }
        int i = 0;
        for (String server : args.split(",")) {
            if (ProxyServer.getInstance().getServers().containsKey(server)) {
                i += RedisBungee.getApi().getPlayersOnServer(server).size();
            }
        }
        return Integer.toString(i);
    }
}
