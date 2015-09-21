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
package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.api.ITabList;
import codecrafter47.bungeetablistplus.api.ITabListProvider;
import codecrafter47.bungeetablistplus.layout.LayoutException;
import codecrafter47.bungeetablistplus.tablist.TabList;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.logging.Level;

/**
 * Implementation of the ResendThread. Updates the tablist for all players after
 * the given interval
 *
 * @author Florian Stober
 */
class ResendThread implements Runnable {

    private final SendingQueue resendQueue;
    private double updateIntervall;

    /**
     * @param queue
     * @param updateIntervall
     */
    public ResendThread(SendingQueue queue, double updateIntervall) {
        this.resendQueue = queue;
        this.updateIntervall = updateIntervall;
        if (this.updateIntervall <= 0) {
            this.updateIntervall = 5;
        }
    }

    /**
     *
     */
    @Override
    public void run() {
        while (true) {
            try {
                ProxiedPlayer player = resendQueue.getNext();
                if (player != null) {
                    Object tabList = BungeeTabListPlus.getTabList(player);
                    if (tabList instanceof PlayerTablistHandler) {
                        if (player.getServer() != null) {
                            PlayerTablistHandler tablistHandler = (PlayerTablistHandler) tabList;
                            update(tablistHandler);
                        } else {
                            BungeeTabListPlus.getInstance().sendLater(player);
                        }
                    }
                }

                int sleep = (int) (updateIntervall * 1000 / (ProxyServer.
                        getInstance().getOnlineCount() + 1) / 2);
                if (sleep < 1) {
                    sleep = 1;
                }
                if (sleep > 10) {
                    sleep = 10;
                }
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    // don't care
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().reportError(th);
            }
        }
    }

    private void update(PlayerTablistHandler tablistHandler) {
        if (tablistHandler.getPlayer().getServer() != null) {
            if (BungeeTabListPlus.getInstance().getConfigManager().
                    getMainConfig().excludeServers.contains(tablistHandler.getPlayer().
                    getServer().getInfo().getName()) || tablistHandler.isExcluded()) {
                tablistHandler.unload();
                return;
            }
        }

        ITabListProvider tlp = BungeeTabListPlus.getInstance().
                getTabListManager().getTabListForPlayer(tablistHandler.getPlayer());
        if (tlp == null) {
            tablistHandler.exclude();
            tablistHandler.unload();
            return;
        }

        ITabList tabList = new TabList();
        try {
            tlp.fillTabList(tablistHandler.getPlayer(), tabList);
        } catch (LayoutException ex){
            BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Error in tablist config", ex);
        } catch (Throwable th){
            BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, "Error while updating tablist", th);
        }

        tablistHandler.sendTablist(tabList);
    }

}
