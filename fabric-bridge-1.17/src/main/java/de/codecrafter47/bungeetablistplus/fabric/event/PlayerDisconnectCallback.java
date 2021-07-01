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

package de.codecrafter47.bungeetablistplus.fabric.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.server.network.ServerPlayerEntity;

public interface PlayerDisconnectCallback {

    Event<PlayerDisconnectCallback> EVENT = EventFactory.createArrayBacked(PlayerDisconnectCallback.class, (listeners) -> {
        return (player) -> {
            PlayerDisconnectCallback[] var6 = listeners;
            int var7 = listeners.length;

            for (int i = 0; i < var7; i++) {
                PlayerDisconnectCallback event = var6[i];
                event.onDisconnect(player);
            }
        };
    });

    void onDisconnect(ServerPlayerEntity var1);
}
