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
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import java.util.ArrayList;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Florian Stober
 */
public class StaticSection extends Section {

    List<Slot> text;
    int vAlign;

    public StaticSection(int vAlign, List<Slot> text) {
        this.vAlign = vAlign;
        this.text = text;
    }

    public StaticSection(int vAlign) {
        this.vAlign = vAlign;
        this.text = new ArrayList<>();
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return text.size();
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        return text.size();
    }

    public void add(Slot slot) {
        text.add(slot);
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        for (Slot s : text) {
            tabList.setSlot(pos++, new Slot(s));
        }
        return pos;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
    }

    @Override
    public int getStartCollumn() {
        return vAlign;
    }
}
