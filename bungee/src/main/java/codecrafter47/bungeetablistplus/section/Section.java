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

import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.layout.LayoutSection;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public abstract class Section implements LayoutSection, Function<TabListContext, List<Section>> {
    public abstract Slot getSlotAt(TabListContext context, int pos, int size);

    public abstract void preCalculate(TabListContext context);

    @Override
    public String toString() {
        return "Section(minSize=" + getMinSize() + " ," + "maxSize=" + getMaxSize() + ", startColumn=" + getStartColumn() + ", constantSize=" + isSizeConstant() + ")";
    }

    @Override
    public List<Section> apply(TabListContext context) {
        return Collections.singletonList(this);
    }
}
