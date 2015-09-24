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
import codecrafter47.bungeetablistplus.tablist.TabListContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AutoFillPlayers implements Function<TabListContext, List<Section>> {

    public final List<String> groups;
    private List<Function<List<String>, Section>> sections;
    private final boolean showEmptyGroups;

    public AutoFillPlayers(List<String> list, List<Function<List<String>, Section>> sections, boolean showEmptyGroups) {
        this.groups = list;
        this.sections = sections;
        this.showEmptyGroups = showEmptyGroups;
    }

    @Override
    public List<Section> apply(TabListContext context) {
        // sort groups, most populated first
        Collections.sort(groups, (s1, s2) -> {
            int p1 = context.
                    getPlayerManager().getServerPlayerCount(s1, context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3);
            int p2 = context.
                    getPlayerManager().getServerPlayerCount(s2, context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3);
            if (p1 < p2) {
                return 1;
            }
            if (p1 > p2) {
                return -1;
            }
            return s1.compareTo(s2);
        });

        List<Section> sections = new ArrayList<>();
        for (String server : groups) {
            List<String> filter = Arrays.asList(server.split(","));
            if (showEmptyGroups || context.getPlayerManager().getPlayerCount(filter, context.getViewer(), BungeeTabListPlus.getInstance().getConfigManager().getMainConfig().showPlayersInGamemode3) > 0) {
                sections.addAll(this.sections.stream().map(section -> section.apply(filter)).collect(Collectors.toList()));
            }
        }

        return sections;
    }
}
