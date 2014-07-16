package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import java.util.Arrays;
import net.md_5.bungee.api.config.ServerInfo;

public class CurrentServerPlayerCountVariable extends ServerVariable {

    public CurrentServerPlayerCountVariable(String name) {
        super(name);
    }

    @Override
    public String getReplacement(ServerInfo server) {
        if (!BungeeTabListPlus.areHiddenPlayers()) {
            return "" + server.getPlayers().size();
        } else {
            return "" + BungeeTabListPlus.getInstance().getPlayerManager().getServerPlayerCount(server.getName());
        }
    }

}
