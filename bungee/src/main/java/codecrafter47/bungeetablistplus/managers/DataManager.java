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
import codecrafter47.bungeetablistplus.api.bungee.BungeeTabListPlusAPI;
import codecrafter47.bungeetablistplus.data.BTLPBungeeDataKeys;
import codecrafter47.bungeetablistplus.data.TrackingDataCache;
import codecrafter47.bungeetablistplus.player.ConnectedPlayer;
import de.codecrafter47.data.api.DataAccess;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.api.JoinedDataAccess;
import de.codecrafter47.data.bungee.AbstractBungeeDataAccess;
import de.codecrafter47.data.bungee.PlayerDataAccess;
import net.md_5.bungee.UserConnection;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class DataManager implements Listener {
    private final BungeeTabListPlus bungeeTabListPlus;

    private final DataAccess<ProxiedPlayer> playerDataAccess;

    public DataManager(BungeeTabListPlus bungeeTabListPlus) {
        this.bungeeTabListPlus = bungeeTabListPlus;

        Plugin plugin = bungeeTabListPlus.getPlugin();
        Logger logger = bungeeTabListPlus.getLogger();

        playerDataAccess = JoinedDataAccess.of(new PlayerDataAccess(plugin, logger),
                new LocalPlayerDataAccess(plugin, logger));

        ProxyServer.getInstance().getScheduler().schedule(plugin, this::updateData, 1, 1, TimeUnit.SECONDS);
    }

    public LocalDataCache createDataCacheForPlayer(ConnectedPlayer player) {
        return new LocalDataCache();
    }

    @SuppressWarnings("unchecked")
    private void updateData() {
        for (ConnectedPlayer player : bungeeTabListPlus.getConnectedPlayerManager().getPlayers()) {
            for (DataKey<?> dataKey : player.getLocalDataCache().getQueriedKeys()) {
                DataKey<Object> key = (DataKey<Object>) dataKey;
                updateIfNecessary(player, key, playerDataAccess.get(key, player.getPlayer()));
            }
        }
    }

    private <T> void updateIfNecessary(ConnectedPlayer player, DataKey<T> key, T value) {
        DataCache data = player.getLocalDataCache();
        if (!Objects.equals(data.get(key), value)) {
            bungeeTabListPlus.runInMainThread(() -> data.updateValue(key, value));
        }
    }

    public static class LocalPlayerDataAccess extends AbstractBungeeDataAccess<ProxiedPlayer> {

        public LocalPlayerDataAccess(Plugin plugin, Logger logger) {
            super(plugin, logger);

            addProvider(BTLPBungeeDataKeys.DATA_KEY_GAMEMODE, p -> ((UserConnection) p).getGamemode());
            addProvider(BTLPBungeeDataKeys.DATA_KEY_ICON, BungeeTabListPlusAPI::getIconFromPlayer);
        }
    }

    public static class LocalDataCache extends TrackingDataCache {

    }
}
