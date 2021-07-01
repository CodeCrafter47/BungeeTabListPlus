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

package de.codecrafter47.bungeetablistplus.fabric.mixin;

import de.codecrafter47.bungeetablistplus.fabric.event.PlayerDisconnectCallback;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Created by florian on 01.02.15.
 */
@Mixin(ServerPlayNetworkHandler.class)
public class MixinNetHandlerPlayServer {

    @Shadow
    public ServerPlayerEntity player;

    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void onDisconnect(Text var1, CallbackInfo callback) {
        PlayerDisconnectCallback.EVENT.invoker().onDisconnect(player);
    }
}
