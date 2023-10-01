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

package codecrafter47.bungeetablistplus.handler;

import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItem;
import lombok.SneakyThrows;

import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class LegacyTabOverlayHandlerImpl extends AbstractLegacyTabOverlayHandler {

    private final Player player;

    private boolean logVersionMismatch = false;

    public LegacyTabOverlayHandlerImpl(Logger logger, int playerListSize, Executor eventLoopExecutor, Player player, boolean is13OrLater) {
        super(logger, playerListSize, eventLoopExecutor, is13OrLater);
        this.player = player;
    }

    @SneakyThrows
    @Override
    protected void sendPacket(MinecraftPacket packet) {
        if ((packet instanceof LegacyPlayerListItem) && (player.getProtocolVersion().getProtocol() >= 761)) {
            // error
            if (!logVersionMismatch) {
                logVersionMismatch = true;
                this.logger.warning("Cannot correctly update tablist for player " + player.getUsername() + "\nThe client and server versions do not match. Client <= 1.7.10, server >= 1.19.3.\nUse ViaVersion on the spigot server for the best experience.");
            }
        } else {
            ReflectionUtil.getChannelWrapper(player).write(packet);
        }
    }
}
