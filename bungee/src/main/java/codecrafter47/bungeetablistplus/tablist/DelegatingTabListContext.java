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

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.PlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

public class DelegatingTabListContext extends AbstractTabListContext {
    private final TabListContext parent;

    public DelegatingTabListContext(TabListContext parent) {
        this.parent = parent;
    }

    @Override
    public int getTabSize() {
        return parent.getTabSize();
    }

    @Override
    public int getRows() {
        return parent.getRows();
    }

    @Override
    public int getColumns() {
        return parent.getColumns();
    }

    @Override
    public ProxiedPlayer getViewer() {
        return parent.getViewer();
    }

    @Override
    public PlayerManager getPlayerManager() {
        return parent.getPlayerManager();
    }

    @Override
    public IPlayer getPlayer() {
        return parent.getPlayer();
    }

    @Override
    public Optional<ServerInfo> getServer() {
        return parent.getServer();
    }

    @Override
    public Optional<ServerGroup> getServerGroup() {
        return parent.getServerGroup();
    }

    @Override
    public int getOtherPlayerCount() {
        return parent.getOtherPlayerCount();
    }
}
