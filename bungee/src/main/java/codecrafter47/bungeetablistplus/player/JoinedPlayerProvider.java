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

import de.codecrafter47.taboverlay.config.player.Player;
import de.codecrafter47.taboverlay.config.player.PlayerProvider;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

public class JoinedPlayerProvider implements PlayerProvider {

    private final Set<Listener> listeners = new ObjectOpenHashSet<>();
    private final Set<Player> players = new ObjectOpenHashSet<>();

    public JoinedPlayerProvider(Collection<? extends PlayerProvider> providers) {

        Listener myListener = new Listener() {
            @Override
            public void onPlayerAdded(Player p) {
                players.add(p);
                listeners.forEach(listener -> listener.onPlayerAdded(p));
            }

            @Override
            public void onPlayerRemoved(Player p) {
                players.remove(p);
                listeners.forEach(listener -> listener.onPlayerRemoved(p));
            }
        };

        for (PlayerProvider provider : providers) {
            players.addAll(provider.getPlayers());
            provider.registerListener(myListener);
        }
    }

    @Override
    public Collection<Player> getPlayers() {
        return Collections.unmodifiableCollection(players);
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
