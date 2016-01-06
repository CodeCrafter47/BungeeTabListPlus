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

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.PlayerManager;
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;

import java.util.List;

public class PlayerColumn {

    final PlayerManager.Filter filter;
    private final SlotTemplate prefix;
    private final SlotTemplate suffix;
    private List<IPlayer> players;
    private final PlayerSorter sorter;
    private final int maxPlayers;
    private final List<SlotTemplate> playerLines;
    private final List<SlotTemplate> morePlayerLines;

    public PlayerColumn(PlayerManager.Filter filter, SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        this.filter = filter;
        this.prefix = prefix;
        this.suffix = suffix;
        this.sorter = sorter;
        this.maxPlayers = maxPlayers;
        this.playerLines = playerLines;
        this.morePlayerLines = morePlayerLines;
    }

    public void precalculate(TabListContext context) {
        this.players = context.getPlayerManager().getPlayers(filter);
        sorter.sort(context, players);
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
