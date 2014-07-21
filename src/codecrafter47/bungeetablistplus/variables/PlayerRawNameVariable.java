package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.PlayerVariable;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerRawNameVariable implements PlayerVariable {

    @Override
    public String getReplacement(String args, ProxiedPlayer player) {
        return player.getName();
    }

}
