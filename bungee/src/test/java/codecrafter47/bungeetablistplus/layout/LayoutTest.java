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

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class LayoutTest {

    @Test
    public void testPlaceSection() throws Exception {
        Layout<Object> layout = new Layout<>(20, 3);
        layout.placeSection(new Object(), 0, 0, 4);
        layout.placeSection(new Object(), 2, 0, 8);
        layout.placeSection(new Object(), 8, 0, 16);
        layout.placeSection(new Object(), 19, 0, 3);
    }

    @Test
    public void testGetSlotData() throws Exception {
        Layout<Object> layout = new Layout<>(20, 3);
        Object section = new Object();
        layout.placeSection(section, 2, 1, 8);
        Optional<Layout<Object>.SlotData> optionalSlotData = layout.getSlotData(3, 0);
        Assert.assertTrue(optionalSlotData.isPresent());
        Layout<Object>.SlotData slotData = optionalSlotData.get();
        Assert.assertEquals(section, slotData.getSection());
        Assert.assertEquals(8, slotData.getSectionSize());
        Assert.assertEquals(2, slotData.getSlotIndex());
    }

    @Test
    public void testGetSlotData2() throws Exception {
        Layout<Object> layout = new Layout<>(20, 3);
        Object section = new Object();
        layout.placeSection(section, 2, 1, 8);
        Optional<Layout<Object>.SlotData> optionalSlotData = layout.getSlotData(3, 1);
        Assert.assertTrue(optionalSlotData.isPresent());
        Layout<Object>.SlotData slotData = optionalSlotData.get();
        Assert.assertEquals(section, slotData.getSection());
        Assert.assertEquals(8, slotData.getSectionSize());
        Assert.assertEquals(3, slotData.getSlotIndex());
    }

    @Test
    public void testGetSlotData3() throws Exception {
        Layout<Object> layout = new Layout<>(20, 3);
        Object section = new Object();
        layout.placeSection(section, 2, 1, 8);
        Optional<Layout<Object>.SlotData> optionalSlotData = layout.getSlotData(2, 0);
        Assert.assertFalse(optionalSlotData.isPresent());
    }

    @Test
    public void testGetSlotData4() throws Exception {
        Layout<Object> layout = new Layout<>(20, 3);
        Object section = new Object();
        layout.placeSection(section, 2, 1, 8);
        Optional<Layout<Object>.SlotData> optionalSlotData = layout.getSlotData(10, 2);
        Assert.assertFalse(optionalSlotData.isPresent());
    }

    @Test
    public void testCoordinatesToInt() throws Exception {
        Layout<Object> layout = new Layout<>(20, 3);
        Assert.assertEquals(2, layout.coordinatesToInt(0, 2));
        Assert.assertEquals(3, layout.coordinatesToInt(1, 0));
        Assert.assertEquals(31, layout.coordinatesToInt(10, 1));
    }
}