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

import com.google.common.collect.Sets;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataKey;
import lombok.Getter;

import java.util.Set;

public class TrackingDataCache extends DataCache {
    @Getter
    private Set<DataKey<?>> activeKeys = Sets.newConcurrentHashSet();

    @Override
    public <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {
        if (!hasListeners(key)) {
            addActiveKey(key);
        }
        super.addDataChangeListener(key, listener);
    }

    protected <T> void addActiveKey(DataKey<T> key) {
        activeKeys.add(key);
    }

    @Override
    public <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {
        super.removeDataChangeListener(key, listener);
        if (!hasListeners(key)) {
            removeActiveKey(key);
        }
    }

    protected <T> void removeActiveKey(DataKey<T> key) {
        activeKeys.remove(key);
    }
}
