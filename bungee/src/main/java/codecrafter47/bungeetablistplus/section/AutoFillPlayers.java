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

import java.util.List;
import java.util.OptionalInt;

public class AutoFillPlayers extends Section {

    public final SlotTemplate prefix;
    public final SlotTemplate suffix;
    public final int startColumn;
    public final int maxPlayers;
    public final List<String> sortRules;
    public final List<SlotTemplate> playerLines;
    public final List<SlotTemplate> morePlayerLines;

    public AutoFillPlayers(int startColumn, SlotTemplate prefix, SlotTemplate suffix, List<String> sortRules, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.startColumn = startColumn;
        this.sortRules = sortRules;
        this.maxPlayers = maxPlayers;
        this.playerLines = playerLines;
        this.morePlayerLines = morePlayerLines;
    }

    @Override
    public Slot getSlotAt(TabListContext context, int pos, int size) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public void precalculate(TabListContext context) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getMinSize() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getMaxSize() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public boolean isSizeConstant() {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public int getEffectiveSize(int proposedSize) {
        throw new UnsupportedOperationException("Not supported");
    }

    @Override
    public OptionalInt getStartColumn() {
        throw new UnsupportedOperationException("Not supported");
    }
}
