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

import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

public class CompoundDataAccessor<B> implements DataAccessor<B> {
    private final Collection<DataAccessor<B>> accessors;

    public static <B> CompoundDataAccessor<B> of(DataAccessor<B>... accessors) {
        return new CompoundDataAccessor<>(Arrays.asList(accessors));
    }

    public CompoundDataAccessor(Collection<DataAccessor<B>> accessors) {
        this.accessors = accessors;
    }

    @Override
    public <V> Optional<V> getValue(DataKey<V> key, B context) {
        for (DataAccessor<B> accessor : accessors) {
            Optional<V> value = accessor.getValue(key, context);
            if (value.isPresent()) return value;
        }
        return Optional.empty();
    }
}
