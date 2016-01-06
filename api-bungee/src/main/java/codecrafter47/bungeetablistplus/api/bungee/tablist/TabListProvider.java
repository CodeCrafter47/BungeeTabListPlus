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

import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 * To create a custom tab list all methods in this interface must be implemented.
 */
public interface TabListProvider {

    /**
     * This method is periodically invoked by BungeeTabListPlus to update the tablist.
     * <p>
     * This method must fill the tablist with it's content. It cannot change the size of
     * the tab list.
     * <p>
     * This method will not be invoked concurrently.
     *
     * @param player  the player who will see the tab list
     * @param tabList an empty TabList
     * @param context the TabListContext
     */
    void fillTabList(ProxiedPlayer player, TabList tabList, TabListContext context);

    /**
     * This method may be called by BungeeTabListPlus. You can use it to tell BungeeTabListPlus how big you like
     * the tab list to be. The plugin will do its best to fulfill your request. However there is no guarantee
     * that the tab list will have the size you asked for. You can query the tab list size from the TabListContext
     * object in the fillTabList method.
     *
     * @return requested tab list size
     */
    default int getWishedTabListSize() {
        return 80;
    }
}
