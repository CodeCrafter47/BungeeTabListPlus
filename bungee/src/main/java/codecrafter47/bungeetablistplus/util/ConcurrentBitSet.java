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

import com.google.common.base.Preconditions;

import java.util.concurrent.atomic.AtomicLongArray;
import java.util.function.IntConsumer;

public final class ConcurrentBitSet {
    final int size;
    final AtomicLongArray array;

    public ConcurrentBitSet(int size) {
        Preconditions.checkArgument(size >= 0, "size must not be negative");
        this.size = size;
        this.array = new AtomicLongArray((size + 63) / 64);
    }

    public void set(int index) {
        Preconditions.checkElementIndex(index, size);
        int longIndex = index >> 6;
        long mask = 1L << (index & 0x3F);
        long expect;
        do {
            expect = array.get(longIndex);
        } while (!array.compareAndSet(longIndex, expect, expect | mask));
    }

    public void clear(int index) {
        Preconditions.checkElementIndex(index, size);
        int longIndex = index >> 6;
        long mask = ~(1L << (index & 0x3F));
        long expect;
        do {
            expect = array.get(longIndex);
        } while (!array.compareAndSet(longIndex, expect, expect & mask));
    }

    public void iterateAndClear(IntConsumer consumer) {
        for (int longIndex = 0; longIndex < array.length(); longIndex++) {
            long l = array.getAndSet(longIndex, 0);
            while (l != 0) {
                int i = Long.numberOfTrailingZeros(l);
                consumer.accept(longIndex << 6 | i);
                l = l & ~(1L << i);
            }
        }
    }
}
