package codecrafter47.bungeetablistplus.api;

import net.md_5.bungee.api.config.ServerInfo;

public abstract interface ServerVariable {
	public abstract String getReplacement(String args, ServerInfo server);
}
