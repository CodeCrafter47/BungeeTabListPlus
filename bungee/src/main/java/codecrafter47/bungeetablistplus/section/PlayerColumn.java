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
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.player.IPlayer;
import codecrafter47.bungeetablistplus.sorting.AdminFirst;
import codecrafter47.bungeetablistplus.sorting.Alphabet;
import codecrafter47.bungeetablistplus.sorting.ISortingRule;
import codecrafter47.bungeetablistplus.sorting.YouFirst;
import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.tablist.TabListContext;

import java.util.*;

public class PlayerColumn {

    final Collection<String> filter;
    private final SlotTemplate prefix;
    private final SlotTemplate suffix;
    private List<IPlayer> players;
    private final List<String> sort;
    private final int maxPlayers;
    private final List<SlotTemplate> playerLines;
    private final List<SlotTemplate> morePlayerLines;

    public PlayerColumn(List<String> filter, SlotTemplate prefix, SlotTemplate suffix, List<String> sortrules, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        this.filter = filter;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sort = sortrules;
        this.maxPlayers = maxPlayers;
        this.playerLines = playerLines;
        this.morePlayerLines = morePlayerLines;
    }

    public void precalculate(TabListContext context) {
        this.players = context.getPlayerManager().getPlayers(filter, context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3);

        final List<ISortingRule> srules = new ArrayList<>();
        for (String rule : sort) {
            if (rule.equalsIgnoreCase("you") || rule.
                    equalsIgnoreCase("youfirst")) {
                srules.add(new YouFirst(context.getViewer()));
            } else if (rule.equalsIgnoreCase("admin") || rule.equalsIgnoreCase(
                    "adminfirst")) {
                srules.add(new AdminFirst());
            } else if (rule.equalsIgnoreCase("alpha") || rule.equalsIgnoreCase(
                    "alphabet") || rule.equalsIgnoreCase("alphabetic") || rule.
                    equalsIgnoreCase("alphabetical") || rule.equalsIgnoreCase(
                    "alphabetically")) {
                srules.add(new Alphabet());
            }
        }

        Collections.sort(players, new Comparator<IPlayer>() {

            @Override
            public int compare(IPlayer p1, IPlayer p2) {
                for (ISortingRule rule : srules) {
                    int i = rule.compare(p1, p2);
                    if (i != 0) {
                        return -i;
                    }
                }
                if (players.indexOf(p2) > players.indexOf(p1)) {
                    return -1;
                }
                return 1;
            }
        });
    }

    public int getMaxSize() {
        int m = players.size();
        if (m > maxPlayers) {
            m = maxPlayers;
        }
        return m * playerLines.size();
    }

    public Slot getSlotAt(TabListContext context, int pos, int size) {
        int playersToShow = players.size();
        if (playersToShow > maxPlayers) {
            playersToShow = maxPlayers;
        }
        if (playersToShow * playerLines.size() > size) {
            playersToShow = (size - morePlayerLines.size()) / playerLines.size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = players.size() - playersToShow;

        if (pos < playersToShow * playerLines.size()) {
            int playerIndex = pos / playerLines.size();
            int playerLinesIndex = pos % playerLines.size();
            IPlayer player = players.get(playerIndex);
            return SlotTemplate.of(SlotTemplate.skin(player.getSkin()), SlotTemplate.ping(player.getPing()),
                    prefix, playerLines.get(playerLinesIndex), suffix)
                    .buildSlot(context.setPlayer(player));
        } else if (other_count > 0) {
            int morePlayerLinesIndex = pos - playersToShow * playerLines.size();
            return SlotTemplate.of(prefix, morePlayerLines.get(morePlayerLinesIndex), suffix).buildSlot(context.setOtherCount(other_count));
        } else {
            return null;
        }
    }
}
