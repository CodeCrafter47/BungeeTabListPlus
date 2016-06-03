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
import codecrafter47.bungeetablistplus.managers.ConnectedPlayerManager;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import codecrafter47.bungeetablistplus.tablist.GenericTabList;
import codecrafter47.bungeetablistplus.tablist.GenericTabListContext;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.tablistproviders.ErrorTabListProvider;
import gnu.trove.set.hash.THashSet;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.Collections;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

class ResendThread implements Runnable, Executor {

    private final Queue<ProxiedPlayer> queue = new ConcurrentLinkedQueue<>();
    private final Queue<Runnable> tasks = new ConcurrentLinkedQueue<>();
    private final Set<ProxiedPlayer> set = Collections.synchronizedSet(new THashSet<>());
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private Thread mainThread = null;

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
        return Objects.equals(Thread.currentThread(), mainThread);
    }

    @Override
    public void run() {
        mainThread = Thread.currentThread();
        while (true) {
            try {
                while (tasks.isEmpty() && queue.isEmpty()) {
                    lock.lock();
                    try {
                        condition.await(1, TimeUnit.SECONDS);
                    } finally {
                        lock.unlock();
                    }
                }
                while (!tasks.isEmpty()) {
                    tasks.poll().run();
                }
                if (!queue.isEmpty()) {
                    ProxiedPlayer player = queue.poll();
                    set.remove(player);
                    if (player.getServer() != null) {
                        ConnectedPlayerManager connectedPlayerManager = BungeeTabListPlus.getInstance().getConnectedPlayerManager();
                        ConnectedPlayer connectedPlayer = connectedPlayerManager.getPlayerIfPresent(player);
                        if (connectedPlayer != null) {
                            update(player, connectedPlayer);
                        }
                    }
                } else {
                    set.clear();
                }
            } catch (InterruptedException ex) {
                break;
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
    }

    private void update(ProxiedPlayer player, ConnectedPlayer connectedPlayer) {
        PlayerTablistHandler tablistHandler = connectedPlayer.getPlayerTablistHandler();

        try {
            Server server = player.getServer();
            if (server != null && (BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().excludeServers.contains(server.getInfo().getName()))) {
                tablistHandler.setPassThrough(true);
                return;
            }

            TabListProvider tlp = BungeeTabListPlus.getInstance().
                    getTabListManager().getTabListForPlayer(player);
            if (tlp == null) {
                tablistHandler.setPassThrough(true);
                return;
            }

            TabList tabList;

            if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player)) {
                int wishedTabListSize = tlp.getWishedTabListSize();
                if (wishedTabListSize < 1) {
                    wishedTabListSize = 1;
                }
                if (wishedTabListSize > 80) {
                    wishedTabListSize = 80;
                }
                int columns = (wishedTabListSize + 19) / 20;
                tabList = new GenericTabList(wishedTabListSize / columns, columns);
            } else {
                tabList = new GenericTabList();
            }

            TabListContext context = new GenericTabListContext(tabList.getRows(), tabList.getColumns(), player, BungeeTabListPlus.getInstance().constructPlayerManager(player));

                context = context.setPlayer(connectedPlayer);

            tlp.fillTabList(player, tabList, context);

            tablistHandler.sendTabList(tabList);
        } catch (Throwable th) {
            try {
                BungeeTabListPlus.getInstance().getLogger().log(th instanceof LayoutException ? Level.WARNING : Level.SEVERE, "Error while updating tablist", th);
                TabList tabList;
                if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(player)) {
                    tabList = new GenericTabList(20, 4);
                } else {
                    tabList = new GenericTabList();
                }

                ErrorTabListProvider.constructErrorTabList(player, tabList, "Error while updating tablist", th);

                tablistHandler.sendTabList(tabList);
            } catch (Throwable th2) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Failed to construct error tab list", th2);
            }
        }
    }
}
