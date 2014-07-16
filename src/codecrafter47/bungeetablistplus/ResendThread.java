/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package codecrafter47.bungeetablistplus;

import codecrafter47.bungeetablistplus.tablisthandler.IMyTabListHandler;
import io.netty.handler.logging.LogLevel;
import java.util.logging.Level;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.tab.TabListHandler;

/**
 *
 * @author florian
 */
public class ResendThread implements Runnable {

    private final SendingQueue resendQueue;
    private double updateIntervall;

    public ResendThread(SendingQueue queue, double updateIntervall) {
        this.resendQueue = queue;
        this.updateIntervall = updateIntervall;
        if (this.updateIntervall <= 0) {
            this.updateIntervall = 5;
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                ProxiedPlayer player = resendQueue.getNext();
                if (player != null) {
                    TabListHandler tabList = player.getTabList();
                    if (tabList instanceof IMyTabListHandler) {
                        if(player.getServer() != null){
                        ((IMyTabListHandler) tabList).recreate();
                        }else{
                            BungeeTabListPlus.getInstance().sendLater(player);
                        }
                    }
                }

                int sleep = (int) (updateIntervall * 1000 / (ProxyServer.getInstance().getOnlineCount() + 1) / 2);
                if (sleep < 1) {
                    sleep = 1;
                }
                if (sleep > 10) {
                    sleep = 10;
                }
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE, null, ex);
                }
            } catch (Throwable th) {
                BungeeTabListPlus.getInstance().getLogger().log(Level.SEVERE,"An internal Error occured, please send the following Stacktrace to the developer to help resolving the problem",th);
                //BungeeTabListPlus.getInstance().getLogger().warning(th.toString() + th.getLocalizedMessage());
            }
        }
    }

}
