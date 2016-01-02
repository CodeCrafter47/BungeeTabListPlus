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

package codecrafter47.bungeetablistplus.data.bukkit;

import codecrafter47.bungeetablistplus.data.DataAccess;
import codecrafter47.bungeetablistplus.data.DataKey;

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class JoinedDataAccess<B> implements DataAccess<B> {
    private final Collection<DataAccess<B>> accessors;

    public static <B> JoinedDataAccess<B> of(DataAccess<B>... accessors) {
        return new JoinedDataAccess<>(Arrays.asList(accessors));
    }

    public JoinedDataAccess(Collection<DataAccess<B>> accessors) {
        this.accessors = accessors;
    }

    @Override
    public <V> Optional<V> getValue(DataKey<V> key, B context) {
        for (DataAccess<B> accessor : accessors) {
            Optional<V> value = accessor.getValue(key, context);
            if (value.isPresent()) return value;
        }
        return Optional.empty();
    }
}
