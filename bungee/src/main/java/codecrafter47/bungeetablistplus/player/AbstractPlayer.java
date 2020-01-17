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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.taboverlay.config.player.Player;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import lombok.Value;

import javax.annotation.Nonnull;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;

public abstract class AbstractPlayer implements Player {
    private final UUID uuid;
    private final String name;

    final DataHolder serverData;

    AbstractPlayer(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
        this.serverData = new ServerDataHolder(AbstractPlayer::getServerDataHolder);
    }

    @Nonnull
    @Override
    public final String getName() {
        return name;
    }

    @Nonnull
    @Override
    public final UUID getUniqueID() {
        return uuid;
    }

    protected abstract DataHolder getResponsibleDataHolder(DataKey<?> key);

    @Override
    public final <V> V get(DataKey<V> key) {
        return getResponsibleDataHolder(key).get(key);
    }

    @Override
    public final <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {
        getResponsibleDataHolder(key).addDataChangeListener(key, listener);
    }

    @Override
    public final <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {
        getResponsibleDataHolder(key).removeDataChangeListener(key, listener);
    }

    private class ServerDataHolder implements DataHolder {

        private final Function<String, DataHolder> serverDataHolderResolver;

        private final Map<ListenerKey, ManagedDataChangeListener<?>> listenerMap = new Object2ObjectOpenHashMap<>();

        private ServerDataHolder(Function<String, DataHolder> serverDataHolderResolver) {
            this.serverDataHolderResolver = serverDataHolderResolver;
        }

        @Override
        public <V> V get(DataKey<V> dataKey) {
            String server = AbstractPlayer.this.get(BungeeData.BungeeCord_Server);
            if (server != null) {
                return serverDataHolderResolver.apply(server).get(dataKey);
            }
            return null;
        }

        @Override
        public <T> void addDataChangeListener(DataKey<T> dataKey, Runnable dataChangeListener) {
            ListenerKey key = new ListenerKey(AbstractPlayer.this, dataKey, dataChangeListener);
            if (!listenerMap.containsKey(key)) {
                ManagedDataChangeListener<T> managedListener = new ManagedDataChangeListener<>(dataChangeListener, dataKey, serverDataHolderResolver);
                listenerMap.put(key, managedListener);
            }
        }

        @Override
        public <T> void removeDataChangeListener(DataKey<T> dataKey, Runnable dataChangeListener) {
            ListenerKey key = new ListenerKey(AbstractPlayer.this, dataKey, dataChangeListener);
            ManagedDataChangeListener<?> managedListener = listenerMap.remove(key);
            if (managedListener != null) {
                managedListener.deactivate();
            }
        }
    }

    private class ManagedDataChangeListener<T> implements Runnable {
        private final Runnable delegate;
        private final DataKey<T> dataKey;
        private final Function<String, DataHolder> serverDataHolderResolver;

        private DataHolder activeDataHolder = NullDataHolder.INSTANCE;

        private ManagedDataChangeListener(Runnable delegate, DataKey<T> dataKey, Function<String, DataHolder> serverDataHolderResolver) {
            this.delegate = delegate;
            this.dataKey = dataKey;
            this.serverDataHolderResolver = serverDataHolderResolver;

            AbstractPlayer.this.addDataChangeListener(BungeeData.BungeeCord_Server, this);
            update(false);
        }

        public void run() {
            update(true);
        }

        private void update(boolean notify) {
            String server = AbstractPlayer.this.get(BungeeData.BungeeCord_Server);
            T oldVal = activeDataHolder.get(dataKey);
            activeDataHolder.removeDataChangeListener(dataKey, delegate);
            if (server == null) {
                activeDataHolder = NullDataHolder.INSTANCE;
            } else {
                activeDataHolder = serverDataHolderResolver.apply(server);
            }
            activeDataHolder.addDataChangeListener(dataKey, delegate);
            if (notify) {
                T newVal = activeDataHolder.get(dataKey);
                if (!Objects.equals(oldVal, newVal)) {
                    delegate.run();
                }
            }
        }

        void deactivate() {
            AbstractPlayer.this.removeDataChangeListener(BungeeData.BungeeCord_Server, this);
            activeDataHolder.removeDataChangeListener(dataKey, delegate);
        }
    }

    @Value
    private static class ListenerKey {
        private AbstractPlayer player;
        private DataKey<?> dataKey;
        private Runnable listener;
    }

    private static DataHolder getServerDataHolder(String server) {
        DataHolder serverDataHolder = null;
        if (server != null) {
            serverDataHolder = BungeeTabListPlus.getInstance().getDataManager().getServerDataHolder(server);
        }
        if (serverDataHolder == null) {
            serverDataHolder = NullDataHolder.INSTANCE;
        }
        return serverDataHolder;
    }
}
