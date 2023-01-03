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

package codecrafter47.bungeetablistplus.bridge;

import de.codecrafter47.data.api.DataKey;

import javax.annotation.Nullable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NetDataKeyIdMap {

    private final ConcurrentHashMap<DataKey<?>, Integer> map = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, DataKey<?>> mapReversed = new ConcurrentHashMap<>();

    private final AtomicInteger nextId = new AtomicInteger(0);

    public int getNetId(DataKey<?> key) {
        return map.computeIfAbsent(key, this::computeNetId);
    }

    private int computeNetId(DataKey<?> key) {
        int id = nextId.getAndIncrement();
        mapReversed.put(id, key);
        return id;
    }

    @Nullable
    public DataKey<?> getKey(int id) {
        return mapReversed.get(id);
    }
}
