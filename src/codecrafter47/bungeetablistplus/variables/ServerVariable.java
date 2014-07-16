package codecrafter47.bungeetablistplus.variables;

import net.md_5.bungee.api.config.ServerInfo;

public abstract class ServerVariable {
private final String name;
	
	public ServerVariable(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public abstract String getReplacement(ServerInfo server);
}
