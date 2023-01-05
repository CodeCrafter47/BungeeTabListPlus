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

package codecrafter47.bungeetablistplus.managers;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.handler.*;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.protocol.PacketListener;
import codecrafter47.bungeetablistplus.util.GeyserCompat;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import codecrafter47.bungeetablistplus.version.ProtocolVersionProvider;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.player.ServerPostConnectEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.MinecraftConnection;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.network.Connections;
import de.codecrafter47.taboverlay.TabView;
import de.codecrafter47.taboverlay.config.misc.ChildLogger;
import de.codecrafter47.taboverlay.handler.TabOverlayHandler;
import io.netty.channel.EventLoop;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TabViewManager {

    private final BungeeTabListPlus btlp;
    private final ProtocolVersionProvider protocolVersionProvider;

    private final Map<Player, PlayerTabView> playerTabViewMap = new ConcurrentHashMap<>();

    public TabViewManager(BungeeTabListPlus btlp, ProtocolVersionProvider protocolVersionProvider) {
        this.btlp = btlp;
        this.protocolVersionProvider = protocolVersionProvider;
        btlp.getPlugin().getProxy().getEventManager().register(btlp.getPlugin(), this);
    }

    public TabView onPlayerJoin(Player player) {
        if (playerTabViewMap.containsKey(player)) {
            throw new AssertionError("Duplicate PostLoginEvent for player " + player.getUsername());
        }

        if (player.getCurrentServer().isPresent()) {
            throw new AssertionError("Player already connected to server in PostLoginEvent: " + player.getUsername());
        }

        PlayerTabView tabView = createTabView(player);
        playerTabViewMap.put(player, tabView);
        return tabView;
    }

    public TabView onPlayerDisconnect(Player player) {
        PlayerTabView tabView = playerTabViewMap.remove(player);

        if (null == tabView) {
            throw new AssertionError("Received PlayerDisconnectEvent for non-existent player " + player.getUsername());
        }

        tabView.deactivate();

        return tabView;
    }

    @Subscribe
    public void onServerConnected(ServerPostConnectEvent event) {
        if (GeyserCompat.isBedrockPlayer(event.getPlayer().getUniqueId())) {
            return;
        }
        try {
            Player player = event.getPlayer();

            PlayerTabView tabView = playerTabViewMap.get(player);

            if (tabView == null) {
                throw new AssertionError("Received ServerSwitchEvent for non-existent player " + player.getUsername());
            }

            VelocityServerConnection server = (VelocityServerConnection) event.getPlayer().getCurrentServer().orElse(null);

            MinecraftConnection wrapper = server.getConnection();

            PacketHandler packetHandler = tabView.packetHandler;
            PacketListener packetListener = new PacketListener(server, packetHandler, player);
            wrapper.getChannel().pipeline().addBefore(Connections.HANDLER, "btlp-packet-listener", packetListener);

            packetHandler.onServerSwitch(protocolVersionProvider.has113OrLater(player));

        } catch (Exception ex) {
            btlp.getLogger().log(Level.SEVERE, "Failed to inject packet listener", ex);
        }
    }

    @Nullable
    public TabView getTabView(Player player) {
        return playerTabViewMap.get(player);
    }

    private PlayerTabView createTabView(Player player) {
        try {
            TabOverlayHandler tabOverlayHandler;
            PacketHandler packetHandler;

            Logger logger = new ChildLogger(btlp.getLogger(), player.getUsername());
            EventLoop eventLoop = ReflectionUtil.getChannelWrapper(player).eventLoop();

            if (protocolVersionProvider.has1193OrLater(player)) {
                NewTabOverlayHandler handler = new NewTabOverlayHandler(logger, eventLoop, player);
                tabOverlayHandler = handler;
                packetHandler = new RewriteLogic(new GetGamemodeLogic(handler, player.getUniqueId()));
            } else if (protocolVersionProvider.has18OrLater(player)) {
                LowMemoryTabOverlayHandlerImpl tabOverlayHandlerImpl = new LowMemoryTabOverlayHandlerImpl(logger, eventLoop, player.getUniqueId(), player, protocolVersionProvider.is18(player), protocolVersionProvider.has113OrLater(player), protocolVersionProvider.has119OrLater(player));
                tabOverlayHandler = tabOverlayHandlerImpl;
                packetHandler = new RewriteLogic(new GetGamemodeLogic(tabOverlayHandlerImpl, player.getUniqueId()));
            } else {
                LegacyTabOverlayHandlerImpl legacyTabOverlayHandler = new LegacyTabOverlayHandlerImpl(logger, ReflectionUtil.getTablistHandler(player).getEntries().size(), eventLoop, player, protocolVersionProvider.has113OrLater(player));
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
