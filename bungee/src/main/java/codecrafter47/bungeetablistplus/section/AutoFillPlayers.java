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

import codecrafter47.bungeetablistplus.api.bungee.ServerGroup;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AutoFillPlayers implements Function<TabListContext, List<Section>> {

    public final List<ServerGroup> groups;
    private final List<Function<ServerGroup, Section>> sections;
    private final boolean showEmptyGroups;

    public AutoFillPlayers(List<ServerGroup> groups, List<Function<ServerGroup, Section>> sections, boolean showEmptyGroups) {
        this.groups = groups;
        this.sections = sections;
        this.showEmptyGroups = showEmptyGroups;
    }

    @Override
    public List<Section> apply(TabListContext context) {
        // sort groups, most populated first
        Collections.sort(groups, (s1, s2) -> {
            int p1 = context.getPlayerManager().getPlayerCount(s1.getServerNames());
            int p2 = context.getPlayerManager().getPlayerCount(s2.getServerNames());
            if (p1 < p2) {
                return 1;
            }
            if (p1 > p2) {
                return -1;
            }
            return s1.getName().compareTo(s2.getName());
        });

        List<Section> sections = new ArrayList<>();
        for (ServerGroup serverGroup : groups) {
            if (showEmptyGroups || context.getPlayerManager().getPlayerCount(serverGroup.getServerNames()) > 0) {
                sections.addAll(this.sections.stream().map(section -> section.apply(serverGroup)).collect(Collectors.toList()));
            }
        }

        return sections;
    }
}
