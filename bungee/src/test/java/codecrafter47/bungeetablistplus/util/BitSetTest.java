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

package codecrafter47.bungeetablistplus.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BitSetTest {

    @Test
    public void test() {
        BitSet bitSet = new BitSet(80);

        bitSet.set(15);

        for (int i = 0; i < 80; i++) {
            if (i == 15) {
                assertTrue("index: " + i, bitSet.get(i));
            } else {
                assertFalse("index: " + i, bitSet.get(i));
            }
        }

        bitSet.clear();

        bitSet.set(19);

        bitSet.set(17);

        bitSet.clear(17);

        for (int i = 0; i < 80; i++) {
            if (i == 19) {
                assertTrue("index: " + i, bitSet.get(i));
            } else {
                assertFalse("index: " + i, bitSet.get(i));
            }
        }

        bitSet.clear();

        for (int i = 0; i < 80; i++) {
            assertFalse("index: " + i, bitSet.get(i));
        }
    }

    @Test
    public void testNextSetBit() {
        BitSet bitSet = new BitSet(80);

        bitSet.set(15);

        bitSet.set(19);

        bitSet.set(17);

        assertEquals(15, bitSet.nextSetBit(0));
        assertEquals(17, bitSet.nextSetBit(15 + 1));
        assertEquals(19, bitSet.nextSetBit(17 + 1));
        assertEquals(-1, bitSet.nextSetBit(19 + 1));

        bitSet.clear();

        bitSet.set(16);

        bitSet.set(79);

        assertEquals(16, bitSet.nextSetBit(0));
        assertEquals(79, bitSet.nextSetBit(16 + 1));
        assertEquals(-1, bitSet.nextSetBit(79 + 1));

        bitSet.clear();

        bitSet.set(78);

        bitSet.set(79);

        assertEquals(78, bitSet.nextSetBit(0));
        assertEquals(79, bitSet.nextSetBit(78 + 1));
        assertEquals(-1, bitSet.nextSetBit(79 + 1));

    }

}