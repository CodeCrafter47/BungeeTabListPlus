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

package codecrafter47.bungeetablistplus.protocol;

import codecrafter47.bungeetablistplus.BungeeTabListPlus;
import codecrafter47.bungeetablistplus.util.ReflectionUtil;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.proxy.connection.backend.VelocityServerConnection;
import com.velocitypowered.proxy.protocol.packet.HeaderAndFooter;
import com.velocitypowered.proxy.protocol.packet.LegacyPlayerListItem;
import com.velocitypowered.proxy.protocol.packet.RemovePlayerInfo;
import com.velocitypowered.proxy.protocol.packet.Team;
import com.velocitypowered.proxy.protocol.packet.UpsertPlayerInfo;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class PacketListener extends MessageToMessageDecoder<PacketWrapper> {
    private final VelocityServerConnection connection;
    private final PacketHandler handler;
    private final Player player;

    public PacketListener(VelocityServerConnection connection, PacketHandler handler, Player player) {
        this.connection = connection;
        this.handler = handler;
        this.player = player;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, PacketWrapper packetWrapper, List<Object> out) {
        boolean shouldRelease = true;
        try {
            if (connection.isActive()) {
                if (packetWrapper.packet != null) {

                    PacketListenerResult result = PacketListenerResult.PASS;
                    boolean handled = false;

                    if (packetWrapper.packet instanceof Team) {
                        result = handler.onTeamPacket((Team) packetWrapper.packet);
                        if (result == PacketListenerResult.MODIFIED) {
                            ReflectionUtil.getChannelWrapper(player).getChannel().write(packetWrapper.packet);
                        }
                        if (result != PacketListenerResult.PASS) {
                            return;
                        }
                    } else if (packetWrapper.packet instanceof LegacyPlayerListItem) {
                        result = handler.onPlayerListPacket((LegacyPlayerListItem) packetWrapper.packet);
                        handled = true;
                    } else if (packetWrapper.packet instanceof HeaderAndFooter) {
                        result = handler.onPlayerListHeaderFooterPacket((HeaderAndFooter) packetWrapper.packet);
                        handled = true;
                    } else if (packetWrapper.packet instanceof UpsertPlayerInfo) {
                        result = handler.onPlayerListUpdatePacket((UpsertPlayerInfo) packetWrapper.packet);
                        handled = true;
                    } else if (packetWrapper.packet instanceof RemovePlayerInfo) {
                        result = handler.onPlayerListRemovePacket((RemovePlayerInfo) packetWrapper.packet);
                        handled = true;
                    }

                    if (handled) {
                        if (result != PacketListenerResult.CANCEL) {
                            ReflectionUtil.getChannelWrapper(player).getChannel().write(packetWrapper.packet);
                        }
                        return;
                    }
                }
            }
            out.add(packetWrapper);
            shouldRelease = false;
        } catch (Throwable th) {
            BungeeTabListPlus.getInstance().reportError(th);
        } finally {
            if (shouldRelease) {
                packetWrapper.trySingleRelease();
            }
        }
    }
}
