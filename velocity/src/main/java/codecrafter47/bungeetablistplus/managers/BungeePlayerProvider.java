/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.data.BTLPVelocityDataKeys;
import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import com.velocitypowered.api.proxy.Player;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import io.netty.util.concurrent.EventExecutor;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class BungeePlayerProvider implements PlayerProvider {

    private final EventExecutor mainThread;

    private final Map<Player, VelocityPlayer> players = new ConcurrentHashMap<>();
    private final Set<Listener> listeners = new ReferenceOpenHashSet<>();

    public BungeePlayerProvider(EventExecutor mainThread) {
        this.mainThread = mainThread;
        mainThread.scheduleWithFixedDelay(this::checkForStalePlayers, 5, 5, TimeUnit.MINUTES);
    }

    private void checkForStalePlayers() {
        for (Map.Entry<Player, VelocityPlayer> entry : players.entrySet()) {
            if (!entry.getKey().isActive()) {
                if (!entry.getValue().isStale()) {
                    entry.getValue().setStale(true);
                } else {
                    BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Player " + entry.getKey().getUsername() + " is no longer connected to the network, but PlayerDisconnectEvent has not been called.");
                    onPlayerDisconnected(entry.getKey());
                }
            }
        }

    }

    @Override
    public Collection<? extends VelocityPlayer> getPlayers() {
        return Collections.unmodifiableCollection(players.values());
    }

    @Nonnull
    public VelocityPlayer getPlayer(Player player) {
        return Objects.requireNonNull(getPlayerIfPresent(player));
    }

    @Nullable
    public VelocityPlayer getPlayerIfPresent(Player player) {
        return players.get(player);
    }

    public VelocityPlayer onPlayerConnected(Player player) {
        VelocityPlayer velocityPlayer = new VelocityPlayer(player);
        String version = BungeeTabListPlus.getInstance().getProtocolVersionProvider().getVersion(player);
        boolean version_below_1_8 = !BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player);
        mainThread.execute(() -> {
            velocityPlayer.getLocalDataCache().updateValue(BTLPVelocityDataKeys.DATA_KEY_CLIENT_VERSION, version);
            velocityPlayer.getLocalDataCache().updateValue(BTLPVelocityDataKeys.DATA_KEY_CLIENT_VERSION_BELOW_1_8, version_below_1_8);
            players.put(player, velocityPlayer);
            listeners.forEach(listener -> listener.onPlayerAdded(velocityPlayer));
        });
        return velocityPlayer;
    }

    public void onPlayerDisconnected(Player player) {
        mainThread.execute(() -> {
            VelocityPlayer velocityPlayer;
            if (null == (velocityPlayer = players.remove(player))) {
                return;
            }
            listeners.forEach(listener -> listener.onPlayerRemoved(velocityPlayer));
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
