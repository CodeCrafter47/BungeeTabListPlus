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

package codecrafter47.bungeetablistplus.data;

import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataKey;
import lombok.Getter;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TrackingDataCache extends DataCache {
    @Getter
    private Set<DataKey<?>> queriedKeys = Collections.newSetFromMap(new ConcurrentHashMap<>());

    @Override
    public <V> V get(DataKey<V> key) {
        V v = super.get(key);
        if (v == null && !queriedKeys.contains(key)) {
            onMissingData(key);
        }
        return v;
    }

    protected <V> void onMissingData(DataKey<V> key) {
        queriedKeys.add(key);
    }
}
