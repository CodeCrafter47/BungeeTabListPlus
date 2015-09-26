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

package codecrafter47.bungeetablistplus.api.bungee.tablist;

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.PlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Optional;

/**
 * Context object holding all necessary data required for replacing variables
 */
public interface TabListContext {
    /**
     * get the size of the tab list
     * in most cases this is getRows() * getColumns()
     *
     * @return the size of the tab list
     */
    int getTabSize();

    /**
     * get the number of rows in the tab list
     *
     * @return the number of rows in the tab list
     */
    int getRows();

    /**
     * get the number of columns in the tab list
     *
     * @return the number of columns in the tab list
     */
    int getColumns();

    /**
     * get the player who will see the tab list
     *
     * @return the player
     */
    ProxiedPlayer getViewer();

    /**
     * get the PlayerManager
     * the PlayerManager can be used to get player counts and lists of players
     *
     * @return the player manager
     */
    PlayerManager getPlayerManager();

    /**
     * get the player
     * in case of resolving a variable this is the player the variable should
     * be resolved for
     *
     * @return the player
     */
    IPlayer getPlayer();

    /**
     * get the server
     *
     * @return the server
     */
    Optional<ServerInfo> getServer();

    /**
     * get the server group
     *
     * @return the server group
     */
    Optional<ServerGroup> getServerGroup();

    /**
     * get the number of players not shown in the tab list
     * this is used by the {other_count} variable
     *
     * @return number of players not shown in the tab list
     */
    int getOtherPlayerCount();

    /**
     * create a copy of the TabListContext and set the player
     * also sets server and server group to the server associated with the player
     *
     * @param player the player
     * @return a copy of this TabListContext with player set to the given parameter
     */
    TabListContext setPlayer(IPlayer player);

    /**
     * create a copy of this TabListContext set the number of players not in the tablist
     *
     * @param otherCount number of players not shown in the tab list
     * @return a copy of this TabListContext with the number of players not in the tablist set
     */
    TabListContext setOtherCount(int otherCount);

    /**
     * create a copy of the TabListContext and set the server group
     * if the server group consists of a single server, server is set to
     *
     * @param serverGroup the server group
     * @return a copy of this TabListContext with server group changed
     */
    TabListContext setServerGroup(ServerGroup serverGroup);
}
