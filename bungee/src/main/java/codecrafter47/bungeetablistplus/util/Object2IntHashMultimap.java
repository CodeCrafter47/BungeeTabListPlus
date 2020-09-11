/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.util;

import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLinkedOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

public class Object2IntHashMultimap<T> {

    private final Object2ObjectMap<T, IntCollection> map = new Object2ObjectOpenHashMap<>();

    public boolean contains(T key, int value) {
        IntCollection collection = map.get(key);
        return collection != null && collection.contains(value);
    }

    public IntCollection get(T key) {
        return map.getOrDefault(key, IntSets.EMPTY_SET);
    }

    public void remove(T key, int value) {
        IntCollection collection = map.get(key);
        if (collection != null) {
            collection.rem(value);
            if (collection.isEmpty()) {
                map.remove(key);
            }
        }
    }

    public void put(T key, int value) {
        IntCollection collection = map.computeIfAbsent(key, k -> new IntLinkedOpenHashSet(2, .75f));
        collection.add(value);
    }

    public boolean containsKey(T oldUuid) {
        return map.containsKey(oldUuid);
    }
}
