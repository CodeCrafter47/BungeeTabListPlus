package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.PlayerVariable;
import codecrafter47.bungeetablistplus.api.Variable;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class ServerPlayerCountVariable implements PlayerVariable {

    @Override
    public String getReplacement(String args, ProxiedPlayer player) {
        if (args == null) {
            return Integer.toString(BungeeTabListPlus.getInstance().
                    getPlayerManager().getGlobalPlayerCount());
        }
        return Integer.toString(BungeeTabListPlus.getInstance().
                getPlayerManager().getPlayerCount(args, player));
    }

}
