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
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

public class DelegatingTabListContext extends TabListContext {
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
    public List<String> getServer() {
        return parent.getServer();
    }

    @Override
    public int getOtherPlayerCount() {
        return parent.getOtherPlayerCount();
    }
}
