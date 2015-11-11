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
import com.google.common.base.Preconditions;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

public class GenericTabListContext extends AbstractTabListContext {
    private final int rows;
    private final int columns;
    private final int size;
    private final ProxiedPlayer player;
    private final PlayerManager playerManager;

    public GenericTabListContext(int rows, int columns, ProxiedPlayer player, PlayerManager playerManager) {
        Preconditions.checkArgument(rows > 0, "tab list has 0 rows");
        Preconditions.checkArgument(columns > 0, "tab list has 0 columns");
        this.rows = rows;
        this.columns = columns;
        this.playerManager = playerManager;
        this.size = rows * columns;
        this.player = player;
    }

    @Override
    public int getTabSize() {
        return size;
    }

    @Override
    public int getRows() {
        return rows;
    }

    @Override
    public int getColumns() {
        return columns;
    }

    @Override
    public ProxiedPlayer getViewer() {
        return player;
    }

    @Override
    public PlayerManager getPlayerManager() {
        return playerManager;
    }

    @Override
    public IPlayer getPlayer() {
        throw new IllegalStateException("player not available");
    }

    @Override
    public Optional<ServerInfo> getServer() {
        return Optional.empty();
    }

    @Override
    public Optional<ServerGroup> getServerGroup() {
        return Optional.empty();
    }

    @Override
    public int getOtherPlayerCount() {
        throw new IllegalStateException("other_count not available");
    }
}
