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

package codecrafter47.bungeetablistplus.layout;

import com.google.common.base.Preconditions;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class Layout<S> {
    final List<S> sections;
    final List<Integer> effectiveSectionSizes;
    final int rows;
    final int columns;
    final int effectiveSection[];
    final int slotIndex[];
    private final int tabSize;

    public Layout(int rows, int columns) {
        this.sections = new ArrayList<>();
        this.rows = rows;
        this.columns = columns;
        this.tabSize = rows * columns;
        this.effectiveSectionSizes = new ArrayList<>();
        this.effectiveSection = new int[tabSize];
        Arrays.fill(effectiveSection, -1);
        this.slotIndex = new int[tabSize];
        Arrays.fill(slotIndex, -1);
    }

    public void placeSection(S section, int row, int column, int size){
        Preconditions.checkElementIndex(row, rows, "row");
        Preconditions.checkElementIndex(column, columns, "column");
        int startIndex = coordinatesToInt(row, column);
        placeSection(section, startIndex, size);
    }

    public void placeSection(S section, int pos, int size) {
        Preconditions.checkElementIndex(pos, tabSize, "pos");
        int endIndex = pos + size - 1;
        Preconditions.checkElementIndex(endIndex, this.tabSize + 1);
        int sectionId = sections.size();
        sections.add(section);
        effectiveSectionSizes.add(size);
        for(int i = pos; i <= endIndex; i++){
            Preconditions.checkState(effectiveSection[i] == -1, "Slot %s already used", i);
            effectiveSection[i] = sectionId;
            slotIndex[i] = i - pos;
        }
    }

    public Optional<SlotData> getSlotData(int row, int column){
        Preconditions.checkElementIndex(row, rows, "row");
        Preconditions.checkElementIndex(column, columns, "column");
        int index = coordinatesToInt(row, column);
        return getSlotData(index);
    }

    public Optional<SlotData> getSlotData(int index) {
        Preconditions.checkElementIndex(index, tabSize, "index");
        int sectionId = effectiveSection[index];
        if(sectionId != -1){
            return Optional.of(new SlotData(sections.get(sectionId), effectiveSectionSizes.get(sectionId), slotIndex[index]));
        }
        return Optional.empty();
    }

    int coordinatesToInt(int row, int column){
        return row * columns + column;
    }

    @Getter
    public final class SlotData{
        private final S section;
        private final int sectionSize;
        private final int slotIndex;

        public SlotData(S section, int sectionSize, int slotIndex) {
            this.section = section;
            this.sectionSize = sectionSize;
            this.slotIndex = slotIndex;
        }
    }
}
