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

package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.managers.PlayerManager;
import codecrafter47.bungeetablistplus.player.IPlayer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public abstract class TabListContext {

    private List<String> server;

    public abstract int getTabSize();

    public abstract int getRows();

    public abstract int getColumns();

    public abstract ProxiedPlayer getViewer();

    public abstract PlayerManager getPlayerManager();

    public abstract IPlayer getPlayer();

    public abstract List<String> getServer();

    public abstract int getOtherPlayerCount();

    public TabListContext setPlayer(IPlayer player) {
        return new DelegatingTabListContext(this) {
            @Override
            public IPlayer getPlayer() {
                return player;
            }

            @Override
            public List<String> getServer() {
                Optional<ServerInfo> server = player.getServer();
                return server.map(ServerInfo::getName).map(Collections::singletonList).orElse(Collections.emptyList());
            }
        };
    }

    public TabListContext setOtherCount(int otherCount) {
        return new DelegatingTabListContext(this) {
            @Override
            public int getOtherPlayerCount() {
                return otherCount;
            }
        };
    }

    public TabListContext setServer(List<String> server) {
        return new DelegatingTabListContext(this) {
            @Override
            public List<String> getServer() {
                return server;
            }
        };
    }
}
