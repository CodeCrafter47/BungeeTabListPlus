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
package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.api.bungee.tablist.TabList;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListContext;
import codecrafter47.bungeetablistplus.api.bungee.tablist.TabListProvider;
import codecrafter47.bungeetablistplus.layout.LayoutException;
import codecrafter47.bungeetablistplus.tablist.GenericTabList;
import codecrafter47.bungeetablistplus.tablist.GenericTabListContext;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.tablistproviders.ErrorTabListProvider;
import gnu.trove.set.hash.THashSet;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;

class ResendThread implements Runnable {

    private final BlockingQueue<ProxiedPlayer> queue = new LinkedBlockingQueue<>();
    private final Set<ProxiedPlayer> set = Collections.synchronizedSet(new THashSet<>());

    public void add(ProxiedPlayer player) {
        if (!set.contains(player)) {
            set.add(player);
            queue.add(player);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if (queue.isEmpty()) {
                    set.clear();
                }
                ProxiedPlayer player = queue.take();
                set.remove(player);
                Object tabList = BungeeTabListPlus.getTabList(player);
                if (tabList instanceof PlayerTablistHandler) {
                    if (player.getServer() != null) {
                        PlayerTablistHandler tablistHandler = (PlayerTablistHandler) tabList;
                        update(tablistHandler);
                    }
                }
            } catch (InterruptedException ex) {
                break;
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
    }

    private void update(PlayerTablistHandler tablistHandler) {
        try {
            if (tablistHandler.getPlayer().getServer() != null) {
                if (BungeeTabListPlus.getInstance().getConfigManager().
                        getMainConfig().excludeServers.contains(tablistHandler.getPlayer().
                        getServer().getInfo().getName()) || tablistHandler.isExcluded()) {
                    tablistHandler.unload();
                    return;
                }
            }

            TabListProvider tlp = BungeeTabListPlus.getInstance().
                    getTabListManager().getTabListForPlayer(tablistHandler.getPlayer());
            if (tlp == null) {
                tablistHandler.exclude();
                tablistHandler.unload();
                return;
            }
            TabList tabList = new GenericTabList();

            TabListContext context = new GenericTabListContext(tabList.getRows(), tabList.getColumns(), tablistHandler.getPlayer(), BungeeTabListPlus.getInstance().constructPlayerManager(tablistHandler.getPlayer()));
            context = context.setPlayer(BungeeTabListPlus.getInstance().getBungeePlayerProvider().wrapPlayer(context.getViewer()));

            tlp.fillTabList(tablistHandler.getPlayer(), tabList, context);

            tablistHandler.sendTablist(tabList);
        } catch (Throwable th) {
            try {
                BungeeTabListPlus.getInstance().getLogger().log(th instanceof LayoutException ? Level.WARNING : Level.SEVERE, "Error while updating tablist", th);
                TabList tabList;
                if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().getProtocolVersion(tablistHandler.getPlayer()) >= 47) {
                    tabList = new GenericTabList(20, 4);
                } else {
                    tabList = new GenericTabList();
                }

                ErrorTabListProvider.constructErrorTabList(tablistHandler.getPlayer(), tabList, "Error while updating tablist", th);

                tablistHandler.sendTablist(tabList);
            } catch (Throwable th2) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to construct error tab list", th2);
            }
        }
    }

}
