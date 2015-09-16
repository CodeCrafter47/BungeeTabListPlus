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
            return fastLayout(topSections, bottomSections, context);
        } catch (Throwable th) {
            throw new Exception("Failed to calculate Layout for topSections=" + topSections.toString() + ", botSections=" + bottomSections.toString(), th);
        }
    }

    Layout<Section> slowLayout(List<Section> topSections, List<Section> bottomSections, TabListContext context) {
        int topSectionSize[] = new int[topSections.size()];
        int bottomSectionSize[] = new int[bottomSections.size()];

        for (int i = 0; i < bottomSections.size(); i++) {
            Section section = bottomSections.get(i);
            bottomSectionSize[i] = section.getMinSize();
        }

        int maxUsedSlots = -1;
        int bestTopSectionSizes[] = null;
        int bestBottomSectionSizes[] = null;

        boolean repeatOuter;
        do {
            for (int i = 0; i < topSections.size(); i++) {
                Section section = topSections.get(i);
                topSectionSize[i] = section.getMinSize();
            }
            int requiredSizeBottom = 0;
            for (int i = bottomSections.size() - 1; i >= 0; i--) {
                Section section = bottomSections.get(i);
                requiredSizeBottom += section.getEffectiveSize(bottomSectionSize[i]);
                OptionalInt startColumn = section.getStartColumn();
                if (startColumn.isPresent()) {
                    requiredSizeBottom += calculateSpace(startColumn.getAsInt(), context.getColumns() - (requiredSizeBottom % context.getColumns()), context);
                }
            }

            boolean repeatInner;
            do {
                int requiredSizeIgnoringAlign = 0;
                for (int i = 0; i < topSections.size(); i++) {
                    Section section = topSections.get(i);
                    requiredSizeIgnoringAlign += section.getEffectiveSize(topSectionSize[i]);
                }
                for (int i = 0; i < bottomSections.size(); i++) {
                    Section section = bottomSections.get(i);
                    requiredSizeIgnoringAlign += section.getEffectiveSize(bottomSectionSize[i]);
                }
                if (requiredSizeIgnoringAlign <= context.getTabSize()) {
                    int requiredSizeTop = 0;
                    for (int i = 0; i < topSections.size(); i++) {
                        Section section = topSections.get(i);
                        OptionalInt startColumn = section.getStartColumn();
                        if (startColumn.isPresent()) {
                            requiredSizeTop += calculateSpace(requiredSizeTop, startColumn.getAsInt(), context);
                        }
                        requiredSizeTop += section.getEffectiveSize(topSectionSize[i]);
                    }
                    int usedSlots = requiredSizeBottom + requiredSizeTop;
                    if (usedSlots <= context.getTabSize()) {
                        if (requiredSizeIgnoringAlign > maxUsedSlots) {
                            maxUsedSlots = requiredSizeIgnoringAlign;
                            bestTopSectionSizes = new int[topSectionSize.length];
                            for (int i = 0; i < topSections.size(); i++) {
                                Section section = topSections.get(i);
                                bestTopSectionSizes[i] = section.getEffectiveSize(topSectionSize[i]);
                            }
                            bestBottomSectionSizes = new int[bottomSectionSize.length];
                            for (int i = 0; i < bottomSections.size(); i++) {
                                Section section = bottomSections.get(i);
                                bestBottomSectionSizes[i] = section.getEffectiveSize(bottomSectionSize[i]);
                            }
                        }
                    }
                }

                repeatInner = false;
                int pos = 0;
                for (int i = 0; i < topSections.size(); i++) {
                    Section section = topSections.get(i);
                    if (section.getStartColumn().isPresent()) {
                        pos = section.getStartColumn().getAsInt();
                    }
                    if (topSectionSize[i] < section.getMaxSize()) {
                        int newSize = topSectionSize[i];
                        do {
                            newSize++;
                            if (i + 1 < bottomSections.size() && bottomSections.get(i + 1).getStartColumn().isPresent()) {
                                newSize += calculateSpace(pos + newSize, bottomSections.get(i + 1).getStartColumn().getAsInt(), context);
                            }
                        } while (section.getEffectiveSize(topSectionSize[i]) == section.getEffectiveSize(newSize));
                        topSectionSize[i] = newSize;
                        repeatInner = true;
                        break;
                    } else {
                        topSectionSize[i] = section.getMinSize();
                        pos += topSectionSize[i];
                    }
                }
            } while (repeatInner);

            repeatOuter = false;
            OptionalInt pos = OptionalInt.empty();
            for (int i = 0; i < bottomSections.size(); i++) {
                Section section = bottomSections.get(i);
                if (section.getStartColumn().isPresent()) {
                    pos = section.getStartColumn();
                }
                if (bottomSectionSize[i] < section.getMaxSize()) {
                    int newSize = bottomSectionSize[i];
                    do {
                        newSize++;
                        if (pos.isPresent() && i + 1 < bottomSections.size() && bottomSections.get(i + 1).getStartColumn().isPresent()) {
                            newSize += calculateSpace(pos.getAsInt() + newSize, bottomSections.get(i + 1).getStartColumn().getAsInt(), context);
                        }
                    } while (section.getEffectiveSize(bottomSectionSize[i]) == section.getEffectiveSize(newSize));
                    bottomSectionSize[i] = newSize;
                    repeatOuter = true;
                    break;
                } else {
                    bottomSectionSize[i] = section.getMinSize();
                    if (pos.isPresent()) {
                        pos = OptionalInt.of(bottomSectionSize[i] + pos.getAsInt());
                    }
                }
            }
        } while (repeatOuter);

        if (bestTopSectionSizes != null) {
            Layout<Section> layout = new Layout<>(context.getRows(), context.getColumns());
            int pos = 0;
            for (int i = 0; i < topSections.size(); i++) {
                Section section = topSections.get(i);
                OptionalInt startColumn = section.getStartColumn();
                if (startColumn.isPresent()) {
                    pos += calculateSpace(pos, startColumn.getAsInt(), context);
                }
                layout.placeSection(section, pos, bestTopSectionSizes[i]);
                pos += bestTopSectionSizes[i];
            }

            pos = context.getTabSize();
            for (int i = bottomSections.size() - 1; i >= 0; i--) {
                Section section = bottomSections.get(i);
                pos -= bestBottomSectionSizes[i];
                OptionalInt startColumn = section.getStartColumn();
                if (startColumn.isPresent()) {
                    pos -= calculateSpace(startColumn.getAsInt(), pos, context);
                }
                layout.placeSection(section, pos, bestBottomSectionSizes[i]);
            }

            return layout;
        }

        throw new IllegalStateException("Too much content for tablist");
    }

    Layout<Section> fastLayout(List<Section> topSections, List<Section> bottomSections, TabListContext context) {
        int topSectionSize[] = new int[topSections.size()];
        for (int i = 0; i < topSections.size(); i++) {
            Section section = topSections.get(i);
            topSectionSize[i] = section.getMinSize();
        }

        int bottomSectionSize[] = new int[bottomSections.size()];
        for (int i = 0; i < bottomSections.size(); i++) {
            Section section = bottomSections.get(i);
            bottomSectionSize[i] = section.getMinSize();
        }

        if (calculateSizeTop(topSections, context, Section::getMinSize) + calculateSizeBottom(bottomSections, context, Section::getMinSize) > context.getTabSize()) {
            throw new IllegalStateException("Too much content for tablist");
        }

        boolean repeat;
        do {
            repeat = false;

            for (int topOrBottom = 0; topOrBottom < 2; topOrBottom++) {
                List<Section> sectionList;
                int sectionSizes[];
                if (topOrBottom == 0) {
                    // top
                    sectionList = topSections;
                    sectionSizes = topSectionSize;
                } else {
                    // bottom
                    sectionList = bottomSections;
                    sectionSizes = bottomSectionSize;
                }

                for (int i = 0; i < sectionList.size(); i++) {
                    Section section = sectionList.get(i);
                    int oldSectionSize = sectionSizes[i];
                    if (oldSectionSize >= section.getMaxSize()) {
                        continue;
                    }
                    int newSectionSize = oldSectionSize + 1;
                    while (section.getEffectiveSize(oldSectionSize) == section.getEffectiveSize(newSectionSize)) {
                        newSectionSize++;
                    }
                    sectionSizes[i] = newSectionSize;

                    int requiredSizeBottom = 0;
                    for (int j = bottomSections.size() - 1; j >= 0; j--) {
                        Section section2 = bottomSections.get(j);
                        requiredSizeBottom += section2.getEffectiveSize(bottomSectionSize[j]);
                        OptionalInt startColumn = section2.getStartColumn();
                        if (startColumn.isPresent()) {
                            requiredSizeBottom += calculateSpace(startColumn.getAsInt(), context.getColumns() - (requiredSizeBottom % context.getColumns()), context);
                        }
                    }

                    int requiredSizeTop = 0;
                    for (int j = 0; j < topSections.size(); j++) {
                        Section section2 = topSections.get(j);
                        OptionalInt startColumn = section2.getStartColumn();
                        if (startColumn.isPresent()) {
                            requiredSizeTop += calculateSpace(requiredSizeTop, startColumn.getAsInt(), context);
                        }
                        requiredSizeTop += section2.getEffectiveSize(topSectionSize[j]);
                    }

                    int usedSlots = requiredSizeBottom + requiredSizeTop;

                    if (usedSlots <= context.getTabSize()) {
                        repeat = true;
                    } else {
                        sectionSizes[i] = oldSectionSize;
                    }
                }
            }
        } while (repeat);

        Layout<Section> layout = new Layout<>(context.getRows(), context.getColumns());
        int pos = 0;
        for (int i = 0; i < topSections.size(); i++) {
            Section section = topSections.get(i);
            OptionalInt startColumn = section.getStartColumn();
            if (startColumn.isPresent()) {
                pos += calculateSpace(pos, startColumn.getAsInt(), context);
            }
            layout.placeSection(section, pos, topSectionSize[i]);
            pos += topSectionSize[i];
        }

        pos = context.getTabSize();
        for (int i = bottomSections.size() - 1; i >= 0; i--) {
            Section section = bottomSections.get(i);
            pos -= bottomSectionSize[i];
            OptionalInt startColumn = section.getStartColumn();
            if (startColumn.isPresent()) {
                pos -= calculateSpace(startColumn.getAsInt(), pos, context);
            }
            layout.placeSection(section, pos, bottomSectionSize[i]);
        }

        return layout;
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

    int calculateSpace(int fromColumn, int toColumn, TabListContext context) {
        return (((toColumn - fromColumn) % context.getColumns()) + context.getColumns()) % context.getColumns();
    }
}
