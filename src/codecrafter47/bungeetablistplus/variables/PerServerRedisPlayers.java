package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.ServerVariable;
import com.imaginarycode.minecraft.redisbungee.RedisBungee;
import net.md_5.bungee.api.config.ServerInfo;

/**
 * Created by florian on 30.09.14.
 */
public class PerServerRedisPlayers implements ServerVariable {

    @Override
    public String getReplacement(String args, ServerInfo server) {
        return  Integer.toString(RedisBungee.getApi().getPlayersOnServer(server.getName()).size());
    }
}
