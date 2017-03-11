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

import static org.junit.Assert.fail;

public class ConcurrentBitSetTest {

    @Test
    public void test() {
        ConcurrentBitSet bitSet = new ConcurrentBitSet(80);

        bitSet.set(15);

        bitSet.iterateAndClear(index -> {
            if (index != 15) {
                fail();
            }
        });

        bitSet.set(19);

        bitSet.set(17);

        bitSet.clear(17);

        bitSet.iterateAndClear(index -> {
            if (index != 19) {
                fail();
            }
        });

        bitSet.iterateAndClear(index -> {
            fail();
        });
    }

}