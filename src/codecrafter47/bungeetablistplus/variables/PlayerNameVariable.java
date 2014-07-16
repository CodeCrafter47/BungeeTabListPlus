package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerNameVariable extends PlayerVariable {

    public PlayerNameVariable(String name) {
        super(name);
    }

    @Override
    public String getReplacement(ProxiedPlayer player) {
        String vname = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(player, "tabName");
        if (vname != null) {
            return vname;
        }
        return player.getDisplayName();
    }

}
