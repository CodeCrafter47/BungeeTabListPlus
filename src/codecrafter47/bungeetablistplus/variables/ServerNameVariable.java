package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.api.ServerVariable;
import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import net.md_5.bungee.api.config.ServerInfo;

public class ServerNameVariable implements ServerVariable {

	@Override
	public String getReplacement(String args, ServerInfo server) {
		return BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().getServerAlias(server.getName());
	}

}
