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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.player.BungeePlayer;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import io.netty.util.concurrent.EventExecutor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class BungeePlayerProvider implements PlayerProvider {

    private final EventExecutor mainThread;

    private final Map<ProxiedPlayer, BungeePlayer> players = new ConcurrentHashMap<>();
    private final Set<Listener> listeners = new ReferenceOpenHashSet<>();

    public BungeePlayerProvider(EventExecutor mainThread) {
        this.mainThread = mainThread;
        mainThread.scheduleWithFixedDelay(this::checkForStalePlayers, 5, 5, TimeUnit.MINUTES);
    }

    private void checkForStalePlayers() {
        for (Map.Entry<ProxiedPlayer, BungeePlayer> entry : players.entrySet()) {
            if (!entry.getKey().isConnected()) {
                if (!entry.getValue().isStale()) {
                    entry.getValue().setStale(true);
                } else {
                    BungeeTabListPlus.getInstance().getLogger().severe("Player " + entry.getKey().getName() + " is no longer connected to the network, but PlayerDisconnectEvent has not been called.");
                    onPlayerDisconnected(entry.getKey());
                }
            }
        }

    }

    @Override
    public Collection<? extends BungeePlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    @Nonnull
    public BungeePlayer getPlayer(ProxiedPlayer player) {
        return Objects.requireNonNull(getPlayerIfPresent(player));
    }

    @Nullable
    public BungeePlayer getPlayerIfPresent(ProxiedPlayer player) {
        return players.get(player);
    }

    public BungeePlayer onPlayerConnected(ProxiedPlayer player) {
        BungeePlayer bungeePlayer = new BungeePlayer(player);
        String version = BungeeTabListPlus.getInstance().getProtocolVersionProvider().getVersion(player);
        boolean version_below_1_8 = !BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player);
        mainThread.execute(() -> {
            bungeePlayer.getLocalDataCache().updateValue(BTLPBungeeDataKeys.DATA_KEY_CLIENT_VERSION, version);
            bungeePlayer.getLocalDataCache().updateValue(BTLPBungeeDataKeys.DATA_KEY_CLIENT_VERSION_BELOW_1_8, version_below_1_8);
            players.put(player, bungeePlayer);
            listeners.forEach(listener -> listener.onPlayerAdded(bungeePlayer));
        });
        return bungeePlayer;
    }

    public void onPlayerDisconnected(ProxiedPlayer player) {
        mainThread.execute(() -> {
            BungeePlayer bungeePlayer;
            if (null == (bungeePlayer = players.remove(player))) {
                return;
            }
            listeners.forEach(listener -> listener.onPlayerRemoved(bungeePlayer));
        });
    }

    @Override
    public void registerListener(Listener listener) {
        listeners.add(listener);
    }

    @Override
    public void unregisterListener(Listener listener) {
        listeners.remove(listener);
    }
}
