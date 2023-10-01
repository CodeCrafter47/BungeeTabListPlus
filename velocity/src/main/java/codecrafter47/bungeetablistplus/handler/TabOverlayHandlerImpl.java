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

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.protocol.PacketListenerResult;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import com.velocitypowered.api.network.ProtocolVersion;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.protocol.MinecraftPacket;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfo;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfo;
import lombok.SneakyThrows;

import java.util.UUID;
import java.util.concurrent.Executor;
import java.util.logging.Logger;

public class TabOverlayHandlerImpl extends AbstractTabOverlayHandler {

    private final Player player;

    private boolean logVersionMismatch = false;

    public TabOverlayHandlerImpl(Logger logger, Executor eventLoopExecutor, UUID viewerUuid, Player player, boolean is18, boolean is13OrLater, boolean is119OrLater) {
        super(logger, eventLoopExecutor, viewerUuid, is18, is13OrLater, is119OrLater);
        this.player = player;
    }

    @SneakyThrows
    @Override
    protected void sendPacket(MinecraftPacket packet) {
        if ((packet instanceof UpsertPlayerInfo) && (player.getProtocolVersion().getProtocol() >= 761)) {
            // error
            if (!logVersionMismatch) {
                logVersionMismatch = true;
                this.logger.warning("Cannot correctly update tablist for player " + player.getUsername() + "\nThe client and server versions do not match. Client < 1.19.3, server >= 1.19.3.\nUse ViaVersion on the spigot server for the best experience.");
            }
        } else {
            ReflectionUtil.getChannelWrapper(player).write(packet);
        }
    }

    @Override
    protected boolean isExperimentalTabCompleteSmileys() {
        return BungeeTabListPlus.getInstance().getConfig().experimentalTabCompleteSmileys;
    }

    @Override
    protected boolean isExperimentalTabCompleteFixForTabSize80() {
        return BungeeTabListPlus.getInstance().getConfig().experimentalTabCompleteFixForTabSize80;
    }

    @SneakyThrows
    @Override
    protected boolean isUsingAltRespawn() {
        return player.getProtocolVersion().getProtocol() >= 735
                && ProtocolVersion.isSupported(736);
    }

    @Override
    public PacketListenerResult onPlayerListUpdatePacket(UpsertPlayerInfo packet) {
        return PacketListenerResult.PASS;
    }

    @Override
    public PacketListenerResult onPlayerListRemovePacket(RemovePlayerInfo packet) {
        return PacketListenerResult.PASS;
    }
}
