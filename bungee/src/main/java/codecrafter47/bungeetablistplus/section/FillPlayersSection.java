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

import java.util.List;

public class FillPlayersSection extends AbstractFillPlayersSection {

    private final PlayerManager.Filter filter;

    public FillPlayersSection(int vAlign, PlayerManager.Filter filter, SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        super(vAlign, prefix, suffix, sorter, maxPlayers, playerLines, morePlayerLines);
        this.filter = filter;
    }

    @Override
    public void precalculate(TabListContext context) {
        players = getPlayers(context.getViewer(), context);
        sorter.sort(context, players);
    }

    @Override
    protected List<IPlayer> getPlayers(ProxiedPlayer player, TabListContext context) {
        return context.getPlayerManager().getPlayers(filter);
    }
}
