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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.Options;
import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.managers.DataManager;
import codecrafter47.bungeetablistplus.protocol.PacketHandler;
import codecrafter47.bungeetablistplus.tablisthandler.LegacyTabList;
import codecrafter47.bungeetablistplus.tablisthandler.LoggingTabListLogic;
import codecrafter47.bungeetablistplus.tablisthandler.PlayerTablistHandler;
import codecrafter47.bungeetablistplus.tablisthandler.logic.GetGamemodeLogic;
import codecrafter47.bungeetablistplus.tablisthandler.logic.LowMemoryTabListLogic;
import codecrafter47.bungeetablistplus.tablisthandler.logic.RewriteLogic;
import codecrafter47.bungeetablistplus.tablisthandler.logic.TabListLogic;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.Synchronized;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;

import java.util.UUID;

public class ConnectedPlayer extends AbstractPlayer {

    private final ProxiedPlayer player;

    private PacketHandler packetHandler = null;
    private PlayerTablistHandler playerTablistHandler = null;

    @Getter
    private final BukkitBridge.PlayerBridgeDataCache bridgeDataCache;

    @Getter
    private final DataManager.LocalDataCache localDataCache;

    @Getter
    @Setter
    private CustomTablist customTablist = null;

    public ConnectedPlayer(ProxiedPlayer player) {
        this.player = player;
        this.localDataCache = BungeeTabListPlus.getInstance().getDataManager().createDataCacheForPlayer(this);
        this.bridgeDataCache = BungeeTabListPlus.getInstance().getBridge().createDataCacheForPlayer(this);
    }

    @Override
    public String getName() {
        return player.getName();
    }

    @Override
    public UUID getUniqueID() {
        return player.getUniqueId();
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    @Synchronized
    public PacketHandler getPacketHandler() {
        if (packetHandler == null) {
            createTabListHandler();
        }
        return packetHandler;
    }

    @Synchronized
    public PlayerTablistHandler getPlayerTablistHandler() {
        if (playerTablistHandler == null) {
            createTabListHandler();
        }
        return playerTablistHandler;
    }

    @SneakyThrows
    private void createTabListHandler() {
        if (BungeeTabListPlus.getInstance().getProtocolVersionProvider().has18OrLater(getPlayer())) {
            TabListLogic tabListLogic;
            if (Options.DEBUG) {
                tabListLogic = new LoggingTabListLogic(null, getPlayer());
            } else {
                //TabListLogic tabListLogic = new TabListLogic(null, getPlayer());
                // TODO: revert this change as soon as the underlying issue is fixed
                tabListLogic = new LowMemoryTabListLogic(null, getPlayer());
            }
            playerTablistHandler = PlayerTablistHandler.create(getPlayer(), tabListLogic);
            packetHandler = new RewriteLogic(new GetGamemodeLogic(tabListLogic, ((UserConnection) getPlayer())));
            if (ReflectionUtil.getChannelWrapper(player).getHandle().eventLoop().inEventLoop()) {
                tabListLogic.onConnected();
            } else {
                ReflectionUtil.getChannelWrapper(player).getHandle().eventLoop().submit(tabListLogic::onConnected);
            }
        } else {
            LegacyTabList legacyTabList = new LegacyTabList(getPlayer(), getPlayer().getPendingConnection().getListener().getTabListSize());
            playerTablistHandler = PlayerTablistHandler.create(getPlayer(), legacyTabList);
            packetHandler = legacyTabList;
        }
    }

    private DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(BungeeData.SCOPE_BUNGEE_PLAYER)) {
            return localDataCache;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return bridgeDataCache;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER)) {
            Server server = player.getServer();
            if (server != null) {
                return BungeeTabListPlus.getInstance().getBridge().getServerDataHolder(server.getInfo().getName());
            }
            return NullDataHolder.INSTANCE;
        }

        BungeeTabListPlus.getInstance().getLogger().warning("Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }

    @Override
    public <V> V get(DataKey<V> key) {
        return getResponsibleDataHolder(key).get(key);
    }

    @Override
    public <T> void addDataChangeListener(DataKey<T> key, Runnable listener) {
        getResponsibleDataHolder(key).addDataChangeListener(key, listener);
    }

    @Override
    public <T> void removeDataChangeListener(DataKey<T> key, Runnable listener) {
        getResponsibleDataHolder(key).removeDataChangeListener(key, listener);
    }
}
