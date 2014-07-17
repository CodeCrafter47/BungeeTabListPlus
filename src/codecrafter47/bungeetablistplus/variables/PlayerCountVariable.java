package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.Variable;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ProxyServer;

public class PlayerCountVariable implements Variable {

    @Override
    public String getReplacement(String args) {
        if (false && !BungeeTabListPlus.areHiddenPlayers()) {
            return "" + ProxyServer.getInstance().getOnlineCount();
        } else {
            return "" + BungeeTabListPlus.getInstance().getPlayerManager().getGlobalPlayerCount();
        }
    }

}
