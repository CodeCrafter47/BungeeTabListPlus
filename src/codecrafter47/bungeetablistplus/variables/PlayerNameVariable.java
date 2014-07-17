package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.PlayerVariable;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerNameVariable implements PlayerVariable {

    @Override
    public String getReplacement(String args, ProxiedPlayer player) {
        String vname = BungeeTabListPlus.getInstance().getBridge().getPlayerInformation(player, "tabName");
        if (vname != null) {
            return vname;
        }
        return player.getDisplayName();
    }

}
