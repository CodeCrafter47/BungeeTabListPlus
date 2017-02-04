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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Ordering;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public abstract class ContextAwareOrdering<Context, T> {

    public abstract int compare(Context context, T first, T second);

    public static <Context, T> ContextAwareOrdering<Context, T> from(Comparator<T> comparator) {
        return new ComparatorContextAwareOrdering<>(comparator);
    }

    public static <Context, T> ContextAwareOrdering<Context, T> compound(Iterable<ContextAwareOrdering<Context, T>> comparators) {
        return new CompoundContextAwareOrdering<>(ImmutableList.copyOf(comparators));
    }

    public List<T> immutableSortedCopy(Context context, Collection<T> elements) {
        return new Ordering<T>() {

            @Override
            public int compare(@Nullable T left, @Nullable T right) {
                return ContextAwareOrdering.this.compare(context, left, right);
            }
        }.immutableSortedCopy(elements);
    }

    private static class ComparatorContextAwareOrdering<Context, T> extends ContextAwareOrdering<Context, T> {
        private final Comparator<T> comperator;

        public ComparatorContextAwareOrdering(Comparator<T> comperator) {
            this.comperator = comperator;
        }

        @Override
        public int compare(Context context, T first, T second) {
            return comperator.compare(first, second);
        }
    }

    private static class CompoundContextAwareOrdering<Context, T> extends ContextAwareOrdering<Context, T> {
        private final ImmutableList<ContextAwareOrdering<Context, T>> comperators;

        private CompoundContextAwareOrdering(ImmutableList<ContextAwareOrdering<Context, T>> comperators) {
            this.comperators = comperators;
        }

        @Override
        public int compare(Context context, T first, T second) {
            int result;
            for (int i = 0; i < comperators.size(); i++) {
                if (0 != (result = comperators.get(i).compare(context, first, second))) {
                    return result;
                }
            }
            return 0;
        }
    }
}
