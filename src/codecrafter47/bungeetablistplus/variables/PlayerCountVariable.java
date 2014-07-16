package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ProxyServer;

public class PlayerCountVariable extends Variable {

    public PlayerCountVariable(String name) {
        super(name);
    }

    @Override
    public String getReplacement() {
        if (false && !BungeeTabListPlus.areHiddenPlayers()) {
            return "" + ProxyServer.getInstance().getOnlineCount();
        } else {
            return "" + BungeeTabListPlus.getInstance().getPlayerManager().getGlobalPlayerCount();
        }
    }

}
