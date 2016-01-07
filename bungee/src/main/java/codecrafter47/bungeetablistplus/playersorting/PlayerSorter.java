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

package codecrafter47.bungeetablistplus.playersorting;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;

import java.util.Collections;
import java.util.List;
import java.util.logging.Level;

public class PlayerSorter {
    private final List<SortingRule> rules;

    public PlayerSorter(List<SortingRule> rules) {
        this.rules = rules;
    }

    public void sort(TabListContext context, List<IPlayer> players) {
        Throwable exception = null;
        // TODO this isn't a really efficient solution for the problem
        //      problem is we are sorting mutable data. Trying 5 times increases
        //      the chance that the won't be an error. A better solution would be
        //      to use a sort algorithm that doesn't care about concurrent modification
        //      of the sorted data. If the players aren't sorted 100% correctly
        //      that would be acceptable.
        for (int attempt = 1; attempt <= 5; attempt++) {
            try {
                Collections.sort(players, (p1, p2) -> {
                    for (SortingRule rule : rules) {
                        int i = rule.compare(context, p1, p2);
                        if (i != 0) {
                            return i;
                        }
                    }
                    if (players.indexOf(p2) > players.indexOf(p1)) {
                        return -1;
                    }
                    return 1;
                });
            } catch (IllegalArgumentException ex) {
                exception = ex;
            }
        }
        if (exception != null) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to sort players using rules " + rules, exception);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PlayerSorter that = (PlayerSorter) o;

        return rules.equals(that.rules);

    }

    @Override
    public int hashCode() {
        return rules.hashCode();
    }
}
