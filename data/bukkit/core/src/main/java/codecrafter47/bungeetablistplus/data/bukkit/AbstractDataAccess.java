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
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;


public abstract class AbstractDataAccess<B> implements DataAccess<B> {
    protected final Logger logger;
    private final Plugin plugin;
    private final Map<DataKey<?>, Function<B, ?>> providersByDataKey = new HashMap<>();
    private final Map<Class, BiFunction<B, DataKey<?>, ?>> providersByDataKeyClass = new HashMap<>();

    public AbstractDataAccess(Logger logger, Plugin plugin) {
        this.logger = logger;
        this.plugin = plugin;
    }

    protected <V> void bind(DataKey<V> dataKey, Function<B, V> provider) {
        providersByDataKey.put(dataKey, provider);
    }

    @SuppressWarnings("unchecked")
    protected <V, K extends DataKey<V>> void bind(Class<K> clazz, BiFunction<B, K, V> provider) {
        providersByDataKeyClass.put(clazz, (BiFunction<B, DataKey<?>, ?>) (Object) provider);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <V> Optional<V> getValue(DataKey<V> key, B context) {
        try {
            if (providersByDataKeyClass.containsKey(key.getClass())) {
                return Optional.ofNullable(providersByDataKeyClass.get(key.getClass())).map(provider -> (V) provider.apply(context, key));
            } else {
                return Optional.ofNullable(providersByDataKey.get(key)).map(provider -> (V) provider.apply(context));
            }
        } catch (Throwable th) {
            if (isAsyncOpError(th)) {
                if (!Bukkit.isPrimaryThread()) {
                    try {
                        return Bukkit.getScheduler().callSyncMethod(plugin, () -> getValue(key, context)).get();
                    } catch (InterruptedException | ExecutionException e) {
                        logger.log(Level.SEVERE, "Unexpected exception", e);
                    }
                }
            }
            logger.log(Level.SEVERE, "Unexpected exception", th);
        }
        return Optional.empty();
    }

    private boolean isAsyncOpError(Throwable th) {
        return th instanceof ConcurrentModificationException || th.getStackTrace()[0].getClassName().equals("org.spigotmc.AsyncCatcher") || (th.getCause() != null && isAsyncOpError(th.getCause()));
    }
}
