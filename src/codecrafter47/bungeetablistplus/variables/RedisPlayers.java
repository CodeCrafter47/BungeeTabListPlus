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
        if(args != null && ProxyServer.getInstance().getServers().containsKey(args)){
            return  Integer.toString(RedisBungee.getApi().getPlayersOnServer(args).size());
        }
        return Integer.toString(RedisBungee.getApi().getPlayerCount());
    }
}
