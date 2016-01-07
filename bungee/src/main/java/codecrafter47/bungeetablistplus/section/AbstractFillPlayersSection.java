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
import codecrafter47.bungeetablistplus.api.bungee.tablist.Slot;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Iterator;
import java.util.List;
import java.util.OptionalInt;

public abstract class AbstractFillPlayersSection extends Section {

    private final OptionalInt vAlign;
    protected final int maxPlayers;
    private final List<SlotTemplate> playerLines;
    private final List<SlotTemplate> morePlayerLines;
    protected final List<PlayerList> playerLists;

    public AbstractFillPlayersSection(int vAlign, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines, List<PlayerList> playerLists) {
        this.playerLines = playerLines;
        this.morePlayerLines = morePlayerLines;
        this.playerLists = playerLists;
        this.vAlign = vAlign == -1 ? OptionalInt.empty() : OptionalInt.of(vAlign);
        this.maxPlayers = maxPlayers;
    }

    @Override
    public void preCalculate(TabListContext context) {
        for (PlayerList playerList : playerLists) {
            playerList.preCalculate(context);
        }
    }

    @Override
    public int getMinSize() {
        return getEffectiveSize(0);
    }

    @Override
    public int getMaxSize() {
        int m = getNumberOfPlayers();
        if (m > this.maxPlayers) {
            m = this.maxPlayers;
        }
        return m * playerLines.size();
    }

    private int getNumberOfPlayers() {
        int n = 0;
        for (PlayerList playerList : playerLists) {
            n += playerList.players.size();
        }
        return n;
    }

    @Override
    public boolean isSizeConstant() {
        return false;
    }

    @Override
    public int getEffectiveSize(int proposedSize) {
        int playersToShow = getNumberOfPlayers();
        if (playersToShow > this.maxPlayers) {
            playersToShow = this.maxPlayers;
        }
        if (playersToShow * playerLines.size() > proposedSize) {
            playersToShow = (proposedSize - morePlayerLines.size()) / playerLines.size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = getNumberOfPlayers() - playersToShow;
        return playersToShow * playerLines.size() + (other_count > 0 ? morePlayerLines.size() : 0);
    }

    @Override
    public Slot getSlotAt(TabListContext context, int pos, int size) {
        int playersToShow = getNumberOfPlayers();
        if (playersToShow > this.maxPlayers) {
            playersToShow = this.maxPlayers;
        }
        if (playersToShow * playerLines.size() > size) {
            playersToShow = (size - morePlayerLines.size()) / playerLines.size();
            if (playersToShow < 0) {
                playersToShow = 0;
            }
        }
        int other_count = getNumberOfPlayers() - playersToShow;

        if (pos < playersToShow * playerLines.size()) {
            int playerIndex = pos / playerLines.size();
            Iterator<PlayerList> iterator = playerLists.iterator();
            PlayerList playerList = iterator.next();
            while (playerIndex >= playerList.players.size()) {
                playerIndex -= playerList.players.size();
                playerList = iterator.next();
            }
            int playerLinesIndex = pos % playerLines.size();

            IPlayer player = playerList.players.get(playerIndex);
            return SlotTemplate.of(SlotTemplate.skin(player.getSkin()), SlotTemplate.ping(player.getPing()),
                    playerList.prefix, playerLines.get(playerLinesIndex), playerList.suffix)
                    .buildSlot(context.setPlayer(player));
        } else if (other_count > 0) {
            int morePlayerLinesIndex = pos - playersToShow * playerLines.size();
            return morePlayerLines.get(morePlayerLinesIndex).buildSlot(context.setOtherCount(other_count));
        } else {
            return null;
        }
    }

    @Override
    public OptionalInt getStartColumn() {
        return vAlign;
    }

    protected static abstract class PlayerList {
        private final SlotTemplate prefix;
        private final SlotTemplate suffix;
        protected List<IPlayer> players;
        protected final PlayerSorter sorter;

        protected PlayerList(SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter) {
            this.prefix = prefix;
            this.suffix = suffix;
            this.sorter = sorter;
        }

        public void preCalculate(TabListContext context) {
            players = getPlayers(context.getViewer(), context);
            sorter.sort(context, players);
        }

        protected abstract List<IPlayer> getPlayers(ProxiedPlayer player, TabListContext context);
    }

}
