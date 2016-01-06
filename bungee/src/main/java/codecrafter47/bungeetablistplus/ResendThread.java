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

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

class ResendThread implements Runnable, Executor {

    private final Queue<ProxiedPlayer> queue = new LinkedList<>();
    private final Queue<Runnable> tasks = new LinkedList<>();
    private final Set<ProxiedPlayer> set = new THashSet<>();
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();

    public void add(ProxiedPlayer player) {
        lock.lock();
        try {
            if (!set.contains(player)) {
                set.add(player);
                queue.add(player);
                condition.signal();
            }
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void execute(Runnable runnable) {
        lock.lock();
        try {
            tasks.add(runnable);
            condition.signal();
        } finally {
            lock.unlock();
        }
    }

    public boolean isInMainThread() {
        return lock.isHeldByCurrentThread();
    }

    @Override
    public void run() {
        lock.lock();
        try {
            while (true) {
                try {
                    while (!tasks.isEmpty()) {
                        tasks.poll().run();
                    }
                    while (!queue.isEmpty()) {
                        ProxiedPlayer player = queue.poll();
                        set.remove(player);
                        Object tabList = BungeeTabListPlus.getTabList(player);
                        if (tabList instanceof PlayerTablistHandler) {
                            if (player.getServer() != null) {
                                PlayerTablistHandler tablistHandler = (PlayerTablistHandler) tabList;
                                update(tablistHandler);
                            }
                        }
                    }
                    set.clear();
                    condition.await(1, TimeUnit.SECONDS);
                } catch (InterruptedException ex) {
                    break;
                } catch (Throwable th) {
                    BungeeTabListPlus.getInstance().reportError(th);
                }
            }
        } finally {
            lock.unlock();
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
            TabList tabList;

            if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().getProtocolVersion(tablistHandler.getPlayer()) >= 47) {
                int whishedTabListSize = tlp.getWhishedTabListSize();
                if (whishedTabListSize < 1) {
                    whishedTabListSize = 1;
                }
                if (whishedTabListSize > 80) {
                    whishedTabListSize = 80;
                }
                int columns = (whishedTabListSize + 19) / 20;
                tabList = new GenericTabList(whishedTabListSize / columns, columns);
            } else {
                tabList = new GenericTabList();
            }

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
