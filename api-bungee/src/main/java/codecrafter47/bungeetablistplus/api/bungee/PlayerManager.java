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

import java.util.Collection;
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
     * @param filter            names of servers or permission groups, if provided only players on these
     *                          servers/ in these permission groups will be shown in the tab list
     * @param who               the player who will see the tablist
     * @param includeSpectators whether players in spectator mode should be included
     * @return the resulting list of players
     */
    List<IPlayer> getPlayers(Collection<String> filter, ProxiedPlayer who, boolean includeSpectators);

    /**
     * The number of players on a server
     * Does not count hidden players
     *
     * @param server            the name of the server
     * @param viewer            the player who will see that tablist
     * @param includeSpectators whether players in spectator mode should be included
     * @return the number of players on the given server
     */
    int getServerPlayerCount(String server, ProxiedPlayer viewer, boolean includeSpectators);

    /**
     * The number of players on the network
     * Does not count hidden players
     *
     * @param viewer            the player who will see that tablist
     * @param includeSpectators whether players in spectator mode should be included
     * @return the number of players on the given server
     */
    int getGlobalPlayerCount(ProxiedPlayer viewer, boolean includeSpectators);

    /**
     * Counts players
     * Does not count hidden players
     *
     * @param filter            names of servers or permission groups, if provided only players on these
     *                          servers/ in these permission groups will be shown in the tab list
     * @param viewer            the player who will see that tablist
     * @param includeSpectators whether players in spectator mode should be included
     * @return the number of players on the given server
     */
    int getPlayerCount(Collection<String> filter, ProxiedPlayer viewer, boolean includeSpectators);
}
