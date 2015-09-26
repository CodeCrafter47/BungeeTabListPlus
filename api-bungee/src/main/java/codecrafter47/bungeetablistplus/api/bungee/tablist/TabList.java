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

import codecrafter47.bungeetablistplus.api.bungee.Skin;

/**
 * Represents a tablist
 */
public interface TabList {
    /**
     * get the header set for the tab list
     *
     * @return the header
     */
    String getHeader();

    /**
     * set the header for the tab list
     * may contain color codes like &6
     *
     * @param header the header
     */
    void setHeader(String header);

    /**
     * get the footer for the tab list
     *
     * @return the footer
     */
    String getFooter();

    /**
     * set the footer
     *
     * @param footer the footer
     */
    void setFooter(String footer);

    /**
     * get the number of rows in the tab list
     *
     * @return the number of rows
     */
    int getRows();

    /**
     * get the number of columns in the tab list
     *
     * @return the number of columns
     */
    int getColumns();

    int getUsedSlots();

    /**
     * get the slot at an index
     * the index works left-to-right first, then top-to-bottom
     *
     * @param n the index
     * @return the slot at the given index or null
     */
    Slot getSlot(int n);

    /**
     * get the slot at the position specified by row and column
     *
     * @param row    the row
     * @param column the column
     * @return the slot at the given position or null
     */
    Slot getSlot(int row, int column);

    /**
     * set the slot at a position given as an index
     * the index works left-to-right first, then top-to-bottom
     *
     * @param n the index
     * @param s the slot
     */
    void setSlot(int n, Slot s);

    /**
     * set the slot at a position specified by row and column
     *
     * @param row    the row
     * @param column the column
     * @param s      the slot
     */
    void setSlot(int row, int column, Slot s);

    /**
     * The ping to use for unset slots
     *
     * @return the default
     */
    int getDefaultPing();

    /**
     * Set the ping to use for unset slots
     *
     * @param defaultPing the ping
     */
    void setDefaultPing(int defaultPing);

    /**
     * get the size of the tablist
     * usually this is the same as getRows() * getColumns()
     *
     * @return the size of the tablist
     */
    int getSize();

    /**
     * get the default skin
     * the default skin is used for all unset slots
     *
     * @return the default skin
     */
    Skin getDefaultSkin();

    /**
     * set the default skin
     * the default skin is used for all unset slots
     *
     * @param defaultSkin the skin to use as new default skin
     */
    void setDefaultSkin(Skin defaultSkin);

    /**
     * get a flipped live view of the tab list.
     *
     * @return a tablist with rows and columns flipped
     */
    TabList flip();

    /**
     * whether the tab list will be shrinked before sending to the client
     * this defaults to false
     *
     * @return whether the tab list will be shrinked before sending to the client
     */
    boolean shouldShrink();

    /**
     * set whether the tab list will be shrinked before sending to the client
     * if set to true only filled slots will be sent to the client
     * this will mess up the layout so use with caution
     *
     * @param shouldShrink whether the tab list will be shrinked before sending to the client
     */
    void setShouldShrink(boolean shouldShrink);
}
