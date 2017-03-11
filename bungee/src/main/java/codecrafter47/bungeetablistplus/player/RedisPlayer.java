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
import codecrafter47.bungeetablistplus.data.NullDataHolder;
import codecrafter47.bungeetablistplus.data.TrackingDataCache;
import de.codecrafter47.data.api.DataCache;
import de.codecrafter47.data.api.DataHolder;
import de.codecrafter47.data.api.DataKey;
import de.codecrafter47.data.bungee.api.BungeeData;
import de.codecrafter47.data.minecraft.api.MinecraftData;
import lombok.Getter;

import java.util.UUID;

public class RedisPlayer extends AbstractPlayer {
    @Getter
    private final DataCache data = new TrackingDataCache() {

        @Override
        protected <T> void addActiveKey(DataKey<T> key) {
            super.addActiveKey(key);
            BungeeTabListPlus.getInstance().getRedisPlayerManager().request(getUniqueID(), key);
        }
    };

    public RedisPlayer(UUID uuid, String name) {
        super(uuid, name);
    }

    @Override
    protected DataHolder getResponsibleDataHolder(DataKey<?> key) {

        if (key.getScope().equals(BungeeData.SCOPE_BUNGEE_PLAYER) || key.getScope().equals(MinecraftData.SCOPE_PLAYER)) {
            return data;
        }

        if (key.getScope().equals(MinecraftData.SCOPE_SERVER) || key.getScope().equals(BungeeData.SCOPE_BUNGEE_SERVER)) {
            return serverData;
        }

        BungeeTabListPlus.getInstance().getLogger().warning("Data key with unknown scope: " + key);
        return NullDataHolder.INSTANCE;
    }
}
