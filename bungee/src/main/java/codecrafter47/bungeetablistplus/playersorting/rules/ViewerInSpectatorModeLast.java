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
import codecrafter47.bungeetablistplus.context.Context;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.playersorting.SortingRule;

public class ViewerInSpectatorModeLast implements SortingRule {

    @Override
    public int compare(Context context, IPlayer player1, IPlayer player2) {
        Integer gamemode = context.get(Context.KEY_VIEWER).get(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE);
        if (gamemode != null && gamemode == 3) {
            if (player1 == context.get(Context.KEY_VIEWER)) {
                return 1;
            }
            if (player2 == context.get(Context.KEY_VIEWER)) {
                return -1;
            }
        }
        return 0;
    }

}
