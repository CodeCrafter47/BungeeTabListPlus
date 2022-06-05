/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.api.bungee;

import lombok.NonNull;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * @deprecated The custom tab list api has been changed. See {@link BungeeTabListPlusAPI#getTabViewForPlayer(ProxiedPlayer)}
 */
@Deprecated
public interface CustomTablist {
    /**
     * Set the size of the tab list.
     * <p>
     * Recommended values:
     * <table>
     * <caption></caption>
     * <thead>
     * <tr>
     * <th>Size</th>
     * <th>Columns</th>
     * <th>Rows</th>
     * </tr>
     * </thead>
     * <tbody>
     * <tr>
     * <td>0</td>
     * <td>0</td>
     * <td>0</td>
     * </tr>
     * <tr>
     * <td>10</td>
     * <td>1</td>
     * <td>10</td>
     * </tr>
     * <tr>
     * <td>20</td>
     * <td>1</td>
     * <td>20</td>
     * </tr>
     * <tr>
     * <td>30</td>
     * <td>2</td>
     * <td>15</td>
     * </tr>
     * <tr>
     * <td>40</td>
     * <td>2</td>
     * <td>20</td>
     * </tr>
     * <tr>
     * <td>60</td>
     * <td>3</td>
     * <td>20</td>
     * </tr>
     * <tr>
     * <td>80</td>
     * <td>4</td>
     * <td>20</td>
     * </tr>
     * </tbody>
     * </table>
     *
     * @param size new size of the tab list
     * @throws IllegalArgumentException if the given size is not allowed
     */
    void setSize(int size);

    /**
     * Get the size of the tab list
     * This is the same as getRows() * getColumns()
     *
     * @return the size of the tablist
     */
    int getSize();

    /**
     * Get the number of rows in the tab list
     *
     * @return the number of rows
     */
    int getRows();

    /**
     * Get the number of columns in the tab list
     *
     * @return the number of columns
     */
    int getColumns();

    /**
     * Get the icon of the slot at the position specified by row and column
     *
     * @param row    the row
     * @param column the column
     * @return the icon at the given position
     */
    @Nonnull
    Icon getIcon(int row, int column);

    /**
     * Get the text of the slot at the position specified by row and column
     *
     * @param row    the row
     * @param column the column
     * @return the text at the given position
     */
    @Nonnull
    String getText(int row, int column);

    /**
     * Get the ping of the slot at the position specified by row and column
     *
     * @param row    the row
     * @param column the column
     * @return the ping at the given position
     */
    int getPing(int row, int column);

    /**
     * Set the slot at a position specified by row and column
     *
     * @param row    the row
     * @param column the column
     * @param icon   the icon
     * @param text   the text
     * @param ping   the ping
     */
    void setSlot(int row, int column, @Nonnull @NonNull Icon icon, @Nonnull @NonNull String text, int ping);

    /**
     * Get the header set for the tab list
     *
     * @return the header
     */
    @Nullable
    String getHeader();

    /**
     * Set the header for the tab list
     * may contain color codes like &amp;6
     *
     * @param header the header
     */
    void setHeader(@Nullable String header);

    /**
     * Get the footer for the tab list
     *
     * @return the footer
     */
    @Nullable
    String getFooter();

    /**
     * Set the footer
     *
     * @param footer the footer
     */
    void setFooter(@Nullable String footer);
}
