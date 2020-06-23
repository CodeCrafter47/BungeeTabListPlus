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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.handler.GetGamemodeLogic;
import codecrafter47.bungeetablistplus.handler.LegacyTabOverlayHandlerImpl;
import codecrafter47.bungeetablistplus.handler.LowMemoryTabOverlayHandlerImpl;
import codecrafter47.bungeetablistplus.handler.RewriteLogic;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListener;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import codecrafter47.bungeetablistplus.version.ProtocolVersionProvider;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.handler.TabOverlayHandler;
import de.codecrafter47.taboverlay.util.ChildLogger;
import io.netty.channel.EventLoop;
import net.md_5.bungee.ServerConnection;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.event.ServerSwitchEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.netty.ChannelWrapper;
import net.md_5.bungee.netty.PipelineUtils;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TabViewManager implements Listener {

    private final BungeeTabListPlus btlp;
    private final ProtocolVersionProvider protocolVersionProvider;

    private final Map<ProxiedPlayer, PlayerTabView> playerTabViewMap = new ConcurrentHashMap<>();

    public TabViewManager(BungeeTabListPlus btlp, ProtocolVersionProvider protocolVersionProvider) {
        this.btlp = btlp;
        this.protocolVersionProvider = protocolVersionProvider;
        btlp.getPlugin().getProxy().getPluginManager().registerListener(btlp.getPlugin(), this);
    }

    public TabView onPlayerJoin(ProxiedPlayer player) {
        if (playerTabViewMap.containsKey(player)) {
            throw new AssertionError("Duplicate PostLoginEvent for player " + player.getName());
        }

        if (player.getServer() != null) {
            throw new AssertionError("Player already connected to server in PostLoginEvent: " + player.getName());
        }

        PlayerTabView tabView = createTabView(player);
        playerTabViewMap.put(player, tabView);
        return tabView;
    }

    public TabView onPlayerDisconnect(ProxiedPlayer player) {
        PlayerTabView tabView = playerTabViewMap.remove(player);

        if (null == tabView) {
            throw new AssertionError("Received PlayerDisconnectEvent for non-existent player " + player.getName());
        }

        tabView.deactivate();

        return tabView;
    }

    @EventHandler
    public void onServerConnected(ServerSwitchEvent event) {
        try {
            ProxiedPlayer player = event.getPlayer();

            PlayerTabView tabView = playerTabViewMap.get(player);

            if (tabView == null) {
                throw new AssertionError("Received ServerSwitchEvent for non-existent player " + player.getName());
            }

            ServerConnection server = (ServerConnection) event.getPlayer().getServer();

            ChannelWrapper wrapper = server.getCh();

            PacketHandler packetHandler = tabView.packetHandler;
            PacketListener packetListener = new PacketListener(server, packetHandler, player);

            wrapper.getHandle().pipeline().addBefore(PipelineUtils.BOSS_HANDLER, "btlp-packet-listener", packetListener);

            packetHandler.onServerSwitch(protocolVersionProvider.has113OrLater(player));

        } catch (Exception ex) {
            btlp.getLogger().log(Level.SEVERE, "Failed to inject packet listener", ex);
        }
    }

    @Nullable
    public TabView getTabView(ProxiedPlayer player) {
        return playerTabViewMap.get(player);
    }

    private PlayerTabView createTabView(ProxiedPlayer player) {
        try {
            TabOverlayHandler tabOverlayHandler;
            PacketHandler packetHandler;

            Logger logger = new ChildLogger(btlp.getLogger(), player.getName());
            EventLoop eventLoop = ReflectionUtil.getChannelWrapper(player).getHandle().eventLoop();

            if (protocolVersionProvider.has18OrLater(player)) {
                LowMemoryTabOverlayHandlerImpl tabOverlayHandlerImpl = new LowMemoryTabOverlayHandlerImpl(logger, eventLoop, player.getUniqueId(), player, protocolVersionProvider.is18(player), protocolVersionProvider.has113OrLater(player));
                tabOverlayHandler = tabOverlayHandlerImpl;
                packetHandler = new RewriteLogic(new GetGamemodeLogic(tabOverlayHandlerImpl, ((UserConnection) player)));
            } else {
                LegacyTabOverlayHandlerImpl legacyTabOverlayHandler = new LegacyTabOverlayHandlerImpl(logger, player.getPendingConnection().getListener().getTabListSize(), eventLoop, player, protocolVersionProvider.has113OrLater(player));
                tabOverlayHandler = legacyTabOverlayHandler;
                packetHandler = legacyTabOverlayHandler;
            }

            return new PlayerTabView(tabOverlayHandler, logger, btlp.getAsyncExecutor(), packetHandler);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new AssertionError("Failed to create tab view", e);
        }
    }

    private static class PlayerTabView extends TabView {

        private final PacketHandler packetHandler;

        private PlayerTabView(TabOverlayHandler tabOverlayHandler, Logger logger, Executor updateExecutor, PacketHandler packetHandler) {
            super(tabOverlayHandler, logger, updateExecutor);
            this.packetHandler = packetHandler;
        }
    }
}
