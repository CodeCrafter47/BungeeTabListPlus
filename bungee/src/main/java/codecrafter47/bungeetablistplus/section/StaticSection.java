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
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;

/**
 * @author Florian Stober
 */
public class StaticSection extends Section {

    final List<SlotTemplate> text;
    private final OptionalInt vAlign;

    public StaticSection(int vAlign, List<SlotTemplate> text) {
        this.vAlign = vAlign == -1 ? OptionalInt.empty() : OptionalInt.of(vAlign);
        this.text = text;
    }

    public StaticSection(int vAlign) {
        this(vAlign, new ArrayList<>());
    }

    @Override
    public int getMinSize() {
        return text.size();
    }

    @Override
    public int getMaxSize() {
        return text.size();
    }

    @Override
    public boolean isSizeConstant() {
        return true;
    }

    @Override
    public int getEffectiveSize(int proposedSize) {
        return text.size();
    }

    public void add(SlotTemplate slot) {
        text.add(slot);
    }

    @Override
    public Slot getSlotAt(TabListContext context, int pos, int size) {
        return text.get(pos).buildSlot(context);
    }

    @Override
    public void precalculate(TabListContext context) {
    }

    @Override
    public OptionalInt getStartColumn() {
        return vAlign;
    }
}
