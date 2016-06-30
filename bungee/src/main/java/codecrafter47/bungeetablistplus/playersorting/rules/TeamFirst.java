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

package codecrafter47.bungeetablistplus.playersorting.rules;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.playersorting.SortingRule;

import java.util.Optional;

public class TeamFirst implements SortingRule {
    @Override
    public int compare(TabListContext context, IPlayer player1, IPlayer player2) {
        IPlayer viewer = BungeeTabListPlus.getInstance().getConnectedPlayerManager().getPlayerIfPresent(context.getViewer());
        if (viewer != null) {
            Optional<String> team = ((Player) viewer).get(DataKeys.Team);
            if (team.isPresent()) {
                Optional<String> team1 = ((Player) player1).get(DataKeys.Team);
                Optional<String> team2 = ((Player) player2).get(DataKeys.Team);
                if (!team1.equals(team2)) {
                    if (team1.equals(team)) return -1;
                    if (team2.equals(team)) return 1;
                }
            }
        }
        return 0;
    }
}
