/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package codecrafter47.bungeetablistplus.tablist;

import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.skin.Skin;

public class FlippedTabList implements ITabList {

    TabList tabList;

    public FlippedTabList(TabList tabList) {
        this.tabList = tabList;
    }

    @Override
    public String getHeader() {
        return tabList.getHeader();
    }

    @Override
    public void setHeader(String header) {
        tabList.setHeader(header);
    }

    @Override
    public String getFooter() {
        return tabList.getFooter();
    }

    @Override
    public void setFooter(String footer) {
        tabList.setFooter(footer);
    }

    @Override
    public int getRows() {
        return tabList.getColumns();
    }

    @Override
    public int getColumns() {
        return tabList.getRows();
    }

    @Override
    public int getUsedSlots() {
        return tabList.usedSlotsFlipped;
    }

    @Override
    public Slot getSlot(int n) {
        int column = n % getColumns();
        int row = (n - column) / getColumns();
        return getSlot(row, column);
    }

    @Override
    public Slot getSlot(int row, int column) {
        return tabList.getSlot(column, row);
    }

    @Override
    public void setSlot(int n, Slot s) {
        int column = n % getColumns();
        int row = (n - column) / getColumns();
        setSlot(row, column, s);
    }

    @Override
    public void setSlot(int row, int column, Slot s) {
        tabList.setSlot(column, row, s);
    }

    @Override
    public int getDefaultPing() {
        return tabList.getDefaultPing();
    }

    @Override
    public void setDefaultPing(int defaultPing) {
        tabList.setDefaultPing(defaultPing);
    }

    @Override
    public int getSize() {
        return tabList.getSize();
    }

    @Override
    public Skin getDefaultSkin() {
        return tabList.getDefaultSkin();
    }

    @Override
    public void setDefaultSkin(Skin defaultSkin) {
        tabList.setDefaultSkin(defaultSkin);
    }

    @Override
    public ITabList flip() {
        return tabList;
    }
}
