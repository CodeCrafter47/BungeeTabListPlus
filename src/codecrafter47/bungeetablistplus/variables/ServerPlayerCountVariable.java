package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.Variable;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class ServerPlayerCountVariable implements Variable {

    @Override
    public String getReplacement(String args) {
        if (args == null) {
            return Integer.toString(BungeeTabListPlus.getInstance().getPlayerManager().getGlobalPlayerCount());
        }
        return Integer.toString(BungeeTabListPlus.getInstance().getPlayerManager().getServerPlayerCount(args));
    }

}
