/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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

/**
 *
 * @author Florian Stober
 */
public class TabList {

    private int rows;
    private int collums;
    private int usedSlots;
    private Slot[] slots;

    public TabList(int rows, int collums) {
        this.rows = rows;
        this.collums = collums;
        this.usedSlots = 0;
        this.slots = new Slot[rows * collums];
    }

    public int getRows() {
        return this.rows;
    }

    public int getCollums() {
        return this.collums;
    }

    public int getUsedSlots() {
        return this.usedSlots;
    }

    public Slot getSlot(int n) {
        return this.slots[n];
    }

    public Slot getSlot(int row, int collum) {
        return getSlot(row * collums + collum);
    }

    public void setSlot(int n, Slot s) {
        if (n >= slots.length) {
            return;
        }
        this.slots[n] = s;
        if (n + 1 > usedSlots) {
            usedSlots = n + 1;
        }
    }

    public void setSlot(int row, int collum, Slot s) {
        setSlot(row * collums + collum, s);
    }
}
