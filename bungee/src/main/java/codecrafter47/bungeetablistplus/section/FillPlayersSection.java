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
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.ArrayList;
import java.util.List;

public class FillPlayersSection extends AbstractFillPlayersSection {

    public FillPlayersSection(int vAlign, PlayerManager.Filter filter, SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter, int minSlots, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        super(vAlign, minSlots, maxPlayers, playerLines, morePlayerLines, new ArrayList<>());
        addPlayers(filter, prefix, suffix, sorter);
    }

    public void addPlayers(PlayerManager.Filter filter, SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter) {
        playerLists.add(new Players(prefix, suffix, sorter, filter));
    }

    public boolean allowsExtension() {
        return minSlots == 0 && maxPlayers == 1000 && !getStartColumn().isPresent();
    }

    private class Players extends PlayerList {

        private final PlayerManager.Filter filter;

        protected Players(SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter, PlayerManager.Filter filter) {
            super(prefix, suffix, sorter);
            this.filter = filter;
        }

        @Override
        protected List<IPlayer> getPlayers(ProxiedPlayer player, TabListContext context) {
            return context.getPlayerManager().getPlayers(filter);
        }

    }
}
