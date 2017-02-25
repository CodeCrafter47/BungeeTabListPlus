/*
 * BungeeTabListPlus - a BungeeCord plugin to customize the tablist
 *
 * Copyright (C) 2014 - 2015 Florian Stober
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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import de.codecrafter47.data.bungee.api.BungeeData;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;

import java.util.Optional;

public abstract class AbstractPlayer implements Player {

    @Override
    public Optional<ServerInfo> getServer() {
        String serverName = get(BungeeData.BungeeCord_Server);
        if (serverName == null) {
            return Optional.empty();
        } else {
            return Optional.ofNullable(ProxyServer.getInstance().getServerInfo(serverName));
        }
    }

    @Override
    public int getPing() {
        Integer ping = get(BungeeData.BungeeCord_Ping);
        return ping != null ? ping : 0;
    }

    @Override
    public int getGameMode() {
        Integer gamemode = get(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE);
        return gamemode != null ? gamemode : 0;
    }
}
