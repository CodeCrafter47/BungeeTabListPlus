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

package codecrafter47.bungeetablistplus.layout;

import lombok.SneakyThrows;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.ToIntFunction;

public class TablistLayoutManager<Section extends LayoutSection> {

    @SneakyThrows
    public Layout<Section> calculateLayout(List<Section> topSections, List<Section> bottomSections, TabListContext context) {
        try {
            int topMax = calculateSizeTop(topSections, context, LayoutSection::getMaxSize);
            int topMin = calculateSizeTop(topSections, context, LayoutSection::getMinSize);

            int bottomMax = calculateSizeBottom(bottomSections, context, LayoutSection::getMaxSize);
            int bottomMin = calculateSizeBottom(bottomSections, context, LayoutSection::getMinSize);

            // determine how much space top and bottom sections get
            int bottomSize = bottomMin;
            {
                int left = context.getTabSize() - topMin - bottomMin;

                if (left > 0) {
                    int top_diff = topMax - topMin;
                    int bot_diff = bottomMax - bottomMin;
                    int avrg = left / 2;

                    if (top_diff + bot_diff < left) {
                        bottomSize = bottomMax;
                    } else if (top_diff < avrg) {
                        bottomSize += left - top_diff;
                    } else if (bot_diff < avrg) {
                        bottomSize += bot_diff;
                    } else {
                        bottomSize += avrg;
                    }
                } else if (left < 0) {
                    // TODO
                    throw new IllegalStateException("Too much content for tab list");
                }
            }

            Layout<Section> layout = new Layout<>(context.getRows(), context.getColumns());

            // calc bottom sections
            int pos = context.getTabSize();
            int availableAdditionalSpace = bottomSize - bottomMin;
            for (int i = bottomSections.size() - 1; i >= 0; i--) {
                Section section = bottomSections.get(i);
                int additionalSpace = 0;
                if (!section.isSizeConstant() && availableAdditionalSpace > 0) {
                    additionalSpace = availableAdditionalSpace * (section.getMaxSize() - section.getMinSize()) / (bottomMax - bottomMin);
                }
                int size = section.getMinSize() + additionalSpace;
                OptionalInt startColumn = section.getStartColumn();
                if (startColumn.isPresent()) {
                    size += calculateSpace(startColumn.getAsInt(), pos - size, context);
                }
                availableAdditionalSpace -= (size - section.getMinSize());
                pos -= size;
                size = section.getEffectiveSize(size);
                layout.placeSection(section, pos, size);
            }

            // fix topSize
            int topSize = pos;
            int bottomStart = pos;
            pos = 0;
            availableAdditionalSpace = topSize - topMin;
            // calc top sections
            for (int i = 0; i < topSections.size(); i++) {
                Section section = topSections.get(i);
                int additionalSpace = 0;
                if (!section.isSizeConstant() && availableAdditionalSpace > 0 && topMax - topMin > 0) {
                    additionalSpace = availableAdditionalSpace * (section.getMaxSize() - section.getMinSize()) / (topMax - topMin);
                }
                int size = section.getMinSize() + additionalSpace;
                if (i + 1 < topSections.size()) {
                    OptionalInt nextStartColumn = topSections.get(i + 1).getStartColumn();
                    if (nextStartColumn.isPresent()) {
                        size += calculateSpace(pos + size, nextStartColumn.getAsInt(), context);
                    }
                } else {
                    size += calculateSpace(pos + size, bottomStart, context);
                }
                size = section.getEffectiveSize(size);
                int space = 0;
                OptionalInt startColumn = section.getStartColumn();
                if (startColumn.isPresent()) {
                    space = calculateSpace(pos, startColumn.getAsInt(), context);
                }
                pos += space;
                availableAdditionalSpace -= space;
                layout.placeSection(section, pos, size);
                pos += size;
                availableAdditionalSpace -= size - section.getMinSize();
            }
            return layout;
        } catch (Throwable th){
            throw new Exception("Failed to calculate Layout for topSections=" + topSections.toString() + ", botSections=" + bottomSections.toString(), th);
        }
    }

    int calculateSizeBottom(List<Section> bottomSections, TabListContext context, ToIntFunction<Section> getSize) {
        int bottomSize = 0;
        for (int i = bottomSections.size() - 1; i >= 0; i--) {
            Section section = bottomSections.get(i);
            bottomSize += getSize.applyAsInt(section);
            OptionalInt startColumn = section.getStartColumn();
            if (startColumn.isPresent()) {
                bottomSize += calculateSpace(startColumn.getAsInt(), context.getColumns() - (bottomSize % context.getColumns()), context);
            }
        }
        return bottomSize;
    }

    int calculateSizeTop(List<Section> topSections, TabListContext context, ToIntFunction<Section> getSize) {
        int topSize = 0;
        for (Section section : topSections) {
            OptionalInt startColumn = section.getStartColumn();
            if (startColumn.isPresent()) {
                topSize += calculateSpace(topSize, startColumn.getAsInt(), context);
            }
            topSize += getSize.applyAsInt(section);
        }
        return topSize;
    }

    int calculateSpace(int fromColumn, int toColumn, TabListContext context){
        return (((toColumn - fromColumn) % context.getColumns()) + context.getColumns()) % context.getColumns();
    }
}
