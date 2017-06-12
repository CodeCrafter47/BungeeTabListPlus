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

import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;

public class NullDataHolder implements DataHolder {

    public static final NullDataHolder INSTANCE = new NullDataHolder();

    private NullDataHolder() {

    }

    @Override
    public <V> V get(DataKey<V> key) {
        return null;
    }

    @Override
    public <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {

    }

    @Override
    public <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {

    }
}
