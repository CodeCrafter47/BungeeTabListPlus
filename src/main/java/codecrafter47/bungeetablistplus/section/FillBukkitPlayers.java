/*
 *
 *  * BungeeTabListPlus - a bungeecord plugin to customize the tablist
 *  *
 *  * Copyright (C) 2014 Florian Stober
 *  *
 *  * This program is free software: you can redistribute it and/or modify
 *  * it under the terms of the GNU General Public License as published by
 *  * the Free Software Foundation, either version 3 of the License, or
 *  * (at your option) any later version.
 *  *
 *  * This program is distributed in the hope that it will be useful,
 *  * but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  * GNU General Public License for more details.
 *  *
 *  * You should have received a copy of the GNU General Public License
 *  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package codecrafter47.bungeetablistplus.section;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.Slot;
import codecrafter47.bungeetablistplus.api.TabList;
import codecrafter47.bungeetablistplus.config.TabListConfig;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author Florian Stober
 */
public class FillBukkitPlayers extends Section {

    private final int startColumn;
    private final TabListConfig config;

    public FillBukkitPlayers(int startColumn, TabListConfig config) {
        this.startColumn = startColumn;
        this.config = config;
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        return 0;
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        try {
            Object tabList = BungeeTabListPlus.getTabList(player);
            Class clasz = tabList.getClass();
            Field bukkitplayers = clasz.getField("bukkitplayers");
            bukkitplayers.setAccessible(true);
            List<String> bplayers = (List<String>) bukkitplayers.get(tabList);
            return bplayers.size();
        } catch (NoSuchFieldException | SecurityException |
                IllegalArgumentException | IllegalAccessException ex) {
            BungeeTabListPlus.getInstance().reportError(ex);
        }
        return 0;
    }

    @Override
    public int calculate(ProxiedPlayer player, TabList ttabList, int pos,
                         int size) {
        try {
            Object tabList = BungeeTabListPlus.getTabList(player);
            Class clasz = tabList.getClass();
            Field bukkitplayers = clasz.getField("bukkitplayers");
            bukkitplayers.setAccessible(true);
            List<String> players = (List<String>) bukkitplayers.get(tabList);
            int p = pos;
            for (; p < pos + size; p++) {
                if (players.size() > p - pos) {
                    ttabList.setSlot(p, new Slot(players.get(p - pos), config.defaultPing));
                }
            }
            return p;
        } catch (IllegalArgumentException | IllegalAccessException |
                NoSuchFieldException ex) {
            BungeeTabListPlus.getInstance().reportError(ex);
        }
        return pos;
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
