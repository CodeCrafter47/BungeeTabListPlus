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
package codecrafter47.bungeetablistplus.tablisthandler;

import codecrafter47.bungeetablistplus.api.bungee.IPlayer;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;

/**
 * @author Florian Stober
 */
public interface PlayerTablistHandler {

    void sendTablist(TabList tabList);

    boolean isExcluded();

    void unload();

    void exclude();

    ProxiedPlayer getPlayer();

    List<IPlayer> getPlayers();

    void setTabListHandler(TabListHandler tabListHandler);
}
