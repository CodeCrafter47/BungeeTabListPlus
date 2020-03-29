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

/**
 * Simple BitSet implementation
 */
public class BitSet {
    private final int size;
    private final long[] array;

    public BitSet(int size) {
        Preconditions.checkArgument(size >= 0, "size must not be negative");
        this.size = size;
        this.array = new long[(size + 63) / 64];
    }

    public void set(int index) {
        Preconditions.checkElementIndex(index, size);
        int longIndex = index >> 6;
        long mask = 1L << (index & 0x3F);
        array[longIndex] |= mask;
    }

    public void set(int fromIndex, int toIndex) {
        Preconditions.checkElementIndex(fromIndex, size);
        Preconditions.checkPositionIndex(toIndex, size);
        if (fromIndex >= toIndex)
            return;

        int startWordIndex = fromIndex >> 6;
        int endWordIndex = (toIndex - 1) >> 6;

        long firstWordMask = -1L << fromIndex;
        long lastWordMask = -1L >>> -toIndex;
        if (startWordIndex == endWordIndex) {
            array[startWordIndex] |= (firstWordMask & lastWordMask);
        } else {
            array[startWordIndex] |= firstWordMask;

            for (int i = startWordIndex + 1; i < endWordIndex; i++)
                array[i] = -1L;

            array[endWordIndex] |= lastWordMask;
        }
    }

    public void clear(int index) {
        Preconditions.checkElementIndex(index, size);
        int longIndex = index >> 6;
        long mask = ~(1L << (index & 0x3F));
        array[longIndex] &= mask;
    }

    public void clear() {
        for (int i = 0; i < array.length; i++) {
            array[i] = 0;
        }
    }

    public boolean get(int index) {
        Preconditions.checkElementIndex(index, size);
        int longIndex = index >> 6;
        long mask = 1L << (index & 0x3F);
        return 0 != (array[longIndex] & mask);
    }

    public int cardinality() {
        int sum = 0;
        for (int i = 0; i < array.length; i++)
            sum += Long.bitCount(array[i]);
        return sum;
    }

    public boolean isEmpty() {
        for (int i = 0; i < array.length; i++) {
            long l = array[i];
            if (l != 0)
                return false;
        }
        return true;
    }

    public int nextSetBit(int previous) {
        Preconditions.checkPositionIndex(previous, size);
        int longIndex = previous >> 6;

        if (longIndex >= array.length) {
            return -1;
        }

        long word = array[longIndex] & (-1L << previous);

        while (true) {
            if (word != 0)
                return (longIndex * 64) + Long.numberOfTrailingZeros(word);
            if (++longIndex == array.length)
                return -1;
            word = array[longIndex];
        }
    }

    public int previousSetBit(int fromIndex) {
        Preconditions.checkElementIndex(fromIndex, size);

        int longIndex = fromIndex >> 6;

        long word = array[longIndex] & (-1L >>> -(fromIndex + 1));

        while (true) {
            if (word != 0)
                return (longIndex + 1) * 64 - 1 - Long.numberOfLeadingZeros(word);
            if (longIndex-- == 0)
                return -1;
            word = array[longIndex];
        }
    }

    public void copyAndClear(ConcurrentBitSet source) {
        Preconditions.checkArgument(source.size == this.size);
        for (int i = 0; i < this.array.length; i++) {
            this.array[i] = source.array.getAndSet(i, 0);
        }
    }

    public void orAndClear(ConcurrentBitSet source) {
        Preconditions.checkArgument(source.size == this.size);
        for (int i = 0; i < this.array.length; i++) {
            this.array[i] |= source.array.getAndSet(i, 0);
        }
    }

    public void orXor(BitSet a, BitSet b) {
        Preconditions.checkArgument(a.size == this.size);
        Preconditions.checkArgument(b.size == this.size);
        for (int i = 0; i < this.array.length; i++) {
            this.array[i] |= (a.array[i] ^ b.array[i]);
        }
    }

    public void or(BitSet a) {
        Preconditions.checkArgument(a.size == this.size);
        for (int i = 0; i < this.array.length; i++) {
            this.array[i] |= a.array[i];
        }
    }
}
