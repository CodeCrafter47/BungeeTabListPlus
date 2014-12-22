package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.Variable;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.ProxyServer;

/**
 * Created by florian on 30.09.14.
 */
public class RedisPlayers implements Variable {

    @Override
    public String getReplacement(String args) {
        if (args == null) {
            return Integer.toString(RedisBungee.getApi().getPlayerCount());
        }
        int i = 0;
        for (String server : args.split(",")) {
            if (ProxyServer.getInstance().getServers().containsKey(args)) {
                i += RedisBungee.getApi().getPlayersOnServer(args).size();
            }
        }
        return Integer.toString(i);
    }
}
