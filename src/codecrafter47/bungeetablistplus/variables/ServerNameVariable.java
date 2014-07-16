package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.config.ServerInfo;

public class ServerNameVariable extends ServerVariable {

	public ServerNameVariable(String name) {
		super(name);
	}

	@Override
	public String getReplacement(ServerInfo server) {
		return BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getServerAlias(server.getName());
	}

}
