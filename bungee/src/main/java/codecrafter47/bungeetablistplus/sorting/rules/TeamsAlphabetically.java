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

package codecrafter47.bungeetablistplus.sorting.rules;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.player.IPlayer;
import codecrafter47.bungeetablistplus.sorting.SortingRule;
import codecrafter47.bungeetablistplus.tablist.TabListContext;
import codecrafter47.data.Values;

import java.text.Collator;
import java.util.Optional;

public class TeamsAlphabetically implements SortingRule {
    @Override
    public int compare(TabListContext context, IPlayer player1, IPlayer player2) {
        BukkitBridge bridge = BungeeTabListPlus.getInstance().getBridge();
        Optional<String> team1 = bridge.getPlayerInformation(player1, Values.Player.Minecraft.Team);
        Optional<String> team2 = bridge.getPlayerInformation(player2, Values.Player.Minecraft.Team);
        if (team1.isPresent() && team2.isPresent()) {
            return Collator.getInstance().compare(team1.get(), team2.get());
        }
        return 0;
    }
}
