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

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.data.DataKeys;
import codecrafter47.bungeetablistplus.player.Player;
import codecrafter47.bungeetablistplus.playersorting.SortingRule;

public class AFKLast implements SortingRule {

    @Override
    public int compare(TabListContext context, IPlayer player1, IPlayer player2) {
        boolean player1AFK = ((Player) player1).get(DataKeys.Essentials_IsAFK).orElse(false);
        boolean player2AFK = ((Player) player2).get(DataKeys.Essentials_IsAFK).orElse(false);
        if (player1AFK == player2AFK) {
            return 0;
        }
        if (player1AFK) {
            return 1;
        }
        if (player2AFK) {
            return -1;
        }
        return 0;
    }

}
