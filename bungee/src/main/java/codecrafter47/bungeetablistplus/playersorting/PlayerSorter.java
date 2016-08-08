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

package codecrafter47.bungeetablistplus.playersorting;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.PlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.player.Player;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

public class PlayerSorter {
    private final List<SortingRule> rules;

    public PlayerSorter(List<SortingRule> rules) {
        this.rules = rules;
    }

    public PlayerSorter(String desc) {
        this(Arrays.stream(desc.split(",")).sequential().map(r -> {
            Optional<SortingRule> rule = SortingRuleRegistry.getRule(r);
            if (rule.isPresent()) {
                return rule.get();
            } else {
                BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Sorting rule \"{0}\" does not exists.", r);
                return null;
            }
        }).filter(Objects::nonNull).collect(Collectors.toList()));
    }

    public void sort(Context context, List<Player> players) {
        // todo replace with something more efficient
        sort(new TabListContext() {
            @Override
            public int getTabSize() {
                return 0;
            }

            @Override
            public int getRows() {
                return 0;
            }

            @Override
            public int getColumns() {
                return 0;
            }

            @Override
            public ProxiedPlayer getViewer() {
                return ((ConnectedPlayer) context.getViewer()).getPlayer();
            }

            @Override
            public PlayerManager getPlayerManager() {
                return null;
            }

            @Override
            public IPlayer getPlayer() {
                return context.getPlayer();
            }

            @Override
            public Optional<ServerInfo> getServer() {
                return context.getPlayer().getServer();
            }

            @Override
            public Optional<ServerGroup> getServerGroup() {
                return Optional.empty();
            }

            @Override
            public int getOtherPlayerCount() {
                return context.getOtherPlayersCount();
            }

            @Override
            public TabListContext setPlayer(IPlayer player) {
                return null;
            }

            @Override
            public TabListContext setOtherCount(int otherCount) {
                return null;
            }

            @Override
            public TabListContext setServerGroup(ServerGroup serverGroup) {
                return null;
            }
        }, players);
    }

    public void sort(TabListContext context, List<? extends IPlayer> players) {
        Collections.sort(players, (p1, p2) -> {
            for (SortingRule rule : rules) {
                int i = rule.compare(context, p1, p2);
                if (i != 0) {
                    return i;
                }
            }
            if (players.indexOf(p2) > players.indexOf(p1)) {
                return -1;
            }
            return 1;
        });
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerSorter that = (PlayerSorter) o;

        return rules.equals(that.rules);
    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }
}
