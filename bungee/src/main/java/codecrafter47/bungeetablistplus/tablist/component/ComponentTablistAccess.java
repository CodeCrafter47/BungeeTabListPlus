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

package codecrafter47.bungeetablistplus.tablist.component;

import codecrafter47.bungeetablistplus.api.bungee.Icon;
import codecrafter47.bungeetablistplus.tablist.AbstractCustomTablist;
import codecrafter47.bungeetablistplus.util.IntToIntFunction;

/**
 * Provides access to an area of the tab list.
 */
public interface ComponentTablistAccess {

    /**
     * Get the size of the area of the tab list accessible through this object.
     *
     * @return number of slots
     */
    int getSize();

    /**
     * Change the content of a single slot.
     * <p>
     * The index parameter identifies the position of the slot. If the specified
     * slot is outside the area this method does nothing
     *
     * @param index position
     * @param icon  icon
     * @param text  text
     * @param ping  ping
     */
    void setSlot(int index, Icon icon, String text, int ping);

    static ComponentTablistAccess createChild(ComponentTablistAccess parent, int size, int offset) {
        return new ComponentTablistAccess() {
            @Override
            public int getSize() {
                return size;
            }

            @Override
            public void setSlot(int index, Icon icon, String text, int ping) {
                if (index >= 0 && index < size) {
                    parent.setSlot(index + offset, icon, text, ping);
                }
            }
        };
    }

    static ComponentTablistAccess createChild(ComponentTablistAccess parent, int size, IntToIntFunction indexTransform) {
        return new ComponentTablistAccess() {
            @Override
            public int getSize() {
                return size;
            }

            @Override
            public void setSlot(int index, Icon icon, String text, int ping) {
                if (index >= 0 && index < size) {
                    parent.setSlot(indexTransform.apply(index), icon, text, ping);
                }
            }
        };
    }

    static ComponentTablistAccess of(AbstractCustomTablist tablist) {
        return new ComponentTablistAccess() {
            @Override
            public int getSize() {
                return tablist.getSize();
            }

            @Override
            public void setSlot(int index, Icon icon, String text, int ping) {
                int row = index / tablist.getColumns();
                if (row < tablist.getRows()) {
                    tablist.setSlot(row, index % tablist.getColumns(), icon, text, ping);
                }
            }
        };
    }
}
