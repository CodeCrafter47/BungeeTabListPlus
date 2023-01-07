/*
 *     Copyright (C) 2020 Florian Stober
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package codecrafter47.bungeetablistplus.listener;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.player.VelocityPlayer;
import codecrafter47.bungeetablistplus.tablist.ExcludedServersTabOverlayProvider;
import codecrafter47.bungeetablistplus.util.GeyserCompat;
import com.velocitypowered.api.event.PostOrder;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.DisconnectEvent;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.event.proxy.ProxyReloadEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.client.ConnectedPlayer;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.config.platform.EventListener;
import net.kyori.adventure.text.Component;

import java.util.concurrent.TimeUnit;

public class TabListListener {

    private final BungeeTabListPlus btlp;

    public TabListListener(BungeeTabListPlus btlp) {
        this.btlp = btlp;
    }

    @Subscribe(order = PostOrder.LATE)
    public void onPlayerJoin(PostLoginEvent e) {
        try {
            // Hacks -> Remove everyone from all tablists
            btlp.getProxy().getScheduler().buildTask(btlp.getPlugin(), () -> {
                for(Player tmpPlayer : btlp.getProxy().getAllPlayers()){
                    tmpPlayer.getTabList().clearAll();
                }
            }).delay(2, TimeUnit.SECONDS).schedule();

            VelocityPlayer player = btlp.getVelocityPlayerProvider().onPlayerConnected(e.getPlayer());
            
            if (GeyserCompat.isBedrockPlayer(e.getPlayer().getUniqueId())) {
                return;
            }
            
            TabView tabView = btlp.getTabViewManager().onPlayerJoin(e.getPlayer());
            tabView.getTabOverlayProviders().addProvider(new ExcludedServersTabOverlayProvider(player, btlp));
            for (EventListener listener : btlp.getListeners()) {
                listener.onTabViewAdded(tabView, player);
            }
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @Subscribe(order = PostOrder.FIRST)
    public void onPlayerDisconnect(DisconnectEvent e) {
        try {
            btlp.getVelocityPlayerProvider().onPlayerDisconnected(e.getPlayer());

            if (GeyserCompat.isBedrockPlayer(e.getPlayer().getUniqueId())) {
                return;
            }
            
            TabView tabView = btlp.getTabViewManager().onPlayerDisconnect(e.getPlayer());
            tabView.deactivate();
            for (EventListener listener : btlp.getListeners()) {
                listener.onTabViewRemoved(tabView);
            }

            // hack to revert changes from https://github.com/SpigotMC/BungeeCord/commit/830f18a35725f637d623594eaaad50b566376e59
            e.getPlayer().getCurrentServer().ifPresent(server -> server.getPlayer().disconnect(Component.text("Quitting")));
            ((ConnectedPlayer) e.getPlayer()).setConnectedServer(null);
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        }
    }

    @Subscribe
    public void onReload(ProxyReloadEvent event) {
        btlp.reload();
    }
}
