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

import com.google.common.cache.Cache;
import com.google.common.cache.CacheStats;
import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

// cleanUp is not called automatically
public class ExpireAfterAccessCache<K, V> implements Cache<K, V> {
    private final long expireAfter;
    private final Map<K, CachedValue<V>> map = new ConcurrentHashMap<>();

    public ExpireAfterAccessCache(long expireAfter) {
        this.expireAfter = expireAfter;
    }

    @Nullable
    @Override
    public V getIfPresent(Object o) {
        CachedValue<V> cachedValue = map.get(o);
        return cachedValue != null ? cachedValue.getValue() : null;
    }

    @Override
    public V get(K k, Callable<? extends V> callable) throws ExecutionException {
        CachedValue<V> cachedValue = map.computeIfAbsent(k, new Function<K, CachedValue<V>>() {
            @Override
            @SneakyThrows
            public CachedValue<V> apply(K i) {
                return new CachedValue<>(callable.call());
            }
        });
        return cachedValue.getValue();
    }

    @Override
    public ImmutableMap<K, V> getAllPresent(Iterable<?> keys) {
        ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
        for (Object key : keys) {
            V value = getIfPresent(key);
            if (value != null) {
                builder.put((K) key, value);
            }
        }
        return builder.build();
    }

    @Override
    public void put(K k, V v) {
        map.put(k, new CachedValue<>(v));
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        map.forEach((k, v) -> put(k, v));
    }

    @Override
    public void invalidate(Object o) {
        map.remove(o);
    }

    @Override
    public void invalidateAll(Iterable<?> iterable) {
        iterable.forEach(o -> invalidate(o));
    }

    @Override
    public void invalidateAll() {
        map.clear();
    }

    @Override
    public long size() {
        return map.size();
    }

    @Override
    public CacheStats stats() {
        return null;
    }

    @Override
    public ConcurrentMap<K, V> asMap() {
        throw new UnsupportedOperationException("asMap");
    }

    @Override
    public void cleanUp() {
        for (Iterator<CachedValue<V>> iterator = map.values().iterator(); iterator.hasNext(); ) {
            CachedValue<V> value = iterator.next();
            if (System.currentTimeMillis() - value.getLastAccess() > expireAfter) {
                iterator.remove();
            }
        }
    }

    private final class CachedValue<V> {
        private V value;
        private long lastAccess;

        private CachedValue(V value) {
            this.value = value;
            lastAccess = System.currentTimeMillis();
        }

        public V getValue() {
            lastAccess = System.currentTimeMillis();
            return value;
        }

        public void setValue(V value) {
            lastAccess = System.currentTimeMillis();
            this.value = value;
        }

        public long getLastAccess() {
            return lastAccess;
        }
    }
}
