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
import codecrafter47.bungeetablistplus.api.bungee.CustomTablist;
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.managers.DataManager;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.Getter;
import lombok.Setter;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import javax.annotation.Nonnull;
import java.util.Objects;

public class BungeePlayer extends AbstractPlayer {

    @Nonnull
    private final ProxiedPlayer player;

    @Getter
    private final BukkitBridge.PlayerBridgeDataCache bridgeDataCache;

    @Getter
    private final DataManager.LocalDataCache localDataCache;

    @Getter
    @Setter
    private CustomTablist customTablist = null;

    /**
     * The player is no longer connected to the proxy, but a DisconnectEvent has not been called.
     */
    @Getter
    @Setter
    private boolean stale;

    public BungeePlayer(@Nonnull ProxiedPlayer player) {
        super(player.getUniqueId(), player.getName());
        this.player = player;
        this.localDataCache = BungeeTabListPlus.getInstance().getDataManager().createDataCacheForPlayer(this);
        this.bridgeDataCache = BungeeTabListPlus.getInstance().getBridge().createDataCacheForPlayer(this);
    }

    public ProxiedPlayer getPlayer() {
        return player;
    }

    @Override
    protected DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(BungeeData.SCOPE_BUNGEE_PLAYER)) {
            return localDataCache;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return bridgeDataCache;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER) || key.getScope().equals(BungeeData.SCOPE_BUNGEE_SERVER)) {
            return serverData;
        }

        BungeeTabListPlus.getInstance().getLogger().warning("Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BungeePlayer that = (BungeePlayer) o;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
