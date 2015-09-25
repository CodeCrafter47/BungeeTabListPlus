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

import codecrafter47.bungeetablistplus.tablist.GenericTabListContext;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

public class TablistLayoutManagerTest {

    @Test
    public void testCalculateSizeBottom1() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> sections = new ArrayList<>();
        sections.add(new MockLayoutSection(1, 4, OptionalInt.of(0)));

        Assert.assertEquals(3, layoutManager.calculateSizeBottom(sections, tabListContext, LayoutSection::getMinSize));
        Assert.assertEquals(6, layoutManager.calculateSizeBottom(sections, tabListContext, LayoutSection::getMaxSize));
    }

    @Test
    public void testCalculateSizeBottom2() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> sections = new ArrayList<>();
        sections.add(new MockLayoutSection(2, 7, OptionalInt.empty()));

        Assert.assertEquals(2, layoutManager.calculateSizeBottom(sections, tabListContext, LayoutSection::getMinSize));
        Assert.assertEquals(7, layoutManager.calculateSizeBottom(sections, tabListContext, LayoutSection::getMaxSize));
    }

    @Test
    public void testCalculateSizeBottom3() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> sections = new ArrayList<>();
        sections.add(new MockLayoutSection(1, 1, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 1, OptionalInt.of(1)));
        sections.add(new MockLayoutSection(1, 1, OptionalInt.of(2)));

        Assert.assertEquals(3, layoutManager.calculateSizeBottom(sections, tabListContext, LayoutSection::getMinSize));
        Assert.assertEquals(3, layoutManager.calculateSizeBottom(sections, tabListContext, LayoutSection::getMaxSize));
    }

    @Test
    public void testCalculateSizeTop() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> sections = new ArrayList<>();
        sections.add(new MockLayoutSection(1, 4, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 4, OptionalInt.of(0)));

        Assert.assertEquals(4, layoutManager.calculateSizeTop(sections, tabListContext, LayoutSection::getMinSize));
        Assert.assertEquals(10, layoutManager.calculateSizeTop(sections, tabListContext, LayoutSection::getMaxSize));
    }

    @Test
    public void testCalculateSpace1() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        Assert.assertEquals(0, layoutManager.calculateSpace(0, 0, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(1, 1, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(2, 2, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(0, 3, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(3, 0, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(2, 0, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(0, 1, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(1, 2, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(1, 5, tabListContext));
        Assert.assertEquals(2, layoutManager.calculateSpace(0, 5, tabListContext));
        Assert.assertEquals(2, layoutManager.calculateSpace(1, 6, tabListContext));
        Assert.assertEquals(2, layoutManager.calculateSpace(2, 7, tabListContext));
        Assert.assertEquals(2, layoutManager.calculateSpace(2, 1, tabListContext));
        Assert.assertEquals(2, layoutManager.calculateSpace(5, 1, tabListContext));
    }

    @Test
    public void testCalculateSpace2() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 5);

        Assert.assertEquals(0, layoutManager.calculateSpace(0, 0, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(0, 5, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(5, 0, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(4, 0, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(0, 1, tabListContext));
        Assert.assertEquals(1, layoutManager.calculateSpace(1, 2, tabListContext));
        Assert.assertEquals(3, layoutManager.calculateSpace(0, 3, tabListContext));
        Assert.assertEquals(4, layoutManager.calculateSpace(0, 4, tabListContext));
    }

    @Test
    public void testCalculateSpace3() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 1);

        Assert.assertEquals(0, layoutManager.calculateSpace(0, 0, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(0, 5, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(5, 0, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(4, 0, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(0, 1, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(1, 2, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(0, 3, tabListContext));
        Assert.assertEquals(0, layoutManager.calculateSpace(0, 4, tabListContext));
    }

    @Test
    public void testCalculateLayout1() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> topSections = new ArrayList<>();
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));

        List<LayoutSection> bottomSections = new ArrayList<>();
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));

        Layout<LayoutSection> layout = layoutManager.calculateLayout(topSections, bottomSections, tabListContext);

        for (int row = 0; row < tabListContext.getRows(); row++) {
            for (int column = 0; column < tabListContext.getColumns(); column++) {
                Optional<Layout<LayoutSection>.SlotData> slotData = layout.getSlotData(row, column);
                Assert.assertTrue("Slot row=" + row + " column=" + column + " not filled", slotData.isPresent());
                Layout<LayoutSection>.SlotData data = slotData.get();
                Assert.assertEquals("Section length wrong", 3, data.getSectionSize());
            }
        }
    }

    @Test
    public void testCalculateLayout2() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> topSections = new ArrayList<>();
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 4, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(3, 4, OptionalInt.of(0)));

        List<LayoutSection> bottomSections = new ArrayList<>();
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(3, 3, OptionalInt.of(0)));

        Layout<LayoutSection> layout = layoutManager.calculateLayout(topSections, bottomSections, tabListContext);

        for (int row = 0; row < tabListContext.getRows(); row++) {
            for (int column = 0; column < tabListContext.getColumns(); column++) {
                Optional<Layout<LayoutSection>.SlotData> slotData = layout.getSlotData(row, column);
                Assert.assertTrue("Slot row=" + row + " column=" + column + " not filled", slotData.isPresent());
                Layout<LayoutSection>.SlotData data = slotData.get();
                Assert.assertEquals("Section length wrong", 3, data.getSectionSize());
            }
        }
    }

    @Test
    public void testCalculateLayout3() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> topSections = new ArrayList<>();
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        topSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));

        List<LayoutSection> bottomSections = new ArrayList<>();
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        bottomSections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));

        Layout<LayoutSection> layout = layoutManager.calculateLayout(topSections, bottomSections, tabListContext);

        for (int row = 0; row < tabListContext.getRows(); row++) {
            for (int column = 0; column < tabListContext.getColumns(); column++) {
                Optional<Layout<LayoutSection>.SlotData> slotData = layout.getSlotData(row, column);
                Assert.assertTrue("Slot row=" + row + " column=" + column + " not filled", slotData.isPresent());
                Layout<LayoutSection>.SlotData data = slotData.get();
                Assert.assertEquals("Section length wrong", 3, data.getSectionSize());
            }
        }
    }

    @Test
    public void testCalculateLayout4() throws Exception {
        TablistLayoutManager<LayoutSection> layoutManager = new TablistLayoutManager<>();

        TabListContext tabListContext = new MockTabListContext(20, 3);

        List<LayoutSection> sections = new ArrayList<>();
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));
        sections.add(new MockLayoutSection(1, 7, OptionalInt.of(0)));

        Layout<LayoutSection> layout = layoutManager.calculateLayout(sections, Collections.emptyList(), tabListContext);

        for (int row = 0; row < tabListContext.getRows(); row++) {
            for (int column = 0; column < tabListContext.getColumns(); column++) {
                Optional<Layout<LayoutSection>.SlotData> slotData = layout.getSlotData(row, column);
                Assert.assertTrue("Slot row=" + row + " column=" + column + " not filled", slotData.isPresent());
                Layout<LayoutSection>.SlotData data = slotData.get();
                Assert.assertEquals("Section length wrong", 3, data.getSectionSize());
            }
        }

        layout = layoutManager.calculateLayout(Collections.emptyList(), sections, tabListContext);

        for (int row = 0; row < tabListContext.getRows(); row++) {
            for (int column = 0; column < tabListContext.getColumns(); column++) {
                Optional<Layout<LayoutSection>.SlotData> slotData = layout.getSlotData(row, column);
                Assert.assertTrue("Slot row=" + row + " column=" + column + " not filled", slotData.isPresent());
                Layout<LayoutSection>.SlotData data = slotData.get();
                Assert.assertEquals("Section length wrong", 3, data.getSectionSize());
            }
        }
    }

    class MockLayoutSection implements LayoutSection {
        private final int minSize;
        private final int maxSize;
        private final OptionalInt startColumn;

        MockLayoutSection(int minSize, int maxSize, OptionalInt startColumn) {
            this.minSize = minSize;
            this.maxSize = maxSize;
            this.startColumn = startColumn;
        }

        @Override
        public int getMinSize() {
            return minSize;
        }

        @Override
        public int getMaxSize() {
            return maxSize;
        }

        @Override
        public boolean isSizeConstant() {
            return minSize == maxSize;
        }

        @Override
        public int getEffectiveSize(int proposedSize) {
            return proposedSize;
        }

        @Override
        public OptionalInt getStartColumn() {
            return startColumn;
        }
    }

    class MockTabListContext extends GenericTabListContext {
        MockTabListContext(int rows, int columns) {
            super(rows, columns, null, null);
        }
    }
}