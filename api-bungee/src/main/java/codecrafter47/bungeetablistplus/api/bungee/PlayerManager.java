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

package codecrafter47.bungeetablistplus.api.bungee;

import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

/**
 * The player manager can be used to get access to player counts and the
 * players which are shown in the tab list
 */
public interface PlayerManager {
    /**
     * gets a list of players
     * Does not include hidden players
     *
     * @param filter decides which players you want
     * @return the resulting list of players
     */
    List<IPlayer> getPlayers(Filter filter);

    /**
     * The number of players on a server
     * Does not count hidden players
     *
     * @param server the name of the server
     * @return the number of players on the given server
     */
    int getServerPlayerCount(String server);

    /**
     * The number of players on the network
     * Does not count hidden players
     *
     * @return the number of players on the given server
     */
    int getGlobalPlayerCount();

    /**
     * Counts players
     * Does not count hidden players
     *
     * @param filter decides which players you want
     * @return the number of players on the given server
     */
    int getPlayerCount(Filter filter);

    interface Filter {

        boolean test(ProxiedPlayer viewer, IPlayer player);
    }
}
