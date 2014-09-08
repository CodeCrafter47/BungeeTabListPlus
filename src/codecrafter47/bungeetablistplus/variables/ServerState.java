/*
 * Copyright (C) 2014 Florian Stober
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.bungeetablistplus.variables;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.ServerVariable;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

/**
 *
 * @author florian
 */
public class ServerState implements ServerVariable {

    @Override
    public String getReplacement(String args, ServerInfo server) {
        if (args != null) {
            server = ProxyServer.getInstance().getServerInfo(args);
        }
        boolean isOnline = BungeeTabListPlus.getInstance().getServerState(
                server.getName()).isOnline();
        String replacement = isOnline ? BungeeTabListPlus.getInstance().
                getConfigManager().getMainConfig().online_text : BungeeTabListPlus.
                getInstance().getConfigManager().getMainConfig().offline_text;
        return replacement;
    }

}
