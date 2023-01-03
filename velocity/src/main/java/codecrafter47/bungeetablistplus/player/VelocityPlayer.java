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

package codecrafter47.bungeetablistplus.player;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.bridge.BukkitBridge;
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.managers.DataManager;
import com.velocitypowered.api.proxy.Player;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import de.codecrafter47.data.velocity.api.VelocityData;
import lombok.Getter;
import lombok.Setter;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.logging.Level;

public class VelocityPlayer extends AbstractPlayer {

    @Nonnull
    private final Player player;

    @Getter
    private final BukkitBridge.PlayerBridgeDataCache bridgeDataCache;

    @Getter
    private final DataManager.LocalDataCache localDataCache;

    /**
     * The player is no longer connected to the proxy, but a DisconnectEvent has not been called.
     */
    @Getter
    @Setter
    private boolean stale;

    public VelocityPlayer(@Nonnull Player player) {
        super(player.getUniqueId(), player.getUsername());
        this.player = player;
        this.localDataCache = BungeeTabListPlus.getInstance().getDataManager().createDataCacheForPlayer(this);
        this.bridgeDataCache = BungeeTabListPlus.getInstance().getBridge().createDataCacheForPlayer(this);
    }

    public Player getPlayer() {
        return player;
    }

    @Override
    protected DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(VelocityData.SCOPE_VELOCITY_PLAYER)) {
            return localDataCache;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return bridgeDataCache;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER) || key.getScope().equals(VelocityData.SCOPE_VELOCITY_SERVER)) {
            return serverData;
        }

        BungeeTabListPlus.getInstance().getLogger().log(Level.WARNING, "Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VelocityPlayer that = (VelocityPlayer) o;
        return player.equals(that.player);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player);
    }
}
