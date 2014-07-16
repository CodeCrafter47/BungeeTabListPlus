package codecrafter47.bungeetablistplus.variables;

import net.md_5.bungee.api.connection.ProxiedPlayer;

public abstract class PlayerVariable {
	private final String name;
	
	public PlayerVariable(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract String getReplacement(ProxiedPlayer player);
}
