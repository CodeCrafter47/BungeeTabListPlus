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

import java.util.Collection;

/**
 * A group of servers
 */
public interface ServerGroup {
    /**
     * The names of the servers which are part of this group
     *
     * @return the names of the servers as defined in BungeeCords config.yml
     */
    Collection<String> getServerNames();

    /**
     * Get the name of this group. The name returned by this method is the
     * name that should be shown to the users
     *
     * @return the name of this group
     */
    String getName();
}
