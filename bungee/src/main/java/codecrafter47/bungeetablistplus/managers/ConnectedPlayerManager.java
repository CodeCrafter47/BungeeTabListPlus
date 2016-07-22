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
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.IPlayerProvider;
import com.google.common.collect.ImmutableList;
import lombok.Synchronized;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class ConnectedPlayerManager implements IPlayerProvider {

    private Set<ConnectedPlayer> players = Collections.newSetFromMap(new IdentityHashMap<>());
    private List<ConnectedPlayer> playerList = Collections.emptyList();
    private Map<String, ConnectedPlayer> byName = new HashMap<>();
    private Map<UUID, ConnectedPlayer> byUUID = new HashMap<>();

    @Override
    public Collection<ConnectedPlayer> getPlayers() {
        return playerList;
    }

    @Nonnull
    public ConnectedPlayer getPlayer(ProxiedPlayer player) {
        return Objects.requireNonNull(getPlayerIfPresent(player));
    }

    @Nonnull
    public ConnectedPlayer getPlayer(String name) {
        return Objects.requireNonNull(byName.get(name));
    }

    @Nonnull
    public ConnectedPlayer getPlayer(UUID uuid) {
        return Objects.requireNonNull(byUUID.get(uuid));
    }

    @Nullable
    public ConnectedPlayer getPlayerIfPresent(ProxiedPlayer player) {
        ConnectedPlayer connectedPlayer = byName.get(player.getName());
        return connectedPlayer != null && connectedPlayer.getPlayer() == player ? connectedPlayer : null;
    }

    @Nullable
    public ConnectedPlayer getPlayerIfPresent(String name) {
        return byName.get(name);
    }

    @Nullable
    public ConnectedPlayer getPlayerIfPresent(UUID uuid) {
        return byUUID.get(uuid);
    }

    @Synchronized
    public void onPlayerConnected(ConnectedPlayer player) {
        player.setBukkitData(BungeeTabListPlus.getInstance().getBridge().onConnected(player.getPlayer()));
        players.add(player);
        byName.put(player.getName(), player);
        byUUID.put(player.getUniqueID(), player);
        playerList = ImmutableList.copyOf((Iterable<? extends ConnectedPlayer>) players);
    }

    @Synchronized
    public void onPlayerDisconnected(ConnectedPlayer player) {
        players.remove(player);
        byName.remove(player.getName(), player);
        byUUID.remove(player.getUniqueID(), player);
        BungeeTabListPlus.getInstance().getBridge().onDisconnected(player.getPlayer());
        playerList = ImmutableList.copyOf((Iterable<? extends ConnectedPlayer>) players);
    }
}
