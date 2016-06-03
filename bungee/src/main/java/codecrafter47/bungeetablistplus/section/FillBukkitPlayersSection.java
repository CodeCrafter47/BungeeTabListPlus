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
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.SlotTemplate;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.playersorting.PlayerSorter;
import lombok.SneakyThrows;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.List;

/**
 * @author Florian Stober
 */
public class FillBukkitPlayersSection extends AbstractFillPlayersSection {

    public FillBukkitPlayersSection(int startColumn, SlotTemplate prefix, SlotTemplate suffix,
                                    PlayerSorter sorter, int minSlots, int maxPlayers, List<SlotTemplate> playerLines, List<SlotTemplate> morePlayerLines) {
        super(startColumn, minSlots, maxPlayers, playerLines, morePlayerLines, Collections.singletonList(new BukkitPlayers(prefix, suffix, sorter)));
    }

    private static class BukkitPlayers extends PlayerList {
        protected BukkitPlayers(SlotTemplate prefix, SlotTemplate suffix, PlayerSorter sorter) {
            super(prefix, suffix, sorter);
        }

        @Override
        @SneakyThrows
        protected List<IPlayer> getPlayers(ProxiedPlayer player, TabListContext context) {
            ConnectedPlayer connectedPlayer = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(player);
            if (connectedPlayer != null) {
                return connectedPlayer.getPlayerTablistHandler().getServerTabList();
            } else {
                return Collections.emptyList();
            }
        }
    }
}
