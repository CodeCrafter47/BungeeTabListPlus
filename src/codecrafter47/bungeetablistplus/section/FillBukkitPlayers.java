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

import codecrafter47.bungeetablistplus.tablist.Slot;
import codecrafter47.bungeetablistplus.tablist.TabList;
import codecrafter47.bungeetablistplus.tablisthandler.CustomTabListHandler;
import java.util.List;
import net.md_5.bungee.api.connection.ProxiedPlayer;

/**
 *
 * @author Florian Stober
 */
public class FillBukkitPlayers extends Section {

    int startColumn;

    public FillBukkitPlayers(int startColumn) {
        this.startColumn = startColumn;
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        return 0;//((CustomTabListHandler) player.getTabList()).bukkitplayers.size();
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList tabList, int pos,
            int size) {
        /*
         List<String> players = ((CustomTabListHandler) player.getTabList()).bukkitplayers;
         int p = pos;
         for (; p < pos + size; p++) {
         if (players.size() > p - pos) {
         tabList.setSlot(p, new Slot(players.get(p - pos)));
         }
         }
         return p;*/ return pos;
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        // Do nothing
    }

    @Override
    public int getStartCollumn() {
        return this.startColumn;
    }

}
