package codecrafter47.bungeetablistplus.variables;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public class PlayerPingVariable extends PlayerVariable{

	public PlayerPingVariable(String name) {
		super(name);
	}

	@Override
	public String getReplacement(ProxiedPlayer player) {
		return "" + player.getPing();
	}
	
}
