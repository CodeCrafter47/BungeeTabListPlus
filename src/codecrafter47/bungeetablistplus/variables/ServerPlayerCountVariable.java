package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

public class ServerPlayerCountVariable extends Variable {

    private final String server11;

    public ServerPlayerCountVariable(String name, String server) {
        super(name);
        this.server11 = server;
    }

    @Override
    public String getReplacement() {
        if (false && !BungeeTabListPlus.areHiddenPlayers()) {
            ServerInfo server = ProxyServer.getInstance().getServerInfo(server11);
            if (server == null) {
                return "";
            }
            return "" + server.getPlayers().size();
        } else {
            return "" + BungeeTabListPlus.getInstance().getPlayerManager().getServerPlayerCount(server11);
        }
    }

}
