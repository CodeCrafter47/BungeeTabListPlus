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

package codecrafter47.bungeetablistplus.tablistproviders.legacy;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.logging.Level;

public class CheckedTabListProvider implements TabListProvider {
    private final TabListProvider delegate;

    public CheckedTabListProvider(TabListProvider delegate) {
        this.delegate = delegate;
    }

    @Override
    public void fillTabList(ProxiedPlayer player, TabList tabList, TabListContext context) {
        try {
            delegate.fillTabList(player, tabList, context);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "An error occurred while invoking TabListProvider " + delegate.getClass(), th);
            ErrorTabListProvider.constructErrorTabList(player, tabList, "An error occurred while invoking TabListProvider " + delegate.getClass(), th);
        }
    }
}
