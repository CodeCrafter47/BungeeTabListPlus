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

import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.skin.Skin;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

/**
 * @author Florian Stober
 */
public class AutoFillPlayers extends Section {

    public final String prefix;
    public final String suffix;
    public final int startColumn;
    public final int maxPlayers;
    public final List<String> sortRules;
    public final Skin skin;

    public AutoFillPlayers(int startColumn, String prefix, String suffix, Skin skin,
                           List<String> sortRules, int maxPlayers) {
        this.prefix = prefix;
        this.suffix = suffix;
        this.startColumn = startColumn;
        this.sortRules = sortRules;
        this.maxPlayers = maxPlayers;
        this.skin = skin;
    }

    @Override
    public int getMinSize(ProxiedPlayer player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getMaxSize(ProxiedPlayer player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int calculate(ProxiedPlayer player, ITabList ITabList, int pos,
                         int size) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void precalculate(ProxiedPlayer player) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int getStartCollumn() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
