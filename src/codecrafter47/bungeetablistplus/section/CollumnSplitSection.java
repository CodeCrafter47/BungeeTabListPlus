/*
 * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *
 * Copyright (C) 2014 Florian Stober
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

import codecrafter47.bungeetablistplus.managers.ConfigManager;
import codecrafter47.bungeetablistplus.tablist.TabList;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Florian Stober
 */
public class CollumnSplitSection extends Section {

    PlayerColumn pc[] = new PlayerColumn[ConfigManager.getCols()];

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        int max = 0;
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null) {
                int m = pc[i].getMaxSize(player);
                int span = 1;
                while (i + span != pc.length && pc[i + span] != null && (i + span < pc.length && pc[i + span - 1].filter.
                        equals(pc[i + span].filter))) {
                    span++;
                }
                m = (m + span - 1) / span;
                if (max < m) {
                    max = m;
                }
                i += span - 1;
            }
        }
        return max * ConfigManager.getCols();
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        int sizePerCol = size / ConfigManager.getCols();
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null) {
                int span = 1;
                while (i + span != pc.length && pc[i + span] != null && (i + span < pc.length && pc[i + span - 1].filter.
                        equals(pc[i + span].filter))) {
                    span++;
                }
                pc[i].calculate(player, tabList, i, pos / ConfigManager.
                        getCols(), sizePerCol * span, span);
                i += span - 1;
            }
        }
        return pos + sizePerCol * ConfigManager.getCols();
    }

    public void addCollumn(int i, PlayerColumn collumn) {
        pc[i] = collumn;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        for (int i = 0; i < pc.length; i++) {
            if (pc[i] != null) {
                pc[i].precalculate(player);
            }
        }
    }

    @Override
    public int getStartCollumn() {
        return 0;
    }

}
